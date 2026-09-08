package com.byy.blogprojectbackend.notification.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** notification 表实体；实时推送失败时仍以该表为最终数据来源。 */
@Data
public class Notification {
    private Long id;
    private Long recipientUserId;
    private Long actorUserId;
    private String type;
    private String resourceType;
    private Long resourceId;
    private String summary;
    private Boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
