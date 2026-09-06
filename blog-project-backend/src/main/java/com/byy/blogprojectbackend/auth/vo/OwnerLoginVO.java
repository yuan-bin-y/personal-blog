package com.byy.blogprojectbackend.auth.vo;

/** Visitor/Owner 共用的登录成功响应，Bearer Token 由前端仅保存在运行时内存。 */
public record OwnerLoginVO(
        String accessToken,
        String tokenType,
        long expiresIn,
        IdentityVO identity
) {
}
