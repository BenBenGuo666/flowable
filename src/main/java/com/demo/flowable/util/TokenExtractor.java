package com.demo.flowable.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Token 提取工具类
 * 用于从请求头中提取 JWT Token
 *
 * @author e-Benben.Guo
 * @date 2025/11
 */
@Slf4j
@Component
public class TokenExtractor {

    /**
     * Bearer Token 前缀
     */
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;

    /**
     * 从 ServerWebExchange 中提取 Token
     *
     * @param exchange ServerWebExchange
     * @return Mono<String> Token（不包含 Bearer 前缀）
     */
    public Mono<String> extractToken(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .flatMap(this::extractTokenFromAuthorizationHeader)
                .doOnNext(token -> log.debug("从请求头提取 Token 成功"))
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("请求头中未找到 Token");
                    return Mono.empty();
                }));
    }

    /**
     * 从 ServerHttpRequest 中提取 Token
     *
     * @param request ServerHttpRequest
     * @return Mono<String> Token（不包含 Bearer 前缀）
     */
    public Mono<String> extractToken(ServerHttpRequest request) {
        return Mono.justOrEmpty(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .flatMap(this::extractTokenFromAuthorizationHeader);
    }

    /**
     * 从 Authorization 请求头字符串中提取 Token
     *
     * @param authorization Authorization 请求头值
     * @return Mono<String> Token（不包含 Bearer 前缀）
     */
    public Mono<String> extractTokenFromAuthorizationHeader(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return Mono.empty();
        }

        // 检查是否以 "Bearer " 开头（不区分大小写）
        if (authorization.length() > BEARER_PREFIX_LENGTH &&
                authorization.substring(0, BEARER_PREFIX_LENGTH).equalsIgnoreCase(BEARER_PREFIX)) {
            String token = authorization.substring(BEARER_PREFIX_LENGTH).trim();

            if (token.isBlank()) {
                log.warn("Authorization 请求头格式错误：Bearer 后面没有 Token");
                return Mono.empty();
            }

            return Mono.just(token);
        }

        log.warn("Authorization 请求头格式错误：缺少 'Bearer ' 前缀");
        return Mono.empty();
    }

    /**
     * 从 Authorization 请求头字符串中提取 Token（同步方法）
     *
     * @param authorization Authorization 请求头值
     * @return Token（不包含 Bearer 前缀），如果格式错误返回 null
     */
    public String extractTokenSync(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        // 检查是否以 "Bearer " 开头（不区分大小写）
        if (authorization.length() > BEARER_PREFIX_LENGTH &&
                authorization.substring(0, BEARER_PREFIX_LENGTH).equalsIgnoreCase(BEARER_PREFIX)) {
            String token = authorization.substring(BEARER_PREFIX_LENGTH).trim();
            return token.isBlank() ? null : token;
        }

        return null;
    }

    /**
     * 检查请求头中是否包含 Token
     *
     * @param exchange ServerWebExchange
     * @return Mono<Boolean> 是否包含 Token
     */
    public Mono<Boolean> hasToken(ServerWebExchange exchange) {
        return extractToken(exchange)
                .map(token -> true)
                .defaultIfEmpty(false);
    }

    /**
     * 检查 Authorization 请求头格式是否正确
     *
     * @param authorization Authorization 请求头值
     * @return 是否格式正确
     */
    public boolean isValidAuthorizationHeader(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return false;
        }
        return authorization.length() > BEARER_PREFIX_LENGTH &&
                authorization.substring(0, BEARER_PREFIX_LENGTH).equalsIgnoreCase(BEARER_PREFIX) &&
                !authorization.substring(BEARER_PREFIX_LENGTH).trim().isBlank();
    }
}
