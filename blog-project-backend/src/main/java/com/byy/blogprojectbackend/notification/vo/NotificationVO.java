package com.byy.blogprojectbackend.notification.vo;

import com.byy.blogprojectbackend.interaction.vo.VisitorAuthorVO;

import java.time.Instant;

/** 通知中心和 SSE data 共用的输出模型。 */
public record NotificationVO(
        String id,
        String type,
        VisitorAuthorVO actor,
        String resourceType,
        String resourceId,
        String summary,
        boolean read,
        Instant createdAt
) {
}
