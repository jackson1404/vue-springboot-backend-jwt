package com.jackson.vue.jwt_backend_integrate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RedisTokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    private String key(String jti){
        return "bl:" + jti;
    }

    public void blacklist(String jti, Instant expiryInstant){
        long remainSecond = expiryInstant.getEpochSecond() - Instant.now().getEpochSecond();
        if (remainSecond <= 0) return;
        redisTemplate.opsForValue().set(key(jti), "1", Duration.ofSeconds(remainSecond));
    }

    public boolean isBlacklist(String jti){
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(jti)));
    }




}
