package com.demo.flowable.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 速率限制配置
 * 基于 Caffeine 缓存实现用户级别的 API 速率限制
 *
 * @author e-Benben.Guo
 * @date 2025/11
 */
@Getter
@Configuration
public class RateLimitConfig {

    /**
     * 每个用户每分钟最大请求次数
     */
    @Value("${rate-limit.requests-per-minute:200}")
    private int requestsPerMinute;

    /**
     * 速率限制时间窗口（分钟）
     */
    @Value("${rate-limit.time-window-minutes:1}")
    private int timeWindowMinutes;

    /**
     * 是否启用速率限制
     */
    @Value("${rate-limit.enabled:true}")
    private boolean enabled;

    /**
     * 缓存最大容量（用户数）
     */
    @Value("${rate-limit.cache-max-size:10000}")
    private int cacheMaxSize;

    /**
     * 创建速率限制缓存
     * Key: 用户ID（String）
     * Value: 请求计数器（AtomicInteger）
     *
     * @return Caffeine Cache
     */
    @Bean
    public Cache<String, AtomicInteger> rateLimitCache() {
        return Caffeine.newBuilder()
                // 缓存过期时间 = 时间窗口
                .expireAfterWrite(Duration.ofMinutes(timeWindowMinutes))
                // 最大缓存用户数
                .maximumSize(cacheMaxSize)
                // 启用统计（可选，用于监控）
                .recordStats()
                .build();
    }

    /**
     * 获取剩余请求次数的提示信息
     *
     * @param current 当前请求次数
     * @return 剩余次数
     */
    public int getRemainingRequests(int current) {
        return Math.max(0, requestsPerMinute - current);
    }

    /**
     * 获取速率限制重置时间（秒）
     *
     * @return 重置时间（秒）
     */
    public long getResetTimeSeconds() {
        return timeWindowMinutes * 60L;
    }
}
