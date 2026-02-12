package com.authentication.jwt.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:";

    /**
     * Blacklists a JWT access token.
     * The token stays in Redis until its natural expiration time.
     */
    public void blacklistToken(String jwt, long expiryMillis) {
        long ttlSeconds = (expiryMillis - System.currentTimeMillis()) / 1000;
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + jwt,
                    "blacklisted",
                    ttlSeconds,
                    TimeUnit.SECONDS
            );
        }
    }

    /**
     * Checks if a JWT access token has been blacklisted.
     */
    public boolean isBlacklisted(String jwt) {
        if (jwt == null) return false;
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jwt));
    }
}
