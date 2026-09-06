package com.byy.blogprojectbackend.auth.token;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/** JWT 签发配置；密钥必须通过环境变量注入，不能写入源码。 */
@Validated
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        @NotBlank String issuer,
        @NotNull Duration accessTokenTtl,
        @NotBlank String secretBase64
) {
}
