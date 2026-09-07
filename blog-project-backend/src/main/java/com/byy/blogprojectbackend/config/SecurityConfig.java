package com.byy.blogprojectbackend.config;

import com.byy.blogprojectbackend.auth.handler.RestAccessDeniedHandler;
import com.byy.blogprojectbackend.auth.handler.RestAuthenticationEntryPoint;
import com.byy.blogprojectbackend.auth.principal.SpaceUserDetailsService;
import com.byy.blogprojectbackend.common.constant.AuthorityConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;

/**
 * BinSpace V1 安全配置。
 *
 * <p>
 * Visitor 或 Owner 通过用户名密码登录并取得 JWT，
 * 后续请求使用 Authorization: Bearer Token。
 * 服务端不创建登录 Session；
 * JWT 还必须通过 Redis 有效会话校验。
 * </p>
 */
@Configuration
public class SecurityConfig {

    /**
     * 定义公开接口、登录用户接口、OWNER 接口和无状态 Bearer JWT 认证。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
    ) throws Exception {

        http
                // Bearer Token 不由浏览器自动附带，
                // 因此这里不使用 Cookie 场景下的 CSRF 防护。
                .csrf(AbstractHttpConfigurer::disable)

                // 不创建服务端登录 Session。
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 接口访问权限配置。
                .authorizeHttpRequests(authorize -> authorize

                        // =========================
                        // OWNER 专属接口
                        // =========================

                        .requestMatchers(HttpMethod.POST, "/api/owner/posts/tech", "/api/owner/posts/moments")
                        .hasAuthority(AuthorityConstants.Permission.POST_CREATE.getCode())

                        .requestMatchers(HttpMethod.PUT, "/api/owner/posts/tech/*", "/api/owner/posts/moments/*")
                        .hasAuthority(AuthorityConstants.Permission.POST_EDIT.getCode())

                        .requestMatchers(HttpMethod.DELETE, "/api/owner/posts/tech/*", "/api/owner/posts/moments/*")
                        .hasAuthority(AuthorityConstants.Permission.POST_DELETE.getCode())

                        .requestMatchers("/api/owner/categories", "/api/owner/categories/**")
                        .hasAuthority(AuthorityConstants.Permission.CATEGORY_MANAGE.getCode())

                        .requestMatchers("/api/owner/tags", "/api/owner/tags/**")
                        .hasAuthority(AuthorityConstants.Permission.TAG_MANAGE.getCode())

                        .requestMatchers(HttpMethod.POST, "/api/owner/comments/*/reply")
                        .hasAuthority(AuthorityConstants.Permission.COMMENT_REPLY.getCode())

                        .requestMatchers(HttpMethod.DELETE, "/api/owner/comments/*")
                        .hasAuthority(AuthorityConstants.Permission.COMMENT_DELETE.getCode())

                        .requestMatchers(HttpMethod.POST, "/api/owner/guestbook/*/reply")
                        .hasAuthority(AuthorityConstants.Permission.GUESTBOOK_REPLY.getCode())

                        .requestMatchers(HttpMethod.DELETE, "/api/owner/guestbook/*")
                        .hasAuthority(AuthorityConstants.Permission.GUESTBOOK_DELETE.getCode())

                        .requestMatchers("/api/owner/**")
                        .hasRole("OWNER")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/logout"
                        )
                        .authenticated()


                        // =========================
                        // Visitor / Public 接口
                        // =========================

                        // Visitor 与 Owner 登录后都可以对公开 Post 发表评论。
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/posts/*/comments"
                        )
                        .authenticated()

                        // 注册和登录接口。
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/register"
                        )
                        .permitAll()

                        // 获取当前身份：
                        // 未登录返回 Visitor，
                        // 已登录返回其真实 Visitor/Owner 身份。
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/me"
                        )
                        .permitAll()

                        // 获取公开站点初始化数据。
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/site/bootstrap"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/posts",
                                "/api/posts/**"
                        )
                        .permitAll()

                        // Visitor 公开归档。
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/archive"
                        )
                        .permitAll()

                        // 分类、标签、评论和留言板公开读取。
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categories",
                                "/api/tags",
                                "/api/guestbook",
                                "/api/posts/*/comments"
                        )
                        .permitAll()


                        // =========================
                        // 其它接口
                        // =========================

                        // 没有明确声明为公开的接口，
                        // 默认都要求登录。
                        .anyRequest()
                        .authenticated()
                )

                // OAuth2 Resource Server：
                // 使用 JWT Bearer Token 认证。
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                        .authenticationEntryPoint(
                                authenticationEntryPoint
                        )
                        .accessDeniedHandler(
                                accessDeniedHandler
                        )
                )

                // 统一处理 Spring Security 的 401 / 403。
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(
                                authenticationEntryPoint
                        )
                        .accessDeniedHandler(
                                accessDeniedHandler
                        )
                )

                // 不使用传统表单登录。
                .formLogin(AbstractHttpConfigurer::disable)

                // 不使用 HTTP Basic。
                .httpBasic(AbstractHttpConfigurer::disable)

                // 不使用 Spring Security 默认 logout。
                // 我们自己的 /api/auth/logout 负责撤销 Redis 中的 JWT 会话。
                .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * 使用 BCrypt 校验数据库中的密码哈希。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 使用数据库账号加载器 + BCrypt
     * 创建用户名密码登录使用的 AuthenticationManager。
     */
    @Bean
    public AuthenticationManager authenticationManager(
            SpaceUserDetailsService spaceUserDetailsService,
            PasswordEncoder passwordEncoder
    ) {

        DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider(
                        spaceUserDetailsService
                );

        authenticationProvider.setPasswordEncoder(
                passwordEncoder
        );

        return new ProviderManager(
                authenticationProvider
        );
    }
}
