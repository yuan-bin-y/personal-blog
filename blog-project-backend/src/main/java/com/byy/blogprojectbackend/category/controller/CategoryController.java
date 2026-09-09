package com.byy.blogprojectbackend.category.controller;

import com.byy.blogprojectbackend.category.dto.CreateCategoryDTO;
import com.byy.blogprojectbackend.category.dto.UpdateCategoryDTO;
import com.byy.blogprojectbackend.category.service.CategoryService;
import com.byy.blogprojectbackend.category.vo.CategoryAdminVO;
import com.byy.blogprojectbackend.common.enums.FilterStatus;
import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.common.vo.PageVO;
import com.byy.blogprojectbackend.post.vo.CategoryVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** Visitor/Owner 分类接口。 */
@Validated
@RestController
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/api/categories")
    public Result<PageVO<CategoryVO>> publicList(@RequestParam(defaultValue = "false") boolean used,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize) {
        return Result.success(categoryService.listPublic(used, page, pageSize));
    }

    @GetMapping("/api/owner/categories")
    public Result<PageVO<CategoryAdminVO>> ownerList(@RequestParam(defaultValue = FilterStatus.DEFAULT_CODE) String status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize) {
        return Result.success(categoryService.listOwner(status, page, pageSize));
    }

    @PostMapping("/api/owner/categories")
    public ResponseEntity<Result<CategoryAdminVO>> create(@Valid @RequestBody CreateCategoryDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(categoryService.create(dto)));
    }

    @PutMapping("/api/owner/categories/{id}")
    public Result<CategoryAdminVO> update(@PathVariable @Positive Long id, @Valid @RequestBody UpdateCategoryDTO dto) {
        return Result.success(categoryService.update(id, dto));
    }

    @DeleteMapping("/api/owner/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id, @RequestParam @Min(0) int version) {
        categoryService.delete(id, version);
    }
}
