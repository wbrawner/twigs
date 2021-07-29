package com.wbrawner.twigs

import com.wbrawner.twigs.model.Permission
import com.wbrawner.twigs.model.User
import java.util.*

data class NewUserRequest(
    val username: String,
    val password: String,
    val email: String? = null
)

data class UpdateUserRequest(
    val username: String? = null,
    val password: String? = null,
    val email: String? = null
)

data class LoginRequest(val username: String? = null, val password: String? = null)

data class UserResponse(val id: String, val username: String, val email: String?) {
    constructor(user: User) : this(user.id, user.name, user.email)
}

data class UserPermissionRequest(
    val user: String,
    val permission: Permission = Permission.READ
)

data class UserPermissionResponse(val user: String, val permission: Permission?)

data class SessionResponse(val token: String, val expiration: String) {
    constructor(session: Session) : this(session.token, session.expiration.toInstant().toString())
}

data class PasswordResetRequest(
    val userId: Long,
    val id: String = randomString(),
    private val date: Calendar = GregorianCalendar(),
    private val token: String = randomString()
)