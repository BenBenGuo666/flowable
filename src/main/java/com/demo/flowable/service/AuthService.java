package com.demo.flowable.service;

import com.demo.flowable.auth.ReactiveUserService;
import com.demo.flowable.config.JwtConfig;
import com.demo.flowable.dto.TokenResponse;
import com.demo.flowable.dto.UserDTO;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 认证业务服务（响应式 OAuth 2.0）
 * 处理登录、Token 刷新、登出等认证相关业务逻辑
 *
 * 职责：
 * - 封装所有认证相关的业务逻辑
 * - 协调多个服务（TokenService、UserService、BlacklistService）
 * - 提供简洁的业务接口给控制器层调用
 *
 * @author e-Benben.Guo
 * @since 2025/11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final ReactiveAuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final ReactiveUserService reactiveUserService;
    private final JwtConfig jwtConfig;

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return TokenResponse 包含 access_token 和 refresh_token
     */
    public Mono<TokenResponse> login(String username, String password) {
        log.info("执行登录业务逻辑: {}", username);

        // 创建认证凭证
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password);

        // 执行认证
        return authenticationManager.authenticate(authToken)
                .flatMap(this::generateTokenResponse)
                .doOnSuccess(response -> log.info("用户登录成功: {}", username))
                .doOnError(e -> log.error("登录失败: {}", e.getMessage()));
    }

    /**
     * 刷新 Token
     *
     * @param refreshToken Refresh Token
     * @return TokenResponse 包含新的 access_token 和 refresh_token
     */
    public Mono<TokenResponse> refreshToken(String refreshToken) {
        log.info("执行刷新 Token 业务逻辑");

        // 验证 Refresh Token 类型
        if (!tokenService.isRefreshToken(refreshToken)) {
            return Mono.error(new IllegalArgumentException("不是有效的 Refresh Token"));
        }

        // 验证 Token 是否有效（签名、过期、黑名单）
        return tokenService.validateToken(refreshToken)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new IllegalArgumentException("Refresh Token 无效或已过期"));
                    }

                    // 解析 Token 获取用户信息
                    String username = tokenService.getUsernameFromToken(refreshToken);
                    Long userId = tokenService.getUserIdFromToken(refreshToken);

                    // 查询用户详情（获取最新权限）
                    return reactiveUserService.findByUsername(username)
                            .flatMap(userDetails -> {
                                // 生成新的 Token
                                String newAccessToken = tokenService.generateAccessToken(
                                        username, userId, userDetails.getAuthorities());
                                String newRefreshToken = tokenService.generateRefreshToken(username, userId);

                                // 将旧的 Refresh Token 加入黑名单
                                return revokeToken(refreshToken)
                                        .then(Mono.defer(() -> {
                                            TokenResponse tokenResponse = buildTokenResponse(
                                                    newAccessToken, newRefreshToken, null);

                                            log.info("Token 刷新成功: {}", username);
                                            return Mono.just(tokenResponse);
                                        }));
                            })
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("用户不存在")));
                });
    }

    /**
     * 登出（废除 Token）
     *
     * @param token Access Token
     * @return 完成信号
     */
    public Mono<Void> logout(String token) {
        log.info("执行登出业务逻辑");
        return revokeToken(token)
                .doOnSuccess(v -> log.info("登出成功"));
    }

    /**
     * 获取当前用户信息
     *
     * @param authentication 认证信息
     * @return 用户信息 DTO
     */
    public Mono<UserDTO> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Mono.error(new IllegalArgumentException("未认证"));
        }

        String username = authentication.getName();
        log.info("获取当前用户信息: {}", username);

        return reactiveUserService.findUserByUsername(username)
                .map(user -> {
                    UserDTO userDTO = new UserDTO();
                    BeanUtils.copyProperties(user, userDTO);
                    userDTO.setPassword(null); // 不返回密码

                    // 添加权限信息
                    var authorities = authentication.getAuthorities().stream()
                            .map(authority -> authority.getAuthority())
                            .toList();
                    userDTO.setAuthorities(authorities);

                    return userDTO;
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("用户不存在")));
    }

    /**
     * 生成 Token 响应
     *
     * @param authentication 认证信息
     * @return TokenResponse
     */
    private Mono<TokenResponse> generateTokenResponse(Authentication authentication) {
        String username = authentication.getName();

        // 查询用户信息
        return reactiveUserService.findUserByUsername(username)
                .map(user -> {
                    // 生成 Access Token 和 Refresh Token
                    String accessToken = tokenService.generateAccessToken(
                            username, user.getId(), authentication.getAuthorities());
                    String refreshToken = tokenService.generateRefreshToken(username, user.getId());

                    // 构建用户 DTO
                    UserDTO userDTO = new UserDTO();
                    BeanUtils.copyProperties(user, userDTO);
                    userDTO.setPassword(null); // 不返回密码

                    // 构建 Token 响应
                    return buildTokenResponse(accessToken, refreshToken, userDTO);
                });
    }

    /**
     * 构建 Token 响应对象
     *
     * @param accessToken  Access Token
     * @param refreshToken Refresh Token
     * @param userDTO      用户信息（可选）
     * @return TokenResponse
     */
    private TokenResponse buildTokenResponse(String accessToken, String refreshToken, UserDTO userDTO) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getAccessTokenExpiration() / 1000) // 转换为秒
                .user(userDTO)
                .build();
    }

    /**
     * 废除 Token（加入黑名单）
     *
     * @param token Token
     * @return 完成信号
     */
    private Mono<Void> revokeToken(String token) {
        try {
            Claims claims = tokenService.parseToken(token);
            String tokenId = claims.getId();
            long expirationTime = claims.getExpiration().getTime();

            return tokenBlacklistService.addToBlacklist(tokenId, expirationTime)
                    .doOnSuccess(v -> log.debug("Token 已加入黑名单: {}", tokenId));
        } catch (Exception e) {
            log.error("废除 Token 失败: {}", e.getMessage());
            return Mono.error(new IllegalArgumentException("Token 无效"));
        }
    }
}
