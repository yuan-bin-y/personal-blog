package com.byy.blogprojectbackend.search.gateway;

import java.time.Instant;
import java.util.List;

/** Elasticsearch 命中，context 仅供 AI 组装上下文，不直接暴露给搜索列表。 */
public record SearchHit(
        String id,
        String type,
        String slug,
        String title,
        String excerpt,
        String context,
        List<String> highlights,
        Instant publishedAt
) {
}
