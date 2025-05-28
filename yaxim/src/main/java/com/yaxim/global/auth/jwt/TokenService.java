package com.yaxim.global.auth.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RedisTemplate<String, String> redisTemplate;

    public void storeRefreshTokenJti(String userId, String jti) {
        redisTemplate.opsForValue().set(userId, jti);
    }

    public void resetRefreshTokenJti(String userId, String token) {
        redisTemplate.opsForValue().setIfPresent(userId, token);
    }

    public boolean isRefreshTokenValid(String userId, String jti) {
        String storedJti = redisTemplate.opsForValue().get(userId);
        return jti.equals(storedJti);
    }
}
