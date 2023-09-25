package com.wbrawner.twigs.service

import com.wbrawner.twigs.model.Permission
import com.wbrawner.twigs.service.budget.BudgetResponse
import com.wbrawner.twigs.storage.BudgetRepository
import com.wbrawner.twigs.storage.PermissionRepository
import io.ktor.http.*

suspend fun Pair<BudgetRepository, PermissionRepository>.budgetWithPermission(
    userId: String,
    budgetId: String,
    permission: Permission
): BudgetResponse {
    val allPermissions = second.findAll(budgetIds = listOf(budgetId))
    val userPermission = allPermissions.firstOrNull { it.userId == userId }
        ?: throw HttpException(HttpStatusCode.NotFound)
    if (!userPermission.permission.isAtLeast(permission)) {
        throw HttpException(HttpStatusCode.Forbidden)
    }
    return BudgetResponse(first.findAll(ids = listOf(budgetId)).first(), allPermissions)
}