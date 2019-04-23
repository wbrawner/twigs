package com.wbrawner.budgetserver.user

import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface UserRepository: PagingAndSortingRepository<User, Long> {
    fun findByName(username: String): Optional<User>
    fun findByEmail(email: String): Optional<User>
}