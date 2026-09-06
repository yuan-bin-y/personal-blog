package com.byy.blogprojectbackend.site.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SiteStatsMapper {

    /**
     * 已发布 TECH 数量。
     */
    @Select("""
            SELECT COUNT(*)
            FROM post
            WHERE type = 'TECH'
              AND status = 'PUBLISHED'
              AND deleted = 0
              AND published_at <= UTC_TIMESTAMP()
            """)
    int countPublishedTechPosts();

    /**
     * 已发布 MOMENT 数量。
     */
    @Select("""
            SELECT COUNT(*)
            FROM post
            WHERE type = 'MOMENT'
              AND status = 'PUBLISHED'
              AND deleted = 0
              AND published_at <= UTC_TIMESTAMP()
            """)
    int countPublishedMoments();

    /**
     * 未删除的顶层留言数量。
     */
    @Select("""
            SELECT COUNT(*)
            FROM guestbook
            WHERE deleted = 0
              AND parent_id IS NULL
            """)
    int countGuestbookEntries();
}