package com.byy.blogprojectbackend.auth.service.impl;

import com.byy.blogprojectbackend.auth.dto.OwnerLoginDTO;
import com.byy.blogprojectbackend.auth.principal.OwnerPrincipal;
import com.byy.blogprojectbackend.auth.service.AuthService;
import com.byy.blogprojectbackend.auth.token.IssuedAccessToken;
import com.byy.blogprojectbackend.auth.token.JwtTokenService;
import com.byy.blogprojectbackend.auth.vo.IdentityUserVO;
import com.byy.blogprojectbackend.auth.vo.IdentityVO;
import com.byy.blogprojectbackend.auth.vo.OwnerLoginVO;
import com.byy.blogprojectbackend.common.constant.AuthorityConstants;
import com.byy.blogprojectbackend.profile.entity.SpaceProfile;
import com.byy.blogprojectbackend.profile.mapper.SpaceProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OWNER 认证业务实现。
 *
 * <p>服务端不创建登录 Session：凭据校验成功后签发 JWT Access Token，
 * 并将 Token 登记到 Redis；后续受保护接口通过 Bearer JWT + Redis 有效会话认证。</p>
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final SpaceProfileMapper spaceProfileMapper;

    /**
     * 校验 OWNER 凭据，签发 JWT 并登记 Redis Token Session。
     */
    @Override
    public OwnerLoginVO login(OwnerLoginDTO loginDTO) {
        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        loginDTO.username(),
                        loginDTO.password()
                );

        Authentication authenticationResult =
                authenticationManager.authenticate(authenticationRequest);

        Object principalObject = authenticationResult.getPrincipal();

        if (!(principalObject instanceof OwnerPrincipal principal)) {
            throw new IllegalStateException(
                    "Unsupported authenticated principal"
            );
        }

        IdentityVO identity = buildOwnerIdentity(
                principal.getId(),
                principal.getUsername(),
                principal.getPermissionNames()
        );

        IssuedAccessToken issuedToken = jwtTokenService.issue(principal);

        return new OwnerLoginVO(
                issuedToken.value(),
                "Bearer",
                issuedToken.expiresInSeconds(),
                identity
        );
    }

    /**
     * 根据请求携带的有效 JWT 组装当前 OWNER 身份。
     */
    @Override
    public IdentityVO getOwnerIdentity(Jwt jwt) {
        String userId = jwt.getClaimAsString(JwtTokenService.CLAIM_USER_ID);
        List<String> authorities =
                jwt.getClaimAsStringList(JwtTokenService.CLAIM_AUTHORITIES);

        return buildOwnerIdentity(
                Long.valueOf(userId),
                jwt.getSubject(),
                filterRoleAuthority(authorities)
        );
    }

    /**
     * 撤销当前 JWT 对应的 Redis Token Session，使尚未过期的 Token 立即失效。
     */
    @Override
    public void logout(Jwt jwt) {
        jwtTokenService.revoke(jwt);
    }

    /**
     * 组装 OWNER 身份。
     */
    private IdentityVO buildOwnerIdentity(
            Long userId,
            String username,
            List<String> permissions
    ) {
        SpaceProfile profile = spaceProfileMapper.selectByUserId(userId);

        if (profile == null) {
            throw new IllegalStateException(
                    "Owner profile has not been initialized"
            );
        }

        IdentityUserVO user = new IdentityUserVO(
                userId.toString(),
                username,
                profile.getDisplayName(),
                profile.getAvatarUrl()
        );

        return new IdentityVO(
                true,
                "OWNER",
                user,
                List.copyOf(permissions)
        );
    }

    /**
     * 返回给前端的权限列表不含 Spring Security 内部使用的 ROLE_ 前缀。
     */
    private static List<String> filterRoleAuthority(List<String> authorities) {
        if (authorities == null) {
            return List.of();
        }

        return authorities.stream()
                .filter(authority -> !AuthorityConstants.ROLE_OWNER.equals(authority))
                .toList();
    }
}
