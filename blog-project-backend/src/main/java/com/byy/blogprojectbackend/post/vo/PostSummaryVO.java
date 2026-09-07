package com.byy.blogprojectbackend.post.vo;

import java.time.Instant;
import java.util.List;

/**
 * 首页 Feed 和文章列表统一使用的 Post 摘要。
 */
public record PostSummaryVO(
        String id,
        String type,
        String slug,
        String title,
        String summary,
        String content,
        PostAuthorVO author,
        CategoryVO category,
        List<TagVO> tags,
        MediaVO cover,
        List<MediaVO> images,
        Instant createdAt,
        Instant updatedAt,
        Instant publishedAt,
        Integer readingTime,
        String status,
        Integer likeCount,
        boolean likedByMe,
        Integer commentCount,
        Integer version
) {
}
