package com.wbrawner.twigs.web.transaction

import com.wbrawner.twigs.service.budget.BudgetResponse
import com.wbrawner.twigs.service.category.CategoryResponse
import com.wbrawner.twigs.service.transaction.TransactionResponse
import com.wbrawner.twigs.service.user.UserResponse
import com.wbrawner.twigs.web.AuthenticatedPage

data class TransactionDetailsPage(
    val transaction: TransactionResponse,
    val category: CategoryResponse?,
    val budget: BudgetResponse,
    val amountLabel: String,
    val dateLabel: String,
    val budgets: List<BudgetResponse>,
    val createdBy: UserResponse,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = transaction.title.orEmpty()
}

data class TransactionFormPage(
    val transaction: TransactionResponse,
    val budget: BudgetResponse,
    val incomeCategories: List<CategoryResponse>,
    val expenseCategories: List<CategoryResponse>,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = if (transaction.id.isBlank()) {
        "New Category"
    } else {
        "Edit Category"
    }
}
