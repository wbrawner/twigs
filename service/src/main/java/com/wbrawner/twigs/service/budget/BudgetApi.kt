package com.wbrawner.twigs.service.budget

import com.wbrawner.twigs.model.Budget
import com.wbrawner.twigs.model.UserPermission
import com.wbrawner.twigs.service.user.UserPermissionRequest
import com.wbrawner.twigs.service.user.UserPermissionResponse
import kotlinx.serialization.Serializable

@Serializable
data class BudgetRequest(
    val name: String? = null,
    val description: String? = null,
    val users: Set<UserPermissionRequest>? = null
)

@Serializable
data class BudgetResponse(
    val id: String,
    val name: String? = null,
    val description: String? = null,
    val users: List<UserPermissionResponse>
) {
    constructor(budget: Budget, users: Iterable<UserPermission>) : this(
        requireNotNull(budget.id),
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
