package com.wbrawner.twigs

import com.wbrawner.twigs.service.category.CategoryRequest
import com.wbrawner.twigs.service.category.CategoryService
import com.wbrawner.twigs.service.requireSession
import com.wbrawner.twigs.service.respondCatching
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Application.categoryRoutes(categoryService: CategoryService) {
    routing {
        route("/api/categories") {
            authenticate(optional = false) {
                get {
                    call.respondCatching {
                        categoryService.categories(
                            budgetIds = call.request.queryParameters.getAll("budgetIds").orEmpty(),
                            userId = requireSession().userId,
                            expense = call.request.queryParameters["expense"]?.toBoolean(),
                            archived = call.request.queryParameters["archived"]?.toBoolean()
                        )
                    }
                }

                get("/{id}") {
                    call.respondCatching {
                        categoryService.category(
                            categoryId = call.parameters.getOrFail("id"),
                            userId = requireSession().userId
                        )
                    }
                }

                post {
                    call.respondCatching {
                        categoryService.save(call.receive<CategoryRequest>(), requireSession().userId)
                    }
                }

                put("/{id}") {
                    call.respondCatching {
                        categoryService.save(
                            request = call.receive<CategoryRequest>(),
                            userId = requireSession().userId,
                            categoryId = call.parameters.getOrFail("id")
                        )
                    }
                }

                delete("/{id}") {
                    call.respondCatching {
                        categoryService.delete(
                            call.parameters.getOrFail("id"),
                            requireSession().userId
                        )
                        HttpStatusCode.NoContent
                    }
                }
            }
        }
    }
}
