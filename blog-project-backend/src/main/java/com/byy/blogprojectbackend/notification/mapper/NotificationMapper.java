package com.byy.blogprojectbackend.notification.mapper;

import com.byy.blogprojectbackend.notification.entity.Notification;
import com.byy.blogprojectbackend.notification.mapper.projection.NotificationRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {
    int insert(@Param("notification") Notification notification);
    Notification selectAny(@Param("id") Long id);
    NotificationRow selectViewForRecipient(
            @Param("id") Long id,
            @Param("recipientUserId") Long recipientUserId
    );
    long countByRecipient(
            @Param("recipientUserId") Long recipientUserId,
            @Param("unreadOnly") boolean unreadOnly
    );
    List<NotificationRow> selectPage(
            @Param("recipientUserId") Long recipientUserId,
            @Param("unreadOnly") boolean unreadOnly,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );
    int markRead(
            @Param("id") Long id,
            @Param("recipientUserId") Long recipientUserId
    );
    int markAllRead(@Param("recipientUserId") Long recipientUserId);
    long countUnread(@Param("recipientUserId") Long recipientUserId);
    List<NotificationRow> selectAfterId(
            @Param("recipientUserId") Long recipientUserId,
            @Param("lastEventId") Long lastEventId,
            @Param("limit") int limit
    );
}
