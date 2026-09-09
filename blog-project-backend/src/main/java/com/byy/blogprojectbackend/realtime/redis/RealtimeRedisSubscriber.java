package com.byy.blogprojectbackend.realtime.redis;

import com.byy.blogprojectbackend.notification.service.NotificationService;
import com.byy.blogprojectbackend.realtime.registry.SseConnectionRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

/** 每个后端实例都订阅同一频道，只向本实例持有的 SSE 连接发送。 */
@Component
@RequiredArgsConstructor
public class RealtimeRedisSubscriber implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(RealtimeRedisSubscriber.class);

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final SseConnectionRegistry registry;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            RealtimeRedisMessage payload = objectMapper.readValue(
                    new String(message.getBody(), StandardCharsets.UTF_8),
                    RealtimeRedisMessage.class
            );
            registry.sendToUser(
                    payload.recipientUserId(),
                    notificationService.getForDelivery(
                            payload.notificationId(),
                            payload.recipientUserId()
                    )
            );
        } catch (Exception exception) {
            // Pub/Sub 没有确认机制，失败时仍可由通知列表和 Last-Event-ID 补偿。
            log.warn("已忽略格式不正确的 Redis 实时通知消息", exception);
        }
    }
}
