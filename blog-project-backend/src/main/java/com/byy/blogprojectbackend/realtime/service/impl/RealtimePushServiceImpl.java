package com.byy.blogprojectbackend.realtime.service.impl;

import com.byy.blogprojectbackend.notification.service.NotificationService;
import com.byy.blogprojectbackend.notification.vo.NotificationVO;
import com.byy.blogprojectbackend.realtime.registry.SseConnectionRegistry;
import com.byy.blogprojectbackend.realtime.service.RealtimePushService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RealtimePushServiceImpl implements RealtimePushService {

    private final NotificationService notificationService;
    private final SseConnectionRegistry registry;

    @Override
    public void pushNotification(Long notificationId, Long recipientUserId) {
        NotificationVO notification = notificationService.getForDelivery(
                notificationId,
                recipientUserId
        );
        registry.sendToUser(recipientUserId, notification);
    }
}
