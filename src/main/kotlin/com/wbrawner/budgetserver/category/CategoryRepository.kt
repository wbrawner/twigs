package com.wbrawner.budgetserver.category

import com.wbrawner.budgetserver.account.Account
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface CategoryRepository: PagingAndSortingRepository<Category, Long> {
    fun findAllByAccount(account: Account, pageable: Pageable): List<Category>
    fun findByAccountAndId(account: Account, id: Long): Optional<Category>
}