package com.byy.blogprojectbackend.post.controller;

import com.byy.blogprojectbackend.auth.token.JwtTokenService;
import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.dto.*;
import com.byy.blogprojectbackend.post.enums.PostTypeFilter;
import com.byy.blogprojectbackend.post.service.OwnerAdvancedPostService;
import com.byy.blogprojectbackend.post.vo.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Owner 内容高级能力：定时、自动保存、版本、回收站、批量和导入。 */
@Validated
@RestController
@RequestMapping("/api/owner/posts")
@RequiredArgsConstructor
public class OwnerAdvancedPostController {
    private final OwnerAdvancedPostService service;

    @PutMapping("/{postId}/schedule")
    public Result<PostSummaryVO> schedule(
            @PathVariable @Positive Long postId,
            @Valid @RequestBody SchedulePostDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(service.schedule(postId, dto, ownerId(jwt)));
    }

    @DeleteMapping("/{postId}/schedule")
    public Result<PostSummaryVO> cancelSchedule(
            @PathVariable @Positive Long postId,
            @RequestParam @Min(0) int version,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(service.cancelSchedule(postId, version, ownerId(jwt)));
    }

    @PutMapping("/{postId}/autosave")
    public Result<AutosaveVO> autosave(
            @PathVariable @Positive Long postId,
            @Valid @RequestBody AutosavePostDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(service.autosave(postId, dto, ownerId(jwt)));
    }

    @GetMapping("/{postId}/versions")
    public Result<PageVO<PostVersionVO>> versions(
            @PathVariable @Positive Long postId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize
    ) {
        return Result.success(service.versions(postId, page, pageSize));
    }

    @GetMapping("/{postId}/versions/{versionId}")
    public Result<PostVersionDetailVO> version(
            @PathVariable @Positive Long postId,
            @PathVariable @Positive Long versionId
    ) {
        return Result.success(service.version(postId, versionId));
    }

    @PostMapping("/{postId}/versions/{versionId}/restore")
    public Result<PostSummaryVO> restoreVersion(
            @PathVariable @Positive Long postId,
            @PathVariable @Positive Long versionId,
            @Valid @RequestBody RestoreVersionDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(service.restoreVersion(postId, versionId, dto, ownerId(jwt)));
    }

    @GetMapping("/trash")
    public Result<PageVO<PostSummaryVO>> trash(
            @RequestParam(defaultValue = PostTypeFilter.DEFAULT_CODE)
            @Pattern(regexp = PostTypeFilter.VALIDATION_PATTERN, message = PostTypeFilter.VALIDATION_MESSAGE) String type,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize
    ) {
        return Result.success(service.trash(type, page, pageSize));
    }

    @PostMapping("/{postId}/restore")
    public Result<PostSummaryVO> restoreTrash(
            @PathVariable @Positive Long postId,
            @RequestParam @Min(0) int version,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(service.restoreTrash(postId, version, ownerId(jwt)));
    }

    @PostMapping("/batch/publish")
    public Result<BatchPostResultVO> batchPublish(
            @Valid @RequestBody BatchPostDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(service.batchPublish(dto, ownerId(jwt)));
    }

    @PostMapping("/batch/delete")
    public Result<BatchPostResultVO> batchDelete(
            @Valid @RequestBody BatchPostDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(service.batchDelete(dto, ownerId(jwt)));
    }

    @PutMapping("/tech/{id}/slug")
    public Result<TechDetailVO> updateSlug(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateSlugDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(service.updateSlug(id, dto, ownerId(jwt)));
    }

    @PostMapping(value = "/import/markdown", consumes = "multipart/form-data")
    public Result<MarkdownImportVO> importMarkdown(@RequestPart("file") MultipartFile file) {
        return Result.success(service.importMarkdown(file));
    }

    private Long ownerId(Jwt jwt) {
        return Long.valueOf(jwt.getClaimAsString(JwtTokenService.CLAIM_USER_ID));
    }
}
