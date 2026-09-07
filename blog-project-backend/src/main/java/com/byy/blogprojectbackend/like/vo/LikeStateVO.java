package com.byy.blogprojectbackend.like.vo;

/** 点赞操作完成后的最终状态。 */
public record LikeStateVO(
        String postId,
        boolean liked,
        int likeCount
) {
}