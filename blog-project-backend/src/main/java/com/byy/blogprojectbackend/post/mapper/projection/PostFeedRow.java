package com.byy.blogprojectbackend.post.mapper.projection;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页 Feed 主查询结果。
 */
@Data
public class PostFeedRow {

    private Long id;
    private String type;
    private String slug;
    private String title;
    private String summary;
    private String content;
    private String contentFormat;

    private Long authorUserId;
    private String authorName;
    private String authorAvatar;

    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private String categoryDescription;
    private Boolean categoryDeleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    private Integer readingTime;
    private String status;
    private Integer likeCount;
    private Integer commentCount;
    private Integer version;
}
