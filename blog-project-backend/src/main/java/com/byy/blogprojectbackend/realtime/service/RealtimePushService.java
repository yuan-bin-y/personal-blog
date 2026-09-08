package com.byy.blogprojectbackend.realtime.service;

public interface RealtimePushService {
    void pushNotification(Long notificationId, Long recipientUserId);
}
