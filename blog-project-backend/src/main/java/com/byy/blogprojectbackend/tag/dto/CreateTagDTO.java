package com.byy.blogprojectbackend.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagDTO(
        @NotBlank(message = "标签名称不能为空")
        @Size(max = 64, message = "标签名称不能超过 64 个字符") String name,
        @Size(max = 80, message = "标签 slug 不能超过 80 个字符") String slug
) {
}
