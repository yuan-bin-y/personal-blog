package com.byy.blogprojectbackend.like.controller;

import com.byy.blogprojectbackend.auth.token.JwtTokenService;
import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.like.service.LikeService;
import com.byy.blogprojectbackend.like.vo.LikeStateVO;
import com.byy.blogprojectbackend.post.vo.PostSummaryVO;
import com.byy.blogprojectbackend.common.ratelimit.RateLimitProperties;
import com.byy.blogprojectbackend.common.ratelimit.RedisFixedWindowRateLimiter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    private final RedisFixedWindowRateLimiter rateLimiter;
    private final RateLimitProperties rateLimitProperties;

    /** 登录用户幂等点赞一个公开 Post。 */
    @PutMapping("/api/posts/{postId}/like")
    public Result<LikeStateVO> like(
            @PathVariable @Positive Long postId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = requiredUserId(jwt);
        checkLikeRate(userId);
        return Result.success(
                likeService.like(
                        postId,
                        userId
                )
        );
    }

    /** 登录用户幂等取消对公开 Post 的点赞。 */
    @DeleteMapping("/api/posts/{postId}/like")
    public Result<LikeStateVO> unlike(
            @PathVariable @Positive Long postId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = requiredUserId(jwt);
        checkLikeRate(userId);
        return Result.success(
                likeService.unlike(postId, userId)
        );
    }

    /** 按点赞时间倒序查询当前用户点赞过的公开 Post。 */
    @GetMapping("/api/me/likes")
    public Result<PageVO<PostSummaryVO>> listMyLikes(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(
                likeService.listMyLikes(requiredUserId(jwt), page, pageSize)
        );
    }

    private Long requiredUserId(Jwt jwt) {
        return Long.valueOf(
                jwt.getClaimAsString(
                        JwtTokenService.CLAIM_USER_ID
                )
        );
    }

    private void checkLikeRate(Long userId) {
        rateLimiter.check(
                "interaction:like",
                userId.toString(),
                rateLimitProperties.getLikePerMinute(),
                java.time.Duration.ofMinutes(1),
                "点赞操作过于频繁，请稍后再试"
        );
    }
}
