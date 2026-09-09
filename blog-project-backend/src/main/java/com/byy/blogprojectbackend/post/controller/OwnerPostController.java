package com.byy.blogprojectbackend.post.controller;

import com.byy.blogprojectbackend.auth.token.JwtTokenService;
import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.dto.CreateMomentDTO;
import com.byy.blogprojectbackend.post.dto.CreateTechPostDTO;
import com.byy.blogprojectbackend.post.dto.UpdateMomentDTO;
import com.byy.blogprojectbackend.post.dto.UpdateTechPostDTO;
import com.byy.blogprojectbackend.post.enums.PostStatusFilter;
import com.byy.blogprojectbackend.post.enums.PostTypeFilter;
import com.byy.blogprojectbackend.post.service.OwnerPostService;
import com.byy.blogprojectbackend.post.vo.MomentDetailVO;
import com.byy.blogprojectbackend.post.vo.PostSummaryVO;
import com.byy.blogprojectbackend.post.vo.TechDetailVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** Owner TECH/MOMENT 内容管理接口。 */
@Validated
@RestController
@RequestMapping("/api/owner/posts")
@RequiredArgsConstructor
public class OwnerPostController {

    private final OwnerPostService ownerPostService;

    @GetMapping
    public Result<PageVO<PostSummaryVO>> list(
            @RequestParam(defaultValue = PostTypeFilter.DEFAULT_CODE)
            @Pattern(regexp = PostTypeFilter.VALIDATION_PATTERN, message = PostTypeFilter.VALIDATION_MESSAGE) String type,
            @RequestParam(defaultValue = PostStatusFilter.DEFAULT_CODE)
            @Pattern(regexp = PostStatusFilter.VALIDATION_PATTERN, message = PostStatusFilter.VALIDATION_MESSAGE) String status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize
    ) {
        return Result.success(ownerPostService.list(type, status, page, pageSize));
    }

    @PostMapping("/tech")
    public ResponseEntity<Result<TechDetailVO>> createTech(
            @Valid @RequestBody CreateTechPostDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        TechDetailVO created = ownerPostService.createTech(dto, ownerId(jwt));
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(created));
    }

    @GetMapping("/tech/{id}")
    public Result<TechDetailVO> getTech(@PathVariable @Positive Long id) {
        return Result.success(ownerPostService.getTech(id));
    }

    @PutMapping("/tech/{id}")
    public Result<TechDetailVO> updateTech(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateTechPostDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(ownerPostService.updateTech(id, dto, ownerId(jwt)));
    }

    @DeleteMapping("/tech/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTech(
            @PathVariable @Positive Long id,
            @RequestParam @Min(0) int version,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ownerPostService.deleteTech(id, version, ownerId(jwt));
    }

    @PostMapping("/moments")
    public ResponseEntity<Result<MomentDetailVO>> createMoment(
            @Valid @RequestBody CreateMomentDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        MomentDetailVO created = ownerPostService.createMoment(dto, ownerId(jwt));
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(created));
    }

    @GetMapping("/moments/{id}")
    public Result<MomentDetailVO> getMoment(@PathVariable @Positive Long id) {
        return Result.success(ownerPostService.getMoment(id));
    }

    @PutMapping("/moments/{id}")
    public Result<MomentDetailVO> updateMoment(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateMomentDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Result.success(ownerPostService.updateMoment(id, dto, ownerId(jwt)));
    }

    @DeleteMapping("/moments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMoment(
            @PathVariable @Positive Long id,
            @RequestParam @Min(0) int version,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ownerPostService.deleteMoment(id, version, ownerId(jwt));
    }

    private Long ownerId(Jwt jwt) {
        return Long.valueOf(jwt.getClaimAsString(JwtTokenService.CLAIM_USER_ID));
    }
}
