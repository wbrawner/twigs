package com.wbrawner.twigs

import com.wbrawner.twigs.model.Permission
import com.wbrawner.twigs.model.User
import com.wbrawner.twigs.storage.Session
import java.util.*

data class UserRequest(
    val username: String? = null,
    val password: String? = null,
    val email: String? = null
)

data class LoginRequest(val username: String, val password: String)

data class UserResponse(val id: String, val username: String, val email: String?)

data class UserPermissionRequest(
    val user: String,
    val permission: Permission = Permission.READ
)

data class UserPermissionResponse(val user: String, val permission: Permission?)

data class SessionResponse(val token: String, val expiration: String)

data class PasswordResetRequest(
    val userId: Long,
    val id: String = randomString(),
    private val date: Calendar = GregorianCalendar(),
    private val token: String = randomString()
)

fun User.asResponse(): UserResponse = UserResponse(id, name, email)

fun Session.asResponse(): SessionResponse = SessionResponse(token, expiration.toInstant().toString())