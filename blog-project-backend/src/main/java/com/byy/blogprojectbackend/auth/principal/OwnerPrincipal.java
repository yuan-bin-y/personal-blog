package com.byy.blogprojectbackend.auth.principal;

import com.byy.blogprojectbackend.common.constant.AuthorityConstants;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Spring Security 保存的 OWNER 登录主体。
 *
 * <p>它只携带认证和授权所需信息，不直接暴露数据库 Entity，也不作为 API 响应。</p>
 */
public class OwnerPrincipal implements UserDetails, CredentialsContainer {

    private final Long id;
    private final String username;
    private String passwordHash;
    private final String status;

    public OwnerPrincipal(
            Long id,
            String username,
            String passwordHash,
            String status
    ) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public List<String> getPermissionNames() {
        return AuthorityConstants.OWNER_PERMISSIONS;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(
                new SimpleGrantedAuthority(AuthorityConstants.ROLE_OWNER)
        );

        AuthorityConstants.OWNER_PERMISSIONS.stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        return List.copyOf(authorities);
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"LOCKED".equals(status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(status);
    }

    /** 认证完成后清除内存中的密码哈希。 */
    @Override
    public void eraseCredentials() {
        this.passwordHash = null;
    }
}
