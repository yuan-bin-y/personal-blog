package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.common.trace.TraceIdContext;
import com.byy.blogprojectbackend.common.trace.TraceIdFilter;
import com.byy.blogprojectbackend.common.result.Result;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdFilterTest {

    private final TraceIdFilter filter = new TraceIdFilter();

    @Test
    void shouldReuseTrustedIncomingTraceIdAndClearMdcAfterRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/posts");
        request.addHeader(TraceIdContext.HEADER_NAME, "trace-12345678");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> responseTraceId = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                {
                    assertThat(MDC.get(TraceIdContext.MDC_KEY)).isEqualTo("trace-12345678");
                    responseTraceId.set(Result.success(null).traceId());
                }
        );

        assertThat(response.getHeader(TraceIdContext.HEADER_NAME)).isEqualTo("trace-12345678");
        assertThat(responseTraceId.get()).isEqualTo("trace-12345678");
        assertThat(MDC.get(TraceIdContext.MDC_KEY)).isNull();
    }

    @Test
    void shouldReplaceUnsafeIncomingTraceId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/posts");
        request.addHeader(TraceIdContext.HEADER_NAME, "unsafe\ntrace-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
        });

        String traceId = response.getHeader(TraceIdContext.HEADER_NAME);
        assertThat(traceId).matches("[A-Za-z0-9-]{8,64}");
        assertThat(traceId).isNotEqualTo("unsafe\ntrace-id");
    }
}
