package com.byy.blogprojectbackend.auth.principal;

import com.byy.blogprojectbackend.common.constant.AuthorityConstants;
import com.byy.blogprojectbackend.user.enums.UserRole;
import com.byy.blogprojectbackend.user.enums.UserStatus;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * BinSpace 通用登录主体。
 *
 * 同时支持 VISITOR 和 OWNER，但不直接作为接口响应。
 */
public class SpaceUserPrincipal
        implements UserDetails, CredentialsContainer {

    private final Long id;
    private final String username;
    private String passwordHash;
    private final String role;
    private final String status;

    public SpaceUserPrincipal(
            Long id,
            String username,
            String passwordHash,
            String role,
            String status
    ) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public List<String> getPermissionNames() {
        if (UserRole.OWNER.matches(role)) {
            return AuthorityConstants.OWNER_PERMISSIONS;
        }

        return List.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (UserRole.OWNER.matches(role)) {
            authorities.add(
                    new SimpleGrantedAuthority(
                            AuthorityConstants.ROLE_OWNER
                    )
            );

            AuthorityConstants.OWNER_PERMISSIONS.stream()
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        } else if (UserRole.VISITOR.matches(role)) {
            authorities.add(
                    new SimpleGrantedAuthority(
                            AuthorityConstants.ROLE_VISITOR
                    )
            );
        } else {
            throw new IllegalStateException("不受支持的用户角色：" + role);
        }

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
        return !UserStatus.LOCKED.matches(status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return UserStatus.ACTIVE.matches(status);
    }

    @Override
    public void eraseCredentials() {
        passwordHash = null;
    }
}
