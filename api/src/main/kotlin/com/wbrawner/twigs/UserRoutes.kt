package com.wbrawner.twigs

import com.wbrawner.twigs.service.requireSession
import com.wbrawner.twigs.service.respondCatching
import com.wbrawner.twigs.service.user.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Application.userRoutes(userService: UserService) {
    routing {
        route("/api/users") {
            post("/login") {
                call.respondCatching {
                    userService.login(call.receive())
                }
            }

            post("/register") {
                call.respondCatching {
                    userService.register(call.receive())
                }
            }


            route("/resetpassword") {
                post {
                    call.respondCatching {
                        userService.requestPasswordResetEmail(call.receive())
                        HttpStatusCode.Accepted
                    }
                }

                put {
                    call.respondCatching {
                        userService.resetPassword(call.receive())
                        HttpStatusCode.NoContent
                    }
                }
            }

            authenticate(optional = false) {
                get {
                    call.respondCatching {
                        userService.users(
                            query = call.request.queryParameters["query"],
                            budgetIds = call.request.queryParameters.getAll("budgetId"),
                            requestingUserId = requireSession().userId
                        )
                    }
                }

                get("/{id}") {
                    call.respondCatching {
                        userService.user(call.parameters.getOrFail("id"))
                    }
                }

                put("/{id}") {
                    call.respondCatching {
                        userService.save(
                            request = call.receive(),
                            targetUserId = call.parameters.getOrFail("id"),
                            requestingUserId = requireSession().userId
                        )
                    }
                }

                delete("/{id}") {
                    call.respondCatching {
                        userService.delete(
                            targetUserId = call.parameters.getOrFail("id"),
                            requestingUserId = requireSession().userId
                        )
                        HttpStatusCode.NoContent
                    }
                }
            }
        }
    }
}
