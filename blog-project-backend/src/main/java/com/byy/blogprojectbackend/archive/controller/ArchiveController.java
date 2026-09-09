package com.byy.blogprojectbackend.archive.controller;

import com.byy.blogprojectbackend.archive.service.ArchiveService;
import com.byy.blogprojectbackend.archive.vo.ArchiveVO;
import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.post.enums.PostTypeFilter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Visitor 公开归档接口。
 */
@Validated
@RestController
@RequestMapping("/api/archive")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;

    @GetMapping
    public Result<ArchiveVO> getPublicArchive(
            @RequestParam(required = false)
            @Min(value = 1970, message = "年份不能小于 1970")
            @Max(value = 9999, message = "年份不能大于 9999")
            Integer year,

            @RequestParam(defaultValue = PostTypeFilter.DEFAULT_CODE)
            @Pattern(
                    regexp = PostTypeFilter.VALIDATION_PATTERN,
                    message = PostTypeFilter.VALIDATION_MESSAGE
            )
            String type,

            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "页码不能小于 1")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "每页条数不能小于 1")
            @Max(value = 50, message = "每页条数不能超过 50")
            int pageSize
    ) {
        return Result.success(
                archiveService.getPublicArchive(
                        year,
                        type,
                        page,
                        pageSize
                )
        );
    }
}
