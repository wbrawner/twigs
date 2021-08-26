package com.wbrawner.twigs.server

import com.wbrawner.twigs.storage.SessionRepository

class SessionCleanupJob(private val sessionRepository: SessionRepository) : Job {
    override suspend fun run() {
        sessionRepository.deleteExpired()
    }
}