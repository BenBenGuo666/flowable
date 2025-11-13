package com.demo.flowable.util;

import com.demo.flowable.filter.JwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Security Context 工具类
 * 用于在业务代码中方便地获取当前认证用户的信息
 *
 * @author e-Benben.Guo
 * @date 2025/11
 */
@Slf4j
public class SecurityContextUtil {

    /**
     * 获取当前认证的用户名
     *
     * @return Mono<String> 用户名
     */
    public static Mono<String> getCurrentUsername() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .doOnNext(username -> log.debug("获取当前用户名: {}", username));
    }

    /**
     * 获取当前认证的用户 ID
     *
     * @return Mono<Long> 用户 ID
     */
    public static Mono<Long> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth instanceof UsernamePasswordAuthenticationToken)
                .map(auth -> (UsernamePasswordAuthenticationToken) auth)
                .map(auth -> {
                    Object details = auth.getDetails();
                    if (details instanceof JwtAuthenticationFilter.JwtUserDetails) {
                        return ((JwtAuthenticationFilter.JwtUserDetails) details).getUserId();
                    }
                    return null;
                })
                .doOnNext(userId -> log.debug("获取当前用户 ID: {}", userId));
    }

    /**
     * 获取当前认证用户的详细信息
     *
     * @return Mono<JwtUserDetails> 用户详情
     */
    public static Mono<JwtAuthenticationFilter.JwtUserDetails> getCurrentUserDetails() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth instanceof UsernamePasswordAuthenticationToken)
                .map(auth -> (UsernamePasswordAuthenticationToken) auth)
                .map(auth -> (JwtAuthenticationFilter.JwtUserDetails) auth.getDetails())
                .doOnNext(details -> log.debug("获取当前用户详情: userId={}, username={}",
                        details.getUserId(), details.getUsername()));
    }

    /**
     * 获取当前用户的权限列表
     *
     * @return Mono<List<String>> 权限列表
     */
    public static Mono<List<String>> getCurrentAuthorities() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(auth -> auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .doOnNext(authorities -> log.debug("获取当前用户权限: {}", authorities));
    }

    /**
     * 检查当前用户是否拥有指定权限
     *
     * @param authority 权限字符串
     * @return Mono<Boolean> 是否拥有权限
     */
    public static Mono<Boolean> hasAuthority(String authority) {
        return getCurrentAuthorities()
                .map(authorities -> authorities.contains(authority))
                .doOnNext(hasAuth -> log.debug("检查权限 '{}': {}", authority, hasAuth));
    }

    /**
     * 检查当前用户是否拥有指定角色
     *
     * @param role 角色字符串（无需 "ROLE_" 前缀）
     * @return Mono<Boolean> 是否拥有角色
     */
    public static Mono<Boolean> hasRole(String role) {
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return hasAuthority(roleWithPrefix);
    }

    /**
     * 获取当前认证对象
     *
     * @return Mono<Authentication> 认证对象
     */
    public static Mono<Authentication> getCurrentAuthentication() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication);
    }

    /**
     * 从 ServerWebExchange 中获取自定义属性
     * （由 JwtAuthenticationFilter 存储的自定义信息）
     *
     * @param exchange ServerWebExchange
     * @param key 属性键
     * @param type 属性类型
     * @param <T> 类型参数
     * @return 属性值，如果不存在返回 null
     */
    public static <T> T getAttribute(ServerWebExchange exchange, String key, Class<T> type) {
        Object value = exchange.getAttributes().get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    /**
     * 从 ServerWebExchange 中获取租户 ID
     *
     * @param exchange ServerWebExchange
     * @return 租户 ID，如果不存在返回 null
     */
    public static String getTenantId(ServerWebExchange exchange) {
        return getAttribute(exchange, "tenant_id", String.class);
    }

    /**
     * 从 ServerWebExchange 中获取设备 ID
     *
     * @param exchange ServerWebExchange
     * @return 设备 ID，如果不存在返回 null
     */
    public static String getDeviceId(ServerWebExchange exchange) {
        return getAttribute(exchange, "device_id", String.class);
    }

    /**
     * 从 ServerWebExchange 中获取客户端类型
     *
     * @param exchange ServerWebExchange
     * @return 客户端类型，如果不存在返回 null
     */
    public static String getClientType(ServerWebExchange exchange) {
        return getAttribute(exchange, "client_type", String.class);
    }

    /**
     * 检查用户是否已认证
     *
     * @return Mono<Boolean> 是否已认证
     */
    public static Mono<Boolean> isAuthenticated() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::isAuthenticated)
                .defaultIfEmpty(false);
    }
}
