package com.byy.blogprojectbackend.auth.handler;

import com.byy.blogprojectbackend.common.result.ApiErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler
        implements AccessDeniedHandler {

    private final SecurityErrorWriter securityErrorWriter;

    public RestAccessDeniedHandler(
            SecurityErrorWriter securityErrorWriter
    ) {
        this.securityErrorWriter = securityErrorWriter;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException, ServletException {

        securityErrorWriter.write(
                response,
                HttpStatus.FORBIDDEN,
                ApiErrorCode.FORBIDDEN,
                "没有权限执行此操作"
        );
    }
}