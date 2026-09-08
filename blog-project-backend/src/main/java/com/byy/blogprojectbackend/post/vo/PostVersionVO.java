package com.byy.blogprojectbackend.post.vo;

import java.time.Instant;

public record PostVersionVO(
        String id,
        String postId,
        Integer versionNo,
        String summary,
        Instant createdAt
) {
}
