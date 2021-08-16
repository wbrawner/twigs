package com.wbrawner.twigs.storage

import com.wbrawner.twigs.model.Session

interface SessionRepository : Repository<Session> {
    fun findAll(
        token: String
    ): List<Session>

    fun deleteExpired()
}