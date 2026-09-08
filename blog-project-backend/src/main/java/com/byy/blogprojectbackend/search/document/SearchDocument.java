package com.byy.blogprojectbackend.search.document;

import java.time.Instant;
import java.util.List;

/** Elasticsearch 中只保存可公开且可重建的 Post 搜索副本。 */
public record SearchDocument(
        String id,
        String type,
        String slug,
        String title,
        String summary,
        String content,
        String categorySlug,
        String categoryName,
        List<String> tagSlugs,
        List<String> tagNames,
        Instant publishedAt
) {
}
