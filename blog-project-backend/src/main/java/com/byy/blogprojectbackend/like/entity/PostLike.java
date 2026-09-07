package com.byy.blogprojectbackend.like.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 用户与 Post 之间的持久化点赞关系。 */
@Data
public class PostLike {

    private Long id;
    private Long postId;
    private Long userId;
    private LocalDateTime createdAt;
}