package com.byy.blogprojectbackend.profile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byy.blogprojectbackend.profile.entity.SpaceProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SpaceProfileMapper extends BaseMapper<SpaceProfile> {

    /**
     * 查询当前空间唯一的 Owner Profile。
     */
    SpaceProfile selectOwnerProfile();

    /**
     * 根据用户 ID 查询 Profile。
     * Auth 获取当前 Owner 身份时使用。
     */
    SpaceProfile selectByUserId(
            @Param("userId") Long userId
    );

    /**
     * 根据旧 version 更新 Owner Profile。
     *
     * @param profile 需要更新的资料
     * @param version 前端提交的旧版本号
     * @return 受影响行数；1 表示成功，0 表示版本冲突或数据不存在
     */
    int updateOwnerProfile(
            @Param("profile") SpaceProfile profile,
            @Param("version") Integer version
    );
}