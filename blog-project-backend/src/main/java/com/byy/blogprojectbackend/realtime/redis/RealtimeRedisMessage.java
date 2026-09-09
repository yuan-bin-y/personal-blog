package com.byy.blogprojectbackend.realtime.redis;

/** Redis Pub/Sub 中只传递定位信息，完整通知仍从 MySQL 读取。 */
public record RealtimeRedisMessage(
        Long notificationId,
        Long recipientUserId
) {
}
