package com.byy.blogprojectbackend.archive.controller;

import com.byy.blogprojectbackend.archive.service.ArchiveService;
import com.byy.blogprojectbackend.archive.vo.ArchiveVO;
import com.byy.blogprojectbackend.common.result.Result;
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
            @Min(value = 1970, message = "year 不能小于1970")
            @Max(value = 9999, message = "year 不能大于9999")
            Integer year,

            @RequestParam(defaultValue = "ALL")
            @Pattern(
                    regexp = "ALL|TECH|MOMENT",
                    message = "type 只能是 ALL、TECH 或 MOMENT"
            )
            String type,

            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "page 不能小于1")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "pageSize 不能小于1")
            @Max(value = 50, message = "pageSize 不能超过50")
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
