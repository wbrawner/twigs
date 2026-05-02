package com.wbrawner.twigs

import com.wbrawner.twigs.service.budget.BudgetRequest
import com.wbrawner.twigs.service.budget.BudgetResponse
import com.wbrawner.twigs.service.budget.BudgetService
import com.wbrawner.twigs.service.requireSession
import com.wbrawner.twigs.service.respondCatching
import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.server.util.*
import io.ktor.utils.io.*

@OptIn(ExperimentalKtorApi::class)
fun Application.budgetRoutes(budgetService: BudgetService) {
    routing {
        route("/api/budgets") {
            authenticate(optional = false) {
                get {
                    call.respondCatching {
                        budgetService.budgetsForUser(userId = requireSession().userId)
                    }
                }.describe {
                    summary = "Get budgets"
                    description = "Retrieve a list of list of budgets a user has access to"
                    responses {
                        HttpStatusCode.OK {
                            description = "A list of budgets"
                            schema = jsonSchema<List<BudgetResponse>>()
                        }
                    }
                }

                get("/{id}") {
                    call.respondCatching {
                        budgetService.budget(
                            budgetId = call.parameters.getOrFail("id"),
                            userId = requireSession().userId
                        )
                    }
                }.describe {
                    summary = "Get a budget by id"
                    description = "Retrieve a budget by id"
                    responses {
                        HttpStatusCode.OK {
                            schema = jsonSchema<BudgetResponse>()
                        }
                    }
                }

                post {
                    call.respondCatching {
                        budgetService.save(request = call.receive(), userId = requireSession().userId)
                    }
                }.describe {
                    summary = "Create a budget"
                    requestBody {
                        schema = jsonSchema<BudgetRequest>()
                    }
                    responses {
                        HttpStatusCode.OK {
                            schema = jsonSchema<BudgetResponse>()
                        }
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
                }.describe {
                    summary = "Update a budget"
                    requestBody {
                        schema = jsonSchema<BudgetRequest>()
                    }
                    responses {
                        HttpStatusCode.OK {
                            schema = jsonSchema<BudgetResponse>()
                        }
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
                }.describe {
                    summary = "Delete a budget"
                    responses {
                        HttpStatusCode.NoContent {
                            description = "The budget was successfully deleted"
                        }
                    }
                }
            }
        }
    }
}
