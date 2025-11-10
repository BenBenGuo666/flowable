package com.demo.flowable.controller;

import com.demo.flowable.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 数据库初始化控制器（仅用于开发环境）
 */
@Slf4j
@RestController
@RequestMapping("/api/init")
public class InitController {

    @Autowired
    private DataSource dataSource;

    /**
     * 初始化数据库表结构
     */
    @PostMapping("/schema")
    public Result<String> initSchema() {
        try (Connection connection = dataSource.getConnection()) {
            log.info("开始初始化数据库表结构...");
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("sql/schema.sql"));
            log.info("数据库表结构初始化完成");
            return Result.success("数据库表结构初始化成功");
        } catch (Exception e) {
            log.error("数据库表结构初始化失败", e);
            return Result.error("数据库表结构初始化失败: " + e.getMessage());
        }
    }

    /**
     * 初始化数据
     */
    @PostMapping("/data")
    public Result<String> initData() {
        try (Connection connection = dataSource.getConnection()) {
            log.info("开始初始化数据...");
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("sql/data.sql"));
            log.info("数据初始化完成");
            return Result.success("数据初始化成功");
        } catch (Exception e) {
            log.error("数据初始化失败", e);
            return Result.error("数据初始化失败: " + e.getMessage());
        }
    }

    /**
     * 一键初始化（表结构 + 数据）
     */
    @PostMapping("/all")
    public Result<String> initAll() {
        try (Connection connection = dataSource.getConnection()) {
            log.info("开始一键初始化...");

            // 初始化表结构
            log.info("初始化数据库表结构...");
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("sql/schema.sql"));

            // 初始化数据
            log.info("初始化数据...");
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("sql/data.sql"));

            log.info("一键初始化完成");
            return Result.success("数据库一键初始化成功");
        } catch (Exception e) {
            log.error("一键初始化失败", e);
            return Result.error("一键初始化失败: " + e.getMessage());
        }
    }
}
