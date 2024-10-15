package com.gym.gymsystem.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final long BLACKLIST_EXPIRATION = 10 * 60 * 60;

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token, long expirationInSeconds) {
        redisTemplate.opsForValue().set(token, "BLACKLISTED", Duration.ofSeconds(expirationInSeconds));
    }

    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }
}
