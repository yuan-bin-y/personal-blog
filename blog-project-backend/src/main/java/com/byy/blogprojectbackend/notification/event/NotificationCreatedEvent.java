package com.byy.blogprojectbackend.notification.event;

/** 通知记录写入事务后，用于触发实时投递。 */
public record NotificationCreatedEvent(
        Long notificationId,
        Long recipientUserId
) {
}
