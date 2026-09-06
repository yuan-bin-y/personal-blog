package com.byy.blogprojectbackend.profile.dto;

import jakarta.validation.constraints.Size;

/**
 * 更新个人状态请求。
 *
 * <p>字段允许为空；有值时长度必须与 profile 表字段保持一致。</p>
 */
public record UpdateProfileStatusDTO(

        @Size(max = 64)
        String label,

        @Size(max = 255)
        String text,

        @Size(max = 32)
        String emoji
) {
}
