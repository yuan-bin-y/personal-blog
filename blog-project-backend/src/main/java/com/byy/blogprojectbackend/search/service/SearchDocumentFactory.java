package com.byy.blogprojectbackend.search.service;

import com.byy.blogprojectbackend.search.document.SearchDocument;
import com.byy.blogprojectbackend.search.mapper.projection.SearchIndexRow;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

/** 把 MySQL 投影稳定转换为 Elasticsearch Document。 */
@Component
public class SearchDocumentFactory {
    private static final String DELIMITER = "\\|\\|\\|";

    public SearchDocument from(SearchIndexRow row) {
        return new SearchDocument(
                String.valueOf(row.getId()),
                row.getType(),
                row.getSlug(),
                row.getTitle(),
                row.getSummary(),
                row.getContent(),
                row.getCategorySlug(),
                row.getCategoryName(),
                split(row.getTagSlugs()),
                split(row.getTagNames()),
                row.getPublishedAt().toInstant(ZoneOffset.UTC)
        );
    }

    private List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(DELIMITER))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .distinct()
                .toList();
    }
}
