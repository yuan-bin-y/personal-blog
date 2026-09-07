package com.byy.blogprojectbackend.like.mapper;

import com.byy.blogprojectbackend.like.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikeMapper {

    /**
     * 查询公开 Post 的点赞数。
     * 返回 null 表示 Post 不存在或不是公开状态。
     */
    Integer selectPublicLikeCount(
            @Param("postId") Long postId
    );

    /**
     * 插入点赞关系。
     * 第一次点赞返回 1，重复点赞返回 0。
     */
    int insertIgnore(
            @Param("postLike") PostLike postLike
    );

    /**
     * 只有 Post 仍然公开时才增加点赞数。
     */
    int incrementPublicLikeCount(
            @Param("postId") Long postId
    );
}