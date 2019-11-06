package com.wbrawner.budgetserver.budget

import com.wbrawner.budgetserver.category.Category
import com.wbrawner.budgetserver.transaction.Transaction
import com.wbrawner.budgetserver.user.User
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface BudgetRepository: PagingAndSortingRepository<Budget, Long> {
    fun findAllByIdIn(ids: List<Long>): List<Budget>
    fun findAllByUsersContainsOrOwner(user: User, owner: User = user, pageable: Pageable? = null): List<Budget>
    fun findByUsersContainsAndId(user: User, id: Long): Optional<Budget>
    fun findByUsersContainsAndTransactionsContains(user: User, transaction: Transaction): Optional<Budget>
    fun findByUsersContainsAndCategoriesContains(user: User, category: Category): Optional<Budget>
}