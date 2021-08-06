package com.wbrawner.twigs.storage

interface SessionRepository : Repository<Session> {
    fun findAll(
        token: String
    ): List<Session>

    fun deleteExpired()
}