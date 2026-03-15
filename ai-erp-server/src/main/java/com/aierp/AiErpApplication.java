package com.aierp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * AI原生ERP系统 - 主启动类
 *
 * 核心特性：
 * - AI驱动的采购供应链管理
 * - 自然语言交互界面
 * - 多模型AI服务支持
 * - AI自主执行+人工监督模式
 */
@SpringBootApplication
public class AiErpApplication {

    public static void main(String[] args) {
        // 确保SQLite数据库目录存在
        ensureDataDirectoryExists();

        SpringApplication.run(AiErpApplication.class, args);
    }

    /**
     * 确保数据库目录存在
     * SQLite需要目录存在才能创建数据库文件
     */
    private static void ensureDataDirectoryExists() {
        try {
            var dataDir = Paths.get("./data");
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
                System.out.println("创建数据目录: " + dataDir.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("创建数据目录失败: " + e.getMessage());
            throw new RuntimeException("无法创建数据目录", e);
        }
    }
}
