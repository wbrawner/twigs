package com.wbrawner.twigs.server.permission

import com.wbrawner.budgetserver.budget.Budget
import com.wbrawner.budgetserver.user.User
import com.wbrawner.budgetserver.user.UserResponse
import java.io.Serializable
import javax.persistence.*

enum class Permission {
    /**
     * The user can read the content but cannot make any modifications.
     */
    READ,

    /**
     * The user can read and write the content but cannot make any modifications to the container of the content.
     */
    WRITE,

    /**
     * The user can read and write the content, and make modifications to the container of the content including things like name, description, and other users' permissions (with the exception of the owner user, whose role can never be removed by a user with only MANAGE permissions).
     */
    MANAGE,

    /**
     * The user has complete control over the resource. There can only be a single owner user at any given time.
     */
    OWNER;

    fun isNotAtLeast(wanted: Permission): Boolean {
        return ordinal < wanted.ordinal
    }
}

@Entity
data class UserPermission(
    @field:EmbeddedId
    val id: UserPermissionKey? = null,
    @field:JoinColumn(
        nullable = false,
        name = "budget_id"
    )
    @field:MapsId(
        "budgetId"
    )
    @field:ManyToOne
    val budget: Budget? = null,
    @field:JoinColumn(
        nullable = false,
        name = "user_id"
    )
    @field:MapsId("userId")
    @field:ManyToOne
    val user: User? = null,
    @field:Enumerated(
        EnumType.STRING
    )
    @field:JoinColumn(
        nullable = false
    )
    val permission: Permission = Permission.READ
) {
    constructor(budget: Budget, user: User, permission: Permission) : this(
        UserPermissionKey(budget.id, user.id),
        budget,
        user,
        permission
    )
}

@Embeddable
data class UserPermissionKey(
    private val budgetId: String? = null,
    private val userId: String? = null
) : Serializable

data class UserPermissionRequest(
    val user: String? = null,
    val permission: Permission = Permission.READ
)

data class UserPermissionResponse(val user: UserResponse, val permission: Permission?) {
    constructor(userPermission: UserPermission) : this(
        UserResponse(userPermission.user!!),
        userPermission.permission
    )
}