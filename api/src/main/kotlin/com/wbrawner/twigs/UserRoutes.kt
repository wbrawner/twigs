package com.wbrawner.twigs

import com.wbrawner.twigs.model.Session
import com.wbrawner.twigs.model.User
import com.wbrawner.twigs.storage.PermissionRepository
import com.wbrawner.twigs.storage.SessionRepository
import com.wbrawner.twigs.storage.UserRepository
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.userRoutes(
    permissionRepository: PermissionRepository,
    sessionRepository: SessionRepository,
    userRepository: UserRepository
) {
    routing {
        route("/api/users") {
            post("/login") {
                val request = call.receive<LoginRequest>()
                val user =
                    userRepository.findAll(nameOrEmail = request.username, password = request.password.hash())
                        .firstOrNull()
                        ?: userRepository.findAll(nameOrEmail = request.username, password = request.password.hash())
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
                call.respond(
                    userRepository.save(
                        User(
                            name = request.username,
                            password = request.password.hash(),
                            email = request.email
                        )
                    ).asResponse()
                )
            }

            authenticate(optional = false) {
                get {
                    val query = call.request.queryParameters.getAll("query")
                    if (query?.firstOrNull()?.isNotBlank() == true) {
                        call.respond(userRepository.findAll(nameLike = query.first()).map { it.asResponse() })
                        return@get
                    }
                    permissionRepository.findAll(
                        budgetIds = call.request.queryParameters.getAll("budgetId")
                    ).mapNotNull {
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

                post {
                    val request = call.receive<UserRequest>()
                    if (request.username.isNullOrBlank()) {
                        errorResponse(HttpStatusCode.BadRequest, "Username must not be null or blank")
                        return@post
                    }
                    if (request.password.isNullOrBlank()) {
                        errorResponse(HttpStatusCode.BadRequest, "Password must not be null or blank")
                        return@post
                    }
                    call.respond(
                        userRepository.save(
                            User(
                                name = request.username,
                                password = request.password,
                                email = request.email
                            )
                        ).asResponse()
                    )
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
                                        password = request.password?.hash() ?: password,
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
    }
}
