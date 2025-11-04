package com.demo.flowable.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

/**
 * @author: e-Benben.Guo
 * @date: 2025/11
 * @desc: 虚拟线程配置类 (JDK 21+)
 * <p>
 * 使用虚拟线程替代传统线程池，提升高并发场景性能
 * Virtual Threads 特点：
 * 1. 轻量级：创建成本极低，可创建数百万个虚拟线程
 * 2. 调度高效：JVM自动管理调度，无需手动配置线程池
 * 3. 适合I/O密集型任务：如数据库查询、HTTP调用、文件操作
 * </p>
 */
@Slf4j
@Configuration
@EnableAsync
public class VirtualThreadConfig implements AsyncConfigurer {

    /**
     * 配置异步任务执行器使用虚拟线程
     * 覆盖 Spring Boot 默认的 TaskExecutor
     */
    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor asyncTaskExecutor(TaskExecutorBuilder builder) {
        log.info("初始化虚拟线程异步任务执行器 (Virtual Threads)");

        return builder
                .taskDecorator(loggingTaskDecorator())
                .build();
    }

    /**
     * 获取异步执行器（使用虚拟线程）
     */
    @Override
    public Executor getAsyncExecutor() {
        log.info("配置全局异步执行器: 使用虚拟线程 (Virtual Threads per Task)");

        // 使用虚拟线程执行器：每个任务一个虚拟线程
        return command -> Thread.startVirtualThread(command);
    }

    /**
     * 任务装饰器：添加日志和上下文传递
     */
    private TaskDecorator loggingTaskDecorator() {
        return runnable -> () -> {
            String threadInfo = Thread.currentThread().toString();
            log.debug("异步任务开始执行 - 线程: {}", threadInfo);

            try {
                runnable.run();
            } catch (Exception e) {
                log.error("异步任务执行失败 - 线程: {}, 错误: {}", threadInfo, e.getMessage(), e);
                throw e;
            } finally {
                log.debug("异步任务执行完成 - 线程: {}", threadInfo);
            }
        };
    }

    /**
     * 虚拟线程执行器 Bean
     * 可在需要时通过 @Qualifier("virtualThreadExecutor") 注入
     */
    @Bean("virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        log.info("创建专用虚拟线程执行器 Bean");

        return command -> Thread.startVirtualThread(() -> {
            log.trace("虚拟线程任务开始: {}", Thread.currentThread());
            try {
                command.run();
            } catch (Exception e) {
                log.error("虚拟线程任务执行失败: {}", e.getMessage(), e);
            }
        });
    }
}
