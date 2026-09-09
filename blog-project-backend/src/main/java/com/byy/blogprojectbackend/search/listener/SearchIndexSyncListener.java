package com.byy.blogprojectbackend.search.listener;

import com.byy.blogprojectbackend.search.event.PublishedPostChangedEvent;
import com.byy.blogprojectbackend.search.gateway.ElasticsearchGateway;
import com.byy.blogprojectbackend.search.mapper.SearchIndexMapper;
import com.byy.blogprojectbackend.search.mapper.projection.SearchIndexRow;
import com.byy.blogprojectbackend.search.service.SearchDocumentFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * MySQL 事务提交后同步单篇索引。
 * Elasticsearch 失败不回滚已提交的业务数据，可通过全量重建修复。
 */
@Component
@RequiredArgsConstructor
public class SearchIndexSyncListener {
    private static final Logger log = LoggerFactory.getLogger(SearchIndexSyncListener.class);

    private final SearchIndexMapper indexMapper;
    private final SearchDocumentFactory documentFactory;
    private final ElasticsearchGateway elasticsearchGateway;

    @Async("searchTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostChanged(PublishedPostChangedEvent event) {
        try {
            SearchIndexRow row = indexMapper.selectPublishedById(event.postId());
            if (row == null) {
                elasticsearchGateway.delete(String.valueOf(event.postId()));
            } else {
                elasticsearchGateway.upsert(documentFactory.from(row));
            }
        } catch (Exception exception) {
            log.error("搜索增量索引同步失败，postId={}", event.postId(), exception);
        }
    }
}
