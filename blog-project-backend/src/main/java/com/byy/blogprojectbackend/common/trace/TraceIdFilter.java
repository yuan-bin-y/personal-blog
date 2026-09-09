package com.byy.blogprojectbackend.common.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 为每一个 HTTP 请求建立 MDC 链路，并将 traceId 回传给调用方。
 *
 * <p>只接受长度受限的字母、数字和连字符请求头，避免日志注入；其余情况由服务端生成 UUID。</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);
    private static final int MAX_TRACE_ID_LENGTH = 64;
    private static final Pattern TRUSTED_TRACE_ID = Pattern.compile("[A-Za-z0-9-]{8,64}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String traceId = resolveTraceId(request.getHeader(TraceIdContext.HEADER_NAME));
        long startedAt = System.nanoTime();

        TraceIdContext.set(traceId);
        response.setHeader(TraceIdContext.HEADER_NAME, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.info(
                    "HTTP 请求完成，method={} path={} status={} elapsedMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    elapsedMs
            );
            TraceIdContext.clear();
        }
    }

    private String resolveTraceId(String requestedTraceId) {
        if (requestedTraceId != null
                && requestedTraceId.length() <= MAX_TRACE_ID_LENGTH
                && TRUSTED_TRACE_ID.matcher(requestedTraceId).matches()) {
            return requestedTraceId;
        }
        return UUID.randomUUID().toString();
    }
}
