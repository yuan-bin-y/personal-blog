package com.byy.blogprojectbackend.auth.principal;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.byy.blogprojectbackend.user.entity.SpaceUser;
import com.byy.blogprojectbackend.user.mapper.SpaceUserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security 的 BinSpace 账号加载器。
 *
 * <p>认证时按用户名读取 {@code space_user}，再转换为 {@link SpaceUserPrincipal}。</p>
 */
@Service
public class SpaceUserDetailsService implements UserDetailsService {

    private final SpaceUserMapper spaceUserMapper;

    public SpaceUserDetailsService(SpaceUserMapper spaceUserMapper) {
        this.spaceUserMapper = spaceUserMapper;
    }

    /** 按用户名读取未逻辑删除账号；MyBatis-Plus 会自动附加 deleted=0。 */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        SpaceUser user = spaceUserMapper.selectOne(
                Wrappers.<SpaceUser>lambdaQuery()
                        .eq(SpaceUser::getUsername, username)
        );

        if (user == null) {
            // 对外统一提示，避免泄露“用户名是否存在”。
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        return new SpaceUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getRole(),
                user.getStatus()
        );
    }
}
