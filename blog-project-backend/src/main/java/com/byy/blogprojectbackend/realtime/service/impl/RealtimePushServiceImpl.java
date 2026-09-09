package com.byy.blogprojectbackend.realtime.service.impl;

import com.byy.blogprojectbackend.realtime.redis.RealtimeRedisPublisher;
import com.byy.blogprojectbackend.realtime.service.RealtimePushService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RealtimePushServiceImpl implements RealtimePushService {

    private final RealtimeRedisPublisher redisPublisher;

    @Override
    public void pushNotification(Long notificationId, Long recipientUserId) {
        redisPublisher.publish(notificationId, recipientUserId);
    }
}
