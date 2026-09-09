package com.byy.blogprojectbackend.realtime.listener;

import com.byy.blogprojectbackend.notification.event.NotificationCreatedEvent;
import com.byy.blogprojectbackend.realtime.service.RealtimePushService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** 数据库提交成功后才尝试实时推送；推送失败不回滚已经成功的业务。 */
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);
    private final RealtimePushService realtimePushService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommit(NotificationCreatedEvent event) {
        try {
            realtimePushService.pushNotification(
                    event.notificationId(),
                    event.recipientUserId()
            );
        } catch (RuntimeException exception) {
            // 数据已经持久化，客户端可通过通知列表补偿，不能反向破坏主业务。
            log.warn(
                    "实时通知投递失败，notificationId={}",
                    event.notificationId(),
                    exception
            );
        }
    }
}
