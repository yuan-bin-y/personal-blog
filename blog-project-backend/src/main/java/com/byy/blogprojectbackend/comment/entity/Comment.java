package com.byy.blogprojectbackend.comment.entity;

import lombok.Data;
import java.time.LocalDateTime;

/** comment 表实体。 */
@Data
public class Comment {
    private Long id;
    private Long postId;
    private Long parentId;
    private Long authorUserId;
    private String authorName;
    private String authorAvatarUrl;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;
    private LocalDateTime deletedAt;
    private Long deletedBy;
}
