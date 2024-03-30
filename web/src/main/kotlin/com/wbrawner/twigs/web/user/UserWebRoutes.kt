package com.wbrawner.twigs.web.user

import com.wbrawner.twigs.model.CookieSession
import com.wbrawner.twigs.service.HttpException
import com.wbrawner.twigs.service.user.LoginRequest
import com.wbrawner.twigs.service.user.UserRequest
import com.wbrawner.twigs.service.user.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.mustache.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*

const val TWIGS_SESSION_COOKIE = "twigsSession"

fun Application.userWebRoutes(userService: UserService) {
    routing {
        route("/login") {
            get {
                call.respond(MustacheContent("login.mustache", LoginPage()))
            }

            post {
                val request = call.receiveParameters().toLoginRequest()
                try {
                    val session = userService.login(request)
                    call.sessions.set(CookieSession(session.token))
                    call.respondRedirect("/")
                } catch (e: HttpException) {
                    call.respond(
                        status = e.statusCode,
                        MustacheContent("login.mustache", LoginPage(username = request.username, error = e.message))
                    )
                }
            }
        }
        route("/register") {
            get {
                call.respond(MustacheContent("register.mustache", RegisterPage()))
            }

            post {
                val request = call.receiveParameters()
                val userRequest = request.toUserRequest()
                val confirmPassword = request.getOrFail("confirmPassword")
                if (userRequest.password != confirmPassword) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        MustacheContent(
                            "register.mustache",
                            userRequest.toPage("passwords don't match")
                        )
                    )
                    return@post
                }
                try {
                    userService.register(userRequest)
                    val session = userService.login(
                        LoginRequest(
                            requireNotNull(userRequest.username),
                            requireNotNull(userRequest.password)
                        )
                    )
                    call.sessions.set(CookieSession(session.token))
                    call.respondRedirect("/")
                } catch (e: HttpException) {
                    call.respond(
                        status = e.statusCode,
                        MustacheContent("register.mustache", userRequest.toPage(error = e.message))
                    )
                }
            }
        }
    }
}

private fun Parameters.toLoginRequest() = LoginRequest(getOrFail("username"), getOrFail("password"))

private fun Parameters.toUserRequest() = UserRequest(getOrFail("username"), getOrFail("password"), get("email"))

private fun UserRequest.toPage(error: String? = null) =
    RegisterPage(username = username.orEmpty(), email = email.orEmpty(), error = error)