package com.demo.flowable.exception;

import com.demo.flowable.dto.ErrorResponse;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * 全局异常处理器（响应式）
 * 处理认证、授权、JWT 相关异常
 *
 * @author e-Benben.Guo
 * @date 2025/11
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAuthenticationException(AuthenticationException e) {
        log.error("认证失败: {}", e.getMessage());

        ErrorResponse errorResponse;
        if (e instanceof BadCredentialsException) {
            errorResponse = ErrorResponse.invalidGrant("用户名或密码错误");
        } else if (e instanceof UsernameNotFoundException) {
            errorResponse = ErrorResponse.invalidGrant("用户不存在");
        } else {
            errorResponse = ErrorResponse.invalidGrant("认证失败: " + e.getMessage());
        }

        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
    }

    /**
     * 处理访问拒绝异常（权限不足）
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("访问拒绝: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                "access_denied",
                "权限不足: " + e.getMessage()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse));
    }

    /**
     * 处理 JWT 异常
     */
    @ExceptionHandler(JwtException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleJwtException(JwtException e) {
        log.error("JWT 验证失败: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.invalidToken("Token 无效: " + e.getMessage());

        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
    }

    /**
     * 处理速率限制超限异常
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleRateLimitExceededException(RateLimitExceededException e) {
        log.warn("速率限制超限: userId={}, limit={}", e.getUserId(), e.getLimit());

        ErrorResponse errorResponse = ErrorResponse.of(
                "rate_limit_exceeded",
                e.getMessage()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(e.getResetTimeSeconds()))
                .body(errorResponse));
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(WebExchangeBindException e) {
        log.error("参数校验失败: {}", e.getMessage());

        // 收集所有字段错误信息
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.invalidRequest(errorMessage);

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    /**
     * 处理通用运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);

        ErrorResponse errorResponse = ErrorResponse.of(
                "server_error",
                "服务器错误: " + e.getMessage()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleException(Exception e) {
        log.error("未知异常: ", e);

        ErrorResponse errorResponse = ErrorResponse.of(
                "server_error",
                "服务器内部错误"
        );

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }
}
