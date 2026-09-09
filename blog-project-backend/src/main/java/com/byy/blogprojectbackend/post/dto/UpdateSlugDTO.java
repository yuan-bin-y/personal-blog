package com.byy.blogprojectbackend.post.dto;

import com.byy.blogprojectbackend.common.validation.ValidationPatterns;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateSlugDTO(
        @NotBlank(message = "文章别名不能为空")
        @Size(max = 180, message = "文章别名不能超过 180 个字符")
        @Pattern(regexp = ValidationPatterns.SLUG, message = "文章别名只能包含小写字母、数字和单个连字符")
        String slug,
        @NotNull(message = "版本号不能为空")
        @Min(value = 0, message = "版本号不能小于 0") Integer version
) {
}
