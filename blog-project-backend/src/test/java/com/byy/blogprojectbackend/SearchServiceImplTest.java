package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.post.service.PostService;
import com.byy.blogprojectbackend.search.ai.AiAnswerClient;
import com.byy.blogprojectbackend.search.ai.AiRateLimiter;
import com.byy.blogprojectbackend.search.config.AiProperties;
import com.byy.blogprojectbackend.search.dto.AiSearchDTO;
import com.byy.blogprojectbackend.search.gateway.ElasticsearchGateway;
import com.byy.blogprojectbackend.search.gateway.SearchHit;
import com.byy.blogprojectbackend.search.gateway.SearchPage;
import com.byy.blogprojectbackend.search.gateway.SearchQuery;
import com.byy.blogprojectbackend.search.service.impl.SearchServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchServiceImplTest {
    private final ElasticsearchGateway gateway = mock(ElasticsearchGateway.class);
    private final PostService postService = mock(PostService.class);
    private final AiAnswerClient aiClient = mock(AiAnswerClient.class);
    private final AiRateLimiter rateLimiter = mock(AiRateLimiter.class);
    private final AiProperties properties = new AiProperties();
    private final SearchServiceImpl service = new SearchServiceImpl(
            gateway, postService, aiClient, rateLimiter, properties
    );

    @Test
    void search_mapsElasticsearchPageWithoutExposingContext() {
        SearchHit hit = hit("1", "Redis 实践");
        when(gateway.search(any(SearchQuery.class))).thenReturn(new SearchPage(List.of(hit), 11));

        var page = service.search(" Redis ", "TECH", " java ", null, 1, 10);

        assertEquals(11, page.total());
        assertEquals(2, page.totalPages());
        assertEquals("1", page.items().get(0).id());
        assertEquals("Redis 实践", page.items().get(0).title());
    }

    @Test
    void ask_withoutSearchEvidence_doesNotCallAiProvider() {
        when(gateway.search(any(SearchQuery.class))).thenReturn(new SearchPage(List.of(), 0));

        var answer = service.ask(new AiSearchDTO("项目如何使用 Redis？"), "127.0.0.1");

        assertTrue(answer.sources().isEmpty());
        verify(rateLimiter).check("127.0.0.1");
        verify(aiClient, never()).answer(any(), any());
    }

    @Test
    void ask_returnsGeneratedAnswerAndExplicitSources() {
        SearchHit hit = hit("1", "Redis 实践");
        when(gateway.search(any(SearchQuery.class))).thenReturn(new SearchPage(List.of(hit), 1));
        when(aiClient.answer(any(), any())).thenReturn("项目使用 Redis 保存 Token Session。[1]");

        var answer = service.ask(new AiSearchDTO("Redis 用来做什么？"), "127.0.0.1");

        assertEquals("项目使用 Redis 保存 Token Session。[1]", answer.answer());
        assertEquals("1", answer.sources().get(0).postId());
        assertEquals("Redis 实践", answer.sources().get(0).title());
    }

    private SearchHit hit(String id, String title) {
        return new SearchHit(
                id, "TECH", "redis", title, "摘要", "完整检索上下文",
                List.of("<em>Redis</em>"), Instant.parse("2026-09-08T00:00:00Z")
        );
    }
}
