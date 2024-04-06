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
import com.wbrawner.twigs.toInstant
import com.wbrawner.twigs.toInstantOrNull
import com.wbrawner.twigs.web.NotFoundPage
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
import io.ktor.util.date.*
import java.text.DateFormat
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
                                        expense = true,
                                        archived = false,
                                    ),
                                    budget = budgetService.budget(budgetId = budgetId, userId = user.id),
                                    user = user
                                )
                            )
                        )
                    }

                    post {
                        val user = userService.user(requireSession().userId)
                        val budgetId = call.parameters.getOrFail("budgetId")
                        try {
                            val request = call.receiveParameters().toCategoryRequest()
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
                                            amount = call.parameters["amount"]?.toLongOrNull() ?: 0L,
                                            expense = call.parameters["expense"]?.toBoolean() ?: false,
                                            archived = call.parameters["archived"]?.toBoolean() ?: false,
                                            budgetId = budgetId
                                        ),
                                        budget = budgetService.budget(budgetId = budgetId, userId = user.id),
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
                        // TODO: Allow user-configurable locale
                        val numberFormat = NumberFormat.getCurrencyInstance(Locale.US)
                        try {
                            val category = categoryService.category(categoryId = categoryId, userId = user.id)
                            val categoryBalance =
                                abs(transactionService.sum(categoryId = category.id, userId = user.id))
                            val categoryWithBalance = CategoryWithBalanceResponse(
                                category = category,
                                amountLabel = category.amount.toCurrencyString(numberFormat),
                                balance = categoryBalance,
                                balanceLabel = categoryBalance.toCurrencyString(numberFormat),
                                remainingAmountLabel = (category.amount - categoryBalance).toCurrencyString(numberFormat)
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
                            val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US)
                            val transactionsByDate = transactions.groupBy {
                                dateFormat.format(it.date.toInstant().toGMTDate().toJvmDate())
                            }
                                .mapValues { (_, transactions) -> transactions.map { it.toListItem(numberFormat) } }
                                .entries
                                .sortedBy { it.key }
                            val budgets = budgetService.budgetsForUser(user.id)
                            val budgetId = call.parameters.getOrFail("budgetId")
                            val budget = budgets.first { it.id == budgetId }
                            call.respond(
                                MustacheContent(
                                    "category-details.mustache", CategoryDetailsPage(
                                        category = categoryWithBalance,
                                        transactions = transactionsByDate,
                                        transactionCount = transactionCount,
                                        budgets = budgets,
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
                            val budget = budgetService.budget(budgetId = budgetId, userId = user.id)
                            call.respond(
                                MustacheContent(
                                    "category-form.mustache",
                                    CategoryFormPage(category, budget, user)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Parameters.toCategoryRequest() = CategoryRequest(
    title = get("title"),
    description = get("description"),
    amount = get("amount")?.toLongOrNull(),
    expense = get("expense")?.toBoolean(),
    archived = get("archived")?.toBoolean(),
    budgetId = get("budgetId"),
)