package com.byy.blogprojectbackend.comment.service.impl;

import com.byy.blogprojectbackend.comment.dto.CreateCommentDTO;
import com.byy.blogprojectbackend.comment.dto.UpdateCommentDTO;
import com.byy.blogprojectbackend.comment.entity.Comment;
import com.byy.blogprojectbackend.comment.mapper.CommentMapper;
import com.byy.blogprojectbackend.comment.mapper.projection.CommentRow;
import com.byy.blogprojectbackend.comment.service.CommentService;
import com.byy.blogprojectbackend.comment.vo.CommentVO;
import com.byy.blogprojectbackend.common.exception.ResourceConflictException;
import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.exception.ForbiddenOperationException;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.common.vo.PageVO;
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
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final SpaceProfileMapper profileMapper;
    private final IdGenerator idGenerator;
    private final NotificationService notificationService;

    @Override
    public PageVO<CommentVO> list(
            Long postId,
            int page,
            int pageSize,
            Long currentUserId
    ) {
        if (commentMapper.countPublicPost(postId) != 1) {
            throw new ResourceNotFoundException("公开内容不存在");
        }

        long total = commentMapper.countTopLevel(postId);
        long offset = (long) (page - 1) * pageSize;
        List<CommentVO> items = commentMapper
                .selectPage(postId, offset, pageSize)
                .stream()
                .map(row -> toVO(row, currentUserId))
                .toList();
        long totalPages = (total + pageSize - 1) / pageSize;

        return new PageVO<>(
                items,
                page,
                pageSize,
                total,
                totalPages,
                page < totalPages
        );
    }

    /**
     * 登录用户对公开 Post 发表一条顶层评论。
     * 评论和 post.comment_count 必须在同一事务中更新，避免计数失真。
     */
    @Override
    @Transactional
    public CommentVO create(
            Long postId,
            CreateCommentDTO dto,
            Long userId
    ) {
        if (commentMapper.countPublicPost(postId) != 1) {
            throw new ResourceNotFoundException("公开内容不存在");
        }

        SpaceProfile profile = requireProfile(userId);

        Comment comment = new Comment();
        comment.setId(idGenerator.nextId());
        comment.setPostId(postId);
        comment.setParentId(null);
        comment.setAuthorUserId(userId);
        comment.setAuthorName(profile.getDisplayName());
        comment.setAuthorAvatarUrl(profile.getAvatarUrl());
        comment.setContent(dto.content().trim());

        if (commentMapper.insertTopLevel(comment) != 1) {
            throw new IllegalStateException("评论创建失败");
        }

        /*
         * 再次附带公开条件更新，可以覆盖“检查后文章立刻被撤回”的并发情况。
         * 更新失败会抛异常，并由事务回滚刚插入的评论。
         */
        if (commentMapper.incrementPostCommentCount(postId) != 1) {
            throw new ResourceNotFoundException("公开内容不存在");
        }

        notificationService.notifyCommentCreated(
                userId,
                profile.getDisplayName(),
                postId
        );

        return toVO(requireView(comment.getId()), userId);
    }

    /**
     * 只允许作者修改自己发布的顶层评论。
     * Service 先区分 404/403，UPDATE 再携带 userId 条件作为最终权限防线。
     */
    @Override
    @Transactional
    public CommentVO updateOwnComment(
            Long commentId,
            UpdateCommentDTO dto,
            Long userId
    ) {
        Comment comment = commentMapper.selectAny(commentId);
        if (comment == null
                || Boolean.TRUE.equals(comment.getDeleted())
                || comment.getParentId() != null) {
            throw new ResourceNotFoundException("评论不存在");
        }

        if (!userId.equals(comment.getAuthorUserId())) {
            throw new ForbiddenOperationException("只能修改自己发布的评论");
        }

        int updated = commentMapper.updateOwnComment(
                commentId,
                userId,
                dto.content().trim()
        );
        if (updated != 1) {
            // 读取后若评论被并发删除，不能误报修改成功。
            throw new ResourceNotFoundException("评论不存在");
        }

        return toVO(requireView(commentId), userId);
    }

    @Override
    @Transactional
    public ReplyResult<CommentVO> reply(
            Long commentId,
            ReplyDTO dto,
            Long ownerId
    ) {
        Comment parent = commentMapper.selectAny(commentId);
        if (parent == null
                || parent.getParentId() != null
                || Boolean.TRUE.equals(parent.getDeleted())) {
            throw new ResourceNotFoundException("顶层评论不存在");
        }

        SpaceProfile profile = requireProfile(ownerId);
        Comment existing = commentMapper.selectReplyAny(commentId);
        boolean created;

        if (existing == null) {
            Comment reply = new Comment();
            reply.setId(idGenerator.nextId());
            reply.setPostId(parent.getPostId());
            reply.setParentId(commentId);
            reply.setAuthorUserId(ownerId);
            reply.setAuthorName(profile.getDisplayName());
            reply.setAuthorAvatarUrl(profile.getAvatarUrl());
            reply.setContent(dto.content().trim());
            commentMapper.insertReply(reply);
            created = true;
        } else if (Boolean.TRUE.equals(existing.getDeleted())) {
            commentMapper.restoreReply(
                    existing.getId(),
                    ownerId,
                    profile.getDisplayName(),
                    profile.getAvatarUrl(),
                    dto.content().trim()
            );
            created = false;
        } else {
            throw new ResourceConflictException("该评论已经有 Owner 回复");
        }

        notificationService.notifyCommentReplied(
                ownerId,
                profile.getDisplayName(),
                parent.getAuthorUserId(),
                commentId
        );

        return new ReplyResult<>(
                toVO(requireView(commentId), ownerId),
                created
        );
    }

    @Override
    @Transactional
    public void delete(Long commentId, Long ownerId) {
        Comment target = commentMapper.selectAny(commentId);
        if (target == null || Boolean.TRUE.equals(target.getDeleted())) {
            throw new ResourceNotFoundException("评论不存在");
        }

        commentMapper.softDeleteOne(commentId, ownerId);
        if (target.getParentId() == null) {
            commentMapper.softDeleteReply(commentId, ownerId);
            commentMapper.decrementPostCommentCount(target.getPostId());
        }
    }

    private SpaceProfile requireProfile(Long userId) {
        SpaceProfile profile = profileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new ResourceNotFoundException("用户资料不存在");
        }
        return profile;
    }

    private CommentRow requireView(Long id) {
        CommentRow row = commentMapper.selectTopLevelView(id);
        if (row == null) {
            throw new ResourceNotFoundException("评论不存在");
        }
        return row;
    }

    private CommentVO toVO(CommentRow row, Long currentUserId) {
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

        boolean ownedByMe = currentUserId != null
                && currentUserId.equals(row.getAuthorUserId());

        return new CommentVO(
                String.valueOf(row.getId()),
                String.valueOf(row.getPostId()),
                new VisitorAuthorVO(
                        row.getAuthorName(),
                        row.getAuthorAvatar()
                ),
                row.getContent(),
                row.getCreatedAt().toInstant(ZoneOffset.UTC),
                ownedByMe,
                reply
        );
    }

    @Override
    @Transactional
    public void deleteOwnComment(
            Long commentId,
            Long userId
    ) {
        Comment comment = commentMapper.selectAny(commentId);

        // 只能删除存在、未删除的顶层评论
        if (comment == null
                || Boolean.TRUE.equals(comment.getDeleted())
                || comment.getParentId() != null) {
            throw new ResourceNotFoundException("评论不存在");
        }

        // 评论存在，但作者不是当前登录用户
        if (!userId.equals(comment.getAuthorUserId())) {
            throw new ForbiddenOperationException(
                    "只能删除自己发布的评论"
            );
        }

        /*
         * SQL 再次携带 author_user_id 条件，
         * 防止检查后评论状态发生并发变化。
         */
        int deleted = commentMapper.softDeleteOwnComment(
                commentId,
                userId
        );

        if (deleted != 1) {
            throw new ResourceNotFoundException("评论不存在");
        }

        // 删除顶层评论时，它下面的 Owner 回复也不能继续显示
        commentMapper.softDeleteReply(
                commentId,
                userId
        );

        // 维护 Post 的冗余评论数量
        commentMapper.decrementPostCommentCount(
                comment.getPostId()
        );
    }
}
