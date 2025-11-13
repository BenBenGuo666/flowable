package com.demo.flowable.config;

import com.demo.flowable.service.TokenBlacklistService;
import com.demo.flowable.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 配置（WebFlux 响应式）
 * 配置 OAuth 2.0 Resource Server 和 JWT 认证
 *
 * @author e-Benben.Guo
 * @date 2025/11
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ReactiveUserDetailsService reactiveUserDetailsService;
    private final JwtConfig jwtConfig;
    private final TokenService tokenService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 响应式认证管理器（用于用户名密码登录）
     * 标记为 @Primary，作为默认的认证管理器
     */
    @Bean
    @Primary
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager =
                new UserDetailsRepositoryReactiveAuthenticationManager(reactiveUserDetailsService);
        authenticationManager.setPasswordEncoder(passwordEncoder());
        return authenticationManager;
    }

    /**
     * Security Web Filter Chain（响应式）
     * 使用自定义的 JwtAuthenticationFilter 进行令牌验证
     *
     * 注意：
     * - JwtAuthenticationFilter 会自动拦截并验证所有请求的 Token
     * - 不再使用 OAuth2ResourceServer，改用自定义过滤器（更灵活，易于扩展）
     * - 白名单和黑名单在 JwtAuthenticationFilter 中配置
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // 禁用 CSRF（因为使用 JWT）
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // 配置 CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 禁用默认的表单登录和 HTTP Basic 认证
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

                // 配置授权规则
                // 注意：由于使用了自定义的 JwtAuthenticationFilter，
                // 这里的配置主要用于兜底和方法级权限控制
                .authorizeExchange(exchanges -> exchanges
                        // ✅ 认证接口白名单（在 JwtAuthenticationFilter 中也有配置）
                        .pathMatchers("/api/auth/login", "/api/auth/refresh").permitAll()

                        // ❌ 禁用初始化接口
                        .pathMatchers("/api/init/**").denyAll()

                        // 🔐 其他所有接口需要认证（由 JwtAuthenticationFilter 处理）
                        .anyExchange().authenticated()
                )

                .build();
    }

    /**
     * CORS 配置（响应式）
     * 允许前端跨域访问
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:3000"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
