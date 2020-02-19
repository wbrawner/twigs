package com.wbrawner.budgetserver.category

import com.wbrawner.budgetserver.budget.Budget
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface CategoryRepository: PagingAndSortingRepository<Category, Long> {
    fun findAllByBudget(budget: Budget, pageable: Pageable): List<Category>
    fun findAllByBudgetIn(budgets: List<Budget>, pageable: Pageable? = null): List<Category>
    fun findByBudgetInAndId(budgets: List<Budget>, id: Long): Optional<Category>
    fun findAllByBudgetInAndExpense(budgets: List<Budget>, isExpense: Boolean, pageable: Pageable? = null): List<Category>
    fun findByBudgetAndId(budget: Budget, id: Long): Optional<Category>
    fun findAllByBudgetInAndIdIn(budgets: List<Budget>, ids: List<Long>, pageable: Pageable? = null): List<Category>
}