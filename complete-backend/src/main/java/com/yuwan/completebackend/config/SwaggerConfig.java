package com.yuwan.completebackend.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置 (Knife4j增强版)
 * 
 * @author 系统架构师
 * @version 1.0
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("毕业设计全过程管理系统API文档")
                        .description("毕业设计全过程管理系统后端接口文档")
                        .version("V2.0")
                        .contact(new Contact()
                                .name("系统架构师")
                                .email("admin@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .externalDocs(new ExternalDocumentation()
                        .description("项目文档")
                        .url("https://github.com/example/complete-backend"));
    }

    /**
     * 全部接口分组
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("全部接口")
                .pathsToMatch("/**")
                .build();
    }

    /**
     * 认证接口分组
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("认证管理")
                .pathsToMatch("/auth/**")
                .build();
    }

    /**
     * 用户管理接口分组
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户管理")
                .pathsToMatch("/user/**", "/role/**")
                .build();
    }

    /**
     * 课题管理接口分组
     */
    @Bean
    public GroupedOpenApi topicApi() {
        return GroupedOpenApi.builder()
                .group("课题管理")
                .pathsToMatch("/topic/**")
                .build();
    }

    /**
     * 文档管理接口分组
     */
    @Bean
    public GroupedOpenApi documentApi() {
        return GroupedOpenApi.builder()
                .group("文档管理")
                .pathsToMatch("/document/**")
                .build();
    }

    /**
     * 系统管理接口分组
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("系统管理")
                .pathsToMatch("/health/**", "/system/**")
                .build();
    }
}

