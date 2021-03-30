package com.wbrawner.budgetserver.session

import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface UserSessionRepository : PagingAndSortingRepository<Session, String> {
    fun findByUserId(userId: String?): List<Session?>?
    fun findByToken(token: String?): Optional<Session?>?
    fun findByUserIdAndToken(userId: String?, token: String?): Optional<Session?>?
    fun deleteAllByExpirationBefore(expiration: Date?)
}