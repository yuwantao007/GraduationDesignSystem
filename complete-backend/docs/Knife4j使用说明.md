# Knife4j 接口文档使用说明

## 什么是 Knife4j？

Knife4j 是一个集 Swagger2 和 OpenAPI3 为一体的增强解决方案，为 Java 开发者提供了更加优雅、更加强大的接口文档展示功能。

## 核心特性

### 1. 美观的UI界面
- 采用左右布局，更符合国人使用习惯
- 支持暗黑模式
- 界面更加简洁美观

### 2. 离线文档
- 支持导出 Markdown、HTML、Word 格式文档
- 支持接口文档离线查看

### 3. 接口调试
- 支持全局参数配置
- 支持请求参数持久化
- 支持文件上传测试
- 支持复杂参数（JSON、XML）测试

### 4. 搜索功能
- 支持接口搜索
- 支持参数搜索
- 快速定位接口

### 5. 个性化设置
- 支持自定义主题
- 支持接口分组管理
- 支持接口排序

## 访问地址

- **Knife4j文档** (推荐): http://localhost:8080/api/doc.html
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/v3/api-docs

## 接口分组

系统已按业务模块划分为以下分组：

1. **全部接口** - 查看所有接口
2. **用户管理** - 用户相关接口 (/user/**)
3. **课题管理** - 课题相关接口 (/topic/**)
4. **文档管理** - 文档相关接口 (/document/**)
5. **系统管理** - 系统相关接口 (/health/**, /system/**)

## 常用功能

### 1. 调试接口

1. 在左侧菜单选择要测试的接口
2. 点击"调试"按钮
3. 填写请求参数
4. 点击"发送"按钮
5. 查看响应结果

### 2. 导出文档

1. 点击右上角"文档管理"
2. 选择"离线文档"
3. 选择导出格式（Markdown/HTML/Word）
4. 点击"下载"按钮

### 3. 全局参数设置

1. 点击右上角"全局参数设置"
2. 添加全局Header（如：Authorization）
3. 所有接口调试时自动携带该参数

### 4. 搜索接口

1. 点击顶部搜索框
2. 输入接口名称或路径
3. 快速定位到目标接口

## 生产环境配置

在生产环境建议关闭接口文档，在 `application.yml` 中配置：

```yaml
knife4j:
  # 开启生产环境屏蔽
  production: true
```

或者通过环境变量：

```bash
--knife4j.production=true
```

## 安全配置

Knife4j 支持开启 Basic 认证，保护接口文档：

```yaml
knife4j:
  basic:
    enable: true
    username: admin
    password: admin123
```

开启后访问文档需要输入用户名和密码。

## 常见问题

### 1. 文档页面空白

- 检查是否正确引入 knife4j 依赖
- 检查是否配置了正确的包扫描路径
- 检查 Controller 是否添加了 Swagger 注解

### 2. 接口不显示

- 检查 Controller 的包路径是否在扫描范围内
- 检查是否添加了 `@RestController` 或 `@Controller` 注解
- 检查接口方法是否为 public

### 3. 参数不显示

- 检查是否使用了 `@ApiModel` 和 `@ApiModelProperty` 注解
- 检查参数对象是否有 getter/setter 方法
- 使用 Lombok 时确保已正确配置

## 注解说明

### Controller级别注解

```java
@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/user")
public class UserController {
    // ...
}
```

### 方法级别注解

```java
@Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
@GetMapping("/{id}")
public Result<UserVO> getUserById(@PathVariable String id) {
    // ...
}
```

### 参数级别注解

```java
@Schema(description = "用户信息")
@Data
public class UserVO {
    @Schema(description = "用户ID")
    private String userId;
    
    @Schema(description = "用户名称")
    private String userName;
}
```

## 最佳实践

1. **合理使用分组** - 按业务模块划分接口分组
2. **添加详细注释** - 每个接口、参数都应添加清晰的描述
3. **示例数据** - 为复杂参数提供示例数据
4. **版本管理** - 接口文档应与代码版本保持一致
5. **安全考虑** - 生产环境务必关闭或加密文档访问

## 参考资料

- [Knife4j 官方文档](https://doc.xiaominfo.com/)
- [Swagger 官方文档](https://swagger.io/docs/)
- [OpenAPI 规范](https://swagger.io/specification/)
