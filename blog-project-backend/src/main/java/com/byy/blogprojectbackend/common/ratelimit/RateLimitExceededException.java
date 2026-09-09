package com.byy.blogprojectbackend.common.ratelimit;

/** 请求超过 Redis 限流窗口时抛出的统一业务异常。 */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
