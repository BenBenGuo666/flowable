package com.demo.flowable.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 配置（WebFlux 响应式）
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security Web Filter Chain（响应式）
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // 禁用CSRF（因为使用JWT）
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // 配置CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 配置授权规则
                .authorizeExchange(exchanges -> exchanges
                        // 允许登录注册接口匿名访问
                        .pathMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        // 允许访问数据库初始化接口（仅开发环境）
                        .pathMatchers("/init/**").permitAll()
                        // 允许 Flowable 流程相关接口（暂时）
                        .pathMatchers("/process-definition/**", "/process-instance/**",
                                "/task/**", "/process-template/**").permitAll()
                        // 允许表单相关接口（暂时）
                        .pathMatchers("/api/form-definition/**", "/api/form-data/**").permitAll()
                        // 允许请假相关接口（暂时）
                        .pathMatchers("/api/leave/**").permitAll()
                        // 允许用户角色权限接口（暂时）
                        .pathMatchers("/api/user/**", "/api/role/**", "/api/permission/**").permitAll()
                        // 其他请求需要认证
                        .anyExchange().authenticated()
                )
                .build();
    }

    /**
     * CORS 配置（响应式）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:5174", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
