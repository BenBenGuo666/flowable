package com.demo.flowable.controller;

import com.demo.flowable.dto.*;
import com.demo.flowable.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * OAuth 2.0 认证控制器（响应式）
 *
 * 职责：
 * - 接收 HTTP 请求
 * - 调用 Service 层处理业务逻辑
 * - 返回 HTTP 响应
 * - 处理异常并返回标准错误格式
 *
 * 原则：
 * - 控制器应该保持简洁，不包含复杂的业务逻辑
 * - 所有业务逻辑都在 AuthService 中实现
 *
 * @author e-Benben.Guo
 * @since 2025/11
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OAuth2AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * POST /auth/login
     *
     * @param loginRequest 登录请求（用户名、密码）
     * @return TokenResponse 包含 access_token 和 refresh_token
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest.getUsername(), loginRequest.getPassword())
                .map(ResponseEntity::ok)
                .onErrorResume(this::handleAuthError);
    }

    /**
     * 刷新 Token
     * POST /auth/refresh
     *
     * @param refreshTokenRequest 包含 refresh_token
     * @return TokenResponse 包含新的 access_token 和 refresh_token
     */
    @PostMapping("/refresh")
    public Mono<ResponseEntity<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        return authService.refreshToken(refreshTokenRequest.getRefreshToken())
                .map(ResponseEntity::ok)
                .onErrorResume(this::handleAuthError);
    }

    /**
     * 登出（废除 Token）
     * POST /auth/logout
     * 需要携带 Authorization: Bearer {access_token}
     *
     * @param authorization Authorization 请求头
     * @return 成功消息
     */
    @PostMapping("/logout")
    public Mono<ResponseEntity<MessageResponse>> logout(@RequestHeader("Authorization") String authorization) {
        // 提取 Token（去掉 "Bearer " 前缀）
        String token = extractToken(authorization);

        return authService.logout(token)
                .then(Mono.just(ResponseEntity.ok(new MessageResponse("登出成功"))))
                .onErrorResume(e -> {
                    log.error("登出失败: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new MessageResponse("登出失败")));
                });
    }

    /**
     * 获取当前用户信息
     * GET /auth/me
     * 需要携带 Authorization: Bearer {access_token}
     *
     * @return 当前用户信息
     */
    @GetMapping("/me")
    public Mono<ResponseEntity<UserDTO>> getCurrentUser() {
        // 从 Security Context 中获取认证信息
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .flatMap(authService::getCurrentUser)
                .map(ResponseEntity::ok)
                .onErrorResume(this::handleAuthError);
    }

    /**
     * 提取 Token（去掉 "Bearer " 前缀）
     *
     * @param authorization Authorization 请求头
     * @return Token
     */
    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }

    /**
     * 处理认证错误
     *
     * @param e 异常
     * @param <T> 响应类型
     * @return 错误响应
     */
    private <T> Mono<ResponseEntity<T>> handleAuthError(Throwable e) {
        log.error("认证失败: {}", e.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    /**
     * 消息响应 DTO
     */
    public record MessageResponse(String message) {}
}
