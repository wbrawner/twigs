package com.wbrawner.twigs.web

import com.wbrawner.twigs.service.budget.BudgetResponse
import com.wbrawner.twigs.service.user.UserResponse

interface Page {
    val title: String
    val error: String?
}

interface AuthenticatedPage : Page {
    val user: UserResponse
    val budgets: List<BudgetListItem>
}

data class BudgetListItem(val id: String, val name: String, val description: String, val selected: Boolean)

fun BudgetResponse.toBudgetListItem(selectedId: String? = null) = BudgetListItem(
    id = id,
    name = name.orEmpty(),
    description = description.orEmpty(),
    selected = id == selectedId
)

object NotFoundPage : Page {
    override val title: String = "404 Not Found"
    override val error: String? = null
}