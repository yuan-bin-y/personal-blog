package com.byy.blogprojectbackend.guestbook.controller;

import com.byy.blogprojectbackend.auth.token.JwtTokenService;
import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.common.vo.PageVO;
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

    @GetMapping("/api/guestbook")
    public Result<PageVO<GuestbookVO>> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize
    ) {
        return Result.success(guestbookService.list(page, pageSize));
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
                ownerId(jwt)
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
        guestbookService.delete(entryId, ownerId(jwt));
    }

    private Long ownerId(Jwt jwt) {
        return Long.valueOf(jwt.getClaimAsString(JwtTokenService.CLAIM_USER_ID));
    }
}
