package com.byy.blogprojectbackend.realtime.registry;

import com.byy.blogprojectbackend.notification.vo.NotificationVO;
import com.byy.blogprojectbackend.realtime.config.RealtimeProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 保存当前实例上的用户 SSE 连接。一个用户可能同时打开多个浏览器标签页，
 * 因此 userId 对应一组连接而不是一个连接。
 */
@Component
@RequiredArgsConstructor
public class SseConnectionRegistry {

    private static final Logger log = LoggerFactory.getLogger(SseConnectionRegistry.class);
    private final RealtimeProperties properties;
    private final ConcurrentHashMap<Long, Set<SseEmitter>> connections =
            new ConcurrentHashMap<>();

    public SseEmitter register(Long userId) {
        SseEmitter emitter = new SseEmitter(properties.getTimeoutMs());
        connections.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet())
                .add(emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> {
            remove(userId, emitter);
            emitter.complete();
        });
        emitter.onError(error -> remove(userId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .reconnectTime(3000L)
                    .comment("connected"));
        } catch (IOException exception) {
            removeAndComplete(userId, emitter, exception);
        }
        return emitter;
    }

    public void sendToUser(Long userId, NotificationVO notification) {
        Set<SseEmitter> emitters = connections.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            sendNotification(userId, emitter, notification);
        }
    }

    public boolean sendReplay(
            Long userId,
            SseEmitter emitter,
            NotificationVO notification
    ) {
        return sendNotification(userId, emitter, notification);
    }

    public void heartbeat() {
        connections.forEach((userId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                } catch (IOException | IllegalStateException exception) {
                    removeAndComplete(userId, emitter, exception);
                }
            }
        });
    }

    int connectionCount(Long userId) {
        Set<SseEmitter> emitters = connections.get(userId);
        return emitters == null ? 0 : emitters.size();
    }

    private boolean sendNotification(
            Long userId,
            SseEmitter emitter,
            NotificationVO notification
    ) {
        try {
            emitter.send(SseEmitter.event()
                    .id(notification.id())
                    .name(notification.type())
                    .data(notification));
            return true;
        } catch (IOException | IllegalStateException exception) {
            removeAndComplete(userId, emitter, exception);
            return false;
        }
    }

    private void removeAndComplete(Long userId, SseEmitter emitter, Exception exception) {
        remove(userId, emitter);
        try {
            emitter.completeWithError(exception);
        } catch (RuntimeException completionException) {
            log.debug("SSE connection already completed, userId={}", userId);
        }
    }

    private void remove(Long userId, SseEmitter emitter) {
        connections.computeIfPresent(userId, (ignored, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }
}
