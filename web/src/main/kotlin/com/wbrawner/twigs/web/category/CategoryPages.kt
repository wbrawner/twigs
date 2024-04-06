package com.wbrawner.twigs.web.category

import com.wbrawner.twigs.service.budget.BudgetResponse
import com.wbrawner.twigs.service.category.CategoryResponse
import com.wbrawner.twigs.service.transaction.TransactionResponse
import com.wbrawner.twigs.service.user.UserResponse
import com.wbrawner.twigs.web.AuthenticatedPage
import com.wbrawner.twigs.web.budget.toCurrencyString
import java.text.NumberFormat

data class CategoryDetailsPage(
    val category: CategoryWithBalanceResponse,
    val budgets: List<BudgetResponse>,
    val transactionCount: String,
    val transactions: List<Map.Entry<String, List<TransactionListItem>>>,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = category.category.title
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

data class CategoryFormPage(
    val category: CategoryResponse,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = if (category.id.isBlank()) {
        "New Category"
    } else {
        "Edit Category"
    }
}

data class CategoryWithBalanceResponse(
    val category: CategoryResponse,
    val amountLabel: String,
    val balance: Long,
    val balanceLabel: String,
    val remainingAmountLabel: String,
)