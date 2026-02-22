# 毕业设计全过程管理系统代码规范与开发规则

## 1. 代码书写风格规范

### 1.1 命名规范

#### 1.1.1 类名命名
- 采用大驼峰命名法（PascalCase）
- 类名应具有描述性，能够清晰表达类的职责
- 接口名以"I"开头（如：IUserService）
- 抽象类以"Abstract"开头（如：AbstractController）

```java
// 正确示例
public class UserServiceImpl implements IUserService {
    // 实现代码
}

// 错误示例
public class userserviceimpl implements iuserservice {
    // 不规范的命名
}
```

#### 1.1.2 方法名命名
- 采用小驼峰命名法（camelCase）
- 动词开头，表达方法的具体行为
- 布尔类型方法以"is"、"has"、"can"等开头

```java
// 正确示例
public List<User> getUserList() {
    // 获取用户列表
}

public boolean isValidUser(String userId) {
    // 验证用户有效性
}

// 错误示例
public List<User> getuserlist() {
    // 不规范的命名
}
```

#### 1.1.3 变量名命名
- 采用小驼峰命名法（camelCase）
- 变量名应具有明确含义
- 避免使用单个字母命名（循环变量除外）

```java
// 正确示例
private String userName;
private Integer userAge;
private List<User> userList;

// 错误示例
private String n;
private Integer a;
private List<User> l;
```

#### 1.1.4 常量命名
- 全部大写，单词间用下划线分隔
- 常量应定义在类的顶部

```java
// 正确示例
public static final String DEFAULT_USER_ROLE = "STUDENT";
public static final Integer MAX_RETRY_COUNT = 3;

// 错误示例
public static final String default_user_role = "student";
```

### 1.2 代码格式规范

#### 1.2.1 缩进与空格
- 使用4个空格进行缩进（不要使用Tab）
- 大括号前后保持一致的空格规则
- 运算符前后添加空格

```java
// 正确示例
if (condition) {
    doSomething();
} else {
    doOtherThing();
}

int result = a + b * c;

// 错误示例
if(condition){
doSomething();}
else{
doOtherThing();
}

int result=a+b*c;
```

#### 1.2.2 行长度限制
- 单行代码不超过120个字符
- 超长行应适当换行并保持良好的缩进

```java
// 正确示例
String sql = "SELECT user_id, user_name, user_email, user_phone " +
             "FROM user_info " +
             "WHERE user_status = ? AND create_time > ?";

// 错误示例
String sql = "SELECT user_id, user_name, user_email, user_phone FROM user_info WHERE user_status = ? AND create_time > ?";
```

#### 1.2.3 空行规范
- 方法之间空一行
- 逻辑代码块之间可适当添加空行提高可读性
- 类声明和第一个方法之间空一行

### 1.3 注释规范

#### 1.3.1 类注释
```java
/**
 * 用户服务实现类
 * 提供用户相关的业务逻辑处理
 * 
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-19
 */
@Service
public class UserServiceImpl implements IUserService {
    // 实现代码
}
```

#### 1.3.2 方法注释
```java
/**
 * 根据用户ID获取用户信息
 * 
 * @param userId 用户ID
 * @return 用户信息对象，不存在时返回null
 * @throws BusinessException 用户不存在时抛出业务异常
 */
public User getUserById(String userId) throws BusinessException {
    // 实现代码
}
```

#### 1.3.3 行内注释
```java
// 验证用户输入参数
if (StringUtils.isEmpty(userId)) {
    throw new BusinessException("用户ID不能为空");
}

// 查询用户信息
User user = userMapper.selectById(userId);
```

### 1.4 异常处理规范

#### 1.4.1 异常捕获
```java
// 正确示例
try {
    // 业务逻辑处理
    userService.processUser(user);
} catch (BusinessException e) {
    // 记录业务异常日志
    log.warn("业务处理异常: {}", e.getMessage());
    throw e; // 重新抛出业务异常
} catch (Exception e) {
    // 记录系统异常日志
    log.error("系统异常", e);
    throw new SystemException("系统内部错误");
}
```

#### 1.4.2 异常抛出
```java
// 正确示例
if (user == null) {
    throw new BusinessException("用户不存在");
}

// 错误示例
if (user == null) {
    return null; // 不明确的返回值
}
```

## 2. 架构设计规范

### 2.1 分层架构规范

#### 2.1.0 Model层设计原则

**分层数据对象规范：**
1. **Entity实体类**：与数据库表一一对应，包含完整的表字段
2. **DTO数据传输对象**：用于接收前端请求参数，包含业务校验注解
3. **VO视图对象**：用于向前端返回数据，可包含格式化后的字段
4. **QueryVO查询对象**：专门用于接收查询条件参数

**命名规范：**
- Entity：使用业务实体名称（如：User、Topic、Document）
- DTO：业务名称 + DTO（如：CreateUserDTO、UpdateTopicDTO）
- VO：业务名称 + VO（如：UserVO、TopicDetailVO）
- QueryVO：业务名称 + QueryVO（如：UserQueryVO、TopicQueryVO）

**使用场景：**
- Controller接口参数复杂时，必须使用DTO/QueryVO封装
- 返回给前端的数据必须使用VO封装
- Service层内部传递使用Entity
- 跨服务调用使用DTO

#### 2.1.1 Model层规范
```java
// 实体类 - 对应数据库表结构
@Data
@TableName("user_info")
@ApiModel("用户信息实体")
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty("用户ID")
    private String userId;
    
    @ApiModelProperty("用户姓名")
    private String userName;
    
    @ApiModelProperty("用户邮箱")
    private String userEmail;
    
    @ApiModelProperty("创建时间")
    private Date createTime;
}

// 查询参数VO - 用于接收查询条件
@Data
@ApiModel("用户查询参数")
public class UserQueryVO {
    @ApiModelProperty("用户姓名(模糊查询)")
    private String userName;
    
    @ApiModelProperty("用户状态")
    private String userStatus;
    
    @ApiModelProperty("创建时间开始")
    private Date createTimeStart;
    
    @ApiModelProperty("创建时间结束")
    private Date createTimeEnd;
    
    @ApiModelProperty("页码")
    private Integer pageNum = 1;
    
    @ApiModelProperty("每页大小")
    private Integer pageSize = 10;
}

// 请求参数DTO - 用于接收创建/更新请求
@Data
@ApiModel("创建用户请求参数")
public class CreateUserDTO {
    @NotBlank(message = "用户姓名不能为空")
    @ApiModelProperty("用户姓名")
    private String userName;
    
    @Email(message = "邮箱格式不正确")
    @ApiModelProperty("用户邮箱")
    private String userEmail;
    
    @NotBlank(message = "用户角色不能为空")
    @ApiModelProperty("用户角色")
    private String userRole;
}

// 响应结果VO - 用于返回给前端的数据
@Data
@ApiModel("用户信息响应")
public class UserVO {
    @ApiModelProperty("用户ID")
    private String userId;
    
    @ApiModelProperty("用户姓名")
    private String userName;
    
    @ApiModelProperty("用户邮箱")
    private String userEmail;
    
    @ApiModelProperty("用户角色")
    private String userRole;
    
    @ApiModelProperty("创建时间")
    private String createTime;
    
    @ApiModelProperty("最后登录时间")
    private String lastLoginTime;
}
```

#### 2.1.2 Controller层
```java
@RestController
@RequestMapping("/api/user")
@Api(tags = "用户管理接口")
public class UserController {
    
    @Autowired
    private IUserService userService;
    
    @PostMapping("/create")
    @ApiOperation("创建用户")
    public Result<UserVO> createUser(@RequestBody @Valid CreateUserDTO createDTO) {
        UserVO userVO = userService.createUser(createDTO);
        return Result.success(userVO);
    }
    
    @GetMapping("/list")
    @ApiOperation("查询用户列表")
    public Result<PageResult<UserVO>> getUserList(UserQueryVO queryVO) {
        PageResult<UserVO> result = userService.getUserList(queryVO);
        return Result.success(result);
    }
    
    @GetMapping("/{userId}")
    @ApiOperation("获取用户详情")
    public Result<UserVO> getUserDetail(@PathVariable String userId) {
        UserVO userVO = userService.getUserDetail(userId);
        return Result.success(userVO);
    }
    
    @PutMapping("/{userId}")
    @ApiOperation("更新用户信息")
    public Result<UserVO> updateUser(@PathVariable String userId, 
                                   @RequestBody @Valid UpdateUserDTO updateDTO) {
        UserVO userVO = userService.updateUser(userId, updateDTO);
        return Result.success(userVO);
    }
    
    // 复杂参数示例 - 使用DTO封装
    @PostMapping("/batch-update")
    @ApiOperation("批量更新用户状态")
    public Result<Boolean> batchUpdateUserStatus(@RequestBody @Valid BatchUpdateUserStatusDTO batchDTO) {
        boolean result = userService.batchUpdateUserStatus(batchDTO);
        return Result.success(result);
    }
    
    // 多参数查询示例 - 使用QueryVO封装
    @GetMapping("/advanced-search")
    @ApiOperation("高级搜索用户")
    public Result<PageResult<UserVO>> advancedSearchUsers(UserAdvancedQueryVO queryVO) {
        PageResult<UserVO> result = userService.advancedSearchUsers(queryVO);
        return Result.success(result);
    }
}

// 复杂参数DTO示例
@Data
@ApiModel("批量更新用户状态请求")
public class BatchUpdateUserStatusDTO {
    @NotEmpty(message = "用户ID列表不能为空")
    @ApiModelProperty("用户ID列表")
    private List<String> userIds;
    
    @NotBlank(message = "状态不能为空")
    @ApiModelProperty("目标状态")
    private String targetStatus;
    
    @ApiModelProperty("操作原因")
    private String reason;
    
    @ApiModelProperty("是否发送通知")
    private Boolean sendNotification = false;
}

// 复杂查询QueryVO示例
@Data
@ApiModel("用户高级查询参数")
public class UserAdvancedQueryVO {
    @ApiModelProperty("用户姓名(模糊查询)")
    private String userName;
    
    @ApiModelProperty("用户邮箱(模糊查询)")
    private String userEmail;
    
    @ApiModelProperty("用户角色列表")
    private List<String> userRoles;
    
    @ApiModelProperty("创建时间范围开始")
    private Date createTimeStart;
    
    @ApiModelProperty("创建时间范围结束")
    private Date createTimeEnd;
    
    @ApiModelProperty("最后登录时间范围开始")
    private Date lastLoginTimeStart;
    
    @ApiModelProperty("最后登录时间范围结束")
    private Date lastLoginTimeEnd;
    
    @ApiModelProperty("用户状态")
    private String userStatus;
    
    @ApiModelProperty("排序字段")
    private String orderBy = "create_time";
    
    @ApiModelProperty("排序方式")
    private String orderType = "desc";
    
    @ApiModelProperty("页码")
    private Integer pageNum = 1;
    
    @ApiModelProperty("每页大小")
    private Integer pageSize = 10;
}
```

#### 2.1.3 Service层
```java
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements IUserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public UserVO createUser(CreateUserDTO createDTO) {
        // 参数校验已在Controller层完成
        
        // 业务逻辑处理
        User user = new User();
        BeanUtils.copyProperties(createDTO, user);
        user.setUserId(IdUtil.simpleUUID());
        user.setCreateTime(new Date());
        user.setUserStatus("ACTIVE");
        
        // 数据持久化
        userMapper.insert(user);
        
        // 转换为VO返回
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setCreateTime(DateUtil.formatDateTime(user.getCreateTime()));
        
        return userVO;
    }
    
    @Override
    public PageResult<UserVO> getUserList(UserQueryVO queryVO) {
        // 构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(queryVO.getUserName())) {
            queryWrapper.like("user_name", queryVO.getUserName());
        }
        if (StringUtils.isNotBlank(queryVO.getUserStatus())) {
            queryWrapper.eq("user_status", queryVO.getUserStatus());
        }
        
        // 分页查询
        Page<User> page = new Page<>(queryVO.getPageNum(), queryVO.getPageSize());
        IPage<User> userPage = userMapper.selectPage(page, queryWrapper);
        
        // 转换为VO列表
        List<UserVO> userVOList = userPage.getRecords().stream()
            .map(user -> {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                userVO.setCreateTime(DateUtil.formatDateTime(user.getCreateTime()));
                return userVO;
            })
            .collect(Collectors.toList());
        
        return new PageResult<>(userVOList, userPage.getTotal());
    }
    
    @Override
    public UserVO getUserDetail(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setCreateTime(DateUtil.formatDateTime(user.getCreateTime()));
        
        return userVO;
    }
    
    @Override
    public UserVO updateUser(String userId, UpdateUserDTO updateDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 更新用户信息
        BeanUtils.copyProperties(updateDTO, user);
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
        
        // 转换为VO返回
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setCreateTime(DateUtil.formatDateTime(user.getCreateTime()));
        
        return userVO;
    }
}
```

#### 2.1.4 Mapper层
```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    @Select("SELECT * FROM user_info WHERE user_name = #{userName} AND user_status = 'ACTIVE'")
    User selectByUserName(@Param("userName") String userName);
    
    @Update("UPDATE user_info SET user_status = #{status}, update_time = NOW() WHERE user_id = #{userId}")
    int updateUserStatus(@Param("userId") String userId, @Param("status") String status);
    
    @Select("SELECT COUNT(*) FROM user_info WHERE user_status = 'ACTIVE'")
    Integer countActiveUsers();
}
```


### 2.2 数据库设计规范

#### 2.2.1 表命名规范
- 表名采用小写字母，单词间用下划线分隔
- 表名应具有明确含义
- 关联表名格式：表1_表2_relation

```sql
-- 正确示例
CREATE TABLE user_info (
    user_id VARCHAR(32) PRIMARY KEY COMMENT '用户ID',
    user_name VARCHAR(50) NOT NULL COMMENT '用户姓名',
    user_email VARCHAR(100) COMMENT '用户邮箱',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT '用户信息表';

-- 错误示例
CREATE TABLE UserInfo (
    UserId VARCHAR(32) PRIMARY KEY,
    UserName VARCHAR(50) NOT NULL
);
```

#### 2.2.2 字段命名规范
- 字段名采用小写字母，单词间用下划线分隔
- 主键字段统一命名为"id"或"表名_id"
- 时间字段统一后缀：_time、_date

### 2.3 API设计规范

#### 2.3.1 RESTful API设计
```java
// 正确示例
@GetMapping("/users")           // 查询用户列表
@PostMapping("/users")          // 创建用户
@GetMapping("/users/{id}")      // 查询单个用户
@PutMapping("/users/{id}")      // 更新用户
@DeleteMapping("/users/{id}")   // 删除用户
```

#### 2.3.2 统一响应格式
```java
public class Result<T> {
    private Integer code;      // 响应码
    private String message;    // 响应消息
    private T data;            // 响应数据
    
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }
    
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
```

## 3. 开发待完成事项清单

### 3.1 后端开发完成事项

#### 3.1.1 基础框架搭建
- [√] Spring Boot项目初始化
- [√] Maven依赖配置完成
- [√] 数据库连接配置完成
- [√] Redis缓存配置完成
- [√] 日志框架配置完成
- [√] 统一异常处理配置完成
- [√] 跨域配置完成
- [√] Swagger+knife4j接口文档配置完成

#### 3.1.2 核心模块开发
- [ ] 用户管理模块
  - [ ] 用户注册接口
  - [ ] 用户登录接口
  - [ ] 用户信息查询接口
  - [ ] 用户信息修改接口
  - [ ] 用户权限管理接口

- [ ] 课题管理模块
  - [ ] 课题申报接口
  - [ ] 课题审批接口
  - [ ] 课题查询接口
  - [ ] 课题修改接口
  - [ ] 课题删除接口

- [ ] 文档管理模块
  - [ ] 文档上传接口
  - [ ] 文档下载接口
  - [ ] 文档预览接口
  - [ ] 文档版本管理接口
  - [ ] 文档权限控制接口

- [ ] 审批流程模块
  - [ ] 流程定义接口
  - [ ] 流程实例管理接口
  - [ ] 任务分配接口
  - [ ] 审批操作接口
  - [ ] 流程监控接口

- [ ] 质量监控模块
  - [ ] 数据统计接口
  - [ ] 图表数据接口
  - [ ] 预警通知接口
  - [ ] 报表生成接口

#### 3.1.3 系统管理模块
- [ ] 系统配置管理
- [ ] 数据字典管理
- [ ] 操作日志管理
- [ ] 系统监控接口
- [ ] 数据备份接口

### 3.2 前端开发完成事项

#### 3.2.1 基础环境搭建
- [ ] Vue.js项目初始化
- [ ] 路由配置完成
- [ ] 状态管理配置完成
- [ ] HTTP请求封装完成
- [ ] 权限控制配置完成
- [ ] 国际化配置完成
- [ ] 主题样式配置完成

#### 3.2.2 页面组件开发
- [ ] 登录页面
- [ ] 首页仪表盘
- [ ] 用户管理页面
- [ ] 课题管理页面
- [ ] 文档管理页面
- [ ] 审批流程页面
- [ ] 质量监控页面
- [ ] 系统设置页面

#### 3.2.3 组件库建设
- [ ] 公共组件库
- [ ] 业务组件库
- [ ] 表单组件
- [ ] 表格组件
- [ ] 图表组件
- [ ] 通知组件

### 3.3 测试完成事项

#### 3.3.1 单元测试
- [ ] Controller层单元测试
- [ ] Service层单元测试
- [ ] Mapper层单元测试
- [ ] 工具类单元测试
- [ ] 单元测试覆盖率≥80%

#### 3.3.2 集成测试
- [ ] 接口集成测试
- [ ] 业务流程测试
- [ ] 数据一致性测试
- [ ] 性能压力测试
- [ ] 安全测试


### 3.5 文档编写完成事项

#### 3.5.1 技术文档
- [ ] 系统架构设计文档
- [ ] 数据库设计文档
- [ ] API接口文档
- [ ] 部署手册
- [ ] 运维手册

#### 3.5.2 用户文档
- [ ] 用户操作手册
- [ ] 管理员手册
- [ ] 常见问题解答
- [ ] 视频教程制作

## 4. 代码审查规范

### 4.1 代码审查要点
- 功能实现是否正确完整
- 代码是否符合规范要求
- 异常处理是否完善
- 性能优化是否合理
- 安全性考虑是否充分
- 可维护性是否良好

### 4.2 审查流程
1. 开发人员自测通过
2. 提交代码审查申请
3. 指定审查人员进行审查
4. 记录审查意见和建议
5. 开发人员修改完善
6. 审查通过后合并代码

## 5. 版本管理规范

### 5.1 分支管理策略
- main：生产环境稳定版本
- develop：开发环境最新版本
- feature/xxx：功能开发分支
- hotfix/xxx：紧急修复分支
- release/xxx：发布准备分支

### 5.2 提交信息规范
```bash
# 格式：类型(模块): 简要描述
feat(user): 添加用户注册功能
fix(topic): 修复课题审批流程问题
docs(readme): 更新README文档
style(css): 调整页面样式
refactor(service): 重构用户服务代码
test(api): 添加API接口测试
chore(config): 更新配置文件
```

## 6. 接口参数封装规范

### 5.1 参数封装原则

**简洁性原则**：
- Controller接口参数超过3个时，必须使用DTO/QueryVO封装
- 简单查询接口可直接使用@RequestParam接收参数
- 复杂业务操作必须使用DTO封装请求参数

**一致性原则**：
- 同一业务模块的参数封装风格保持一致
- 查询参数统一使用QueryVO后缀
- 请求参数统一使用DTO后缀
- 响应数据统一使用VO后缀

**可维护性原则**：
- 参数对象添加详细注释说明
- 使用Swagger注解完善API文档
- 参数校验注解明确业务规则
- 为复杂参数提供示例数据

### 6.2 参数封装最佳实践

```java
// ✅ 推荐做法：复杂参数使用DTO封装
@PostMapping("/complex-operation")
public Result<Boolean> complexOperation(@RequestBody @Valid ComplexOperationDTO request) {
    return Result.success(service.complexOperation(request));
}

// ✅ 推荐做法：简单查询可直接使用参数
@GetMapping("/simple-query")
public Result<List<UserVO>> simpleQuery(
    @RequestParam(required = false) String userName,
    @RequestParam(required = false) String userStatus) {
    return Result.success(service.simpleQuery(userName, userStatus));
}

// ❌ 不推荐做法：参数过多时不封装
@PostMapping("/bad-example")
public Result<Boolean> badExample(
    @RequestParam String userId,
    @RequestParam String status,
    @RequestParam String reason,
    @RequestParam Boolean notify,
    @RequestParam String operator) {
    // 参数过多，难以维护
    return Result.success(false);
}
```

### 6.3 参数校验规范

```java
// DTO参数校验示例
@Data
@ApiModel("用户创建请求")
public class CreateUserDTO {
    @NotBlank(message = "用户姓名不能为空")
    @Length(min = 2, max = 20, message = "用户姓名长度必须在2-20个字符之间")
    @ApiModelProperty("用户姓名")
    private String userName;
    
    @NotBlank(message = "用户邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @ApiModelProperty("用户邮箱")
    private String userEmail;
    
    @NotBlank(message = "用户角色不能为空")
    @Pattern(regexp = "STUDENT|TEACHER|ADMIN", message = "用户角色必须为STUDENT/TEACHER/ADMIN")
    @ApiModelProperty("用户角色")
    private String userRole;
    
    @Min(value = 18, message = "年龄不能小于18岁")
    @Max(value = 100, message = "年龄不能大于100岁")
    @ApiModelProperty("用户年龄")
    private Integer userAge;
}
```

## 7. 性能优化规范

### 7.1 数据库优化
- 合理设计索引
- 避免N+1查询问题
- 使用连接池管理
- 定期分析慢查询

### 7.2 代码优化
- 避免重复计算
- 合理使用缓存
- 减少对象创建
- 优化循环处理

### 7.3 网络优化
- 合理设置超时时间
- 使用连接复用
- 压缩传输数据
- CDN加速静态资源

## 8. 前端开发规范

### 格式模板规范
 - <template>

 </template>
 <script setup lang="ts">

 </script>
 <style scoped>

 </style>

### 8.1 前端命名规范

#### 8.1.1 文件命名规范
- Vue组件文件名采用大驼峰命名法（PascalCase）
- 工具函数文件名采用小驼峰命名法（camelCase）
- 样式文件采用小写字母+连字符（kebab-case）
- 常量配置文件全部大写+下划线

```typescript
// ✅ 正确示例
components/
  UserList.vue           // 组件使用大驼峰
  UserDetail.vue
utils/
  formatDate.ts          // 工具函数使用小驼峰
  validateForm.ts
styles/
  user-list.scss         // 样式文件使用kebab-case
  common-layout.scss
constants/
  API_CONFIG.ts          // 常量文件全大写
  USER_ROLES.ts

// ❌ 错误示例
components/
  userlist.vue           // 不规范
  user-detail.vue        // 组件不应使用kebab-case
utils/
  FormatDate.ts          // 工具函数不应大驼峰
```

#### 8.1.2 变量命名规范
```typescript
// ✅ 正确示例 - 变量使用小驼峰
const userName = ref<string>('')
const userList = ref<User[]>([])
const isLoading = ref<boolean>(false)
const hasPermission = computed(() => store.hasPermission('user:edit'))

// 常量使用全大写+下划线
const MAX_RETRY_COUNT = 3
const DEFAULT_PAGE_SIZE = 10
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

// ❌ 错误示例
const UserName = ref('')           // 变量不应大驼峰
const user_list = ref([])          // 不应使用下划线
const Is_Loading = ref(false)      // 混合风格
```

#### 8.1.3 函数命名规范
```typescript
// ✅ 正确示例 - 动词开头的小驼峰
const getUserList = async () => {
  // 获取用户列表
}

const handleSubmit = () => {
  // 处理提交
}

const validateForm = (): boolean => {
  // 验证表单
  return true
}

const isValidEmail = (email: string): boolean => {
  // 判断邮箱格式
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

// ❌ 错误示例
const GetUserList = () => {}       // 不应大驼峰
const submit = () => {}            // 缺少动词前缀
const user_list = () => {}         // 不应使用下划线
```

#### 8.1.4 组件命名规范
```typescript
// ✅ 正确示例
// UserList.vue - 组件名多个单词，大驼峰
<script setup lang="ts">
defineOptions({
  name: 'UserList'  // 组件name与文件名一致
})
</script>

// UserDetailModal.vue - 业务组件名称具有描述性
<script setup lang="ts">
defineOptions({
  name: 'UserDetailModal'
})
</script>

// ❌ 错误示例
// User.vue - 单个单词容易与HTML元素冲突
// userlist.vue - 文件名应大驼峰
// user-detail-modal.vue - 文件名不应kebab-case
```

### 8.2 Vue 组件开发规范

#### 8.2.1 组件结构规范
```vue
<!-- ✅ 推荐的组件结构顺序 -->
<template>
  <div class="user-list">
    <!-- 模板内容 -->
  </div>
</template>

<script setup lang="ts">
// 1. 导入依赖
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { User, UserQueryParams } from '@/types/user'

// 2. 定义组件选项
defineOptions({
  name: 'UserList'
})

// 3. 定义Props
interface Props {
  status?: string
  pageSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  status: 'active',
  pageSize: 10
})

// 4. 定义Emits
const emit = defineEmits<{
  change: [id: string]
  delete: [user: User]
}>()

// 5. 响应式数据
const userList = ref<User[]>([])
const loading = ref(false)
const searchForm = reactive<UserQueryParams>({
  userName: '',
  status: props.status
})

// 6. 计算属性
const filteredUsers = computed(() => {
  return userList.value.filter(user => user.status === 'active')
})

// 7. 方法定义
const getUserList = async () => {
  loading.value = true
  try {
    const res = await userApi.getUserList(searchForm)
    userList.value = res.data
  } catch (error) {
    console.error('获取用户列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleDelete = (user: User) => {
  emit('delete', user)
}

// 8. 生命周期钩子
onMounted(() => {
  getUserList()
})

// 9. 暴露给模板的方法（如需要）
defineExpose({
  getUserList
})
</script>

<style scoped lang="scss">
// 样式内容
.user-list {
  padding: 20px;
  
  &__header {
    margin-bottom: 16px;
  }
}
</style>
```

#### 8.2.2 Props 定义规范
```typescript
// ✅ 推荐做法：使用TypeScript接口定义Props
interface Props {
  // 必填props
  userId: string
  userName: string
  
  // 可选props，提供默认值
  userStatus?: 'active' | 'inactive' | 'pending'
  pageSize?: number
  showHeader?: boolean
  
  // 复杂类型props
  userData?: User
  userList?: User[]
  
  // 函数类型props
  onSuccess?: (data: User) => void
  onError?: (error: Error) => void
}

const props = withDefaults(defineProps<Props>(), {
  userStatus: 'active',
  pageSize: 10,
  showHeader: true
})

// ❌ 不推荐做法：缺少类型定义
const props = defineProps({
  userId: String,
  userName: String,
  pageSize: {
    type: Number,
    default: 10
  }
})
```

#### 8.2.3 Emits 定义规范
```typescript
// ✅ 推荐做法：明确定义事件类型
const emit = defineEmits<{
  // 事件名: [参数类型1, 参数类型2, ...]
  update: [value: string]
  change: [user: User]
  delete: [userId: string]
  submit: [formData: UserFormData, callback: () => void]
}>()

// 使用emit
emit('update', 'newValue')
emit('change', userData)
emit('delete', '123456')

// ❌ 不推荐做法：不定义类型
const emit = defineEmits(['update', 'change', 'delete'])
```

#### 8.2.4 组件通信规范
```typescript
// ✅ 父子组件通信
// 父组件 -> 子组件：使用Props
<UserDetail :user-id="selectedUserId" :editable="true" />

// 子组件 -> 父组件：使用Emits
// 子组件
emit('update:userId', newUserId)

// 父组件
<UserDetail @update:userId="handleUserIdUpdate" />

// ✅ 跨层级组件通信：使用 provide/inject
// 父组件
import { provide, ref } from 'vue'

const userPermissions = ref<string[]>([])
provide('userPermissions', userPermissions)

// 子孙组件
import { inject } from 'vue'

const permissions = inject<Ref<string[]>>('userPermissions', ref([]))

// ✅ 全局状态管理：使用Pinia
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const currentUser = computed(() => userStore.currentUser)
```

### 8.3 TypeScript 类型规范

#### 8.3.1 类型定义规范
```typescript
// ✅ 推荐：类型定义放在 types 目录
// src/types/user.ts

// 基础类型定义
export interface User {
  userId: string
  userName: string
  userEmail: string
  userRole: UserRole
  userStatus: UserStatus
  createTime: string
  updateTime?: string
}

// 枚举类型
export enum UserRole {
  STUDENT = 'STUDENT',
  TEACHER = 'TEACHER',
  ADMIN = 'ADMIN'
}

// 联合类型
export type UserStatus = 'active' | 'inactive' | 'pending'

// 查询参数类型
export interface UserQueryParams {
  userName?: string
  userStatus?: UserStatus
  userRole?: UserRole
  createTimeStart?: string
  createTimeEnd?: string
  pageNum?: number
  pageSize?: number
}

// API响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

// DTO类型
export interface CreateUserDTO {
  userName: string
  userEmail: string
  userPassword: string
  userRole: UserRole
}

export interface UpdateUserDTO {
  userName?: string
  userEmail?: string
  userStatus?: UserStatus
}

// ❌ 不推荐：使用any类型
const userData: any = {}  // 避免使用any

// ✅ 推荐：使用具体类型或unknown
const userData: User = {} as User
const responseData: unknown = await fetchData()
```

#### 8.3.2 工具类型使用
```typescript
// ✅ 灵活使用TypeScript工具类型

// Partial - 所有属性变为可选
type PartialUser = Partial<User>

// Required - 所有属性变为必填
type RequiredUser = Required<User>

// Pick - 选择部分属性
type UserBasicInfo = Pick<User, 'userId' | 'userName' | 'userEmail'>

// Omit - 排除部分属性
type UserWithoutPassword = Omit<User, 'userPassword'>

// Record - 构建键值对类型
type UserMap = Record<string, User>

// 实际应用示例
interface UserFormData extends Omit<User, 'userId' | 'createTime' | 'updateTime'> {
  // 表单数据不包含自动生成的字段
}

const userCache: Record<string, User> = {}
const basicInfo: Pick<User, 'userId' | 'userName'> = {
  userId: '123',
  userName: 'Tom'
}
```

### 8.4 API 调用规范

#### 8.4.1 API 封装规范
```typescript
// ✅ src/api/user.ts - API接口定义
import request from './request'
import type { User, UserQueryParams, CreateUserDTO, UpdateUserDTO, PageResult } from '@/types/user'

/**
 * 用户相关API
 */
export const userApi = {
  /**
   * 获取用户列表
   */
  getUserList(params: UserQueryParams) {
    return request.get<PageResult<User>>('/user/list', { params })
  },

  /**
   * 获取用户详情
   */
  getUserDetail(userId: string) {
    return request.get<User>(`/user/${userId}`)
  },

  /**
   * 创建用户
   */
  createUser(data: CreateUserDTO) {
    return request.post<User>('/user/create', data)
  },

  /**
   * 更新用户
   */
  updateUser(userId: string, data: UpdateUserDTO) {
    return request.put<User>(`/user/${userId}`, data)
  },

  /**
   * 删除用户
   */
  deleteUser(userId: string) {
    return request.delete<boolean>(`/user/${userId}`)
  },

  /**
   * 批量删除用户
   */
  batchDeleteUsers(userIds: string[]) {
    return request.post<boolean>('/user/batch-delete', { userIds })
  }
}

// 组件中使用
import { userApi } from '@/api/user'

const getUserList = async () => {
  try {
    const res = await userApi.getUserList({ pageNum: 1, pageSize: 10 })
    userList.value = res.records
  } catch (error) {
    console.error('获取用户列表失败', error)
  }
}
```

#### 8.4.2 请求拦截器规范
```typescript
// ✅ src/api/request.ts - Axios封装
import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 添加Token
    const userStore = useUserStore()
    const token = userStore.token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 根据业务约定的code处理
    if (res.code === 200) {
      return res.data
    }
    
    // 处理业务错误
    message.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    // 处理HTTP错误
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          message.error('登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          break
        case 403:
          message.error('没有权限访问')
          break
        case 404:
          message.error('请求的资源不存在')
          break
        case 500:
          message.error('服务器错误')
          break
        default:
          message.error(data?.message || '请求失败')
      }
    } else if (error.request) {
      message.error('网络连接失败，请检查网络')
    } else {
      message.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

// 封装请求方法
class HttpRequest {
  get<T = any>(url: string, config?: AxiosRequestConfig) {
    return service.get<any, T>(url, config)
  }

  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
    return service.post<any, T>(url, data, config)
  }

  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
    return service.put<any, T>(url, data, config)
  }

  delete<T = any>(url: string, config?: AxiosRequestConfig) {
    return service.delete<any, T>(url, config)
  }
}

export default new HttpRequest()
```

### 8.5 状态管理规范（Pinia）

#### 8.5.1 Store 定义规范
```typescript
// ✅ src/stores/user.ts - 用户状态管理
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types/user'
import { authApi } from '@/api/auth'
import { getToken, setToken, removeToken, clearAuth } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  // State - 使用ref定义
  const token = ref<string>(getToken() || '')
  const userInfo = ref<User | null>(null)
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])

  // Getters - 使用computed定义
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.userName || '')
  const userRole = computed(() => userInfo.value?.userRole || '')

  // Actions - 使用函数定义
  /**
   * 用户登录
   */
  const login = async (loginForm: { username: string; password: string }) => {
    try {
      const res = await authApi.login(loginForm)
      token.value = res.token
      setToken(res.token)
      
      // 登录成功后获取用户信息
      await getUserInfo()
      
      return res
    } catch (error) {
      console.error('登录失败', error)
      throw error
    }
  }

  /**
   * 获取用户信息
   */
  const getUserInfo = async () => {
    try {
      const res = await authApi.getCurrentUser()
      userInfo.value = res
      roles.value = res.roles || []
      permissions.value = res.permissions || []
      return res
    } catch (error) {
      console.error('获取用户信息失败', error)
      throw error
    }
  }

  /**
   * 用户登出
   */
  const logout = async () => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('登出失败', error)
    } finally {
      // 清空状态
      token.value = ''
      userInfo.value = null
      roles.value = []
      permissions.value = []
      clearAuth()
    }
  }

  /**
   * 检查权限
   */
  const hasPermission = (permission: string): boolean => {
    return permissions.value.includes(permission)
  }

  /**
   * 检查角色
   */
  const hasRole = (role: string): boolean => {
    return roles.value.includes(role)
  }

  return {
    // State
    token,
    userInfo,
    roles,
    permissions,
    // Getters
    isLoggedIn,
    userName,
    userRole,
    // Actions
    login,
    getUserInfo,
    logout,
    hasPermission,
    hasRole
  }
})
```

#### 8.5.2 Store 使用规范
```typescript
// ✅ 在组件中使用Store
<script setup lang="ts">
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 访问state
const userName = computed(() => userStore.userName)

// 调用action
const handleLogin = async () => {
  await userStore.login({ username: 'admin', password: '123456' })
}

// 检查权限
const canEdit = computed(() => userStore.hasPermission('user:edit'))
</script>

// ❌ 不推荐：直接解构会失去响应性
const { userName, userRole } = userStore  // 失去响应性

// ✅ 推荐：使用storeToRefs解构
import { storeToRefs } from 'pinia'
const { userName, userRole } = storeToRefs(userStore)  // 保持响应性
const { login, logout } = userStore  // actions可以直接解构
```

### 8.6 路由管理规范

#### 8.6.1 路由定义规范
```typescript
// ✅ src/router/index.ts - 路由配置
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// 路由元信息类型定义
declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
    requiresAuth?: boolean
    roles?: string[]
    permissions?: string[]
    keepAlive?: boolean
    hidden?: boolean
  }
}

// 公共路由（无需权限）
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: {
      title: '登录',
      hidden: true
    }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: {
      title: '404',
      hidden: true
    }
  }
]

// 需要权限的路由
export const asyncRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: {
          title: '首页',
          icon: 'HomeOutlined',
          requiresAuth: true
        }
      },
      {
        path: '/user',
        name: 'User',
        meta: {
          title: '用户管理',
          icon: 'UserOutlined',
          requiresAuth: true,
          permissions: ['user:view']
        },
        children: [
          {
            path: '/user/list',
            name: 'UserList',
            component: () => import('@/views/user/UserList.vue'),
            meta: {
              title: '用户列表',
              requiresAuth: true,
              permissions: ['user:view'],
              keepAlive: true
            }
          },
          {
            path: '/user/detail/:id',
            name: 'UserDetail',
            component: () => import('@/views/user/UserDetail.vue'),
            meta: {
              title: '用户详情',
              requiresAuth: true,
              permissions: ['user:view'],
              hidden: true
            }
          }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [...constantRoutes, ...asyncRoutes],
  scrollBehavior: () => ({ top: 0 })
})

export default router
```

#### 8.6.2 路由守卫规范
```typescript
// ✅ src/router/guards.ts - 路由守卫
import type { Router } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { message } from 'ant-design-vue'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 白名单路由
const whiteList = ['/login', '/register', '/404']

/**
 * 设置路由守卫
 */
export function setupRouterGuard(router: Router) {
  // 前置守卫
  router.beforeEach(async (to, from, next) => {
    NProgress.start()

    // 设置页面标题
    document.title = `${to.meta.title || '系统'} - 毕业设计管理系统`

    const userStore = useUserStore()

    // 检查是否已登录
    if (userStore.isLoggedIn) {
      if (to.path === '/login') {
        // 已登录，跳转到首页
        next({ path: '/' })
      } else {
        // 检查是否已获取用户信息
        if (!userStore.userInfo) {
          try {
            await userStore.getUserInfo()
          } catch (error) {
            // 获取用户信息失败，清除token并跳转登录
            await userStore.logout()
            message.error('获取用户信息失败，请重新登录')
            next({ path: '/login', query: { redirect: to.fullPath } })
            return
          }
        }

        // 权限检查
        if (to.meta.permissions && to.meta.permissions.length > 0) {
          const hasPermission = to.meta.permissions.some(permission =>
            userStore.hasPermission(permission)
          )
          if (!hasPermission) {
            message.error('没有权限访问该页面')
            next({ path: '/403' })
            return
          }
        }

        // 角色检查
        if (to.meta.roles && to.meta.roles.length > 0) {
          const hasRole = to.meta.roles.some(role => userStore.hasRole(role))
          if (!hasRole) {
            message.error('没有权限访问该页面')
            next({ path: '/403' })
            return
          }
        }

        next()
      }
    } else {
      // 未登录
      if (whiteList.includes(to.path)) {
        // 白名单路由直接放行
        next()
      } else {
        // 跳转登录页
        next({ path: '/login', query: { redirect: to.fullPath } })
      }
    }
  })

  // 后置守卫
  router.afterEach(() => {
    NProgress.done()
  })

  // 错误处理
  router.onError((error) => {
    console.error('路由错误', error)
    message.error('页面加载失败')
  })
}
```

### 8.7 样式规范

#### 8.7.1 CSS 命名规范（BEM）
```scss
// ✅ 推荐：使用BEM命名规范
// Block - Element - Modifier

// src/views/user/UserList.vue
<style scoped lang="scss">
// Block: user-list
.user-list {
  padding: 20px;
  background: #fff;

  // Element: user-list__header
  &__header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 16px;
  }

  // Element: user-list__search
  &__search {
    display: flex;
    gap: 12px;
  }

  // Element: user-list__table
  &__table {
    margin-top: 16px;
  }

  // Element: user-list__actions
  &__actions {
    display: flex;
    gap: 8px;
  }

  // Modifier: user-list--loading
  &--loading {
    opacity: 0.6;
    pointer-events: none;
  }
}

// ❌ 不推荐：随意命名
.header {
  // 容易与其他组件的header冲突
}

.btn {
  // 命名太宽泛
}

.box1 {
  // 无意义的命名
}
</style>
```

#### 8.7.2 SCSS 变量规范
```scss
// ✅ src/styles/variables.scss - 全局变量
// 颜色变量
$primary-color: #1890ff;
$success-color: #52c41a;
$warning-color: #faad14;
$error-color: #f5222d;
$info-color: #1890ff;

$text-primary: #262626;
$text-secondary: #8c8c8c;
$text-disabled: #bfbfbf;

$border-color: #d9d9d9;
$background-color: #f0f2f5;

// 尺寸变量
$header-height: 64px;
$sidebar-width: 200px;
$sidebar-collapsed-width: 64px;

$border-radius-base: 4px;
$border-radius-lg: 8px;
$border-radius-sm: 2px;

// 间距变量
$spacing-xs: 4px;
$spacing-sm: 8px;
$spacing-md: 16px;
$spacing-lg: 24px;
$spacing-xl: 32px;

// 字体变量
$font-size-sm: 12px;
$font-size-base: 14px;
$font-size-lg: 16px;
$font-size-xl: 18px;

// 使用变量
.user-list {
  padding: $spacing-lg;
  background: $background-color;
  border-radius: $border-radius-base;

  &__header {
    color: $text-primary;
    font-size: $font-size-lg;
  }
}
```

#### 8.7.3 响应式设计规范
```scss
// ✅ src/styles/mixins.scss - 响应式Mixin
@mixin respond-to($breakpoint) {
  @if $breakpoint == 'xs' {
    @media (max-width: 576px) {
      @content;
    }
  } @else if $breakpoint == 'sm' {
    @media (min-width: 577px) and (max-width: 768px) {
      @content;
    }
  } @else if $breakpoint == 'md' {
    @media (min-width: 769px) and (max-width: 992px) {
      @content;
    }
  } @else if $breakpoint == 'lg' {
    @media (min-width: 993px) and (max-width: 1200px) {
      @content;
    }
  } @else if $breakpoint == 'xl' {
    @media (min-width: 1201px) {
      @content;
    }
  }
}

// 使用示例
.user-list {
  padding: 24px;

  @include respond-to('xs') {
    padding: 12px;
  }

  @include respond-to('sm') {
    padding: 16px;
  }

  &__header {
    display: flex;

    @include respond-to('xs') {
      flex-direction: column;
    }
  }
}
```

### 8.8 代码质量规范

#### 8.8.1 ESLint 配置
```javascript
// ✅ .eslintrc.cjs - ESLint配置
module.exports = {
  root: true,
  env: {
    browser: true,
    es2021: true,
    node: true
  },
  extends: [
    'plugin:vue/vue3-essential',
    'eslint:recommended',
    '@vue/eslint-config-typescript',
    '@vue/eslint-config-prettier'
  ],
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module'
  },
  rules: {
    // Vue规则
    'vue/multi-word-component-names': 'error',
    'vue/no-unused-vars': 'error',
    'vue/no-v-html': 'warn',
    'vue/require-default-prop': 'error',
    
    // TypeScript规则
    '@typescript-eslint/no-explicit-any': 'warn',
    '@typescript-eslint/no-unused-vars': 'error',
    '@typescript-eslint/explicit-module-boundary-types': 'off',
    
    // 通用规则
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-unused-vars': 'off',
    'no-undef': 'off'
  }
}
```

#### 8.8.2 注释规范
```typescript
// ✅ 推荐的注释方式

/**
 * 用户列表组件
 * @description 展示系统用户列表，支持搜索、分页、编辑、删除等功能
 * @author 张三
 * @date 2026-02-20
 */

/**
 * 获取用户列表
 * @param params 查询参数
 * @param params.userName 用户名（支持模糊查询）
 * @param params.userStatus 用户状态
 * @param params.pageNum 页码
 * @param params.pageSize 每页数量
 * @returns 用户列表数据
 * @example
 * ```ts
 * const result = await getUserList({ pageNum: 1, pageSize: 10 })
 * ```
 */
const getUserList = async (params: UserQueryParams): Promise<PageResult<User>> => {
  // 参数校验
  if (!params.pageNum || params.pageNum < 1) {
    params.pageNum = 1
  }
  
  try {
    // 调用API获取数据
    const res = await userApi.getUserList(params)
    return res
  } catch (error) {
    console.error('获取用户列表失败', error)
    throw error
  }
}

// 单行注释：说明关键代码逻辑
// TODO: 待优化的功能
// FIXME: 需要修复的问题
// NOTE: 重要说明
```

### 8.9 性能优化规范

#### 8.9.1 组件懒加载
```typescript
// ✅ 路由懒加载
const routes = [
  {
    path: '/user',
    component: () => import('@/views/user/UserList.vue')  // 懒加载
  }
]

// ✅ 组件懒加载
<script setup lang="ts">
import { defineAsyncComponent } from 'vue'

const UserDetail = defineAsyncComponent(() => 
  import('@/components/UserDetail.vue')
)
</script>

// ❌ 不推荐：一次性导入所有组件
import UserList from '@/views/user/UserList.vue'
import UserDetail from '@/views/user/UserDetail.vue'
import UserEdit from '@/views/user/UserEdit.vue'
```

#### 8.9.2 列表渲染优化
```vue
<!-- ✅ 使用v-memo优化重复渲染 -->
<template>
  <div
    v-for="user in userList"
    :key="user.userId"
    v-memo="[user.userId, user.userName, user.userStatus]"
  >
    <UserCard :user="user" />
  </div>
</template>

<!-- ✅ 虚拟滚动处理大列表 -->
<script setup lang="ts">
import { VirtualScroller } from 'vue-virtual-scroller'

const userList = ref<User[]>([])  // 假设有10000条数据
</script>

<template>
  <VirtualScroller
    :items="userList"
    :item-height="50"
    key-field="userId"
  >
    <template #default="{ item }">
      <UserCard :user="item" />
    </template>
  </VirtualScroller>
</template>
```

#### 8.9.3 计算属性和侦听器优化
```typescript
// ✅ 推荐：使用计算属性缓存结果
const filteredUsers = computed(() => {
  return userList.value.filter(user => user.status === 'active')
})

// ❌ 不推荐：在模板中直接计算
<template>
  <div v-for="user in userList.filter(u => u.status === 'active')">
    <!-- 每次渲染都会重新计算 -->
  </div>
</template>

// ✅ 推荐：使用watchEffect自动追踪依赖
watchEffect(() => {
  console.log('用户信息变化', userStore.userInfo)
})

// ✅ 推荐：watch指定特定依赖
watch(
  () => userStore.userInfo?.userId,
  (newUserId, oldUserId) => {
    if (newUserId !== oldUserId) {
      loadUserData(newUserId)
    }
  }
)
```

### 8.10 前端安全规范

#### 8.10.1 XSS 防护
```typescript
// ✅ 推荐：避免使用v-html
<template>
  <div>{{ userInput }}</div>  <!-- 自动转义 -->
</template>

// ❌ 不推荐：直接使用v-html渲染用户输入
<template>
  <div v-html="userInput"></div>  <!-- 危险！可能导致XSS -->
</template>

// ✅ 如果必须使用v-html，进行sanitize处理
import DOMPurify from 'dompurify'

const sanitizedHtml = computed(() => {
  return DOMPurify.sanitize(userInput.value)
})

<template>
  <div v-html="sanitizedHtml"></div>
</template>
```

#### 8.10.2 敏感信息保护
```typescript
// ✅ 推荐：不在前端存储敏感信息
// 不要在localStorage中存储密码、完整身份证号等敏感信息

// ✅ Token使用建议
// 1. 使用httpOnly cookie存储（后端设置）
// 2. 使用短期token + refresh token机制
// 3. Token过期自动刷新

// src/utils/auth.ts
const TOKEN_KEY = 'access_token'

export const getToken = (): string => {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export const setToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token)
}

export const removeToken = (): void => {
  localStorage.removeItem(TOKEN_KEY)
}

// ❌ 不推荐：在localStorage存储敏感信息
localStorage.setItem('password', '123456')  // 危险！
localStorage.setItem('idCard', '110101199001011234')  // 危险！
```

### 8.11 测试规范

#### 8.11.1 单元测试规范
```typescript
// ✅ src/utils/__tests__/formatDate.spec.ts
import { describe, it, expect } from 'vitest'
import { formatDate } from '../formatDate'

describe('formatDate', () => {
  it('应该正确格式化日期', () => {
    const date = new Date('2026-02-20 15:30:00')
    expect(formatDate(date, 'YYYY-MM-DD')).toBe('2026-02-20')
    expect(formatDate(date, 'YYYY-MM-DD HH:mm:ss')).toBe('2026-02-20 15:30:00')
  })

  it('应该处理无效日期', () => {
    expect(formatDate(null)).toBe('')
    expect(formatDate(undefined)).toBe('')
  })
})

// ✅ src/stores/__tests__/user.spec.ts
import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '../user'

describe('UserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('应该正确设置用户信息', () => {
    const store = useUserStore()
    const userInfo = {
      userId: '123',
      userName: 'Test User'
    }
    
    store.userInfo = userInfo
    expect(store.userName).toBe('Test User')
  })
})
```

### 8.12 项目结构规范

```
complete-frontend/
├── public/                   # 静态资源
│   └── favicon.ico
├── src/
│   ├── api/                  # API接口
│   │   ├── request.ts        # Axios封装
│   │   ├── auth.ts           # 认证API
│   │   ├── user.ts           # 用户API
│   │   └── index.ts          # API导出
│   ├── assets/               # 资源文件
│   │   ├── images/           # 图片
│   │   ├── icons/            # 图标
│   │   └── fonts/            # 字体
│   ├── components/           # 公共组件
│   │   ├── common/           # 通用组件
│   │   │   ├── PageHeader.vue
│   │   │   └── Loading.vue
│   │   └── business/         # 业务组件
│   │       └── UserCard.vue
│   ├── composables/          # 组合式函数
│   │   ├── useTable.ts
│   │   └── useForm.ts
│   ├── layouts/              # 布局组件
│   │   ├── MainLayout.vue
│   │   └── EmptyLayout.vue
│   ├── router/               # 路由配置
│   │   ├── index.ts
│   │   └── guards.ts
│   ├── stores/               # 状态管理
│   │   ├── user.ts
│   │   └── app.ts
│   ├── styles/               # 全局样式
│   │   ├── variables.scss    # 变量
│   │   ├── mixins.scss       # Mixin
│   │   ├── common.scss       # 通用样式
│   │   └── index.scss        # 样式入口
│   ├── types/                # 类型定义
│   │   ├── common.ts
│   │   ├── user.ts
│   │   └── api.ts
│   ├── utils/                # 工具函数
│   │   ├── auth.ts           # 认证工具
│   │   ├── storage.ts        # 存储工具
│   │   ├── format.ts         # 格式化工具
│   │   └── validate.ts       # 验证工具
│   ├── views/                # 页面组件
│   │   ├── Login.vue
│   │   ├── Dashboard.vue
│   │   ├── user/
│   │   │   ├── UserList.vue
│   │   │   └── UserDetail.vue
│   │   └── NotFound.vue
│   ├── App.vue               # 根组件
│   └── main.ts               # 入口文件
├── .env.development          # 开发环境变量
├── .env.production           # 生产环境变量
├── .eslintrc.cjs             # ESLint配置
├── .prettierrc.json          # Prettier配置
├── tsconfig.json             # TypeScript配置
├── vite.config.ts            # Vite配置
└── package.json              # 依赖配置
```

## 9. 个人规则要求
- 每次创建表操作需要在complete-backend\docs\sql文件夹中创建对应的表创建语句
- 创建表时需向我征求意见，询问表设计是否合理，经过我的修改确认后再次执行业务代码
- sql实现方式:单表 CRUD 优先用 QueryWrapper：比如 selectById()、updateById()、简单条件查询（eq/like/orderBy），提升开发效率；
复杂场景手写 SQL：
多表联查、聚合查询、子查询写在 Mapper 注解中；
核心业务的 SQL 手动优化，确保性能；

- 将所有的项目报错误并修改成功的案例，以及优化策略的思路放入一个单独的ErrorLog.md文档中

## 10. 前后端协作规范

### 10.1 接口对接规范
- 前端开发前，必须先与后端确认接口文档（使用Knife4j/Swagger）
- 接口字段命名必须保持一致（后端返回camelCase，前端对应使用camelCase）
- 分页参数统一使用 pageNum（页码）和 pageSize（每页数量）
- 时间格式统一使用 ISO8601 格式或时间戳

### 10.2 错误处理规范
- 后端返回统一的错误码和错误信息
- 前端根据错误码进行统一处理
- 业务异常在拦截器中统一提示
- 网络异常提供友好的错误提示

### 10.3 开发调试规范
- 使用代理解决开发环境跨域问题
- 使用环境变量管理不同环境的API地址
- 保持前后端日志格式一致，便于问题排查
- 使用浏览器开发工具(DevTools)进行前端调试

---
**文档版本**：V2.0  
**制定日期**：2026年2月19日  
**更新日期**：2026年2月20日  
**更新内容**：新增前端开发规范（第8章）、前后端协作规范（第10章）  
**适用范围**：毕业设计全过程管理系统开发团队  
**维护人**：系统架构师