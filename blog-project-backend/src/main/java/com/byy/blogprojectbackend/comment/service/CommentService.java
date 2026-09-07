package com.byy.blogprojectbackend.comment.service;

import com.byy.blogprojectbackend.comment.dto.CreateCommentDTO;
import com.byy.blogprojectbackend.comment.dto.UpdateCommentDTO;
import com.byy.blogprojectbackend.comment.vo.CommentVO;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.interaction.dto.ReplyDTO;
import com.byy.blogprojectbackend.interaction.service.ReplyResult;

public interface CommentService {
    PageVO<CommentVO> list(Long postId, int page, int pageSize, Long currentUserId);
    CommentVO create(Long postId, CreateCommentDTO dto, Long userId);
    CommentVO updateOwnComment(Long commentId, UpdateCommentDTO dto, Long userId);
    ReplyResult<CommentVO> reply(Long commentId,ReplyDTO dto,Long ownerId);
    void delete(Long commentId,Long ownerId);
}
