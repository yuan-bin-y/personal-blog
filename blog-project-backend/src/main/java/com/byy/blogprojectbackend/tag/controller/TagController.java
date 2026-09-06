package com.byy.blogprojectbackend.tag.controller;

import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.vo.TagVO;
import com.byy.blogprojectbackend.tag.dto.CreateTagDTO;
import com.byy.blogprojectbackend.tag.dto.UpdateTagDTO;
import com.byy.blogprojectbackend.tag.service.TagService;
import com.byy.blogprojectbackend.tag.vo.TagAdminVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** Visitor 标签查询与 Owner 标签管理接口。 */
@Validated
@RestController
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/api/tags")
    public Result<PageVO<TagVO>> publicList(
            @RequestParam(defaultValue = "false") boolean used,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize
    ) {
        return Result.success(tagService.listPublic(used, page, pageSize));
    }

    @GetMapping("/api/owner/tags")
    public Result<PageVO<TagAdminVO>> ownerList(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize
    ) {
        return Result.success(tagService.listOwner(status, page, pageSize));
    }

    @PostMapping("/api/owner/tags")
    public ResponseEntity<Result<TagAdminVO>> create(
            @Valid @RequestBody CreateTagDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(tagService.create(dto)));
    }

    @PutMapping("/api/owner/tags/{id}")
    public Result<TagAdminVO> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateTagDTO dto
    ) {
        return Result.success(tagService.update(id, dto));
    }

    @DeleteMapping("/api/owner/tags/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        tagService.delete(id);
    }
}
