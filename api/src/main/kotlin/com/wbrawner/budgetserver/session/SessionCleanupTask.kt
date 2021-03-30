package com.wbrawner.budgetserver.session

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
open class SessionCleanupTask @Autowired constructor(private val sessionRepository: UserSessionRepository) {
    @Scheduled(cron = "0 0 * * * *")
    open fun cleanup() {
        sessionRepository.deleteAllByExpirationBefore(Date())
    }
}