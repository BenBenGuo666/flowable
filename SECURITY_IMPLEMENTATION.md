# Spring Security OAuth 2.0 令牌权限校验实施文档

## 📋 实施概述

本项目基于 **Spring Security OAuth 2.0 Resource Server** + **JWT** 实现了完整的令牌校验和权限控制系统。

### 核心功能
- ✅ **令牌校验**：所有接口（除登录/刷新）都需要有效的 Bearer Token
- ✅ **细粒度权限控制**：基于 `@PreAuthorize` 注解的方法级权限控制
- ✅ **速率限制**：每用户 200 次/分钟的请求频率限制
- ✅ **Token 黑名单**：支持登出和 Token 废除
- ✅ **全局异常处理**：统一的错误响应格式

---

## 🔐 权限设计

### 权限常量定义
所有权限定义在 `PermissionConstant.java` 中：

```java
// 用户管理权限
user:create      - 创建用户
user:update      - 更新用户
user:delete      - 删除用户
user:view        - 查看用户
user:assign_role - 分配角色

// 角色管理权限
role:create      - 创建角色
role:update      - 更新角色
role:delete      - 删除角色
role:view        - 查看角色
role:assign_permission - 分配权限

// 权限管理
permission:create - 创建权限
permission:update - 更新权限
permission:delete - 删除权限
permission:view   - 查看权限

// ... 更多权限请查看 PermissionConstant.java
```

### 预定义角色
```java
ROLE_ADMIN         - 超级管理员（拥有所有权限）
ROLE_USER          - 普通用户
ROLE_PROCESS_ADMIN - 流程管理员
```

---

## ⚙️ 配置说明

### application.yml 配置

```yaml
# JWT 配置
jwt:
  secret: flowable-secret-key-for-jwt-token-generation-must-be-at-least-256-bits
  access-token-expiration: 3600000    # Access Token 过期时间（1小时）
  refresh-token-expiration: 604800000 # Refresh Token 过期时间（7天）
  issuer: flowable-auth-server

# 速率限制配置
rate-limit:
  enabled: true                        # 是否启用速率限制
  requests-per-minute: 200             # 每用户每分钟最大请求次数
  time-window-minutes: 1               # 时间窗口（分钟）
  cache-max-size: 10000                # 缓存最大用户数

# CORS 配置（已在 SecurityConfig 中配置）
# 允许的前端地址：
# - http://localhost:5173
# - http://localhost:5174
# - http://localhost:3000
```

### 生产环境建议

1. **修改 JWT 密钥**
   ```yaml
   jwt:
     secret: ${JWT_SECRET}  # 使用环境变量，生成强密钥（至少 256 位）
   ```

2. **调整 Token 过期时间**
   ```yaml
   jwt:
     access-token-expiration: 1800000   # 30分钟（更安全）
     refresh-token-expiration: 2592000000 # 30天
   ```

3. **配置 CORS 白名单**
   ```java
   // 在 SecurityConfig.corsConfigurationSource() 中修改
   configuration.setAllowedOrigins(Arrays.asList(
       "https://your-production-domain.com"
   ));
   ```

4. **启用 HTTPS**
   - 生产环境必须使用 HTTPS
   - 配置 SSL 证书

---

## 🚀 使用指南

### 1. 用户登录

**请求：**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }'
```

**响应：**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "authorities": ["user:create", "user:update", "user:delete", "user:view"]
  }
}
```

### 2. 访问受保护的接口

所有接口（除 `/api/auth/login` 和 `/api/auth/refresh`）都需要在请求头中携带 Token：

```bash
curl -X GET http://localhost:8080/api/user/list \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 3. 权限控制示例

#### 示例1：创建用户（需要 user:create 权限）
```bash
curl -X POST http://localhost:8080/api/user \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "password",
    "email": "newuser@example.com"
  }'
```

**成功响应（有权限）：**
```json
{
  "code": 200,
  "message": "用户创建成功",
  "data": 2
}
```

**失败响应（无权限）：**
```json
{
  "error": "access_denied",
  "error_description": "权限不足: Access Denied"
}
```
HTTP 状态码：`403 Forbidden`

#### 示例2：删除用户（需要 user:delete 权限）
```bash
curl -X DELETE http://localhost:8080/api/user/2 \
  -H "Authorization: Bearer <token>"
```

### 4. 速率限制

每个用户每分钟最多 200 次请求。超过限制后：

**响应：**
```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后再试",
  "data": {
    "userId": "1",
    "limit": 200,
    "resetTime": 60
  }
}
```
HTTP 状态码：`429 Too Many Requests`

**响应头：**
```
X-RateLimit-Limit: 200
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 60
Retry-After: 60
```

### 5. 刷新 Token

当 Access Token 即将过期时，使用 Refresh Token 获取新的 Token：

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }'
```

**响应：** 返回新的 Access Token 和 Refresh Token

### 6. 登出

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <access_token>"
```

登出后，该 Token 会被加入黑名单，无法再使用。

---

## 🛠️ 开发指南

### 1. 为新接口添加权限控制

**步骤1：** 在 `PermissionConstant.java` 中定义权限常量
```java
public static final String REPORT_VIEW = "report:view";
public static final String REPORT_EXPORT = "report:export";
```

**步骤2：** 在 Controller 方法上添加 `@PreAuthorize` 注解
```java
@GetMapping("/report/list")
@PreAuthorize("hasAuthority('" + PermissionConstant.REPORT_VIEW + "')")
public Result<List<Report>> getReportList() {
    // ...
}
```

**步骤3：** 在数据库中为角色分配权限
```sql
-- 为管理员角色添加报表查看权限
INSERT INTO role_permission (role_id, permission_code)
VALUES (1, 'report:view');
```

### 2. 自定义权限校验逻辑

如果需要更复杂的权限校验（如：只能查看自己的数据），可以使用 SpEL 表达式：

```java
@GetMapping("/user/{id}")
@PreAuthorize("hasAuthority('user:view') or #id == authentication.principal.id")
public Result<UserDTO> getUserById(@PathVariable Long id) {
    // 有 user:view 权限，或者查询的是自己的信息
}
```

### 3. 角色级别的权限控制

```java
@PostMapping("/admin/settings")
@PreAuthorize("hasRole('ADMIN')")  // 只有 ROLE_ADMIN 角色可以访问
public Result<Void> updateSettings(@RequestBody Settings settings) {
    // ...
}
```

### 4. 组合权限控制

```java
@DeleteMapping("/critical-data/{id}")
@PreAuthorize("hasAuthority('data:delete') and hasRole('ADMIN')")
public Result<Void> deleteCriticalData(@PathVariable Long id) {
    // 必须同时拥有 data:delete 权限和 ADMIN 角色
}
```

---

## 📊 接口保护状态

### 白名单（无需 Token）
```
POST /api/auth/login       - 用户登录
POST /api/auth/refresh     - 刷新 Token
```

### 需要 Token 但无权限限制
```
POST /api/auth/logout      - 登出
GET  /api/auth/me          - 获取当前用户信息
```

### 需要 Token + 权限控制
```
用户管理接口 (/api/user/**)
- POST   /api/user                → user:create
- PUT    /api/user/{id}           → user:update
- DELETE /api/user/{id}           → user:delete
- GET    /api/user/{id}           → user:view
- GET    /api/user/list           → user:view
- POST   /api/user/{id}/roles     → user:assign_role

角色管理接口 (/api/role/**)       → 需要添加对应的 role:* 权限
权限管理接口 (/api/permission/**) → 需要添加对应的 permission:* 权限
流程管理接口 (/api/process-**/**, /api/task/**) → 目前需要 Token，可添加 process:* 权限
表单管理接口 (/api/form-**)       → 目前需要 Token，可添加 form:* 权限
请假接口 (/api/leave/**)          → 目前需要 Token，可添加 leave:* 权限
```

### 禁用接口
```
/api/init/**               - 初始化接口（已禁用）
```

---

## 🧪 测试

### 1. 测试无 Token 访问
```bash
curl -X GET http://localhost:8080/api/user/list
# 预期：401 Unauthorized
```

### 2. 测试有效 Token 但无权限
```bash
# 假设当前用户没有 user:delete 权限
curl -X DELETE http://localhost:8080/api/user/999 \
  -H "Authorization: Bearer <token>"
# 预期：403 Forbidden
```

### 3. 测试速率限制
```bash
# 快速发送 201 次请求
for i in {1..201}; do
  curl -X GET http://localhost:8080/api/user/list \
    -H "Authorization: Bearer <token>"
done
# 预期：第 201 次请求返回 429 Too Many Requests
```

### 4. 测试 Token 黑名单
```bash
# 1. 登出
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <token>"

# 2. 使用同一 Token 访问接口
curl -X GET http://localhost:8080/api/user/list \
  -H "Authorization: Bearer <token>"
# 预期：403 Forbidden（Token 已被废除）
```

---

## 🔧 故障排查

### 问题1：提示 "Token 已被废除"

**原因：** Token 在黑名单中

**解决方案：**
- 使用 Refresh Token 获取新的 Access Token
- 或重新登录

### 问题2：提示 "权限不足"

**原因：** 用户没有对应的权限

**解决方案：**
1. 检查用户的权限列表（调用 `/api/auth/me`）
2. 为用户分配对应的权限
3. 或为用户分配拥有该权限的角色

### 问题3：速率限制过于严格

**解决方案：**
修改 `application.yml` 配置：
```yaml
rate-limit:
  requests-per-minute: 500  # 增加限制次数
  time-window-minutes: 1    # 或延长时间窗口
```

### 问题4：CORS 错误

**解决方案：**
在 `SecurityConfig.corsConfigurationSource()` 中添加你的前端地址：
```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:5173",
    "http://your-frontend-domain.com"
));
```

---

## 📁 核心文件清单

| 文件路径 | 说明 |
|---------|------|
| `config/SecurityConfig.java` | Spring Security 主配置 |
| `config/JwtConfig.java` | JWT 配置 |
| `config/RateLimitConfig.java` | 速率限制配置 |
| `constant/PermissionConstant.java` | 权限常量定义 |
| `filter/RateLimitFilter.java` | 速率限制过滤器 |
| `service/TokenService.java` | Token 生成和验证 |
| `service/TokenBlacklistService.java` | Token 黑名单管理 |
| `service/TokenValidationService.java` | Token 校验服务 |
| `exception/GlobalExceptionHandler.java` | 全局异常处理 |
| `exception/RateLimitExceededException.java` | 速率限制异常 |
| `controller/OAuth2AuthController.java` | 认证接口 |

---

## 🎯 后续优化建议

1. **集群部署优化**
   - 将 Token 黑名单从内存改为 Redis
   - 速率限制也使用 Redis 实现

2. **监控和日志**
   - 集成 Spring Boot Actuator
   - 监控 API 调用频率、错误率
   - 记录权限拒绝日志

3. **权限管理界面**
   - 开发权限管理前端页面
   - 支持动态分配权限

4. **OAuth 2.0 完整流程**
   - 支持授权码模式
   - 支持第三方登录（如 GitHub、Google）

---

## 📞 技术支持

如有问题，请联系：
- **作者：** e-Benben.Guo
- **日期：** 2025/11
- **项目：** Flowable UI System

---

**🎉 恭喜！你的系统已经具备完整的 OAuth 2.0 令牌校验和权限控制能力！**
