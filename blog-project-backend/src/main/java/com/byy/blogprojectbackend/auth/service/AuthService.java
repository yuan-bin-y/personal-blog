package com.byy.blogprojectbackend.auth.service;

import com.byy.blogprojectbackend.auth.dto.OwnerLoginDTO;
import com.byy.blogprojectbackend.auth.vo.IdentityVO;
import com.byy.blogprojectbackend.auth.vo.OwnerLoginVO;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * OWNER 认证业务。
 */
public interface AuthService {

    /**
     * 校验 OWNER 凭据，签发 JWT 并登记 Redis Token Session。
     */
    OwnerLoginVO login(OwnerLoginDTO loginDTO);

    /**
     * 根据请求携带的有效 JWT 组装当前 OWNER 身份。
     */
    IdentityVO getOwnerIdentity(Jwt jwt);

    /**
     * 撤销当前 JWT 对应的 Redis Token Session，使尚未过期的 Token 立即失效。
     */
    void logout(Jwt jwt);
}
