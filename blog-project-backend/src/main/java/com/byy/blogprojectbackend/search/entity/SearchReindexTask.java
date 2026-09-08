package com.byy.blogprojectbackend.search.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 全量索引重建任务的 MySQL 持久化记录。 */
@Data
@TableName("search_reindex_task")
public class SearchReindexTask {
    @TableId
    private String taskId;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer indexedCount;
    private String failureMessage;
}
