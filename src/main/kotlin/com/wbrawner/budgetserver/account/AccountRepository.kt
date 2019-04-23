package com.wbrawner.budgetserver.account

import com.wbrawner.budgetserver.category.Category
import com.wbrawner.budgetserver.transaction.Transaction
import com.wbrawner.budgetserver.user.User
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface AccountRepository: PagingAndSortingRepository<Account, Long> {
    fun findAllByUsersContainsOrOwner(user: User, owner: User = user): List<Account>
    fun findByUsersContainsAndId(user: User, id: Long): Optional<Account>
    fun findByUsersContainsAndTransactionsContains(user: User, transaction: Transaction): Optional<Account>
    fun findByUsersContainsAndCategoriesContains(user: User, category: Category): Optional<Account>
}