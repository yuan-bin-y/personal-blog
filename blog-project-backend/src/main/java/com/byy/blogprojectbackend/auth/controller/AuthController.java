package com.byy.blogprojectbackend.auth.controller;

import com.byy.blogprojectbackend.auth.dto.OwnerLoginDTO;
import com.byy.blogprojectbackend.auth.service.AuthService;
import com.byy.blogprojectbackend.auth.vo.IdentityVO;
import com.byy.blogprojectbackend.auth.vo.OwnerLoginVO;
import com.byy.blogprojectbackend.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    /** 使用用户名和密码登录 OWNER，返回 JWT Access Token 与当前身份。 */
    @PostMapping("/login")
    public Result<OwnerLoginVO> login(
            @Valid @RequestBody OwnerLoginDTO loginDTO
    ) {
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
                    authService.getOwnerIdentity(jwt)
            );
        }

        throw new IllegalStateException(
                "Unsupported authenticated principal"
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
