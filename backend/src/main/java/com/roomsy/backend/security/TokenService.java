package com.roomsy.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    private static final String ACCESS_TOKEN_PREFIX = "access_token:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USER_TOKENS_PREFIX = "user_tokens:";

    @Autowired
    public TokenService(RedisTemplate<String, String> redisTemplate, JwtUtil jwtUtil) {
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }

    public void storeAccessToken(UUID userId, String token) {
        String key = ACCESS_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(
                key,
                userId.toString(),
                jwtUtil.getAccessTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        // Also store in user's token list
        String userKey = USER_TOKENS_PREFIX + userId + ":access";
        redisTemplate.opsForSet().add(userKey, token);
        redisTemplate.expire(userKey, jwtUtil.getAccessTokenExpiration(), TimeUnit.MILLISECONDS);
    }

    public void storeRefreshToken(UUID userId, String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(
                key,
                userId.toString(),
                jwtUtil.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        // Also store in user's token list
        String userKey = USER_TOKENS_PREFIX + userId + ":refresh";
        redisTemplate.opsForSet().add(userKey, token);
        redisTemplate.expire(userKey, jwtUtil.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);
    }

    public boolean isAccessTokenValid(String token) {
        String key = ACCESS_TOKEN_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public boolean isRefreshTokenValid(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void revokeAccessToken(String token) {
        String key = ACCESS_TOKEN_PREFIX + token;
        String userId = redisTemplate.opsForValue().get(key);

        redisTemplate.delete(key);

        if (userId != null) {
            String userKey = USER_TOKENS_PREFIX + userId + ":access";
            redisTemplate.opsForSet().remove(userKey, token);
        }
    }

    public void revokeRefreshToken(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        String userId = redisTemplate.opsForValue().get(key);

        redisTemplate.delete(key);

        if (userId != null) {
            String userKey = USER_TOKENS_PREFIX + userId + ":refresh";
            redisTemplate.opsForSet().remove(userKey, token);
        }
    }

    public void revokeAllUserTokens(UUID userId) {
        // Revoke all access tokens
        String accessKey = USER_TOKENS_PREFIX + userId + ":access";
        var accessTokens = redisTemplate.opsForSet().members(accessKey);
        if (accessTokens != null) {
            for (String token : accessTokens) {
                redisTemplate.delete(ACCESS_TOKEN_PREFIX + token);
            }
            redisTemplate.delete(accessKey);
        }

        // Revoke all refresh tokens
        String refreshKey = USER_TOKENS_PREFIX + userId + ":refresh";
        var refreshTokens = redisTemplate.opsForSet().members(refreshKey);
        if (refreshTokens != null) {
            for (String token : refreshTokens) {
                redisTemplate.delete(REFRESH_TOKEN_PREFIX + token);
            }
            redisTemplate.delete(refreshKey);
        }
    }
}