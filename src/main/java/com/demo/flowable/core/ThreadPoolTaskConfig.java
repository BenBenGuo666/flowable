package com.demo.flowable.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: e-Benben.Guo
 * @date: 2025/11
 * @desc: 传统线程池配置类（已废弃）
 * <p>
 * ⚠️ 已废弃：推荐使用虚拟线程（VirtualThreadConfig）
 * <p>
 * 在 JDK 21+ 环境中，建议使用虚拟线程替代传统线程池：
 * 1. 虚拟线程更轻量，创建成本极低
 * 2. 无需手动配置线程池参数
 * 3. 更适合 I/O 密集型任务
 * <p>
 * 此配置保留作为备选方案，仅在需要时启用
 * 启用方式: 设置 spring.threads.virtual.enabled=false
 * </p>
 */
@Slf4j
@Deprecated(since = "JDK 21", forRemoval = false)
@Configuration
@ConditionalOnProperty(name = "spring.threads.virtual.enabled", havingValue = "false")
public class ThreadPoolTaskConfig {

    @Bean("legacyThreadPoolExecutor")
    public ThreadPoolTaskExecutor legacyThreadPoolExecutor() {
        log.warn("⚠️ 正在使用传统线程池，建议启用虚拟线程以获得更好性能");
        log.warn("启用虚拟线程: spring.threads.virtual.enabled=true");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 根据CPU核心数动态配置
        int core = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(core * 2 + 1);
        executor.setKeepAliveSeconds(120);
        executor.setQueueCapacity(120);
        executor.setThreadNamePrefix("legacy-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        // 等待任务完成后再关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        log.info("传统线程池配置: 核心={}, 最大={}, 队列={}", core, core * 2 + 1, 120);

        return executor;
    }
}
