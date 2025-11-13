package com.demo.flowable.filter;

import com.demo.flowable.service.TokenService;
import com.demo.flowable.util.TokenExtractor;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义 JWT 认证过滤器
 * 提供完整的 JWT Token 拦截、验证、解析功能，支持扩展
 *
 * 扩展点：
 * 1. preValidate() - Token 验证前的前置处理
 * 2. postValidate() - Token 验证后的后置处理
 * 3. extractAdditionalClaims() - 从 Token 中提取额外的自定义信息
 * 4. handleValidationError() - 自定义错误处理
 *
 * @author e-Benben.Guo
 * @date 2025/11
 */
@Slf4j
@Component
@Order(2)  // 在 RateLimitFilter 之后执行
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final TokenService tokenService;
    private final TokenExtractor tokenExtractor;

    /**
     * 白名单路径（不需要 Token 验证）
     */
    private static final String[] WHITE_LIST_PATHS = {
            "/api/auth/login",
            "/api/auth/refresh"
    };

    /**
     * 黑名单路径（明确禁止访问）
     */
    private static final String[] BLACK_LIST_PATHS = {
            "/api/init/"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 检查黑名单
        if (isBlackListPath(path)) {
            log.warn("访问被禁止的路径: {}", path);
            return handleForbidden(exchange, "访问被禁止");
        }

        // 检查白名单
        if (isWhiteListPath(path)) {
            log.debug("白名单路径，跳过 Token 验证: {}", path);
            return chain.filter(exchange);
        }

        // 从请求头中提取 Token
        return tokenExtractor.extractToken(exchange)
                .flatMap(token -> {
                    log.debug("提取到 Token，开始验证: {}", token.substring(0, Math.min(20, token.length())) + "...");

                    // 【扩展点1】前置处理
                    return preValidate(exchange, token)
                            .then(validateAndAuthenticate(exchange, token))
                            .flatMap(authentication -> {
                                // 【扩展点2】后置处理
                                return postValidate(exchange, token, authentication)
                                        .then(chain.filter(exchange)
                                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)));
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("请求头中未找到 Token: {}", path);
                    return handleUnauthorized(exchange, "缺少认证令牌");
                }))
                .onErrorResume(e -> handleValidationError(exchange, e));
    }

    /**
     * 验证 Token 并创建 Authentication 对象
     */
    private Mono<UsernamePasswordAuthenticationToken> validateAndAuthenticate(
            ServerWebExchange exchange, String token) {

        return tokenService.validateToken(token)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new RuntimeException("Token 验证失败"));
                    }

                    try {
                        // 解析 Token
                        Claims claims = tokenService.parseToken(token);

                        // 提取基本信息
                        String username = claims.getSubject();
                        Long userId = getUserIdFromClaims(claims);

                        // 提取权限列表
                        List<String> authorities = getAuthoritiesFromClaims(claims);

                        // 【扩展点3】提取额外的自定义 Claims
                        extractAdditionalClaims(exchange, claims);

                        // 构建权限对象
                        List<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                        log.info("Token 验证成功: username={}, userId={}, authorities={}",
                                username, userId, authorities);

                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        username,
                                        null,
                                        grantedAuthorities
                                );

                        // 将用户信息存储到 details 中，方便后续使用
                        authentication.setDetails(new JwtUserDetails(userId, username, claims));

                        return Mono.just(authentication);

                    } catch (Exception e) {
                        log.error("Token 解析失败: {}", e.getMessage());
                        return Mono.error(new RuntimeException("Token 解析失败: " + e.getMessage()));
                    }
                });
    }

    /**
     * 【扩展点1】Token 验证前的前置处理
     * 可以在这里添加自定义的前置逻辑，例如：
     * - 记录日志
     * - 检查 IP 白名单
     * - 检查设备信息
     * - 自定义业务逻辑
     *
     * @param exchange ServerWebExchange
     * @param token JWT Token
     * @return Mono<Void>
     */
    protected Mono<Void> preValidate(ServerWebExchange exchange, String token) {
        // 默认实现：记录请求信息
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().toString();
        String remoteAddress = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        log.debug("JWT 认证前置处理: method={}, path={}, remoteAddress={}",
                method, path, remoteAddress);

        // 可以在这里添加自定义逻辑
        // 例如：检查 IP 是否在白名单中
        // 例如：检查请求来源
        // 例如：记录审计日志

        return Mono.empty();
    }

    /**
     * 【扩展点2】Token 验证后的后置处理
     * 可以在这里添加自定义的后置逻辑，例如：
     * - 更新用户最后活动时间
     * - 记录登录日志
     * - 刷新 Token（自动续期）
     * - 自定义业务逻辑
     *
     * @param exchange ServerWebExchange
     * @param token JWT Token
     * @param authentication 认证对象
     * @return Mono<Void>
     */
    protected Mono<Void> postValidate(ServerWebExchange exchange, String token,
                                     UsernamePasswordAuthenticationToken authentication) {
        // 默认实现：记录认证成功日志
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getDetails();
        log.debug("JWT 认证后置处理: userId={}, username={}",
                userDetails.getUserId(), userDetails.getUsername());

        // 可以在这里添加自定义逻辑
        // 例如：更新用户最后活动时间
        // 例如：记录登录日志到数据库
        // 例如：检查 Token 是否即将过期，自动刷新

        return Mono.empty();
    }

    /**
     * 【扩展点3】从 Token 中提取额外的自定义 Claims
     * 可以在这里提取你自定义的 Claims 信息，例如：
     * - 租户 ID（多租户系统）
     * - 设备信息
     * - 客户端版本
     * - 其他自定义字段
     *
     * @param exchange ServerWebExchange
     * @param claims JWT Claims
     */
    protected void extractAdditionalClaims(ServerWebExchange exchange, Claims claims) {
        // 默认实现：提取常见的自定义字段

        // 示例1：提取租户 ID
        if (claims.containsKey("tenant_id")) {
            String tenantId = claims.get("tenant_id", String.class);
            log.debug("提取租户 ID: {}", tenantId);
            // 可以将租户 ID 存储到 ThreadLocal 或 Exchange Attributes 中
            exchange.getAttributes().put("tenant_id", tenantId);
        }

        // 示例2：提取设备信息
        if (claims.containsKey("device_id")) {
            String deviceId = claims.get("device_id", String.class);
            log.debug("提取设备 ID: {}", deviceId);
            exchange.getAttributes().put("device_id", deviceId);
        }

        // 示例3：提取客户端类型
        if (claims.containsKey("client_type")) {
            String clientType = claims.get("client_type", String.class);
            log.debug("提取客户端类型: {}", clientType);
            exchange.getAttributes().put("client_type", clientType);
        }

        // 你可以在这里添加更多自定义 Claims 的提取逻辑
    }

    /**
     * 【扩展点4】自定义错误处理
     * 可以在这里自定义 Token 验证失败时的错误响应
     *
     * @param exchange ServerWebExchange
     * @param error 错误信息
     * @return Mono<Void>
     */
    protected Mono<Void> handleValidationError(ServerWebExchange exchange, Throwable error) {
        log.error("Token 验证失败: {}", error.getMessage());

        String errorMessage;
        HttpStatus status;

        if (error.getMessage().contains("过期")) {
            errorMessage = "Token 已过期，请重新登录";
            status = HttpStatus.UNAUTHORIZED;
        } else if (error.getMessage().contains("黑名单") || error.getMessage().contains("废除")) {
            errorMessage = "Token 已失效，请重新登录";
            status = HttpStatus.FORBIDDEN;
        } else if (error.getMessage().contains("签名")) {
            errorMessage = "Token 签名无效";
            status = HttpStatus.UNAUTHORIZED;
        } else {
            errorMessage = "Token 验证失败: " + error.getMessage();
            status = HttpStatus.UNAUTHORIZED;
        }

        return respondWithError(exchange, status, errorMessage);
    }

    /**
     * 从 Claims 中提取用户 ID
     */
    private Long getUserIdFromClaims(Claims claims) {
        Object userId = claims.get("user_id");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 从 Claims 中提取权限列表
     */
    @SuppressWarnings("unchecked")
    private List<String> getAuthoritiesFromClaims(Claims claims) {
        Object authorities = claims.get("authorities");
        if (authorities instanceof List) {
            return (List<String>) authorities;
        }
        // 默认权限
        return new ArrayList<>(List.of("ROLE_USER"));
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhiteListPath(String path) {
        for (String whiteListPath : WHITE_LIST_PATHS) {
            if (path.startsWith(whiteListPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查路径是否在黑名单中
     */
    private boolean isBlackListPath(String path) {
        for (String blackListPath : BLACK_LIST_PATHS) {
            if (path.startsWith(blackListPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回 401 Unauthorized
     */
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        return respondWithError(exchange, HttpStatus.UNAUTHORIZED, message);
    }

    /**
     * 返回 403 Forbidden
     */
    private Mono<Void> handleForbidden(ServerWebExchange exchange, String message) {
        return respondWithError(exchange, HttpStatus.FORBIDDEN, message);
    }

    /**
     * 返回错误响应
     */
    private Mono<Void> respondWithError(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorResponse = String.format(
                "{\"error\":\"%s\",\"error_description\":\"%s\",\"path\":\"%s\"}",
                status.getReasonPhrase().toLowerCase().replace(" ", "_"),
                message,
                exchange.getRequest().getURI().getPath()
        );

        byte[] bytes = errorResponse.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
        );
    }

    /**
     * JWT 用户详情
     * 存储从 Token 中提取的用户信息
     */
    public static class JwtUserDetails {
        private final Long userId;
        private final String username;
        private final Claims claims;

        public JwtUserDetails(Long userId, String username, Claims claims) {
            this.userId = userId;
            this.username = username;
            this.claims = claims;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public Claims getClaims() {
            return claims;
        }

        /**
         * 获取自定义 Claim
         */
        public <T> T getCustomClaim(String key, Class<T> type) {
            return claims.get(key, type);
        }
    }
}
