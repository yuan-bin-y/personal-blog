package com.byy.blogprojectbackend.search.exception;

/** AI 提供方未配置、超时或返回无效结果。 */
public class AiUpstreamException extends RuntimeException {
    public AiUpstreamException(String message) {
        super(message);
    }

    public AiUpstreamException(String message, Throwable cause) {
        super(message, cause);
    }
}
