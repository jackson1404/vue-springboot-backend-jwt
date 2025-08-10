package com.jackson.vue.jwt_backend_integrate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisTokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    private String key(String jti){
        return "bl:" + jti;
    }

    public void blacklist(String jti, Instant expiryInstant){
        long remainSecond = expiryInstant.getEpochSecond() - Instant.now().getEpochSecond();
        if (remainSecond <= 0) return;
        redisTemplate.opsForValue().set(key(jti), "1", Duration.ofSeconds(remainSecond));
        log.info("Token blacklisted: {} with expiry: {}", jti, expiryInstant);
    }

    public boolean isBlacklist(String jti){
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(jti)));
    }

    // Manual cleanup method
    public void cleanupExpiredTokens() {
        try {
            Set<String> keys = redisTemplate.keys("bl:*");
            if (keys != null && !keys.isEmpty()) {
                long count = keys.size();
                redisTemplate.delete(keys);
                log.info("Cleaned up {} expired blacklisted tokens", count);
            }
        } catch (Exception e) {
            log.error("Error during cleanup: {}", e.getMessage());
        }
    }

    // Get count of blacklisted tokens
    public long getBlacklistedTokenCount() {
        try {
            Set<String> keys = redisTemplate.keys("bl:*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            log.error("Error getting blacklisted token count: {}", e.getMessage());
            return 0;
        }
    }

    // Force delete a specific token from blacklist
    public void removeFromBlacklist(String jti) {
        try {
            redisTemplate.delete(key(jti));
            log.info("Removed token from blacklist: {}", jti);
        } catch (Exception e) {
            log.error("Error removing token from blacklist: {}", e.getMessage());
        }
    }

    // Scheduled cleanup - runs every hour
    @Scheduled(fixedRate = 3600000) // 1 hour = 3600000 milliseconds
    public void scheduledCleanup() {
        log.info("Starting scheduled cleanup of expired blacklisted tokens");
        long beforeCount = getBlacklistedTokenCount();
        cleanupExpiredTokens();
        long afterCount = getBlacklistedTokenCount();
        log.info("Scheduled cleanup completed. Tokens before: {}, after: {}", beforeCount, afterCount);
    }

    // Scheduled cleanup - runs daily at 2 AM
    @Scheduled(cron = "0 0 2 * * ?") // Every day at 2:00 AM
    public void dailyCleanup() {
        log.info("Starting daily cleanup of expired blacklisted tokens");
        long beforeCount = getBlacklistedTokenCount();
        cleanupExpiredTokens();
        long afterCount = getBlacklistedTokenCount();
        log.info("Daily cleanup completed. Tokens before: {}, after: {}", beforeCount, afterCount);
    }
}
