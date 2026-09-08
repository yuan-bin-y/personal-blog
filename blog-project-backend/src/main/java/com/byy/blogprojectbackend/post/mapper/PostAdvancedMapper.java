package com.byy.blogprojectbackend.post.mapper;

import com.byy.blogprojectbackend.post.entity.Post;
import com.byy.blogprojectbackend.post.entity.PostAutosave;
import com.byy.blogprojectbackend.post.entity.PostMedia;
import com.byy.blogprojectbackend.post.entity.PostVersion;
import com.byy.blogprojectbackend.post.mapper.projection.PostFeedRow;
import com.byy.blogprojectbackend.post.mapper.projection.PostVersionRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostAdvancedMapper {
    int schedule(@Param("id") Long id, @Param("publishAt") java.time.LocalDateTime publishAt,
                 @Param("version") int version, @Param("ownerId") Long ownerId);

    int cancelSchedule(@Param("id") Long id, @Param("version") int version,
                       @Param("ownerId") Long ownerId);

    int saveAutosave(@Param("autosave") PostAutosave autosave);

    long countVersions(@Param("postId") Long postId);

    List<PostVersionRow> selectVersions(@Param("postId") Long postId,
                                        @Param("offset") long offset,
                                        @Param("pageSize") int pageSize);

    PostVersionRow selectVersion(@Param("postId") Long postId,
                                 @Param("versionId") Long versionId);

    PostVersionRow selectVersionByNumber(@Param("postId") Long postId,
                                         @Param("versionNo") int versionNo);

    int insertVersion(@Param("version") PostVersion version);

    List<Long> selectTagIds(@Param("postId") Long postId);

    List<PostMedia> selectActiveMedia(@Param("postId") Long postId);

    int restoreSnapshot(@Param("post") Post post, @Param("version") int version,
                        @Param("ownerId") Long ownerId);

    long countTrash(@Param("type") String type);

    List<PostFeedRow> selectTrash(@Param("type") String type,
                                  @Param("offset") long offset,
                                  @Param("pageSize") int pageSize);

    int restoreTrash(@Param("id") Long id, @Param("version") int version,
                     @Param("ownerId") Long ownerId);

    int restoreLatestDeletedMedia(@Param("postId") Long postId);

    int publishNow(@Param("id") Long id, @Param("version") int version,
                   @Param("ownerId") Long ownerId);

    int updateSlug(@Param("id") Long id, @Param("slug") String slug,
                   @Param("version") int version, @Param("ownerId") Long ownerId);

    int countSlugExcluding(@Param("id") Long id, @Param("slug") String slug);

    List<Post> selectDueScheduled(@Param("limit") int limit);

    int publishScheduled(@Param("id") Long id, @Param("version") int version);
}
