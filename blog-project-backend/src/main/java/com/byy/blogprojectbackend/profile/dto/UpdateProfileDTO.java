package com.byy.blogprojectbackend.profile.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * 更新 Owner 个人资料请求。
 */
public record UpdateProfileDTO(

        @NotBlank
        @Size(max = 64)
        String name,

        @Size(max = 1024)
        String avatar,

        @Size(max = 128)
        String role,

        @Size(max = 500)
        String bio,

        @NotNull
        @Valid
        UpdateProfileStatusDTO status,

        @NotNull
        @PositiveOrZero
        Integer version
) {
}
