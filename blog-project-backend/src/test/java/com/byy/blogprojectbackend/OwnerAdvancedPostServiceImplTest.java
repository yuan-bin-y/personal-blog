package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.exception.VersionConflictException;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.post.dto.AutosavePostDTO;
import com.byy.blogprojectbackend.post.dto.BatchPostDTO;
import com.byy.blogprojectbackend.post.dto.BatchPostItemDTO;
import com.byy.blogprojectbackend.post.entity.Post;
import com.byy.blogprojectbackend.post.mapper.PostAdvancedMapper;
import com.byy.blogprojectbackend.post.mapper.PostMapper;
import com.byy.blogprojectbackend.post.service.OwnerPostService;
import com.byy.blogprojectbackend.post.service.PostVersionSnapshotService;
import com.byy.blogprojectbackend.post.service.impl.OwnerAdvancedPostItemService;
import com.byy.blogprojectbackend.post.service.impl.OwnerAdvancedPostServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OwnerAdvancedPostServiceImplTest {
    private final PostMapper postMapper = mock(PostMapper.class);
    private final PostAdvancedMapper advancedMapper = mock(PostAdvancedMapper.class);
    private final OwnerPostService ownerPostService = mock(OwnerPostService.class);
    private final OwnerAdvancedPostItemService itemService = mock(OwnerAdvancedPostItemService.class);
    private final PostVersionSnapshotService snapshotService = mock(PostVersionSnapshotService.class);
    private final IdGenerator idGenerator = mock(IdGenerator.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final OwnerAdvancedPostServiceImpl service = new OwnerAdvancedPostServiceImpl(
            postMapper, advancedMapper, ownerPostService, itemService, snapshotService,
            idGenerator, objectMapper, publisher
    );

    @Test
    void autosave_rejectsStaleBaseVersionBeforeWriting() {
        Post post = post(10L, "TECH", 4, false);
        when(postMapper.selectById(10L)).thenReturn(post);

        assertThrows(VersionConflictException.class, () -> service.autosave(
                10L, new AutosavePostDTO("TECH", Map.of("title", "draft"), 3), 1L
        ));
        verifyNoInteractions(advancedMapper);
    }

    @Test
    void batchDelete_keepsOtherItemsWhenOneFails() {
        doThrow(new ResourceNotFoundException("内容不存在"))
                .when(itemService).deleteOne(20L, 1, 1L);

        var result = service.batchDelete(new BatchPostDTO(List.of(
                new BatchPostItemDTO("10", 1), new BatchPostItemDTO("20", 1)
        )), 1L);

        assertEquals(List.of("10"), result.succeededIds());
        assertEquals("RESOURCE_NOT_FOUND", result.failed().get(0).code());
        verify(itemService).deleteOne(10L, 1, 1L);
        verify(itemService).deleteOne(20L, 1, 1L);
    }

    @Test
    void importMarkdown_parsesFrontMatterAndDoesNotWriteDatabase() {
        String markdown = "---\ntitle: Redis 入门\ntags: [Redis, Java]\n---\n# ignored\n\n正文";
        var file = new MockMultipartFile(
                "file", "redis.md", "text/markdown", markdown.getBytes(StandardCharsets.UTF_8)
        );

        var result = service.importMarkdown(file);

        assertEquals("Redis 入门", result.title());
        assertEquals(List.of("Redis", "Java"), result.frontMatter().get("tags"));
        assertTrue(result.content().startsWith("# ignored"));
        verifyNoInteractions(postMapper, advancedMapper);
    }

    @Test
    void batch_rejectsDuplicatePostIds() {
        BatchPostDTO request = new BatchPostDTO(List.of(
                new BatchPostItemDTO("10", 1), new BatchPostItemDTO("10", 2)
        ));
        assertThrows(IllegalArgumentException.class, () -> service.batchPublish(request, 1L));
        verifyNoInteractions(itemService);
    }

    private Post post(Long id, String type, int version, boolean deleted) {
        Post post = new Post();
        post.setId(id);
        post.setType(type);
        post.setVersion(version);
        post.setDeleted(deleted);
        return post;
    }
}
