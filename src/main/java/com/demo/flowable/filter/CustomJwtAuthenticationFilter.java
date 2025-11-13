package com.demo.flowable.filter;

import com.demo.flowable.service.TokenService;
import com.demo.flowable.util.TokenExtractor;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 自定义 JWT 认证过滤器示例
 * 演示如何扩展 JwtAuthenticationFilter 添加自定义功能
 *
 * 使用方法：
 * 1. 继承 JwtAuthenticationFilter
 * 2. 重写需要自定义的方法（preValidate、postValidate、extractAdditionalClaims、handleValidationError）
 * 3. 在 @Component 注解中保持激活，或者在 SecurityConfig 中手动注册
 *
 * @author e-Benben.Guo
 * @date 2025/11
 */
@Slf4j
// @Component  // 取消注释以激活此自定义过滤器（会替代默认的 JwtAuthenticationFilter）
public class CustomJwtAuthenticationFilter extends JwtAuthenticationFilter {

    public CustomJwtAuthenticationFilter(TokenService tokenService, TokenExtractor tokenExtractor) {
        super(tokenService, tokenExtractor);
    }

    /**
     * 【自定义扩展1】Token 验证前的前置处理
     *
     * 示例用途：
     * - IP 白名单检查
     * - 设备指纹验证
     * - 地理位置限制
     * - 时间段限制（如仅工作时间可访问）
     */
    @Override
    protected Mono<Void> preValidate(ServerWebExchange exchange, String token) {
        log.info("=== 自定义前置处理开始 ===");

        String path = exchange.getRequest().getURI().getPath();
        String remoteAddress = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        log.info("请求路径: {}", path);
        log.info("客户端 IP: {}", remoteAddress);

        // 示例1：IP 白名单检查
        if (isIpBlocked(remoteAddress)) {
            log.warn("IP 地址被封禁: {}", remoteAddress);
            return Mono.error(new RuntimeException("IP 地址被封禁"));
        }

        // 示例2：检查 User-Agent
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
        log.info("User-Agent: {}", userAgent);

        // 示例3：检查自定义请求头
        String deviceId = exchange.getRequest().getHeaders().getFirst("X-Device-ID");
        if (deviceId != null) {
            log.info("设备 ID: {}", deviceId);
            exchange.getAttributes().put("device_id", deviceId);
        }

        // 示例4：时间段限制（仅工作时间 9:00-18:00 可访问）
        /*
        LocalTime now = LocalTime.now();
        if (now.isBefore(LocalTime.of(9, 0)) || now.isAfter(LocalTime.of(18, 0))) {
            log.warn("非工作时间访问: {}", now);
            return Mono.error(new RuntimeException("仅工作时间（9:00-18:00）可访问"));
        }
        */

        log.info("=== 自定义前置处理完成 ===");
        return Mono.empty();
    }

    /**
     * 【自定义扩展2】Token 验证后的后置处理
     *
     * 示例用途：
     * - 更新用户最后活动时间
     * - 记录审计日志
     * - Token 自动续期
     * - 发送用户行为分析事件
     */
    @Override
    protected Mono<Void> postValidate(ServerWebExchange exchange, String token,
                                     UsernamePasswordAuthenticationToken authentication) {
        log.info("=== 自定义后置处理开始 ===");

        JwtUserDetails userDetails = (JwtUserDetails) authentication.getDetails();
        Long userId = userDetails.getUserId();
        String username = userDetails.getUsername();

        log.info("认证成功 - 用户 ID: {}, 用户名: {}", userId, username);

        // 示例1：记录用户最后活动时间
        recordUserActivity(userId, exchange.getRequest().getURI().getPath());

        // 示例2：检查 Token 是否即将过期，自动刷新
        long expirationSeconds = getTokenExpirationSeconds(token);
        if (expirationSeconds < 300) {  // 小于 5 分钟
            log.warn("Token 即将过期（{}秒），建议刷新", expirationSeconds);
            // 可以在响应头中添加提示
            exchange.getResponse().getHeaders().set("X-Token-Expiring", "true");
            exchange.getResponse().getHeaders().set("X-Token-Expires-In", String.valueOf(expirationSeconds));
        }

        // 示例3：记录审计日志
        logAuditTrail(userId, username, exchange);

        // 示例4：将用户信息存储到 Exchange Attributes（供后续使用）
        exchange.getAttributes().put("current_user_id", userId);
        exchange.getAttributes().put("current_username", username);

        log.info("=== 自定义后置处理完成 ===");
        return Mono.empty();
    }

    /**
     * 【自定义扩展3】从 Token 中提取额外的自定义 Claims
     *
     * 示例用途：
     * - 多租户系统：提取租户 ID
     * - 多设备登录：提取设备信息
     * - 角色扩展：提取自定义角色信息
     * - 业务标识：提取部门、组织等信息
     */
    @Override
    protected void extractAdditionalClaims(ServerWebExchange exchange, Claims claims) {
        log.info("=== 提取自定义 Claims ===");

        // 示例1：提取租户 ID（多租户系统）
        if (claims.containsKey("tenant_id")) {
            String tenantId = claims.get("tenant_id", String.class);
            log.info("租户 ID: {}", tenantId);
            exchange.getAttributes().put("tenant_id", tenantId);
        }

        // 示例2：提取部门 ID
        if (claims.containsKey("department_id")) {
            Long departmentId = claims.get("department_id", Long.class);
            log.info("部门 ID: {}", departmentId);
            exchange.getAttributes().put("department_id", departmentId);
        }

        // 示例3：提取用户角色（除了 authorities）
        if (claims.containsKey("roles")) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles");
            log.info("用户角色: {}", roles);
            exchange.getAttributes().put("user_roles", roles);
        }

        // 示例4：提取设备信息
        if (claims.containsKey("device_info")) {
            String deviceInfo = claims.get("device_info", String.class);
            log.info("设备信息: {}", deviceInfo);
            exchange.getAttributes().put("device_info", deviceInfo);
        }

        // 示例5：提取客户端类型（Web、Mobile、API）
        if (claims.containsKey("client_type")) {
            String clientType = claims.get("client_type", String.class);
            log.info("客户端类型: {}", clientType);
            exchange.getAttributes().put("client_type", clientType);
        }

        log.info("=== 自定义 Claims 提取完成 ===");
    }

    /**
     * 【自定义扩展4】自定义错误处理
     *
     * 示例用途：
     * - 不同类型的错误返回不同的响应
     * - 记录错误日志到数据库或监控系统
     * - 发送告警通知
     */
    @Override
    protected Mono<Void> handleValidationError(ServerWebExchange exchange, Throwable error) {
        log.error("=== Token 验证失败，执行自定义错误处理 ===");
        log.error("错误信息: {}", error.getMessage());
        log.error("请求路径: {}", exchange.getRequest().getURI().getPath());

        // 示例1：记录错误到数据库（可以异步执行）
        // auditService.logSecurityError(exchange, error);

        // 示例2：发送告警（如果是重要接口）
        String path = exchange.getRequest().getURI().getPath();
        if (path.contains("/admin") || path.contains("/critical")) {
            log.warn("重要接口访问失败，发送告警！");
            // alertService.sendSecurityAlert(exchange, error);
        }

        // 示例3：根据错误类型返回不同的提示信息
        if (error.getMessage().contains("IP 地址被封禁")) {
            log.error("IP 被封禁");
            // 可以返回特殊的错误码
        }

        // 调用父类的默认错误处理
        return super.handleValidationError(exchange, error);
    }

    // ==================== 辅助方法 ====================

    /**
     * 检查 IP 是否被封禁
     */
    private boolean isIpBlocked(String ip) {
        // 示例：检查 IP 黑名单
        // 实际使用时可以从数据库或缓存中查询
        List<String> blockedIps = List.of("127.0.0.2", "192.168.1.100");
        return blockedIps.contains(ip);
    }

    /**
     * 记录用户活动
     */
    private void recordUserActivity(Long userId, String path) {
        log.debug("记录用户活动: userId={}, path={}", userId, path);
        // 实际使用时可以异步写入数据库
        // userActivityService.recordActivity(userId, path, LocalDateTime.now());
    }

    /**
     * 获取 Token 剩余有效时间（秒）
     */
    private long getTokenExpirationSeconds(String token) {
        try {
            // 这里应该调用 TokenService 的方法
            // 简化示例，返回固定值
            return 3600;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 记录审计日志
     */
    private void logAuditTrail(Long userId, String username, ServerWebExchange exchange) {
        String method = exchange.getRequest().getMethod().toString();
        String path = exchange.getRequest().getURI().getPath();
        String remoteAddress = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        log.info("审计日志: userId={}, username={}, method={}, path={}, ip={}",
                userId, username, method, path, remoteAddress);

        // 实际使用时可以写入数据库
        // auditService.log(userId, username, method, path, remoteAddress, LocalDateTime.now());
    }
}
