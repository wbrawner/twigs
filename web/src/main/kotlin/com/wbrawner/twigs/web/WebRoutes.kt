package com.wbrawner.twigs.web

import com.wbrawner.twigs.model.CookieSession
import com.wbrawner.twigs.service.budget.BudgetService
import com.wbrawner.twigs.service.user.UserService
import com.wbrawner.twigs.web.user.userWebRoutes
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.mustache.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Application.webRoutes(
    budgetService: BudgetService,
    userService: UserService
) {
    routing {
        staticResources("/", "static")
        get("/") {
            call.sessions.get(CookieSession::class)
                ?.let { userService.session(it.token) }
                ?.let { session ->
                    application.environment.log.info("Session found!")
                    budgetService.budgetsForUser(session.userId)
                        .firstOrNull()
                        ?.let { budget ->
                            call.respondRedirect("/budgets/${budget.id}")
                        } ?: call.respondRedirect("/budgets")
                } ?: call.respond(MustacheContent("index.mustache", null))
        }
    }
    userWebRoutes(userService)
}