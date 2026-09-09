package com.byy.blogprojectbackend.realtime.redis;

import com.byy.blogprojectbackend.notification.service.NotificationService;
import com.byy.blogprojectbackend.realtime.registry.SseConnectionRegistry;
import com.byy.blogprojectbackend.realtime.config.RealtimeProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/** 发布跨实例实时通知；Redis 不可用时退回当前实例的 SSE 连接。 */
@Component
@RequiredArgsConstructor
public class RealtimeRedisPublisher {
    private static final Logger log = LoggerFactory.getLogger(RealtimeRedisPublisher.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RealtimeProperties properties;
    private final NotificationService notificationService;
    private final SseConnectionRegistry registry;

    public void publish(Long notificationId, Long recipientUserId) {
        try {
            String payload = objectMapper.writeValueAsString(
                    new RealtimeRedisMessage(notificationId, recipientUserId)
            );
            Long subscribers = redisTemplate.convertAndSend(properties.getRedisChannel(), payload);
            if (subscribers == null || subscribers == 0L) {
                deliverLocally(notificationId, recipientUserId);
            }
        } catch (DataAccessException exception) {
            log.warn("Redis realtime publish unavailable, falling back to local SSE");
            deliverLocally(notificationId, recipientUserId);
        } catch (Exception exception) {
            throw new IllegalStateException("Realtime message serialization failed", exception);
        }
    }

    private void deliverLocally(Long notificationId, Long recipientUserId) {
        registry.sendToUser(
                recipientUserId,
                notificationService.getForDelivery(notificationId, recipientUserId)
        );
    }
}
