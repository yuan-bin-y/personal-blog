package com.byy.blogprojectbackend.guestbook.mapper.projection;

import lombok.Data;
import java.time.LocalDateTime;

/** 顶层留言和唯一 Owner 回复的扁平查询结果。 */
@Data
public class GuestbookRow {
    private Long id;
    private String authorName;
    private String authorAvatar;
    private String content;
    private LocalDateTime createdAt;
    private Long replyId;
    private Long replyUserId;
    private String replyAuthorName;
    private String replyAuthorAvatar;
    private String replyContent;
    private LocalDateTime replyCreatedAt;
}
