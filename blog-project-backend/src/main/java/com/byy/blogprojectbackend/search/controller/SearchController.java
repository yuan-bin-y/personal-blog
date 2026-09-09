package com.byy.blogprojectbackend.search.controller;

import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.vo.PostSummaryVO;
import com.byy.blogprojectbackend.post.enums.PostTypeFilter;
import com.byy.blogprojectbackend.search.dto.AiSearchDTO;
import com.byy.blogprojectbackend.search.service.SearchService;
import com.byy.blogprojectbackend.search.vo.AiSearchVO;
import com.byy.blogprojectbackend.search.vo.SearchResultVO;
import com.byy.blogprojectbackend.common.ratelimit.RateLimitProperties;
import com.byy.blogprojectbackend.common.ratelimit.RedisFixedWindowRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Visitor 公开搜索、相关内容与 AI 问答。 */
@Validated
@RestController
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;
    private final RedisFixedWindowRateLimiter rateLimiter;
    private final RateLimitProperties rateLimitProperties;

    @GetMapping("/api/search")
    public Result<PageVO<SearchResultVO>> search(
            @RequestParam @NotBlank @Size(max = 100) String q,
            @RequestParam(defaultValue = PostTypeFilter.DEFAULT_CODE)
            @Pattern(regexp = PostTypeFilter.VALIDATION_PATTERN, message = PostTypeFilter.VALIDATION_MESSAGE) String type,
            @RequestParam(required = false) @Size(max = 80) String category,
            @RequestParam(required = false) @Size(max = 80) String tag,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize
            , HttpServletRequest request
    ) {
        rateLimiter.check(
                "search:public",
                request.getRemoteAddr(),
                rateLimitProperties.getSearchPerMinute(),
                java.time.Duration.ofMinutes(1),
                "搜索请求过于频繁，请稍后再试"
        );
        return Result.success(searchService.search(q, type, category, tag, page, pageSize));
    }

    @GetMapping("/api/posts/{postId}/related")
    public Result<List<PostSummaryVO>> related(
            @PathVariable @Positive Long postId,
            @RequestParam(defaultValue = "4") @Min(1) @Max(10) int limit
    ) {
        return Result.success(searchService.related(postId, limit));
    }

    @PostMapping("/api/search/ai")
    public Result<AiSearchVO> ask(
            @Valid @RequestBody AiSearchDTO dto,
            HttpServletRequest request
    ) {
        // 默认只信任 Servlet 容器看到的对端地址，避免伪造 X-Forwarded-For 绕过限流。
        return Result.success(searchService.ask(dto, request.getRemoteAddr()));
    }
}
