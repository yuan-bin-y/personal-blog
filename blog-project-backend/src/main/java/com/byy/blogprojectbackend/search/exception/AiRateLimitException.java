package com.byy.blogprojectbackend.search.exception;

/** 公开 AI 问答超过当前限流窗口。 */
public class AiRateLimitException extends RuntimeException {
    public AiRateLimitException(String message) {
        super(message);
    }
}
