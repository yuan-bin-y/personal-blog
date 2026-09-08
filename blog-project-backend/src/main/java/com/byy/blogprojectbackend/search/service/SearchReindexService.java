package com.byy.blogprojectbackend.search.service;

import com.byy.blogprojectbackend.search.vo.ReindexTaskVO;

/** Owner 全量重建 Elasticsearch 索引的任务入口。 */
public interface SearchReindexService {
    ReindexTaskVO create(Long ownerId);
    ReindexTaskVO get(String taskId);
}
