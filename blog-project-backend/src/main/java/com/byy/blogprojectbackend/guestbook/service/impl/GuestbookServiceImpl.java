package com.byy.blogprojectbackend.guestbook.service.impl;

import com.byy.blogprojectbackend.common.exception.ForbiddenOperationException;
import com.byy.blogprojectbackend.common.exception.ResourceConflictException;
import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.guestbook.dto.CreateGuestbookDTO;
import com.byy.blogprojectbackend.guestbook.dto.UpdateGuestbookDTO;
import com.byy.blogprojectbackend.guestbook.entity.GuestbookEntry;
import com.byy.blogprojectbackend.guestbook.mapper.GuestbookMapper;
import com.byy.blogprojectbackend.guestbook.mapper.projection.GuestbookRow;
import com.byy.blogprojectbackend.guestbook.service.GuestbookService;
import com.byy.blogprojectbackend.guestbook.vo.GuestbookVO;
import com.byy.blogprojectbackend.interaction.dto.ReplyDTO;
import com.byy.blogprojectbackend.interaction.service.ReplyResult;
import com.byy.blogprojectbackend.interaction.vo.ReplyAuthorVO;
import com.byy.blogprojectbackend.interaction.vo.ReplyVO;
import com.byy.blogprojectbackend.interaction.vo.VisitorAuthorVO;
import com.byy.blogprojectbackend.notification.service.NotificationService;
import com.byy.blogprojectbackend.profile.entity.SpaceProfile;
import com.byy.blogprojectbackend.profile.mapper.SpaceProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestbookServiceImpl implements GuestbookService {

    private final GuestbookMapper guestbookMapper;
    private final SpaceProfileMapper profileMapper;
    private final IdGenerator idGenerator;
    private final NotificationService notificationService;

    @Override
    public PageVO<GuestbookVO> list(int page, int pageSize, Long currentUserId) {
        long total = guestbookMapper.countPublicTopLevel();
        long offset = (long) (page - 1) * pageSize;
        List<GuestbookVO> items = guestbookMapper.selectPage(offset, pageSize)
                .stream()
                .map(row -> toVO(row, currentUserId))
                .toList();
        long totalPages = (total + pageSize - 1) / pageSize;

        return new PageVO<>(items, page, pageSize, total, totalPages, page < totalPages);
    }

    /** 登录用户发布一条不属于任何 Post 的顶层空间留言。 */
    @Override
    @Transactional
    public GuestbookVO create(CreateGuestbookDTO dto, Long userId) {
        SpaceProfile profile = requireProfile(userId);

        GuestbookEntry entry = new GuestbookEntry();
        entry.setId(idGenerator.nextId());
        entry.setParentId(null);
        entry.setAuthorUserId(userId);
        entry.setAuthorName(profile.getDisplayName());
        entry.setAuthorAvatarUrl(profile.getAvatarUrl());
        entry.setContent(dto.content().trim());

        if (guestbookMapper.insertTopLevel(entry) != 1) {
            throw new IllegalStateException("留言创建失败");
        }
        notificationService.notifyGuestbookCreated(
                userId,
                profile.getDisplayName(),
                entry.getId()
        );
        return toVO(requireView(entry.getId()), userId);
    }

    /** 只允许留言作者修改自己的顶层留言。 */
    @Override
    @Transactional
    public GuestbookVO updateOwnEntry(Long entryId, UpdateGuestbookDTO dto, Long userId) {
        GuestbookEntry entry = requireActiveTopLevel(entryId);
        requireOwner(entry, userId, "只能修改自己发布的留言");

        int updated = guestbookMapper.updateOwnEntry(entryId, userId, dto.content().trim());
        if (updated != 1) {
            throw new ResourceNotFoundException("留言不存在");
        }
        return toVO(requireView(entryId), userId);
    }

    /** 软删除自己的顶层留言，同时隐藏它下面的 Owner 回复。 */
    @Override
    @Transactional
    public void deleteOwnEntry(Long entryId, Long userId) {
        GuestbookEntry entry = requireActiveTopLevel(entryId);
        requireOwner(entry, userId, "只能删除自己发布的留言");

        if (guestbookMapper.softDeleteOwnEntry(entryId, userId) != 1) {
            throw new ResourceNotFoundException("留言不存在");
        }
        guestbookMapper.softDeleteReply(entryId, userId);
    }

    @Override
    @Transactional
    public ReplyResult<GuestbookVO> reply(Long entryId, ReplyDTO dto, Long ownerId) {
        GuestbookEntry parent = requireActiveTopLevel(entryId);
        SpaceProfile profile = requireProfile(ownerId);
        GuestbookEntry existing = guestbookMapper.selectReplyAny(entryId);
        boolean created;

        if (existing == null) {
            GuestbookEntry reply = new GuestbookEntry();
            reply.setId(idGenerator.nextId());
            reply.setParentId(entryId);
            reply.setAuthorUserId(ownerId);
            reply.setAuthorName(profile.getDisplayName());
            reply.setAuthorAvatarUrl(profile.getAvatarUrl());
            reply.setContent(dto.content().trim());
            guestbookMapper.insertReply(reply);
            created = true;
        } else if (Boolean.TRUE.equals(existing.getDeleted())) {
            guestbookMapper.restoreReply(
                    existing.getId(),
                    ownerId,
                    profile.getDisplayName(),
                    profile.getAvatarUrl(),
                    dto.content().trim()
            );
            created = false;
        } else {
            throw new ResourceConflictException("该留言已经有 Owner 回复");
        }

        notificationService.notifyGuestbookReplied(
                ownerId,
                profile.getDisplayName(),
                parent.getAuthorUserId(),
                entryId
        );

        return new ReplyResult<>(toVO(requireView(parent.getId()), ownerId), created);
    }

    /** Owner 管理删除，可删除任意留言；删除顶层留言时同时软删回复。 */
    @Override
    @Transactional
    public void delete(Long entryId, Long ownerId) {
        GuestbookEntry target = guestbookMapper.selectAny(entryId);
        if (target == null || Boolean.TRUE.equals(target.getDeleted())) {
            throw new ResourceNotFoundException("留言不存在");
        }

        if (guestbookMapper.softDeleteOne(entryId, ownerId) != 1) {
            throw new ResourceNotFoundException("留言不存在");
        }
        if (target.getParentId() == null) {
            guestbookMapper.softDeleteReply(entryId, ownerId);
        }
    }

    private GuestbookEntry requireActiveTopLevel(Long entryId) {
        GuestbookEntry entry = guestbookMapper.selectAny(entryId);
        if (entry == null || Boolean.TRUE.equals(entry.getDeleted()) || entry.getParentId() != null) {
            throw new ResourceNotFoundException("留言不存在");
        }
        return entry;
    }

    private void requireOwner(GuestbookEntry entry, Long userId, String message) {
        if (!userId.equals(entry.getAuthorUserId())) {
            throw new ForbiddenOperationException(message);
        }
    }

    private SpaceProfile requireProfile(Long userId) {
        SpaceProfile profile = profileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new ResourceNotFoundException("用户资料不存在");
        }
        return profile;
    }

    private GuestbookRow requireView(Long id) {
        GuestbookRow row = guestbookMapper.selectTopLevelView(id);
        if (row == null) {
            throw new ResourceNotFoundException("留言不存在");
        }
        return row;
    }

    private GuestbookVO toVO(GuestbookRow row, Long currentUserId) {
        ReplyVO reply = row.getReplyId() == null
                ? null
                : new ReplyVO(
                        String.valueOf(row.getReplyId()),
                        new ReplyAuthorVO(
                                String.valueOf(row.getReplyUserId()),
                                row.getReplyAuthorName(),
                                row.getReplyAuthorAvatar()
                        ),
                        row.getReplyContent(),
                        row.getReplyCreatedAt().toInstant(ZoneOffset.UTC)
                );
        boolean ownedByMe = currentUserId != null && currentUserId.equals(row.getAuthorUserId());

        return new GuestbookVO(
                String.valueOf(row.getId()),
                new VisitorAuthorVO(row.getAuthorName(), row.getAuthorAvatar()),
                row.getContent(),
                row.getCreatedAt().toInstant(ZoneOffset.UTC),
                ownedByMe,
                reply
        );
    }
}
