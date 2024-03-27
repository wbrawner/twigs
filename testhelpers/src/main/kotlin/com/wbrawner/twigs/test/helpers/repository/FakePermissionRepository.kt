package com.wbrawner.twigs.test.helpers.repository

import com.wbrawner.twigs.model.UserPermission
import com.wbrawner.twigs.storage.PermissionRepository

class FakePermissionRepository : PermissionRepository {
    val permissions: MutableList<UserPermission> = mutableListOf()
    override suspend fun findAll(budgetIds: List<String>?, userId: String?): List<UserPermission> =
        permissions.filter { userPermission ->
            budgetIds?.contains(userPermission.budgetId) ?: true
                    && userId?.let { it == userPermission.userId } ?: true
        }

    override suspend fun findAll(ids: List<String>?): List<UserPermission> {
        throw UnsupportedOperationException("UserPermission requires a userId and budgetId")
    }

    override suspend fun save(item: UserPermission): UserPermission {
        permissions.removeIf { it.budgetId == item.budgetId && it.userId == item.userId }
        permissions.add(item)
        return item
    }

    override suspend fun delete(item: UserPermission): Boolean =
        permissions.removeIf { it.budgetId == item.budgetId && it.userId == item.userId }
}