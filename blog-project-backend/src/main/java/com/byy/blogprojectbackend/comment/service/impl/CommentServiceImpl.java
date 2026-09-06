package com.byy.blogprojectbackend.comment.service.impl;

import com.byy.blogprojectbackend.comment.entity.Comment;
import com.byy.blogprojectbackend.comment.mapper.CommentMapper;
import com.byy.blogprojectbackend.comment.mapper.projection.CommentRow;
import com.byy.blogprojectbackend.comment.service.CommentService;
import com.byy.blogprojectbackend.comment.vo.CommentVO;
import com.byy.blogprojectbackend.common.exception.*;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.interaction.dto.ReplyDTO;
import com.byy.blogprojectbackend.interaction.service.ReplyResult;
import com.byy.blogprojectbackend.interaction.vo.*;
import com.byy.blogprojectbackend.profile.entity.SpaceProfile;
import com.byy.blogprojectbackend.profile.mapper.SpaceProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentMapper commentMapper;
    private final SpaceProfileMapper profileMapper;
    private final IdGenerator idGenerator;

    public PageVO<CommentVO> list(Long postId,int page,int pageSize){
        if(commentMapper.countPublicPost(postId)!=1)throw new ResourceNotFoundException("公开内容不存在");
        long total=commentMapper.countTopLevel(postId); List<CommentVO> items=commentMapper.selectPage(postId,(long)(page-1)*pageSize,pageSize).stream().map(this::toVO).toList();
        long pages=(total+pageSize-1)/pageSize;return new PageVO<>(items,page,pageSize,total,pages,page<pages);
    }

    @Transactional public ReplyResult<CommentVO> reply(Long commentId,ReplyDTO dto,Long ownerId){
        Comment parent=commentMapper.selectAny(commentId);
        if(parent==null||parent.getParentId()!=null||Boolean.TRUE.equals(parent.getDeleted()))throw new ResourceNotFoundException("顶层评论不存在");
        SpaceProfile profile=requireProfile(ownerId); Comment existing=commentMapper.selectReplyAny(commentId); boolean created;
        if(existing==null){Comment reply=new Comment();reply.setId(idGenerator.nextId());reply.setPostId(parent.getPostId());reply.setParentId(commentId);reply.setAuthorUserId(ownerId);reply.setAuthorName(profile.getDisplayName());reply.setAuthorAvatarUrl(profile.getAvatarUrl());reply.setContent(dto.content().trim());commentMapper.insertReply(reply);created=true;}
        else if(Boolean.TRUE.equals(existing.getDeleted())){commentMapper.restoreReply(existing.getId(),ownerId,profile.getDisplayName(),profile.getAvatarUrl(),dto.content().trim());created=false;}
        else throw new ResourceConflictException("该评论已经有 Owner 回复");
        return new ReplyResult<>(toVO(requireView(commentId)),created);
    }

    @Transactional public void delete(Long commentId,Long ownerId){
        Comment target=commentMapper.selectAny(commentId);if(target==null||Boolean.TRUE.equals(target.getDeleted()))throw new ResourceNotFoundException("评论不存在");
        commentMapper.softDeleteOne(commentId,ownerId);
        if(target.getParentId()==null){commentMapper.softDeleteReply(commentId,ownerId);commentMapper.decrementPostCommentCount(target.getPostId());}
    }

    private SpaceProfile requireProfile(Long ownerId){SpaceProfile p=profileMapper.selectByUserId(ownerId);if(p==null)throw new ResourceNotFoundException("Owner Profile 不存在");return p;}
    private CommentRow requireView(Long id){CommentRow r=commentMapper.selectTopLevelView(id);if(r==null)throw new ResourceNotFoundException("评论不存在");return r;}
    private CommentVO toVO(CommentRow r){ReplyVO reply=r.getReplyId()==null?null:new ReplyVO(String.valueOf(r.getReplyId()),new ReplyAuthorVO(String.valueOf(r.getReplyUserId()),r.getReplyAuthorName(),r.getReplyAuthorAvatar()),r.getReplyContent(),r.getReplyCreatedAt().toInstant(ZoneOffset.UTC));return new CommentVO(String.valueOf(r.getId()),String.valueOf(r.getPostId()),new VisitorAuthorVO(r.getAuthorName(),r.getAuthorAvatar()),r.getContent(),r.getCreatedAt().toInstant(ZoneOffset.UTC),reply);}
}
