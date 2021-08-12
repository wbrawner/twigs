package com.wbrawner.twigs

import com.wbrawner.twigs.model.Permission
import com.wbrawner.twigs.model.User
import com.wbrawner.twigs.storage.Session
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UserRequest(
    val username: String? = null,
    val password: String? = null,
    val email: String? = null
)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class UserResponse(val id: String, val username: String, val email: String?)

@Serializable
data class UserPermissionRequest(
    val user: String,
    val permission: Permission = Permission.READ
)

@Serializable
data class UserPermissionResponse(val user: String, val permission: Permission?)

@Serializable
data class SessionResponse(val userId: String, val token: String, val expiration: String)

data class PasswordResetRequest(
    val userId: Long,
    val id: String = randomString(),
    private val date: Calendar = GregorianCalendar(),
    private val token: String = randomString()
)

fun User.asResponse(): UserResponse = UserResponse(id, name, email)

fun Session.asResponse(): SessionResponse = SessionResponse(userId, token, expiration.toString())