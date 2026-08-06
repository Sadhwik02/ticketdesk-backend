package com.ticketdesk.config;

import com.ticketdesk.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Scheduled tasks for housekeeping operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Purge expired refresh tokens daily at midnight.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void purgeExpiredRefreshTokens() {
        log.info("Purging expired refresh tokens...");
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
        log.info("Expired refresh tokens purged successfully");
    }
}
