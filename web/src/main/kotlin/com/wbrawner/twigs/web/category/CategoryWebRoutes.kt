package com.wbrawner.twigs.web.category

import com.wbrawner.twigs.endOfMonth
import com.wbrawner.twigs.firstOfMonth
import com.wbrawner.twigs.service.HttpException
import com.wbrawner.twigs.service.budget.BudgetService
import com.wbrawner.twigs.service.category.CategoryRequest
import com.wbrawner.twigs.service.category.CategoryResponse
import com.wbrawner.twigs.service.category.CategoryService
import com.wbrawner.twigs.service.requireSession
import com.wbrawner.twigs.service.transaction.TransactionService
import com.wbrawner.twigs.service.user.UserService
import com.wbrawner.twigs.toInstantOrNull
import com.wbrawner.twigs.web.*
import com.wbrawner.twigs.web.budget.toCurrencyString
import com.wbrawner.twigs.web.user.TWIGS_SESSION_COOKIE
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.mustache.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import java.text.NumberFormat
import java.util.*
import kotlin.math.abs

fun Application.categoryWebRoutes(
    budgetService: BudgetService,
    categoryService: CategoryService,
    transactionService: TransactionService,
    userService: UserService
) {
    routing {
        authenticate(TWIGS_SESSION_COOKIE) {
            route("/budgets/{budgetId}/categories") {
                get {
                    val budgetId = call.parameters.getOrFail("budgetId")
                    call.respondRedirect("/budgets/$budgetId")
                }

                route("/new") {
                    get {
                        val user = userService.user(requireSession().userId)
                        val budgetId = call.parameters.getOrFail("budgetId")
                        val budgets = budgetService.budgetsForUser(user.id)
                        val budget = budgets.first { it.id == budgetId }
                        call.respond(
                            MustacheContent(
                                "category-form.mustache",
                                CategoryFormPage(
                                    CategoryResponse(
                                        id = "",
                                        title = "",
                                        description = "",
                                        amount = 0,
                                        budgetId = budgetId,
                                        expense = call.request.queryParameters["expense"]?.toBoolean() ?: true,
                                        archived = false,
                                    ),
                                    amountLabel = 0L.toDecimalString(),
                                    budget = budget,
                                    budgets = budgets.map { it.toBudgetListItem(budgetId) },
                                    user = user
                                )
                            )
                        )
                    }

                    post {
                        val user = userService.user(requireSession().userId)
                        val budgetId = call.parameters.getOrFail("budgetId")
                        val budgets = budgetService.budgetsForUser(user.id)
                        val budget = budgets.first { it.id == budgetId }
                        try {
                            val request = call.receiveParameters().toCategoryRequest(budgetId)
                            val category = categoryService.save(request, user.id)
                            call.respondRedirect("/budgets/${category.budgetId}/categories/${category.id}")
                        } catch (e: HttpException) {
                            call.respond(
                                status = e.statusCode,
                                MustacheContent(
                                    "category-form.mustache",
                                    CategoryFormPage(
                                        CategoryResponse(
                                            id = "",
                                            title = call.parameters["title"].orEmpty(),
                                            description = call.parameters["description"].orEmpty(),
                                            amount = 0L,
                                            expense = call.parameters["expense"]?.toBoolean() ?: false,
                                            archived = call.parameters["archived"]?.toBoolean() ?: false,
                                            budgetId = budgetId
                                        ),
                                        amountLabel = call.parameters["amount"]?.toLongOrNull().toDecimalString(),
                                        budget = budget,
                                        budgets = budgets.map { it.toBudgetListItem(budgetId) },
                                        user = user,
                                        error = e.message
                                    )
                                )
                            )
                        }
                    }
                }

                route("/{id}") {
                    get {
                        val user = userService.user(requireSession().userId)
                        val categoryId = call.parameters.getOrFail("id")
                        try {
                            val category = categoryService.category(categoryId = categoryId, userId = user.id)
                            val categoryBalance =
                                abs(transactionService.sum(categoryId = category.id, userId = user.id))
                            val categoryWithBalance = CategoryWithBalanceResponse(
                                category = category,
                                amountLabel = category.amount.toCurrencyString(currencyFormat),
                                balance = categoryBalance,
                                balanceLabel = categoryBalance.toCurrencyString(currencyFormat),
                                remainingAmountLabel = (category.amount - categoryBalance).toCurrencyString(
                                    currencyFormat
                                )
                            )
                            val transactions = transactionService.transactions(
                                budgetIds = listOf(category.budgetId),
                                categoryIds = listOf(category.id),
                                from = call.parameters["from"]?.toInstantOrNull() ?: firstOfMonth,
                                to = call.parameters["to"]?.toInstantOrNull() ?: endOfMonth,
                                userId = user.id
                            )
                            val transactionCount = NumberFormat.getNumberInstance(Locale.US)
                                .format(transactions.size)
                            val budgets = budgetService.budgetsForUser(user.id)
                            val budgetId = call.parameters.getOrFail("budgetId")
                            val budget = budgets.first { it.id == budgetId }
                            call.respond(
                                MustacheContent(
                                    "category-details.mustache", CategoryDetailsPage(
                                        category = categoryWithBalance,
                                        transactions = transactions.groupByDate(),
                                        transactionCount = transactionCount,
                                        budgets = budgets.map { it.toBudgetListItem(budgetId) },
                                        budget = budget,
                                        user = user
                                    )
                                )
                            )
                        } catch (e: HttpException) {
                            call.respond(
                                status = e.statusCode,
                                MustacheContent("404.mustache", NotFoundPage)
                            )
                        }
                    }

                    route("/edit") {
                        get {
                            val user = userService.user(requireSession().userId)
                            val category = categoryService.category(
                                categoryId = call.parameters.getOrFail("id"),
                                userId = user.id
                            )
                            val budgetId = call.parameters.getOrFail("budgetId")
                            val budgets = budgetService.budgetsForUser(user.id)
                            call.respond(
                                MustacheContent(
                                    "category-form.mustache",
                                    CategoryFormPage(
                                        category = category,
                                        amountLabel = category.amount.toDecimalString(),
                                        budget = budgets.first { it.id == budgetId },
                                        budgets = budgets.map { it.toBudgetListItem(budgetId) },
                                        user = user
                                    )
                                )
                            )
                        }

                        post {
                            val user = userService.user(requireSession().userId)
                            val budgetId = call.parameters.getOrFail("budgetId")
                            val budgets = budgetService.budgetsForUser(user.id)
                            val categoryId = call.parameters.getOrFail("id")
                            try {
                                val request = call.receiveParameters().toCategoryRequest(budgetId)
                                val category = categoryService.save(request, userId = user.id, categoryId = categoryId)
                                call.respondRedirect("/budgets/${category.budgetId}/categories/${category.id}")
                            } catch (e: HttpException) {
                                call.respond(
                                    status = e.statusCode,
                                    MustacheContent(
                                        "category-form.mustache",
                                        CategoryFormPage(
                                            CategoryResponse(
                                                id = "",
                                                title = call.parameters["title"].orEmpty(),
                                                description = call.parameters["description"].orEmpty(),
                                                amount = 0L,
                                                expense = call.parameters["expense"]?.toBoolean() ?: false,
                                                archived = call.parameters["archived"]?.toBoolean() ?: false,
                                                budgetId = budgetId
                                            ),
                                            amountLabel = call.parameters["amount"]?.toLongOrNull().toDecimalString(),
                                            budget = budgets.first { it.id == budgetId },
                                            budgets = budgets.map { it.toBudgetListItem(budgetId) },
                                            user = user,
                                            error = e.message
                                        )
                                    )
                                )
                            }
                        }
                    }

                    route("/delete") {
                        post {
                            val user = userService.user(requireSession().userId)
                            val categoryId = call.parameters.getOrFail("id")
                            categoryService.delete(categoryId = categoryId, userId = user.id)
                            val budgetId = call.parameters.getOrFail("budgetId")
                            call.respondRedirect("/budgets/$budgetId")
                        }
                    }
                }
            }
        }
    }
}

private fun Parameters.toCategoryRequest(budgetId: String) = CategoryRequest(
    title = get("title"),
    description = get("description"),
    amount = getAmount(),
    expense = get("expense")?.toBoolean(),
    archived = get("archived") == "on",
    budgetId = budgetId
)