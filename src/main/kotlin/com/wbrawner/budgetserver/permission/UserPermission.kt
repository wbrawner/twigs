package com.wbrawner.budgetserver.permission

import com.wbrawner.budgetserver.budget.Budget
import com.wbrawner.budgetserver.user.User
import com.wbrawner.budgetserver.user.UserResponse
import java.io.Serializable
import javax.persistence.*

@Entity
data class UserPermission(
        @EmbeddedId
        val id: UserPermissionKey? = null,
        @ManyToOne
        @MapsId("budgetId")
        @JoinColumn(nullable = false, name = "budget_id")
        val budget: Budget? = null,
        @ManyToOne
        @MapsId("userId")
        @JoinColumn(nullable = false, name = "user_id")
        val user: User? = null,
        @JoinColumn(nullable = false)
        @Enumerated(EnumType.STRING)
        val permission: Permission? = null
) {
    constructor(budget: Budget, user: User, permission: Permission) : this(UserPermissionKey(budget.id, user.id), budget, user, permission)
}

@Embeddable
data class UserPermissionKey(
        var budgetId: Long? = null,
        var userId: Long? = null
) : Serializable

enum class Permission {
    READ,
    WRITE,
    OWNER
}

data class UserPermissionResponse(
        val user: UserResponse,
        val permission: Permission
) {
    constructor(userPermission: UserPermission) : this(UserResponse(userPermission.user!!), userPermission.permission!!)
}

data class UserPermissionRequest(
        val user: Long,
        val permission: Permission
)
