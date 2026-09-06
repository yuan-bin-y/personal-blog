package com.byy.blogprojectbackend.archive.mapper;

import com.byy.blogprojectbackend.archive.mapper.projection.ArchiveRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公开归档只读查询。
 */
@Mapper
public interface ArchiveMapper {

    long countPublicArchive(
            @Param("year") Integer year,
            @Param("type") String type
    );

    List<ArchiveRow> selectPublicArchive(
            @Param("year") Integer year,
            @Param("type") String type,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );
}
