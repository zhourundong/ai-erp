package com.aierp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.aierp.mapper", "com.aierp.ai.mapper", "com.aierp.modules.**.mapper"})
public class AiErpApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AiErpApplication.class, args);
        System.out.println("========================================");
        System.out.println("   AI Native ERP Started Successfully!  ");
        System.out.println("   http://localhost:8080                ");
        System.out.println("   Default Admin: admin / admin123      ");
        System.out.println("========================================");
    }
}
