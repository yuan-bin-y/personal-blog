package com.byy.blogprojectbackend.config;

import com.byy.blogprojectbackend.auth.token.JwtProperties;
import com.byy.blogprojectbackend.auth.token.JwtTokenService;
import com.byy.blogprojectbackend.auth.token.RedisTokenValidator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/** JWT 密钥、编解码器、校验器和权限映射配置。 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    private static final int MINIMUM_HS256_KEY_BYTES = 32;

    /** 将 Base64 环境变量转换为至少 256 bit 的 HMAC 密钥。 */
    @Bean
    public SecretKey jwtSecretKey(JwtProperties properties) {
        byte[] secretBytes;

        try {
            secretBytes = Base64.getDecoder().decode(properties.secretBase64());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET_BASE64 不是有效的 Base64", exception);
        }

        if (secretBytes.length < MINIMUM_HS256_KEY_BYTES) {
            throw new IllegalStateException("JWT_SECRET_BASE64 解码后必须至少为 32 字节");
        }

        return new SecretKeySpec(secretBytes, "HmacSHA256");
    }

    /** 使用 HS256 签发 JWT。 */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return NimbusJwtEncoder.withSecretKey(jwtSecretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    /** 同时校验签名、issuer、时间字段以及 Redis 中的 Token 有效状态。 */
    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey,
            JwtProperties properties,
            RedisTokenValidator redisTokenValidator
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.issuer()),
                redisTokenValidator
        ));

        return decoder;
    }

    /** 将 JWT authorities Claim 原样转换为 Spring Security 权限。 */
    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName(JwtTokenService.CLAIM_AUTHORITIES);
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return authenticationConverter;
    }
}
