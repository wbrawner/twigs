package com.wbrawner.twigs.web.recurring

import com.wbrawner.twigs.model.Frequency
import com.wbrawner.twigs.model.Time
import com.wbrawner.twigs.service.HttpException
import com.wbrawner.twigs.service.budget.BudgetService
import com.wbrawner.twigs.service.category.CategoryService
import com.wbrawner.twigs.service.recurringtransaction.RecurringTransactionRequest
import com.wbrawner.twigs.service.recurringtransaction.RecurringTransactionResponse
import com.wbrawner.twigs.service.recurringtransaction.RecurringTransactionService
import com.wbrawner.twigs.service.requireSession
import com.wbrawner.twigs.service.user.UserService
import com.wbrawner.twigs.toInstant
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
import java.time.Instant
import java.time.ZoneOffset.UTC

fun Application.recurringTransactionWebRoutes(
    budgetService: BudgetService,
    categoryService: CategoryService,
    recurringTransactionService: RecurringTransactionService,
    userService: UserService
) {
    routing {
        authenticate(TWIGS_SESSION_COOKIE) {
            route("/budgets/{budgetId}/recurring") {
                get {
                    val user = userService.user(requireSession().userId)
                    val budgetId = call.parameters.getOrFail("budgetId")
                    val budgets = budgetService.budgetsForUser(user.id)
                    val transactions = recurringTransactionService.recurringTransactions(
                        budgetId = budgetId,
                        userId = user.id
                    )
                    call.respond(
                        MustacheContent(
                            "recurring-transactions.mustache",
                            RecurringTransactionListPage(
                                budgets = budgets.map { it.toBudgetListItem(budgetId) },
                                budget = budgets.first { it.id == budgetId },
                                transactions = transactions.groupByOccurrence(),
                                user = user
                            )
                        )
                    )
                }

                route("/new") {
                    get {
                        val user = userService.user(requireSession().userId)
                        val budgetId = call.parameters.getOrFail("budgetId")
                        val budgets = budgetService.budgetsForUser(user.id)
                        val budget = budgets.first { it.id == budgetId }
                        val categoryId = call.request.queryParameters["categoryId"]
                        val transaction = RecurringTransactionResponse(
                            id = "",
                            title = "",
                            description = "",
                            amount = 0,
                            budgetId = budgetId,
                            expense = true,
                            categoryId = categoryId,
                            createdBy = user.id,
                            frequency = Frequency.Daily(1, Time(9, 0, 0)).toString(),
                            start = Instant.now().toString(),
                            finish = null,
                            lastRun = null
                        )
                        call.respond(
                            MustacheContent(
                                "recurring-transaction-form.mustache",
                                RecurringTransactionFormPage(
                                    transaction = transaction,
                                    amountLabel = 0L.toDecimalString(),
                                    budget = budget,
                                    categoryOptions = categoryOptions(
                                        selectedCategoryId = transaction.categoryId,
                                        categoryService = categoryService,
                                        budgetId = budgetId,
                                        user = user
                                    ),
                                    budgets = budgets.map { it.toBudgetListItem(budgetId) },
                                    user = user
                                )
                            )
                        )
                    }

                    post {
                        val user = userService.user(requireSession().userId)
                        val urlBudgetId = call.parameters.getOrFail("budgetId")
                        val budgets = budgetService.budgetsForUser(user.id)
                        val budget = budgets.first { it.id == urlBudgetId }
                        try {
                            val request = call.receiveParameters().toRecurringTransactionRequest()
                                .run {
                                    copy(
                                        expense = categoryService.category(
                                            categoryId = requireNotNull(categoryId),
                                            userId = user.id
                                        ).expense,
                                        budgetId = urlBudgetId
                                    )
                                }
                            val transaction = recurringTransactionService.save(request, user.id)
                            call.respondRedirect("/budgets/${transaction.budgetId}/recurring/${transaction.id}")
                        } catch (e: HttpException) {
                            val transaction = RecurringTransactionResponse(
                                id = "",
                                title = call.parameters["title"],
                                description = call.parameters["description"],
                                amount = call.parameters["amount"]?.toLongOrNull()?: 0L,
                                budgetId = urlBudgetId,
                                expense = call.parameters["expense"]?.toBoolean() ?: true,
                                start = call.parameters["start"].orEmpty(),
                                finish = call.parameters["finish"].orEmpty(),
                                frequency = call.parameters["frequency"].orEmpty(),
                                categoryId = call.parameters["categoryId"],
                                lastRun = null,
                                createdBy = user.id
                            )
                            call.respond(
                                status = e.statusCode,
                                MustacheContent(
                                    "recurring-transaction-form.mustache",
                                    RecurringTransactionFormPage(
                                        transaction = transaction,
                                        amountLabel = call.parameters["amount"].orEmpty(),
                                        budget = budget,
                                        categoryOptions = categoryOptions(
                                            transaction.categoryId,
                                            categoryService,
                                            urlBudgetId,
                                            user
                                        ),
                                        budgets = budgets.map { it.toBudgetListItem(urlBudgetId) },
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
                        val transactionId = call.parameters.getOrFail("id")
                        val budgetId = call.parameters.getOrFail("budgetId")
                        // TODO: Allow user-configurable locale
                        try {
                            val transaction = recurringTransactionService.recurringTransaction(
                                recurringTransactionId = transactionId,
                                userId = user.id
                            )
                            check(transaction.budgetId == budgetId) {
                                // TODO: redirect instead of error?
                                "Attempted to fetch transaction from wrong budget"
                            }
                            val category = transaction.categoryId?.let {
                                categoryService.category(categoryId = it, userId = user.id)
                            }
                            val budgets = budgetService.budgetsForUser(user.id)
                            val budget = budgets.first { it.id == budgetId }
                            val startLabel = transaction.start.toInstant()
                                .atOffset(UTC)
                                .run {
                                    shortDateFormat.format(this)
                                }
                            val finishLabel = transaction.finish?.toInstant()
                                ?.atOffset(UTC)
                                ?.run {
                                    shortDateFormat.format(this)
                                }
                                ?: "-"
                            call.respond(
                                MustacheContent(
                                    "recurring-transaction-details.mustache", RecurringTransactionDetailsPage(
                                        transaction = transaction,
                                        category = category,
                                        budget = budget,
                                        budgets = budgets.map { it.toBudgetListItem(budgetId) },
                                        amountLabel = transaction.amount?.toCurrencyString(currencyFormat).orEmpty(),
                                        startLabel = startLabel,
                                        finishLabel = finishLabel,
                                        createdBy = userService.user(transaction.createdBy),
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
                            val budgetId = call.parameters.getOrFail("budgetId")
                            val budgets = budgetService.budgetsForUser(user.id)
                            val budget = budgets.first { it.id == budgetId }
                            val transaction = recurringTransactionService.recurringTransaction(
                                recurringTransactionId = call.parameters.getOrFail("id"),
                                userId = user.id
                            )
                            call.respond(
                                MustacheContent(
                                    "recurring-transaction-form.mustache",
                                    RecurringTransactionFormPage(
                                        transaction = transaction.copy(
                                            start = transaction.start.toInstant().toHtmlInputString(),
                                            finish = transaction.finish?.toInstant()?.toHtmlInputString(),
                                        ),
                                        amountLabel = transaction.amount.toDecimalString(),
                                        budget = budget,
                                        categoryOptions = categoryOptions(transaction.categoryId, categoryService, budgetId, user),
                                        budgets = budgets.map { it.toBudgetListItem(budgetId) },
                                        user = user
                                    )
                                )
                            )
                        }

                        post {
                            val user = userService.user(requireSession().userId)
                            val transactionId = call.parameters.getOrFail("id")
                            val urlBudgetId = call.parameters.getOrFail("budgetId")
                            val budgets = budgetService.budgetsForUser(user.id)
                            val budget = budgets.first { it.id == urlBudgetId }
                            try {
                                val request = call.receiveParameters().toRecurringTransactionRequest()
                                    .run {
                                        copy(
                                            expense = categoryService.category(
                                                categoryId = requireNotNull(categoryId),
                                                userId = user.id
                                            ).expense,
                                            budgetId = urlBudgetId
                                        )
                                    }
                                val transaction = recurringTransactionService.save(
                                    request = request,
                                    userId = user.id,
                                    recurringTransactionId = transactionId
                                )
                                call.respondRedirect("/budgets/${transaction.budgetId}/recurring/${transaction.id}")
                            } catch (e: HttpException) {
                                val transaction = RecurringTransactionResponse(
                                    id = "",
                                    title = call.parameters["title"],
                                    description = call.parameters["description"],
                                    amount = call.parameters["amount"]?.toLongOrNull()?: 0L,
                                    budgetId = urlBudgetId,
                                    expense = call.parameters["expense"]?.toBoolean() ?: true,
                                    start = call.parameters["start"].orEmpty(),
                                    finish = call.parameters["finish"].orEmpty(),
                                    frequency = call.parameters["frequency"].orEmpty(),
                                    categoryId = call.parameters["categoryId"],
                                    lastRun = null,
                                    createdBy = user.id
                                )
                                call.respond(
                                    status = e.statusCode,
                                    MustacheContent(
                                        "recurring-transaction-form.mustache",
                                        RecurringTransactionFormPage(
                                            transaction = transaction,
                                            amountLabel = call.parameters["amount"].orEmpty(),
                                            budget = budget,
                                            categoryOptions = categoryOptions(
                                                transaction.categoryId,
                                                categoryService,
                                                urlBudgetId,
                                                user
                                            ),
                                            budgets = budgets.map { it.toBudgetListItem(urlBudgetId) },
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
                            val transactionId = call.parameters.getOrFail("id")
                            val urlBudgetId = call.parameters.getOrFail("budgetId")
                            recurringTransactionService.delete(recurringTransactionId = transactionId, userId = user.id)
                            call.respondRedirect("/budgets/${urlBudgetId}")
                        }
                    }
                }
            }
        }
    }
}

private fun Parameters.toRecurringTransactionRequest() = RecurringTransactionRequest(
    title = get("title"),
    description = get("description"),
    amount = getAmount(),
    expense = false,
    start = getDateString("start"),
    finish = getDateString("finish"),
    categoryId = get("categoryId"),
    budgetId = get("budgetId"),
    frequency = get("frequency").orEmpty()
)
