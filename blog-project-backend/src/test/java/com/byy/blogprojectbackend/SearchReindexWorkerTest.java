package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.search.config.SearchProperties;
import com.byy.blogprojectbackend.search.gateway.ElasticsearchGateway;
import com.byy.blogprojectbackend.search.mapper.SearchIndexMapper;
import com.byy.blogprojectbackend.search.mapper.SearchReindexTaskMapper;
import com.byy.blogprojectbackend.search.mapper.projection.SearchIndexRow;
import com.byy.blogprojectbackend.search.service.SearchDocumentFactory;
import com.byy.blogprojectbackend.search.service.impl.SearchReindexWorker;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchReindexWorkerTest {

    @Test
    void rebuild_indexesPublishedRowsThenSwitchesAlias() {
        SearchReindexTaskMapper taskMapper = mock(SearchReindexTaskMapper.class);
        SearchIndexMapper indexMapper = mock(SearchIndexMapper.class);
        ElasticsearchGateway gateway = mock(ElasticsearchGateway.class);
        SearchProperties properties = new SearchProperties();
        properties.setReindexBatchSize(200);

        SearchIndexRow row = new SearchIndexRow();
        row.setId(101L);
        row.setType("TECH");
        row.setSlug("redis");
        row.setTitle("Redis 实践");
        row.setSummary("摘要");
        row.setContent("正文");
        row.setTagSlugs("redis|||java");
        row.setTagNames("Redis|||Java");
        row.setPublishedAt(LocalDateTime.of(2026, 9, 8, 0, 0));

        when(taskMapper.markRunning("task")).thenReturn(1);
        when(indexMapper.selectPublishedAfterId(0L, 200)).thenReturn(List.of(row));

        SearchReindexWorker worker = new SearchReindexWorker(
                taskMapper,
                indexMapper,
                new SearchDocumentFactory(),
                gateway,
                properties
        );
        worker.rebuild("task");

        verify(gateway).createIndex(anyString());
        verify(gateway).bulkIndex(anyString(), anyList());
        verify(gateway).switchAlias(anyString());
        verify(taskMapper).markSucceeded("task", 1);
    }
}
