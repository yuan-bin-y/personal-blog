package com.byy.blogprojectbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Visitor 注册请求。
 *
 * 前端只能提交注册需要的字段，不能提交 id、role、status 等服务端字段。
 */
public record RegisterVisitorDTO(

        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 64, message = "用户名长度必须为 3 到 64 个字符")
        @Pattern(
                regexp = "^[A-Za-z0-9_]+$",
                message = "用户名只能包含字母、数字和下划线"
        )
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 128, message = "密码长度必须为 8 到 128 个字符")
        String password,

        @NotBlank(message = "昵称不能为空")
        @Size(max = 64, message = "昵称不能超过 64 个字符")
        String displayName
) {
}