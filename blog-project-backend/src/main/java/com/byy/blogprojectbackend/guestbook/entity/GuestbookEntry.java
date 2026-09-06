package com.byy.blogprojectbackend.guestbook.entity;

import lombok.Data;
import java.time.LocalDateTime;

/** guestbook 表实体，与 Post Comment 保持独立。 */
@Data
public class GuestbookEntry {
    private Long id;
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
