package com.byy.blogprojectbackend.post.vo;

import java.time.Instant;
import java.util.List;

/**
 * Visitor 技术文章详情。
 *
 * <p>在 PostSummaryVO 公共字段基础上增加完整正文、正文格式和相关文章。</p>
 */
public record TechDetailVO(
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
        String contentFormat,
        List<PostSummaryVO> relatedPosts
) {
}
