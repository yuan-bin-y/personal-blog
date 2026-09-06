package com.byy.blogprojectbackend.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byy.blogprojectbackend.user.entity.SpaceUser;
import org.apache.ibatis.annotations.Mapper;

/** OWNER 账号表的 MyBatis-Plus Mapper。 */
@Mapper
public interface SpaceUserMapper extends BaseMapper<SpaceUser> {
}
