package com.yuwan.completebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 毕业设计全过程管理系统主启动类
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-19
 */
@SpringBootApplication
@MapperScan("com.yuwan.completebackend.mapper")
@EnableAsync
@EnableScheduling
public class CompleteBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompleteBackendApplication.class, args);
        System.out.println("========================================");
        System.out.println("毕业设计全过程管理系统启动成功！");
        System.out.println("Knife4j文档地址: http://localhost:8080/api/doc.html");
        System.out.println("Swagger文档地址: http://localhost:8080/api/swagger-ui.html");
        System.out.println("========================================");
    }
}
