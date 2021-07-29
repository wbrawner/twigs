package com.wbrawner.twigs.storage

import com.wbrawner.twigs.model.UserPermission

interface PermissionRepository : Repository<UserPermission> {
    fun findAllByBudgetId(budgetId: String): List<UserPermission>
    fun findAllByUserId(userId: String): List<UserPermission>
}