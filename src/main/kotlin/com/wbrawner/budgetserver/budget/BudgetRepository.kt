package com.wbrawner.budgetserver.budget

import org.springframework.data.repository.PagingAndSortingRepository

interface BudgetRepository: PagingAndSortingRepository<Budget, Long> {
    fun findAllByIdIn(ids: List<Long>): List<Budget>
//    fun findByUsersContainsAndId(user: User, id: Long): Optional<Budget>
//    fun findByUsersContainsAndTransactionsContains(user: User, transaction: Transaction): Optional<Budget>
//    fun findByUsersContainsAndCategoriesContains(user: User, category: Category): Optional<Budget>
}