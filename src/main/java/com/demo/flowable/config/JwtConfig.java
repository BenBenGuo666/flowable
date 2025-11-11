package com.demo.flowable.config;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * JWT 配置类
 * 配置 JWT 的签名密钥、过期时间等参数
 *
 * @author e-Benben.Guo
 * @date 2025/11
 */
@Getter
@Configuration
public class JwtConfig {

    /**
     * JWT 签名密钥（必须至少 256 位）
     */
    @Value("${jwt.secret:flowable-secret-key-for-jwt-token-generation-must-be-at-least-256-bits}")
    private String secret;

    /**
     * Access Token 过期时间（毫秒）
     * 默认 1 小时
     */
    @Value("${jwt.access-token-expiration:3600000}")
    private Long accessTokenExpiration;

    /**
     * Refresh Token 过期时间（毫秒）
     * 默认 7 天
     */
    @Value("${jwt.refresh-token-expiration:604800000}")
    private Long refreshTokenExpiration;

    /**
     * JWT 签发者
     */
    @Value("${jwt.issuer:flowable-auth-server}")
    private String issuer;

    /**
     * 获取 HMAC-SHA256 签名密钥
     *
     * @return SecretKey
     */
    @Bean
    public SecretKey jwtSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 配置响应式 JWT 解码器（用于 OAuth 2.0 Resource Server）
     * Spring Security 会自动使用此解码器验证 JWT Token
     *
     * @return ReactiveJwtDecoder
     */
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        // 使用对称密钥（HMAC-SHA256）创建响应式 JWT 解码器
        SecretKey key = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8),
            "HmacSHA256"
        );
        return NimbusReactiveJwtDecoder.withSecretKey(key).build();
    }
}
