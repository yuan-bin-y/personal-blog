package com.byy.blogprojectbackend.common.exception;

import com.byy.blogprojectbackend.common.result.ApiErrorCode;
import com.byy.blogprojectbackend.common.result.Result;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller/Service 层的统一异常处理。
 *
 * <p>Spring Security 过滤器阶段的 401/403 由 auth.handler 包处理，二者职责不同。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 处理请求体 DTO 的 Bean Validation 错误。 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Map<String, String>>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> fieldErrors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage() == null
                                ? "参数不正确"
                                : error.getDefaultMessage()
                ));

        return ResponseEntity
                .badRequest()
                .body(Result.validation(fieldErrors));
    }

    /** 处理路径参数和查询参数的 Bean Validation 错误。 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Map<String, String>>> handleConstraintViolation(
            ConstraintViolationException exception
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getConstraintViolations()
                .forEach(violation -> fieldErrors.put(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ));

        return ResponseEntity
                .badRequest()
                .body(Result.validation(fieldErrors));
    }

    /** 处理请求 JSON 缺失或格式错误。 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleUnreadableMessage(
            HttpMessageNotReadableException exception
    ) {
        return ResponseEntity
                .badRequest()
                .body(Result.failure(
                        ApiErrorCode.BAD_REQUEST,
                        "请求 JSON 格式不正确"
                ));
    }

    /** 对外统一用户名不存在与密码错误，避免泄露账号是否存在。 */
    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<Result<Void>> handleAuthenticationFailed(
            RuntimeException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Result.failure(
                        ApiErrorCode.AUTH_FAILED,
                        "用户名或密码错误"
                ));
    }

    /** 处理账号被锁定或禁用。 */
    @ExceptionHandler({
            LockedException.class,
            DisabledException.class
    })
    public ResponseEntity<Result<Void>> handleAccountUnavailable(
            RuntimeException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Result.failure(
                        ApiErrorCode.FORBIDDEN,
                        "Owner 账号当前不可用"
                ));
    }

    /** 处理路径参数或查询参数无法转换为目标类型。 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Map<String, String>>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        Map<String, String> fieldErrors = Map.of(
                exception.getName(),
                "参数格式不正确"
        );

        return ResponseEntity
                .badRequest()
                .body(Result.validation(fieldErrors));
    }

    /** 处理请求的业务资源不存在。 */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Result<Void>> handleResourceNotFound(
            ResourceNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Result.failure(
                        ApiErrorCode.RESOURCE_NOT_FOUND,
                        exception.getMessage()
                ));
    }

    /** 处理乐观锁版本冲突。 */
    @ExceptionHandler(VersionConflictException.class)
    public ResponseEntity<Result<Void>> handleVersionConflict(
            VersionConflictException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Result.failure(
                        ApiErrorCode.VERSION_CONFLICT,
                        exception.getMessage()
                ));
    }

    /** 处理名称、slug、单条回复等唯一约束冲突。 */
    @ExceptionHandler({ResourceConflictException.class, DuplicateKeyException.class})
    public ResponseEntity<Result<Void>> handleResourceConflict(
            RuntimeException exception
    ) {
        String message = exception instanceof ResourceConflictException
                ? exception.getMessage()
                : "名称或 slug 已存在";

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Result.failure(ApiErrorCode.RESOURCE_CONFLICT, message));
    }

    /** Service 主动拒绝不符合类型规则或关联规则的请求。 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(
            IllegalArgumentException exception
    ) {
        return ResponseEntity
                .badRequest()
                .body(Result.failure(ApiErrorCode.BAD_REQUEST, exception.getMessage()));
    }

    /** 未知异常只向客户端返回通用信息，详细堆栈写入服务端日志。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnexpectedException(
            Exception exception
    ) {
        Result<Void> error = Result.failure(
                ApiErrorCode.INTERNAL_ERROR,
                "服务器内部错误"
        );

        log.error(
                "Unhandled exception, traceId={}",
                error.traceId(),
                exception
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}
