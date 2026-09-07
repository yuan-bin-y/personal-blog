package com.byy.blogprojectbackend.like.service;

import com.byy.blogprojectbackend.like.vo.LikeStateVO;

public interface LikeService {

    LikeStateVO like(Long postId, Long userId);
}