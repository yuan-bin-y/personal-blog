package com.byy.blogprojectbackend.search.controller;

import com.byy.blogprojectbackend.auth.token.JwtTokenService;
import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.search.service.SearchReindexService;
import com.byy.blogprojectbackend.search.vo.ReindexTaskVO;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** Owner 全量搜索索引重建入口。 */
@Validated
@RestController
@RequiredArgsConstructor
public class OwnerSearchController {
    private final SearchReindexService reindexService;

    @PostMapping("/api/owner/search/reindex")
    public ResponseEntity<Result<ReindexTaskVO>> create(@AuthenticationPrincipal Jwt jwt) {
        ReindexTaskVO task = reindexService.create(
                Long.valueOf(jwt.getClaimAsString(JwtTokenService.CLAIM_USER_ID))
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Result.success(task));
    }

    @GetMapping("/api/owner/search/reindex/{taskId}")
    public Result<ReindexTaskVO> get(
            @PathVariable
            @Pattern(regexp = "[a-f0-9]{32}", message = "taskId 格式不正确")
            String taskId
    ) {
        return Result.success(reindexService.get(taskId));
    }
}
