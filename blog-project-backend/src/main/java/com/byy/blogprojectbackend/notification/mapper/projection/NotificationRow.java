package com.byy.blogprojectbackend.notification.mapper.projection;

import lombok.Data;

import java.time.LocalDateTime;

/** 通知表与发送者资料连接后的查询投影。 */
@Data
public class NotificationRow {
    private Long id;
    private Long recipientUserId;
    private Long actorUserId;
    private String actorName;
    private String actorAvatar;
    private String type;
    private String resourceType;
    private Long resourceId;
    private String summary;
    private Boolean read;
    private LocalDateTime createdAt;
}
