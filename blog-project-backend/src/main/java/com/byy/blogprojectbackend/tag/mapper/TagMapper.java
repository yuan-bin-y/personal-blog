package com.byy.blogprojectbackend.tag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byy.blogprojectbackend.tag.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    long countPublic(@Param("used") boolean used);
    List<Tag> selectPublic(@Param("used") boolean used, @Param("offset") long offset, @Param("pageSize") int pageSize);
    long countOwner(@Param("status") String status);
    List<Tag> selectOwner(@Param("status") String status, @Param("offset") long offset, @Param("pageSize") int pageSize);
    Tag selectAnyById(@Param("id") Long id);
    int updateActive(@Param("tag") Tag tag);
    int softDelete(@Param("id") Long id);
}
