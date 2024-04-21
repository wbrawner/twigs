package com.wbrawner.twigs.web.transaction

import com.wbrawner.twigs.service.HttpException
import com.wbrawner.twigs.service.budget.BudgetService
import com.wbrawner.twigs.service.category.CategoryService
import com.wbrawner.twigs.service.requireSession
import com.wbrawner.twigs.service.transaction.TransactionRequest
import com.wbrawner.twigs.service.transaction.TransactionResponse
import com.wbrawner.twigs.service.transaction.TransactionService
import com.wbrawner.twigs.service.user.UserResponse
import com.wbrawner.twigs.service.user.UserService
import com.wbrawner.twigs.toInstant
import com.wbrawner.twigs.web.NotFoundPage
import com.wbrawner.twigs.web.budget.toCurrencyString
import com.wbrawner.twigs.web.currencyFormat
import com.wbrawner.twigs.web.getAmount
import com.wbrawner.twigs.web.toDecimalString
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
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun Application.transactionWebRoutes(
    budgetService: BudgetService,
    categoryService: CategoryService,
    transactionService: TransactionService,
    userService: UserService
) {
    routing {
        authenticate(TWIGS_SESSION_COOKIE) {
            route("/budgets/{budgetId}/transactions") {
                get {
                    // TODO: Show transaction list here
                    val budgetId = call.parameters.getOrFail("budgetId")
                    call.respondRedirect("/budgets/$budgetId")
                }

                route("/new") {
                    get {
                        val user = userService.user(requireSession().userId)
                        val budgetId = call.parameters.getOrFail("budgetId")
                        val categoryId = call.request.queryParameters["categoryId"]
                        val transaction = TransactionResponse(
                            id = "",
                            title = "",
                            description = "",
                            amount = 0,
                            budgetId = budgetId,
                            expense = true,
                            date = Instant.now().toHtmlInputString(),
                            categoryId = categoryId,
                            createdBy = user.id
                        )
                        call.respond(
                            MustacheContent(
                                "transaction-form.mustache",
                                TransactionFormPage(
                                    transaction = transaction,
                                    amountLabel = 0L.toDecimalString(),
                                    budget = budgetService.budget(budgetId = budgetId, userId = user.id),
                                    categoryOptions = categoryOptions(
                                        transaction = transaction,
                                        categoryService = categoryService,
                                        budgetId = budgetId,
                                        user = user
                                    ),
                                    user = user
                                )
                            )
                        )
                    }

                    post {
                        val user = userService.user(requireSession().userId)
                        val urlBudgetId = call.parameters.getOrFail("budgetId")
                        try {
                            val request = call.receiveParameters().toTransactionRequest()
                                .run {
                                    copy(
                                        date = "$date:00Z",
                                        expense = categoryService.category(
                                            categoryId = requireNotNull(categoryId),
                                            userId = user.id
                                        ).expense,
                                        budgetId = urlBudgetId
                                    )
                                }
                            val transaction = transactionService.save(request, user.id)
                            call.respondRedirect("/budgets/${transaction.budgetId}/transactions/${transaction.id}")
                        } catch (e: HttpException) {
                            val transaction = TransactionResponse(
                                id = "",
                                title = call.parameters["title"],
                                description = call.parameters["description"],
                                amount = 0L,
                                budgetId = urlBudgetId,
                                expense = call.parameters["expense"]?.toBoolean() ?: true,
                                date = call.parameters["date"].orEmpty(),
                                categoryId = call.parameters["categoryId"],
                                createdBy = user.id
                            )
                            call.respond(
                                status = e.statusCode,
                                MustacheContent(
                                    "transaction-form.mustache",
                                    TransactionFormPage(
                                        transaction = transaction,
                                        amountLabel = call.parameters["amount"].orEmpty(),
                                        budget = budgetService.budget(budgetId = urlBudgetId, userId = user.id),
                                        categoryOptions = categoryOptions(
                                            transaction,
                                            categoryService,
                                            urlBudgetId,
                                            user
                                        ),
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
                            val transaction = transactionService.transaction(
                                transactionId = transactionId,
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
                            val dateFormat = DateTimeFormatter.ofPattern("H:mm a 'on' MMMM d, yyyy")
                            val transactionInstant = transaction.date.toInstant()
                            val transactionOffset = transactionInstant.atOffset(UTC)
                            val dateLabel = transactionOffset.format(dateFormat)
                            call.respond(
                                MustacheContent(
                                    "transaction-details.mustache", TransactionDetailsPage(
                                        transaction = transaction,
                                        category = category,
                                        budget = budget,
                                        budgets = budgets,
                                        amountLabel = transaction.amount?.toCurrencyString(currencyFormat).orEmpty(),
                                        dateLabel = dateLabel,
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
                            val transaction = transactionService.transaction(
                                transactionId = call.parameters.getOrFail("id"),
                                userId = user.id
                            )
                            call.respond(
                                MustacheContent(
                                    "transaction-form.mustache",
                                    TransactionFormPage(
                                        transaction = transaction.copy(
                                            date = transaction.date.toInstant().toHtmlInputString()
                                        ),
                                        amountLabel = transaction.amount.toDecimalString(),
                                        budget = budgetService.budget(budgetId = budgetId, userId = user.id),
                                        categoryOptions = categoryOptions(transaction, categoryService, budgetId, user),
                                        user = user
                                    )
                                )
                            )
                        }

                        post {
                            val user = userService.user(requireSession().userId)
                            val transactionId = call.parameters.getOrFail("id")
                            val urlBudgetId = call.parameters.getOrFail("budgetId")
                            try {
                                val request = call.receiveParameters().toTransactionRequest()
                                    .run {
                                        copy(
                                            date = "$date:00Z",
                                            expense = categoryService.category(
                                                categoryId = requireNotNull(categoryId),
                                                userId = user.id
                                            ).expense,
                                            budgetId = urlBudgetId
                                        )
                                    }
                                val transaction =
                                    transactionService.save(request, userId = user.id, transactionId = transactionId)
                                call.respondRedirect("/budgets/${transaction.budgetId}/transactions/${transaction.id}")
                            } catch (e: HttpException) {
                                val transaction = TransactionResponse(
                                    id = transactionId,
                                    title = call.parameters["title"],
                                    description = call.parameters["description"],
                                    amount = 0L,
                                    budgetId = urlBudgetId,
                                    expense = call.parameters["expense"]?.toBoolean() ?: true,
                                    date = call.parameters["date"].orEmpty(),
                                    categoryId = call.parameters["categoryId"],
                                    createdBy = user.id
                                )
                                call.respond(
                                    status = e.statusCode,
                                    MustacheContent(
                                        "transaction-form.mustache",
                                        TransactionFormPage(
                                            transaction = transaction,
                                            amountLabel = call.parameters["amount"].orEmpty(),
                                            budget = budgetService.budget(budgetId = urlBudgetId, userId = user.id),
                                            categoryOptions = categoryOptions(
                                                transaction,
                                                categoryService,
                                                urlBudgetId,
                                                user
                                            ),
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
                            transactionService.delete(transactionId = transactionId, userId = user.id)
                            call.respondRedirect("/budgets/${urlBudgetId}")
                        }
                    }
                }
            }
        }
    }
}

private suspend fun categoryOptions(
    transaction: TransactionResponse,
    categoryService: CategoryService,
    budgetId: String,
    user: UserResponse
): List<TransactionFormPage.CategoryOption> {
    val selectedCategoryId = transaction.categoryId.orEmpty()
    val categoryOptions = listOf(
        TransactionFormPage.CategoryOption(
            "",
            "Select a category",
            isSelected = transaction.categoryId.isNullOrBlank(),
            isDisabled = true
        ),
        TransactionFormPage.CategoryOption("income", "Income", isDisabled = true),
    )
        .plus(
            categoryService.categories(
                budgetIds = listOf(budgetId),
                userId = user.id,
                expense = false,
                archived = false
            ).map { category ->
                category.asOption(selectedCategoryId)
            }
        )
        .plus(
            TransactionFormPage.CategoryOption("expense", "Expense", isDisabled = true),
        )
        .plus(
            categoryService.categories(
                budgetIds = listOf(budgetId),
                userId = user.id,
                expense = true,
                archived = false
            ).map { category ->
                category.asOption(selectedCategoryId)
            }
        )
    return categoryOptions
}

private fun Parameters.toTransactionRequest() = TransactionRequest(
    title = get("title"),
    description = get("description"),
    amount = getAmount(),
    expense = false,
    date = get("date"),
    categoryId = get("categoryId"),
    budgetId = get("budgetId"),
)

private fun Instant.toHtmlInputString() = truncatedTo(ChronoUnit.MINUTES).toString().substringBefore(":00Z")
