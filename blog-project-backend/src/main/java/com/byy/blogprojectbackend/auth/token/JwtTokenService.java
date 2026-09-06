package com.byy.blogprojectbackend.auth.token;

import com.byy.blogprojectbackend.auth.principal.SpaceUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** 负责签发 JWT，并通过 RedisTokenSessionService 管理 Token 的登录有效状态。 */
@Service
public class JwtTokenService {

    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_AUTHORITIES = "authorities";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final RedisTokenSessionService tokenSessionService;
    private final Clock clock;

    @Autowired
    public JwtTokenService(
            JwtEncoder jwtEncoder,
            JwtProperties jwtProperties,
            RedisTokenSessionService tokenSessionService
    ) {
        this(jwtEncoder, jwtProperties, tokenSessionService, Clock.systemUTC());
    }

    JwtTokenService(
            JwtEncoder jwtEncoder,
            JwtProperties jwtProperties,
            RedisTokenSessionService tokenSessionService,
            Clock clock
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
        this.tokenSessionService = tokenSessionService;
        this.clock = clock;
    }

    /** 签发 access token，并用相同 TTL 将其 JTI 登记到 Redis。 */
    public IssuedAccessToken issue(
            SpaceUserPrincipal principal
    ) {
        Instant issuedAt = clock.instant();
        Duration ttl = jwtProperties.accessTokenTtl();
        Instant expiresAt = issuedAt.plus(ttl);
        String tokenId = UUID.randomUUID().toString();

        List<String> authorities = principal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(principal.getUsername())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(tokenId)
                .claim(CLAIM_USER_ID, principal.getId().toString())
                .claim(CLAIM_ROLE, principal.getRole())
                .claim(CLAIM_AUTHORITIES, authorities)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        String tokenValue = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        tokenSessionService.activate(tokenId, principal.getId(), ttl);

        return new IssuedAccessToken(
                tokenValue,
                tokenId,
                ttl.toSeconds()
        );
    }

    /** 注销当前请求携带的 JWT。 */
    public void revoke(Jwt jwt) {
        tokenSessionService.revoke(jwt.getId());
    }
}
