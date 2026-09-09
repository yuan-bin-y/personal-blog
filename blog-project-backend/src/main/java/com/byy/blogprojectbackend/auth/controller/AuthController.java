package com.byy.blogprojectbackend.auth.controller;

import com.byy.blogprojectbackend.auth.dto.OwnerLoginDTO;
import com.byy.blogprojectbackend.auth.dto.RegisterVisitorDTO;
import com.byy.blogprojectbackend.auth.service.AuthService;
import com.byy.blogprojectbackend.auth.vo.IdentityVO;
import com.byy.blogprojectbackend.auth.vo.OwnerLoginVO;
import com.byy.blogprojectbackend.common.result.Result;
import com.byy.blogprojectbackend.common.ratelimit.RateLimitProperties;
import com.byy.blogprojectbackend.common.ratelimit.RedisFixedWindowRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 身份认证接口。
 *
 * <p>Controller 只负责 HTTP 参数和响应；认证、JWT 签发/撤销与身份组装交给
 * AuthService。登录成功后由前端在后续请求中显式携带 Bearer Token。</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RedisFixedWindowRateLimiter rateLimiter;
    private final RateLimitProperties rateLimitProperties;

    /** 注册 Visitor，并直接返回 JWT Access Token 与当前身份。 */
    @PostMapping("/register")
    public ResponseEntity<Result<OwnerLoginVO>> registerVisitor(
            @Valid @RequestBody RegisterVisitorDTO registerDTO,
            HttpServletRequest request
    ) {
        rateLimiter.check(
                "auth:register",
                request.getRemoteAddr(),
                rateLimitProperties.getRegisterPerHour(),
                java.time.Duration.ofHours(1),
                "注册请求过于频繁，请稍后再试"
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Result.success(authService.registerVisitor(registerDTO)));
    }

    /** 使用用户名和密码登录 Visitor 或 Owner，返回 JWT 与当前身份。 */
    @PostMapping("/login")
    public Result<OwnerLoginVO> login(
            @Valid @RequestBody OwnerLoginDTO loginDTO,
            HttpServletRequest request
    ) {
        rateLimiter.check(
                "auth:login",
                request.getRemoteAddr() + ":" + loginDTO.username().trim().toLowerCase(java.util.Locale.ROOT),
                rateLimitProperties.getLoginPerMinute(),
                java.time.Duration.ofMinutes(1),
                "登录尝试过于频繁，请稍后再试"
        );
        return Result.success(
                authService.login(loginDTO)
        );
    }

    /** 返回当前身份；未携带有效 Bearer Token 时仍返回 200 和 VISITOR。 */
    @GetMapping("/me")
    public Result<IdentityVO> getCurrentIdentity(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Result.success(IdentityVO.visitor());
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return Result.success(
                    authService.getIdentity(jwt)
            );
        }

        throw new IllegalStateException(
                "登录认证主体类型不受支持"
        );
    }

    /** 撤销当前 Access Token 的 Redis Token Session，成功时返回 204。 */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication
    ) {
        if (authentication != null
                && authentication.getPrincipal() instanceof Jwt jwt) {
            authService.logout(jwt);
        }

        return ResponseEntity
                .noContent()
                .build();
    }
}
