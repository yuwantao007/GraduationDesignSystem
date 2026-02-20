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

## 5. 接口参数封装规范

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

### 5.2 参数封装最佳实践

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

### 5.3 参数校验规范

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

## 6. 性能优化规范

### 6.1 数据库优化
- 合理设计索引
- 避免N+1查询问题
- 使用连接池管理
- 定期分析慢查询

### 6.2 代码优化
- 避免重复计算
- 合理使用缓存
- 减少对象创建
- 优化循环处理

### 6.3 网络优化
- 合理设置超时时间
- 使用连接复用
- 压缩传输数据
- CDN加速静态资源

### 7 个人规则要求
- 每次创建表操作需要在complete-backend\docs\sql文件夹中创建对应的表创建语句
- 创建表时需向我征求意见，询问表设计是否合理，经过我的修改确认后再次执行业务代码
- sql实现方式:单表 CRUD 优先用 QueryWrapper：比如 selectById()、updateById()、简单条件查询（eq/like/orderBy），提升开发效率；
复杂场景手写 SQL：
多表联查、聚合查询、子查询写在 Mapper 注解中；
核心业务的 SQL 手动优化，确保性能；

- 将所有的项目报错误并修改成功的案例，以及优化策略的思路放入一个单独的ErrorLog.md文档中
---
**文档版本**：V1.0  
**制定日期**：2026年2月19日  
**适用范围**：毕业设计全过程管理系统开发团队  
**维护人**：系统架构师