package com.wbrawner.twigs.service.user

import com.wbrawner.twigs.EmailService
import com.wbrawner.twigs.model.PasswordResetToken
import com.wbrawner.twigs.model.Session
import com.wbrawner.twigs.model.User
import com.wbrawner.twigs.service.HttpException
import com.wbrawner.twigs.storage.*
import io.ktor.http.*
import java.time.Instant

interface UserService {

    suspend fun login(request: LoginRequest): SessionResponse

    suspend fun register(request: UserRequest): UserResponse

    suspend fun requestPasswordResetEmail(request: ResetPasswordRequest)

    suspend fun resetPassword(request: PasswordResetRequest)
    suspend fun users(query: String?, budgetIds: List<String>?, requestingUserId: String): List<UserResponse>

    suspend fun user(userId: String): UserResponse

    suspend fun session(token: String): SessionResponse

    suspend fun save(request: UserRequest, targetUserId: String, requestingUserId: String): UserResponse

    suspend fun delete(targetUserId: String, requestingUserId: String)
}

class DefaultUserService(
    private val emailService: EmailService,
    private val passwordResetRepository: PasswordResetRepository,
    private val permissionRepository: PermissionRepository,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher
) : UserService {

    override suspend fun login(request: LoginRequest): SessionResponse {
        val user = userRepository.findAll(
            nameOrEmail = request.username,
            password = passwordHasher.hash(request.password)
        )
            .firstOrNull()
            ?: throw HttpException(HttpStatusCode.Unauthorized, message = "Invalid credentials")
        return sessionRepository.save(Session(userId = user.id)).asResponse()
    }

    override suspend fun register(request: UserRequest): UserResponse {
        if (request.username.isNullOrBlank()) {
            throw HttpException(HttpStatusCode.BadRequest, message = "username must not be null or blank")
        }
        if (request.password.isNullOrBlank()) {
            throw HttpException(HttpStatusCode.BadRequest, message = "password must not be null or blank")
        }
        val existingUser = userRepository.findAll(nameOrEmail = request.username).firstOrNull()
            ?: request.email?.let {
                if (it.isBlank()) {
                    null
                } else {
                    userRepository.findAll(nameOrEmail = it).firstOrNull()
                }
            }
        existingUser?.let {
            throw HttpException(HttpStatusCode.BadRequest, message = "username or email already taken")
        }
        return userRepository.save(
            User(
                name = request.username,
                password = passwordHasher.hash(request.password),
                email = if (request.email.isNullOrBlank()) null else request.email
            )
        ).asResponse()
    }

    override suspend fun requestPasswordResetEmail(request: ResetPasswordRequest) {
        userRepository.findAll(nameOrEmail = request.username)
            .firstOrNull()
            ?.let {
                val email = it.email ?: return@let
                val passwordResetToken = passwordResetRepository.save(PasswordResetToken(userId = it.id))
                emailService.sendPasswordResetEmail(passwordResetToken, email)
            }
    }

    override suspend fun resetPassword(request: PasswordResetRequest) {
        val passwordResetToken = passwordResetRepository.findAll(listOf(request.token))
            .firstOrNull()
            ?: throw HttpException(HttpStatusCode.Unauthorized, message = "Invalid token")
        if (passwordResetToken.expiration.isBefore(Instant.now())) {
            throw HttpException(HttpStatusCode.Unauthorized, message = "Token expired")
        }
        if (request.password.isBlank()) {
            throw HttpException(HttpStatusCode.BadRequest, message = "password cannot be empty")
        }
        userRepository.findAll(listOf(passwordResetToken.userId))
            .firstOrNull()
            ?.let {
                userRepository.save(it.copy(password = passwordHasher.hash(request.password)))
                passwordResetRepository.delete(passwordResetToken)
            }
            ?: throw HttpException(HttpStatusCode.InternalServerError, message = "Invalid token")
    }

    override suspend fun users(
        query: String?,
        budgetIds: List<String>?,
        requestingUserId: String
    ): List<UserResponse> {
        if (query != null) {
            if (query.isBlank()) {
                throw HttpException(HttpStatusCode.BadRequest, message = "query cannot be empty")
            }
            return userRepository.findAll(nameLike = query).map { it.asResponse() }
        } else if (budgetIds == null || budgetIds.all { it.isBlank() }) {
            throw HttpException(HttpStatusCode.BadRequest, message = "query or budgetId required but absent")
        }
        return permissionRepository.findAll(budgetIds = budgetIds, userId = requestingUserId)
            .mapNotNull {
                userRepository.findAll(ids = listOf(it.userId))
                    .firstOrNull()
                    ?.asResponse()
            }
    }

    override suspend fun user(
        userId: String
    ): UserResponse {
        return userRepository.findAll(ids = listOf(userId))
            .firstOrNull()
            ?.asResponse()
            ?: throw HttpException(HttpStatusCode.NotFound)
    }

    override suspend fun session(token: String): SessionResponse {
        return sessionRepository.findAll(token = token)
            .firstOrNull()
            ?.asResponse()
            ?: throw HttpException(HttpStatusCode.Unauthorized)
    }

    override suspend fun save(
        request: UserRequest,
        targetUserId: String,
        requestingUserId: String,
    ): UserResponse {
        // TODO: Add some kind of admin denotation to allow admins to edit other users
        if (targetUserId != requestingUserId) {
            throw HttpException(HttpStatusCode.Forbidden)
        }
        return userRepository.save(
            userRepository.findAll(ids = listOf(targetUserId))
                .first()
                .run {
                    val newPassword = if (request.password.isNullOrBlank()) {
                        password
                    } else {
                        passwordHasher.hash(request.password)
                    }
                    copy(
                        name = request.username ?: name,
                        password = newPassword,
                        email = request.email ?: email
                    )
                }
        ).asResponse()
    }

    override suspend fun delete(targetUserId: String, requestingUserId: String) {
        // TODO: Add some kind of admin denotation to allow admins to delete other users
        if (targetUserId != requestingUserId) {
            throw HttpException(HttpStatusCode.Forbidden)
        }
        userRepository.delete(userRepository.findAll(targetUserId).first())
    }
}