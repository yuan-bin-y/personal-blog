package com.byy.blogprojectbackend.search.vo;

import java.time.Instant;
import java.util.List;

/** 公开全文搜索的单条命中。 */
public record SearchResultVO(
        String id,
        String type,
        String slug,
        String title,
        String excerpt,
        List<String> highlights,
        Instant publishedAt
) {
}
