package com.byy.blogprojectbackend.search.vo;

import java.time.Instant;

/** Elasticsearch 全量重建任务的对外状态。 */
public record ReindexTaskVO(
        String taskId,
        String status,
        Instant createdAt,
        Instant finishedAt,
        Integer indexedCount,
        String failureMessage
) {
}
