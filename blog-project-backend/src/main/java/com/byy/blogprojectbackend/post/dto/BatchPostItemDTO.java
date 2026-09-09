package com.byy.blogprojectbackend.post.dto;

import com.byy.blogprojectbackend.common.validation.ValidationPatterns;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record BatchPostItemDTO(
        @NotBlank(message = "内容 ID 不能为空")
        @Pattern(regexp = ValidationPatterns.POSITIVE_INTEGER, message = "内容 ID 必须是正整数") String postId,
        @NotNull(message = "版本号不能为空")
        @Min(value = 0, message = "版本号不能小于 0") Integer version
) {
}
