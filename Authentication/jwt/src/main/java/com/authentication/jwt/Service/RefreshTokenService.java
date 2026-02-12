package com.authentication.jwt.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final String USER_REFRESH_PREFIX = "user_refresh:";

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry; // 7 days in milliseconds

    /**
     * Creates a new refresh token for the given email.
     * Stores mapping in both directions: token→email and email→token
     */
    public String createRefreshToken(String email) {
        // Delete any existing refresh token for this user
        deleteAllUserTokens(email);

        String refreshToken = UUID.randomUUID().toString();
        long expirySeconds = refreshTokenExpiry / 1000;

        // Store token → email mapping
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + refreshToken,
                email,
                expirySeconds,
                TimeUnit.SECONDS
        );

        // Store email → token mapping (for cleanup)
        redisTemplate.opsForValue().set(
                USER_REFRESH_PREFIX + email,
                refreshToken,
                expirySeconds,
                TimeUnit.SECONDS
        );

        return refreshToken;
    }

    /**
     * Validates a refresh token and returns the associated email.
     * Returns null if the token is invalid or expired.
     */
    public String validateRefreshToken(String refreshToken) {
        if (refreshToken == null) return null;
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + refreshToken);
    }

    /**
     * Deletes a specific refresh token.
     */
    public void deleteRefreshToken(String refreshToken) {
        if (refreshToken == null) return;
        String email = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + refreshToken);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + refreshToken);
        if (email != null) {
            redisTemplate.delete(USER_REFRESH_PREFIX + email);
        }
    }

    /**
     * Deletes all refresh tokens for a user (useful on password change).
     */
    public void deleteAllUserTokens(String email) {
        String existingToken = redisTemplate.opsForValue().get(USER_REFRESH_PREFIX + email);
        if (existingToken != null) {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + existingToken);
        }
        redisTemplate.delete(USER_REFRESH_PREFIX + email);
    }
}
