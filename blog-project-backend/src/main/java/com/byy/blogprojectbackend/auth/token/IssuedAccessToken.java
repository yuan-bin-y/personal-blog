package com.byy.blogprojectbackend.auth.token;

/** JWT 签发后的内部结果，不直接作为 Controller 响应。 */
public record IssuedAccessToken(
        String value,
        String tokenId,
        long expiresInSeconds
) {
}
