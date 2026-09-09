package com.byy.blogprojectbackend.post.dto;

import com.byy.blogprojectbackend.post.enums.PostType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record AutosavePostDTO(
        @NotNull(message = "内容类型不能为空")
        @Pattern(regexp = PostType.VALIDATION_PATTERN, message = PostType.VALIDATION_MESSAGE) String type,
        @NotNull(message = "自动保存内容不能为空") Map<String, Object> payload,
        @NotNull(message = "基础版本号不能为空")
        @Min(value = 0, message = "基础版本号不能小于 0") Integer baseVersion
) {
}
