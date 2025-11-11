package com.demo.flowable.service;

import com.demo.flowable.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Token 服务
 * 负责 JWT Token 的生成、验证、解析
 *
 * @author e-Benben.Guo
 * @date 2025/11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtConfig jwtConfig;
    private final SecretKey jwtSigningKey;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 生成 Access Token
     *
     * @param username 用户名
     * @param userId 用户ID
     * @param authorities 权限列表
     * @return Access Token
     */
    public String generateAccessToken(String username, Long userId, Collection<? extends GrantedAuthority> authorities) {
        log.debug("生成 Access Token, 用户: {}, ID: {}", username, userId);

        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtConfig.getAccessTokenExpiration());

        // 提取权限字符串列表
        List<String> authorityList = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", userId);
        claims.put("authorities", authorityList);
        claims.put("token_type", "access");

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuer(jwtConfig.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .id(UUID.randomUUID().toString()) // jti: 唯一标识符，用于黑名单
                .signWith(jwtSigningKey)
                .compact();
    }

    /**
     * 生成 Refresh Token
     *
     * @param username 用户名
     * @param userId 用户ID
     * @return Refresh Token
     */
    public String generateRefreshToken(String username, Long userId) {
        log.debug("生成 Refresh Token, 用户: {}, ID: {}", username, userId);

        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtConfig.getRefreshTokenExpiration());

        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", userId);
        claims.put("token_type", "refresh");

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuer(jwtConfig.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .id(UUID.randomUUID().toString())
                .signWith(jwtSigningKey)
                .compact();
    }

    /**
     * 验证 Token（响应式）
     * 检查签名、过期时间、黑名单
     *
     * @param token JWT Token
     * @return Mono<Boolean> 是否有效
     */
    public Mono<Boolean> validateToken(String token) {
        try {
            Claims claims = parseToken(token);

            // 检查是否过期
            if (claims.getExpiration().before(new Date())) {
                log.debug("Token 已过期");
                return Mono.just(false);
            }

            // 检查是否在黑名单中
            String jti = claims.getId();
            return tokenBlacklistService.isBlacklisted(jti)
                    .map(isBlacklisted -> {
                        if (isBlacklisted) {
                            log.debug("Token 在黑名单中: {}", jti);
                        }
                        return !isBlacklisted;
                    });

        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token 验证失败: {}", e.getMessage());
            return Mono.just(false);
        }
    }

    /**
     * 解析 Token 获取 Claims
     *
     * @param token JWT Token
     * @return Claims
     * @throws JwtException 解析失败
     */
    public Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(jwtSigningKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (JwtException e) {
            log.error("从 Token 获取用户名失败: {}", e.getMessage());
            throw new RuntimeException("无效的 Token");
        }
    }

    /**
     * 从 Token 中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            Object userId = claims.get("user_id");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            }
            return (Long) userId;
        } catch (JwtException e) {
            log.error("从 Token 获取用户ID失败: {}", e.getMessage());
            throw new RuntimeException("无效的 Token");
        }
    }

    /**
     * 从 Token 中获取权限列表
     *
     * @param token JWT Token
     * @return 权限列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getAuthoritiesFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            Object authorities = claims.get("authorities");
            if (authorities instanceof List) {
                return (List<String>) authorities;
            }
            return Collections.emptyList();
        } catch (JwtException e) {
            log.error("从 Token 获取权限列表失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 检查是否为 Refresh Token
     *
     * @param token JWT Token
     * @return 是否为 Refresh Token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            String tokenType = (String) claims.get("token_type");
            return "refresh".equals(tokenType);
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 获取 Token 的过期时间（秒）
     *
     * @param token JWT Token
     * @return 剩余秒数，-1 表示已过期或无效
     */
    public long getTokenExpirationSeconds(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            long remainingMillis = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remainingMillis / 1000);
        } catch (JwtException e) {
            return -1;
        }
    }

    /**
     * 提取 Token ID (jti)
     *
     * @param token JWT Token
     * @return Token ID
     */
    public String getTokenId(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getId();
        } catch (JwtException e) {
            log.error("提取 Token ID 失败: {}", e.getMessage());
            return null;
        }
    }
}
