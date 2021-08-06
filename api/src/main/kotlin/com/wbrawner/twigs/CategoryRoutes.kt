package com.wbrawner.twigs

import com.wbrawner.twigs.model.Category
import com.wbrawner.twigs.model.Permission
import com.wbrawner.twigs.storage.CategoryRepository
import com.wbrawner.twigs.storage.PermissionRepository
import com.wbrawner.twigs.storage.Session
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.categoryRoutes(
    categoryRepository: CategoryRepository,
    permissionRepository: PermissionRepository
) {
    routing {
        route("/api/categories") {
            authenticate(optional = false) {
                get("/") {
                    val session = call.principal<Session>()!!
                    call.respond(categoryRepository.findAll(
                        budgetIds = permissionRepository.findAll(
                            budgetIds = call.request.queryParameters.getAll("budgetIds"),
                            userId = session.userId
                        ).map { it.budgetId },
                        expense = call.request.queryParameters["expense"]?.toBoolean(),
                        archived = call.request.queryParameters["archived"]?.toBoolean()
                    ).map { CategoryResponse(it) })
                }

                get("/{id}") {
                    val session = call.principal<Session>()!!
                    call.respond(categoryRepository.findAll(
                        ids = call.parameters.getAll("id"),
                        budgetIds = permissionRepository.findAll(
                            userId = session.userId
                        ).map { it.budgetId }
                    ).map { CategoryResponse(it) })
                }

                post("/") {
                    val session = call.principal<Session>()!!
                    val request = call.receive<CategoryRequest>()
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
                        CategoryResponse(
                            categoryRepository.save(
                                Category(
                                    title = request.title,
                                    description = request.description,
                                    amount = request.amount ?: 0L,
                                    expense = request.expense ?: true,
                                )
                            )
                        )
                    )
                }

                put("/{id}") {
                    val session = call.principal<Session>()!!
                    val request = call.receive<CategoryRequest>()
                    val category = categoryRepository.findAll(ids = call.parameters.getAll("id"))
                        .firstOrNull()
                        ?: run {
                            call.respond(HttpStatusCode.NotFound)
                            return@put
                        }
                    requireBudgetWithPermission(
                        permissionRepository,
                        session.userId,
                        category.budgetId!!,
                        Permission.WRITE
                    ) {
                        return@put
                    }
                    call.respond(
                        CategoryResponse(
                            categoryRepository.save(
                                Category(
                                    title = request.title ?: category.title,
                                    description = request.description ?: category.description,
                                    amount = request.amount ?: category.amount,
                                    expense = request.expense ?: category.expense,
                                    archived = request.archived ?: category.archived
                                )
                            )
                        )
                    )
                }

                delete("/{id}") {
                    val session = call.principal<Session>()!!
                    val category = categoryRepository.findAll(ids = call.parameters.getAll("id"))
                        .firstOrNull()
                        ?: run {
                            errorResponse(HttpStatusCode.NotFound)
                            return@delete
                        }
                    requireBudgetWithPermission(
                        permissionRepository,
                        session.userId,
                        category.budgetId!!,
                        Permission.WRITE
                    ) {
                        return@delete
                    }
                    categoryRepository.delete(category)
                }
            }
        }
    }
}
