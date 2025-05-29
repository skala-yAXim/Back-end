package com.yaxim.global.auth.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final String ACCESS_TOKEN = "access:";
    private final String REFRESH_TOKEN = "refresh:";
    private final RedisTemplate<String, String> redisTemplate;

    public void storeAccessTokenJti(String userId, String jti, Duration ttl) {
        redisTemplate.opsForValue().set(ACCESS_TOKEN + userId, jti, ttl);
    }

    public boolean isAccessTokenInvalid(String userId, String jti) {
        String storedJti = redisTemplate.opsForValue().get(ACCESS_TOKEN + userId);
        return !jti.equals(storedJti);
    }

    public void invalidateAccessToken(String userId) {
        redisTemplate.delete(ACCESS_TOKEN + userId);
    }

    public void storeRefreshTokenJti(String userId, String jti, Duration ttl) {
        redisTemplate.opsForValue().set(REFRESH_TOKEN + userId, jti, ttl);
    }

    public boolean isRefreshTokenInvalid(String userId, String jti) {
        String storedJti = redisTemplate.opsForValue().get(REFRESH_TOKEN + userId);
        return !jti.equals(storedJti);
    }

    public void resetRefreshTokenJti(String userId, String token, Duration ttl) {
        redisTemplate.opsForValue().setIfPresent(REFRESH_TOKEN + userId, token, ttl);
    }

    public void invalidateRefreshToken(String userId) {
        redisTemplate.delete(REFRESH_TOKEN + userId);
    }
}
