# 自定义 JWT 认证过滤器扩展指南

## 📋 概述

本项目提供了一个**可扩展的自定义 JWT 认证过滤器**（`JwtAuthenticationFilter`），它完全替代了 Spring Security 的 OAuth 2.0 Resource Server，提供更灵活的令牌验证和自定义功能。

### 核心优势
- ✅ **完全可控**：所有验证逻辑都在你的代码中，易于调试和定制
- ✅ **4个扩展点**：preValidate、postValidate、extractAdditionalClaims、handleValidationError
- ✅ **易于扩展**：通过继承和重写方法即可添加自定义功能
- ✅ **无侵入性**：不影响现有的 Spring Security 配置

---

## 🏗️ 架构设计

### 核心类

| 类名 | 说明 | 路径 |
|------|------|------|
| `JwtAuthenticationFilter` | 基础 JWT 认证过滤器（可扩展） | `filter/JwtAuthenticationFilter.java` |
| `CustomJwtAuthenticationFilter` | 自定义扩展示例 | `filter/CustomJwtAuthenticationFilter.java` |
| `SecurityContextUtil` | 安全上下文工具类 | `util/SecurityContextUtil.java` |
| `TokenExtractor` | Token 提取工具 | `util/TokenExtractor.java` |

### 工作流程

```
客户端请求
    ↓
【请求头】Authorization: Bearer <token>
    ↓
┌─────────────────────────────────────────┐
│  1. RateLimitFilter (Order=1)           │
│     - 速率限制检查                       │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│  2. JwtAuthenticationFilter (Order=2)   │
│     ┌─────────────────────────────────┐ │
│     │ 【扩展点1】preValidate()         │ │
│     │ - IP 白名单检查                  │ │
│     │ - 设备验证                       │ │
│     │ - 时间段限制                     │ │
│     └─────────────────────────────────┘ │
│               ↓                          │
│     ┌─────────────────────────────────┐ │
│     │ Token 验证                       │ │
│     │ - 签名验证                       │ │
│     │ - 过期检查                       │ │
│     │ - 黑名单检查                     │ │
│     └─────────────────────────────────┘ │
│               ↓                          │
│     ┌─────────────────────────────────┐ │
│     │ 【扩展点3】extractAdditionalClaims() │
│     │ - 提取租户 ID                    │ │
│     │ - 提取部门 ID                    │ │
│     │ - 提取设备信息                   │ │
│     └─────────────────────────────────┘ │
│               ↓                          │
│     ┌─────────────────────────────────┐ │
│     │ 【扩展点2】postValidate()        │ │
│     │ - 更新最后活动时间               │ │
│     │ - 记录审计日志                   │ │
│     │ - Token 自动续期                 │ │
│     └─────────────────────────────────┘ │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│  3. SecurityContext                     │
│     - 存储 Authentication 对象           │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│  4. @PreAuthorize 权限检查              │
└─────────────────────────────────────────┘
    ↓
Controller 方法执行
```

---

## 🎯 4个扩展点详解

### 扩展点1：preValidate() - Token 验证前置处理

**触发时机：** Token 提取成功后，验证之前

**用途：**
- IP 白名单/黑名单检查
- 设备指纹验证
- 地理位置限制
- 时间段限制（如仅工作时间可访问）
- 请求来源验证
- 自定义请求头检查

**示例：**
```java
@Override
protected Mono<Void> preValidate(ServerWebExchange exchange, String token) {
    String remoteAddress = exchange.getRequest().getRemoteAddress()
            .getAddress().getHostAddress();

    // IP 黑名单检查
    if (isIpBlocked(remoteAddress)) {
        log.warn("IP 地址被封禁: {}", remoteAddress);
        return Mono.error(new RuntimeException("IP 地址被封禁"));
    }

    // 检查自定义请求头
    String deviceId = exchange.getRequest().getHeaders().getFirst("X-Device-ID");
    if (deviceId != null) {
        exchange.getAttributes().put("device_id", deviceId);
    }

    // 时间段限制（仅工作时间 9:00-18:00）
    LocalTime now = LocalTime.now();
    if (now.isBefore(LocalTime.of(9, 0)) || now.isAfter(LocalTime.of(18, 0))) {
        return Mono.error(new RuntimeException("仅工作时间（9:00-18:00）可访问"));
    }

    return Mono.empty();
}
```

---

### 扩展点2：postValidate() - Token 验证后置处理

**触发时机：** Token 验证成功后，请求继续之前

**用途：**
- 更新用户最后活动时间
- 记录登录日志/审计日志
- Token 自动续期
- 发送用户行为分析事件
- 存储自定义信息到 Exchange Attributes

**示例：**
```java
@Override
protected Mono<Void> postValidate(ServerWebExchange exchange, String token,
                                 UsernamePasswordAuthenticationToken authentication) {
    JwtUserDetails userDetails = (JwtUserDetails) authentication.getDetails();
    Long userId = userDetails.getUserId();
    String username = userDetails.getUsername();

    // 更新用户最后活动时间（异步）
    userActivityService.updateLastActiveTime(userId)
            .subscribe();

    // 检查 Token 是否即将过期
    long expirationSeconds = getTokenExpirationSeconds(token);
    if (expirationSeconds < 300) {  // 小于 5 分钟
        exchange.getResponse().getHeaders().set("X-Token-Expiring", "true");
        exchange.getResponse().getHeaders().set("X-Token-Expires-In",
                String.valueOf(expirationSeconds));
    }

    // 记录审计日志
    auditService.logAccess(userId, username,
            exchange.getRequest().getURI().getPath())
            .subscribe();

    // 存储用户信息到 Exchange Attributes
    exchange.getAttributes().put("current_user_id", userId);
    exchange.getAttributes().put("current_username", username);

    return Mono.empty();
}
```

---

### 扩展点3：extractAdditionalClaims() - 提取自定义 Claims

**触发时机：** Token 解析成功后

**用途：**
- 多租户系统：提取租户 ID
- 多设备登录：提取设备信息
- 角色扩展：提取自定义角色信息
- 业务标识：提取部门、组织等信息
- 客户端类型：提取 Web/Mobile/API 等

**示例：**
```java
@Override
protected void extractAdditionalClaims(ServerWebExchange exchange, Claims claims) {
    // 提取租户 ID（多租户系统）
    if (claims.containsKey("tenant_id")) {
        String tenantId = claims.get("tenant_id", String.class);
        log.info("租户 ID: {}", tenantId);
        exchange.getAttributes().put("tenant_id", tenantId);
    }

    // 提取部门 ID
    if (claims.containsKey("department_id")) {
        Long departmentId = claims.get("department_id", Long.class);
        exchange.getAttributes().put("department_id", departmentId);
    }

    // 提取用户角色
    if (claims.containsKey("roles")) {
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        exchange.getAttributes().put("user_roles", roles);
    }

    // 提取设备信息
    if (claims.containsKey("device_info")) {
        String deviceInfo = claims.get("device_info", String.class);
        exchange.getAttributes().put("device_info", deviceInfo);
    }
}
```

---

### 扩展点4：handleValidationError() - 自定义错误处理

**触发时机：** Token 验证失败时

**用途：**
- 自定义错误响应格式
- 记录错误日志到数据库
- 发送安全告警
- 根据错误类型返回不同响应

**示例：**
```java
@Override
protected Mono<Void> handleValidationError(ServerWebExchange exchange, Throwable error) {
    log.error("Token 验证失败: {}", error.getMessage());

    // 记录错误到数据库（异步）
    auditService.logSecurityError(exchange, error).subscribe();

    // 发送告警（如果是重要接口）
    String path = exchange.getRequest().getURI().getPath();
    if (path.contains("/admin") || path.contains("/critical")) {
        alertService.sendSecurityAlert(exchange, error).subscribe();
    }

    // 根据错误类型返回不同响应
    if (error.getMessage().contains("IP 地址被封禁")) {
        return respondWithError(exchange, HttpStatus.FORBIDDEN,
                "访问被拒绝：IP 地址受限");
    }

    // 调用父类的默认错误处理
    return super.handleValidationError(exchange, error);
}
```

---

## 🛠️ 如何扩展

### 方法1：继承 JwtAuthenticationFilter

**步骤：**

1. 创建自定义过滤器类
```java
@Slf4j
@Component  // 激活此自定义过滤器
public class MyCustomJwtFilter extends JwtAuthenticationFilter {

    public MyCustomJwtFilter(TokenService tokenService, TokenExtractor tokenExtractor) {
        super(tokenService, tokenExtractor);
    }

    // 重写需要自定义的方法
    @Override
    protected Mono<Void> preValidate(ServerWebExchange exchange, String token) {
        // 你的自定义逻辑
        return Mono.empty();
    }
}
```

2. 在配置类中注册（如果需要）
```java
// 如果使用 @Component 注解，Spring 会自动注册
// 如果需要手动注册，可以在 SecurityConfig 中配置
```

### 方法2：直接修改 JwtAuthenticationFilter

如果你的自定义逻辑比较简单，可以直接修改 `JwtAuthenticationFilter.java` 中的扩展点方法。

---

## 📚 使用工具类

### SecurityContextUtil - 获取当前用户信息

```java
// 在 Controller 或 Service 中使用

// 获取当前用户 ID
Mono<Long> userId = SecurityContextUtil.getCurrentUserId();

// 获取当前用户名
Mono<String> username = SecurityContextUtil.getCurrentUsername();

// 获取当前用户详情
Mono<JwtUserDetails> userDetails = SecurityContextUtil.getCurrentUserDetails();

// 获取当前用户权限
Mono<List<String>> authorities = SecurityContextUtil.getCurrentAuthorities();

// 检查权限
Mono<Boolean> hasPermission = SecurityContextUtil.hasAuthority("user:create");

// 检查角色
Mono<Boolean> hasRole = SecurityContextUtil.hasRole("ADMIN");
```

### 获取自定义 Attributes

```java
// 在 Controller 方法中

@GetMapping("/example")
public Mono<Result> example(ServerWebExchange exchange) {
    // 获取租户 ID
    String tenantId = SecurityContextUtil.getTenantId(exchange);

    // 获取设备 ID
    String deviceId = SecurityContextUtil.getDeviceId(exchange);

    // 获取客户端类型
    String clientType = SecurityContextUtil.getClientType(exchange);

    // 获取自定义属性
    Long userId = SecurityContextUtil.getAttribute(exchange, "current_user_id", Long.class);

    // 使用这些信息执行业务逻辑
    return Mono.just(Result.success("success"));
}
```

---

## 🎨 实际应用场景

### 场景1：多租户系统

**需求：** 每个请求都需要携带租户 ID，确保数据隔离

**实现：**

1. 在 Token 中添加租户 ID
```java
// TokenService.generateAccessToken()
claims.put("tenant_id", tenantId);
```

2. 在 JwtAuthenticationFilter 中提取
```java
@Override
protected void extractAdditionalClaims(ServerWebExchange exchange, Claims claims) {
    if (claims.containsKey("tenant_id")) {
        String tenantId = claims.get("tenant_id", String.class);
        exchange.getAttributes().put("tenant_id", tenantId);
    }
}
```

3. 在业务代码中使用
```java
@GetMapping("/data")
public Mono<Result> getData(ServerWebExchange exchange) {
    String tenantId = SecurityContextUtil.getTenantId(exchange);
    return dataService.getDataByTenant(tenantId)
            .map(Result::success);
}
```

---

### 场景2：设备绑定

**需求：** 限制每个账号只能在指定设备上登录

**实现：**

1. 登录时记录设备 ID
```java
// 在 Token 中添加设备 ID
claims.put("device_id", deviceId);
```

2. 验证设备 ID
```java
@Override
protected Mono<Void> preValidate(ServerWebExchange exchange, String token) {
    String requestDeviceId = exchange.getRequest().getHeaders().getFirst("X-Device-ID");

    // 从 Token 中获取设备 ID（需要先解析 Token）
    Claims claims = tokenService.parseToken(token);
    String tokenDeviceId = claims.get("device_id", String.class);

    if (!tokenDeviceId.equals(requestDeviceId)) {
        return Mono.error(new RuntimeException("设备不匹配"));
    }

    return Mono.empty();
}
```

---

### 场景3：IP 白名单

**需求：** 管理员接口只允许特定 IP 访问

**实现：**

```java
@Override
protected Mono<Void> preValidate(ServerWebExchange exchange, String token) {
    String path = exchange.getRequest().getURI().getPath();

    // 管理员接口检查 IP 白名单
    if (path.startsWith("/api/admin")) {
        String remoteAddress = exchange.getRequest().getRemoteAddress()
                .getAddress().getHostAddress();

        List<String> allowedIps = List.of("192.168.1.100", "10.0.0.1");
        if (!allowedIps.contains(remoteAddress)) {
            return Mono.error(new RuntimeException("IP 不在白名单中"));
        }
    }

    return Mono.empty();
}
```

---

### 场景4：时间段限制

**需求：** 某些接口只在工作时间可访问

**实现：**

```java
@Override
protected Mono<Void> preValidate(ServerWebExchange exchange, String token) {
    String path = exchange.getRequest().getURI().getPath();

    // 财务接口只在工作时间可访问
    if (path.startsWith("/api/finance")) {
        LocalTime now = LocalTime.now();
        if (now.isBefore(LocalTime.of(9, 0)) || now.isAfter(LocalTime.of(18, 0))) {
            return Mono.error(new RuntimeException("财务接口仅工作时间（9:00-18:00）可访问"));
        }
    }

    return Mono.empty();
}
```

---

### 场景5：审计日志

**需求：** 记录所有 API 访问日志

**实现：**

```java
@Override
protected Mono<Void> postValidate(ServerWebExchange exchange, String token,
                                 UsernamePasswordAuthenticationToken authentication) {
    JwtUserDetails userDetails = (JwtUserDetails) authentication.getDetails();

    // 记录审计日志（异步）
    auditService.logAccess(
        userDetails.getUserId(),
        userDetails.getUsername(),
        exchange.getRequest().getMethod().toString(),
        exchange.getRequest().getURI().getPath(),
        exchange.getRequest().getRemoteAddress().getAddress().getHostAddress(),
        LocalDateTime.now()
    ).subscribe();

    return Mono.empty();
}
```

---

## 🧪 测试

### 测试自定义功能

```bash
# 1. 测试 IP 白名单
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer <token>"
# 预期：如果 IP 不在白名单，返回 403

# 2. 测试设备绑定
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer <token>" \
  -H "X-Device-ID: device-123"
# 预期：设备 ID 匹配，返回 200

# 3. 测试时间段限制
curl -X GET http://localhost:8080/api/finance/report \
  -H "Authorization: Bearer <token>"
# 预期：非工作时间，返回错误

# 4. 测试自定义 Claims
curl -X GET http://localhost:8080/api/custom-auth-example/custom-claims \
  -H "Authorization: Bearer <token>"
# 预期：返回租户 ID、设备 ID 等自定义信息
```

---

## 📁 文件清单

| 文件 | 说明 |
|------|------|
| `filter/JwtAuthenticationFilter.java` | 基础 JWT 认证过滤器（4个扩展点） |
| `filter/CustomJwtAuthenticationFilter.java` | 自定义扩展示例（完整实现所有扩展点） |
| `util/SecurityContextUtil.java` | 安全上下文工具类（获取当前用户信息） |
| `util/TokenExtractor.java` | Token 提取工具 |
| `controller/CustomAuthExampleController.java` | 使用示例 Controller |

---

## 🎯 最佳实践

1. **扩展点保持简洁**
   - 扩展点方法应该快速执行，避免耗时操作
   - 异步操作使用 `subscribe()` 而不是阻塞

2. **合理使用 Exchange Attributes**
   - 将提取的自定义信息存储到 Exchange Attributes
   - 使用 SecurityContextUtil 统一访问

3. **日志记录**
   - 关键步骤记录日志，方便调试和审计
   - 使用不同的日志级别（DEBUG、INFO、WARN、ERROR）

4. **错误处理**
   - 自定义错误处理时，提供清晰的错误信息
   - 记录错误日志，方便排查问题

5. **性能考虑**
   - 避免在扩展点中进行数据库查询（除非必要）
   - 使用缓存减少重复查询

---

## 📞 技术支持

如有问题，请联系：
- **作者：** e-Benben.Guo
- **日期：** 2025/11
- **项目：** Flowable UI System

---

**🎉 现在你可以自由扩展 JWT 认证功能了！**
