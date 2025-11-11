package com.demo.flowable.core;

import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import io.asyncer.r2dbc.mysql.constant.SslMode;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

/**
 * R2DBC 响应式数据访问配置
 *
 * 配置说明：
 * 1. 使用 MySQL R2DBC 驱动（io.asyncer:r2dbc-mysql）
 * 2. 独立的 ConnectionFactory（不与 JDBC DataSource 冲突）
 * 3. 独立的响应式事务管理器（r2dbcTransactionManager）
 * 4. Repository 扫描路径：com.demo.flowable.data.reactor.repository
 *
 * 使用方式：
 * - Repository: 继承 ReactiveCrudRepository
 * - 事务管理: @Transactional(transactionManager = "r2dbcTransactionManager")
 *
 * @author: e-Benben.Guo
 * @date: 2025/11
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "com.demo.flowable.data.reactor.repository")
public class R2dbcConfig extends AbstractR2dbcConfiguration {

    @Value("${spring.r2dbc.url}")
    private String r2dbcUrl;

    /**
     * 配置 R2DBC ConnectionFactory（MySQL）
     *
     * 方式：直接使用 application.yml 中配置的 URL
     * URL 格式：r2dbc:mysql://localhost:3306/flowable?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC
     *
     * 优点：
     * - 简洁，直接从配置文件读取
     * - Spring Boot 自动处理连接池配置（spring.r2dbc.pool.*）
     */
    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        return MySqlConnectionFactory.from(
                MySqlConnectionConfiguration.builder()
                        .host("localhost")
                        .port(3306)
                        .username("root")
                        .password("123456")
                        .database("flowable")
                        .sslMode(SslMode.DISABLED)
                        .build());
    }

    /**
     * 配置 R2DBC 响应式事务管理器
     *
     * 使用时需要显式指定：
     * @Transactional(transactionManager = "r2dbcTransactionManager")
     *
     * 注意：
     * - 不能与同步事务管理器（transactionManager）混用
     * - 不能在同一个事务中混用同步和响应式代码
     */
    @Bean(name = "r2dbcTransactionManager")
    public ReactiveTransactionManager r2dbcTransactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    /**
     * 配置 R2DBC 模板（可选，用于更灵活的数据库操作）
     *
     * 使用场景：
     * - 执行原生 SQL 查询
     * - 动态构建查询条件
     * - 批量操作
     */
    @Bean
    public R2dbcEntityTemplate r2dbcEntityTemplate(ConnectionFactory connectionFactory) {
        return new R2dbcEntityTemplate(connectionFactory);
    }
}
