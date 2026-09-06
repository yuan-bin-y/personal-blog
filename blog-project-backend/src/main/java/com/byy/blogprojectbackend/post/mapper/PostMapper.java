package com.byy.blogprojectbackend.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byy.blogprojectbackend.post.entity.Post;
import com.byy.blogprojectbackend.post.mapper.projection.PostFeedRow;
import com.byy.blogprojectbackend.post.mapper.projection.PostMediaRow;
import com.byy.blogprojectbackend.post.mapper.projection.PostTagRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import com.byy.blogprojectbackend.post.entity.PostMedia;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * Bootstrap 使用：统计指定类型公开内容数量。
     */
    int countPublishedByType(
            @Param("type") String type
    );

    /**
     * 统计首页公开 Feed。
     */
    long countPublicFeed(
            @Param("type") String type,
            @Param("categorySlug") String categorySlug,
            @Param("tagSlug") String tagSlug
    );

    /**
     * 查询首页公开 Feed 当前页。
     */
    List<PostFeedRow> selectPublicFeed(
            @Param("type") String type,
            @Param("categorySlug") String categorySlug,
            @Param("tagSlug") String tagSlug,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );

    /**
     * 根据 slug 查询公开 TECH 详情主体。
     */
    PostFeedRow selectPublicTechBySlug(
            @Param("slug") String slug
    );

    /**
     * 查询同分类的公开 TECH 作为相关文章。
     */
    List<PostFeedRow> selectRelatedPublicTech(
            @Param("postId") Long postId,
            @Param("categoryId") Long categoryId,
            @Param("limit") int limit
    );

    /**
     * 统计指定年月的公开 MOMENT。
     */
    long countPublicMoments(
            @Param("year") Integer year,
            @Param("month") Integer month
    );

    /**
     * 分页查询指定年月的公开 MOMENT。
     */
    List<PostFeedRow> selectPublicMoments(
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );

    /**
     * 根据 ID 查询公开 MOMENT 详情主体。
     */
    PostFeedRow selectPublicMomentById(
            @Param("id") Long id
    );

    /**
     * 批量查询 Post 标签。
     */
    List<PostTagRow> selectTagsByPostIds(
            @Param("postIds") List<Long> postIds
    );

    /**
     * 批量查询 Post 媒体。
     */
    List<PostMediaRow> selectMediaByPostIds(
            @Param("postIds") List<Long> postIds
    );

    long countOwnerPosts(@Param("type") String type, @Param("status") String status);
    List<PostFeedRow> selectOwnerPosts(@Param("type") String type, @Param("status") String status, @Param("offset") long offset, @Param("pageSize") int pageSize);
    PostFeedRow selectOwnerPostById(@Param("id") Long id, @Param("type") String type);
    int updateOwnerPost(@Param("post") Post post, @Param("version") int version);
    int softDeleteOwnerPost(@Param("id") Long id, @Param("type") String type, @Param("version") int version, @Param("ownerId") Long ownerId);
    int countActiveCategory(@Param("id") Long id);
    int countActiveTags(@Param("ids") List<Long> ids);
    void deletePostTags(@Param("postId") Long postId);
    void insertPostTag(@Param("postId") Long postId, @Param("tagId") Long tagId);
    void softDeletePostMedia(@Param("postId") Long postId);
    void insertPostMedia(@Param("media") PostMedia media);
}
