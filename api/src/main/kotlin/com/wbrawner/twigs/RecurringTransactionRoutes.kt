package com.wbrawner.twigs

import com.wbrawner.twigs.model.Permission
import com.wbrawner.twigs.model.RecurringTransaction
import com.wbrawner.twigs.model.Session
import com.wbrawner.twigs.storage.PermissionRepository
import com.wbrawner.twigs.storage.RecurringTransactionRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import java.time.Instant

fun Application.recurringTransactionRoutes(
    recurringTransactionRepository: RecurringTransactionRepository,
    permissionRepository: PermissionRepository
) {
    suspend fun PipelineContext<Unit, ApplicationCall>.recurringTransactionAfterPermissionCheck(
        id: String?,
        userId: String,
        success: suspend (RecurringTransaction) -> Unit
    ) {
        if (id.isNullOrBlank()) {
            errorResponse(HttpStatusCode.BadRequest, "id is required")
            return
        }
        val recurringTransaction = recurringTransactionRepository.findAll(ids = listOf(id)).firstOrNull()
            ?: run {
                errorResponse()
                return
            }
        requireBudgetWithPermission(
            permissionRepository,
            userId,
            recurringTransaction.budgetId,
            Permission.WRITE
        ) {
            application.log.info("No permissions on budget ${recurringTransaction.budgetId}.")
            return
        }
        success(recurringTransaction)
    }

    routing {
        route("/api/recurringtransactions") {
            authenticate(optional = false) {
                get {
                    val session = call.principal<Session>()!!
                    val budgetId = call.request.queryParameters["budgetId"]
                    requireBudgetWithPermission(
                        permissionRepository,
                        session.userId,
                        budgetId,
                        Permission.WRITE
                    ) {
                        return@get
                    }
                    call.respond(
                        recurringTransactionRepository.findAll(
                            budgetId = budgetId!!
                        ).map { it.asResponse() }
                    )
                }

                get("/{id}") {
                    val session = call.principal<Session>()!!
                    recurringTransactionAfterPermissionCheck(call.parameters["id"]!!, session.userId) {
                        call.respond(it.asResponse())
                    }
                }

                post {
                    val session = call.principal<Session>()!!
                    val request = call.receive<RecurringTransactionRequest>()
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
                        recurringTransactionRepository.save(
                            RecurringTransaction(
                                title = request.title,
                                description = request.description,
                                amount = request.amount ?: 0L,
                                expense = request.expense ?: true,
                                budgetId = request.budgetId,
                                categoryId = request.categoryId,
                                createdBy = session.userId,
                                start = request.start?.toInstant() ?: Instant.now(),
                                finish = request.finish?.toInstant(),
                                frequency = request.frequency.asFrequency()
                            )
                        ).asResponse()
                    )
                }

                put("/{id}") {
                    val session = call.principal<Session>()!!
                    val request = call.receive<RecurringTransactionRequest>()
                    recurringTransactionAfterPermissionCheck(
                        call.parameters["id"]!!,
                        session.userId
                    ) { recurringTransaction ->
                        if (request.budgetId != recurringTransaction.budgetId) {
                            requireBudgetWithPermission(
                                permissionRepository,
                                session.userId,
                                request.budgetId,
                                Permission.WRITE
                            ) {
                                return@recurringTransactionAfterPermissionCheck
                            }
                        }
                        call.respond(
                            recurringTransactionRepository.save(
                                recurringTransaction.copy(
                                    title = request.title ?: recurringTransaction.title,
                                    description = request.description ?: recurringTransaction.description,
                                    amount = request.amount ?: recurringTransaction.amount,
                                    expense = request.expense ?: recurringTransaction.expense,
                                    categoryId = request.categoryId ?: recurringTransaction.categoryId,
                                    budgetId = request.budgetId ?: recurringTransaction.budgetId,
                                    start = request.start?.toInstant() ?: recurringTransaction.start,
                                    finish = request.finish?.toInstant() ?: recurringTransaction.finish,
                                    frequency = request.frequency.asFrequency()
                                )
                            ).asResponse()
                        )
                    }
                }

                delete("/{id}") {
                    val session = call.principal<Session>()!!
                    recurringTransactionAfterPermissionCheck(call.parameters["id"]!!, session.userId) {
                        val response = if (recurringTransactionRepository.delete(it)) {
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
}
