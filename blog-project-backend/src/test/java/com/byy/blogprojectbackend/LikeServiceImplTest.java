package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.like.entity.PostLike;
import com.byy.blogprojectbackend.like.mapper.LikeMapper;
import com.byy.blogprojectbackend.like.service.impl.LikeServiceImpl;
import com.byy.blogprojectbackend.like.vo.LikeStateVO;
import org.junit.jupiter.api.Test;

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
    private final IdGenerator idGenerator = mock(IdGenerator.class);
    private final LikeServiceImpl likeService = new LikeServiceImpl(likeMapper, idGenerator);

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
}
