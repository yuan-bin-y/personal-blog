package com.byy.blogprojectbackend.auth.handler;

import com.byy.blogprojectbackend.common.result.ApiErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** 未登录访问受保护接口时返回统一的 401 JSON，而不是跳转 HTML 登录页。 */
@Component
public class RestAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final SecurityErrorWriter securityErrorWriter;

    public RestAuthenticationEntryPoint(
            SecurityErrorWriter securityErrorWriter
    ) {
        this.securityErrorWriter = securityErrorWriter;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        securityErrorWriter.write(
                response,
                HttpStatus.UNAUTHORIZED,
                ApiErrorCode.AUTH_REQUIRED,
                "请先登录"
        );
    }
}
