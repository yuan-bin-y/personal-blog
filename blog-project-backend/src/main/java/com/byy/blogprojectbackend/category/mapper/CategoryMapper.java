package com.byy.blogprojectbackend.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byy.blogprojectbackend.category.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 分类读写 Mapper。 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    long countPublic(@Param("used") boolean used);
    List<Category> selectPublic(@Param("used") boolean used, @Param("offset") long offset, @Param("pageSize") int pageSize);
    long countOwner(@Param("status") String status);
    List<Category> selectOwner(@Param("status") String status, @Param("offset") long offset, @Param("pageSize") int pageSize);
    Category selectAnyById(@Param("id") Long id);
    int updateWithVersion(@Param("category") Category category, @Param("version") int version);
    int softDelete(@Param("id") Long id, @Param("version") int version);
}
