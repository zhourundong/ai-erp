package com.aierp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据库初始化器
 *
 * 在应用启动时检查并初始化数据库表结构
 */
@Slf4j
@Component
public class DatabaseInitializer {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @PostConstruct
    public void init() {
        log.info("开始初始化数据库...");

        // 检查表是否存在
        if (!tablesExist()) {
            log.info("数据库表不存在，开始创建表结构...");
            executeSchemaScript();
            log.info("数据库表结构创建完成");
        } else {
            log.info("数据库表已存在，跳过初始化");
        }
    }

    /**
     * 检查核心表是否存在
     */
    private boolean tablesExist() {
        try (Connection connection = dataSource.getConnection()) {
            // SQLite使用sqlite_master表检查表是否存在
            String sql = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='users'";
            try (var stmt = connection.createStatement();
                 var rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            log.warn("检查表存在时出错: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 执行schema.sql脚本
     */
    private void executeSchemaScript() {
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("schema.sql"));
            populator.setContinueOnError(false);
            populator.execute(dataSource);
            log.info("schema.sql 执行成功");
        } catch (Exception e) {
            log.error("执行schema.sql失败: {}", e.getMessage(), e);
            throw new RuntimeException("数据库初始化失败", e);
        }
    }
}
