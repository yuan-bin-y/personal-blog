package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.notification.service.NotificationService;
import com.byy.blogprojectbackend.notification.vo.NotificationVO;
import com.byy.blogprojectbackend.realtime.config.RealtimeProperties;
import com.byy.blogprojectbackend.realtime.registry.SseConnectionRegistry;
import com.byy.blogprojectbackend.realtime.service.impl.RealtimeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RealtimeServiceImplTest {

    private final SseConnectionRegistry registry = mock(SseConnectionRegistry.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final RealtimeProperties properties = new RealtimeProperties();
    private final RealtimeServiceImpl service = new RealtimeServiceImpl(
            registry,
            notificationService,
            properties
    );

    @Test
    void subscribe_registersConnectionAndReplaysAfterLastEventId() {
        SseEmitter emitter = mock(SseEmitter.class);
        NotificationVO notification = new NotificationVO(
                "302",
                "COMMENT_REPLIED",
                null,
                "COMMENT",
                "101",
                "玢回复了你的评论",
                false,
                Instant.parse("2026-09-08T02:00:00Z")
        );
        when(registry.register(202L)).thenReturn(emitter);
        when(notificationService.replayAfter(202L, 301L, 100))
                .thenReturn(List.of(notification));
        when(registry.sendReplay(202L, emitter, notification)).thenReturn(true);

        SseEmitter result = service.subscribe(202L, "301");

        assertSame(emitter, result);
        verify(registry).sendReplay(202L, emitter, notification);
    }

    @Test
    void subscribe_rejectsInvalidLastEventIdBeforeRegistering() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.subscribe(202L, "not-a-number")
        );
        verify(registry, never()).register(202L);
    }
}
