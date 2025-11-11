# Spring Security OAuth 2.0 认证系统实现指南

## 项目概述

本项目实现了一个完整的基于 ReactiveUserDetailsService 的 Spring Security OAuth 2.0 认证系统，使用响应式编程（WebFlux + Reactor）。

### 核心特性

1. **响应式架构**：完全基于 Spring WebFlux 的非阻塞式架构
2. **JWT 认证**：使用 JWT（JSON Web Token）进行无状态认证
3. **OAuth 2.0 标准**：符合 OAuth 2.0 规范的 Token 响应格式
4. **Token 刷新机制**：支持 Refresh Token 自动刷新 Access Token
5. **Token 黑名单**：支持主动登出和 Token 废除
6. **自动清理**：定时清理过期的黑名单记录
7. **全局异常处理**：统一的异常处理机制

## 架构设计

### 目录结构

```
com.demo.flowable
├── auth
│   └── ReactiveUserService.java          # 响应式用户详情服务
├── config
│   ├── SecurityConfig.java               # Security 配置
│   └── JwtConfig.java                    # JWT 配置
├── controller
│   └── OAuth2AuthController.java         # 认证控制器
├── dto
│   ├── LoginRequest.java                 # 登录请求
│   ├── TokenResponse.java                # Token 响应
│   ├── RefreshTokenRequest.java          # 刷新 Token 请求
│   └── ErrorResponse.java                # 错误响应
├── service
│   ├── TokenService.java                 # Token 服务
│   └── TokenBlacklistService.java        # Token 黑名单服务
└── exception
    └── GlobalExceptionHandler.java       # 全局异常处理器
```

### 技术栈

- **Spring Boot**: 3.2.5
- **JDK**: 21（支持虚拟线程）
- **Spring Security**: OAuth 2.0 Resource Server
- **Spring WebFlux**: 响应式 Web 框架
- **R2DBC**: 响应式数据库访问
- **JWT**: 0.12.5（jjwt）
- **MyBatis Plus**: 3.5.5（同步数据访问）

## 主要文件说明

### 1. JwtConfig.java

JWT 配置类，定义了 Token 的签名密钥、过期时间等参数。

**关键配置：**
- `secret`: HMAC-SHA256 签名密钥（至少 256 位）
- `accessTokenExpiration`: Access Token 有效期（默认 1 小时）
- `refreshTokenExpiration`: Refresh Token 有效期（默认 7 天）
- `reactiveJwtDecoder`: 响应式 JWT 解码器

### 2. TokenService.java

Token 服务类，负责 JWT Token 的生成、验证、解析。

**核心方法：**
- `generateAccessToken()`: 生成 Access Token
- `generateRefreshToken()`: 生成 Refresh Token
- `validateToken()`: 验证 Token（检查签名、过期、黑名单）
- `parseToken()`: 解析 Token 获取 Claims
- `isRefreshToken()`: 检查是否为 Refresh Token

**Token 结构：**

Access Token Payload:
```json
{
  "sub": "admin",
  "user_id": 1,
  "authorities": ["ROLE_ADMIN", "ROLE_USER"],
  "token_type": "access",
  "iat": 1699000000,
  "exp": 1699003600,
  "jti": "uuid",
  "iss": "flowable-auth-server"
}
```

Refresh Token Payload:
```json
{
  "sub": "admin",
  "user_id": 1,
  "token_type": "refresh",
  "iat": 1699000000,
  "exp": 1699604800,
  "jti": "uuid",
  "iss": "flowable-auth-server"
}
```

### 3. TokenBlacklistService.java

Token 黑名单服务，管理已废除的 Token。

**特点：**
- 使用 ConcurrentHashMap 内存存储
- 定时清理过期记录（每小时）
- 支持 Token ID (jti) 黑名单
- 响应式操作

**注意：** 如需支持大规模集群，可替换为 Redis 实现。

### 4. ReactiveUserService.java

响应式用户详情服务，实现 Spring Security 的 ReactiveUserDetailsService 接口。

**功能：**
- 从数据库加载用户信息（R2DBC 响应式查询）
- 从数据库加载用户权限（MyBatis 同步查询，使用 boundedElastic 线程池）
- 构建 Spring Security UserDetails 对象

### 5. SecurityConfig.java

Spring Security 配置类，配置 OAuth 2.0 Resource Server 和访问控制规则。

**核心配置：**
- OAuth 2.0 Resource Server（JWT）
- 自定义 JWT 认证管理器（增加黑名单检查）
- 响应式认证管理器（用户名密码登录）
- CORS 配置
- 路径访问权限配置

**白名单路径：**
- `/auth/login` - 登录
- `/auth/register` - 注册
- `/auth/refresh` - 刷新 Token
- `/init/**` - 数据库初始化（开发环境）

### 6. OAuth2AuthController.java

OAuth 2.0 认证控制器，提供登录、刷新、登出等接口。

**接口列表：**
- `POST /auth/login` - 用户登录
- `POST /auth/refresh` - 刷新 Token
- `POST /auth/logout` - 登出（废除 Token）
- `GET /auth/me` - 获取当前用户信息

### 7. GlobalExceptionHandler.java

全局异常处理器，统一处理认证相关异常。

**处理的异常：**
- `AuthenticationException` - 认证异常
- `AccessDeniedException` - 访问拒绝异常
- `JwtException` - JWT 异常
- `WebExchangeBindException` - 参数校验异常
- `RuntimeException` - 运行时异常

## API 文档

### 1. 用户登录

**请求：**
```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

**成功响应 (200 OK)：**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "user": {
    "id": 1,
    "username": "admin",
    "realName": "管理员",
    "email": "admin@example.com"
  }
}
```

**失败响应 (401 Unauthorized)：**
```json
{
  "error": "invalid_grant",
  "error_description": "用户名或密码错误"
}
```

### 2. 刷新 Token

**请求：**
```http
POST /auth/refresh
Content-Type: application/json

{
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**成功响应 (200 OK)：**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

**失败响应 (401 Unauthorized)：**
```json
{
  "error": "invalid_token",
  "error_description": "Refresh Token 无效或已过期"
}
```

### 3. 登出（废除 Token）

**请求：**
```http
POST /auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**成功响应 (200 OK)：**
```json
{
  "message": "登出成功"
}
```

**失败响应 (401 Unauthorized)：**
```json
{
  "error": "invalid_token",
  "error_description": "Token 无效"
}
```

### 4. 获取当前用户信息

**请求：**
```http
GET /auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**成功响应 (200 OK)：**
```json
{
  "id": 1,
  "username": "admin",
  "realName": "管理员",
  "email": "admin@example.com",
  "authorities": ["ROLE_ADMIN", "ROLE_USER"]
}
```

**失败响应 (401 Unauthorized)：**
```json
{
  "error": "invalid_token",
  "error_description": "未认证"
}
```

### 5. 访问受保护的资源

**请求：**
```http
GET /api/users
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**说明：**
- 所有需要认证的接口都需要在请求头中携带 `Authorization: Bearer {access_token}`
- Token 过期或无效时，返回 401 Unauthorized
- Token 在黑名单中时，返回 403 Forbidden

## 测试步骤

### 前置条件

1. 确保数据库中存在测试用户
2. 确保数据库中存在用户权限数据
3. 启动应用

### 测试流程

#### 1. 登录获取 Token

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

**预期结果：**
- 返回 200 状态码
- 包含 `access_token` 和 `refresh_token`
- 包含用户信息

#### 2. 使用 Access Token 访问受保护资源

```bash
curl -X GET http://localhost:8080/auth/me \
  -H "Authorization: Bearer {access_token}"
```

**预期结果：**
- 返回 200 状态码
- 返回当前用户信息

#### 3. 刷新 Token

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refresh_token": "{refresh_token}"
  }'
```

**预期结果：**
- 返回 200 状态码
- 返回新的 `access_token` 和 `refresh_token`
- 旧的 Refresh Token 被加入黑名单

#### 4. 登出（废除 Token）

```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer {access_token}"
```

**预期结果：**
- 返回 200 状态码
- 返回 "登出成功" 消息
- Token 被加入黑名单

#### 5. 使用已废除的 Token 访问资源

```bash
curl -X GET http://localhost:8080/auth/me \
  -H "Authorization: Bearer {revoked_access_token}"
```

**预期结果：**
- 返回 403 状态码
- 返回 "Token 已被废除" 错误消息

## 配置说明

### application.yml

```yaml
# JWT 配置（OAuth 2.0）
jwt:
  secret: flowable-secret-key-for-jwt-token-generation-must-be-at-least-256-bits
  access-token-expiration: 3600000      # Access Token 有效期：1小时（毫秒）
  refresh-token-expiration: 604800000   # Refresh Token 有效期：7天（毫秒）
  issuer: flowable-auth-server          # JWT 签发者

# Token 黑名单配置
token:
  blacklist:
    enabled: true
    cleanup-interval: 3600000  # 清理间隔：1小时（毫秒）
```

**配置项说明：**

- `jwt.secret`: JWT 签名密钥，必须至少 256 位（32 字符）
- `jwt.access-token-expiration`: Access Token 有效期（毫秒）
- `jwt.refresh-token-expiration`: Refresh Token 有效期（毫秒）
- `jwt.issuer`: JWT 签发者标识
- `token.blacklist.enabled`: 是否启用 Token 黑名单
- `token.blacklist.cleanup-interval`: 黑名单清理间隔（毫秒）

## 安全建议

### 1. JWT 密钥管理

- **生产环境**：使用环境变量或密钥管理服务（如 AWS KMS、Azure Key Vault）
- **密钥强度**：至少 256 位（32 字符）
- **密钥轮转**：定期更换密钥

### 2. Token 有效期

- **Access Token**：建议 15 分钟 - 1 小时
- **Refresh Token**：建议 7 天 - 30 天
- 根据业务安全需求调整

### 3. HTTPS

- **生产环境必须使用 HTTPS**
- 防止 Token 被中间人攻击窃取

### 4. Token 存储

**前端存储建议：**
- **推荐**：使用 httpOnly Cookie（防 XSS）
- **不推荐**：localStorage（易受 XSS 攻击）
- **可选**：sessionStorage（刷新页面丢失）

### 5. CORS 配置

```yaml
# 生产环境仅允许可信域名
configuration.setAllowedOrigins(Arrays.asList(
    "https://your-production-domain.com"
));
```

### 6. 速率限制

建议实施速率限制，防止暴力破解：
- 登录接口：5 次/分钟
- 刷新 Token 接口：10 次/分钟

### 7. Token 黑名单扩展

**单机/小集群**：使用内存存储（当前实现）

**大规模集群**：使用 Redis 实现：

```java
@Service
@RequiredArgsConstructor
public class RedisTokenBlacklistService {
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public Mono<Void> addToBlacklist(String tokenId, long expirationTime) {
        long ttl = Math.max(0, expirationTime - System.currentTimeMillis());
        return redisTemplate.opsForValue()
                .set("blacklist:" + tokenId, "1", Duration.ofMillis(ttl))
                .then();
    }

    public Mono<Boolean> isBlacklisted(String tokenId) {
        return redisTemplate.hasKey("blacklist:" + tokenId);
    }
}
```

## 常见问题

### 1. Token 过期如何处理？

**问题：** Access Token 过期后，前端如何自动刷新？

**解决方案：**

```javascript
// 前端拦截器示例（Axios）
axios.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;
    
    // 如果响应 401 且未重试过
    if (error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        // 使用 Refresh Token 刷新
        const { data } = await axios.post('/auth/refresh', {
          refresh_token: getRefreshToken()
        });
        
        // 保存新 Token
        setAccessToken(data.access_token);
        setRefreshToken(data.refresh_token);
        
        // 重试原请求
        originalRequest.headers['Authorization'] = 'Bearer ' + data.access_token;
        return axios(originalRequest);
      } catch (refreshError) {
        // 刷新失败，跳转登录页
        redirectToLogin();
        return Promise.reject(refreshError);
      }
    }
    
    return Promise.reject(error);
  }
);
```

### 2. 如何实现"记住我"功能？

**方案 1：** 延长 Refresh Token 有效期

```yaml
jwt:
  refresh-token-expiration: 2592000000  # 30 天
```

**方案 2：** 增加 Remember Me Token

```java
// 生成长期有效的 Remember Me Token
public String generateRememberMeToken(String username, Long userId) {
    Instant now = Instant.now();
    Instant expiration = now.plusMillis(30 * 24 * 60 * 60 * 1000L); // 30 天

    return Jwts.builder()
            .subject(username)
            .claim("user_id", userId)
            .claim("token_type", "remember_me")
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(jwtSigningKey)
            .compact();
}
```

### 3. 如何实现单设备登录？

**方案：** 使用用户级别的 Token 黑名单

```java
// 登录时废除用户的所有旧 Token
public Mono<Void> revokeAllUserTokens(Long userId) {
    return redisTemplate.opsForSet()
            .members("user:tokens:" + userId)
            .flatMap(tokenId -> addToBlacklist(tokenId, System.currentTimeMillis() + tokenExpiration))
            .then();
}
```

### 4. 如何支持多租户？

**方案：** 在 JWT 中增加 Tenant ID

```java
claims.put("tenant_id", user.getTenantId());
```

然后在 Security 配置中验证：

```java
authenticationManager.setJwtAuthenticationConverter(jwt -> {
    String tenantId = jwt.getClaim("tenant_id");
    // 验证租户ID...
});
```

### 5. 性能优化建议

1. **Token 验证缓存**：缓存已验证的 Token（短时间）
2. **黑名单索引**：使用 Redis Set 提高黑名单查询效率
3. **异步日志**：使用异步日志框架（如 Logback Async）
4. **数据库连接池**：合理配置 R2DBC 连接池大小
5. **虚拟线程**：已启用 JDK 21 虚拟线程，提高并发性能

## 总结

本实现提供了一个生产级别的 OAuth 2.0 认证系统，具有以下优势：

1. **完全响应式**：基于 WebFlux + Reactor，高性能非阻塞
2. **标准兼容**：符合 OAuth 2.0 规范
3. **安全可靠**：JWT 签名、Token 黑名单、全局异常处理
4. **易于扩展**：模块化设计，支持 Redis 扩展
5. **开发友好**：清晰的代码结构，详细的注释

**下一步优化方向：**
- 集成 Redis 实现分布式 Token 黑名单
- 增加速率限制（Spring Cloud Gateway Rate Limiter）
- 增加 OAuth 2.0 Authorization Code Flow 支持（SSO）
- 增加 Token 刷新失败重试机制
- 增加监控和审计日志

## 相关资源

- [Spring Security OAuth 2.0 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [JWT (RFC 7519)](https://datatracker.ietf.org/doc/html/rfc7519)
- [OAuth 2.0 (RFC 6749)](https://datatracker.ietf.org/doc/html/rfc6749)
- [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Project Reactor](https://projectreactor.io/docs/core/release/reference/)
