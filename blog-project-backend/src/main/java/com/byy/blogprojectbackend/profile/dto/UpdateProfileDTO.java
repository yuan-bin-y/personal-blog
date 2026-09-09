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

        @NotBlank(message = "昵称不能为空")
        @Size(max = 64, message = "昵称不能超过 64 个字符")
        String name,

        @Size(max = 1024, message = "头像地址不能超过 1024 个字符")
        String avatar,

        @Size(max = 128, message = "角色说明不能超过 128 个字符")
        String role,

        @Size(max = 500, message = "个人简介不能超过 500 个字符")
        String bio,

        @NotNull(message = "空间状态不能为空")
        @Valid
        UpdateProfileStatusDTO status,

        @NotNull(message = "版本号不能为空")
        @PositiveOrZero(message = "版本号不能小于 0")
        Integer version
) {
}
