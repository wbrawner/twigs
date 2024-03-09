package com.wbrawner.twigs

import com.wbrawner.twigs.model.PasswordResetToken
import com.wbrawner.twigs.model.Session
import com.wbrawner.twigs.model.User
import com.wbrawner.twigs.storage.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant

fun Application.userRoutes(
    emailService: EmailService,
    passwordResetRepository: PasswordResetRepository,
    permissionRepository: PermissionRepository,
    sessionRepository: SessionRepository,
    userRepository: UserRepository,
    passwordHasher: PasswordHasher
) {
    routing {
        route("/api/users") {
            post("/login") {
                val request = call.receive<LoginRequest>()
                val user =
                    userRepository.findAll(
                        nameOrEmail = request.username,
                        password = passwordHasher.hash(request.password)
                    )
                        .firstOrNull()
                        ?: userRepository.findAll(
                            nameOrEmail = request.username,
                            password = passwordHasher.hash(request.password)
                        )
                            .firstOrNull()
                        ?: run {
                            errorResponse(HttpStatusCode.Unauthorized, "Invalid credentials")
                            return@post
                        }
                val session = sessionRepository.save(Session(userId = user.id))
                call.respond(session.asResponse())
            }

            post("/register") {
                val request = call.receive<UserRequest>()
                if (request.username.isNullOrBlank()) {
                    errorResponse(HttpStatusCode.BadRequest, "Username must not be null or blank")
                    return@post
                }
                if (request.password.isNullOrBlank()) {
                    errorResponse(HttpStatusCode.BadRequest, "Password must not be null or blank")
                    return@post
                }
                val existingUser = userRepository.findAll(nameOrEmail = request.username).firstOrNull()
                    ?: request.email?.let {
                        return@let if (it.isBlank()) {
                            null
                        } else {
                            userRepository.findAll(nameOrEmail = it).firstOrNull()
                        }
                    }
                existingUser?.let {
                    errorResponse(HttpStatusCode.BadRequest, "Username or email already taken")
                    return@post
                }
                call.respond(
                    userRepository.save(
                        User(
                            name = request.username,
                            password = passwordHasher.hash(request.password),
                            email = if (request.email.isNullOrBlank()) "" else request.email
                        )
                    ).asResponse()
                )
            }

            authenticate(optional = false) {
                get {
                    val query = call.request.queryParameters["query"]
                    val budgetIds = call.request.queryParameters.getAll("budgetId")
                    if (query != null) {
                        if (query.isBlank()) {
                            errorResponse(HttpStatusCode.BadRequest, "query cannot be empty")
                        }
                        call.respond(userRepository.findAll(nameLike = query).map { it.asResponse() })
                        return@get
                    } else if (budgetIds == null || budgetIds.all { it.isBlank() }) {
                        errorResponse(HttpStatusCode.BadRequest, "query or budgetId required but absent")
                    }
                    permissionRepository.findAll(budgetIds = budgetIds)
                        .mapNotNull {
                            userRepository.findAll(ids = listOf(it.userId))
                                .firstOrNull()
                                ?.asResponse()
                        }.run { call.respond(this) }
                }

                get("/{id}") {
                    userRepository.findAll(ids = call.parameters.getAll("id"))
                        .firstOrNull()
                        ?.asResponse()
                        ?.let { call.respond(it) }
                        ?: errorResponse(HttpStatusCode.NotFound)
                }

                put("/{id}") {
                    val session = call.principal<Session>()!!
                    val request = call.receive<UserRequest>()
                    // TODO: Add some kind of admin denotation to allow admins to edit other users
                    if (call.parameters["id"] != session.userId) {
                        errorResponse(HttpStatusCode.Forbidden)
                        return@put
                    }
                    call.respond(
                        userRepository.save(
                            userRepository.findAll(ids = call.parameters.getAll("id"))
                                .first()
                                .run {
                                    copy(
                                        name = request.username ?: name,
                                        password = request.password?.let { passwordHasher.hash(it) } ?: password,
                                        email = request.email ?: email
                                    )
                                }
                        ).asResponse()
                    )
                }

                delete("/{id}") {
                    val session = call.principal<Session>()!!
                    // TODO: Add some kind of admin denotation to allow admins to delete other users
                    val user = userRepository.findAll(call.parameters.entries().first().value).firstOrNull()
                    if (user == null) {
                        errorResponse()
                        return@delete
                    }
                    if (user.id != session.userId) {
                        errorResponse(HttpStatusCode.Forbidden)
                        return@delete
                    }
                    userRepository.delete(user)
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }

        route("/api/resetpassword") {
            post {
                val request = call.receive<ResetPasswordRequest>()
                userRepository.findAll(nameOrEmail = request.username)
                    .firstOrNull()
                    ?.let {
                        val email = it.email
                        val passwordResetToken = passwordResetRepository.save(PasswordResetToken(userId = it.id))
                        emailService.sendPasswordResetEmail(passwordResetToken, email)
                    }
                call.respond(HttpStatusCode.Accepted)
            }
        }

        route("/api/passwordreset") {
            post {
                val request = call.receive<PasswordResetRequest>()
                val passwordResetToken = passwordResetRepository.findAll(listOf(request.token))
                    .firstOrNull()
                    ?: run {
                        errorResponse(HttpStatusCode.Unauthorized, "Invalid token")
                        return@post
                    }
                if (passwordResetToken.expiration.isBefore(Instant.now())) {
                    errorResponse(HttpStatusCode.Unauthorized, "Token expired")
                    return@post
                }
                userRepository.findAll(listOf(passwordResetToken.userId))
                    .firstOrNull()
                    ?.let {
                        userRepository.save(it.copy(password = passwordHasher.hash(request.password)))
                        passwordResetRepository.delete(passwordResetToken)
                    }
                    ?: run {
                        errorResponse(HttpStatusCode.InternalServerError, "Invalid token")
                        return@post
                    }
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
