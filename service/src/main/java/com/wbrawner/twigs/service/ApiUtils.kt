package com.wbrawner.twigs.service

import com.wbrawner.twigs.model.Permission
import com.wbrawner.twigs.model.Session
import com.wbrawner.twigs.model.UserPermission
import com.wbrawner.twigs.service.budget.BudgetResponse
import com.wbrawner.twigs.storage.BudgetRepository
import com.wbrawner.twigs.storage.PermissionRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

suspend fun PermissionRepository.requirePermission(
    userId: String,
    budgetIds: List<String>,
    permission: Permission
): List<UserPermission> {
    val uniqueBudgetIds = budgetIds.toSet()
    val allPermissions = findAll(budgetIds = uniqueBudgetIds.toList(), userId = userId)
    if (allPermissions.size != uniqueBudgetIds.size) {
        throw HttpException(HttpStatusCode.NotFound)
    } else if (allPermissions.any { !it.permission.isAtLeast(permission) }) {
        throw HttpException(HttpStatusCode.Forbidden)
    }
    return allPermissions
}

suspend fun PermissionRepository.requirePermission(
    userId: String,
    budgetId: String,
    permission: Permission
): List<UserPermission> = requirePermission(userId, listOf(budgetId), permission)

suspend fun Pair<BudgetRepository, PermissionRepository>.budgetWithPermission(
    userId: String,
    budgetId: String,
    permission: Permission
): BudgetResponse {
    val budget = first.findAll(ids = listOf(budgetId)).firstOrNull() ?: throw HttpException(HttpStatusCode.NotFound)
    return BudgetResponse(budget, second.requirePermission(userId, budgetId, permission))
}

fun PipelineContext<Unit, ApplicationCall>.requireSession() = requireNotNull(call.principal<Session>()) {
    "Session required but was null"
}

suspend inline fun <reified T : Any> ApplicationCall.respondCatching(block: () -> T) =
    try {
        val response = block()
        if (response is HttpStatusCode) {
            respond(status = response, message = Unit)
        } else {
            respond(HttpStatusCode.OK, response)
        }
    } catch (e: HttpException) {
        respond(e.statusCode, e.toResponse())
    }
