package com.wbrawner.twigs.web.budget

import com.wbrawner.twigs.endOfMonth
import com.wbrawner.twigs.firstOfMonth
import com.wbrawner.twigs.service.HttpException
import com.wbrawner.twigs.service.budget.BudgetRequest
import com.wbrawner.twigs.service.budget.BudgetResponse
import com.wbrawner.twigs.service.budget.BudgetService
import com.wbrawner.twigs.service.category.CategoryService
import com.wbrawner.twigs.service.requireSession
import com.wbrawner.twigs.service.transaction.BalanceResponse
import com.wbrawner.twigs.service.transaction.TransactionService
import com.wbrawner.twigs.service.user.UserService
import com.wbrawner.twigs.toInstantOrNull
import com.wbrawner.twigs.web.user.TWIGS_SESSION_COOKIE
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.mustache.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Application.budgetWebRoutes(
    budgetService: BudgetService,
    categoryService: CategoryService,
    transactionService: TransactionService,
    userService: UserService
) {
    routing {
        authenticate(TWIGS_SESSION_COOKIE) {
            route("/budgets") {
                get {
                    val user = userService.user(requireSession().userId)
                    val budgets = budgetService.budgetsForUser(user.id)
                    call.respond(MustacheContent("budgets.mustache", BudgetListPage(budgets, user)))
                }

                route("/new") {
                    get {
                        val user = userService.user(requireSession().userId)
                        call.respond(
                            MustacheContent(
                                "budget-form.mustache",
                                BudgetFormPage(
                                    BudgetResponse(
                                        id = "",
                                        name = "",
                                        description = "",
                                        users = listOf()
                                    ),
                                    user
                                )
                            )
                        )
                    }

                    post {
                        val user = userService.user(requireSession().userId)
                        try {
                            val request = call.receiveParameters().toBudgetRequest()
                            val budget = budgetService.save(request, user.id)
                            call.respondRedirect("/budgets/${budget.id}")
                        } catch (e: HttpException) {
                            call.respond(
                                status = e.statusCode,
                                MustacheContent(
                                    "budget-form.mustache",
                                    BudgetFormPage(
                                        BudgetResponse(
                                            id = "",
                                            name = call.parameters["name"].orEmpty(),
                                            description = call.parameters["description"].orEmpty(),
                                            users = listOf()
                                        ),
                                        user,
                                        e.message
                                    )
                                )
                            )
                        }
                    }
                }

                route("/{id}") {

                    get {
                        val user = userService.user(requireSession().userId)
                        val budget = budgetService.budget(budgetId = call.parameters.getOrFail("id"), userId = user.id)
                        val balance = BalanceResponse(transactionService.sum(budgetId = budget.id, userId = user.id))
                        val categories = categoryService.categories(budgetIds = listOf(budget.id), userId = user.id)
                            .map { category ->
                                BudgetDetailsPage.CategoryWithBalanceResponse(
                                    category,
                                    BalanceResponse(transactionService.sum(categoryId = category.id, userId = user.id))
                                )
                            }
                        // TODO: Add a count method so we don't have to do this
                        val transactions = transactionService.transactions(
                            budgetIds = listOf(budget.id),
                            from = call.parameters["from"]?.toInstantOrNull() ?: firstOfMonth,
                            to = call.parameters["to"]?.toInstantOrNull() ?: endOfMonth,
                            userId = user.id
                        )
                        call.respond(
                            MustacheContent(
                                "budget-details.mustache", BudgetDetailsPage(
                                    budget = budget,
                                    balance = balance,
                                    categories = categories.filter { !it.category.archived },
                                    archivedCategories = categories.filter { it.category.archived },
                                    transactionCount = transactions.size.toLong(),
                                    user = user
                                )
                            )
                        )
                    }

                    route("/edit") {
                        get {
                            val user = userService.user(requireSession().userId)
                            val budget = budgetService.budget(
                                budgetId = call.parameters.getOrFail("id"),
                                userId = user.id
                            )
                            call.respond(
                                MustacheContent(
                                    "budget-form.mustache",
                                    BudgetFormPage(budget, user)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Parameters.toBudgetRequest() = BudgetRequest(
    name = get("name"),
    description = get("description"),
    users = setOf() // TODO: Enable adding users at budget creation
)
