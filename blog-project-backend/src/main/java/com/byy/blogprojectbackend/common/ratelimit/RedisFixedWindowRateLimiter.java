package com.byy.blogprojectbackend.common.ratelimit;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

/**
 * 通用 Redis 固定窗口限流器。
 *
 * <p>Lua 将 INCR 与首次设置过期时间放在一个原子操作中，避免并发时产生永不过期的计数键。
 * subject 先做 SHA-256，用户名、IP 等信息不会直接出现在 Redis Key 中。</p>
 */
@Component
@RequiredArgsConstructor
public class RedisFixedWindowRateLimiter {
    private static final Logger log = LoggerFactory.getLogger(RedisFixedWindowRateLimiter.class);
    private static final DefaultRedisScript<Long> INCREMENT_SCRIPT = new DefaultRedisScript<>("""
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
              redis.call('PEXPIRE', KEYS[1], ARGV[1])
            end
            return current
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public void check(String scope, String subject, int limit, Duration window, String message) {
        if (limit <= 0 || subject == null || subject.isBlank()) {
            return;
        }

        long windowNumber = Instant.now().toEpochMilli() / window.toMillis();
        String key = "binspace:rate:" + scope + ":" + digest(subject) + ":" + windowNumber;
        try {
            Long current = redisTemplate.execute(
                    INCREMENT_SCRIPT,
                    List.of(key),
                    String.valueOf(window.toMillis() + 5_000L)
            );
            if (current != null && current > limit) {
                throw new RateLimitExceededException(message);
            }
        } catch (RateLimitExceededException exception) {
            throw exception;
        } catch (DataAccessException exception) {
            // 限流是保护层，不应在 Redis 短暂抖动时把公开阅读和普通互动整体打挂。
            log.warn("Redis rate limiter unavailable, scope={}", scope);
        }
    }

    private String digest(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes, 0, 12);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
