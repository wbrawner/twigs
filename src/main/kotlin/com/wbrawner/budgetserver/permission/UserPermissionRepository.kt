package com.wbrawner.budgetserver.permission

import com.wbrawner.budgetserver.budget.Budget
import com.wbrawner.budgetserver.user.User
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface UserPermissionRepository : PagingAndSortingRepository<UserPermission, UserPermissionKey> {
    fun findAllByUserAndBudget_Id(user: User, budgetId: Long, pageable: Pageable?): List<UserPermission>
    fun findAllByUser(user: User, pageable: Pageable?): List<UserPermission>
    fun findAllByBudget(budget: Budget, pageable: Pageable?): List<UserPermission>
    fun findAllByUserAndBudget(user: User, budget: Budget, pageable: Pageable?): List<UserPermission>
    fun findAllByUserAndBudget_IdIn(user: User, budgetIds: List<Long>, pageable: Pageable?): List<UserPermission>
}