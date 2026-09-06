package com.byy.blogprojectbackend.post.vo;

import java.time.Instant;
import java.util.List;

/**
 * Visitor 说说详情。
 *
 * <p>MOMENT 没有标题、摘要、分类、标签、封面和阅读时间，
 * 这些公共字段按 API 契约分别返回 null 或空数组。</p>
 */
public record MomentDetailVO(
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
        Integer commentCount,
        Integer version,
        String contentFormat
) {
}
