package com.byy.blogprojectbackend.post.service.impl;

import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.exception.VersionConflictException;
import com.byy.blogprojectbackend.post.entity.Post;
import com.byy.blogprojectbackend.post.mapper.PostAdvancedMapper;
import com.byy.blogprojectbackend.post.mapper.PostMapper;
import com.byy.blogprojectbackend.post.service.OwnerPostService;
import com.byy.blogprojectbackend.post.service.PostVersionSnapshotService;
import com.byy.blogprojectbackend.search.event.PublishedPostChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Executes one batch/scheduler item in its own transaction. */
@Service
@RequiredArgsConstructor
public class OwnerAdvancedPostItemService {
    private final PostMapper postMapper;
    private final PostAdvancedMapper advancedMapper;
    private final OwnerPostService ownerPostService;
    private final PostVersionSnapshotService snapshotService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishOne(Long postId, int version, Long ownerId) {
        Post post = require(postId, false);
        if (post.getVersion() != version) {
            throw new VersionConflictException("内容已被其他请求修改，请刷新后重试");
        }
        if ("PUBLISHED".equals(post.getStatus())) {
            return;
        }
        snapshotService.capture(post, ownerId);
        if (advancedMapper.publishNow(postId, version, ownerId) != 1) {
            throw new VersionConflictException("内容已被其他请求修改，请刷新后重试");
        }
        eventPublisher.publishEvent(new PublishedPostChangedEvent(postId));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteOne(Long postId, int version, Long ownerId) {
        Post post = require(postId, false);
        if (post.getVersion() != version) {
            throw new VersionConflictException("内容已被其他请求修改，请刷新后重试");
        }
        if ("TECH".equals(post.getType())) {
            ownerPostService.deleteTech(postId, version, ownerId);
        } else {
            ownerPostService.deleteMoment(postId, version, ownerId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean publishScheduledOne(Long postId) {
        Post post = require(postId, false);
        if (!"SCHEDULED".equals(post.getStatus())) {
            return false;
        }
        snapshotService.capture(post, post.getUpdatedBy());
        if (advancedMapper.publishScheduled(postId, post.getVersion()) != 1) {
            return false;
        }
        eventPublisher.publishEvent(new PublishedPostChangedEvent(postId));
        return true;
    }

    private Post require(Long id, boolean deleted) {
        Post post = postMapper.selectById(id);
        if (post == null || Boolean.TRUE.equals(post.getDeleted()) != deleted) {
            throw new ResourceNotFoundException("内容不存在");
        }
        return post;
    }
}
