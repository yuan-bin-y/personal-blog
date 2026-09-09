package com.byy.blogprojectbackend.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.byy.blogprojectbackend.auth.dto.OwnerLoginDTO;
import com.byy.blogprojectbackend.auth.dto.RegisterVisitorDTO;
import com.byy.blogprojectbackend.auth.principal.SpaceUserPrincipal;
import com.byy.blogprojectbackend.auth.service.AuthService;
import com.byy.blogprojectbackend.auth.token.IssuedAccessToken;
import com.byy.blogprojectbackend.auth.token.JwtTokenService;
import com.byy.blogprojectbackend.auth.vo.IdentityUserVO;
import com.byy.blogprojectbackend.auth.vo.IdentityVO;
import com.byy.blogprojectbackend.auth.vo.OwnerLoginVO;
import com.byy.blogprojectbackend.common.constant.AuthorityConstants;
import com.byy.blogprojectbackend.common.exception.ResourceConflictException;
import com.byy.blogprojectbackend.common.exception.ResourceNotFoundException;
import com.byy.blogprojectbackend.common.id.IdGenerator;
import com.byy.blogprojectbackend.profile.entity.SpaceProfile;
import com.byy.blogprojectbackend.profile.mapper.SpaceProfileMapper;
import com.byy.blogprojectbackend.user.entity.SpaceUser;
import com.byy.blogprojectbackend.user.enums.UserRole;
import com.byy.blogprojectbackend.user.enums.UserStatus;
import com.byy.blogprojectbackend.user.mapper.SpaceUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.List;

/**
 * BinSpace 用户认证业务实现。
 *
 * <p>服务端不创建登录 Session：凭据校验成功后签发 JWT Access Token，
 * 并将 Token 登记到 Redis；后续受保护接口通过 Bearer JWT + Redis 有效会话认证。</p>
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final IdGenerator idGenerator;
    private final SpaceUserMapper spaceUserMapper;
    private final SpaceProfileMapper spaceProfileMapper;

    /**
     * 创建 Visitor 账号与展示资料。两次写入处于同一事务中，任一失败都会回滚。
     */
    @Override
    @Transactional
    public OwnerLoginVO registerVisitor(RegisterVisitorDTO registerDTO) {
        String username = registerDTO.username()
                .trim()
                .toLowerCase(Locale.ROOT);
        String displayName = registerDTO.displayName().trim();

        Long existingCount = spaceUserMapper.selectCount(
                Wrappers.<SpaceUser>lambdaQuery()
                        .eq(SpaceUser::getUsername, username)
        );
        if (existingCount != null && existingCount > 0) {
            throw new ResourceConflictException("用户名已存在");
        }

        Long userId = idGenerator.nextId();
        SpaceUser user = new SpaceUser();
        user.setId(userId);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(registerDTO.password()));
        user.setRole(AuthorityConstants.ROLE_CODE_VISITOR);
        user.setStatus(UserStatus.ACTIVE.code());

        SpaceProfile profile = new SpaceProfile();
        profile.setId(idGenerator.nextId());
        profile.setUserId(userId);
        profile.setDisplayName(displayName);

        try {
            if (spaceUserMapper.insert(user) != 1) {
                throw new IllegalStateException("访客账号创建失败");
            }
        } catch (DuplicateKeyException exception) {
            // 预查询不能消除并发注册竞争，唯一索引才是最终防线。
            throw new ResourceConflictException("用户名已存在");
        }
        if (spaceProfileMapper.insert(profile) != 1) {
            throw new IllegalStateException("访客资料创建失败");
        }

        SpaceUserPrincipal principal = new SpaceUserPrincipal(
                userId,
                username,
                user.getPasswordHash(),
                user.getRole(),
                user.getStatus()
        );
        IdentityVO identity = buildIdentity(
                userId,
                username,
                user.getRole(),
                principal.getPermissionNames(),
                profile
        );
        IssuedAccessToken issuedToken = jwtTokenService.issue(principal);
        principal.eraseCredentials();

        return toLoginVO(issuedToken, identity);
    }

    /**
     * 校验 Visitor 或 Owner 凭据，签发 JWT 并登记 Redis Token Session。
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

        if (!(principalObject instanceof SpaceUserPrincipal principal)) {
            throw new IllegalStateException(
                    "登录认证主体类型不受支持"
            );
        }

        IdentityVO identity = buildIdentity(
                principal.getId(),
                principal.getUsername(),
                principal.getRole(),
                principal.getPermissionNames(),
                null
        );

        IssuedAccessToken issuedToken = jwtTokenService.issue(principal);

        return toLoginVO(issuedToken, identity);
    }

    /**
     * 根据请求携带的有效 JWT 组装当前 Visitor 或 Owner 身份。
     */
    @Override
    public IdentityVO getIdentity(Jwt jwt) {
        String userId = jwt.getClaimAsString(JwtTokenService.CLAIM_USER_ID);
        String role = jwt.getClaimAsString(JwtTokenService.CLAIM_ROLE);
        List<String> authorities =
                jwt.getClaimAsStringList(JwtTokenService.CLAIM_AUTHORITIES);

        // 兼容部署前已签发、尚未过期且没有 role claim 的 Owner Token。
        if (role == null && authorities != null
                && authorities.contains(AuthorityConstants.ROLE_OWNER)) {
            role = AuthorityConstants.ROLE_CODE_OWNER;
        }

        return buildIdentity(
                Long.valueOf(userId),
                jwt.getSubject(),
                role,
                filterRoleAuthority(authorities),
                null
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
     * 组装登录身份。注册时可直接传入刚创建的 Profile，其他场景按 userId 查询。
     */
    private IdentityVO buildIdentity(
            Long userId,
            String username,
            String role,
            List<String> permissions,
            SpaceProfile knownProfile
    ) {
        if (!UserRole.OWNER.matches(role) && !UserRole.VISITOR.matches(role)) {
            throw new IllegalStateException("不受支持的用户角色：" + role);
        }

        SpaceProfile profile = knownProfile != null
                ? knownProfile
                : spaceProfileMapper.selectByUserId(userId);

        if (profile == null) {
            throw new ResourceNotFoundException("用户资料不存在");
        }

        IdentityUserVO user = new IdentityUserVO(
                userId.toString(),
                username,
                profile.getDisplayName(),
                profile.getAvatarUrl()
        );

        return new IdentityVO(
                true,
                role,
                user,
                List.copyOf(permissions)
        );
    }

    private static OwnerLoginVO toLoginVO(
            IssuedAccessToken issuedToken,
            IdentityVO identity
    ) {
        return new OwnerLoginVO(
                issuedToken.value(),
                JwtTokenService.TOKEN_TYPE,
                issuedToken.expiresInSeconds(),
                identity
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
                .filter(authority -> !authority.startsWith(AuthorityConstants.SPRING_ROLE_PREFIX))
                .toList();
    }
}
