package com.byy.blogprojectbackend.like.service;

import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.like.vo.LikeStateVO;
import com.byy.blogprojectbackend.post.vo.PostSummaryVO;

public interface LikeService {

    LikeStateVO like(Long postId, Long userId);
    LikeStateVO unlike(Long postId, Long userId);
    PageVO<PostSummaryVO> listMyLikes(Long userId, int page, int pageSize);
}
