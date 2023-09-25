package com.wbrawner.twigs.service.user

import com.wbrawner.twigs.model.PasswordResetToken
import com.wbrawner.twigs.model.Permission
import com.wbrawner.twigs.model.Session
import com.wbrawner.twigs.model.User
import kotlinx.serialization.Serializable

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

/**
 * Used to request the password reset email
 */
@Serializable
data class ResetPasswordRequest(val username: String)

/**
 * Used to modify the user's password after receiving the password reset email
 */
@Serializable
data class PasswordResetRequest(val token: String, val password: String)

@Serializable
data class PasswordResetTokenResponse(val userId: String, val id: String, val expiration: String)

fun User.asResponse(): UserResponse = UserResponse(id, name, email)

fun Session.asResponse(): SessionResponse = SessionResponse(userId, token, expiration.toString())

fun PasswordResetToken.asResponse(): PasswordResetTokenResponse =
    PasswordResetTokenResponse(userId, id, expiration.toString())