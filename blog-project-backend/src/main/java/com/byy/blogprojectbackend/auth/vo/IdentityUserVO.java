package com.byy.blogprojectbackend.auth.vo;

/** 登录身份响应中的安全用户摘要，不包含密码哈希和数据库控制字段。 */
public record IdentityUserVO(
        String id,
        String username,
        String displayName,
        String avatar
) {
}
