package com.byy.blogprojectbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** OWNER 登录请求；密码只用于本次认证，不写日志、不持久化。 */
public record OwnerLoginDTO(

        @NotBlank(message = "用户名不能为空")
        @Size(max = 64, message = "用户名不能超过64个字符")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(max = 128, message = "密码不能超过128个字符")
        String password
) {
}
