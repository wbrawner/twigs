package com.wbrawner.twigs.server.permission

import com.wbrawner.twigs.server.budget.Budget
import com.wbrawner.twigs.server.user.User
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface UserPermissionRepository : PagingAndSortingRepository<UserPermission, UserPermissionKey> {
    fun findByUserAndBudget_Id(user: User?, budgetId: String?): Optional<UserPermission>
    fun findAllByUser(user: User?, pageable: Pageable?): List<UserPermission>
    fun findAllByBudget(budget: Budget?, pageable: Pageable?): List<UserPermission>
    fun findAllByUserAndBudget(user: User?, budget: Budget?, pageable: Pageable?): List<UserPermission>
    fun findAllByUserAndBudget_IdIn(user: User?, budgetIds: List<String?>?, pageable: Pageable?): List<UserPermission>
}