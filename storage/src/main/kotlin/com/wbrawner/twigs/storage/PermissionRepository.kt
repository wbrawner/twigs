package com.wbrawner.twigs.storage

import com.wbrawner.twigs.model.UserPermission

interface PermissionRepository : Repository<UserPermission> {
    fun findAll(
        budgetIds: List<String>? = null,
        userId: String? = null
    ): List<UserPermission>
}