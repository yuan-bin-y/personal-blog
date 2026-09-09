package com.byy.blogprojectbackend.search.service.impl;

import com.byy.blogprojectbackend.common.exception.ResourceConflictException;
import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.search.entity.SearchReindexTask;
import com.byy.blogprojectbackend.search.enums.ReindexTaskStatus;
import com.byy.blogprojectbackend.search.mapper.SearchReindexTaskMapper;
import com.byy.blogprojectbackend.search.service.SearchReindexService;
import com.byy.blogprojectbackend.search.vo.ReindexTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/** 任务记录先落 MySQL，再交给异步 Worker，因此进程内可查询真实进度。 */
@Service
@RequiredArgsConstructor
public class SearchReindexServiceImpl implements SearchReindexService {
    private final SearchReindexTaskMapper taskMapper;
    private final SearchReindexWorker worker;

    @Override
    public ReindexTaskVO create(Long ownerId) {
        SearchReindexTask task = new SearchReindexTask();
        task.setTaskId(UUID.randomUUID().toString().replace("-", ""));
        task.setStatus(ReindexTaskStatus.QUEUED.code());
        task.setCreatedBy(ownerId);
        task.setIndexedCount(0);

        try {
            taskMapper.insert(task);
        } catch (DuplicateKeyException exception) {
            throw new ResourceConflictException("已有正在执行的索引重建任务");
        }

        worker.rebuild(task.getTaskId());
        return toView(requireTask(task.getTaskId()));
    }

    @Override
    public ReindexTaskVO get(String taskId) {
        return toView(requireTask(taskId));
    }

    private SearchReindexTask requireTask(String taskId) {
        SearchReindexTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("索引重建任务不存在");
        }
        return task;
    }

    private ReindexTaskVO toView(SearchReindexTask task) {
        return new ReindexTaskVO(
                task.getTaskId(),
                task.getStatus(),
                toInstant(task.getCreatedAt()),
                toInstant(task.getFinishedAt()),
                task.getIndexedCount(),
                task.getFailureMessage()
        );
    }

    private java.time.Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
