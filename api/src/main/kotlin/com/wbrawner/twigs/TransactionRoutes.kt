package com.wbrawner.twigs

import com.wbrawner.twigs.service.requireSession
import com.wbrawner.twigs.service.respondCatching
import com.wbrawner.twigs.service.transaction.BalanceResponse
import com.wbrawner.twigs.service.transaction.TransactionRequest
import com.wbrawner.twigs.service.transaction.TransactionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import java.time.Instant

fun Application.transactionRoutes(transactionService: TransactionService) {
    routing {
        route("/api/transactions") {
            authenticate(optional = false) {
                get {
                    call.respondCatching {
                        transactionService.transactions(
                            budgetIds = call.request.queryParameters.getAll("budgetIds").orEmpty(),
                            categoryIds = call.request.queryParameters.getAll("categoryIds"),
                            from = call.request.queryParameters["from"]?.let { Instant.parse(it) },
                            to = call.request.queryParameters["to"]?.let { Instant.parse(it) },
                            expense = call.request.queryParameters["expense"]?.toBoolean(),
                            userId = requireSession().userId
                        )
                    }
                }

                get("/{id}") {
                    call.respondCatching {
                        transactionService.transaction(
                            transactionId = call.parameters.getOrFail("id"),
                            userId = requireSession().userId
                        )
                    }
                }

                get("/sum") {
                    call.respondCatching {
                        BalanceResponse(
                            transactionService.sum(
                                budgetId = call.request.queryParameters["budgetId"],
                                categoryId = call.request.queryParameters["categoryId"],
                                from = call.request.queryParameters["from"]?.toInstant() ?: firstOfMonth,
                                to = call.request.queryParameters["to"]?.toInstant() ?: endOfMonth,
                                userId = requireSession().userId,
                            )
                        )
                    }
                }

                post {
                    call.respondCatching {
                        transactionService.save(
                            request = call.receive<TransactionRequest>(),
                            userId = requireSession().userId
                        )
                    }
                }

                put("/{id}") {
                    call.respondCatching {
                        transactionService.save(
                            request = call.receive<TransactionRequest>(),
                            userId = requireSession().userId,
                            transactionId = call.parameters.getOrFail("id")
                        )
                    }
                }

                delete("/{id}") {
                    call.respondCatching {
                        transactionService.delete(
                            transactionId = call.parameters.getOrFail("id"),
                            userId = requireSession().userId
                        )
                        HttpStatusCode.NoContent
                    }
                }
            }
        }
    }
}
