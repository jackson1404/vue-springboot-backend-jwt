package com.jackson.vue.jwt_backend_integrate.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginAttemptService {

    private final RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();

    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCK_DURATION_MINUTES = 15; // Lock for 15 minutes
    private static final String ATTEMPT_KEY_PREFIX = "login_attempts:";
    private static final String LOCK_KEY_PREFIX = "account_locked:";

    public void recordFailedAttempts(String username){
        String attemptKey = ATTEMPT_KEY_PREFIX + username;
        String lockKey = LOCK_KEY_PREFIX + username;

        try {
            Long attempts = redisTemplate.opsForValue().increment(attemptKey);

            redisTemplate.expire(attemptKey, 24, TimeUnit.HOURS);

            log.info("Failed login attempt for user: {}. Total attempts: {}", username, attempts);

            if (attempts != null && attempts >= MAX_ATTEMPTS){
                lockAccount(username);
            }
        } catch (Exception e){
            log.error("Error recording failed login attempt for user: {}", username, e);
        }
    }

    public void recordSuccessAttempts(String username){
        String attemptKey = ATTEMPT_KEY_PREFIX + username;
        String lockKey = LOCK_KEY_PREFIX + username;

        try {
            // Reset failed attempts
            redisTemplate.delete(attemptKey);
            // Remove lock if exists
            redisTemplate.delete(lockKey);

            log.info("Successful login for user: {}. Reset failed attempts.", username);
        } catch (Exception e) {
            log.error("Error recording successful login for user: {}", username, e);
        }
    }

    public boolean isAccountLocked(String username){
        String lockKey = LOCK_KEY_PREFIX + username;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
        } catch (Exception e){
            log.error("Error checking account lock status for user: {}", username, e);
            return false;
        }
    }

    public int getRemainingAttempts(String username){
        String lockKey = LOCK_KEY_PREFIX + username;
        try {
            String attemptStr = redisTemplate.opsForValue().get(lockKey);
            if (attemptStr == null) return MAX_ATTEMPTS;
            int attempts = MAX_ATTEMPTS - Integer.parseInt(attemptStr);
            return Math.max(0, attempts);
        } catch (Exception e){
            log.error("Error getting remaining attempts for user: {}", username, e);
            return MAX_ATTEMPTS;
        }
    }

    public Long getLockExpirationTime(String username) {
        String lockKey = LOCK_KEY_PREFIX + username;
        try {
            return redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error getting lock expiration for user: {}", username, e);
            return null;
        }
    }

    public void unlockAccount(String username) {
        String attemptKey = ATTEMPT_KEY_PREFIX + username;
        String lockKey = LOCK_KEY_PREFIX + username;

        try {
            redisTemplate.delete(attemptKey);
            redisTemplate.delete(lockKey);
            log.info("Account unlocked for user: {}", username);
        } catch (Exception e) {
            log.error("Error unlocking account for user: {}", username, e);
        }
    }

    public int getFailedAttempts(String username) {
        String attemptKey = ATTEMPT_KEY_PREFIX + username;
        try {
            String attemptsStr = redisTemplate.opsForValue().get(attemptKey);
            return attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;
        } catch (Exception e) {
            log.error("Error getting failed attempts for user: {}", username, e);
            return 0;
        }
    }

    private void lockAccount(String username) {
        String lockKey = LOCK_KEY_PREFIX + username;
        try {
            redisTemplate.opsForValue().set(lockKey, "locked", Duration.ofMinutes(LOCK_DURATION_MINUTES));
            log.warn("Account locked for user: {} due to {} failed attempts", username, MAX_ATTEMPTS);
        } catch (Exception e) {
            log.error("Error locking account for user: {}", username, e);
        }
    }


}
