package com.byy.blogprojectbackend.auth.handler;

import com.byy.blogprojectbackend.common.result.ApiErrorCode;
import com.byy.blogprojectbackend.common.result.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 将 Spring Security 过滤器阶段的异常统一写成项目 JSON 错误结构。
 *
 * <p>过滤器异常发生在 Controller 之前，不能交给 GlobalExceptionHandler 处理。</p>
 */
@Component
public class SecurityErrorWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** 将给定 HTTP 状态和业务错误码直接写入响应流。 */
    public void write(
            HttpServletResponse response,
            HttpStatus status,
            ApiErrorCode code,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Result<Void> error = Result.failure(code, message);

        objectMapper.writeValue(
                response.getOutputStream(),
                error
        );
    }
}
