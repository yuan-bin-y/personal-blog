package com.byy.blogprojectbackend.archive.vo;

import java.time.Instant;

/**
 * 归档中的一条精简内容索引。
 */
public record ArchiveItemVO(
        String id,
        String type,
        String slug,
        String label,
        Instant publishedAt
) {
}
