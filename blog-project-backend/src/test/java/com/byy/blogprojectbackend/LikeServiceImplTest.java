package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.like.entity.PostLike;
import com.byy.blogprojectbackend.like.mapper.LikeMapper;
import com.byy.blogprojectbackend.like.service.impl.LikeServiceImpl;
import com.byy.blogprojectbackend.like.vo.LikeStateVO;
import com.byy.blogprojectbackend.post.mapper.PostMapper;
import com.byy.blogprojectbackend.post.mapper.projection.PostFeedRow;
import com.byy.blogprojectbackend.post.vo.PostSummaryVO;
import com.byy.blogprojectbackend.common.vo.PageVO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LikeServiceImplTest {

    private final LikeMapper likeMapper = mock(LikeMapper.class);
    private final PostMapper postMapper = mock(PostMapper.class);
    private final IdGenerator idGenerator = mock(IdGenerator.class);
    private final LikeServiceImpl likeService = new LikeServiceImpl(
            likeMapper,
            postMapper,
            idGenerator
    );

    @Test
    void like_whenFirstLike_createsRelationAndIncrementsCount() {
        long postId = 101L;
        long userId = 202L;

        when(likeMapper.selectPublicLikeCount(postId)).thenReturn(10, 11);
        when(idGenerator.nextId()).thenReturn(303L);
        when(likeMapper.insertIgnore(any(PostLike.class))).thenReturn(1);
        when(likeMapper.incrementPublicLikeCount(postId)).thenReturn(1);

        LikeStateVO result = likeService.like(postId, userId);

        assertTrue(result.liked());
        assertEquals(11, result.likeCount());
        verify(likeMapper).incrementPublicLikeCount(postId);
    }

    @Test
    void like_whenAlreadyLiked_doesNotIncrementAgain() {
        long postId = 101L;

        when(likeMapper.selectPublicLikeCount(postId)).thenReturn(11);
        when(idGenerator.nextId()).thenReturn(303L);
        when(likeMapper.insertIgnore(any(PostLike.class))).thenReturn(0);

        LikeStateVO result = likeService.like(postId, 202L);

        assertTrue(result.liked());
        assertEquals(11, result.likeCount());
        verify(likeMapper, never()).incrementPublicLikeCount(any());
    }

    @Test
    void like_whenPostIsNotPublic_throwsResourceNotFound() {
        when(likeMapper.selectPublicLikeCount(101L)).thenReturn(null);

        assertThrows(
                ResourceNotFoundException.class,
                () -> likeService.like(101L, 202L)
        );

        verify(likeMapper, never()).insertIgnore(any());
    }

    @Test
    void unlike_whenRelationExists_deletesRelationAndDecrementsCount() {
        long postId = 101L;
        long userId = 202L;

        when(likeMapper.selectPublicLikeCount(postId)).thenReturn(10, 9);
        when(likeMapper.deleteRelation(postId, userId)).thenReturn(1);
        when(likeMapper.decrementPublicLikeCount(postId)).thenReturn(1);

        LikeStateVO result = likeService.unlike(postId, userId);

        assertEquals(false, result.liked());
        assertEquals(9, result.likeCount());
        verify(likeMapper).decrementPublicLikeCount(postId);
    }

    @Test
    void unlike_whenAlreadyUnliked_doesNotDecrementAgain() {
        long postId = 101L;

        when(likeMapper.selectPublicLikeCount(postId)).thenReturn(9);
        when(likeMapper.deleteRelation(postId, 202L)).thenReturn(0);

        LikeStateVO result = likeService.unlike(postId, 202L);

        assertEquals(false, result.liked());
        assertEquals(9, result.likeCount());
        verify(likeMapper, never()).decrementPublicLikeCount(any());
    }

    @Test
    void listMyLikes_returnsEnrichedPublishedPostsMarkedAsLiked() {
        PostFeedRow row = new PostFeedRow();
        row.setId(101L);
        row.setType("MOMENT");
        row.setContent("被点赞的说说");
        row.setAuthorUserId(1L);
        row.setAuthorName("玢");
        row.setCreatedAt(LocalDateTime.of(2026, 9, 7, 12, 0));
        row.setUpdatedAt(LocalDateTime.of(2026, 9, 7, 12, 0));
        row.setPublishedAt(LocalDateTime.of(2026, 9, 7, 12, 0));
        row.setLikeCount(3);
        row.setCommentCount(1);

        when(likeMapper.countMyPublicLikes(202L)).thenReturn(1L);
        when(likeMapper.selectMyPublicLikes(202L, 0L, 10)).thenReturn(List.of(row));
        when(postMapper.selectTagsByPostIds(List.of(101L))).thenReturn(List.of());
        when(postMapper.selectMediaByPostIds(List.of(101L))).thenReturn(List.of());

        PageVO<PostSummaryVO> result = likeService.listMyLikes(202L, 1, 10);

        assertEquals(1, result.total());
        assertEquals("101", result.items().get(0).id());
        assertTrue(result.items().get(0).likedByMe());
    }
}
