package com.wbrawner.twigs

import com.wbrawner.twigs.model.Budget
import com.wbrawner.twigs.model.UserPermission
import kotlinx.serialization.Serializable
import java.util.*

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
