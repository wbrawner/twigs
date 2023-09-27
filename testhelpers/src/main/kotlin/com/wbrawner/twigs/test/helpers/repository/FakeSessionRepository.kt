package com.wbrawner.twigs.test.helpers.repository

import com.wbrawner.twigs.model.Session
import com.wbrawner.twigs.storage.SessionRepository
import java.util.function.Predicate

class FakeSessionRepository : FakeRepository<Session>(), SessionRepository {
    var expirationPredicate: Predicate<in Session> = Predicate { false }

    override fun findAll(token: String): List<Session> = entities.filter { it.token == token }

    override fun deleteExpired() {
        entities.removeIf(expirationPredicate)
    }
}