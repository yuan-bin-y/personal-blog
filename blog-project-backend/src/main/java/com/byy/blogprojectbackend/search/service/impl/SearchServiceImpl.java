package com.byy.blogprojectbackend.search.service.impl;

import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.service.PostService;
import com.byy.blogprojectbackend.post.vo.PostSummaryVO;
import com.byy.blogprojectbackend.search.ai.AiAnswerClient;
import com.byy.blogprojectbackend.search.ai.AiRateLimiter;
import com.byy.blogprojectbackend.search.config.AiProperties;
import com.byy.blogprojectbackend.search.dto.AiSearchDTO;
import com.byy.blogprojectbackend.search.gateway.ElasticsearchGateway;
import com.byy.blogprojectbackend.search.gateway.SearchHit;
import com.byy.blogprojectbackend.search.gateway.SearchPage;
import com.byy.blogprojectbackend.search.gateway.SearchQuery;
import com.byy.blogprojectbackend.search.service.SearchService;
import com.byy.blogprojectbackend.search.vo.AiSearchSourceVO;
import com.byy.blogprojectbackend.search.vo.AiSearchVO;
import com.byy.blogprojectbackend.search.vo.SearchResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** 搜索结果由 Elasticsearch 排序，相关内容仍从 MySQL 读取权威数据。 */
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final ElasticsearchGateway elasticsearchGateway;
    private final PostService postService;
    private final AiAnswerClient aiAnswerClient;
    private final AiRateLimiter aiRateLimiter;
    private final AiProperties aiProperties;

    @Override
    public PageVO<SearchResultVO> search(
            String keyword,
            String type,
            String categorySlug,
            String tagSlug,
            int page,
            int pageSize
    ) {
        SearchPage result = elasticsearchGateway.search(new SearchQuery(
                keyword.trim(),
                type,
                normalize(categorySlug),
                normalize(tagSlug),
                page,
                pageSize
        ));
        List<SearchResultVO> items = result.items().stream()
                .map(this::toSearchResult)
                .toList();
        long totalPages = (result.total() + pageSize - 1) / pageSize;
        return new PageVO<>(items, page, pageSize, result.total(), totalPages, page < totalPages);
    }

    @Override
    public List<PostSummaryVO> related(Long postId, int limit) {
        return postService.getRelatedPublicPosts(postId, limit);
    }

    @Override
    public AiSearchVO ask(AiSearchDTO dto, String clientKey) {
        aiRateLimiter.check(clientKey);
        SearchPage result = elasticsearchGateway.search(new SearchQuery(
                dto.question().trim(),
                "ALL",
                null,
                null,
                1,
                aiProperties.getContextPostLimit()
        ));

        if (result.items().isEmpty()) {
            return new AiSearchVO(
                    "根据当前已发布内容，暂时找不到足够依据回答这个问题。",
                    List.of()
            );
        }

        List<SearchHit> sources = result.items();
        String answer = aiAnswerClient.answer(dto.question(), sources);
        List<AiSearchSourceVO> sourceViews = sources.stream()
                .map(hit -> new AiSearchSourceVO(
                        hit.id(),
                        hit.slug(),
                        hit.title() == null || hit.title().isBlank() ? "说说" : hit.title(),
                        hit.excerpt()
                ))
                .toList();
        return new AiSearchVO(answer, sourceViews);
    }

    private SearchResultVO toSearchResult(SearchHit hit) {
        return new SearchResultVO(
                hit.id(), hit.type(), hit.slug(), hit.title(), hit.excerpt(),
                hit.highlights(), hit.publishedAt()
        );
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
