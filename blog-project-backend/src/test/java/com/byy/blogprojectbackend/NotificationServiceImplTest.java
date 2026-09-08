package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.common.exception.ForbiddenOperationException;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.notification.entity.Notification;
import com.byy.blogprojectbackend.notification.event.NotificationCreatedEvent;
import com.byy.blogprojectbackend.notification.mapper.NotificationMapper;
import com.byy.blogprojectbackend.notification.mapper.projection.NotificationRow;
import com.byy.blogprojectbackend.notification.service.impl.NotificationServiceImpl;
import com.byy.blogprojectbackend.notification.vo.NotificationVO;
import com.byy.blogprojectbackend.notification.vo.UnreadCountVO;
import com.byy.blogprojectbackend.profile.entity.SpaceProfile;
import com.byy.blogprojectbackend.profile.mapper.SpaceProfileMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceImplTest {

    private final NotificationMapper mapper = mock(NotificationMapper.class);
    private final SpaceProfileMapper profileMapper = mock(SpaceProfileMapper.class);
    private final IdGenerator idGenerator = mock(IdGenerator.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final NotificationServiceImpl service = new NotificationServiceImpl(
            mapper,
            profileMapper,
            idGenerator,
            eventPublisher
    );

    @Test
    void list_scopesPaginationToRecipient() {
        NotificationRow row = row(301L, 202L, false);
        when(mapper.countByRecipient(202L, true)).thenReturn(1L);
        when(mapper.selectPage(202L, true, 0L, 10)).thenReturn(List.of(row));

        PageVO<NotificationVO> result = service.list(202L, true, 1, 10);

        assertEquals(1, result.total());
        assertEquals("301", result.items().get(0).id());
        assertEquals("暖光访客", result.items().get(0).actor().name());
    }

    @Test
    void markRead_updatesOnlyNotificationOwner() {
        Notification entity = new Notification();
        entity.setId(301L);
        entity.setRecipientUserId(202L);
        entity.setRead(false);
        when(mapper.selectAny(301L)).thenReturn(entity);
        when(mapper.selectViewForRecipient(301L, 202L)).thenReturn(row(301L, 202L, true));

        NotificationVO result = service.markRead(301L, 202L);

        verify(mapper).markRead(301L, 202L);
        assertEquals(true, result.read());
    }

    @Test
    void markRead_rejectsDifferentRecipient() {
        Notification entity = new Notification();
        entity.setId(301L);
        entity.setRecipientUserId(202L);
        entity.setRead(false);
        when(mapper.selectAny(301L)).thenReturn(entity);

        assertThrows(
                ForbiddenOperationException.class,
                () -> service.markRead(301L, 999L)
        );
        verify(mapper, never()).markRead(any(), any());
    }

    @Test
    void notifyCommentCreated_persistsThenPublishesDomainEvent() {
        SpaceProfile owner = new SpaceProfile();
        owner.setUserId(1L);
        when(profileMapper.selectOwnerProfile()).thenReturn(owner);
        when(idGenerator.nextId()).thenReturn(301L);
        when(mapper.insert(any(Notification.class))).thenReturn(1);

        service.notifyCommentCreated(202L, "暖光访客", 101L);

        ArgumentCaptor<Notification> notificationCaptor =
                ArgumentCaptor.forClass(Notification.class);
        verify(mapper).insert(notificationCaptor.capture());
        assertEquals(1L, notificationCaptor.getValue().getRecipientUserId());
        assertEquals("COMMENT_CREATED", notificationCaptor.getValue().getType());
        assertEquals("POST", notificationCaptor.getValue().getResourceType());

        ArgumentCaptor<NotificationCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(NotificationCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(301L, eventCaptor.getValue().notificationId());
    }

    @Test
    void markAllRead_returnsActualRemainingCount() {
        when(mapper.countUnread(202L)).thenReturn(1L);

        UnreadCountVO result = service.markAllRead(202L);

        verify(mapper).markAllRead(202L);
        assertEquals(1L, result.unreadCount());
    }

    private NotificationRow row(Long id, Long recipientId, boolean read) {
        NotificationRow row = new NotificationRow();
        row.setId(id);
        row.setRecipientUserId(recipientId);
        row.setActorUserId(100L);
        row.setActorName("暖光访客");
        row.setActorAvatar("/media/visitor.png");
        row.setType("COMMENT_CREATED");
        row.setResourceType("POST");
        row.setResourceId(101L);
        row.setSummary("暖光访客评论了你的内容");
        row.setRead(read);
        row.setCreatedAt(LocalDateTime.of(2026, 9, 8, 10, 0));
        return row;
    }
}
