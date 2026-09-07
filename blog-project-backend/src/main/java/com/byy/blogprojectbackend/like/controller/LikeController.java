package com.byy.blogprojectbackend.like.controller;

import com.byy.blogprojectbackend.auth.token.JwtTokenService;
import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.like.service.LikeService;
import com.byy.blogprojectbackend.like.vo.LikeStateVO;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /** 登录用户幂等点赞一个公开 Post。 */
    @PutMapping("/api/posts/{postId}/like")
    public Result<LikeStateVO> like(
            @PathVariable @Positive Long postId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(
                likeService.like(
                        postId,
                        requiredUserId(jwt)
                )
        );
    }

    private Long requiredUserId(Jwt jwt) {
        return Long.valueOf(
                jwt.getClaimAsString(
                        JwtTokenService.CLAIM_USER_ID
                )
        );
    }
}