package com.byy.blogprojectbackend.guestbook.controller;

import com.byy.blogprojectbackend.auth.token.JwtTokenService;
import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.common.ratelimit.RateLimitProperties;
import com.byy.blogprojectbackend.common.ratelimit.RedisFixedWindowRateLimiter;
import com.byy.blogprojectbackend.guestbook.dto.CreateGuestbookDTO;
import com.byy.blogprojectbackend.guestbook.dto.UpdateGuestbookDTO;
import com.byy.blogprojectbackend.guestbook.service.GuestbookService;
import com.byy.blogprojectbackend.guestbook.vo.GuestbookVO;
import com.byy.blogprojectbackend.interaction.dto.ReplyDTO;
import com.byy.blogprojectbackend.interaction.service.ReplyResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** 空间留言读取与 Owner 管理接口；与 Post Comment 保持独立。 */
@Validated
@RestController
@RequiredArgsConstructor
public class GuestbookController {

    private final GuestbookService guestbookService;
    private final RedisFixedWindowRateLimiter rateLimiter;
    private final RateLimitProperties rateLimitProperties;

    @GetMapping("/api/guestbook")
    public Result<PageVO<GuestbookVO>> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(guestbookService.list(page, pageSize, currentUserIdOrNull(jwt)));
    }

    /** 登录后的 Visitor 或 Owner 发布一条顶层空间留言。 */
    @PostMapping("/api/guestbook")
    public ResponseEntity<Result<GuestbookVO>> create(
            @Valid @RequestBody CreateGuestbookDTO dto,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = requiredUserId(jwt);
        rateLimiter.check(
                "interaction:guestbook",
                userId.toString(),
                rateLimitProperties.getInteractionPerMinute(),
                java.time.Duration.ofMinutes(1),
                "留言发布过于频繁，请稍后再试"
        );
        GuestbookVO entry = guestbookService.create(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(entry));
    }

    /** 登录用户修改自己发布的顶层留言。 */
    @PutMapping("/api/guestbook/{entryId}")
    public Result<GuestbookVO> updateOwnEntry(
            @PathVariable @Positive Long entryId,
            @Valid @RequestBody UpdateGuestbookDTO dto,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(guestbookService.updateOwnEntry(entryId, dto, requiredUserId(jwt)));
    }

    /** 登录用户软删除自己发布的顶层留言及其 Owner 回复。 */
    @DeleteMapping("/api/guestbook/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOwnEntry(
            @PathVariable @Positive Long entryId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt
    ) {
        guestbookService.deleteOwnEntry(entryId, requiredUserId(jwt));
    }

    @PostMapping("/api/owner/guestbook/{entryId}/reply")
    public ResponseEntity<Result<GuestbookVO>> reply(
            @PathVariable @Positive Long entryId,
            @Valid @RequestBody ReplyDTO dto,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt
    ) {
        ReplyResult<GuestbookVO> result = guestbookService.reply(
                entryId,
                dto,
                requiredUserId(jwt)
        );

        return ResponseEntity
                .status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(Result.success(result.value()));
    }

    @DeleteMapping("/api/owner/guestbook/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @Positive Long entryId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt
    ) {
        guestbookService.delete(entryId, requiredUserId(jwt));
    }

    private Long requiredUserId(Jwt jwt) {
        return Long.valueOf(jwt.getClaimAsString(JwtTokenService.CLAIM_USER_ID));
    }

    private Long currentUserIdOrNull(Jwt jwt) {
        return jwt == null ? null : requiredUserId(jwt);
    }
}
