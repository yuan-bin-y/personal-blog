package com.byy.blogprojectbackend.guestbook.vo;

import com.byy.blogprojectbackend.interaction.vo.ReplyVO;
import com.byy.blogprojectbackend.interaction.vo.VisitorAuthorVO;
import java.time.Instant;

public record GuestbookVO(String id, VisitorAuthorVO author, String content, Instant createdAt, ReplyVO ownerReply) {
}
