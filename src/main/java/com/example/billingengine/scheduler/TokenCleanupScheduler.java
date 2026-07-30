package com.example.billingengine.scheduler;

import com.example.billingengine.controller.AuthController;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenCleanupScheduler {

    // Every Monday at 3:00 AM server time
    @Scheduled(cron = "0 0 3 * * MON")
    public void cleanUpExpiredTokens() {
        AuthController.purgeExpiredTokens();
    }
}