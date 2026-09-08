package com.byy.blogprojectbackend.notification.service;

import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.notification.vo.NotificationVO;
import com.byy.blogprojectbackend.notification.vo.UnreadCountVO;

import java.util.List;

public interface NotificationService {
    PageVO<NotificationVO> list(Long userId, boolean unreadOnly, int page, int pageSize);
    NotificationVO markRead(Long notificationId, Long userId);
    UnreadCountVO markAllRead(Long userId);
    List<NotificationVO> replayAfter(Long userId, Long lastEventId, int limit);
    NotificationVO getForDelivery(Long notificationId, Long recipientUserId);

    void notifyCommentCreated(Long actorUserId, String actorName, Long postId);
    void notifyGuestbookCreated(Long actorUserId, String actorName, Long entryId);
    void notifyCommentReplied(
            Long actorUserId,
            String actorName,
            Long recipientUserId,
            Long commentId
    );
    void notifyGuestbookReplied(
            Long actorUserId,
            String actorName,
            Long recipientUserId,
            Long entryId
    );
}
