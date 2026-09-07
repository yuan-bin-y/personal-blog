package com.byy.blogprojectbackend.guestbook.mapper;

import com.byy.blogprojectbackend.guestbook.entity.GuestbookEntry;
import com.byy.blogprojectbackend.guestbook.mapper.projection.GuestbookRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface GuestbookMapper {

    /**
     * 统计公开的顶级留言数量。
     */
    int countPublicTopLevel();

    List<GuestbookRow> selectPage(@Param("offset") long offset,@Param("pageSize") int pageSize);
    GuestbookRow selectTopLevelView(@Param("id") Long id);
    int insertTopLevel(@Param("entry") GuestbookEntry entry);
    int updateOwnEntry(@Param("id") Long id, @Param("userId") Long userId, @Param("content") String content);
    int softDeleteOwnEntry(@Param("id") Long id, @Param("userId") Long userId);
    GuestbookEntry selectAny(@Param("id") Long id);
    GuestbookEntry selectReplyAny(@Param("parentId") Long parentId);
    void insertReply(@Param("reply") GuestbookEntry reply);
    int restoreReply(@Param("id") Long id,@Param("ownerId") Long ownerId,@Param("name") String name,@Param("avatar") String avatar,@Param("content") String content);
    int softDeleteOne(@Param("id") Long id,@Param("ownerId") Long ownerId);
    int softDeleteReply(@Param("parentId") Long parentId,@Param("ownerId") Long ownerId);
}
