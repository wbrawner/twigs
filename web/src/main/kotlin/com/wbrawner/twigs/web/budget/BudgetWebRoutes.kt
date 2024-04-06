package com.wbrawner.twigs.web.budget

import com.wbrawner.twigs.endOfMonth
import com.wbrawner.twigs.firstOfMonth
import com.wbrawner.twigs.service.HttpException
import com.wbrawner.twigs.service.budget.BudgetRequest
import com.wbrawner.twigs.service.budget.BudgetResponse
import com.wbrawner.twigs.service.budget.BudgetService
import com.wbrawner.twigs.service.category.CategoryService
import com.wbrawner.twigs.service.requireSession
import com.wbrawner.twigs.service.transaction.TransactionService
import com.wbrawner.twigs.service.user.UserService
import com.wbrawner.twigs.toInstantOrNull
import com.wbrawner.twigs.web.NotFoundPage
import com.wbrawner.twigs.web.category.CategoryWithBalanceResponse
import com.wbrawner.twigs.web.user.TWIGS_SESSION_COOKIE
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.mustache.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs

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
                    val budgets = budgetService.budgetsForUser(user.id).map { it.toBudgetListItem() }
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
                        val budgetId = call.parameters.getOrFail("id")
                        val budgets = budgetService.budgetsForUser(userId = user.id).toMutableList()
                        val budget = budgets.firstOrNull { it.id == budgetId }
                            ?: run {
                                call.respond(MustacheContent("404.mustache", NotFoundPage))
                                return@get
                            }
                        val numberFormat = NumberFormat.getCurrencyInstance(Locale.US)
                        val categories = categoryService.categories(budgetIds = listOf(budget.id), userId = user.id)
                            .map { category ->
                                val categoryBalance =
                                    abs(transactionService.sum(categoryId = category.id, userId = user.id))
                                CategoryWithBalanceResponse(
                                    category = category,
                                    amountLabel = category.amount.toCurrencyString(numberFormat),
                                    balance = categoryBalance,
                                    balanceLabel = categoryBalance.toCurrencyString(numberFormat),
                                    remainingAmountLabel = (category.amount - categoryBalance).toCurrencyString(
                                        numberFormat
                                    )
                                )
                            }
                            .toMutableSet()
                        val incomeCategories = categories.extractIf { !it.category.expense && !it.category.archived }
                        val archivedIncomeCategories =
                            categories.extractIf { !it.category.expense && it.category.archived }
                        val expenseCategories = categories.extractIf { it.category.expense && !it.category.archived }
                        val archivedExpenseCategories =
                            categories.extractIf { it.category.expense && it.category.archived }
                        val transactions = transactionService.transactions(
                            budgetIds = listOf(budget.id),
                            from = call.parameters["from"]?.toInstantOrNull() ?: firstOfMonth,
                            to = call.parameters["to"]?.toInstantOrNull() ?: endOfMonth,
                            userId = user.id
                        )
                        // TODO: Allow user-configurable locale
                        val budgetBalance = transactionService.sum(budgetId = budget.id, userId = user.id)
                            .toCurrencyString(numberFormat)
                        val expectedIncome = incomeCategories.sumOf { it.category.amount }
                        val actualIncome = transactions.sumOf { if (it.expense == false) it.amount ?: 0L else 0L }
                        val expectedExpenses = expenseCategories.sumOf { it.category.amount }
                        val actualExpenses = transactions.sumOf { if (it.expense == true) it.amount ?: 0L else 0L }
                        val balances = BudgetBalances(
                            cashFlow = budgetBalance,
                            expectedIncome = expectedIncome,
                            expectedIncomeLabel = expectedIncome.toCurrencyString(numberFormat),
                            actualIncome = actualIncome,
                            actualIncomeLabel = actualIncome.toCurrencyString(numberFormat),
                            expectedExpenses = expectedExpenses,
                            expectedExpensesLabel = expectedExpenses.toCurrencyString(numberFormat),
                            actualExpenses = actualExpenses,
                            actualExpensesLabel = actualExpenses.toCurrencyString(numberFormat),
                        )
                        call.respond(
                            MustacheContent(
                                "budget-details.mustache", BudgetDetailsPage(
                                    budgets = budgets.map { it.toBudgetListItem(budgetId) }.sortedBy { it.name },
                                    budget = budget,
                                    balances = balances,
                                    incomeCategories = incomeCategories,
                                    archivedIncomeCategories = archivedIncomeCategories,
                                    expenseCategories = expenseCategories,
                                    archivedExpenseCategories = archivedExpenseCategories,
                                    transactionCount = NumberFormat.getNumberInstance(Locale.US)
                                        .format(transactions.size),
                                    monthAndYear = YearMonth.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")),
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

data class BudgetBalances(
    val cashFlow: String,
    val expectedIncome: Long,
    val expectedIncomeLabel: String,
    val actualIncome: Long,
    val actualIncomeLabel: String,
    val expectedExpenses: Long,
    val expectedExpensesLabel: String,
    val actualExpenses: Long,
    val actualExpensesLabel: String,
) {
    val maxProgressBarValue: Long = maxOf(expectedExpenses, expectedIncome, actualIncome, actualExpenses)
}

data class BudgetListItem(val id: String, val name: String, val description: String, val selected: Boolean)

private fun BudgetResponse.toBudgetListItem(selectedId: String? = null) = BudgetListItem(
    id = id,
    name = name.orEmpty(),
    description = description.orEmpty(),
    selected = id == selectedId
)

private fun Parameters.toBudgetRequest() = BudgetRequest(
    name = get("name"),
    description = get("description"),
    users = setOf() // TODO: Enable adding users at budget creation
)

private fun <T> MutableCollection<T>.extractIf(predicate: (T) -> Boolean): List<T> {
    val extracted = mutableListOf<T>()
    val iterator = iterator()
    while (iterator.hasNext()) {
        val item = iterator.next()
        if (predicate(item)) {
            extracted.add(item)
            iterator.remove()
        }
    }
    return extracted
}

fun Long.toCurrencyString(formatter: NumberFormat): String = formatter.format(
    this.toBigDecimal().divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
)