package com.byy.blogprojectbackend.comment.mapper.projection;

import lombok.Data;
import java.time.LocalDateTime;

/** 顶层评论及其唯一 Owner 回复的扁平查询结果。 */
@Data
public class CommentRow {
    private Long id;
    private Long postId;
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
