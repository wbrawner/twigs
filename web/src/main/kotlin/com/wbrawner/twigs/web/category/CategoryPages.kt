package com.wbrawner.twigs.web.category

import com.wbrawner.twigs.service.budget.BudgetResponse
import com.wbrawner.twigs.service.category.CategoryResponse
import com.wbrawner.twigs.service.user.UserResponse
import com.wbrawner.twigs.web.AuthenticatedPage
import com.wbrawner.twigs.web.BudgetListItem
import com.wbrawner.twigs.web.ListGroup
import com.wbrawner.twigs.web.transaction.TransactionListItem

data class CategoryDetailsPage(
    val category: CategoryWithBalanceResponse,
    val budget: BudgetResponse,
    val transactionCount: String,
    val transactions: List<ListGroup<TransactionListItem>>,
    override val budgets: List<BudgetListItem>,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = category.category.title
}

data class CategoryFormPage(
    val category: CategoryResponse,
    val amountLabel: String,
    val budget: BudgetResponse,
    override val budgets: List<BudgetListItem>,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = if (category.id.isBlank()) {
        "New Category"
    } else {
        "Edit Category"
    }

    val expenseChecked: String = if (category.expense) "checked" else ""
    val incomeChecked: String = if (!category.expense) "checked" else ""
    val archivedChecked: String = if (category.archived) "checked" else ""
}

data class CategoryWithBalanceResponse(
    val category: CategoryResponse,
    val amountLabel: String,
    val balance: Long,
    val balanceLabel: String,
    val remainingAmountLabel: String,
)

data class CategoryOption(
    val id: String,
    val title: String,
    val isSelected: Boolean = false,
    val isDisabled: Boolean = false
) {
    val selected: String
        get() = if (isSelected) "selected" else ""

    val disabled: String
        get() = if (isDisabled) "disabled" else ""
}

fun CategoryResponse.asOption(selectedCategoryId: String) = CategoryOption(id, title, id == selectedCategoryId)