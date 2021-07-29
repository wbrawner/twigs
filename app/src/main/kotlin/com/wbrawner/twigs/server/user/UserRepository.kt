package com.wbrawner.twigs.server.user

import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface UserRepository : PagingAndSortingRepository<User, String> {
    fun findByName(name: String?): Optional<User>
    fun findByNameContains(name: String?): List<User>
    fun findByEmail(email: String?): Optional<User>
}