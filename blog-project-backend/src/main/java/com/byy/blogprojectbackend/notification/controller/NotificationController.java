package com.byy.blogprojectbackend.notification.controller;

import com.byy.blogprojectbackend.auth.token.JwtTokenService;
import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.notification.service.NotificationService;
import com.byy.blogprojectbackend.notification.vo.NotificationVO;
import com.byy.blogprojectbackend.notification.vo.UnreadCountVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/api/notifications")
    public Result<PageVO<NotificationVO>> list(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(notificationService.list(
                requiredUserId(jwt),
                unreadOnly,
                page,
                pageSize
        ));
    }

    @PutMapping("/api/notifications/{notificationId}/read")
    public Result<NotificationVO> markRead(
            @PathVariable @Positive Long notificationId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(notificationService.markRead(
                notificationId,
                requiredUserId(jwt)
        ));
    }

    @PutMapping("/api/notifications/read-all")
    public Result<UnreadCountVO> markAllRead(@AuthenticationPrincipal Jwt jwt) {
        return Result.success(notificationService.markAllRead(requiredUserId(jwt)));
    }

    private Long requiredUserId(Jwt jwt) {
        return Long.valueOf(jwt.getClaimAsString(JwtTokenService.CLAIM_USER_ID));
    }
}
