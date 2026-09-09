package com.byy.blogprojectbackend.post.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RestoreVersionDTO(
        @NotNull(message = "版本号不能为空")
        @Min(value = 0, message = "版本号不能小于 0") Integer version
) {
}
