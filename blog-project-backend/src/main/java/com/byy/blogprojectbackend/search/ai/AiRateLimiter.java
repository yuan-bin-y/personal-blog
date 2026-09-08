package com.byy.blogprojectbackend.search.ai;

import com.byy.blogprojectbackend.search.config.AiProperties;
import com.byy.blogprojectbackend.search.exception.AiRateLimitException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/** 基于 Redis 固定分钟窗口限制公开 AI 问答滥用。 */
@Component
@RequiredArgsConstructor
public class AiRateLimiter {
    private static final Logger log = LoggerFactory.getLogger(AiRateLimiter.class);
    private final StringRedisTemplate redisTemplate;
    private final AiProperties properties;

    public void check(String clientKey) {
        long minute = Instant.now().getEpochSecond() / 60;
        String key = "binspace:ai:rate:" + clientKey + ":" + minute;

        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, Duration.ofSeconds(70));
            }
            if (count != null && count > properties.getRequestsPerMinute()) {
                throw new AiRateLimitException("请求过于频繁，请稍后再试");
            }
        } catch (RedisConnectionFailureException exception) {
            // Redis 短暂不可用时不应把公开搜索整体拖垮，但要留有可观测日志。
            log.warn("AI rate limiter skipped because Redis is unavailable");
        }
    }
}
