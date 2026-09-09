package com.byy.blogprojectbackend.post.dto;

import com.byy.blogprojectbackend.common.validation.ValidationPatterns;
import com.byy.blogprojectbackend.post.enums.PostContentFormat;
import com.byy.blogprojectbackend.post.enums.PostStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record CreateTechPostDTO(
        @NotBlank(message = "文章标题不能为空")
        @Size(max = 180, message = "文章标题不能超过 180 个字符") String title,

        @NotBlank(message = "文章摘要不能为空")
        @Size(max = 600, message = "文章摘要不能超过 600 个字符") String summary,

        @NotBlank(message = "文章正文不能为空") String content,

        @NotNull(message = "正文格式不能为空")
        @Pattern(regexp = PostContentFormat.VALIDATION_PATTERN, message = PostContentFormat.VALIDATION_MESSAGE)
        String contentFormat,

        @NotBlank(message = "分类 ID 不能为空")
        @Pattern(regexp = ValidationPatterns.POSITIVE_INTEGER, message = "分类 ID 必须是正整数")
        String categoryId,

        @NotNull(message = "标签 ID 列表不能为空")
        @Size(max = 10, message = "一篇文章最多选择 10 个标签")
        List<@Pattern(regexp = ValidationPatterns.POSITIVE_INTEGER, message = "标签 ID 必须是正整数") String> tagIds,

        @Valid MediaInputDTO cover,

        @NotNull(message = "发布状态不能为空")
        @Pattern(regexp = PostStatus.EDITABLE_PATTERN, message = PostStatus.EDITABLE_MESSAGE)
        String status
) {
}
