package com.wbrawner.twigs

import com.wbrawner.twigs.model.Permission
import com.wbrawner.twigs.model.Session
import com.wbrawner.twigs.model.Transaction
import com.wbrawner.twigs.service.errorResponse
import com.wbrawner.twigs.service.requireBudgetWithPermission
import com.wbrawner.twigs.service.transaction.BalanceResponse
import com.wbrawner.twigs.service.transaction.TransactionRequest
import com.wbrawner.twigs.storage.PermissionRepository
import com.wbrawner.twigs.storage.TransactionRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant

fun Application.transactionRoutes(
    transactionRepository: TransactionRepository,
    permissionRepository: PermissionRepository
) {
    routing {
        route("/api/transactions") {
            authenticate(optional = false) {
                get {
                    val session = call.principal<Session>()!!
                    call.respond(
                        transactionRepository.findAll(
                            budgetIds = permissionRepository.findAll(
                                budgetIds = call.request.queryParameters.getAll("budgetIds"),
                                userId = session.userId
                            ).map { it.budgetId },
                            categoryIds = call.request.queryParameters.getAll("categoryIds"),
                            from = call.request.queryParameters["from"]?.let { Instant.parse(it) },
                            to = call.request.queryParameters["to"]?.let { Instant.parse(it) },
                            expense = call.request.queryParameters["expense"]?.toBoolean(),
                        ).map { it.asResponse() })
                }

                get("/{id}") {
                    val session = call.principal<Session>()!!
                    val transaction = transactionRepository.findAll(
                        ids = call.parameters.getAll("id"),
                        budgetIds = permissionRepository.findAll(
                            userId = session.userId
                        )
                            .map { it.budgetId }
                    )
                        .map { it.asResponse() }
                        .firstOrNull()
                    transaction?.let {
                        call.respond(it)
                    } ?: errorResponse()
                }

                get("/sum") {
                    val categoryId = call.request.queryParameters["categoryId"]
                    val budgetId = call.request.queryParameters["budgetId"]
                    val from = call.request.queryParameters["from"]?.toInstant() ?: firstOfMonth
                    val to = call.request.queryParameters["to"]?.toInstant() ?: endOfMonth
                    val balance = if (!categoryId.isNullOrBlank()) {
                        if (!budgetId.isNullOrBlank()) {
                            errorResponse(
                                HttpStatusCode.BadRequest,
                                "budgetId and categoryId cannot be provided together"
                            )
                            return@get
                        }
                        transactionRepository.sumByCategory(categoryId, from, to)
                    } else if (!budgetId.isNullOrBlank()) {
                        transactionRepository.sumByBudget(budgetId, from, to)
                    } else {
                        errorResponse(HttpStatusCode.BadRequest, "budgetId or categoryId must be provided to sum")
                        return@get
                    }
                    call.respond(BalanceResponse(balance))
                }

                post {
                    val session = call.principal<Session>()!!
                    val request = call.receive<TransactionRequest>()
                    if (request.title.isNullOrBlank()) {
                        errorResponse(HttpStatusCode.BadRequest, "Title cannot be null or empty")
                        return@post
                    }
                    if (request.budgetId.isNullOrBlank()) {
                        errorResponse(HttpStatusCode.BadRequest, "Budget ID cannot be null or empty")
                        return@post
                    }
                    requireBudgetWithPermission(
                        permissionRepository,
                        session.userId,
                        request.budgetId,
                        Permission.WRITE
                    ) {
                        return@post
                    }
                    call.respond(
                        transactionRepository.save(
                            Transaction(
                                title = request.title,
                                description = request.description,
                                amount = request.amount ?: 0L,
                                expense = request.expense ?: true,
                                budgetId = request.budgetId,
                                categoryId = request.categoryId,
                                createdBy = session.userId,
                                date = request.date?.let { Instant.parse(it) } ?: Instant.now()
                            )
                        ).asResponse()
                    )
                }

                put("/{id}") {
                    val session = call.principal<Session>()!!
                    val request = call.receive<TransactionRequest>()
                    val transaction = transactionRepository.findAll(ids = call.parameters.getAll("id"))
                        .firstOrNull()
                        ?: run {
                            errorResponse()
                            return@put
                        }
                    requireBudgetWithPermission(
                        permissionRepository,
                        session.userId,
                        transaction.budgetId,
                        Permission.WRITE
                    ) {
                        return@put
                    }
                    call.respond(
                        transactionRepository.save(
                            transaction.copy(
                                title = request.title ?: transaction.title,
                                description = request.description ?: transaction.description,
                                amount = request.amount ?: transaction.amount,
                                expense = request.expense ?: transaction.expense,
                                date = request.date?.let { Instant.parse(it) } ?: transaction.date,
                                categoryId = request.categoryId ?: transaction.categoryId,
                                budgetId = request.budgetId ?: transaction.budgetId,
                                createdBy = transaction.createdBy,
                            )
                        ).asResponse()
                    )
                }

                delete("/{id}") {
                    val session = call.principal<Session>()!!
                    val transaction = transactionRepository.findAll(ids = call.parameters.getAll("id"))
                        .firstOrNull()
                        ?: run {
                            errorResponse()
                            return@delete
                        }
                    requireBudgetWithPermission(
                        permissionRepository,
                        session.userId,
                        transaction.budgetId,
                        Permission.WRITE
                    ) {
                        return@delete
                    }
                    val response = if (transactionRepository.delete(transaction)) {
                        HttpStatusCode.NoContent
                    } else {
                        HttpStatusCode.InternalServerError
                    }
                    call.respond(response)
                }
            }
        }
    }
}
