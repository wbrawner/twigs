package com.wbrawner.twigs.service.budget

import com.wbrawner.twigs.model.Budget
import com.wbrawner.twigs.model.Permission
import com.wbrawner.twigs.model.UserPermission
import com.wbrawner.twigs.service.HttpException
import com.wbrawner.twigs.service.budgetWithPermission
import com.wbrawner.twigs.service.user.UserPermissionRequest
import com.wbrawner.twigs.storage.BudgetRepository
import com.wbrawner.twigs.storage.PermissionRepository
import io.ktor.http.*

interface BudgetService {
    suspend fun budgetsForUser(userId: String): List<BudgetResponse>

    suspend fun budget(budgetId: String, userId: String): BudgetResponse

    suspend fun save(request: BudgetRequest, userId: String, budgetId: String? = null): BudgetResponse

    suspend fun delete(budgetId: String, userId: String)
}

class DefaultBudgetService(
    private val budgetRepository: BudgetRepository,
    private val permissionRepository: PermissionRepository
) : BudgetService {
    private val budgetPermissionRepository = budgetRepository to permissionRepository
    override suspend fun budgetsForUser(userId: String): List<BudgetResponse> {
        val budgetIds = permissionRepository.findAll(userId = userId).map { it.budgetId }
        if (budgetIds.isEmpty()) {
            return emptyList()
        }
        return budgetRepository.findAll(ids = budgetIds).map {
            BudgetResponse(it, permissionRepository.findAll(budgetIds = listOf(it.id)))
        }
    }

    override suspend fun budget(budgetId: String, userId: String): BudgetResponse =
        budgetPermissionRepository.budgetWithPermission(userId, budgetId, Permission.READ)

    override suspend fun save(request: BudgetRequest, userId: String, budgetId: String?): BudgetResponse {
        val budget = if (budgetId?.isNotBlank() == true) {
            budgetPermissionRepository.budgetWithPermission(
                budgetId = budgetId,
                userId = userId,
                permission = Permission.MANAGE
            ).run {
                Budget(
                    id = budgetId,
                    name = request.name ?: name,
                    description = request.description ?: description
                )
            }
        } else {
            if (request.name.isNullOrBlank()) {
                throw HttpException(HttpStatusCode.BadRequest, message = "Name cannot be empty or null")
            }
            Budget(
                name = request.name,
                description = request.description
            )
        }
        val users = budgetId?.let {
            // If user is owner, apply changes
            // If user is manager, make sure they're not changing ownership
            val oldUsers = permissionRepository.findAll(budgetIds = listOf(budgetId))
            val oldPermissions = oldUsers.associate { it.userId to it.permission }
            val currentUserPermission = oldPermissions[userId] ?: throw HttpException(HttpStatusCode.NotFound)
            val newUsers = request.users?.map { userPermission ->
                if (userPermission.permission == Permission.OWNER
                    && oldPermissions[userPermission.user] !== Permission.OWNER
                    && currentUserPermission != Permission.OWNER
                ) {
                    // The user is attempting to add a new owner
                    throw HttpException(
                        HttpStatusCode.Forbidden,
                        message = "You must be an owner to be able to modify other users' ownership"
                    )
                }
                userPermission
            } ?: return@let oldUsers.map { UserPermissionRequest(it.userId, it.permission) }
            oldPermissions.filterValues { it == Permission.OWNER }
                .forEach { (user, permission) ->
                    if (newUsers.none { it.user == user && it.permission == permission }
                        && currentUserPermission != Permission.OWNER
                    ) {
                        // The user is attempting to remove a previous owner
                        throw HttpException(
                            HttpStatusCode.Forbidden,
                            message = "You must be an owner to be able to modify other users' ownership"
                        )
                    }
                }
            oldUsers.forEach { oldUserPermission ->
                if (newUsers.none { it.user == oldUserPermission.userId && it.permission == oldUserPermission.permission }) {
                    permissionRepository.delete(oldUserPermission)
                }
            }
            newUsers
        } ?: run {
            val newUsers = request.users
                ?.toMutableList()
                ?: mutableListOf()
            val currentUserPermission = newUsers.firstOrNull { it.user == userId }
            if (currentUserPermission == null || currentUserPermission.permission != Permission.OWNER) {
                newUsers.removeIf { it.user == userId }
                newUsers.add(UserPermissionRequest(userId, Permission.OWNER))
            }
            newUsers
        }
        val savedBudget = budgetRepository.save(budget)
        return BudgetResponse(
            savedBudget,
            users.map {
                permissionRepository.save(
                    UserPermission(
                        budgetId = savedBudget.id,
                        userId = it.user,
                        permission = it.permission
                    )
                )
            }
        )
    }

    override suspend fun delete(budgetId: String, userId: String) {
        val budgetResponse = budgetPermissionRepository.budgetWithPermission(userId, budgetId, Permission.OWNER)
        budgetRepository.delete(Budget(budgetResponse.id, budgetResponse.name, budgetResponse.description))
    }
}