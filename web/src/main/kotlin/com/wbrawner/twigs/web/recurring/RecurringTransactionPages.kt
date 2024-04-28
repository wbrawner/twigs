package com.wbrawner.twigs.web.recurring

import com.wbrawner.twigs.service.budget.BudgetResponse
import com.wbrawner.twigs.service.category.CategoryResponse
import com.wbrawner.twigs.service.recurringtransaction.RecurringTransactionResponse
import com.wbrawner.twigs.service.user.UserResponse
import com.wbrawner.twigs.web.AuthenticatedPage
import com.wbrawner.twigs.web.BudgetListItem
import com.wbrawner.twigs.web.ListGroup
import com.wbrawner.twigs.web.budget.toCurrencyString
import com.wbrawner.twigs.web.category.CategoryOption
import com.wbrawner.twigs.web.transaction.TransactionListItem
import java.text.NumberFormat

data class RecurringTransactionListPage(
    val budget: BudgetResponse,
    val transactions: List<ListGroup<TransactionListItem>>,
    override val budgets: List<BudgetListItem>,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = "Recurring Transactions"
}

fun RecurringTransactionResponse.toListItem(numberFormat: NumberFormat) = TransactionListItem(
    id = id,
    title = title.orEmpty(),
    description = description.orEmpty(),
    budgetId = budgetId,
    expenseClass = if (expense != false) "expense" else "income",
    amountLabel = (amount ?: 0L).toCurrencyString(numberFormat)
)

data class RecurringTransactionDetailsPage(
    val transaction: RecurringTransactionResponse,
    val category: CategoryResponse?,
    val budget: BudgetResponse,
    val amountLabel: String,
    val dateLabel: String,
    val createdBy: UserResponse,
    override val budgets: List<BudgetListItem>,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = transaction.title.orEmpty()
}

data class RecurringTransactionFormPage(
    val transaction: RecurringTransactionResponse,
    val amountLabel: String,
    val budget: BudgetResponse,
    val categoryOptions: List<CategoryOption>,
    override val budgets: List<BudgetListItem>,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = if (transaction.id.isBlank()) {
        "New Recurring Transaction"
    } else {
        "Edit Recurring Transaction"
    }
}