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
     * JWT 认证管理器（用于 OAuth 2.0 Resource Server）
     */
    @Bean
    public JwtReactiveAuthenticationManager jwtAuthenticationManager() {
        JwtReactiveAuthenticationManager authenticationManager =
                new JwtReactiveAuthenticationManager(jwtConfig.reactiveJwtDecoder());

        // 自定义 JWT 验证逻辑，增加黑名单检查
        authenticationManager.setJwtAuthenticationConverter(jwt -> {
            String tokenId = jwt.getId();

            // 检查 Token 是否在黑名单中
            return tokenBlacklistService.isBlacklisted(tokenId)
                    .flatMap(isBlacklisted -> {
                        if (isBlacklisted) {
                            log.warn("Token 在黑名单中，拒绝访问: {}", tokenId);
                            return reactor.core.publisher.Mono.error(
                                    new org.springframework.security.access.AccessDeniedException("Token 已被废除"));
                        }

                        // 从 JWT Claims 中提取权限
                        var authorities = jwt.getClaimAsStringList("authorities");
                        if (authorities == null) {
                            authorities = java.util.Collections.singletonList("ROLE_USER");
                        }

                        var grantedAuthorities = authorities.stream()
                                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                                .collect(java.util.stream.Collectors.toList());

                        return reactor.core.publisher.Mono.just(
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        jwt.getSubject(),
                                        null,
                                        grantedAuthorities
                                )
                        );
                    });
        });

        return authenticationManager;
    }

    /**
     * Security Web Filter Chain（响应式）
     * 配置 OAuth 2.0 Resource Server 和访问控制规则
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // 禁用 CSRF（因为使用 JWT）
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // 配置 CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 配置 OAuth 2.0 Resource Server（JWT）
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationManagerResolver(exchange ->
                                reactor.core.publisher.Mono.just(jwtAuthenticationManager()))
                )

                // 配置授权规则
                .authorizeExchange(exchanges -> exchanges
                        // 允许 OAuth 2.0 认证相关接口匿名访问（白名单）
                        .pathMatchers(
                                "/api/auth/login",      // 用户登录
                                "/api/auth/refresh"     // 刷新 Token
                        ).permitAll()

                        // 允许访问数据库初始化接口（仅开发环境，生产环境应移除）
                        .pathMatchers("/api/init/**").permitAll()

                        // Flowable 流程相关接口（暂时开放，后续应根据权限控制）
                        .pathMatchers(
                                "/api/process-definition/**",
                                "/api/process-instance/**",
                                "/api/task/**",
                                "/api/process-template/**"
                        ).permitAll()

                        // 表单相关接口（暂时开放）
                        .pathMatchers("/api/form-definition/**", "/api/form-data/**").permitAll()

                        // 请假相关接口（暂时开放）
                        .pathMatchers("/api/leave/**").permitAll()

                        // 用户、角色、权限接口（暂时开放，后续应限制为管理员权限）
                        .pathMatchers("/api/user/**", "/api/role/**", "/api/permission/**").permitAll()

                        // 其他所有请求都需要认证（Bearer Token）
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
