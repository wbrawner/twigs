package com.wbrawner.budgetserver.transaction

import com.wbrawner.budgetserver.account.Account
import com.wbrawner.budgetserver.category.Category
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface TransactionRepository: PagingAndSortingRepository<Transaction, Long> {
    fun findAllByAccount(account: Account, pageable: Pageable): List<Transaction>
    fun findByAccountAndId(account: Account, id: Long): Optional<Transaction>
    fun findAllByAccountAndCategory(account: Account, category: Category): List<Transaction>
    @Query(
            nativeQuery = true,
            value = "SELECT (COALESCE((SELECT SUM(amount) from transaction WHERE account_id = :accountId AND expense = 0), 0)) - (COALESCE((SELECT SUM(amount) from transaction WHERE account_id = :accountId AND expense = 1), 0));"
    )
    fun sumBalanceByAccountId(accountId: Long): Long

    @Query(
            nativeQuery = true,
            value = "SELECT (COALESCE((SELECT SUM(amount) from transaction WHERE category_id = :categoryId AND expense = 0), 0)) - (COALESCE((SELECT SUM(amount) from transaction WHERE category_id = :categoryId AND expense = 1), 0));"
    )
    fun sumBalanceByCategoryId(categoryId: Long): Long
}