package com.byy.blogprojectbackend.notification.service.impl;

import com.byy.blogprojectbackend.common.exception.ForbiddenOperationException;
import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.interaction.vo.VisitorAuthorVO;
import com.byy.blogprojectbackend.notification.entity.Notification;
import com.byy.blogprojectbackend.notification.enums.NotificationResourceType;
import com.byy.blogprojectbackend.notification.enums.NotificationType;
import com.byy.blogprojectbackend.notification.event.NotificationCreatedEvent;
import com.byy.blogprojectbackend.notification.mapper.NotificationMapper;
import com.byy.blogprojectbackend.notification.mapper.projection.NotificationRow;
import com.byy.blogprojectbackend.notification.service.NotificationService;
import com.byy.blogprojectbackend.notification.vo.NotificationVO;
import com.byy.blogprojectbackend.notification.vo.UnreadCountVO;
import com.byy.blogprojectbackend.profile.entity.SpaceProfile;
import com.byy.blogprojectbackend.profile.mapper.SpaceProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final SpaceProfileMapper profileMapper;
    private final IdGenerator idGenerator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public PageVO<NotificationVO> list(
            Long userId,
            boolean unreadOnly,
            int page,
            int pageSize
    ) {
        long total = notificationMapper.countByRecipient(userId, unreadOnly);
        long totalPages = (total + pageSize - 1) / pageSize;
        long offset = (long) (page - 1) * pageSize;
        List<NotificationVO> items = total == 0
                ? List.of()
                : notificationMapper.selectPage(
                        userId,
                        unreadOnly,
                        offset,
                        pageSize
                ).stream().map(this::toVO).toList();
        return new PageVO<>(
                items,
                page,
                pageSize,
                total,
                totalPages,
                page < totalPages
        );
    }

    @Override
    @Transactional
    public NotificationVO markRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectAny(notificationId);
        if (notification == null) {
            throw new ResourceNotFoundException("通知不存在");
        }
        if (!userId.equals(notification.getRecipientUserId())) {
            throw new ForbiddenOperationException("不能操作其他用户的通知");
        }
        if (!Boolean.TRUE.equals(notification.getRead())) {
            notificationMapper.markRead(notificationId, userId);
        }
        return requireView(notificationId, userId);
    }

    @Override
    @Transactional
    public UnreadCountVO markAllRead(Long userId) {
        notificationMapper.markAllRead(userId);
        return new UnreadCountVO(notificationMapper.countUnread(userId));
    }

    @Override
    public List<NotificationVO> replayAfter(Long userId, Long lastEventId, int limit) {
        return notificationMapper.selectAfterId(userId, lastEventId, limit)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public NotificationVO getForDelivery(Long notificationId, Long recipientUserId) {
        return requireView(notificationId, recipientUserId);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyCommentCreated(Long actorUserId, String actorName, Long postId) {
        Long ownerId = ownerUserId();
        create(
                NotificationType.COMMENT_CREATED,
                actorUserId,
                ownerId,
                NotificationResourceType.POST,
                postId,
                actorName + " 评论了你的内容"
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyGuestbookCreated(Long actorUserId, String actorName, Long entryId) {
        Long ownerId = ownerUserId();
        create(
                NotificationType.GUESTBOOK_CREATED,
                actorUserId,
                ownerId,
                NotificationResourceType.GUESTBOOK,
                entryId,
                actorName + " 在空间留下了留言"
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyCommentReplied(
            Long actorUserId,
            String actorName,
            Long recipientUserId,
            Long commentId
    ) {
        create(
                NotificationType.COMMENT_REPLIED,
                actorUserId,
                recipientUserId,
                NotificationResourceType.COMMENT,
                commentId,
                actorName + " 回复了你的评论"
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyGuestbookReplied(
            Long actorUserId,
            String actorName,
            Long recipientUserId,
            Long entryId
    ) {
        create(
                NotificationType.GUESTBOOK_REPLIED,
                actorUserId,
                recipientUserId,
                NotificationResourceType.GUESTBOOK,
                entryId,
                actorName + " 回复了你的留言"
        );
    }

    private void create(
            NotificationType type,
            Long actorUserId,
            Long recipientUserId,
            NotificationResourceType resourceType,
            Long resourceId,
            String summary
    ) {
        // Owner 自己操作自己的空间时不制造自我通知。
        if (recipientUserId == null || recipientUserId.equals(actorUserId)) {
            return;
        }

        Notification notification = new Notification();
        notification.setId(idGenerator.nextId());
        notification.setRecipientUserId(recipientUserId);
        notification.setActorUserId(actorUserId);
        notification.setType(type.code());
        notification.setResourceType(resourceType.code());
        notification.setResourceId(resourceId);
        notification.setSummary(shorten(summary));
        notification.setRead(false);

        if (notificationMapper.insert(notification) != 1) {
            throw new IllegalStateException("通知创建失败");
        }

        // 事件当前只被 AFTER_COMMIT 监听；业务事务回滚时不会向 SSE 推送。
        eventPublisher.publishEvent(new NotificationCreatedEvent(
                notification.getId(),
                recipientUserId
        ));
    }

    private Long ownerUserId() {
        SpaceProfile owner = profileMapper.selectOwnerProfile();
        if (owner == null || owner.getUserId() == null) {
            throw new ResourceNotFoundException("空间主人资料不存在");
        }
        return owner.getUserId();
    }

    private NotificationVO requireView(Long notificationId, Long userId) {
        NotificationRow row = notificationMapper.selectViewForRecipient(
                notificationId,
                userId
        );
        if (row == null) {
            throw new ResourceNotFoundException("通知不存在");
        }
        return toVO(row);
    }

    private NotificationVO toVO(NotificationRow row) {
        VisitorAuthorVO actor = row.getActorUserId() == null
                ? null
                : new VisitorAuthorVO(row.getActorName(), row.getActorAvatar());
        return new NotificationVO(
                String.valueOf(row.getId()),
                row.getType(),
                actor,
                row.getResourceType(),
                String.valueOf(row.getResourceId()),
                row.getSummary(),
                Boolean.TRUE.equals(row.getRead()),
                row.getCreatedAt().toInstant(ZoneOffset.UTC)
        );
    }

    private String shorten(String value) {
        String text = value == null ? "新的空间通知" : value.trim();
        return text.length() <= 200 ? text : text.substring(0, 200);
    }
}
