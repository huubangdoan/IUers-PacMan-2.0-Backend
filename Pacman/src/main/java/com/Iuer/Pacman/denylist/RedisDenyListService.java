package com.Iuer.Pacman.denylist;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
public class RedisDenyListService implements DenyListService {

    private static final String JTI_PREFIX = "denylist:jti:";
    private static final String USER_PREFIX = "denylist:user:";

    private final StringRedisTemplate redis;
    private final long tokenLifetimeMs;

    public RedisDenyListService(StringRedisTemplate redis,
                                @Value("${jwt.expiration-ms:86400000}") long tokenLifetimeMs) {
        this.redis = redis;
        this.tokenLifetimeMs = tokenLifetimeMs;
    }

    @Override
    public void deny(String jti, long ttlMillis) {
        if (ttlMillis <= 0) return; 
        redis.opsForValue().set(JTI_PREFIX + jti, "1", Duration.ofMillis(ttlMillis));
    }

    @Override
    public boolean isDenied(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(JTI_PREFIX + jti));
    }

    @Override
    public void revokeAllOfUser(String username) {
        redis.opsForValue().set(USER_PREFIX + username,
                String.valueOf(System.currentTimeMillis()), Duration.ofMillis(tokenLifetimeMs));
    }

    @Override
    public boolean isIssuedBeforeRevoke(String username, Date issuedAt) {
        String mark = redis.opsForValue().get(USER_PREFIX + username);
        return mark != null && issuedAt.getTime() <= Long.parseLong(mark);
    }
}