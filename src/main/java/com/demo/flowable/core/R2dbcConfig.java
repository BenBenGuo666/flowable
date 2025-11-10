package com.demo.flowable.core;

import io.r2dbc.mssql.MssqlConnectionConfiguration;
import io.r2dbc.mssql.MssqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;

/**
 * @author: e-Benben.Guo
 * @date: 2025/11
 * @desc:
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "com.demo.flowable.data.reactor.repository")
public class R2dbcConfig  extends AbstractR2dbcConfiguration {

    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        return  new MssqlConnectionFactory(
                MssqlConnectionConfiguration.builder()
                        .host("localhost")
                        .port(1433)
                        .username("sa")
                        .password("123456")
                        .database("flowable")
                        .build()
        );

    }

    @Bean(name = "r2dbcTransactionManager")
    public R2dbcTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        //@Transactional(transactionManager = "r2dbcTransactionManager")
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    public R2dbcEntityTemplate r2dbcEntityTemplate(ConnectionFactory connectionFactory) {
        return new R2dbcEntityTemplate(connectionFactory);
    }


}
