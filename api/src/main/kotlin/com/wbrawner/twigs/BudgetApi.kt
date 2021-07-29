package com.wbrawner.twigs

import com.wbrawner.twigs.model.Budget
import com.wbrawner.twigs.model.UserPermission
import java.util.*

data class BudgetRequest(
    val name: String? = null,
    val description: String? = null,
    val users: Set<UserPermissionRequest>? = null
)

data class BudgetResponse(
    val id: String,
    val name: String?,
    val description: String?,
    private val users: List<UserPermissionResponse>
) {
    constructor(budget: Budget, users: Iterable<UserPermission>) : this(
        Objects.requireNonNull<String>(budget.id),
        budget.name,
        budget.description,
        users.map { userPermission: UserPermission ->
            UserPermissionResponse(
                userPermission.userId,
                userPermission.permission
            )
        }
    )
}

data class BudgetBalanceResponse(val id: String, val balance: Long)