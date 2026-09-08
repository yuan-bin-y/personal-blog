package com.byy.blogprojectbackend.search.gateway;

import java.util.List;

/** Elasticsearch 搜索结果页。 */
public record SearchPage(List<SearchHit> items, long total) {
}
