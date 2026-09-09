package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.common.trace.MdcTaskDecorator;
import com.byy.blogprojectbackend.common.trace.TraceIdContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MdcTaskDecoratorTest {

    private final MdcTaskDecorator decorator = new MdcTaskDecorator();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldCopyTraceIdToAsyncTaskAndRestoreWorkerContext() {
        MDC.put(TraceIdContext.MDC_KEY, "request-trace-1234");
        Runnable decoratedTask = decorator.decorate(() ->
                assertThat(MDC.get(TraceIdContext.MDC_KEY)).isEqualTo("request-trace-1234")
        );

        MDC.clear();
        MDC.put(TraceIdContext.MDC_KEY, "worker-trace-1234");
        decoratedTask.run();

        assertThat(MDC.get(TraceIdContext.MDC_KEY)).isEqualTo("worker-trace-1234");
    }
}
