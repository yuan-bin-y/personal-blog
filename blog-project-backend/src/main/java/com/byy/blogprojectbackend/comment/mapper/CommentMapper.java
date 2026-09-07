package com.byy.blogprojectbackend.comment.mapper;

import com.byy.blogprojectbackend.comment.entity.Comment;
import com.byy.blogprojectbackend.comment.mapper.projection.CommentRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CommentMapper {
    int countPublicPost(@Param("postId") Long postId);
    long countTopLevel(@Param("postId") Long postId);
    List<CommentRow> selectPage(@Param("postId") Long postId,@Param("offset") long offset,@Param("pageSize") int pageSize);
    CommentRow selectTopLevelView(@Param("id") Long id);
    int insertTopLevel(@Param("comment") Comment comment);
    int incrementPostCommentCount(@Param("postId") Long postId);
    int updateOwnComment(@Param("id") Long id, @Param("userId") Long userId, @Param("content") String content);
    Comment selectAny(@Param("id") Long id);
    Comment selectReplyAny(@Param("parentId") Long parentId);
    void insertReply(@Param("reply") Comment reply);
    int restoreReply(@Param("id") Long id,@Param("ownerId") Long ownerId,@Param("name") String name,@Param("avatar") String avatar,@Param("content") String content);
    int softDeleteOne(@Param("id") Long id,@Param("ownerId") Long ownerId);
    int softDeleteReply(@Param("parentId") Long parentId,@Param("ownerId") Long ownerId);
    int decrementPostCommentCount(@Param("postId") Long postId);
}
