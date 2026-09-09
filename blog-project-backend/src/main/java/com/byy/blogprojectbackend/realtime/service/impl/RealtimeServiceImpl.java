package com.byy.blogprojectbackend.realtime.service.impl;

import com.byy.blogprojectbackend.notification.service.NotificationService;
import com.byy.blogprojectbackend.realtime.config.RealtimeProperties;
import com.byy.blogprojectbackend.realtime.registry.SseConnectionRegistry;
import com.byy.blogprojectbackend.realtime.service.RealtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class RealtimeServiceImpl implements RealtimeService {

    private static final String INVALID_LAST_EVENT_ID = "Last-Event-ID 必须是正整数";

    private final SseConnectionRegistry registry;
    private final NotificationService notificationService;
    private final RealtimeProperties properties;

    @Override
    public SseEmitter subscribe(Long userId, String lastEventId) {
        Long afterId = parseLastEventId(lastEventId);
        SseEmitter emitter = registry.register(userId);

        if (afterId != null) {
            for (var notification : notificationService.replayAfter(
                    userId,
                    afterId,
                    properties.getReplayLimit()
            )) {
                if (!registry.sendReplay(userId, emitter, notification)) {
                    break;
                }
            }
        }
        return emitter;
    }

    private Long parseLastEventId(String lastEventId) {
        if (lastEventId == null || lastEventId.isBlank()) {
            return null;
        }
        final long value;
        try {
            value = Long.parseLong(lastEventId.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(INVALID_LAST_EVENT_ID);
        }
        if (value <= 0) {
            throw new IllegalArgumentException(INVALID_LAST_EVENT_ID);
        }
        return value;
    }
}
