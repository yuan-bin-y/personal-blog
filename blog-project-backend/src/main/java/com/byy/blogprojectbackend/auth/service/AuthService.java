package com.byy.blogprojectbackend.auth.service;

import com.byy.blogprojectbackend.auth.dto.OwnerLoginDTO;
import com.byy.blogprojectbackend.auth.dto.RegisterVisitorDTO;
import com.byy.blogprojectbackend.auth.vo.IdentityVO;
import com.byy.blogprojectbackend.auth.vo.OwnerLoginVO;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * BinSpace 用户认证业务。
 */
public interface AuthService {

    /**
     * 注册 Visitor，签发 JWT 并登记 Redis Token Session。
     */
    OwnerLoginVO registerVisitor(RegisterVisitorDTO registerDTO);

    /**
     * 校验用户凭据，签发 JWT 并登记 Redis Token Session。
     */
    OwnerLoginVO login(OwnerLoginDTO loginDTO);

    /**
     * 根据请求携带的有效 JWT 组装当前登录身份。
     */
    IdentityVO getIdentity(Jwt jwt);

    /**
     * 撤销当前 JWT 对应的 Redis Token Session，使尚未过期的 Token 立即失效。
     */
    void logout(Jwt jwt);
}
