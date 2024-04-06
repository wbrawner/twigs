package com.wbrawner.twigs.web

import com.wbrawner.twigs.model.CookieSession
import com.wbrawner.twigs.service.HttpException
import com.wbrawner.twigs.service.budget.BudgetService
import com.wbrawner.twigs.service.category.CategoryService
import com.wbrawner.twigs.service.transaction.TransactionService
import com.wbrawner.twigs.service.user.UserService
import com.wbrawner.twigs.web.budget.budgetWebRoutes
import com.wbrawner.twigs.web.user.userWebRoutes
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.mustache.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Application.webRoutes(
    budgetService: BudgetService,
    categoryService: CategoryService,
    transactionService: TransactionService,
    userService: UserService
) {
    routing {
        staticResources("/", "static")
        get("/") {
            call.sessions.get(CookieSession::class)
                ?.let {
                    try {
                        userService.session(it.token)
                    } catch (e: HttpException) {
                        null
                    }
                }
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
    budgetWebRoutes(budgetService, categoryService, transactionService, userService)
    userWebRoutes(userService)
}