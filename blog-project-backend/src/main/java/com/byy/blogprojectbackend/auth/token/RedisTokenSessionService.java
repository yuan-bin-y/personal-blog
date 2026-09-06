package com.byy.blogprojectbackend.auth.token;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis 中的 JWT 会话登记服务。
 *
 * <p>JWT 签名证明 Token 没被篡改；Redis 记录证明 Token 仍处于登录有效状态，
 * 因而 logout 可以立即使尚未过期的 JWT 失效。</p>
 */
@Service
public class RedisTokenSessionService {

    private static final String KEY_PREFIX = "binspace:auth:token:";

    private final StringRedisTemplate redisTemplate;

    public RedisTokenSessionService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 登记有效 Token，并让 Redis Key 与 JWT 同时过期。 */
    public void activate(String tokenId, Long userId, Duration ttl) {
        redisTemplate.opsForValue().set(
                key(tokenId),
                userId.toString(),
                ttl
        );
    }

    /** Token 必须存在于 Redis，且必须仍属于 JWT 中声明的用户。 */
    public boolean isActive(String tokenId, String userId) {
        if (tokenId == null || userId == null) {
            return false;
        }

        String storedUserId = redisTemplate.opsForValue().get(key(tokenId));
        return userId.equals(storedUserId);
    }

    /** 删除登记记录，使当前 JWT 立即失效。 */
    public void revoke(String tokenId) {
        if (tokenId != null) {
            redisTemplate.delete(key(tokenId));
        }
    }

    private String key(String tokenId) {
        return KEY_PREFIX + tokenId;
    }
}
