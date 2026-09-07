package com.byy.blogprojectbackend.comment.vo;

import com.byy.blogprojectbackend.interaction.vo.ReplyVO;
import com.byy.blogprojectbackend.interaction.vo.VisitorAuthorVO;

import java.time.Instant;

public record CommentVO(
        String id,
        String postId,
        VisitorAuthorVO author,
        String content,
        Instant createdAt,
        boolean ownedByMe,
        ReplyVO ownerReply
) {
}