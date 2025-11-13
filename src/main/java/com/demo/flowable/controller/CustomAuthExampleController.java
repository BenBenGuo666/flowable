package com.demo.flowable.controller;

import com.demo.flowable.common.Result;
import com.demo.flowable.filter.JwtAuthenticationFilter;
import com.demo.flowable.util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义认证功能使用示例 Controller
 * 展示如何使用自定义的 JWT 认证过滤器和工具类
 *
 * @author e-Benben.Guo
 * @date 2025/11
 */
@Slf4j
@RestController
@RequestMapping("/api/custom-auth-example")
@RequiredArgsConstructor
public class CustomAuthExampleController {

    /**
     * 示例1：获取当前用户信息
     * GET /api/custom-auth-example/current-user
     */
    @GetMapping("/current-user")
    public Mono<Result<Map<String, Object>>> getCurrentUser() {
        log.info("获取当前用户信息");

        return SecurityContextUtil.getCurrentUserDetails()
                .map(userDetails -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("userId", userDetails.getUserId());
                    data.put("username", userDetails.getUsername());

                    // 获取自定义 Claims
                    if (userDetails.getClaims().containsKey("tenant_id")) {
                        data.put("tenantId", userDetails.getCustomClaim("tenant_id", String.class));
                    }

                    log.info("当前用户: userId={}, username={}", userDetails.getUserId(), userDetails.getUsername());
                    return Result.success("获取成功", data);
                })
                .switchIfEmpty(Mono.just(Result.error(401, "未认证")));
    }

    /**
     * 示例2：获取当前用户权限
     * GET /api/custom-auth-example/authorities
     */
    @GetMapping("/authorities")
    public Mono<Result<Map<String, Object>>> getCurrentAuthorities() {
        log.info("获取当前用户权限");

        return SecurityContextUtil.getCurrentAuthorities()
                .flatMap(authorities -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("authorities", authorities);
                    data.put("count", authorities.size());

                    log.info("当前用户权限: {}", authorities);
                    return Mono.just(Result.success("获取成功", data));
                });
    }

    /**
     * 示例3：检查权限
     * GET /api/custom-auth-example/check-permission?permission=user:create
     */
    @GetMapping("/check-permission")
    public Mono<Result<Map<String, Object>>> checkPermission(@RequestParam String permission) {
        log.info("检查权限: {}", permission);

        return SecurityContextUtil.hasAuthority(permission)
                .map(hasPermission -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("permission", permission);
                    data.put("hasPermission", hasPermission);

                    log.info("权限检查结果: {} -> {}", permission, hasPermission);
                    return Result.success("检查完成", data);
                });
    }

    /**
     * 示例4：获取自定义 Claims（从 Exchange Attributes）
     * GET /api/custom-auth-example/custom-claims
     */
    @GetMapping("/custom-claims")
    public Mono<Result<Map<String, Object>>> getCustomClaims(ServerWebExchange exchange) {
        log.info("获取自定义 Claims");

        Map<String, Object> data = new HashMap<>();

        // 从 Exchange Attributes 中获取自定义信息
        String tenantId = SecurityContextUtil.getTenantId(exchange);
        String deviceId = SecurityContextUtil.getDeviceId(exchange);
        String clientType = SecurityContextUtil.getClientType(exchange);

        if (tenantId != null) {
            data.put("tenantId", tenantId);
        }
        if (deviceId != null) {
            data.put("deviceId", deviceId);
        }
        if (clientType != null) {
            data.put("clientType", clientType);
        }

        // 获取所有自定义属性
        Long currentUserId = SecurityContextUtil.getAttribute(exchange, "current_user_id", Long.class);
        String currentUsername = SecurityContextUtil.getAttribute(exchange, "current_username", String.class);

        if (currentUserId != null) {
            data.put("currentUserId", currentUserId);
        }
        if (currentUsername != null) {
            data.put("currentUsername", currentUsername);
        }

        log.info("自定义 Claims: {}", data);
        return Mono.just(Result.success("获取成功", data));
    }

    /**
     * 示例5：组合使用 - 获取完整的用户上下文信息
     * GET /api/custom-auth-example/full-context
     */
    @GetMapping("/full-context")
    public Mono<Result<Map<String, Object>>> getFullContext(ServerWebExchange exchange) {
        log.info("获取完整的用户上下文信息");

        return SecurityContextUtil.getCurrentUserDetails()
                .flatMap(userDetails ->
                        SecurityContextUtil.getCurrentAuthorities()
                                .map(authorities -> {
                                    Map<String, Object> data = new HashMap<>();

                                    // 基本信息
                                    data.put("userId", userDetails.getUserId());
                                    data.put("username", userDetails.getUsername());
                                    data.put("authorities", authorities);

                                    // 自定义 Claims
                                    Map<String, Object> customClaims = new HashMap<>();
                                    String tenantId = SecurityContextUtil.getTenantId(exchange);
                                    String deviceId = SecurityContextUtil.getDeviceId(exchange);
                                    String clientType = SecurityContextUtil.getClientType(exchange);

                                    if (tenantId != null) customClaims.put("tenantId", tenantId);
                                    if (deviceId != null) customClaims.put("deviceId", deviceId);
                                    if (clientType != null) customClaims.put("clientType", clientType);

                                    data.put("customClaims", customClaims);

                                    // 请求信息
                                    Map<String, Object> requestInfo = new HashMap<>();
                                    requestInfo.put("path", exchange.getRequest().getURI().getPath());
                                    requestInfo.put("method", exchange.getRequest().getMethod().toString());
                                    requestInfo.put("remoteAddress",
                                            exchange.getRequest().getRemoteAddress() != null
                                                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                                                    : "unknown");
                                    data.put("requestInfo", requestInfo);

                                    log.info("完整用户上下文: {}", data);
                                    return Result.success("获取成功", data);
                                })
                );
    }

    /**
     * 示例6：根据用户 ID 执行业务逻辑
     * POST /api/custom-auth-example/user-action
     */
    @PostMapping("/user-action")
    public Mono<Result<Map<String, Object>>> performUserAction(@RequestBody Map<String, Object> requestData) {
        log.info("执行用户操作");

        return SecurityContextUtil.getCurrentUserId()
                .flatMap(userId -> SecurityContextUtil.getCurrentUsername()
                        .map(username -> {
                            // 使用当前用户信息执行业务逻辑
                            Map<String, Object> result = new HashMap<>();
                            result.put("operatorUserId", userId);
                            result.put("operatorUsername", username);
                            result.put("action", "业务操作");
                            result.put("requestData", requestData);
                            result.put("timestamp", System.currentTimeMillis());

                            log.info("用户 {} ({}) 执行了操作", username, userId);
                            return Result.success("操作成功", result);
                        })
                );
    }

    /**
     * 示例7：权限检查后执行操作
     * DELETE /api/custom-auth-example/protected-resource/{id}
     */
    @DeleteMapping("/protected-resource/{id}")
    public Mono<Result<Map<String, Object>>> deleteProtectedResource(@PathVariable Long id) {
        log.info("删除受保护资源: {}", id);

        // 先检查权限
        return SecurityContextUtil.hasAuthority("resource:delete")
                .flatMap(hasPermission -> {
                    if (!hasPermission) {
                        log.warn("用户无删除权限");
                        return Mono.just(Result.error(403, "权限不足"));
                    }

                    // 有权限，执行删除操作
                    return SecurityContextUtil.getCurrentUserId()
                            .map(userId -> {
                                Map<String, Object> result = new HashMap<>();
                                result.put("resourceId", id);
                                result.put("deletedBy", userId);
                                result.put("deleted", true);
                                result.put("timestamp", System.currentTimeMillis());

                                log.info("用户 {} 删除了资源 {}", userId, id);
                                return Result.success("删除成功", result);
                            });
                });
    }

    /**
     * 示例8：多租户场景 - 根据租户 ID 过滤数据
     * GET /api/custom-auth-example/tenant-data
     */
    @GetMapping("/tenant-data")
    public Mono<Result<Map<String, Object>>> getTenantData(ServerWebExchange exchange) {
        log.info("获取租户数据");

        String tenantId = SecurityContextUtil.getTenantId(exchange);

        if (tenantId == null) {
            return Mono.just(Result.error(400, "缺少租户信息"));
        }

        // 根据租户 ID 查询数据（示例）
        Map<String, Object> tenantData = new HashMap<>();
        tenantData.put("tenantId", tenantId);
        tenantData.put("data", List.of("数据1", "数据2", "数据3"));
        tenantData.put("count", 3);

        log.info("获取租户 {} 的数据", tenantId);
        return Mono.just(Result.success("获取成功", tenantData));
    }
}
