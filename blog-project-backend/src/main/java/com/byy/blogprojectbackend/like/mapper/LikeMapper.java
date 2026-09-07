package com.byy.blogprojectbackend.like.mapper;

import com.byy.blogprojectbackend.like.entity.PostLike;
import com.byy.blogprojectbackend.post.mapper.projection.PostFeedRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    /** 删除当前用户与 Post 的点赞关系；不存在时返回 0。 */
    int deleteRelation(
            @Param("postId") Long postId,
            @Param("userId") Long userId
    );

    /** 只有 Post 仍然公开时才减少点赞数。 */
    int decrementPublicLikeCount(
            @Param("postId") Long postId
    );

    long countMyPublicLikes(
            @Param("userId") Long userId
    );

    List<PostFeedRow> selectMyPublicLikes(
            @Param("userId") Long userId,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );
}
