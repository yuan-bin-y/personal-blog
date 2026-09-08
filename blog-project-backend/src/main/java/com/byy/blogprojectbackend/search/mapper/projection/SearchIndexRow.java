package com.byy.blogprojectbackend.search.mapper.projection;

import lombok.Data;

import java.time.LocalDateTime;

/** 从 MySQL 抽取的 Elasticsearch 索引源数据。 */
@Data
public class SearchIndexRow {
    private Long id;
    private String type;
    private String slug;
    private String title;
    private String summary;
    private String content;
    private String categorySlug;
    private String categoryName;
    private String tagSlugs;
    private String tagNames;
    private LocalDateTime publishedAt;
}
