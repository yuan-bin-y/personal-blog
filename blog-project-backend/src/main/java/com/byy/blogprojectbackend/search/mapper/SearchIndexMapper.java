package com.byy.blogprojectbackend.search.mapper;

import com.byy.blogprojectbackend.search.mapper.projection.SearchIndexRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 从 MySQL 真实数据源抽取已发布 Post 用于建立搜索索引。 */
@Mapper
public interface SearchIndexMapper {
    List<SearchIndexRow> selectPublishedAfterId(
            @Param("lastId") Long lastId,
            @Param("limit") int limit
    );

    SearchIndexRow selectPublishedById(@Param("postId") Long postId);
}
