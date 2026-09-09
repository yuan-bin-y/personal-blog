package com.byy.blogprojectbackend.auth.token;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/** 在 JWT 签名和过期时间校验后，继续确认该 Token 仍登记在 Redis 中。 */
@Component
public class RedisTokenValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_TOKEN = new OAuth2Error(
            "invalid_token",
            "Token 会话已失效，请重新登录",
            null
    );

    private final RedisTokenSessionService tokenSessionService;

    public RedisTokenValidator(RedisTokenSessionService tokenSessionService) {
        this.tokenSessionService = tokenSessionService;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String userId = jwt.getClaimAsString(JwtTokenService.CLAIM_USER_ID);

        if (!tokenSessionService.isActive(jwt.getId(), userId)) {
            return OAuth2TokenValidatorResult.failure(INVALID_TOKEN);
        }

        return OAuth2TokenValidatorResult.success();
    }
}
