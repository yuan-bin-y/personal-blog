package com.byy.blogprojectbackend.guestbook.service.impl;

import com.byy.blogprojectbackend.common.exception.*;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.guestbook.entity.GuestbookEntry;
import com.byy.blogprojectbackend.guestbook.mapper.GuestbookMapper;
import com.byy.blogprojectbackend.guestbook.mapper.projection.GuestbookRow;
import com.byy.blogprojectbackend.guestbook.service.GuestbookService;
import com.byy.blogprojectbackend.guestbook.vo.GuestbookVO;
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

@Service @RequiredArgsConstructor
public class GuestbookServiceImpl implements GuestbookService {
    private final GuestbookMapper guestbookMapper; private final SpaceProfileMapper profileMapper; private final IdGenerator idGenerator;
    public PageVO<GuestbookVO> list(int page,int pageSize){long total=guestbookMapper.countPublicTopLevel();List<GuestbookVO>items=guestbookMapper.selectPage((long)(page-1)*pageSize,pageSize).stream().map(this::toVO).toList();long pages=(total+pageSize-1)/pageSize;return new PageVO<>(items,page,pageSize,total,pages,page<pages);}
    @Transactional public ReplyResult<GuestbookVO> reply(Long entryId,ReplyDTO dto,Long ownerId){
        GuestbookEntry parent=guestbookMapper.selectAny(entryId);if(parent==null||parent.getParentId()!=null||Boolean.TRUE.equals(parent.getDeleted()))throw new ResourceNotFoundException("顶层留言不存在");
        SpaceProfile profile=requireProfile(ownerId);GuestbookEntry existing=guestbookMapper.selectReplyAny(entryId);boolean created;
        if(existing==null){GuestbookEntry reply=new GuestbookEntry();reply.setId(idGenerator.nextId());reply.setParentId(entryId);reply.setAuthorUserId(ownerId);reply.setAuthorName(profile.getDisplayName());reply.setAuthorAvatarUrl(profile.getAvatarUrl());reply.setContent(dto.content().trim());guestbookMapper.insertReply(reply);created=true;}
        else if(Boolean.TRUE.equals(existing.getDeleted())){guestbookMapper.restoreReply(existing.getId(),ownerId,profile.getDisplayName(),profile.getAvatarUrl(),dto.content().trim());created=false;}
        else throw new ResourceConflictException("该留言已经有 Owner 回复");
        return new ReplyResult<>(toVO(requireView(entryId)),created);
    }
    @Transactional public void delete(Long entryId,Long ownerId){GuestbookEntry target=guestbookMapper.selectAny(entryId);if(target==null||Boolean.TRUE.equals(target.getDeleted()))throw new ResourceNotFoundException("留言不存在");guestbookMapper.softDeleteOne(entryId,ownerId);if(target.getParentId()==null)guestbookMapper.softDeleteReply(entryId,ownerId);}
    private SpaceProfile requireProfile(Long ownerId){SpaceProfile p=profileMapper.selectByUserId(ownerId);if(p==null)throw new ResourceNotFoundException("Owner Profile 不存在");return p;}
    private GuestbookRow requireView(Long id){GuestbookRow r=guestbookMapper.selectTopLevelView(id);if(r==null)throw new ResourceNotFoundException("留言不存在");return r;}
    private GuestbookVO toVO(GuestbookRow r){ReplyVO reply=r.getReplyId()==null?null:new ReplyVO(String.valueOf(r.getReplyId()),new ReplyAuthorVO(String.valueOf(r.getReplyUserId()),r.getReplyAuthorName(),r.getReplyAuthorAvatar()),r.getReplyContent(),r.getReplyCreatedAt().toInstant(ZoneOffset.UTC));return new GuestbookVO(String.valueOf(r.getId()),new VisitorAuthorVO(r.getAuthorName(),r.getAuthorAvatar()),r.getContent(),r.getCreatedAt().toInstant(ZoneOffset.UTC),reply);}
}
