package com.wbrawner.budgetserver.transaction

import com.wbrawner.budgetserver.account.Account
import com.wbrawner.budgetserver.category.Category
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface TransactionRepository: PagingAndSortingRepository<Transaction, Long> {
    fun findAllByAccount(account: Account, pageable: Pageable): List<Transaction>
    fun findByAccountAndId(account: Account, id: Long): Optional<Transaction>
    fun findAllByAccountAndCategory(account: Account, category: Category): List<Transaction>
}