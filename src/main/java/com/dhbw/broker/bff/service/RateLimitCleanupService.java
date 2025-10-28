package com.dhbw.broker.bff.service;

import com.dhbw.broker.bff.config.RateLimitConfig;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RateLimitCleanupService {

    private final RateLimitConfig.RateLimitService rateLimitService;

    public RateLimitCleanupService(RateLimitConfig.RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    // Clean up expired rate limit entries every 5 minutes
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    public void cleanupExpiredEntries() {
        rateLimitService.clearExpiredEntries();
    }
}