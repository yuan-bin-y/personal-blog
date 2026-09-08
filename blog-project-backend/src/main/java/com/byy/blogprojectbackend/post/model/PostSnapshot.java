package com.byy.blogprojectbackend.post.model;

import java.util.List;

/**
 * 历史版本保存的完整可编辑状态。
 * 时间使用 ISO-8601 字符串，避免快照受 JVM 时区影响。
 */
public record PostSnapshot(
        String type,
        String slug,
        String title,
        String summary,
        String content,
        String contentFormat,
        String categoryId,
        Integer readingTimeMinutes,
        String status,
        String publishedAt,
        List<String> tagIds,
        List<SnapshotMedia> media
) {
    public record SnapshotMedia(
            String usageType,
            String mediaType,
            String src,
            String poster,
            String alt,
            Integer width,
            Integer height,
            Integer sortOrder
    ) {
    }
}
