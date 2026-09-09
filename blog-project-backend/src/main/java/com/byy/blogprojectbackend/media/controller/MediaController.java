package com.byy.blogprojectbackend.media.controller;

import com.byy.blogprojectbackend.auth.token.JwtTokenService;
import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.media.enums.MediaUsageType;
import com.byy.blogprojectbackend.media.service.MediaService;
import com.byy.blogprojectbackend.media.vo.MediaAssetVO;
import com.byy.blogprojectbackend.common.ratelimit.RateLimitProperties;
import com.byy.blogprojectbackend.common.ratelimit.RedisFixedWindowRateLimiter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final RedisFixedWindowRateLimiter rateLimiter;
    private final RateLimitProperties rateLimitProperties;

    @PostMapping(
            value = "/api/owner/media",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Result<MediaAssetVO>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam MediaUsageType usageType,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = requiredUserId(jwt);
        rateLimiter.check(
                "owner:media-upload",
                userId.toString(),
                rateLimitProperties.getMediaUploadPerMinute(),
                java.time.Duration.ofMinutes(1),
                "媒体上传过于频繁，请稍后再试"
        );
        MediaAssetVO media = mediaService.upload(
                file,
                usageType,
                userId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(media));
    }

    @GetMapping("/api/owner/media")
    public Result<PageVO<MediaAssetVO>> list(
            @RequestParam(required = false) com.byy.blogprojectbackend.media.enums.MediaType mediaType,
            @RequestParam(required = false) MediaUsageType usageType,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize
    ) {
        return Result.success(mediaService.list(mediaType, usageType, page, pageSize));
    }

    @DeleteMapping("/api/owner/media/{mediaId}")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @Positive Long mediaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        mediaService.delete(mediaId, requiredUserId(jwt));
    }

    private Long requiredUserId(Jwt jwt) {
        return Long.valueOf(jwt.getClaimAsString(JwtTokenService.CLAIM_USER_ID));
    }
}
