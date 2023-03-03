package com.wbrawner.twigs.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class SessionCleanupTask {
    private final UserSessionRepository sessionRepository;

    @Autowired
    public SessionCleanupTask(UserSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanup() {
        sessionRepository.deleteAllByExpirationBefore(new Date());
    }
}
