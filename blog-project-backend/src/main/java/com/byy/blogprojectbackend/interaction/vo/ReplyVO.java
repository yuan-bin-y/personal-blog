package com.byy.blogprojectbackend.interaction.vo;

import java.time.Instant;

public record ReplyVO(String id,ReplyAuthorVO author,String content,Instant createdAt) {
}
