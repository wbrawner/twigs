package com.wbrawner.twigs.web.transaction

import com.wbrawner.twigs.service.budget.BudgetResponse
import com.wbrawner.twigs.service.category.CategoryResponse
import com.wbrawner.twigs.service.transaction.TransactionResponse
import com.wbrawner.twigs.service.user.UserResponse
import com.wbrawner.twigs.web.AuthenticatedPage
import com.wbrawner.twigs.web.BudgetListItem
import com.wbrawner.twigs.web.ListGroup
import com.wbrawner.twigs.web.budget.toCurrencyString
import com.wbrawner.twigs.web.category.CategoryOption
import java.text.NumberFormat

data class TransactionListPage(
    val budget: BudgetResponse,
    val transactions: List<ListGroup<TransactionListItem>>,
    override val budgets: List<BudgetListItem>,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = "Transactions"
}


data class TransactionDetailsPage(
    val transaction: TransactionResponse,
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

data class TransactionFormPage(
    val transaction: TransactionResponse,
    val amountLabel: String,
    val budget: BudgetResponse,
    val categoryOptions: List<CategoryOption>,
    override val budgets: List<BudgetListItem>,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = if (transaction.id.isBlank()) {
        "New Transaction"
    } else {
        "Edit Transaction"
    }
}

data class TransactionListItem(
    val id: String,
    val title: String,
    val description: String,
    val budgetId: String,
    val expenseClass: String,
    val amountLabel: String
)

fun TransactionResponse.toListItem(numberFormat: NumberFormat) = TransactionListItem(
    id,
    title.orEmpty(),
    description.orEmpty(),
    budgetId,
    if (expense != false) "expense" else "income",
    (amount ?: 0L).toCurrencyString(numberFormat)
)
