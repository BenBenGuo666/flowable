package com.demo.flowable.filter;

import com.demo.flowable.config.RateLimitConfig;
import com.demo.flowable.exception.RateLimitExceededException;
import com.demo.flowable.service.TokenService;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 速率限制过滤器
 * 基于用户 ID 进行 API 速率限制（每用户 200 次/分钟）
 *
 * @author e-Benben.Guo
 * @date 2025/11
 */
@Slf4j
@Component
@Order(1) // 优先级设置为 1，确保在安全过滤器之后执行
@RequiredArgsConstructor
public class RateLimitFilter implements WebFilter {

    private final RateLimitConfig rateLimitConfig;
    private final Cache<String, AtomicInteger> rateLimitCache;
    private final TokenService tokenService;

    /**
     * 白名单路径（不进行速率限制）
     */
    private static final String[] WHITE_LIST_PATHS = {
            "/api/auth/login",
            "/api/auth/refresh"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 如果速率限制未启用，直接放行
        if (!rateLimitConfig.isEnabled()) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();

        // 白名单路径不限流
        if (isWhiteListPath(path)) {
            return chain.filter(exchange);
        }

        // 从请求头中提取 Token
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            // 如果没有 Token，跳过速率限制（由 Spring Security 处理认证）
            return chain.filter(exchange);
        }

        String token = authorization.substring(7);

        try {
            // 从 Token 中提取用户 ID
            Long userId = tokenService.getUserIdFromToken(token);
            String userIdStr = userId.toString();

            // 检查速率限制
            AtomicInteger counter = rateLimitCache.get(userIdStr, key -> new AtomicInteger(0));

            if (counter == null) {
                counter = new AtomicInteger(0);
                rateLimitCache.put(userIdStr, counter);
            }

            int currentCount = counter.incrementAndGet();

            // 添加速率限制响应头
            exchange.getResponse().getHeaders().set("X-RateLimit-Limit",
                    String.valueOf(rateLimitConfig.getRequestsPerMinute()));
            exchange.getResponse().getHeaders().set("X-RateLimit-Remaining",
                    String.valueOf(rateLimitConfig.getRemainingRequests(currentCount)));
            exchange.getResponse().getHeaders().set("X-RateLimit-Reset",
                    String.valueOf(rateLimitConfig.getResetTimeSeconds()));

            // 检查是否超过限制
            if (currentCount > rateLimitConfig.getRequestsPerMinute()) {
                log.warn("用户 {} 请求超过速率限制: {}/{}", userId, currentCount,
                        rateLimitConfig.getRequestsPerMinute());

                // 返回 429 Too Many Requests
                return handleRateLimitExceeded(exchange, userIdStr);
            }

            log.debug("用户 {} 当前请求次数: {}/{}", userId, currentCount,
                    rateLimitConfig.getRequestsPerMinute());

            // 继续处理请求
            return chain.filter(exchange);

        } catch (Exception e) {
            log.error("速率限制检查失败: {}", e.getMessage());
            // 如果提取用户 ID 失败，跳过速率限制
            return chain.filter(exchange);
        }
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
     * 处理速率限制超限情况
     */
    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange, String userId) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().set("Retry-After",
                String.valueOf(rateLimitConfig.getResetTimeSeconds()));

        String errorResponse = String.format(
                "{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"data\":{\"userId\":\"%s\",\"limit\":%d,\"resetTime\":%d}}",
                userId,
                rateLimitConfig.getRequestsPerMinute(),
                rateLimitConfig.getResetTimeSeconds()
        );

        byte[] bytes = errorResponse.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
        );
    }
}
