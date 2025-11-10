# 安全策略和指南

## 概述

本文档定义了 Flowable UI 系统的安全策略、最佳实践和安全开发指南，确保系统的安全性和数据保护。

## 安全架构

### 1. 安全层次模型

```
┌─────────────────────────────────────────┐
│           网络安全层                      │
│  HTTPS, WAF, DDoS 防护, 防火墙           │
├─────────────────────────────────────────┤
│           应用安全层                      │
│  认证, 授权, 会话管理, CSRF 防护          │
├─────────────────────────────────────────┤
│           数据安全层                      │
│  加密, 脱敏, 备份, 审计日志               │
├─────────────────────────────────────────┤
│           基础设施安全层                  │
│  容器安全, 主机加固, 网络隔离             │
└─────────────────────────────────────────┘
```

### 2. 威胁模型

#### 2.1 外部威胁
- SQL 注入攻击
- XSS 跨站脚本攻击
- CSRF 跨站请求伪造
- 暴力破解攻击
- DDoS 拒绝服务攻击

#### 2.2 内部威胁
- 权限滥用
- 数据泄露
- 恶意操作
- 配置错误

## 认证和授权

### 1. 认证机制

#### 1.1 JWT 认证配置
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/processes").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/api/processes").hasRole("ADMIN")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(getSecretKey())
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    }
}
```

#### 1.2 JWT 令牌管理
```java
@Service
public class JwtTokenService {

    private static final int ACCESS_TOKEN_VALIDITY = 15 * 60; // 15分钟
    private static final int REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60; // 7天

    public TokenResponse generateTokens(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        String accessToken = createAccessToken(userPrincipal);
        String refreshToken = createRefreshToken(userPrincipal);
        
        // 存储刷新令牌到 Redis
        redisTemplate.opsForValue().set(
            "refresh_token:" + userPrincipal.getId(),
            refreshToken,
            Duration.ofSeconds(REFRESH_TOKEN_VALIDITY)
        );
        
        return TokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(ACCESS_TOKEN_VALIDITY)
            .build();
    }

    private String createAccessToken(UserPrincipal userPrincipal) {
        return Jwts.builder()
            .setSubject(userPrincipal.getId())
            .claim("username", userPrincipal.getUsername())
            .claim("roles", userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()))
            .claim("tenantId", userPrincipal.getTenantId())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY * 1000))
            .signWith(getSecretKey(), SignatureAlgorithm.HS256)
            .compact();
    }
}
```

### 2. 授权机制

#### 2.1 基于角色的访问控制 (RBAC)
```java
@Entity
@Table(name = "roles")
public class Role {
    @Id
    private String id;
    
    @Column(unique = true)
    private String name;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}

@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String resource; // process, task, user
    
    @Column(nullable = false)
    private String action; // read, write, delete
    
    @Column
    private String scope; // own, group, all
}
```

#### 2.2 方法级权限控制
```java
@Service
@PreAuthorize("hasRole('ADMIN')")
public class ProcessService {

    @PreAuthorize("hasPermission(#processId, 'process', 'read')")
    public ProcessModel getProcess(String processId) {
        return processRepository.findById(processId)
            .orElseThrow(() -> new ProcessNotFoundException(processId));
    }

    @PreAuthorize("hasPermission(#request, 'process', 'write')")
    public ProcessModel createProcess(ProcessCreateRequest request) {
        // 创建流程逻辑
    }

    @PostAuthorize("hasPermission(returnObject, 'read')")
    public List<ProcessModel> getUserProcesses() {
        // 返回用户有权限的流程
    }
}
```

#### 2.3 自定义权限评估器
```java
@Component
public class ProcessPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        if (targetDomainObject instanceof ProcessModel) {
            return hasProcessPermission(userPrincipal, (ProcessModel) targetDomainObject, permission.toString());
        }
        
        return false;
    }

    private boolean hasProcessPermission(UserPrincipal user, ProcessModel process, String permission) {
        // 检查用户是否有对该流程的特定权限
        return user.getAuthorities().stream()
            .anyMatch(authority -> {
                String[] parts = authority.getAuthority().split(":");
                return parts.length == 3 
                    && "process".equals(parts[0])
                    && permission.equals(parts[1])
                    && (parts[2].equals("all") || process.getCreatedBy().equals(user.getId()));
            });
    }
}
```

## 数据安全

### 1. 数据加密

#### 1.1 敏感数据加密
```java
@Component
public class DataEncryptionService {

    private final AESUtil aesUtil;

    @EventListener
    @Async
    public void handleUserCreated(UserCreatedEvent event) {
        User user = event.getUser();
        
        // 加密敏感字段
        if (StringUtils.hasText(user.getPhone())) {
            user.setPhone(aesUtil.encrypt(user.getPhone()));
        }
        
        if (StringUtils.hasText(user.getIdCard())) {
            user.setIdCard(aesUtil.encrypt(user.getIdCard()));
        }
        
        userRepository.save(user);
    }
}

@Component
public class AESUtil {
    
    @Value("${app.security.encryption.key}")
    private String encryptionKey;

    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new EncryptionException("数据加密失败", e);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DecryptionException("数据解密失败", e);
        }
    }
}
```

#### 1.2 数据库连接加密
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/flowable_ui?useSSL=true&requireSSL=true&verifyServerCertificate=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      connection-test-query: SELECT 1
      connection-timeout: 20000
      maximum-pool-size: 10
      minimum-idle: 5
```

### 2. 数据脱敏

#### 2.1 日志脱敏
```java
@Component
public class DataMaskingService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\d{3})\\d{4}(\\d{4})");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(\\w{1,3})\\w*@(\\w+)");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(\\d{6})\\d{8}(\\d{4})");

    public String maskPhone(String phone) {
        if (StringUtils.hasText(phone)) {
            return PHONE_PATTERN.matcher(phone).replaceAll("$1****$2");
        }
        return phone;
    }

    public String maskEmail(String email) {
        if (StringUtils.hasText(email)) {
            return EMAIL_PATTERN.matcher(email).replaceAll("$1***@$2");
        }
        return email;
    }

    public String maskIdCard(String idCard) {
        if (StringUtils.hasText(idCard)) {
            return ID_CARD_PATTERN.matcher(idCard).replaceAll("$1********$2");
        }
        return idCard;
    }
}
```

#### 2.2 API 响应脱敏
```java
@JsonSerialize(using = PhoneNumberSerializer.class)
private String phone;

public class PhoneNumberSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        if (value != null) {
            gen.writeString(maskPhoneNumber(value));
        }
    }

    private String maskPhoneNumber(String phone) {
        if (phone.length() >= 11) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        }
        return phone;
    }
}
```

## 输入验证和防护

### 1. 输入验证

#### 1.1 参数验证
```java
@RestController
@Validated
public class ProcessController {

    @PostMapping("/processes")
    public ResponseEntity<ProcessResponse> createProcess(
            @Valid @RequestBody ProcessCreateRequest request) {
        // 处理逻辑
    }
}

public class ProcessCreateRequest {
    
    @NotBlank(message = "流程名称不能为空")
    @Size(max = 100, message = "流程名称长度不能超过100个字符")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9\\s]+$", message = "流程名称只能包含中文、英文、数字和空格")
    private String name;

    @NotBlank(message = "流程键不能为空")
    @Size(max = 50, message = "流程键长度不能超过50个字符")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "流程键必须以字母开头，只能包含字母、数字和下划线")
    private String key;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    @Valid
    private List<@Valid ProcessVariable> variables;
}
```

#### 1.2 自定义验证器
```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ProcessKeyValidator.class)
public @interface ValidProcessKey {
    String message() default "流程键格式不正确";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class ProcessKeyValidator implements ConstraintValidator<ValidProcessKey, String> {
    
    private static final Pattern PROCESS_KEY_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{2,49}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // 由 @NotNull 处理
        }
        
        return PROCESS_KEY_PATTERN.matcher(value).matches();
    }
}
```

### 2. SQL 注入防护

#### 2.1 参数化查询
```java
@Repository
public interface ProcessRepository extends JpaRepository<ProcessModel, String> {
    
    // 使用 @Query 注解的参数化查询
    @Query("SELECT p FROM ProcessModel p WHERE p.name LIKE %:name% AND p.category = :category")
    List<ProcessModel> findByNameContainingAndCategory(
            @Param("name") String name, 
            @Param("category") String category);
    
    // 使用 Specification 进行动态查询
    default List<ProcessModel> findByDynamicCriteria(ProcessSearchCriteria criteria) {
        return findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(criteria.getName())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + criteria.getName().toLowerCase() + "%"
                ));
            }
            
            if (StringUtils.hasText(criteria.getCategory())) {
                predicates.add(criteriaBuilder.equal(root.get("category"), criteria.getCategory()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }
}
```

### 3. XSS 防护

#### 3.1 输入过滤
```java
@Component
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        XssHttpServletRequestWrapper wrappedRequest = new XssHttpServletRequestWrapper(
                (HttpServletRequest) request);
        chain.doFilter(wrappedRequest, response);
    }
}

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    
    private static final Pattern[] XSS_PATTERNS = {
        Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };

    public XssHttpServletRequestWrapper(HttpServletRequest servletRequest) {
        super(servletRequest);
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) {
            return null;
        }
        
        String[] encodedValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            encodedValues[i] = stripXSS(values[i]);
        }
        
        return encodedValues;
    }

    private String stripXSS(String value) {
        if (value != null) {
            value = HtmlUtils.htmlEscape(value);
            
            for (Pattern pattern : XSS_PATTERNS) {
                value = pattern.matcher(value).replaceAll("");
            }
        }
        return value;
    }
}
```

### 4. CSRF 防护

#### 4.1 CSRF 令牌配置
```java
@Configuration
public class CsrfConfig {

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-CSRF-TOKEN");
        return repository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
                .ignoringRequestMatchers("/api/auth/**")
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
            .build();
    }
}
```

#### 4.2 前端 CSRF 处理
```typescript
// axios 拦截器
axios.interceptors.request.use(
  (config) => {
    // 添加 CSRF 令牌
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content')
    if (csrfToken) {
      config.headers['X-CSRF-TOKEN'] = csrfToken
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)
```

## 会话管理

### 1. 会话安全配置

#### 1.1 会话超时
```java
@Configuration
public class SessionConfig {

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @EventListener
    public void sessionCreated(HttpSessionCreatedEvent event) {
        HttpSession session = event.getSession();
        session.setMaxInactiveInterval(30 * 60); // 30分钟
        
        // 记录会话创建日志
        log.info("会话创建: sessionId={}, maxInactiveInterval={}", 
                session.getId(), session.getMaxInactiveInterval());
    }

    @EventListener
    public void sessionDestroyed(HttpSessionDestroyedEvent event) {
        HttpSession session = event.getSession();
        
        // 清理会话相关资源
        String userId = (String) session.getAttribute("userId");
        if (userId != null) {
            // 清理用户相关缓存
            redisTemplate.delete("user_cache:" + userId);
        }
        
        log.info("会话销毁: sessionId={}", session.getId());
    }
}
```

### 2. 并发会话控制

#### 2.1 单用户会话限制
```java
@Configuration
public class ConcurrentSessionConfig {

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .sessionRegistry(sessionRegistry())
                .expiredUrl("/login?expired"))
            .build();
    }
}
```

## 审计和日志

### 1. 安全审计

#### 1.1 审计事件记录
```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String action;
    
    @Column(nullable = false)
    private String resource;
    
    @Column
    private String resourceId;
    
    @Column
    private String ipAddress;
    
    @Column
    private String userAgent;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column
    private String result; // SUCCESS, FAILURE
    
    @Column(length = 1000)
    private String details;
}

@Service
public class AuditService {

    public void logSecurityEvent(SecurityAuditEvent event) {
        AuditLog auditLog = AuditLog.builder()
            .id(UUID.randomUUID().toString())
            .userId(event.getUserId())
            .action(event.getAction())
            .resource(event.getResource())
            .resourceId(event.getResourceId())
            .ipAddress(event.getIpAddress())
            .userAgent(event.getUserAgent())
            .timestamp(LocalDateTime.now())
            .result(event.getResult())
            .details(event.getDetails())
            .build();
            
        auditLogRepository.save(auditLog);
    }
}
```

#### 1.2 审计切面
```java
@Aspect
@Component
public class AuditAspect {

    @Autowired
    private AuditService auditService;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String userId = getCurrentUserId();
        String action = auditable.action();
        String resource = auditable.resource();
        
        try {
            Object result = joinPoint.proceed();
            
            // 记录成功操作
            auditService.logSecurityEvent(SecurityAuditEvent.builder()
                .userId(userId)
                .action(action)
                .resource(resource)
                .result("SUCCESS")
                .build());
                
            return result;
        } catch (Exception e) {
            // 记录失败操作
            auditService.logSecurityEvent(SecurityAuditEvent.builder()
                .userId(userId)
                .action(action)
                .resource(resource)
                .result("FAILURE")
                .details(e.getMessage())
                .build());
                
            throw e;
        }
    }
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String action();
    String resource();
}
```

### 2. 安全日志

#### 2.1 日志配置
```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="SECURITY" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/security.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/security.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <logger name="SECURITY" level="INFO" additivity="false">
        <appender-ref ref="SECURITY"/>
    </logger>
</configuration>
```

## 安全配置

### 1. HTTPS 配置

#### 1.1 SSL 证书配置
```yaml
# application.yml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: flowable-ui
  http2:
    enabled: true

# HTTP 重定向到 HTTPS
management:
  server:
    port: 8080
    ssl:
      enabled: false
```

#### 1.2 安全头配置
```java
@Configuration
public class SecurityHeadersConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true))
                .and()
                .addHeaderWriter(new StaticHeadersWriter("X-Content-Type-Options", "nosniff"))
                .addHeaderWriter(new StaticHeadersWriter("X-XSS-Protection", "1; mode=block"))
                .addHeaderWriter(new StaticHeadersWriter("Referrer-Policy", "strict-origin-when-cross-origin"))
                .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy", "geolocation=(), microphone=(), camera=()")))
            .build();
    }
}
```

### 2. CORS 配置

#### 2.1 跨域资源共享
```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "https://*.example.com",
            "https://localhost:*"
        ));
        
        // 允许的方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        
        // 允许的头
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "X-CSRF-TOKEN"
        ));
        
        // 允许凭证
        configuration.setAllowCredentials(true);
        
        // 预检请求缓存时间
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }
}
```

## 安全监控

### 1. 入侵检测

#### 1.1 异常行为检测
```java
@Service
public class SecurityMonitoringService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);

    @EventListener
    public void handleLoginFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        String clientIp = getClientIpAddress();
        
        // 记录失败尝试
        int attempts = incrementFailedAttempts(username, clientIp);
        
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            // 锁定账户
            lockAccount(username, LOCKOUT_DURATION);
            
            // 发送安全告警
            sendSecurityAlert("账户锁定", 
                String.format("用户 %s (IP: %s) 连续登录失败 %d 次，账户已被锁定", 
                    username, clientIp, attempts));
        }
    }

    @EventListener
    public void handleLoginSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        
        // 清除失败计数
        clearFailedAttempts(username);
        
        // 记录成功登录
        logSecurityEvent("LOGIN_SUCCESS", username, getClientIpAddress());
    }

    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void detectSuspiciousActivity() {
        // 检测异常 IP 访问
        List<String> suspiciousIps = findSuspiciousIpAddresses();
        for (String ip : suspiciousIps) {
            sendSecurityAlert("可疑IP访问", 
                String.format("IP地址 %s 在短时间内产生了大量请求", ip));
        }
        
        // 检测权限提升尝试
        List<String> privilegeEscalationAttempts = findPrivilegeEscalationAttempts();
        for (String attempt : privilegeEscalationAttempts) {
            sendSecurityAlert("权限提升尝试", attempt);
        }
    }
}
```

### 2. 安全指标监控

#### 2.1 Micrometer 指标
```java
@Component
public class SecurityMetrics {

    private final Counter loginAttempts;
    private final Counter loginFailures;
    private final Counter authorizationFailures;
    private final Timer requestDuration;

    public SecurityMetrics(MeterRegistry meterRegistry) {
        this.loginAttempts = Counter.builder("security.login.attempts")
            .description("Total login attempts")
            .register(meterRegistry);
            
        this.loginFailures = Counter.builder("security.login.failures")
            .description("Failed login attempts")
            .register(meterRegistry);
            
        this.authorizationFailures = Counter.builder("security.authorization.failures")
            .description("Authorization failures")
            .register(meterRegistry);
            
        this.requestDuration = Timer.builder("security.request.duration")
            .description("Request processing time")
            .register(meterRegistry);
    }

    public void recordLoginAttempt() {
        loginAttempts.increment();
    }

    public void recordLoginFailure() {
        loginFailures.increment();
    }

    public void recordAuthorizationFailure() {
        authorizationFailures.increment();
    }
}
```

## 安全测试

### 1. 安全测试用例

#### 1.1 认证测试
```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/processes"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidTokens() throws Exception {
        mockMvc.perform(get("/api/processes")
                .header("Authorization", "Bearer invalid-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowValidTokens() throws Exception {
        String validToken = generateValidToken();
        
        mockMvc.perform(get("/api/processes")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isOk());
    }
}
```

#### 1.2 授权测试
```java
@Test
void shouldEnforceRoleBasedAccess() throws Exception {
    String userToken = generateTokenForRole("USER");
    String adminToken = generateTokenForRole("ADMIN");
    
    // 普通用户不能创建流程
    mockMvc.perform(post("/api/processes")
            .header("Authorization", "Bearer " + userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(processJson))
        .andExpect(status().isForbidden());
    
    // 管理员可以创建流程
    mockMvc.perform(post("/api/processes")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(processJson))
        .andExpected(status().isCreated());
}
```

### 2. 渗透测试

#### 2.1 自动化安全扫描
```yaml
# .github/workflows/security-scan.yml
name: Security Scan

on: [push, pull_request]

jobs:
  security-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Run OWASP ZAP Scan
        uses: zaproxy/action-full-scan@v0.4.0
        with:
          target: 'http://localhost:8080'
          
      - name: Run Snyk Security Scan
        uses: snyk/actions/node@master
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
        with:
          args: --severity-threshold=high
```

## 最佳实践

### 1. 安全开发生命周期

#### 1.1 设计阶段
- 进行威胁建模分析
- 定义安全需求
- 选择安全框架和库

#### 1.2 开发阶段
- 遵循安全编码规范
- 使用静态代码分析工具
- 进行代码安全审查

#### 1.3 测试阶段
- 执行安全测试用例
- 进行渗透测试
- 验证安全配置

#### 1.4 部署阶段
- 安全配置检查
- 环境安全加固
- 监控和告警设置

### 2. 安全意识培训

#### 2.1 开发团队培训
- OWASP Top 10 安全风险
- 安全编码最佳实践
- 安全工具使用培训

#### 2.2 定期安全评估
- 季度安全审计
- 年度渗透测试
- 安全配置审查

### 3. 应急响应

#### 3.1 安全事件响应流程
1. 事件检测和报告
2. 事件分析和评估
3. 遏制和消除威胁
4. 恢复和监控
5. 事后分析和改进

#### 3.2 安全事件分类
- **严重**: 数据泄露、系统入侵
- **高**: 权限提升、拒绝服务
- **中**: 配置错误、弱密码
- **低**: 信息泄露、日志异常