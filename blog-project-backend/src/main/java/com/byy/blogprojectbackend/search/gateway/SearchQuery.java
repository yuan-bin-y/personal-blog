package com.byy.blogprojectbackend.search.gateway;

/** 与 Web 层解耦后的 Elasticsearch 搜索条件。 */
public record SearchQuery(
        String keyword,
        String type,
        String categorySlug,
        String tagSlug,
        int page,
        int pageSize
) {
}
