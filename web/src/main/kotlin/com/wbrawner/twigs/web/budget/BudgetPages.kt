package com.wbrawner.twigs.web.budget

import com.wbrawner.twigs.service.budget.BudgetResponse
import com.wbrawner.twigs.service.category.CategoryResponse
import com.wbrawner.twigs.service.transaction.BalanceResponse
import com.wbrawner.twigs.service.user.UserResponse
import com.wbrawner.twigs.web.AuthenticatedPage

data class BudgetListPage(
    val budgets: List<BudgetResponse>,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = "Budgets"
}

data class BudgetDetailsPage(
    val budget: BudgetResponse,
    val balance: BalanceResponse,
    val categories: List<CategoryWithBalanceResponse>,
    val archivedCategories: List<CategoryWithBalanceResponse>,
    val transactionCount: Long,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = "Budgets"

    data class CategoryWithBalanceResponse(val category: CategoryResponse, val balance: BalanceResponse)
}

data class BudgetFormPage(
    val budget: BudgetResponse,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = if (budget.id.isBlank()) {
        "New Budget"
    } else {
        "Edit Budget"
    }
}