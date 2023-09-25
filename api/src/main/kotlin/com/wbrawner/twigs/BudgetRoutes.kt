package com.wbrawner.twigs

import com.wbrawner.twigs.model.Session
import com.wbrawner.twigs.service.budget.BudgetRequest
import com.wbrawner.twigs.service.budget.BudgetService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.budgetRoutes(budgetService: BudgetService) {
    routing {
        route("/api/budgets") {
            authenticate(optional = false) {
                get {
                    val session = requireNotNull(call.principal<Session>()) { "session is required" }
                    call.respond(budgetService.budgetsForUser(userId = session.userId))
                }

                get("/{id}") {
                    val session = requireNotNull(call.principal<Session>()) { "session is required" }
                    val budgetId = requireNotNull(call.parameters["id"]) { "budgetId is required" }
                    call.respond(budgetService.budget(budgetId = budgetId, userId = session.userId))
                }

                post {
                    val session = call.principal<Session>()!!
                    val request = call.receive<BudgetRequest>()
                    call.respond(budgetService.save(request = request, userId = session.userId))
                }

                put("/{id}") {
                    val session = requireNotNull(call.principal<Session>()) { "session was null" }
                    val request = call.receive<BudgetRequest>()
                    val budgetId = requireNotNull(call.parameters["id"]) { "budgetId is required" }
                    call.respond(budgetService.save(request = request, userId = session.id, budgetId = budgetId))
                }

                delete("/{id}") {
                    val session = requireNotNull(call.principal<Session>()) { "session was null" }
                    val budgetId = requireNotNull(call.parameters["id"]) { "budgetId is required" }
                    budgetService.delete(budgetId = budgetId, userId = session.userId)
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}
