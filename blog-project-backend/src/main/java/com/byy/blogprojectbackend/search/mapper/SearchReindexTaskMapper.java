package com.byy.blogprojectbackend.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byy.blogprojectbackend.search.entity.SearchReindexTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 索引重建任务状态写入。 */
@Mapper
public interface SearchReindexTaskMapper extends BaseMapper<SearchReindexTask> {
    int markRunning(@Param("taskId") String taskId);
    int markSucceeded(@Param("taskId") String taskId, @Param("indexedCount") int indexedCount);
    int markFailed(@Param("taskId") String taskId, @Param("failureMessage") String failureMessage);
}
