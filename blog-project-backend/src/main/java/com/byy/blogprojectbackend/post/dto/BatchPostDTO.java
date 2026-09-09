package com.byy.blogprojectbackend.post.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchPostDTO(
        @NotNull(message = "批量操作内容不能为空")
        @Size(min = 1, max = 100, message = "批量操作数量必须在 1 到 100 条之间")
        List<@Valid BatchPostItemDTO> items
) {
}
