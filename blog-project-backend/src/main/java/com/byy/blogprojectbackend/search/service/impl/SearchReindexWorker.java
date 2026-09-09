package com.byy.blogprojectbackend.search.service.impl;

import com.byy.blogprojectbackend.search.config.SearchProperties;
import com.byy.blogprojectbackend.search.document.SearchDocument;
import com.byy.blogprojectbackend.search.gateway.ElasticsearchGateway;
import com.byy.blogprojectbackend.search.mapper.SearchIndexMapper;
import com.byy.blogprojectbackend.search.mapper.SearchReindexTaskMapper;
import com.byy.blogprojectbackend.search.mapper.projection.SearchIndexRow;
import com.byy.blogprojectbackend.search.service.SearchDocumentFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/** 分批从 MySQL 读取已发布内容，写入新索引后原子切换别名。 */
@Component
@RequiredArgsConstructor
public class SearchReindexWorker {
    private static final Logger log = LoggerFactory.getLogger(SearchReindexWorker.class);

    private final SearchReindexTaskMapper taskMapper;
    private final SearchIndexMapper indexMapper;
    private final SearchDocumentFactory documentFactory;
    private final ElasticsearchGateway elasticsearchGateway;
    private final SearchProperties properties;

    @Async("searchTaskExecutor")
    public void rebuild(String taskId) {
        if (taskMapper.markRunning(taskId) != 1) {
            return;
        }

        String newIndex = properties.getIndexAlias() + "-" + Instant.now().toEpochMilli();
        boolean aliasSwitched = false;
        int indexedCount = 0;

        try {
            elasticsearchGateway.createIndex(newIndex);
            long lastId = 0L;

            while (true) {
                List<SearchIndexRow> rows = indexMapper.selectPublishedAfterId(
                        lastId,
                        properties.getReindexBatchSize()
                );
                if (rows.isEmpty()) {
                    break;
                }

                List<SearchDocument> documents = rows.stream()
                        .map(documentFactory::from)
                        .toList();
                elasticsearchGateway.bulkIndex(newIndex, documents);
                indexedCount += documents.size();
                lastId = rows.get(rows.size() - 1).getId();

                if (rows.size() < properties.getReindexBatchSize()) {
                    break;
                }
            }

            elasticsearchGateway.switchAlias(newIndex);
            aliasSwitched = true;
            taskMapper.markSucceeded(taskId, indexedCount);
        } catch (Exception exception) {
            log.error("搜索索引重建失败，taskId={}", taskId, exception);
            taskMapper.markFailed(taskId, truncate(exception.getMessage()));
            if (!aliasSwitched) {
                try {
                    elasticsearchGateway.deleteIndex(newIndex);
                } catch (Exception cleanupException) {
                    log.warn("未能清理未完成的搜索索引：{}", newIndex);
                }
            }
        }
    }

    private String truncate(String value) {
        String message = value == null || value.isBlank() ? "索引重建失败" : value;
        return message.length() <= 500 ? message : message.substring(0, 500);
    }
}
