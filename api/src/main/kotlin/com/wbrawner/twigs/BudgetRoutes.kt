package com.wbrawner.twigs

import com.wbrawner.twigs.service.budget.BudgetService
import com.wbrawner.twigs.service.requireSession
import com.wbrawner.twigs.service.respondCatching
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Application.budgetRoutes(budgetService: BudgetService) {
    routing {
        route("/api/budgets") {
            authenticate(optional = false) {
                get {
                    call.respondCatching {
                        budgetService.budgetsForUser(userId = requireSession().userId)
                    }
                }

                get("/{id}") {
                    call.respondCatching {
                        budgetService.budget(
                            budgetId = call.parameters.getOrFail("id"),
                            userId = requireSession().userId
                        )
                    }
                }

                post {
                    call.respondCatching {
                        budgetService.save(request = call.receive(), userId = requireSession().userId)
                    }
                }

                put("/{id}") {
                    call.respondCatching {
                        budgetService.save(
                            request = call.receive(),
                            userId = requireSession().userId,
                            budgetId = call.parameters.getOrFail("id")
                        )
                    }
                }

                delete("/{id}") {
                    call.respondCatching {
                        budgetService.delete(
                            budgetId = call.parameters.getOrFail("id"),
                            userId = requireSession().userId
                        )
                        HttpStatusCode.NoContent
                    }
                }
            }
        }
    }
}
