# 毕业设计全过程管理系统后端

## 项目说明

这是毕业设计全过程管理系统的后端项目，基于Spring Boot 3.2.x开发。

## 技术栈

- **Java**: 17
- **Spring Boot**: 3.2.2
- **Spring Security**: 6.x
- **MyBatis Plus**: 3.5.5
- **MySQL**: 8.0+
- **Redis**: 7.0+
- **RabbitMQ**: 3.12+
- **接口文档**: Knife4j 4.4.0 (Swagger增强版)

## 项目结构

```
complete-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/yuwan/completebackend/
│   │   │       ├── CompleteBackendApplication.java  # 主启动类
│   │   │       ├── common/                          # 通用类
│   │   │       │   ├── Result.java                  # 统一响应结果
│   │   │       │   └── PageResult.java              # 分页结果
│   │   │       ├── config/                          # 配置类
│   │   │       │   ├── CorsConfig.java              # 跨域配置
│   │   │       │   ├── MyBatisPlusConfig.java       # MyBatis Plus配置
│   │   │       │   ├── SecurityConfig.java          # Spring Security配置
│   │   │       │   └── SwaggerConfig.java           # Swagger配置
│   │   │       ├── controller/                      # 控制器
│   │   │       │   └── HealthController.java        # 健康检查接口
│   │   │       ├── exception/                       # 异常处理
│   │   │       │   ├── BusinessException.java       # 业务异常
│   │   │       │   ├── SystemException.java         # 系统异常
│   │   │       │   └── GlobalExceptionHandler.java  # 全局异常处理器
│   │   │       ├── mapper/                          # Mapper层
│   │   │       ├── model/                           # 数据模型
│   │   │       │   ├── entity/                      # 实体类
│   │   │       │   ├── dto/                         # 数据传输对象
│   │   │       │   └── vo/                          # 视图对象
│   │   │       └── service/                         # 服务层
│   │   └── resources/
│   │       ├── application.yml                      # 应用配置文件
│   │       └── mapper/                              # MyBatis XML文件
│   └── test/                                        # 测试代码
└── pom.xml                                          # Maven配置文件
```

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 7.0+
- RabbitMQ 3.12+ (可选)

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd complete-backend
```

### 2. 配置数据库

修改 `src/main/resources/application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/graduation_system?...
    username: root
    password: your_password
```

### 3. 配置Redis

修改 `src/main/resources/application.yml` 中的Redis配置：

```yaml
spring:  
  data:
    redis:
      host: localhost
      port: 6379
      password: your_password
```

### 4. 编译项目

```bash
mvn clean install -DskipTests
```

### 5. 运行项目

```bash
mvn spring-boot:run
```

或者直接运行主类：

```bash
java -jar target/complete-backend-1.0.0.jar
```

### 6. 访问接口文档

启动成功后，访问：

- **Knife4j文档** (推荐): http://localhost:8080/api/doc.html
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- API文档: http://localhost:8080/api/v3/api-docs

> **推荐使用Knife4j文档**，Knife4j是Swagger的增强版，提供更美观、更强大的功能。

### 7. 测试接口

健康检查接口：

```bash
curl http://localhost:8080/api/health/check
```

预期响应：

```json
{
  "code": 200,
  "message": "健康检查成功",
  "data": {
    "status": "UP",
    "message": "系统运行正常",
    "timestamp": "2026-02-19 17:30:00",
    "version": "V2.0"
  }
}
```

## 开发规范

请参考项目根目录下的 `rule.md` 文件，了解详细的代码规范和开发规则。

## 项目进度

- [x] Maven项目结构搭建
- [x] application.yml配置文件
- [x] 主启动类
- [x] 统一响应和异常处理类
- [x] 基础配置类（跨域、Swagger、MyBatis、Security）
- [x] 示例Controller测试接口
- [ ] 用户管理模块
- [ ] 课题管理模块
- [ ] 文档管理模块
- [ ] 审批流程模块
- [ ] 质量监控模块

## 常见问题

### 1. 编译失败：无效的标记 --release

确保使用JDK 17或更高版本，并且Maven版本为3.6+。

### 2. 数据库连接失败

检查MySQL服务是否启动，数据库是否已创建，用户名密码是否正确。

### 3. Redis连接失败

检查Redis服务是否启动，端口是否正确。

## 许可证

Apache License 2.0

## 联系方式

- 邮箱: admin@example.com
- 项目地址: https://github.com/example/complete-backend
