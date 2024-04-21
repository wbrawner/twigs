package com.wbrawner.twigs.web.transaction

import com.wbrawner.twigs.service.budget.BudgetResponse
import com.wbrawner.twigs.service.category.CategoryResponse
import com.wbrawner.twigs.service.transaction.TransactionResponse
import com.wbrawner.twigs.service.user.UserResponse
import com.wbrawner.twigs.web.AuthenticatedPage
import com.wbrawner.twigs.web.BudgetListItem
import com.wbrawner.twigs.web.category.TransactionListItem

data class TransactionListPage(
    val budget: BudgetResponse,
    val transactions: List<Map.Entry<String, List<TransactionListItem>>>,
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
}

fun CategoryResponse.asOption(selectedCategoryId: String) =
    TransactionFormPage.CategoryOption(id, title, id == selectedCategoryId)