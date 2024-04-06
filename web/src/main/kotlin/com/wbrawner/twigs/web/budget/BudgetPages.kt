package com.wbrawner.twigs.web.budget

import com.wbrawner.twigs.service.budget.BudgetResponse
import com.wbrawner.twigs.service.user.UserResponse
import com.wbrawner.twigs.web.AuthenticatedPage
import com.wbrawner.twigs.web.category.CategoryWithBalanceResponse

data class BudgetListPage(
    val budgets: List<BudgetListItem>,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = "Budgets"
}

data class BudgetDetailsPage(
    val budgets: List<BudgetListItem>,
    val budget: BudgetResponse,
    val balances: BudgetBalances,
    val incomeCategories: List<CategoryWithBalanceResponse>,
    val expenseCategories: List<CategoryWithBalanceResponse>,
    val archivedIncomeCategories: List<CategoryWithBalanceResponse>,
    val archivedExpenseCategories: List<CategoryWithBalanceResponse>,
    val transactionCount: String,
    val monthAndYear: String,
    override val user: UserResponse,
    override val error: String? = null
) : AuthenticatedPage {
    override val title: String = budget.name.orEmpty()
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