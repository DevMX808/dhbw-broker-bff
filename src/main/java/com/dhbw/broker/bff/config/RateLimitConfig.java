package com.dhbw.broker.bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;
import java.time.Duration;

@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    @Bean
    public RateLimitService rateLimitService() {
        return new RateLimitService();
    }

    public static class RateLimitService {
        private final ConcurrentHashMap<String, RateLimitInfo> loginAttempts = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, RateLimitInfo> registerAttempts = new ConcurrentHashMap<>();
        
        public boolean isLoginAllowed(String clientIp) {
            return isAllowed(loginAttempts, clientIp, 5, Duration.ofMinutes(1));
        }
        
        public boolean isRegisterAllowed(String clientIp) {
            return isAllowed(registerAttempts, clientIp, 3, Duration.ofMinutes(5));
        }
        
        private boolean isAllowed(ConcurrentHashMap<String, RateLimitInfo> attempts, 
                                String key, int maxAttempts, Duration window) {
            LocalDateTime now = LocalDateTime.now();
            
            attempts.compute(key, (k, info) -> {
                if (info == null || now.isAfter(info.windowStart.plus(window))) {
                    return new RateLimitInfo(now, new AtomicInteger(1));
                }
                info.count.incrementAndGet();
                return info;
            });
            
            return attempts.get(key).count.get() <= maxAttempts;
        }
        
        public void clearExpiredEntries() {
            LocalDateTime cutoff = LocalDateTime.now().minus(Duration.ofMinutes(10));
            loginAttempts.entrySet().removeIf(entry -> 
                entry.getValue().windowStart.isBefore(cutoff));
            registerAttempts.entrySet().removeIf(entry -> 
                entry.getValue().windowStart.isBefore(cutoff));
        }
    }
    
    private static class RateLimitInfo {
        final LocalDateTime windowStart;
        final AtomicInteger count;
        
        RateLimitInfo(LocalDateTime windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}