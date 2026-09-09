package com.byy.blogprojectbackend;

import com.byy.blogprojectbackend.auth.token.RedisTokenSessionService;
import com.byy.blogprojectbackend.common.ratelimit.RedisFixedWindowRateLimiter;
import com.byy.blogprojectbackend.profile.entity.SpaceProfile;
import com.byy.blogprojectbackend.profile.mapper.SpaceProfileMapper;
import com.byy.blogprojectbackend.user.entity.SpaceUser;
import com.byy.blogprojectbackend.user.mapper.SpaceUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Auth 三接口的行为验证。
 *
 * <p>保持真实 Spring Security + JWT 过滤器链，仅替换数据库 Mapper 与 Redis
 * Token Session，因此不依赖本机 MySQL/Redis 服务即可验证登录签发、可选身份、
 * 退出撤销三条主路径。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    private static final String PASSWORD = "change-me";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpaceUserMapper spaceUserMapper;

    @MockitoBean
    private SpaceProfileMapper spaceProfileMapper;

    @MockitoBean
    private RedisTokenSessionService redisTokenSessionService;

    @MockitoBean
    private RedisFixedWindowRateLimiter rateLimiter;

    private String accessToken;

    @BeforeEach
    void setUp() {
        SpaceUser owner = new SpaceUser();
        owner.setId(1909508401234567168L);
        owner.setUsername("owner");
        owner.setPasswordHash(new BCryptPasswordEncoder().encode(PASSWORD));
        owner.setRole("OWNER");
        owner.setStatus("ACTIVE");

        when(spaceUserMapper.selectOne(any())).thenReturn(owner);
        when(spaceUserMapper.insert(any(SpaceUser.class))).thenReturn(1);

        SpaceProfile profile = new SpaceProfile();
        profile.setId(1L);
        profile.setUserId(owner.getId());
        profile.setDisplayName("玢");
        profile.setAvatarUrl("/media/avatar.jpg");

        when(spaceProfileMapper.selectByUserId(any())).thenReturn(profile);
        when(spaceProfileMapper.insert(any(SpaceProfile.class))).thenReturn(1);

        // 模拟“Token 仍登记在 Redis 中”，使有效 JWT 通过 RedisTokenValidator。
        when(redisTokenSessionService.isActive(anyString(), anyString()))
                .thenReturn(true);
    }

    @Test
    void loginSuccess_returnsAccessTokenAndOwnerIdentity() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"owner","password":"%s"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(7200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.identity.authenticated").value(true))
                .andExpect(jsonPath("$.data.identity.role").value("OWNER"))
                .andExpect(jsonPath("$.data.identity.user.id")
                        .value("1909508401234567168"))
                .andExpect(jsonPath("$.data.identity.user.displayName").value("玢"))
                .andExpect(jsonPath("$.data.identity.permissions[0]")
                        .value("POST_CREATE"))
                .andDo(result -> accessToken = com.jayway.jsonpath.JsonPath.read(
                        result.getResponse().getContentAsString(),
                        "$.data.accessToken"
                ));
    }

    @Test
    void loginFailure_returnsAuthFailed() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"owner","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_FAILED"));
    }

    @Test
    void registerVisitor_returnsCreatedTokenAndVisitorIdentity() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"Warm_Reader",
                                  "password":"Reader_2026",
                                  "displayName":"暖光访客"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.identity.authenticated").value(true))
                .andExpect(jsonPath("$.data.identity.role").value("VISITOR"))
                .andExpect(jsonPath("$.data.identity.user.username").value("warm_reader"))
                .andExpect(jsonPath("$.data.identity.user.displayName").value("暖光访客"))
                .andExpect(jsonPath("$.data.identity.permissions").isEmpty());
    }

    @Test
    void registerVisitorWithExistingUsername_returnsConflict() throws Exception {
        when(spaceUserMapper.selectCount(any())).thenReturn(1L);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"owner",
                                  "password":"Reader_2026",
                                  "displayName":"重复用户"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_CONFLICT"));
    }

    @Test
    void registeredVisitorCannotAccessOwnerApi() throws Exception {
        String token = registerVisitorAndGetToken();

        mockMvc.perform(get("/api/owner/posts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void meWithoutToken_returnsVisitor() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated").value(false))
                .andExpect(jsonPath("$.data.role").value("VISITOR"));
    }

    @Test
    void meWithValidToken_returnsOwnerIdentity() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated").value(true))
                .andExpect(jsonPath("$.data.role").value("OWNER"))
                .andExpect(jsonPath("$.data.user.id")
                        .value("1909508401234567168"));
    }

    @Test
    void meWithInvalidToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"));
    }

    @Test
    void logoutRevokesTokenAndReturnsNoContent() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(redisTokenSessionService).revoke(anyString());
    }

    @Test
    void logoutWithoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"));
    }

    private String loginAndGetToken() throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"owner","password":"%s"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return com.jayway.jsonpath.JsonPath.read(
                body,
                "$.data.accessToken"
        );
    }

    private String registerVisitorAndGetToken() throws Exception {
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"warm_reader",
                                  "password":"Reader_2026",
                                  "displayName":"暖光访客"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return com.jayway.jsonpath.JsonPath.read(
                body,
                "$.data.accessToken"
        );
    }
}
