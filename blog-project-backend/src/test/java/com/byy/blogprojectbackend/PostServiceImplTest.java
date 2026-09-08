package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.post.mapper.PostMapper;
import com.byy.blogprojectbackend.post.mapper.projection.PostFeedRow;
import com.byy.blogprojectbackend.post.service.impl.PostServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Visitor TECH 查询业务测试。
 */
class PostServiceImplTest {

    private final PostMapper postMapper = mock(PostMapper.class);
    private final PostServiceImpl postService = new PostServiceImpl(postMapper);

    @Test
    void getPublicTechPosts_usesFixedTechTypeAndNormalizedFilters() {
        when(postMapper.countPublicFeed("TECH", "redis", "lua"))
                .thenReturn(0L);

        var page = postService.getPublicTechPosts(
                " redis ",
                " lua ",
                1,
                10
        );

        assertTrue(page.items().isEmpty());
        assertEquals(0, page.total());
        verify(postMapper).countPublicFeed("TECH", "redis", "lua");
    }

    @Test
    void getPublicTechPost_whenSlugIsInvisible_throwsNotFound() {
        when(postMapper.selectPublicTechBySlug("missing-tech"))
                .thenReturn(null);

        assertThrows(
                ResourceNotFoundException.class,
                () -> postService.getPublicTechPost("missing-tech")
        );
    }

    @Test
    void getPublicTechPost_returnsFullContentAndRelatedPosts() {
        PostFeedRow row = new PostFeedRow();
        row.setId(1001L);
        row.setType("TECH");
        row.setSlug("redis-lua");
        row.setTitle("Redis + Lua");
        row.setSummary("库存扣减实践");
        row.setContent("# 完整正文");
        row.setContentFormat("MARKDOWN");
        row.setAuthorUserId(1L);
        row.setAuthorName("玢");
        row.setCategoryId(10L);
        row.setCategoryName("Redis");
        row.setCategorySlug("redis");
        row.setCategoryDeleted(false);
        row.setReadingTime(8);
        row.setLikeCount(0);
        row.setCommentCount(0);
        row.setCreatedAt(LocalDateTime.of(2026, 9, 6, 12, 0));
        row.setUpdatedAt(LocalDateTime.of(2026, 9, 6, 12, 0));
        row.setPublishedAt(LocalDateTime.of(2026, 9, 6, 12, 0));

        when(postMapper.selectPublicTechBySlug("redis-lua"))
                .thenReturn(row);
        when(postMapper.selectRelatedPublicTech(1001L, 10L, 3))
                .thenReturn(List.of());
        when(postMapper.selectTagsByPostIds(List.of(1001L)))
                .thenReturn(List.of());
        when(postMapper.selectMediaByPostIds(List.of(1001L)))
                .thenReturn(List.of());

        var detail = postService.getPublicTechPost("redis-lua");

        assertEquals("1001", detail.id());
        assertEquals("# 完整正文", detail.content());
        assertEquals("MARKDOWN", detail.contentFormat());
        assertTrue(detail.relatedPosts().isEmpty());
        assertNull(detail.version());
    }

    @Test
    void getPublicMoments_usesYearAndMonthFilters() {
        when(postMapper.countPublicMoments(2026, 9))
                .thenReturn(0L);

        var page = postService.getPublicMoments(
                2026,
                9,
                1,
                10
        );

        assertTrue(page.items().isEmpty());
        verify(postMapper).countPublicMoments(2026, 9);
    }

    @Test
    void getPublicMoment_whenIdIsInvisible_throwsNotFound() {
        when(postMapper.selectPublicMomentById(2001L))
                .thenReturn(null);

        assertThrows(
                ResourceNotFoundException.class,
                () -> postService.getPublicMoment(2001L)
        );
    }

    @Test
    void getRelatedPublicPosts_whenSourceIsInvisible_throwsNotFound() {
        when(postMapper.selectPublicPostById(3001L)).thenReturn(null);

        assertThrows(
                ResourceNotFoundException.class,
                () -> postService.getRelatedPublicPosts(3001L, 4)
        );
    }
}
