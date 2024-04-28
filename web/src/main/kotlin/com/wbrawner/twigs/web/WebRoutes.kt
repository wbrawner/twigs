package com.wbrawner.twigs.web

import com.wbrawner.twigs.model.CookieSession
import com.wbrawner.twigs.service.HttpException
import com.wbrawner.twigs.service.budget.BudgetService
import com.wbrawner.twigs.service.category.CategoryService
import com.wbrawner.twigs.service.recurringtransaction.RecurringTransactionService
import com.wbrawner.twigs.service.transaction.TransactionService
import com.wbrawner.twigs.service.user.UserService
import com.wbrawner.twigs.web.budget.budgetWebRoutes
import com.wbrawner.twigs.web.category.categoryWebRoutes
import com.wbrawner.twigs.web.recurring.recurringTransactionWebRoutes
import com.wbrawner.twigs.web.transaction.transactionWebRoutes
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
    recurringTransactionService: RecurringTransactionService,
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
                        application.environment.log.debug("Failed to retrieve session for user", e)
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
    categoryWebRoutes(budgetService, categoryService, transactionService, userService)
    recurringTransactionWebRoutes(
        budgetService,
        categoryService,
        recurringTransactionService,
        transactionService,
        userService
    )
    transactionWebRoutes(budgetService, categoryService, transactionService, userService)
    userWebRoutes(userService)
}