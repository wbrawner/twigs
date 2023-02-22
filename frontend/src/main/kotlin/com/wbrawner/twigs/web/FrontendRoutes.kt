package com.wbrawner.twigs.web

import com.github.mustachejava.DefaultMustacheFactory
import com.wbrawner.twigs.EmailService
import com.wbrawner.twigs.model.Session
import com.wbrawner.twigs.model.User
import com.wbrawner.twigs.storage.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.mustache.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Application.frontendRoutes(
        budgetRepository: BudgetRepository,
        categoryRepository: CategoryRepository,
        emailService: EmailService,
        passwordResetRepository: PasswordResetRepository,
        permissionRepository: PermissionRepository,
        recurringTransactionRepository: RecurringTransactionRepository,
        sessionRepository: SessionRepository,
        transactionRepository: TransactionRepository,
        userRepository: UserRepository
) {
    routing {
        val registrationEnabled = true // TODO: Make this configurable and extract to data class
        get("/") {
            call.principal<Session>()?.let {
                call.respondRedirect("/budgets") // TODO: Get last used budget and redirect there instead
            }?: call.respondTemplate(
                template = "index.mustache",
                model = mapOf("registration_enabled" to registrationEnabled)
            )
        }
        get("/login") {
            call.respondTemplate(
                template = "login.mustache",
                model = mapOf("registration_enabled" to registrationEnabled)
            )
        }
        post("/login") {
            val params = call.receiveParameters()
            val username = params["username"]?: return@post call.respondTemplate(
                template = "login.mustache",
                model = mapOf(
                    "registration_enabled" to registrationEnabled,
                    "error" to "Username is required"
                )
            )
            val password = params["password"]?: return@post call.respondTemplate(
                template = "login.mustache",
                model = mapOf(
                    "registration_enabled" to registrationEnabled,
                    "error" to "Password is required"
                )
            )
            val user = userRepository.findAll(nameOrEmail = username, password = password)
                .firstOrNull()
                ?: return@post call.respondTemplate(
                    template = "login.mustache",
                    model = mapOf(
                        "registration_enabled" to registrationEnabled,
                        "error" to "Invalid credentials"
                    )
                )
            val session = sessionRepository.save(Session(userId = user.id))
            call.response.cookies.append("twigs_session", session.token)
            call.respondRedirect("/budgets") // TODO: Get last used budget and redirect there instead
        }
        get("/register") {
            call.respondTemplate(
                template = "register.mustache",
                model = mapOf("registration_enabled" to registrationEnabled)
            )
        }
        post("/register") {
            val params = call.receiveParameters()
            val username = params["username"]?: return@post call.respondTemplate(
                template = "register.mustache",
                model = mapOf(
                    "registration_enabled" to registrationEnabled,
                    "error" to "Username is required"
                )
            )
            val email = params["email"]
            val password = params["password"]?: return@post call.respondTemplate(
                template = "register.mustache",
                model = mapOf(
                    "registration_enabled" to registrationEnabled,
                    "error" to "Password is required"
                )
            )
            val confirmPassword = params["confirm-password"]?: return@post call.respondTemplate(
                template = "register.mustache",
                model = mapOf(
                    "registration_enabled" to registrationEnabled,
                    "error" to "Confirm Password is required"
                )
            )
            if (password != confirmPassword) {
                return@post call.respondTemplate(
                    template = "register.mustache",
                    model = mapOf(
                        "registration_enabled" to registrationEnabled,
                        "error" to "Passwords must match"
                    )
                )
            }
            val user = userRepository.save(User(name = username, password = password, email = email))
            val session = sessionRepository.save(Session(userId = user.id))
            call.response.cookies.append("twigs_session", session.token)
            call.respondRedirect("/budgets") // TODO: Redirect to welcome flow
        }
        authenticate("auth-cookie") {
            get("/budgets") {
                call.respondText("Not yet implemented")
            }
        }
        static {
            resources("static")
        }
    }
}