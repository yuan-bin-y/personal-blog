package com.byy.blogprojectbackend.post.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * post 表实体。
 *
 * TECH 和 MOMENT 共用一张表，
 * 具体字段限制由数据库 CHECK 和业务层共同保证。
 */
@Data
@TableName("post")
public class Post {

    @TableId
    private Long id;

    private String type;
    private String slug;
    private String title;
    private String summary;
    private String content;
    private String contentFormat;
    private Long categoryId;
    private Integer readingTimeMinutes;
    private String status;
    private LocalDateTime publishedAt;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;

    @Version
    private Integer version;

    private Boolean deleted;
    private LocalDateTime deletedAt;
    private Long deletedBy;
}