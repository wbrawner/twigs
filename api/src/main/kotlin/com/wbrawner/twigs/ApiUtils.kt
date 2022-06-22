package com.wbrawner.twigs

import com.wbrawner.twigs.model.Budget
import com.wbrawner.twigs.model.Permission
import com.wbrawner.twigs.model.Session
import com.wbrawner.twigs.storage.BudgetRepository
import com.wbrawner.twigs.storage.PermissionRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

suspend inline fun PipelineContext<Unit, ApplicationCall>.requireBudgetWithPermission(
    permissionRepository: PermissionRepository,
    userId: String,
    budgetId: String?,
    permission: Permission,
    otherwise: () -> Unit
) {
    if (budgetId.isNullOrBlank()) {
        errorResponse(HttpStatusCode.BadRequest, "budgetId is required")
        return
    }
    permissionRepository.findAll(
        userId = userId,
        budgetIds = listOf(budgetId)
    ).firstOrNull {
        it.permission.isAtLeast(permission)
    } ?: run {
        errorResponse(HttpStatusCode.Forbidden, "Insufficient permissions on budget $budgetId")
        otherwise()
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.budgetWithPermission(
    budgetRepository: BudgetRepository,
    permissionRepository: PermissionRepository,
    budgetId: String,
    permission: Permission,
    block: suspend (Budget) -> Unit
) {
    val session = call.principal<Session>()!!
    val userPermission = permissionRepository.findAll(
        userId = session.userId,
        budgetIds = listOf(budgetId)
    ).firstOrNull()
    if (userPermission?.permission?.isAtLeast(permission) != true) {
        errorResponse(HttpStatusCode.Forbidden)
        return
    }
    block(budgetRepository.findAll(ids = listOf(budgetId)).first())
}

suspend inline fun PipelineContext<Unit, ApplicationCall>.errorResponse(
    httpStatusCode: HttpStatusCode = HttpStatusCode.NotFound,
    message: String? = null
) {
    message?.let {
        call.respond(httpStatusCode, ErrorResponse(message))
    } ?: call.respond(httpStatusCode)
}
