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
            } ?: call.respondTemplate(
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
        get("/register") {
            call.respondTemplate(
                    template = "register.mustache",
                    model = mapOf("registration_enabled" to registrationEnabled)
            )
        }
        static {
            resources("static")
        }
        get("{...}") {
            call.respondTemplate(
                    template = "budget.mustache",
                    model = mapOf("registration_enabled" to registrationEnabled)
            )
        }
    }
}