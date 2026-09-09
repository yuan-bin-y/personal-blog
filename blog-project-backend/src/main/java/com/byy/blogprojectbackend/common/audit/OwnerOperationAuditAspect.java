package com.byy.blogprojectbackend.common.audit;

import com.byy.blogprojectbackend.auth.token.JwtTokenService;
import com.byy.blogprojectbackend.common.constant.AuthorityConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 审计 OWNER 对 /api/owner/** 的写操作。
 *
 * <p>只记录操作人、目标接口、Controller 方法和耗时；不记录 DTO 内容、密码、Token、上传文件名或媒体地址。</p>
 */
@Aspect
@Component
public class OwnerOperationAuditAspect {

    private static final Logger log = LoggerFactory.getLogger(OwnerOperationAuditAspect.class);
    private static final String OWNER_API_PREFIX = "/api/owner/";

    @Around("@within(org.springframework.web.bind.annotation.RestController) && "
            + "(@annotation(org.springframework.web.bind.annotation.PostMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.PutMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.PatchMapping))")
    public Object auditOwnerWriteOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = currentRequest();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (request == null
                || !request.getRequestURI().startsWith(OWNER_API_PREFIX)
                || !isOwner(authentication)) {
            return joinPoint.proceed();
        }

        String ownerId = ownerId(authentication);
        String operation = operationName(joinPoint);
        long startedAt = System.nanoTime();

        log.info(
                "OWNER 操作开始，ownerId={} method={} path={} operation={}",
                ownerId,
                request.getMethod(),
                request.getRequestURI(),
                operation
        );

        try {
            Object result = joinPoint.proceed();
            log.info(
                    "OWNER 操作完成，ownerId={} method={} path={} operation={} elapsedMs={}",
                    ownerId,
                    request.getMethod(),
                    request.getRequestURI(),
                    operation,
                    elapsedMillis(startedAt)
            );
            return result;
        } catch (Throwable exception) {
            log.warn(
                    "OWNER 操作失败，ownerId={} method={} path={} operation={} errorType={} elapsedMs={}",
                    ownerId,
                    request.getMethod(),
                    request.getRequestURI(),
                    operation,
                    exception.getClass().getSimpleName(),
                    elapsedMillis(startedAt)
            );
            throw exception;
        }
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return servletAttributes.getRequest();
        }
        return null;
    }

    private boolean isOwner(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(AuthorityConstants.ROLE_OWNER::equals);
    }

    private String ownerId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String userId = jwt.getClaimAsString(JwtTokenService.CLAIM_USER_ID);
            if (userId != null && !userId.isBlank()) {
                return userId;
            }
        }
        return authentication.getName();
    }

    private String operationName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "#" + signature.getName();
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
