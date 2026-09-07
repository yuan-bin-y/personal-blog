package com.byy.blogprojectbackend.comment.controller;

import com.byy.blogprojectbackend.auth.token.JwtTokenService;
import com.byy.blogprojectbackend.comment.dto.CreateCommentDTO;
import com.byy.blogprojectbackend.comment.dto.UpdateCommentDTO;
import com.byy.blogprojectbackend.comment.service.CommentService;
import com.byy.blogprojectbackend.comment.vo.CommentVO;
import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.common.vo.PageVO;
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

/** Post 评论读取与 Owner 管理接口。 */
@Validated
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/api/posts/{postId}/comments")
    public Result<PageVO<CommentVO>> list(
            @PathVariable @Positive Long postId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(commentService.list(
                postId,
                page,
                pageSize,
                currentUserIdOrNull(jwt)
        ));
    }

    /** 登录后的 Visitor 或 Owner 对公开 Post 发表评论。 */
    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<Result<CommentVO>> create(
            @PathVariable @Positive Long postId,
            @Valid @RequestBody CreateCommentDTO dto,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt
    ) {
        CommentVO comment = commentService.create(
                postId,
                dto,
                requiredUserId(jwt)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Result.success(comment));
    }

    /** 登录用户修改自己发布的顶层评论。 */
    @PutMapping("/api/comments/{commentId}")
    public Result<CommentVO> updateOwnComment(
            @PathVariable @Positive Long commentId,
            @Valid @RequestBody UpdateCommentDTO dto,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(commentService.updateOwnComment(
                commentId,
                dto,
                requiredUserId(jwt)
        ));
    }

    @PostMapping("/api/owner/comments/{commentId}/reply")
    public ResponseEntity<Result<CommentVO>> reply(
            @PathVariable @Positive Long commentId,
            @Valid @RequestBody ReplyDTO dto,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt
    ) {
        ReplyResult<CommentVO> result = commentService.reply(
                commentId,
                dto,
                requiredUserId(jwt)
        );

        return ResponseEntity
                .status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(Result.success(result.value()));
    }

    @DeleteMapping("/api/owner/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @Positive Long commentId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt
    ) {
        commentService.delete(commentId, requiredUserId(jwt));
    }

    private Long requiredUserId(Jwt jwt) {
        return Long.valueOf(jwt.getClaimAsString(JwtTokenService.CLAIM_USER_ID));
    }

    private Long currentUserIdOrNull(Jwt jwt) {
        return jwt == null ? null : requiredUserId(jwt);
    }
    /** 登录用户删除自己发布的顶层评论。 */
    @DeleteMapping("/api/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOwnComment(
            @PathVariable @Positive Long commentId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt
    ) {
        commentService.deleteOwnComment(
                commentId,
                requiredUserId(jwt)
        );
    }
}
