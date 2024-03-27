package com.wbrawner.twigs

import com.wbrawner.twigs.service.recurringtransaction.RecurringTransactionRequest
import com.wbrawner.twigs.service.recurringtransaction.RecurringTransactionService
import com.wbrawner.twigs.service.requireSession
import com.wbrawner.twigs.service.respondCatching
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Application.recurringTransactionRoutes(recurringTransactionService: RecurringTransactionService) {
    routing {
        route("/api/recurringtransactions") {
            authenticate(optional = false) {
                get {
                    call.respondCatching {
                        recurringTransactionService.recurringTransactions(
                            budgetId = call.request.queryParameters.getOrFail("budgetId"),
                            userId = requireSession().userId
                        )
                    }
                }

                get("/{id}") {
                    call.respondCatching {
                        recurringTransactionService.recurringTransaction(
                            call.parameters.getOrFail("id"),
                            requireSession().userId
                        )
                    }
                }

                post {
                    call.respondCatching {
                        recurringTransactionService.save(
                            request = call.receive<RecurringTransactionRequest>(),
                            userId = requireSession().userId
                        )
                    }
                }

                put("/{id}") {
                    recurringTransactionService.save(
                        request = call.receive<RecurringTransactionRequest>(),
                        userId = requireSession().userId,
                        recurringTransactionId = call.parameters.getOrFail("id")
                    )
                }

                delete("/{id}") {
                    call.respondCatching {
                        recurringTransactionService.delete(call.parameters.getOrFail("id"), requireSession().userId)
                        HttpStatusCode.NoContent
                    }
                }
            }
        }
    }
}
