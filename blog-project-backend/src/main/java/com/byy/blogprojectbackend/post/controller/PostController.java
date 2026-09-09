package com.byy.blogprojectbackend.post.controller;

import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.service.PostService;
import com.byy.blogprojectbackend.post.enums.PostTypeFilter;
import com.byy.blogprojectbackend.post.vo.MomentDetailVO;
import com.byy.blogprojectbackend.post.vo.PostSummaryVO;
import com.byy.blogprojectbackend.post.vo.TechDetailVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Visitor 公开内容接口。
 */
@Validated
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 获取首页 TECH/MOMENT 混合 Feed。
     */
    @GetMapping
    public Result<PageVO<PostSummaryVO>> getPublicFeed(
            @RequestParam(defaultValue = PostTypeFilter.DEFAULT_CODE)
            @Pattern(
                    regexp = PostTypeFilter.VALIDATION_PATTERN,
                    message = PostTypeFilter.VALIDATION_MESSAGE
            )
            String type,

            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "页码不能小于 1")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "每页条数不能小于 1")
            @Max(value = 50, message = "每页条数不能超过 50")
            int pageSize
    ) {
        return Result.success(
                postService.getPublicFeed(type, page, pageSize)
        );
    }
    /**
     * 获取公开 TECH 列表。
     */
    @GetMapping("/tech")
    public Result<PageVO<PostSummaryVO>> getPublicTechPosts(
            @RequestParam(required = false)
            @Size(
                    max = 80,
                    message = "分类别名不能超过 80 个字符"
            )
            String category,

            @RequestParam(required = false)
            @Size(
                    max = 80,
                    message = "标签别名不能超过 80 个字符"
            )
            String tag,

            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "页码不能小于 1")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "每页条数不能小于 1")
            @Max(value = 50, message = "每页条数不能超过 50")
            int pageSize
    ) {
        return Result.success(
                postService.getPublicTechPosts(
                        category,
                        tag,
                        page,
                        pageSize
                )
        );
    }

    /**
     * 根据 slug 获取公开 TECH 详情。
     */
    @GetMapping("/tech/{slug}")
    public Result<TechDetailVO> getPublicTechPost(
            @PathVariable
            @Size(min = 1, max = 180, message = "文章别名长度必须在 1 到 180 个字符之间")
            String slug
    ) {
        return Result.success(
                postService.getPublicTechPost(slug)
        );
    }

    /**
     * 获取公开 MOMENT 列表，支持年月筛选。
     */
    @GetMapping("/moments")
    public Result<PageVO<PostSummaryVO>> getPublicMoments(
            @RequestParam(required = false)
            @Min(value = 1970, message = "年份不能小于 1970")
            @Max(value = 9999, message = "年份不能大于 9999")
            Integer year,

            @RequestParam(required = false)
            @Min(value = 1, message = "月份不能小于 1")
            @Max(value = 12, message = "月份不能大于 12")
            Integer month,

            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "页码不能小于 1")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "每页条数不能小于 1")
            @Max(value = 50, message = "每页条数不能超过 50")
            int pageSize
    ) {
        return Result.success(
                postService.getPublicMoments(
                        year,
                        month,
                        page,
                        pageSize
                )
        );
    }

    /**
     * 根据 Post ID 获取公开 MOMENT 详情。
     */
    @GetMapping("/moments/{id}")
    public Result<MomentDetailVO> getPublicMoment(
            @PathVariable
            @Positive(message = "内容 ID 必须是正整数")
            Long id
    ) {
        return Result.success(
                postService.getPublicMoment(id)
        );
    }
}
