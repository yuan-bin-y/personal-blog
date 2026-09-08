package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.comment.dto.CreateCommentDTO;
import com.byy.blogprojectbackend.comment.dto.UpdateCommentDTO;
import com.byy.blogprojectbackend.comment.entity.Comment;
import com.byy.blogprojectbackend.comment.mapper.CommentMapper;
import com.byy.blogprojectbackend.comment.mapper.projection.CommentRow;
import com.byy.blogprojectbackend.comment.service.impl.CommentServiceImpl;
import com.byy.blogprojectbackend.comment.vo.CommentVO;
import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.exception.ForbiddenOperationException;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.profile.entity.SpaceProfile;
import com.byy.blogprojectbackend.profile.mapper.SpaceProfileMapper;
import com.byy.blogprojectbackend.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CommentServiceImplTest {

    private final CommentMapper commentMapper = mock(CommentMapper.class);
    private final SpaceProfileMapper profileMapper = mock(SpaceProfileMapper.class);
    private final IdGenerator idGenerator = mock(IdGenerator.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final CommentServiceImpl commentService = new CommentServiceImpl(
            commentMapper,
            profileMapper,
            idGenerator,
            notificationService
    );

    @Test
    void create_buildsAuthorFromProfileAndReturnsOwnedComment() {
        long postId = 101L;
        long userId = 202L;
        long commentId = 303L;

        SpaceProfile profile = new SpaceProfile();
        profile.setUserId(userId);
        profile.setDisplayName("暖光访客");
        profile.setAvatarUrl("/media/visitor.png");

        CommentRow row = new CommentRow();
        row.setId(commentId);
        row.setPostId(postId);
        row.setAuthorUserId(userId);
        row.setAuthorName(profile.getDisplayName());
        row.setAuthorAvatar(profile.getAvatarUrl());
        row.setContent("这篇文章很有帮助。");
        row.setCreatedAt(LocalDateTime.of(2026, 9, 7, 12, 0));

        when(commentMapper.countPublicPost(postId)).thenReturn(1);
        when(profileMapper.selectByUserId(userId)).thenReturn(profile);
        when(idGenerator.nextId()).thenReturn(commentId);
        when(commentMapper.insertTopLevel(any(Comment.class))).thenReturn(1);
        when(commentMapper.incrementPostCommentCount(postId)).thenReturn(1);
        when(commentMapper.selectTopLevelView(commentId)).thenReturn(row);

        CommentVO result = commentService.create(
                postId,
                new CreateCommentDTO("  这篇文章很有帮助。  "),
                userId
        );

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentMapper).insertTopLevel(captor.capture());
        Comment inserted = captor.getValue();

        assertEquals(userId, inserted.getAuthorUserId());
        assertEquals("暖光访客", inserted.getAuthorName());
        assertEquals("这篇文章很有帮助。", inserted.getContent());
        assertEquals(String.valueOf(commentId), result.id());
        assertTrue(result.ownedByMe());
        verify(notificationService).notifyCommentCreated(
                userId,
                "暖光访客",
                postId
        );
    }

    @Test
    void create_whenPostIsNotPublic_throwsResourceNotFound() {
        when(commentMapper.countPublicPost(101L)).thenReturn(0);

        assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.create(
                        101L,
                        new CreateCommentDTO("评论内容"),
                        202L
                )
        );

        verifyNoInteractions(profileMapper);
    }

    @Test
    void updateOwnComment_whenCurrentUserIsAuthor_updatesContent() {
        long commentId = 303L;
        long postId = 101L;
        long userId = 202L;

        Comment existing = new Comment();
        existing.setId(commentId);
        existing.setPostId(postId);
        existing.setAuthorUserId(userId);
        existing.setParentId(null);
        existing.setDeleted(false);

        CommentRow updatedRow = new CommentRow();
        updatedRow.setId(commentId);
        updatedRow.setPostId(postId);
        updatedRow.setAuthorUserId(userId);
        updatedRow.setAuthorName("暖光访客");
        updatedRow.setContent("修改后的评论");
        updatedRow.setCreatedAt(LocalDateTime.of(2026, 9, 7, 12, 0));

        when(commentMapper.selectAny(commentId)).thenReturn(existing);
        when(commentMapper.updateOwnComment(
                commentId,
                userId,
                "修改后的评论"
        )).thenReturn(1);
        when(commentMapper.selectTopLevelView(commentId)).thenReturn(updatedRow);

        CommentVO result = commentService.updateOwnComment(
                commentId,
                new UpdateCommentDTO("  修改后的评论  "),
                userId
        );

        assertEquals("修改后的评论", result.content());
        assertTrue(result.ownedByMe());
    }

    @Test
    void updateOwnComment_whenCurrentUserIsNotAuthor_throwsForbidden() {
        long commentId = 303L;
        long authorUserId = 202L;
        long currentUserId = 999L;

        Comment existing = new Comment();
        existing.setId(commentId);
        existing.setAuthorUserId(authorUserId);
        existing.setParentId(null);
        existing.setDeleted(false);

        when(commentMapper.selectAny(commentId)).thenReturn(existing);

        assertThrows(
                ForbiddenOperationException.class,
                () -> commentService.updateOwnComment(
                        commentId,
                        new UpdateCommentDTO("不能修改别人的评论"),
                        currentUserId
                )
        );

        verify(commentMapper, never()).updateOwnComment(
                any(),
                any(),
                any()
        );
    }

    @Test
    void deleteOwnComment_whenCurrentUserIsAuthor_softDeletesCommentAndReply() {
        long commentId = 303L;
        long postId = 101L;
        long userId = 202L;

        Comment existing = new Comment();
        existing.setId(commentId);
        existing.setPostId(postId);
        existing.setAuthorUserId(userId);
        existing.setParentId(null);
        existing.setDeleted(false);

        when(commentMapper.selectAny(commentId)).thenReturn(existing);
        when(commentMapper.softDeleteOwnComment(commentId, userId)).thenReturn(1);

        commentService.deleteOwnComment(commentId, userId);

        verify(commentMapper).softDeleteReply(commentId, userId);
        verify(commentMapper).decrementPostCommentCount(postId);
    }

    @Test
    void deleteOwnComment_whenCurrentUserIsNotAuthor_throwsForbidden() {
        long commentId = 303L;
        long authorUserId = 202L;
        long currentUserId = 999L;

        Comment existing = new Comment();
        existing.setId(commentId);
        existing.setPostId(101L);
        existing.setAuthorUserId(authorUserId);
        existing.setParentId(null);
        existing.setDeleted(false);

        when(commentMapper.selectAny(commentId)).thenReturn(existing);

        assertThrows(
                ForbiddenOperationException.class,
                () -> commentService.deleteOwnComment(commentId, currentUserId)
        );

        verify(commentMapper, never()).softDeleteOwnComment(any(), any());
        verify(commentMapper, never()).decrementPostCommentCount(any());
    }

    @Test
    void deleteOwnComment_whenConditionalUpdateMisses_throwsResourceNotFound() {
        long commentId = 303L;
        long postId = 101L;
        long userId = 202L;

        Comment existing = new Comment();
        existing.setId(commentId);
        existing.setPostId(postId);
        existing.setAuthorUserId(userId);
        existing.setParentId(null);
        existing.setDeleted(false);

        when(commentMapper.selectAny(commentId)).thenReturn(existing);
        when(commentMapper.softDeleteOwnComment(commentId, userId)).thenReturn(0);

        assertThrows(
                ResourceNotFoundException.class,
                () -> commentService.deleteOwnComment(commentId, userId)
        );

        verify(commentMapper, never()).softDeleteReply(any(), any());
        verify(commentMapper, never()).decrementPostCommentCount(any());
    }
}
