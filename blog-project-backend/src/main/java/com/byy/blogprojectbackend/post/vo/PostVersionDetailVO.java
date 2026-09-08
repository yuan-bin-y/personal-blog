package com.byy.blogprojectbackend.post.vo;

import java.time.Instant;

public record PostVersionDetailVO(
        String id,
        String postId,
        Integer versionNo,
        String summary,
        Instant createdAt,
        String type,
        Object snapshot
) {
}
