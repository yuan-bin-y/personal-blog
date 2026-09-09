package com.byy.blogprojectbackend.common.trace;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * 请求链路标识的唯一入口。
 *
 * <p>HTTP Filter 会在请求开始时写入 MDC。非 HTTP 场景调用
 * {@link #getOrCreate()} 会得到用于响应对象的临时标识，但不会污染复用线程的 MDC。</p>
 */
public final class TraceIdContext {

    public static final String MDC_KEY = "traceId";
    public static final String HEADER_NAME = "X-Trace-Id";

    private TraceIdContext() {
    }

    /** 返回当前链路 ID；当前线程尚未建立 HTTP 链路时返回一个临时 UUID。 */
    public static String getOrCreate() {
        String traceId = MDC.get(MDC_KEY);
        if (traceId == null || traceId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return traceId;
    }

    public static void set(String traceId) {
        MDC.put(MDC_KEY, traceId);
    }

    public static void clear() {
        MDC.remove(MDC_KEY);
    }
}
