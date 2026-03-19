# ErrorLog — 项目问题记录与解决方案

## 目录
1. [Mapper 与 Service 重构优化策略](#mapper-与-service-重构优化策略)
2. [前端登录后页面空白问题修复 - 第一阶段](#前端登录后页面空白问题修复---第一阶段)
3. [前端登录后页面空白问题修复 - 第二阶段（函数初始化顺序错误）](#前端登录后页面空白问题修复---第二阶段函数初始化顺序错误)
4. [登录注册逻辑重构 — 学号/工号替代用户名](#登录注册逻辑重构--学号工号替代用户名)
5. [管理员登录后跳转登录页问题 — JWT认证失败](#管理员登录后跳转登录页问题--jwt认证失败)
6. [角色权限管理页面加载转圈问题 — 前后端数据格式不匹配](#角色权限管理页面加载转圈问题--前后端数据格式不匹配)
7. [用户管理：编辑用户学号/工号未保存问题](#用户管理编辑用户学号工号未保存问题)
8. [角色权限配置缺失问题](#角色权限配置缺失问题--企业教师等角色无法访问课题管理)
9. [企业管理功能404错误 — Controller路径重复/api前缀](#企业管理功能404错误--controller路径重复api前缀)
10. [角色权限配置API缺失问题 — Permission树形结构查询](#角色权限配置api缺失问题--permission树形结构查询)
11. [角色CRUD API缺失问题 — 编辑角色功能无法使用](#角色crud-api缺失问题--编辑角色功能无法使用)
12. [Maven JDK版本配置问题 — 编译失败无效标记](#maven-jdk版本配置问题--编译失败无效标记)
13. [课题审查API参数必传问题 — 我的审批统计接口](#课题审查api参数必传问题--我的审批统计接口)
14. [课题审查API参数必传问题 — 综合意见列表接口](#课题审查api参数必传问题--综合意见列表接口)
15. [系统管理员创建专业方向报错问题](#系统管理员创建专业方向报错问题)
16. [专业代码保存为空问题](#专业代码保存为空问题)
17. [覆盖检查误报未覆盖 — MyBatis-Plus eq(null)生成IS NULL条件](#覆盖检查误报未覆盖--mybatis-plus-eqnull生成is-null条件)
18. [专业管理编辑弹窗企业老师下拉框显示跨企业教师问题](#专业管理编辑弹窗企业老师下拉框显示跨企业教师问题)
19. [企业老师过滤错误导致下拉框为空 — department 字段过滤错误](#企业老师过滤错误导致下拉框为空--department 字段过滤错误)
20. [企业负责人双选审核 TooManyResultsException 异常](#企业负责人双选审核-too-many-results-exception-异常)
21. [学生选报课题无法查看任务书详情](#学生选报课题无法查看任务书详情)
22. [答辩安排创建后报系统错误 — MySQL 排序规则冲突](#答辩安排创建后报系统错误--mysql-排序规则冲突)

---

# Mapper 与 Service 重构优化策略

- 日期：2026-02-20
- 作者：重构执行者

## 一、概要

将单表 CRUD 优先使用 MyBatis-Plus 的 `BaseMapper` + `QueryWrapper` / `LambdaQueryWrapper`，
仅对确实需要数据库函数或多表 JOIN 的场景保留手写 SQL。目标为统一代码风格、降低重复 SQL、便于维护与自动化填充。

## 二、变更要点

- **UserMapper**
  - 移除：`selectByUsername()`、`updateUserStatus()`（这类单表查询/更新使用 QueryWrapper 或 `updateById` 替代）
  - 保留：`updateLoginInfo()`（使用数据库 `NOW()` 函数，保证原子性与时间一致性）

- **RoleMapper**
  - 移除：`selectByRoleCode()`（单表查询改用 `LambdaQueryWrapper`）
  - 保留：`selectRolesByUserId()`（多表 JOIN，保留手写 SQL）

- **UserRoleMapper**
  - 移除：`deleteByUserId()`（改为 `userRoleMapper.delete(new LambdaQueryWrapper<>() .eq(...))`）

- **RolePermissionMapper**
  - 移除：`deleteByRoleId()`（同上，使用 QueryWrapper.delete）

- **PermissionMapper**
  - 保留所有现有手写 SQL（均为多表 JOIN 查询，属于复杂查询场景）

## 三、Service 层调整（摘要）

- `AuthServiceImpl`：将 `selectByUsername` / `selectByRoleCode` 改为使用 `LambdaQueryWrapper` 的 `selectOne`。
- `UserServiceImpl`：将 `selectByUsername` 改为 `selectOne(LambdaQueryWrapper)`；对状态更新改为 `selectById` + `updateById`（以触发自动填充）；删除用户角色关系改为 `delete(new LambdaQueryWrapper<>()...)`。
- `SecurityUserDetailsService`：将 `selectByUsername` 改为 `LambdaQueryWrapper` 查询。

## 四、变更理由

- 统一单表操作到 MyBatis-Plus 能减少手写 SQL、利用框架自动填充、提高可读性与一致性。
- 仅在多表 JOIN 或需数据库函数（如 `NOW()`）时保留手写 SQL，保证性能与原子性需求。

## 五、风险与回退策略

- 若重构导致编译、集成或运行失败，可通过版本控制回退对应 Mapper 文件到手写 SQL 实现，或临时在 Service 中恢复原有调用以保证线上稳定。

## 六、后续建议

- 执行 `mvn compile` 和现有测试套件，确认无回归。若有 CI/CD，请在 PR 中加入变更说明与影响范围。
- 若团队同意此规范，可将其写入项目贡献指南或 Coding Style 文档，作为日后统一标准。

---

# ErrorLog — 前端登录后页面空白问题修复

- 日期：2026-02-21
- 作者：系统维护者
- 严重程度：高（阻塞用户登录后使用系统）

## 一、问题描述

### 问题表现
用户登录成功后，页面跳转但显示完全空白，无法看到仪表盘或任何内容。

### 影响范围
- 所有登录用户
- 完全阻断了系统的正常使用
- 前端路由导航失败

## 二、问题根源分析

通过系统排查，发现了以下三个关键问题：

### 2.1 前后端数据结构不匹配（核心问题）

**问题详情：**
- 后端 `LoginVO.userInfo.roles` 返回的是 `RoleVO` 对象数组，包含 `roleId`、`roleName`、`roleCode` 等完整字段
- 前端期望的是简单的字符串数组 `string[]`
- 这导致前端在处理用户角色时出现类型错误，进而影响路由守卫和权限判断

**后端数据结构 (LoginVO)：**
```java
public class LoginVO {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserVO userInfo;  // userInfo.roles 是 List<RoleVO>
}

public class RoleVO {
    private String roleId;
    private String roleName;
    private String roleCode;
    private String roleDesc;
    // ...
}
```

**前端原错误定义：**
```typescript
interface LoginVO {
    userInfo: {
        roles: string[]  // ❌ 类型不匹配
    }
}
```

### 2.2 API 路径不一致

**问题详情：**
- 前端调用：`/auth/current-user` 获取当前用户信息
- 后端实际接口：`/user/current`
- 导致获取用户信息失败，路由守卫拦截跳转回登录页

**后端 Controller 定义：**
```java
@RestController
@RequestMapping("/user")
public class UserController {
    @GetMapping("/current")
    public Result<UserVO> getCurrentUserInfo() {
        // ...
    }
}
```

### 2.3 缺少开发环境代理配置

**问题详情：**
- Vite 配置中缺少 API 代理设置
- 跨域请求可能导致某些场景下的请求失败

## 三、修复方案

### 3.1 修复前后端数据结构不匹配

#### 1) 更新前端类型定义 (`src/api/auth.ts`)

```typescript
// 新增角色信息接口
export interface RoleVO {
  roleId: string
  roleName: string
  roleCode: string
  roleDesc?: string
  sortOrder?: number
  roleStatus?: number
}

// 新增用户信息接口
export interface UserInfo {
  userId: string
  username: string
  realName: string
  userEmail: string
  userPhone?: string
  avatar?: string
  // ...
  roles: RoleVO[]        // ✅ 正确的类型
  permissions: string[]
}

// 更新登录响应接口
export interface LoginVO {
  accessToken: string
  refreshToken: string
  tokenType?: string
  expiresIn: number
  userInfo: UserInfo     // ✅ 使用完整的 UserInfo
}
```

#### 2) 更新 Store 数据处理逻辑 (`src/stores/user.ts`)

```typescript
import type { LoginDTO, RoleVO, UserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(getUserInfo())
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])

  // 登录处理
  const login = async (loginData: LoginDTO) => {
    const response = await authApi.login(loginData)
    const data = response.data
    
    userInfo.value = data.userInfo
    // ✅ 将 RoleVO 数组转换为 roleCode 字符串数组
    roles.value = data.userInfo.roles?.map((role: RoleVO) => role.roleCode) || []
    permissions.value = data.userInfo.permissions || []
    // ...
  }

  // 获取用户信息处理
  const getUserInfoData = async () => {
    const response = await authApi.getCurrentUser()
    userInfo.value = response.data
    // ✅ 同样进行转换处理
    roles.value = response.data.roles?.map((role: RoleVO) => role.roleCode) || []
    permissions.value = response.data.permissions || []
    // ...
  }
})
```

**设计说明：**
- 前端 Store 层的 `roles` 保持为 `string[]`（角色编码数组），用于权限判断
- 完整的角色信息保存在 `userInfo.roles` 中，供需要显示角色详情的场景使用
- 通过 `map` 方法将 `RoleVO` 数组转换为 `roleCode` 字符串数组

### 3.2 修复 API 路径不匹配

**修改文件：** `src/api/auth.ts`

```typescript
export const authApi = {
  // 获取当前用户信息
  getCurrentUser() {
    return request.get('/user/current')  // ✅ 修正为正确路径
  }
}
```

### 3.3 添加 Vite 代理配置

**修改文件：** `vite.config.ts`

```typescript
export default defineConfig({
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  // ✅ 新增开发服务器配置
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
```

**配置说明：**
- 将前端 `/api/*` 请求代理到后端 `http://localhost:8080`
- `changeOrigin: true` 修改请求头中的 origin
- `rewrite` 去除 `/api` 前缀，因为后端不需要这个前缀

## 四、修改的文件清单

1. **complete-frontend/src/api/auth.ts**
   - 新增 `RoleVO` 和 `UserInfo` 接口定义
   - 更新 `LoginVO` 接口结构
   - 修正 `getCurrentUser()` API 路径

2. **complete-frontend/src/stores/user.ts**
   - 更新类型导入
   - 修改 `userInfo` 类型为 `UserInfo | null`
   - 在 `login()` 和 `getUserInfoData()` 中添加 RoleVO 到 roleCode 的转换逻辑

3. **complete-frontend/vite.config.ts**
   - 添加开发服务器代理配置

## 五、验证步骤

1. **停止现有前端开发服务**
   ```bash
   # 按 Ctrl+C 停止正在运行的前端服务
   ```

2. **重新启动前端服务**
   ```bash
   cd complete-frontend
   npm run dev
   ```

3. **测试登录流程**
   - 访问 http://localhost:5173
   - 输入正确的用户名和密码
   - 验证登录成功后能否正常显示 Dashboard 页面
   - 检查浏览器控制台是否有错误信息

4. **验证用户信息获取**
   - 检查用户头像和昵称是否正常显示
   - 验证菜单权限是否正确加载
   - 测试路由跳转功能

## 六、技术要点总结

### 类型安全的重要性
- 前后端接口定义必须严格对齐
- TypeScript 类型定义能帮助提前发现数据结构不匹配问题
- 使用明确的类型而非 `any`，便于编译时检查

### 数据转换层的设计
- Store 层是数据转换的最佳位置
- 保持 API 层接口定义与后端一致
- 在 Store 中转换为前端业务需要的格式

### API 路径规范
- 需要前后端团队统一 API 路径规范
- 建议使用 OpenAPI/Swagger 生成前端 API 代码，减少路径不一致问题

### 开发环境配置
- 代理配置对于前后端分离开发至关重要
- 注意代理规则的 rewrite 处理

## 七、预防措施建议

1. **接口文档同步**
   - 使用 Swagger/OpenAPI 作为前后端接口契约
   - 考虑使用 OpenAPI Generator 自动生成前端 TypeScript 类型

2. **集成测试**
   - 添加登录流程的端到端测试
   - 测试覆盖关键用户路径

3. **类型检查**
   - 启用 TypeScript 严格模式
   - 定期运行 `tsc --noEmit` 进行类型检查

4. **代码审查**
   - API 接口变更必须通知前端团队
   - 前端类型定义变更需要后端确认

## 八、相关参考

- MyBatis-Plus 官方文档：https://baomidou.com
- Vite 代理配置：https://vitejs.dev/config/server-options.html#server-proxy
- TypeScript 类型系统：https://www.typescriptlang.org/docs/handbook/2/everyday-types.html

---

# 前端登录后页面空白问题修复 - 第二阶段（函数初始化顺序错误）

- 日期：2026-02-21
- 作者：系统维护者
- 严重程度：高（前端页面完全无法显示）
- 状态：已解决

## 一、问题背景

在解决了第一阶段的前后端数据结构不匹配问题后，用户登录成功后页面仍然显示空白。通过添加调试日志进行排查，发现了新的问题。

## 二、问题表现

### 控制台错误信息
```
Uncaught (in promise) ReferenceError: Cannot access 'updateBreadcrumbs' before initialization
    at watch.immediate (MainLayout.vue:179:5)
    at setup (MainLayout.vue:173:3)
```

### 现象描述
- 路由守卫正常执行，用户信息获取成功
- Token 和 UserInfo 状态正常
- MainLayout 组件开始加载时抛出 `ReferenceError` 错误
- 导致整个应用崩溃，页面完全空白

## 三、问题根源分析

### 3.1 代码结构问题

在 `MainLayout.vue` 的 setup 函数中，代码结构如下：

```typescript
// 第165行：定义 getOpenKeys 函数
const getOpenKeys = (path: string): string[] => {
  if (path.startsWith('/user')) return ['user-mgmt']
  // ...
}

// 第173行：定义 watch，设置 immediate: true
watch(
  () => route.path,
  (newPath) => {
    selectedKeys.value = [newPath]
    openKeys.value = getOpenKeys(newPath)
    updateBreadcrumbs(newPath)  // ❌ 第179行：调用还未定义的函数
  },
  { immediate: true }  // ❌ 关键：立即执行
)

// 第184行：updateBreadcrumbs 函数定义
const updateBreadcrumbs = (path: string) => {
  // 函数实现
}
```

### 3.2 问题分析

**JavaScript/TypeScript 执行机制：**
1. 在 Vue 3 的 Composition API 中，`setup` 函数中的代码按**顺序同步执行**
2. `const` 声明的函数在声明之前不能被访问（暂时性死区，Temporal Dead Zone）
3. `watch` 函数的 `immediate: true` 选项会导致 watch 在定义时**立即执行回调函数**

**错误发生流程：**
1. 执行到第173行，创建 `watch`
2. 由于 `immediate: true`，watch 的回调函数立即执行
3. 回调函数第179行调用 `updateBreadcrumbs(newPath)`
4. 此时代码执行还未到达第184行，`updateBreadcrumbs` 尚未定义
5. 抛出 `ReferenceError: Cannot access 'updateBreadcrumbs' before initialization`

### 3.3 为什么第一阶段没有发现此问题

在第一阶段修复中：
- 修复了 Store 的初始化问题
- 修复了 API 路径和数据结构
- 添加了调试日志

但当时没有真正触发到 MainLayout 组件的完整渲染流程，所以没有暴露这个函数定义顺序的问题。

## 四、解决方案

### 修复方法

将 `updateBreadcrumbs` 函数定义移到 `watch` 之前，确保在 `watch` 的 `immediate` 回调执行时函数已经定义。

**修改文件：** `complete-frontend/src/layouts/MainLayout.vue`

**修改前：**
```typescript
const getOpenKeys = (path: string): string[] => {
  // ...
}

watch(
  () => route.path,
  (newPath) => {
    selectedKeys.value = [newPath]
    openKeys.value = getOpenKeys(newPath)
    updateBreadcrumbs(newPath)  // ❌ 函数还未定义
  },
  { immediate: true }
)

const updateBreadcrumbs = (path: string) => {
  // ...
}
```

**修改后：**
```typescript
const getOpenKeys = (path: string): string[] => {
  // ...
}

// ✅ 先定义 updateBreadcrumbs 函数
const updateBreadcrumbs = (path: string) => {
  const breadcrumbMap: Record<string, Array<{ title: string; path?: string }>> = {
    '/dashboard': [{ title: '首页' }, { title: '仪表盘' }],
    '/user': [{ title: '首页', path: '/' }, { title: '用户管理' }, { title: '用户列表' }],
    // ... 其他路由
  }
  appStore.setBreadcrumbs(breadcrumbMap[path] || [{ title: '首页' }])
}

// ✅ 然后定义 watch
watch(
  () => route.path,
  (newPath) => {
    selectedKeys.value = [newPath]
    openKeys.value = getOpenKeys(newPath)
    updateBreadcrumbs(newPath)  // ✅ 现在可以正常调用
  },
  { immediate: true }
)
```

## 五、技术要点总结

### 5.1 Vue 3 Composition API 函数定义顺序

在 Vue 3 的 `setup` 函数中使用 Composition API 时：

1. **函数提升（Hoisting）不适用于 `const` 声明**:
   ```typescript
   // ❌ 错误：不能在声明前调用
   myFunc()
   const myFunc = () => { console.log('hello') }
   
   // ✅ 正确：使用 function 声明（会提升）
   myFunc()
   function myFunc() { console.log('hello') }
   
   // ✅ 正确：先声明再调用
   const myFunc = () => { console.log('hello') }
   myFunc()
   ```

2. **`immediate: true` 的 watch 会立即执行**:
   ```typescript
   // watch 回调中引用的所有函数必须在 watch 之前定义
   watch(source, callback, { immediate: true })
   ```

### 5.2 最佳实践建议

1. **函数定义顺序规范**:
   - 工具函数 / 辅助函数优先定义
   - 副作用函数（watch、watchEffect）后定义
   - 确保函数调用链的依赖顺序正确

2. **使用 `function` 关键字（可选方案）**:
   ```typescript
   // 使用 function 声明可以提升，避免顺序问题
   function updateBreadcrumbs(path: string) {
     // ...
   }
   ```

3. **代码组织建议**:
   ```typescript
   // 1. 引入依赖
   import { ref, watch } from 'vue'
   
   // 2. Props / Emits 定义
   const props = defineProps<{ ... }>()
   
   // 3. 响应式状态
   const state = ref(...)
   
   // 4. 计算属性
   const computed = computed(() => ...)
   
   // 5. 辅助函数（被 watch 依赖的要先定义）
   const helperFunc = () => { ... }
   
   // 6. Watch / WatchEffect
   watch(source, callback, { immediate: true })
   
   // 7. 生命周期钩子
   onMounted(() => { ... })
   ```

## 六、调试技巧

### 6.1 添加调试日志
在解决此问题时，添加了关键位置的日志输出：
- 路由守卫执行流程
- Store 数据处理
- 组件生命周期

这些日志帮助快速定位到问题组件和错误位置。

### 6.2 浏览器开发者工具使用
- **Console 标签**：查看错误堆栈和日志
- **Network 标签**：检查 API 请求状态
- **Vue DevTools**：查看组件状态和 Pinia Store

## 七、教训与启示

1. **前端构建时的错误处理**:
   - Vue 3 在开发模式下会捕获并显示详细错误信息
   - 生产模式下可能只显示空白页，难以调试
   - 建议开发时启用 sourceMap 和详细错误日志

2. **代码审查重点**:
   - 检查 `immediate: true` 的 watch 依赖
   - 确认函数定义和调用顺序
   - 注意 JavaScript 的暂时性死区（TDZ）

3. **测试覆盖**:
   - 组件渲染测试应该包含初始化阶段
   - 测试 `immediate` watch 的执行

## 八、验证步骤

修复后的验证流程：

1. **清除浏览器缓存和 localStorage**
2. **重新登录系统**
3. **检查控制台无错误**
4. **验证页面正常显示**:
   - ✅ Dashboard 页面显示统计卡片
   - ✅ 侧边栏菜单正常显示
   - ✅ 面包屑导航正确显示
   - ✅ 用户信息在头部正确显示

## 九、相关问题排查清单

如果遇到类似的"函数未定义"错误：

- [ ] 检查函数是否在调用前定义
- [ ] 检查是否有 `immediate: true` 的 watch
- [ ] 检查是否使用 `const` 声明函数（考虑改用 `function`）
- [ ] 检查函数调用链的依赖关系
- [ ] 使用 console.log 确认执行顺序
- [ ] 检查是否有异步执行导致的时序问题

## 十、相关参考

- Vue 3 Composition API 文档：https://vuejs.org/guide/extras/composition-api-faq.html
- JavaScript 变量提升与 TDZ：https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Reference/Statements/let#temporal_dead_zone_tdz
- Vue 3 Watch API：https://vuejs.org/api/reactivity-core.html#watch

---

# 登录注册逻辑重构 — 学号/工号替代用户名

> **变更类型：** 功能重构  
> **变更日期：** 今日  
> **影响范围：** 前后端登录 / 注册全链路

---

## 一、变更背景

毕业设计全过程管理系统的用户群体为学生和教师，使用自定义用户名登录存在以下问题：
- 用户名容易遗忘，不如学号/工号直观
- 学号/工号在校园系统中是唯一标识，天然适合作为登录凭据
- 避免用户名重复冲突

## 二、变更内容

### 2.1 后端变更

| 文件 | 变更 |
|------|------|
| `LoginDTO.java` | `username` 字段改为 `identifier`（学号/工号） |
| `RegisterDTO.java` | 移除 `username`，新增 `studentNo` 和 `employeeNo` 字段 |
| `SecurityUserDetailsService.java` | `loadUserByUsername()` 按 `studentNo` OR `employeeNo` 查询 |
| `AuthServiceImpl.login()` | 使用 `identifier` 认证，按学号/工号查询用户 |
| `AuthServiceImpl.register()` | 根据角色校验学号/工号，检查唯一性，`username` 自动设为学号/工号 |
| `AuthController.java` | 接口描述更新为"通过学号/工号和密码登录" |

**关键逻辑 — 注册时的角色判断：**
```java
if ("STUDENT".equals(roleCode)) {
    // 要求填写 studentNo，检查唯一性
    user.setUsername(studentNo);  // 内部 username 设为学号
    user.setStudentNo(studentNo);
} else {
    // UNIVERSITY_TEACHER / ENTERPRISE_TEACHER 要求填写 employeeNo
    user.setUsername(employeeNo);  // 内部 username 设为工号
    user.setEmployeeNo(employeeNo);
}
```

### 2.2 前端变更

| 文件 | 变更 |
|------|------|
| `api/auth.ts` | `LoginDTO.username` → `identifier`；`RegisterDTO` 移除 `username`，加 `studentNo?` / `employeeNo?` |
| `views/Login.vue` | 表单字段改为 `identifier`，标签改为"学号/工号"，提示文字更新 |
| `views/Register.vue` | 移除用户名输入框，角色选择后动态显示学号或工号输入框 |

**注册表单改动要点：**
- 角色选择移至第一行，选择后才显示学号/工号输入框
- 选择「学生」→ 显示学号输入框
- 选择「高校指导教师」或「企业指导教师」→ 显示工号输入框
- 切换角色时自动清空学号/工号

## 三、认证链路说明

```
前端 Login.vue
  ↓ { identifier, password }
stores/user.ts → authApi.login()
  ↓ POST /auth/login
AuthController → AuthServiceImpl.login()
  ↓ new UsernamePasswordAuthenticationToken(identifier, password)
AuthenticationManager → SecurityUserDetailsService.loadUserByUsername(identifier)
  ↓ SELECT * FROM user_info WHERE student_no = ? OR employee_no = ?
SecurityUserDetails → 密码校验 → JWT 生成 → Redis 缓存
  ↓ { accessToken, refreshToken, userInfo }
前端接收并存储
```

## 四、数据库说明

无需修改数据库结构。`user_info` 表已有 `student_no` 和 `employee_no` 字段。`username` 字段在注册时自动设置为学号或工号值，保证 JWT 令牌生成和刷新等内部逻辑正常运行。

---

# 管理员登录后跳转登录页问题 — JWT认证失败

> **问题类型：** BUG修复  
> **问题日期：** 2026-02-21  
> **影响范围：** 管理员账号登录后访问任何受保护页面

---

## 一、问题现象

管理员账号登录成功后，点击"用户列表"或"角色权限"菜单，页面闪现后**直接跳转回登录页**。前端控制台可能出现错误信息但被刷新了看不清楚。

---

## 二、问题根因

**核心问题：`SecurityUserDetailsService.loadUserByUsername()` 无法查询到 admin 用户**

### 问题链路分析

```
用户登录成功
  ↓ JWT token 存储: username='admin'
点击受保护页面（如 /user）
  ↓ 前端发送请求: GET /api/user/current, Header: Authorization: Bearer {token}
后端 JwtAuthenticationFilter 拦截
  ↓ 从 token 解析 username='admin'
  ↓ 调用 SecurityUserDetailsService.loadUserByUsername('admin')
  ↓ 执行查询:
     SELECT * FROM user_info 
     WHERE student_no = 'admin'  -- NULL ≠ 'admin'
     OR employee_no = 'admin'    -- NULL ≠ 'admin'
  ↓ 返回 NULL
抛出 UsernameNotFoundException
  ↓ Spring Security 标记认证失败
  ↓ SecurityContextHolder.getContext().getAuthentication() = null
UserController.getCurrentUserInfo()
  ↓ SecurityUtil.getCurrentUserId() 返回 null
  ↓ userService.getCurrentUserInfo(null)
  ↓ userMapper.selectById(null) 返回 null
抛出 BusinessException("用户不存在")
  ↓ 响应 HTTP 401 Unauthorized
前端 request.ts 响应拦截器
  ↓ case 401: window.location.href = '/login'
跳转到登录页
```

### 问题原因详解

1. **数据库数据不完整**  
   初始化的 admin 账号只有 `username='admin'`，但 `student_no` 和 `employee_no` 字段都是 **NULL**

2. **查询逻辑不兼容**  
   [SecurityUserDetailsService.java](complete-backend/src/main/java/com/yuwan/completebackend/security/SecurityUserDetailsService.java) 的 `loadUserByUsername()` 方法只按 `studentNo` 或 `employeeNo` 查询：
   ```java
   // 原代码 - 无法查询到 username='admin', studentNo=NULL, employeeNo=NULL 的用户
   User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
           .eq(User::getStudentNo, identifier)
           .or()
           .eq(User::getEmployeeNo, identifier));
   ```
   当查询条件是 `identifier='admin'` 时，两个字段都是 NULL，查询结果为空。

3. **认证失败导致级联错误**  
   - JWT filter 无法设置用户上下文
   - `SecurityUtil.getCurrentUser()` 返回 null
   - 后续所有依赖当前用户的接口都会失败
   - 前端收到 401，跳转登录页

---

## 三、修复方案

### 3.1 修改 SecurityUserDetailsService（后端兼容查询）

**文件：** [SecurityUserDetailsService.java](complete-backend/src/main/java/com/yuwan/completebackend/security/SecurityUserDetailsService.java)

```java
@Override
public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
    // 按学号、工号或用户名查询用户信息（兼容管理员等特殊账号）
    User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getStudentNo, identifier)
            .or()
            .eq(User::getEmployeeNo, identifier)
            .or()
            .eq(User::getUsername, identifier));  // ← 新增：兼容 username 查询
    if (user == null) {
        throw new UsernameNotFoundException("用户不存在: " + identifier);
    }
    // ... 后续逻辑不变
}
```

**说明：** 增加 `.or().eq(User::getUsername, identifier)` 作为兜底查询，确保 admin 等特殊账号（没有学号/工号）也能正常认证。

---

### 3.2 更新数据库初始化脚本（SQL完善数据）

**文件：** [user_module.sql](complete-backend/docs/sql/user_module.sql)

```sql
-- 修改前
INSERT INTO user_info (user_id, username, password, real_name, user_email, user_status) VALUES
('1', 'admin', '$2a$10$...', '系统管理员', 'admin@example.com', 1);

-- 修改后
INSERT INTO user_info (user_id, username, password, real_name, user_email, employee_no, user_status) VALUES
('1', 'admin', '$2a$10$...', '系统管理员', 'admin@example.com', 'admin', 1);
       -- ↑ 新增 employee_no='admin' 字段
```

**说明：** 为 admin 账号设置 `employee_no='admin'`，这样即使查询逻辑未修改，也能通过 `employeeNo` 查询到管理员。

---

### 3.3 修复已有数据库（SQL修复脚本）

**文件：** [fix_admin_account.sql](complete-backend/docs/sql/fix_admin_account.sql)

```sql
-- 为已存在的 admin 账号设置 employee_no
UPDATE user_info 
SET employee_no = 'admin' 
WHERE username = 'admin' AND user_id = '1';
```

**执行方式：**
```bash
mysql -u root -p complete_system < complete-backend/docs/sql/fix_admin_account.sql
```

---

### 3.4 清理调试日志（前端代码整理）

同时移除了前端路由守卫和 user store 中的临时调试日志（console.log），保持代码整洁。

---

## 四、验证步骤

1. **更新数据库**  
   执行 `fix_admin_account.sql` 或重新初始化 `user_module.sql`

2. **重启后端服务**  
   确保 `SecurityUserDetailsService` 的修改生效

3. **清除前端缓存**  
   浏览器控制台执行：
   ```javascript
   localStorage.clear()
   sessionStorage.clear()
   ```

4. **重新登录测试**  
   - 使用 `admin` / `123456` 登录
   - 点击"用户列表" — 应正常显示页面
   - 点击"角色权限" — 应正常显示页面

5. **验证查询**  
   数据库执行：
   ```sql
   SELECT user_id, username, employee_no, student_no 
   FROM user_info 
   WHERE username = 'admin';
   ```
   应看到 `employee_no` 为 `'admin'`

---

## 五、问题总结

| 问题层面 | 根本原因 | 修复措施 |
|---------|---------|---------|
| **数据层** | admin 账号缺少 `employee_no` 字段 | SQL 初始化和修复脚本补充数据 |
| **逻辑层** | 查询逻辑不兼容 username 查询 | `SecurityUserDetailsService` 增加 username 查询兜底 |
| **架构层** | Spring Security 认证失败级联错误 | 修复后认证成功，级联问题自动解决 |

**经验教训：**
- 修改登录逻辑时，需考虑历史数据的兼容性
- 特殊账号（如 admin）应有明确的身份标识字段
- 数据库初始化脚本应包含完整的测试账号数据

---

# 角色权限管理页面加载转圈问题 — 前后端数据格式不匹配

> **问题类型：** BUG修复  
> **问题日期：** 2026-02-21  
> **影响范围：** 角色权限管理页面无法正常显示数据

---

## 一、问题现象

点击"角色权限"菜单后，页面一直显示加载转圈（loading），无法显示角色列表。浏览器控制台无明显错误，后端日志显示查询有数据返回。

---

## 二、问题根因

**核心问题：前端期望的数据格式与后端实际返回的格式不匹配**

### 错误链路分析

```
用户点击"角色权限"
  ↓ 前端调用: roleApi.getRoleList()
  ↓ API定义: return request.get<PageResult<RoleInfo>>('/role/list')
前端期望响应格式:
  {
    code: 200,
    data: {
      records: [...],  // ← 期望分页对象
      total: 10,
      current: 1,
      size: 10
    }
  }

后端实际响应:
  {
    code: 200,
    data: [...]       // ← 实际返回数组
  }

前端处理逻辑（loadRoleList）:
  roleList.value = response.data.records  // ← records 为 undefined
  pagination.total = response.data.total  // ← total 为 undefined
  ↓
页面一直处于 loading 状态（因为 roleList 是 undefined）
```

### 问题原因详解

1. **后端接口设计**  
   [RoleController.java](complete-backend/src/main/java/com/yuwan/completebackend/controller/RoleController.java) 的 `/role/list` 接口返回的是 `List<RoleVO>`：
   ```java
   @GetMapping("/list")
   public Result<List<RoleVO>> getAllRoles() {
       List<RoleVO> roles = roleService.getAllRoles();
       return Result.success(roles);  // 返回数组，不是分页对象
   }
   ```

2. **前端API类型定义**  
   [role.ts](complete-frontend/src/api/role.ts) 错误地定义了返回类型为 `PageResult<RoleInfo>`：
   ```typescript
   getRoleList(params: RoleQueryVO) {
     return request.get<PageResult<RoleInfo>>('/role/list', { params })
   }
   ```

3. **前端数据处理逻辑**  
   [RoleList.vue](complete-frontend/src/views/user/RoleList.vue) 试图访问不存在的 `records` 属性：
   ```typescript
   roleList.value = response.data.records  // undefined
   pagination.total = response.data.total  // undefined
   ```

4. **类型不匹配导致的问题**  
   - `response.data` 是数组 `[...]`，不是对象 `{ records: [...] }`
   - 访问 `.records` 和 `.total` 都返回 `undefined`
   - Vue 的响应式系统无法渲染 `undefined` 数据
   - 页面保持 `loading.value = true` 状态不变

---

## 三、修复方案

### 3.1 修改前端 API 类型定义

**文件：** [role.ts](complete-frontend/src/api/role.ts)

```typescript
/**
 * 查询角色列表（分页）
 * @param params - 查询参数
 * @description 注意：后端实际返回的是全部角色列表（数组），前端需要自行处理分页
 */
getRoleList(params: RoleQueryVO) {
  return request.get<RoleInfo[]>('/role/list', { params })  // ← 改为数组类型
},
```

**说明：** 将返回类型从 `PageResult<RoleInfo>` 改为 `RoleInfo[]`，与后端实际返回的格式一致。

---

### 3.2 修改前端数据加载逻辑（前端分页）

**文件：** [RoleList.vue](complete-frontend/src/views/user/RoleList.vue)

```typescript
const loadRoleList = async () => {
  loading.value = true
  try {
    const response = await roleApi.getRoleList({
      ...searchForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    })
    // 后端返回的是数组，需要适配为前端分页格式
    let allRoles = response.data || []
    
    // 前端实现搜索过滤（因为后端不支持搜索参数）
    if (searchForm.roleName) {
      allRoles = allRoles.filter(role => 
        role.roleName?.includes(searchForm.roleName)
      )
    }
    if (searchForm.roleCode) {
      allRoles = allRoles.filter(role => 
        role.roleCode?.includes(searchForm.roleCode)
      )
    }
    if (searchForm.status !== undefined && searchForm.status !== null) {
      allRoles = allRoles.filter(role => 
        role.status === searchForm.status
      )
    }
    
    // 前端实现分页逻辑
    const startIndex = (pagination.current - 1) * pagination.pageSize
    const endIndex = startIndex + pagination.pageSize
    roleList.value = allRoles.slice(startIndex, endIndex)
    pagination.total = allRoles.length
  } catch (error) {
    message.error('加载角色列表失败')
    console.error('加载角色列表失败:', error)
  } finally {
    loading.value = false
  }
}
```

**说明：** 
1. 后端返回全量角色数据（数组）
2. 前端实现搜索过滤逻辑
3. 前端实现分页逻辑（slice）
4. 计算总数并更新分页组件

---

### 3.3 修正前端类型定义

**文件：** [types/user.ts](complete-frontend/src/types/user.ts)

```typescript
export interface RoleInfo {
  /** 角色ID */
  roleId: string
  /** 角色名称 */
  roleName: string
  /** 角色代码 */
  roleCode: string
  /** 角色描述 */
  roleDesc?: string          // ← 改为 roleDesc（匹配后端）
  /** 排序号 */
  sortOrder?: number         // ← 新增字段
  /** 角色状态（0-禁用 1-启用） */
  status?: number            // ← 保持 status（Vue 模板中使用）
  /** 创建时间 */
  createTime?: string
}
```

**说明：** 将 `description` 改为 `roleDesc`，增加 `sortOrder` 字段，与后端 `RoleVO` 字段对齐。

---

## 四、问题总结

| 层面 | 问题 | 后果 |
|------|------|------|
| **接口设计** | 后端返回数组而非分页对象 | 前端无法解析 |
| **类型定义** | 前端 API 类型与实际响应不符 | TypeScript 无法提前发现问题 |
| **数据处理** | 前端尝试访问不存在的属性 | `undefined` 赋值导致渲染失败 |
| **UI 表现** | 数据加载失败但 loading 未关闭 | 页面一直转圈 |

### 根本原因

前后端开发时缺少接口契约约定，导致：
- 后端实际返回的是简单数组
- 前端假设返回的是分页对象
- 运行时类型检查失败但无明显错误提示

### 修复策略

采用**前端适配后端**的方式：
1. 修改前端类型定义匹配后端返回格式
2. 前端实现分页和搜索过滤逻辑
3. 对于角色这种小数据量的场景，前端分页是可接受的方案

### 经验教训

1. **接口契约必须明确**  
   - 使用 OpenAPI/Swagger 规范约定接口格式
   - 前后端开发前先确认数据结构

2. **类型定义要与实际一致**  
   - TypeScript 类型定义应基于后端实际返回值定义
   - 定期同步前后端类型定义

3. **错误处理要完善**  
   - 数据解析失败应有明确的错误提示
   - 避免 `undefined` 静默失败

4. **分页策略要提前约定**  
   - 后端分页：适合大数据量，性能好
   - 前端分页：适合小数据量，开发快

---

## 五、后续优化建议

如果角色数据量增长到一定规模（如 >1000条），建议：

1. **后端增加分页查询接口**
   ```java
   @GetMapping("/list")
   public Result<PageResult<RoleVO>> getRoleList(RoleQueryVO queryVO) {
       PageResult<RoleVO> result = roleService.getRoleList(queryVO);
       return Result.success(result);
   }
   ```

2. **后端增加搜索过滤支持**
   ```java
   public PageResult<RoleVO> getRoleList(RoleQueryVO queryVO) {
       LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
       if (StringUtils.hasText(queryVO.getRoleName())) {
           wrapper.like(Role::getRoleName, queryVO.getRoleName());
       }
       // 分页查询...
   }
   ```

3. **前端切换为后端分页**
   - API 类型改回 `PageResult<RoleInfo>`
   - 移除前端的 filter 和 slice 逻辑
   - 直接使用后端返回的分页数据
- 数据库初始化脚本应包含完整的测试账号数据

---

# 用户管理：编辑用户学号/工号未保存问题

- 日期：2026-02-22
- 严重程度：中

## 一、问题描述

在用户管理页面（管理员编辑用户信息）中，填写并提交“学号/工号”（前端字段 `userCode`）后，页面提示“修改成功”，但再次查看用户详情时学号/工号并未保存到数据库。

## 二、原因分析

1. 前端统一使用 `userCode` 作为学号/工号的单一字段（见 `src/components/user/UserFormModal.vue`），提交的数据里含有 `userCode`。
2. 后端 `UpdateUserDTO` 和 `UserServiceImpl.updateUser` 原先只支持独立的 `studentNo` 与 `employeeNo` 字段，**不识别 `userCode`**，因此前端提交的 `userCode` 被忽略，导致数据库字段没有更新。

## 三、修复内容（已实现）

已在后端做出兼容性修复，主要变更：

- 修改 `UpdateUserDTO`，新增 `userCode` 字段（作为前端统一字段的后端承接）。
- 修改 `UserServiceImpl.updateUser`：
  - 优先处理 `userCode`，若存在则根据用户角色判断是学生还是教师，分别更新 `studentNo` 或 `employeeNo`。
  - 若 `userCode` 不存在，保留原有对 `studentNo` / `employeeNo` 的单独更新逻辑以兼容旧调用。
- 修改 `buildUserVO`：新增 `userCode` 返回（前端展示/详情统一使用 `userCode`，后端根据角色返回学号或工号）。

影响的后端文件：

- `complete-backend/src/main/java/com/yuwan/completebackend/model/dto/UpdateUserDTO.java`（新增 `userCode`）
- `complete-backend/src/main/java/com/yuwan/completebackend/service/impl/UserServiceImpl.java`（新增 `userCode` 处理逻辑并在 VO 构建中返回 `userCode`）
- `complete-backend/src/main/java/com/yuwan/completebackend/model/vo/UserVO.java`（新增 `userCode` 字段，用于前端统一接收）

（前端 `UserFormModal.vue` 已使用 `userCode` 字段，故无需改动前端提交逻辑）

## 四、验证步骤

1. 重新编译并重启后端服务：

```powershell
cd complete-backend
mvn -DskipTests package
# 重启后端服务（根据本地启动方式执行）
```

2. 前端进入用户管理页面，编辑某用户，填写“学号/工号”，保存。
3. 刷新用户详情页或重新打开该用户的编辑弹窗，确认“学号/工号”已显示且与刚刚提交的值一致。
4. 可在数据库中验证：查询 `user_info` 表对应用户的 `student_no` 或 `employee_no` 字段值是否正确写入。

## 五、根因与建议

- 根因：前后端字段契约不一致（前端统一字段 `userCode`，后端原先拆为 `studentNo`/`employeeNo` 且 DTO 未兼容）。
- 建议：统一接口契约并在 API 文档中明确学号/工号的传递字段（例如前端统一使用 `userCode`，后端在 DTO/VO 中兼容），或使用 OpenAPI 自动生成客户端代码以避免类似不一致。

---

# 角色权限配置缺失问题 — 企业教师等角色无法访问课题管理

- 日期：2026-02-22
- 严重程度：高（阻塞企业教师等核心角色使用系统）

## 一、问题描述

### 问题表现

1. **问题1：企业教师登录后可以看到"用户管理"菜单**
   - 企业教师不应该有用户管理权限
   - 实际显示了用户列表和角色权限菜单
   
2. **问题2：企业教师访问"课题列表"显示无权限**
   - 企业教师按业务流程应该有创建课题申报的权限
   - 实际登录后打开课题列表被重定向到403页面

### 影响范围

- 企业教师（ENTERPRISE_TEACHER）
- 企业负责人（ENTERPRISE_LEADER）
- 高校教师（UNIVERSITY_TEACHER）
- 专业方向主管（MAJOR_DIRECTOR）
- 督导教师（SUPERVISOR_TEACHER）
- 学生（STUDENT）

所有非系统管理员角色都无法正常使用系统。

## 二、问题根源分析

### 2.1 前后端权限控制不一致

**后端使用基于角色的访问控制（RBAC - Role）：**
```java
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_TEACHER')")
public Result<TopicVO> createTopic(@RequestBody @Valid CreateTopicDTO createDTO) {
    // 检查角色，ENTERPRISE_TEACHER 可以通过
}
```

**前端使用基于权限的访问控制（RBAC - Permission）：**
```typescript
{
  path: 'topic/list',
  meta: { permission: 'topic:view' }  // 检查权限编码
}

// 路由守卫
if (to.meta.permission && !userStore.hasPermission(to.meta.permission)) {
  next({ path: '/403' })  // 没有权限，跳转403
}
```

### 2.2 数据库权限配置缺失

**问题1：缺少课题管理相关的权限定义**

在 `user_module.sql` 中只定义了用户管理相关的权限：
```sql
INSERT INTO permission_info VALUES
('100', '0', '仪表盘', 'dashboard:view', ...),
('200', '0', '用户管理', 'user:manage', ...),
('201', '200', '用户列表', 'user:view', ...),
('202', '200', '角色权限', 'role:view', ...);
```

**完全缺少课题管理权限定义：**
- `topic:manage` - 课题管理菜单
- `topic:view` - 课题列表查看
- `topic:create` - 创建课题
- `topic:edit` - 编辑课题
- `topic:delete` - 删除课题
- `topic:submit` - 提交课题
- `topic:withdraw` - 撤回课题
- `topic:sign` - 签名审核

**问题2：只为系统管理员分配了权限**

```sql
-- 只有这一条角色权限分配记录
INSERT INTO role_permission (id, role_id, permission_id) VALUES
('1', '1', '100'),  -- 系统管理员 -> 仪表盘
('2', '1', '200'),  -- 系统管理员 -> 用户管理
-- ...
('13', '1', '2024'); -- 系统管理员 -> 分配权限
```

其他角色（企业教师、企业负责人等）的 `role_permission` 表中**完全没有数据**。

### 2.3 权限检查链路

```
用户登录（企业教师）
  ↓
前端获取用户信息
  userInfo: { roles: ['ENTERPRISE_TEACHER'], permissions: [] }  // ← permissions 为空
  ↓
访问 /topic/list
  ↓
路由守卫检查: hasPermission('topic:view')
  ↓
企业教师的 permissions 数组为空
  ↓
检查失败，跳转 /403
```

## 三、修复方案

### 3.1 创建权限配置修复脚本

**文件：** `complete-backend/docs/sql/fix_role_permissions.sql`

该脚本完成以下工作：

1. **添加课题管理相关权限定义**
   - 课题管理菜单（400）
   - 课题列表（401）
   - 创建课题（4011）
   - 编辑课题（4012）
   - 删除课题（4013）
   - 提交课题（4014）
   - 撤回课题（4015）
   - 签名审核（4016）

2. **为各角色分配适当权限**

| 角色 | 仪表盘 | 个人中心 | 用户管理 | 课题列表 | 创建课题 | 编辑课题 | 删除课题 | 提交/撤回 | 签名审核 |
|------|:------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| 系统管理员 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 企业教师 | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| 企业负责人 | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| 高校教师 | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| 专业方向主管 | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| 督导教师 | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| 学生 | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |

### 3.2 创建测试用户脚本

**文件：** `complete-backend/docs/sql/create_test_users.sql`

创建以下测试账号（密码均为：123456）：

- **企业教师**：工号 20001，姓名 王企业教师
- **企业负责人**：工号 20002，姓名 李企业负责人
- **高校教师**：工号 20003，姓名 张高校教师
- **学生**：学号 2024001，姓名 赵学生

## 四、执行步骤

### 4.1 执行SQL脚本

```bash
# 1. 修复角色权限配置
mysql -u root -p graduation_system < complete-backend/docs/sql/fix_role_permissions.sql

# 2. 创建测试用户（可选）
mysql -u root -p graduation_system < complete-backend/docs/sql/create_test_users.sql
```

### 4.2 重启后端服务

```bash
cd complete-backend
mvn clean package -DskipTests
# 重启 Spring Boot 应用
```

### 4.3 清除前端缓存并重新登录

1. 打开浏览器开发者工具（F12）
2. 在 Console 中执行：
   ```javascript
   localStorage.clear()
   sessionStorage.clear()
   ```
3. 刷新页面，重新登录

### 4.4 验证修复效果

**使用企业教师账号登录（20001 / 123456）：**
- ✅ 不应该看到"用户管理"菜单
- ✅ 可以看到"课题列表"菜单
- ✅ 可以点击"创建课题"按钮
- ✅ 可以编辑和删除自己创建的课题

**使用企业负责人账号登录（20002 / 123456）：**
- ✅ 不应该看到"用户管理"菜单
- ✅ 可以看到"课题列表"
- ✅ 不能创建课题
- ✅ 可以对课题进行签名审核

## 五、技术要点总结

### 5.1 RBAC权限模型

**两种权限控制方式：**

1. **基于角色（Role-Based）**
   - 优点：简单直接，适合粗粒度控制
   - 缺点：灵活性差，难以实现细粒度控制
   - 后端使用：`@PreAuthorize("hasRole('ADMIN')")`

2. **基于权限（Permission-Based）**
   - 优点：灵活，支持细粒度控制
   - 缺点：配置复杂，需要维护权限数据
   - 前端使用：`hasPermission('topic:create')`

### 5.2 前后端权限一致性

**最佳实践：**
- 后端：使用 `@PreAuthorize("hasAuthority('topic:create')")` 检查权限编码
- 前端：使用 `hasPermission('topic:create')` 检查权限编码
- 数据库：在 `permission_info` 和 `role_permission` 中维护权限关系

**本系统采用混合方式：**
- 后端：基于角色控制（已实现）
- 前端：基于权限控制（已实现）
- 数据库：通过 `role_permission` 映射角色到权限

这样既保证了后端的简洁性，又满足了前端细粒度的权限控制需求。

### 5.3 权限数据初始化的重要性

在系统初始化时必须：
1. 定义完整的权限树（菜单权限 + 按钮权限）
2. 为每个角色分配合理的权限
3. 确保权限编码与前端路由配置一致

### 5.4 权限检查顺序

```
前端路由守卫（第一道防线）
  ↓ 检查 permission
后端接口权限注解（第二道防线）
  ↓ 检查 role
数据库数据权限（第三道防线）
  ↓ 只返回用户有权访问的数据
```

## 六、预防措施建议

1. **权限配置文档化**
   - 维护角色权限矩阵表
   - 新增功能模块时同步更新权限配置

2. **自动化测试**
   - 为每个角色编写权限测试用例
   - 验证菜单显示和功能可访问性

3. **权限管理界面**
   - 实现可视化的角色权限分配页面
   - 支持动态调整角色权限

4. **统一权限控制**
   - 前后端统一使用权限编码
   - 避免前后端权限检查逻辑不一致

## 七、相关文件清单

**新增SQL脚本：**
- `complete-backend/docs/sql/fix_role_permissions.sql` - 权限配置修复脚本
- `complete-backend/docs/sql/create_test_users.sql` - 测试用户创建脚本

**涉及的核心文件：**
- `complete-frontend/src/router/index.ts` - 前端路由权限配置
- `complete-frontend/src/stores/user.ts` - 用户权限检查逻辑
- `complete-backend/src/main/java/com/yuwan/completebackend/controller/TopicController.java` - 后端接口权限控制
- `complete-backend/docs/sql/user_module.sql` - 用户模块初始化（需补充权限定义）

---

# 企业管理功能404错误 — Controller路径重复/api前缀

- 日期：2026-02-22
- 严重程度：高（阻塞企业管理功能使用）

## 一、问题描述

### 问题表现

管理员登录系统后，点击"企业管理"菜单，页面显示两个错误：
1. **请求资源不存在**（HTTP 404 Not Found）
2. **加载企业列表失败**

数据库确认已创建 `enterprise_info` 表且有数据，但前端无法加载。

### 影响范围

- 企业管理模块完全无法使用
- 所有企业相关的增删改查操作均失败

## 二、问题根源分析

### 2.1 路径映射重复前缀问题

**核心问题：EnterpriseController 的 `@RequestMapping` 路径包含了 `/api` 前缀，而 `application.yml` 中已配置全局 `context-path: /api`，导致路径重复。**

### 2.2 路径解析流程（错误）

```
前端请求: GET /enterprise/list
  ↓
Axios baseURL: /api
  ↓ 实际请求
GET /api/enterprise/list
  ↓
Vite 代理转发
  ↓ 转发到后端
http://localhost:8080/api/enterprise/list
  ↓
Spring Boot 解析
  ↓ context-path: /api
去除 /api 前缀 → 剩余路径: /enterprise/list
  ↓
Spring MVC 匹配
  ↓ 尝试匹配 @RequestMapping("/api/enterprise")
需要路径: /api/enterprise/list
实际路径: /enterprise/list
  ↓
匹配失败！返回 404 Not Found
```

### 2.3 配置冲突详解

**1. application.yml 全局配置：**
```yaml
server:
  port: 8080
  servlet:
    context-path: /api  # ← 所有 Controller 自动添加 /api 前缀
```

**2. EnterpriseController 路径映射（错误）：**
```java
@RestController
@RequestMapping("/api/enterprise")  // ← 错误：又写了一遍 /api
@Tag(name = "企业管理")
public class EnterpriseController {
    
    @GetMapping("/list")  // 期望路径：/api/enterprise/list
    public Result<PageResult<EnterpriseVO>> getEnterpriseList(EnterpriseQueryVO queryVO) {
        // ...
    }
}
```

**3. 实际生效路径：**
- context-path 前缀：`/api`
- Controller 路径：`/api/enterprise`
- 方法路径：`/list`
- **最终完整路径**：`/api` + `/api/enterprise` + `/list` = `/api/api/enterprise/list` ❌

**4. 前端请求路径：**
```typescript
// request.ts
const service = axios.create({
  baseURL: '/api'  // ← Axios 基础路径
})

// enterprise.ts
getEnterpriseList(params) {
  return request.get('/enterprise/list', { params })  // ← 相对路径
}

// 实际请求：/api + /enterprise/list = /api/enterprise/list ✅
```

**5. 路径不匹配：**
- 前端请求：`/api/enterprise/list` ✅
- 后端路径：`/api/api/enterprise/list` ❌
- 结果：404 Not Found

### 2.4 同样的问题也存在于 TopicController

[TopicController.java](complete-backend/src/main/java/com/yuwan/completebackend/controller/TopicController.java) 也存在相同问题：

```java
@RestController
@RequestMapping("/api/topic")  // ← 错误：重复了 /api 前缀
public class TopicController {
    // ...
}
```

## 三、修复方案

### 3.1 修改 EnterpriseController 路径映射

**文件：** [EnterpriseController.java](complete-backend/src/main/java/com/yuwan/completebackend/controller/EnterpriseController.java)

```java
// 修改前（错误）
@RestController
@RequestMapping("/api/enterprise")  // ❌ 重复前缀
public class EnterpriseController {
    // ...
}

// 修改后（正确）
@RestController
@RequestMapping("/enterprise")  // ✅ 相对路径，不包含 /api
public class EnterpriseController {
    // ...
}
```

### 3.2 修改 TopicController 路径映射

**文件：** [TopicController.java](complete-backend/src/main/java/com/yuwan/completebackend/controller/TopicController.java)

```java
// 修改前（错误）
@RestController
@RequestMapping("/api/topic")  // ❌ 重复前缀
public class TopicController {
    // ...
}

// 修改后（正确）
@RestController
@RequestMapping("/topic")  // ✅ 相对路径，不包含 /api
public class TopicController {
    // ...
}
```

### 3.3 路径解析流程（修复后）

```
前端请求: GET /enterprise/list
  ↓
Axios baseURL: /api
  ↓ 实际请求
GET /api/enterprise/list
  ↓
Vite 代理转发
  ↓ 转发到后端
http://localhost:8080/api/enterprise/list
  ↓
Spring Boot 解析
  ↓ context-path: /api
去除 /api 前缀 → 剩余路径: /enterprise/list
  ↓
Spring MVC 匹配
  ↓ 匹配 @RequestMapping("/enterprise") + @GetMapping("/list")
需要路径: /enterprise/list
实际路径: /enterprise/list
  ↓
匹配成功！✅ 正常处理请求
```

## 四、路径配置规范总结

### 4.1 统一路径配置规则

| 配置层 | 配置项 | 值 | 说明 |
|--------|--------|-----|------|
| **后端全局** | `server.servlet.context-path` | `/api` | 所有 Controller 统一前缀 |
| **后端 Controller** | `@RequestMapping` | `/enterprise` | 相对路径，**不包含 /api** |
| **前端 Axios** | `baseURL` | `/api` | 请求基础路径 |
| **前端 API** | `request.get()` | `/enterprise/list` | 相对路径，不包含 /api |
| **Vite 代理** | `proxy['/api'].target` | `http://localhost:8080` | 开发环境代理 |

### 4.2 正确的 Controller 路径写法

**✅ 正确写法（与 UserController、RoleController 一致）：**
```java
// application.yml: context-path: /api

@RequestMapping("/user")       // ✅ 最终路径：/api/user
@RequestMapping("/role")       // ✅ 最终路径：/api/role
@RequestMapping("/enterprise") // ✅ 最终路径：/api/enterprise
@RequestMapping("/topic")      // ✅ 最终路径：/api/topic
```

**❌ 错误写法（重复前缀）：**
```java
@RequestMapping("/api/enterprise") // ❌ 最终路径：/api/api/enterprise
@RequestMapping("/api/topic")      // ❌ 最终路径：/api/api/topic
```

### 4.3 其他 Controller 检查结果

检查现有所有 Controller 的路径配置：

| Controller | 原路径 | 是否正确 | 说明 |
|-----------|--------|----------|------|
| AuthController | `/auth` | ✅ | 正确 |
| UserController | `/user` | ✅ | 正确 |
| RoleController | `/role` | ✅ | 正确 |
| EnterpriseController | `/api/enterprise` | ❌ | **已修复** |
| TopicController | `/api/topic` | ❌ | **已修复** |
| HealthController | `/health` | ✅ | 正确 |

只有 EnterpriseController 和 TopicController 存在路径重复问题。

## 五、验证步骤

### 5.1 重启后端服务

```bash
cd complete-backend
mvn spring-boot:run
```

**注意：** 修改了 Controller 代码，必须重启后端服务才能生效。

### 5.2 清除前端缓存

在浏览器控制台执行：
```javascript
localStorage.clear()
sessionStorage.clear()
```

### 5.3 测试企业管理功能

1. **登录管理员账号**（admin / 123456）
2. **点击"企业管理"菜单**
3. **验证功能**：
   - ✅ 企业列表正常加载
   - ✅ 点击"新建企业"可以创建
   - ✅ 编辑、删除、状态切换功能正常
   - ✅ 搜索和分页功能正常

### 5.4 验证路径生效

**方法1：查看后端启动日志**
```
Mapped "{[/enterprise/list],methods=[GET]}" onto ...
Mapped "{[/enterprise],methods=[POST]}" onto ...
```
应该看到 `/enterprise/*` 而不是 `/api/enterprise/*`

**方法2：浏览器 Network 面板**
- 请求 URL：`http://localhost:5173/api/enterprise/list`
- 响应状态：`200 OK`（不是 404）

## 六、问题总结

### 6.1 错误原因分析

| 层面 | 问题 | 原因 |
|------|------|------|
| **架构设计** | 全局 context-path 未被正确理解 | 开发人员不清楚 context-path 会自动添加前缀 |
| **代码规范** | Controller 路径不一致 | 部分 Controller 写了 `/api` 前缀，其他没写 |
| **测试覆盖** | 未发现路径错误 | 开发时可能先修复了权限问题，未测试路径 |
| **文档缺失** | 缺少路径配置规范文档 | 团队没有统一的 Controller 路径书写规范 |

### 6.2 根本原因

1. **Spring Boot context-path 理解错误**
   - `server.servlet.context-path` 是**全局前缀**
   - 所有 `@RequestMapping` 都会**自动添加**这个前缀
   - Controller 中**不应该再写** context-path 的值

2. **代码一致性检查不足**
   - 部分 Controller 写了 `/api` 前缀
   - 部分 Controller 没写（正确）
   - 缺少统一的代码审查标准

### 6.3 经验教训

**1. 理解 Spring Boot 配置的作用域**
- `context-path` 是容器级别的配置，自动应用于所有请求
- Controller 只需关注业务模块的相对路径

**2. 保持代码一致性**
- 新增 Controller 时参考已有正确的 Controller
- 执行代码审查时检查路径配置

**3. 完善测试覆盖**
- 接口测试应包含路径正确性验证
- 集成测试应覆盖前后端完整请求链路

**4. 建立开发规范**
- 编写并维护 Controller 开发规范文档
- 包含路径配置、权限注解、异常处理等标准

## 七、预防措施建议

### 7.1 添加开发规范文档

创建 `docs/backend-coding-standards.md`，明确规定：

```markdown
## Controller 路径配置规范

### 全局配置
application.yml 已配置 context-path: /api
所有 Controller 的请求都会自动添加 /api 前缀。

### Controller 路径规范
❌ 错误：@RequestMapping("/api/xxx")
✅ 正确：@RequestMapping("/xxx")

### 示例
@RestController
@RequestMapping("/user")  // 最终路径：/api/user
public class UserController {
    @GetMapping("/list")  // 最终路径：/api/user/list
    public Result<List<User>> list() { ... }
}
```

### 7.2 代码审查清单

在 Pull Request 审查时检查：
- [ ] Controller 的 `@RequestMapping` 不包含 `/api` 前缀
- [ ] 路径风格与现有 Controller 一致
- [ ] 接口路径在 Swagger 文档中正确显示
- [ ] 前端 API 调用路径正确

### 7.3 自动化检查

可以编写自定义的 CheckStyle 或 PMD 规则，检测：
```java
// 检测规则：@RequestMapping 的值不能以 /api 开头
@RequestMapping("/api/...")  // 应该触发警告
```

### 7.4 接口测试增强

在集成测试中添加路径验证：
```java
@Test
public void testEnterpriseListPath() {
    mockMvc.perform(get("/api/enterprise/list"))  // 完整路径测试
           .andExpect(status().isOk());
}
```

## 八、相关文件清单

**修改的文件：**
- `complete-backend/src/main/java/com/yuwan/completebackend/controller/EnterpriseController.java` - 移除 /api 前缀
- `complete-backend/src/main/java/com/yuwan/completebackend/controller/TopicController.java` - 移除 /api 前缀

**相关配置文件：**
- `complete-backend/src/main/resources/application.yml` - context-path 配置
- `complete-frontend/vite.config.ts` - 前端代理配置
- `complete-frontend/src/api/request.ts` - Axios baseURL 配置

**参考的正确示例：**
- `complete-backend/src/main/java/com/yuwan/completebackend/controller/UserController.java` - 正确用法
- `complete-backend/src/main/java/com/yuwan/completebackend/controller/RoleController.java` - 正确用法

---

# 角色权限配置API缺失问题 — Permission树形结构查询

- 日期：2026-02-24
- 严重程度：高（阻塞角色权限配置功能）

## 一、问题描述

### 问题表现

管理员登录后，点击角色列表中的"权限配置"按钮时，前端显示**"请求资源不存在"**错误，权限配置弹窗无法正常显示权限树。

### 影响范围

- 角色权限配置功能完全无法使用
- 无法为角色分配或修改权限
- 影响所有需要配置角色权限的场景

## 二、问题根源分析

### 2.1 前端请求分析

前端代码在打开权限配置弹窗时，会并行请求两个接口：

```typescript
// RoleList.vue
const handlePermission = async (role: RoleInfo) => {
  currentRole.value = role
  permissionVisible.value = true
  permissionLoading.value = true

  try {
    // 并行加载权限树和角色已有权限
    await loadPermissionTree()  // ← 调用 GET /permission/tree
    await loadRolePermissions(role.roleId)  // ← 调用 GET /role/{roleId}/permission-ids
    
    permissionLoading.value = false
  } catch (error) {
    // 错误处理
  }
}
```

### 2.2 后端API缺失

检查后端 Controller 发现：
- ✅ `RoleController` 已有角色相关接口
- ❌ **缺少 `PermissionController`**，无法响应 `/permission/tree` 请求
- ❌ **RoleController 缺少权限相关的端点**

**前端需要但后端缺失的接口：**
1. `GET /permission/tree` - 获取权限树形结构
2. `GET /role/{roleId}/permissions` - 获取角色权限列表
3. `GET /role/{roleId}/permission-ids` - 获取角色权限ID列表
4. `PUT /role/{roleId}/permissions` - 更新角色权限

### 2.3 错误链路

```
用户点击"权限配置"
  ↓
前端调用: permissionApi.getPermissionTree()
  ↓ 请求: GET /api/permission/tree
后端 Spring MVC 路径匹配
  ↓ 查找 @RequestMapping("/permission")
找不到对应的 Controller
  ↓
返回 HTTP 404 Not Found
  ↓
前端显示"请求资源不存在"
```

## 三、修复方案

### 3.1 创建 PermissionVO（树形结构）

**文件：** `complete-backend/src/main/java/com/yuwan/completebackend/model/vo/PermissionVO.java`

```java
@Data
@Schema(description = "权限信息响应（树形结构）")
public class PermissionVO implements Serializable {
    
    @Schema(description = "权限ID")
    private String permissionId;
    
    @Schema(description = "父权限ID")
    private String parentId;
    
    @Schema(description = "权限名称")
    private String permissionName;
    
    @Schema(description = "权限编码")
    private String permissionCode;
    
    @Schema(description = "权限类型（1-菜单 2-按钮）")
    private Integer permissionType;
    
    @Schema(description = "路由路径")
    private String path;
    
    @Schema(description = "图标")
    private String icon;
    
    @Schema(description = "排序号")
    private Integer sortOrder;
    
    @Schema(description = "子权限列表")
    private List<PermissionVO> children;  // ← 支持树形结构
}
```

### 3.2 创建 IPermissionService 接口

**文件：** `complete-backend/src/main/java/com/yuwan/completebackend/service/IPermissionService.java`

```java
public interface IPermissionService {
    
    /**
     * 获取权限树（所有权限的树形结构）
     */
    List<PermissionVO> getPermissionTree();
    
    /**
     * 获取所有权限列表（平铺）
     */
    List<PermissionVO> getAllPermissions();
    
    /**
     * 获取角色的权限列表（树形结构）
     */
    List<PermissionVO> getPermissionsByRoleId(String roleId);
    
    /**
     * 获取角色的权限ID列表（用于前端树组件回显）
     */
    List<String> getPermissionIdsByRoleId(String roleId);
    
    /**
     * 更新角色权限
     */
    void updateRolePermissions(String roleId, List<String> permissionIds);
}
```

### 3.3 实现 PermissionServiceImpl

**文件：** `complete-backend/src/main/java/com/yuwan/completebackend/service/impl/PermissionServiceImpl.java`

**关键逻辑 — 构建权限树：**

```java
@Override
public List<PermissionVO> getPermissionTree() {
    // 1. 查询所有权限
    List<Permission> allPermissions = permissionMapper.selectList(
        new LambdaQueryWrapper<Permission>()
            .orderByAsc(Permission::getSortOrder)
    );
    
    // 2. 转换为 VO
    List<PermissionVO> permissionVOs = allPermissions.stream()
        .map(this::convertToVO)
        .collect(Collectors.toList());
    
    // 3. 构建树形结构
    return buildTree(permissionVOs, "0");  // "0" 为根节点的 parentId
}

/**
 * 递归构建树形结构
 */
private List<PermissionVO> buildTree(List<PermissionVO> allNodes, String parentId) {
    return allNodes.stream()
        .filter(node -> parentId.equals(node.getParentId()))
        .peek(node -> {
            // 递归查找子节点
            List<PermissionVO> children = buildTree(allNodes, node.getPermissionId());
            node.setChildren(children.isEmpty() ? null : children);
        })
        .collect(Collectors.toList());
}
```

**角色权限更新逻辑：**

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void updateRolePermissions(String roleId, List<String> permissionIds) {
    // 1. 删除角色的所有现有权限
    rolePermissionMapper.delete(
        new LambdaQueryWrapper<RolePermission>()
            .eq(RolePermission::getRoleId, roleId)
    );
    
    // 2. 批量插入新权限
    if (permissionIds != null && !permissionIds.isEmpty()) {
        List<RolePermission> rolePermissions = permissionIds.stream()
            .map(permissionId -> {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                return rp;
            })
            .collect(Collectors.toList());
        
        // 使用 MyBatis-Plus 的 saveBatch 批量插入
        rolePermissions.forEach(rolePermissionMapper::insert);
    }
    
    log.info("更新角色权限成功，角色ID: {}, 权限数量: {}", roleId, 
            permissionIds != null ? permissionIds.size() : 0);
}
```

### 3.4 创建 PermissionController

**文件：** `complete-backend/src/main/java/com/yuwan/completebackend/controller/PermissionController.java`

```java
@Slf4j
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
@Tag(name = "权限管理接口", description = "权限树查询与管理")
public class PermissionController {

    private final IPermissionService permissionService;

    /**
     * 获取权限树（所有权限的树形结构）
     */
    @GetMapping("/tree")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取权限树", description = "获取系统所有权限的树形结构")
    public Result<List<PermissionVO>> getPermissionTree() {
        log.info("获取权限树");
        List<PermissionVO> tree = permissionService.getPermissionTree();
        return Result.success(tree);
    }

    /**
     * 获取所有权限列表（平铺）
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取权限列表", description = "获取所有权限的平铺列表")
    public Result<List<PermissionVO>> getAllPermissions() {
        log.info("获取所有权限列表");
        List<PermissionVO> permissions = permissionService.getAllPermissions();
        return Result.success(permissions);
    }
}
```

### 3.5 扩展 RoleController（添加权限相关接口）

**文件：** `complete-backend/src/main/java/com/yuwan/completebackend/controller/RoleController.java`

```java
@RestController
@RequestMapping("/role")
public class RoleController {
    
    private final IRoleService roleService;
    private final IPermissionService permissionService;  // ← 注入权限服务

    /**
     * 获取角色的权限列表
     */
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取角色权限", description = "获取指定角色的权限列表")
    public Result<List<PermissionVO>> getRolePermissions(
            @PathVariable String roleId) {
        log.info("获取角色权限，角色ID: {}", roleId);
        List<PermissionVO> permissions = permissionService.getPermissionsByRoleId(roleId);
        return Result.success(permissions);
    }

    /**
     * 获取角色的权限ID列表
     */
    @GetMapping("/{roleId}/permission-ids")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取角色权限ID", description = "获取指定角色的权限ID列表")
    public Result<List<String>> getRolePermissionIds(
            @PathVariable String roleId) {
        log.info("获取角色权限ID，角色ID: {}", roleId);
        List<String> permissionIds = permissionService.getPermissionIdsByRoleId(roleId);
        return Result.success(permissionIds);
    }

    /**
     * 更新角色权限
     */
    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "更新角色权限", description = "更新指定角色的权限配置")
    public Result<Void> updateRolePermissions(
            @PathVariable String roleId,
            @RequestBody Map<String, List<String>> body) {
        List<String> permissionIds = body.get("permissionIds");
        log.info("更新角色权限，角色ID: {}, 权限数量: {}", roleId, 
                permissionIds != null ? permissionIds.size() : 0);
        permissionService.updateRolePermissions(roleId, 
                permissionIds != null ? permissionIds : List.of());
        return Result.success();
    }
}
```

## 四、技术要点总结

### 4.1 树形数据结构的构建

**问题：** 数据库存储的是扁平的父子关系，前端需要嵌套的树形结构。

**解决方案：** 
1. 查询所有权限数据（按 sortOrder 排序）
2. 使用递归算法构建树形结构
3. 通过 `parentId` 找到每个节点的子节点

**关键代码：**
```java
private List<PermissionVO> buildTree(List<PermissionVO> allNodes, String parentId) {
    return allNodes.stream()
        .filter(node -> parentId.equals(node.getParentId()))  // 找到当前层级的节点
        .peek(node -> {
            // 递归查找子节点
            List<PermissionVO> children = buildTree(allNodes, node.getPermissionId());
            node.setChildren(children.isEmpty() ? null : children);
        })
        .collect(Collectors.toList());
}
```

### 4.2 批量更新的事务处理

**问题：** 更新角色权限需要先删除旧数据再插入新数据，必须保证原子性。

**解决方案：**
- 使用 `@Transactional` 注解保证事务
- 先 DELETE 后 INSERT
- 如果出错自动回滚

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void updateRolePermissions(String roleId, List<String> permissionIds) {
    // 1. 删除旧权限
    rolePermissionMapper.delete(...);
    
    // 2. 插入新权限
    permissionIds.forEach(id -> rolePermissionMapper.insert(...));
}
```

### 4.3 前后端权限树数据格式

**前端需要的格式（Ant Design Vue Tree）：**
```json
[
  {
    "key": "100",
    "title": "仪表盘",
    "children": []
  },
  {
    "key": "200",
    "title": "用户管理",
    "children": [
      { "key": "201", "title": "用户列表" },
      { "key": "202", "title": "角色权限" }
    ]
  }
]
```

**后端返回的格式：**
```json
[
  {
    "permissionId": "100",
    "permissionName": "仪表盘",
    "children": null
  },
  {
    "permissionId": "200",
    "permissionName": "用户管理",
    "children": [
      { "permissionId": "201", "permissionName": "用户列表" },
      { "permissionId": "202", "permissionName": "角色权限" }
    ]
  }
]
```

前端通过映射转换：
```typescript
const treeData = permissionTree.map(item => ({
  key: item.permissionId,
  title: item.permissionName,
  children: item.children?.map(child => ({
    key: child.permissionId,
    title: child.permissionName
  }))
}))
```

## 五、验证步骤

1. **重启后端服务**
2. **清除前端缓存**
   ```javascript
   localStorage.clear()
   sessionStorage.clear()
   ```
3. **测试权限配置功能**：
   - 登录管理员账号
   - 进入角色权限页面
   - 点击某个角色的"权限配置"按钮
   - ✅ 权限树正常显示
   - ✅ 角色已有权限正确回显（勾选状态）
   - ✅ 修改权限并保存，提示成功
   - ✅ 重新打开权限配置，勾选状态正确

## 六、相关文件清单

**新增文件：**
- `PermissionVO.java` - 权限树形结构VO
- `IPermissionService.java` - 权限服务接口
- `PermissionServiceImpl.java` - 权限服务实现
- `PermissionController.java` - 权限管理Controller

**修改文件：**
- `RoleController.java` - 添加权限相关接口

---

# 角色CRUD API缺失问题 — 编辑角色功能无法使用

- 日期：2026-02-24
- 严重程度：高（阻塞角色编辑功能）

## 一、问题描述

### 问题表现

在角色权限管理页面，点击"编辑"按钮修改角色信息后，点击"确定"按钮时，前端显示**"请求资源不存在"**错误，角色信息无法保存。

### 影响范围

- 角色编辑功能完全无法使用
- 无法创建新角色
- 无法删除角色
- 影响角色管理的所有 CRUD 操作（除了查询）

## 二、问题根源分析

### 2.1 前端请求分析

前端在提交角色编辑表单时，会调用：

```typescript
// RoleList.vue
const handleFormSubmit = async () => {
  try {
    await roleFormRef.value?.validate()
  } catch {
    return
  }

  formLoading.value = true
  try {
    if (isEditMode.value && currentRole.value) {
      // 编辑模式
      const updateData: UpdateRoleDTO = {
        roleName: roleForm.roleName,
        description: roleForm.description
      }
      await roleApi.updateRole(currentRole.value.roleId, updateData)  // ← PUT /role/{roleId}
      message.success('角色更新成功')
    } else {
      // 创建模式
      await roleApi.createRole({
        roleName: roleForm.roleName,
        roleCode: roleForm.roleCode,
        description: roleForm.description
      })  // ← POST /role/create
      message.success('角色创建成功')
    }
    // ...
  }
}
```

### 2.2 后端API缺失

检查 `RoleController` 发现：
- ✅ `GET /role/list` - 查询角色列表（已有）
- ✅ `GET /role/user/{userId}` - 查询用户角色（已有）
- ❌ **`GET /role/{roleId}`** - 获取角色详情（缺失）
- ❌ **`POST /role/create`** - 创建角色（缺失）
- ❌ **`PUT /role/{roleId}`** - 更新角色（缺失）
- ❌ **`DELETE /role/{roleId}`** - 删除角色（缺失）

### 2.3 错误链路

```
用户点击"确定"保存角色
  ↓
前端调用: roleApi.updateRole(roleId, updateData)
  ↓ 请求: PUT /api/role/{roleId}
后端 Spring MVC 路径匹配
  ↓ 查找 @PutMapping("/{roleId}") in RoleController
找不到对应的方法
  ↓
返回 HTTP 404 Not Found
  ↓
前端显示"请求资源不存在"
```

## 三、修复方案

### 3.1 创建 CreateRoleDTO

**文件：** `complete-backend/src/main/java/com/yuwan/completebackend/model/dto/CreateRoleDTO.java`

```java
@Data
@Schema(description = "创建角色请求")
public class CreateRoleDTO implements Serializable {

    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称", required = true)
    private String roleName;

    @NotBlank(message = "角色代码不能为空")
    @Pattern(regexp = "^[A-Z_]+$", message = "角色代码只能包含大写字母和下划线")
    @Schema(description = "角色代码", required = true, example = "CUSTOM_ROLE")
    private String roleCode;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "权限ID列表")
    private List<String> permissionIds;
}
```

### 3.2 创建 UpdateRoleDTO

**文件：** `complete-backend/src/main/java/com/yuwan/completebackend/model/dto/UpdateRoleDTO.java`

```java
@Data
@Schema(description = "更新角色请求")
public class UpdateRoleDTO implements Serializable {

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "权限ID列表")
    private List<String> permissionIds;
}
```

**关键设计：**
- `CreateRoleDTO` 需要 `roleName` 和 `roleCode`（必填）
- `UpdateRoleDTO` 只需要 `roleName` 和 `description`（可选，不传则不修改）
- 两个 DTO 都支持 `permissionIds`，用于同时更新权限

### 3.3 扩展 IRoleService 接口

**文件：** `complete-backend/src/main/java/com/yuwan/completebackend/service/IRoleService.java`

```java
public interface IRoleService {
    
    /**
     * 查询所有角色
     */
    List<RoleVO> getAllRoles();
    
    /**
     * 查询用户角色
     */
    List<RoleVO> getRolesByUserId(String userId);
    
    /**
     * 获取角色详情
     */
    RoleVO getRoleById(String roleId);
    
    /**
     * 创建角色
     */
    RoleVO createRole(CreateRoleDTO createDTO);
    
    /**
     * 更新角色
     */
    RoleVO updateRole(String roleId, UpdateRoleDTO updateDTO);
    
    /**
     * 删除角色
     */
    void deleteRole(String roleId);
}
```

### 3.4 实现 RoleServiceImpl

**文件：** `complete-backend/src/main/java/com/yuwan/completebackend/service/impl/RoleServiceImpl.java`

**关键实现逻辑：**

```java
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final IPermissionService permissionService;

    @Override
    public RoleVO getRoleById(String roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }
        return convertToVO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "role:list", allEntries = true)
    public RoleVO createRole(CreateRoleDTO createDTO) {
        // 1. 检查角色代码是否已存在
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getRoleCode, createDTO.getRoleCode());
        queryWrapper.eq(Role::getDeleted, 0);
        if (roleMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("角色代码已存在");
        }

        // 2. 创建角色
        Role role = new Role();
        role.setRoleName(createDTO.getRoleName());
        role.setRoleCode(createDTO.getRoleCode());
        role.setRoleDesc(createDTO.getDescription());  // ← description → roleDesc
        role.setRoleStatus(1);
        role.setSortOrder(0);
        roleMapper.insert(role);

        // 3. 分配权限
        if (createDTO.getPermissionIds() != null && !createDTO.getPermissionIds().isEmpty()) {
            permissionService.updateRolePermissions(role.getRoleId(), createDTO.getPermissionIds());
        }

        log.info("创建角色成功，角色ID: {}, 角色名称: {}", role.getRoleId(), role.getRoleName());
        return convertToVO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "role:list", allEntries = true)
    public RoleVO updateRole(String roleId, UpdateRoleDTO updateDTO) {
        // 1. 查询角色
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }

        // 2. 更新基本信息
        if (StringUtils.hasText(updateDTO.getRoleName())) {
            role.setRoleName(updateDTO.getRoleName());
        }
        if (updateDTO.getDescription() != null) {
            role.setRoleDesc(updateDTO.getDescription());  // ← description → roleDesc
        }
        role.setUpdateTime(new Date());
        roleMapper.updateById(role);

        // 3. 更新权限
        if (updateDTO.getPermissionIds() != null) {
            permissionService.updateRolePermissions(roleId, updateDTO.getPermissionIds());
        }

        log.info("更新角色成功，角色ID: {}, 角色名称: {}", role.getRoleId(), role.getRoleName());
        return convertToVO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "role:list", allEntries = true)
    public void deleteRole(String roleId) {
        // 1. 查询角色
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }

        // 2. 检查是否有用户使用该角色
        LambdaQueryWrapper<UserRole> userRoleQuery = new LambdaQueryWrapper<>();
        userRoleQuery.eq(UserRole::getRoleId, roleId);
        if (userRoleMapper.selectCount(userRoleQuery) > 0) {
            throw new BusinessException("该角色已分配给用户，无法删除");
        }

        // 3. 删除角色权限关联
        LambdaQueryWrapper<RolePermission> permissionQuery = new LambdaQueryWrapper<>();
        permissionQuery.eq(RolePermission::getRoleId, roleId);
        rolePermissionMapper.delete(permissionQuery);

        // 4. 逻辑删除角色
        roleMapper.deleteById(roleId);

        log.info("删除角色成功，角色ID: {}", roleId);
    }
}
```

**关键点：**
1. **字段映射**：前端 `description` ↔ 后端 `roleDesc`
2. **事务处理**：CRUD 操作使用 `@Transactional` 保证原子性
3. **缓存清理**：修改操作使用 `@CacheEvict` 清除角色列表缓存
4. **业务校验**：
   - 创建时检查 `roleCode` 唯一性
   - 删除时检查是否有用户使用
5. **级联操作**：删除角色时同时删除角色权限关联

### 3.5 扩展 RoleController

**文件：** `complete-backend/src/main/java/com/yuwan/completebackend/controller/RoleController.java`

```java
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final IRoleService roleService;
    private final IPermissionService permissionService;

    // ... 已有的 list 和 user/{userId} 接口 ...

    /**
     * 获取角色详情
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取角色详情", description = "获取指定角色的详细信息")
    public Result<RoleVO> getRoleById(@PathVariable String roleId) {
        log.info("获取角色详情，角色ID: {}", roleId);
        RoleVO role = roleService.getRoleById(roleId);
        return Result.success(role);
    }

    /**
     * 创建角色
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "创建角色", description = "创建新角色并分配权限")
    public Result<RoleVO> createRole(@Valid @RequestBody CreateRoleDTO createDTO) {
        log.info("创建角色，角色名称: {}, 角色代码: {}", 
                createDTO.getRoleName(), createDTO.getRoleCode());
        RoleVO role = roleService.createRole(createDTO);
        return Result.success(role);
    }

    /**
     * 更新角色
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "更新角色", description = "更新角色信息和权限")
    public Result<RoleVO> updateRole(
            @PathVariable String roleId,
            @Valid @RequestBody UpdateRoleDTO updateDTO) {
        log.info("更新角色，角色ID: {}", roleId);
        RoleVO role = roleService.updateRole(roleId, updateDTO);
        return Result.success(role);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "删除角色", description = "删除指定角色")
    public Result<Void> deleteRole(@PathVariable String roleId) {
        log.info("删除角色，角色ID: {}", roleId);
        roleService.deleteRole(roleId);
        return Result.success();
    }
    
    // ... 权限相关接口 ...
}
```

## 四、技术要点总结

### 4.1 DTO 设计原则

**CreateDTO vs UpdateDTO：**

| 字段 | CreateDTO | UpdateDTO | 说明 |
|------|-----------|-----------|------|
| `roleName` | ✅ 必填 | ✅ 可选 | 创建必须提供，更新可选 |
| `roleCode` | ✅ 必填 | ❌ 不可改 | 角色代码一旦创建不可修改 |
| `description` | ✅ 可选 | ✅ 可选 | 描述信息可为空 |
| `permissionIds` | ✅ 可选 | ✅ 可选 | 权限列表可为空 |

**设计理由：**
- `roleCode` 是角色的唯一标识，创建后不应修改（避免影响已有的权限判断逻辑）
- `UpdateDTO` 的字段都是可选的，只更新传入的字段（部分更新）

### 4.2 字段映射处理

**前后端字段不一致：**
- 前端字段：`description`（符合前端命名习惯）
- 后端字段：`roleDesc`（数据库字段名）

**解决方案：**
- DTO 层使用前端字段名 `description`
- Service 层进行字段映射：`description` → `roleDesc`
- VO 层保持数据库字段名 `roleDesc`

```java
// UpdateRoleDTO
private String description;  // 前端传入

// Service 层映射
if (updateDTO.getDescription() != null) {
    role.setRoleDesc(updateDTO.getDescription());  // 映射到 roleDesc
}

// RoleVO
private String roleDesc;  // 返回给前端
```

### 4.3 缓存管理策略

**缓存注解使用：**

```java
// 查询方法：使用 @Cacheable
@Cacheable(value = "role:list")
public List<RoleVO> getAllRoles() { ... }

// 修改方法：使用 @CacheEvict
@CacheEvict(value = "role:list", allEntries = true)
public RoleVO createRole(CreateRoleDTO createDTO) { ... }

@CacheEvict(value = "role:list", allEntries = true)
public RoleVO updateRole(String roleId, UpdateRoleDTO updateDTO) { ... }

@CacheEvict(value = "role:list", allEntries = true)
public void deleteRole(String roleId) { ... }
```

**缓存策略：**
- 角色列表读多写少，适合使用缓存
- 任何修改操作都清空缓存（`allEntries = true`）
- 下次查询时重新加载最新数据

### 4.4 业务校验规则

**创建角色时的校验：**
1. `roleName` 不能为空（Bean Validation）
2. `roleCode` 不能为空且格式正确（`^[A-Z_]+$`）
3. `roleCode` 不能重复（数据库查询）

**删除角色时的校验：**
1. 角色必须存在
2. 角色未被用户使用（检查 `user_role` 表）
3. 先删除角色权限关联，再删除角色

## 五、验证步骤

1. **重启后端服务**
2. **清除前端缓存**
3. **测试创建角色**：
   - 点击"新建角色"
   - 填写角色名称和角色代码
   - 保存，验证创建成功
4. **测试编辑角色**：
   - 点击某个角色的"编辑"
   - 修改角色名称和描述
   - 保存，验证更新成功
5. **测试删除角色**：
   - 点击某个未使用的角色的"删除"
   - 确认删除，验证删除成功
   - 尝试删除已分配给用户的角色，应提示无法删除

## 六、相关文件清单

**新增文件：**
- `CreateRoleDTO.java` - 创建角色DTO
- `UpdateRoleDTO.java` - 更新角色DTO

**修改文件：**
- `IRoleService.java` - 添加 CRUD 方法签名
- `RoleServiceImpl.java` - 实现 CRUD 方法
- `RoleController.java` - 添加 CRUD 端点

---

# Maven JDK版本配置问题 — 编译失败无效标记

- 日期：2026-02-24
- 严重程度：高（阻塞后端编译和启动）

## 一、问题描述

### 问题表现

执行 `mvn compile` 时，编译失败并显示以下错误：

```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.11.0:compile 
(default-compile) on project complete-backend: Fatal error compiling: 无效的标记: --release
```

### 影响范围

- 无法编译后端项目
- 无法打包部署
- 阻塞所有后端开发工作

## 二、问题根源分析

### 2.1 Maven 使用的 JDK 版本不匹配

执行 `mvn -version` 查看 Maven 使用的 JDK 版本：

```
Apache Maven 3.9.9
Maven home: E:\Java_utils\Maven\maven-3.9.9
Java version: 1.8.0_202, vendor: Oracle Corporation, runtime: E:\java版本\Java_1.8\jdk-1.8.0\jre
```

**问题：Maven 使用的是 Java 1.8，但项目需要 Java 17。**

### 2.2 `--release` 标记不支持

`pom.xml` 中的编译器配置：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <release>17</release>  <!-- ← --release 标记 -->
        <encoding>UTF-8</encoding>
    </configuration>
</plugin>
```

**问题：**
- `<release>` 标记是 Java 9+ 引入的新特性
- Java 8 不支持 `--release` 参数
- Maven 使用 Java 8 编译时抛出"无效的标记"错误

### 2.3 Spring Boot 3.x 需要 Java 17+

**项目依赖：**
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.2</version>  <!-- ← Spring Boot 3.x 需要 Java 17+ -->
</parent>
```

**结论：** 必须使用 Java 17 编译项目。

### 2.4 错误链路

```
执行 mvn compile
  ↓
Maven 使用系统默认 JDK 1.8
  ↓
Maven Compiler Plugin 尝试使用 --release 17 参数
  ↓
Java 8 不识别 --release 参数
  ↓
抛出 "Fatal error compiling: 无效的标记: --release"
  ↓
编译失败
```

## 三、修复方案

### 方案选择

检查系统已安装 JDK 17：
```
E:\java版本\java_17
```

采用**修改 Maven Compiler Plugin 配置**的方式，显式指定 JDK 17 编译器路径。

### 3.1 修改 pom.xml 编译器配置

**文件：** `complete-backend/pom.xml`

**修改前（错误）：**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <release>17</release>  <!-- ❌ Java 8 不支持 -->
        <encoding>UTF-8</encoding>
    </configuration>
</plugin>
```

**修改后（正确）：**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <source>17</source>
        <target>17</target>
        <encoding>UTF-8</encoding>
        <fork>true</fork>  <!-- ← 启用独立进程编译 -->
        <executable>E:\java版本\java_17\bin\javac.exe</executable>  <!-- ← 指定 JDK 17 编译器 -->
        <compilerArgs>
            <arg>-parameters</arg>  <!-- 保留方法参数名 -->
        </compilerArgs>
    </configuration>
</plugin>
```

**配置说明：**
1. **`<source>17</source>`** - 指定源代码兼容 Java 17
2. **`<target>17</target>`** - 指定编译目标为 Java 17 字节码
3. **`<fork>true</fork>`** - 启用独立进程编译（避免使用 Maven 运行时 JDK）
4. **`<executable>`** - 显式指定 JDK 17 的 javac 编译器路径
5. **`<compilerArgs><arg>-parameters</arg>`** - 保留方法参数名（Spring Boot 需要）

### 3.2 为什么这样修改可以解决问题

**Maven Compiler Plugin 的编译器选择逻辑：**

1. **默认行为（`fork=false`）：**
   - 使用运行 Maven 的 JDK（当前是 Java 8）
   - 传递 `<release>17` 参数给 Java 8 编译器
   - Java 8 不识别 `--release`，抛出错误

2. **修改后（`fork=true` + `executable`）：**
   - Maven 创建独立的编译进程
   - 使用 `<executable>` 指定的 JDK 17 编译器
   - JDK 17 编译器正常工作
   - 编译成功

**为什么不改为 `<release>17>`：**
- `<release>17>` 等价于 `<source>17` + `<target>17` + `--release 17`
- 仍然需要 Maven 运行在 Java 9+ 环境
- 使用 `<source>` + `<target>` + `<executable>` 更灵活

### 3.3 验证修改

**执行编译：**
```bash
cd complete-backend
mvn clean compile -q
```

**预期结果：**
```
编译成功，无错误输出
```

**验证编译产物：**
```powershell
Test-Path "target\classes\com\yuwan\completebackend\controller\RoleController.class"
# 输出：True
```

## 四、其他解决方案（未采用）

### 方案1：修改系统 JAVA_HOME 环境变量

**优点：** 一劳永逸，Maven 自动使用 Java 17

**缺点：**
- 需要管理员权限修改系统环境变量
- 影响其他依赖 Java 8 的项目
- 重启终端才能生效

**实施方式：**
```powershell
# 临时设置（仅当前会话）
$env:JAVA_HOME = "E:\java版本\java_17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# 永久设置（需管理员权限）
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "E:\java版本\java_17", "Machine")
```

### 方案2：使用 Maven Toolchains

**优点：** 项目级配置，不影响其他项目

**缺点：**
- 需要额外配置 `~/.m2/toolchains.xml`
- 配置复杂，不适合新手

**实施方式：**

创建 `~/.m2/toolchains.xml`：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>17</version>
    </provides>
    <configuration>
      <jdkHome>E:\java版本\java_17</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

### 方案3：使用 `.mvn/jvm.config`（尝试失败）

尝试创建 `.mvn/jvm.config` 文件：
```
-Djava.home=E:\java版本\java_17
```

**失败原因：**
- 中文路径导致编码问题
- Maven ClassLoader 加载失败
- 抛出 `java.lang.InternalError: Error loading java.security file`

## 五、技术要点总结

### 5.1 Maven Compiler Plugin 配置项

| 配置项 | 说明 | 适用版本 |
|--------|------|----------|
| `<source>` | 源代码语言级别 | 所有 JDK |
| `<target>` | 编译目标字节码版本 | 所有 JDK |
| `<release>` | 统一设置 source/target/bootclasspath | Java 9+ |
| `<fork>` | 是否启用独立编译进程 | 所有 JDK |
| `<executable>` | 指定 javac 路径 | fork=true 时有效 |

### 5.2 Java 版本兼容性

| Spring Boot 版本 | 最低 Java 版本 | 推荐 Java 版本 |
|-----------------|---------------|---------------|
| 2.x | Java 8 | Java 11 |
| 3.x | Java 17 | Java 17/21 |

**本项目使用：**
- Spring Boot 3.2.2 → **必须使用 Java 17+**

### 5.3 `<release>` vs `<source>` + `<target>`

**`<release>` 的作用：**
```xml
<release>17</release>
```
等价于：
```xml
<source>17</source>
<target>17</target>
<compilerArgs>
  <arg>--release</arg>
  <arg>17</arg>
</compilerArgs>
```

**`--release` 参数的额外作用：**
- 确保编译时只能使用目标 JDK 版本的 API
- 例如：`<release>11</release>` 时不能使用 Java 17 新增的 API

**为什么不用 `<release>`：**
- 需要 Maven 运行在 Java 9+ 环境
- 当前环境 Maven 使用 Java 8
- 使用 `<source>` + `<target>` + `<fork>` + `<executable>` 更灵活

## 六、验证步骤

### 6.1 验证 JDK 17 可用

```powershell
& "E:\java版本\java_17\bin\java.exe" -version
```

**预期输出：**
```
java version "17.0.13" 2024-10-15 LTS
Java(TM) SE Runtime Environment (build 17.0.13+10-LTS-268)
Java HotSpot(TM) 64-Bit Server VM (build 17.0.13+10-LTS-268, mixed mode, sharing)
```

### 6.2 验证编译成功

```powershell
cd complete-backend
mvn clean compile
```

**预期输出：**
```
[INFO] BUILD SUCCESS
```

### 6.3 验证编译产物

```powershell
Test-Path "target\classes\com\yuwan\completebackend\controller\RoleController.class"
```

**预期输出：**
```
True
```

### 6.4 验证运行

```powershell
mvn spring-boot:run
```

**预期输出：**
```
Started CompleteBackendApplication in X.XXX seconds
```

## 七、预防措施建议

### 7.1 项目文档中明确 JDK 版本要求

在 `README.md` 中添加：

```markdown
## 开发环境要求

- JDK 17 或更高版本
- Maven 3.8+
- MySQL 8.0+

### 配置 Maven 使用 JDK 17

如果系统默认 JDK 不是 17，需要修改 pom.xml 中的 `<executable>` 路径：

```xml
<executable>你的JDK17路径/bin/javac.exe</executable>
```
```

### 7.2 添加 Maven Wrapper

使用 Maven Wrapper 可以锁定 Maven 版本：

```bash
mvn wrapper:wrapper -Dmaven=3.9.9
```

生成的 `mvnw` 和 `mvnw.cmd` 脚本会自动下载指定版本的 Maven。

### 7.3 CI/CD 配置

在 CI/CD 环境中明确指定 JDK 版本：

```yaml
# GitHub Actions 示例
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build with Maven
        run: mvn clean package
```

### 7.4 开发规范文档

创建 `docs/development-setup.md`，详细说明：
- JDK 安装和配置
- Maven 配置
- IDE 配置（IntelliJ IDEA / Eclipse）
- 常见问题排查

## 八、相关文件清单

**修改文件：**
- `complete-backend/pom.xml` - Maven Compiler Plugin 配置

**相关文档：**
- Maven Compiler Plugin：https://maven.apache.org/plugins/maven-compiler-plugin/
- Java Platform, Standard Edition Tools Reference (Java 17)：https://docs.oracle.com/en/java/javase/17/docs/specs/man/javac.html

---

# 课题审查API参数必传问题 — 我的审批统计接口

- 日期：2026-02-28
- 作者：系统维护者
- 严重程度：中（影响课题审查模块功能）

## 一、问题描述

### 问题表现
在课题审查页面点击"我的审批统计"按钮时，后端报错：
```
org.springframework.web.bind.MissingServletRequestParameterException: 
Required request parameter 'teacherId' for method parameter type String is not present
```

页面显示"系统内部错误"和"获取统计数据失败"。

### 影响范围
- 课题审查模块的"我的审批统计"功能无法使用
- 所有尝试查看自己审批统计的用户

## 二、问题根源分析

### 2.1 后端接口定义问题

**问题代码 (TopicReviewController.java)：**
```java
@GetMapping("/stats/passed-count")
public Result<TeacherPassedCountVO> getTeacherPassedCount(
        @RequestParam String teacherId) {  // ❌ 必传参数
    // ...
}
```

**前端调用 (topicReview.ts)：**
```typescript
getTeacherPassedCount(teacherId?: string) {
    return request.get<TeacherPassedCountVO>('/topic/review/stats/passed-count', {
        params: teacherId ? { teacherId } : undefined  // 不传teacherId时查当前用户
    })
}
```

### 2.2 业务逻辑缺陷

接口设计要求必须传入`teacherId`，但实际业务场景中：
- 用户查看自己的统计时，不需要传入ID，应该自动使用当前登录用户
- 管理员查看其他教师统计时，才需要传入指定的`teacherId`

## 三、修复方案

### 3.1 修改Controller层

```java
// 修改前
@RequestParam String teacherId

// 修改后
@RequestParam(required = false) String teacherId

// 并在方法内添加逻辑
if (teacherId == null || teacherId.isEmpty()) {
    teacherId = SecurityUtil.getCurrentUserId();
}
```

### 3.2 扩展权限配置

```java
// 修改前
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER', 'SUPERVISOR_TEACHER')")

// 修改后：增加ENTERPRISE_TEACHER，使企业教师可以查看自己的统计
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER', 'SUPERVISOR_TEACHER', 'ENTERPRISE_TEACHER')")
```

## 四、修改文件清单

| 文件 | 修改内容 |
|---|---|
| `TopicReviewController.java` | 添加`SecurityUtil`导入，`teacherId`设为可选，添加默认值逻辑 |

## 五、经验教训

### 5.1 设计原则

**API设计时考虑"查询自己"场景：**
- 对于涉及当前用户数据的查询接口，ID参数应设为可选
- 不传ID时，默认查询当前登录用户的数据
- 这样可以简化前端调用，减少不必要的参数传递

### 5.2 开发检查清单

- [ ] `@RequestParam`是否需要设置`required = false`？
- [ ] 不传参数时的默认行为是什么？
- [ ] 权限配置是否覆盖了所有需要使用该接口的角色？

---

# 课题审查API参数必传问题 — 综合意见列表接口

- 日期：2026-02-28
- 作者：系统维护者
- 严重程度：中（影响课题审查模块功能）

## 一、问题描述

### 问题表现
在课题审查页面点击"综合意见管理"按钮时，后端报错：
```
org.springframework.web.bind.MissingServletRequestParameterException: 
Required request parameter 'guidanceDirection' for method parameter type String is not present
```

页面显示"系统内部错误"和"获取综合意见失败"。

### 影响范围
- 课题审查模块的"综合意见管理"功能无法使用
- 所有尝试查看或管理综合意见的用户

## 二、问题根源分析

### 2.1 后端接口定义问题

**问题代码 (TopicReviewController.java)：**
```java
@GetMapping("/general-opinions")
public Result<List<GeneralOpinionVO>> getGeneralOpinions(
        @RequestParam(required = false) Integer reviewStage,
        @RequestParam String guidanceDirection) {  // ❌ 必传参数
    // ...
}
```

**前端调用 (topicReview.ts)：**
```typescript
getGeneralOpinions(guidanceDirection?: string) {
    return request.get<GeneralOpinionVO[]>('/topic/review/general-opinions', {
        params: guidanceDirection ? { guidanceDirection } : undefined  // 不传则查询所有
    })
}
```

### 2.2 业务逻辑缺陷

原始设计假设用户总是按专业方向筛选，但实际场景中：
- 首次打开综合意见管理时，应显示所有意见
- 用户可以选择性地按专业方向筛选

### 2.3 Mapper XML问题

**问题SQL (TopicGeneralOpinionMapper.xml)：**
```xml
WHERE guidance_direction = #{guidanceDirection}  -- ❌ 固定条件，不支持空值
```

## 三、修复方案

### 3.1 修改Controller层

```java
// 修改前
@RequestParam String guidanceDirection

// 修改后
@RequestParam(required = false) String guidanceDirection
```

### 3.2 修改Mapper XML

```xml
<!-- 修改前 -->
WHERE guidance_direction = #{guidanceDirection}
  AND deleted = 0

<!-- 修改后：使用动态条件 -->
WHERE deleted = 0
  <if test="guidanceDirection != null and guidanceDirection != ''">
      AND guidance_direction = #{guidanceDirection}
  </if>
```

## 四、修改文件清单

| 文件 | 修改内容 |
|---|---|
| `TopicReviewController.java` | `guidanceDirection`参数设为可选 |
| `TopicGeneralOpinionMapper.xml` | SQL改为动态条件查询 |

## 五、经验教训

### 5.1 设计原则

**列表查询接口的筛选参数应全部可选：**
- 筛选条件应允许为空，不传时返回全部数据
- 在SQL中使用MyBatis的`<if>`标签实现动态条件
- 这样可以支持灵活的筛选组合

### 5.2 MyBatis动态SQL最佳实践

```xml
<!-- 推荐写法：所有筛选条件使用动态条件 -->
<select id="selectByConditions" resultType="...">
    SELECT * FROM table_name
    WHERE deleted = 0
    <if test="param1 != null and param1 != ''">
        AND column1 = #{param1}
    </if>
    <if test="param2 != null">
        AND column2 = #{param2}
    </if>
</select>
```

### 5.3 开发检查清单

- [ ] 列表查询接口的筛选参数是否都设为可选？
- [ ] Mapper XML中是否使用了动态SQL？
- [ ] 不传筛选条件时，接口返回结果是否正确？

---

# 通用开发规范 — 避免API参数必传问题

## RequestParam参数设计规范

### 1. 何时设置 required = false

| 场景 | 是否必传 | 示例 |
|---|---|---|
| 分页参数 | 可选（有默认值） | `pageNum=1, pageSize=10` |
| 筛选/搜索条件 | 可选 | `keyword, status, category` |
| 查询当前用户数据 | 可选（默认当前用户） | `userId, teacherId` |
| 主键ID（详情/删除） | 必传 | `topicId, userId` |
| 业务必需字段 | 必传 | `审批结果, 操作类型` |

### 2. 代码模板

```java
// ✅ 正确：筛选参数可选
@GetMapping("/list")
public Result<List<Item>> getList(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer status,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize) {
    // ...
}

// ✅ 正确：查询自己数据时ID可选
@GetMapping("/my-stats")
public Result<StatsVO> getMyStats(
        @RequestParam(required = false) String userId) {
    if (userId == null || userId.isEmpty()) {
        userId = SecurityUtil.getCurrentUserId();
    }
    // ...
}
```

### 3. 前端调用注意事项

```typescript
// ✅ 正确：参数为空时不传递
getList(params?: QueryParams) {
    return request.get('/api/list', { 
        params: params || undefined 
    })
}

// ❌ 错误：传递空对象可能导致参数问题
getList(params: QueryParams) {
    return request.get('/api/list', { params })  // params可能是 {}
}
```

---

# 系统管理员创建专业方向报错问题

- 日期：2026-03-01
- 作者：系统维护者
- 严重程度：高（阻塞系统管理员创建专业方向功能）

## 一、问题描述

### 问题表现
系统管理员点击企业节点（如"IBM"）下的"添加专业方向"按钮时，填写表单后提交报错：**"系统管理员请指定企业ID"**

### 影响范围
- 系统管理员无法为指定企业创建专业方向
- 前端虽然通过组件props传递了企业ID，但后端未能接收
- 完全阻断专业方向管理功能

### 错误截图描述
弹窗显示红色错误提示："系统管理员请指定企业ID"，数据未能保存。

## 二、问题根源分析

### 2.1 数据流断层问题

**问题链路：**
```
1. 用户点击"IBM"企业 → MajorList传递enterpriseId给DirectionFormModal
2. DirectionFormModal接收props.enterpriseId（值正确）
3. 表单提交 → formState中没有enterpriseId字段
4. 前端发送请求 → 不包含enterpriseId参数
5. 后端MajorDirectionDTO → 没有enterpriseId字段接收
6. 后端addDirection() → 调用getCurrentUserEnterpriseId()
7. 方法检测到系统管理员 → 抛出异常："系统管理员请指定企业ID"
```

### 2.2 具体原因

#### 后端问题
**MajorDirectionDTO缺少enterpriseId字段：**
```java
// ❌ 问题代码：没有enterpriseId字段
@Data
@Schema(description = "专业方向表单参数")
public class MajorDirectionDTO implements Serializable {
    @NotBlank(message = "专业方向名称不能为空")
    private String directionName;
    private String directionCode;
    // ... 其他字段，但没有enterpriseId
}
```

**addDirection()方法逻辑问题：**
```java
// ❌ 问题代码：只从当前用户获取企业ID
@Override
public MajorDirectionVO addDirection(MajorDirectionDTO dto) {
    String enterpriseId = getCurrentUserEnterpriseId(); // 系统管理员会抛异常
    // ...
}
```

**getCurrentUserEnterpriseId()对管理员的限制：**
```java
private String getCurrentUserEnterpriseId() {
    // 系统管理员需要指定企业ID
    if (SecurityUtil.hasRole("SYSTEM_ADMIN")) {
        throw new BusinessException("系统管理员请指定企业ID"); // ❌ 直接抛异常
    }
    // ...
}
```

#### 前端问题
**DirectionFormModal表单状态缺少enterpriseId：**
```typescript
// ❌ 问题代码：formState没有enterpriseId字段
const formState = reactive<MajorDirectionDTO>({
  directionName: '',
  directionCode: '',
  leaderId: undefined,
  sortOrder: 0,
  description: ''
  // 缺少 enterpriseId 字段
})
```

虽然组件接收了`props.enterpriseId`，但提交时没有将其包含在表单数据中。

## 三、解决方案

### 3.1 后端DTO添加企业ID字段

**修改文件：** `MajorDirectionDTO.java`

```java
@Data
@Schema(description = "专业方向表单参数")
public class MajorDirectionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ✅ 新增：企业ID字段
    @Schema(description = "企业ID（系统管理员创建时必填）")
    private String enterpriseId;

    @NotBlank(message = "专业方向名称不能为空")
    @Schema(description = "专业方向名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String directionName;

    // ... 其他字段
}
```

### 3.2 后端创建逻辑优化

**修改文件：** `MajorServiceImpl.java`

```java
@Override
public MajorDirectionVO addDirection(MajorDirectionDTO dto) {
    // ✅ 优先使用DTO中的enterpriseId（系统管理员指定），否则从当前用户获取
    String enterpriseId = StringUtils.hasText(dto.getEnterpriseId()) 
            ? dto.getEnterpriseId() 
            : getCurrentUserEnterpriseId();

    // 检查专业方向名称是否重复
    if (isDirectionNameExists(enterpriseId, dto.getDirectionName(), null)) {
        throw new BusinessException("专业方向名称已存在");
    }

    // ... 后续逻辑保持不变
}
```

**修改说明：**
- 增加条件判断：如果DTO中有enterpriseId就使用，否则才调用getCurrentUserEnterpriseId()
- 这样系统管理员可以指定企业ID，企业负责人自动使用自己的企业ID

### 3.3 前端类型定义更新

**修改文件：** `src/types/major.ts`

```typescript
/**
 * 专业方向表单 DTO
 */
export interface MajorDirectionDTO {
  /** 企业ID（系统管理员创建时必填） */
  enterpriseId?: string  // ✅ 添加字段
  /** 方向名称 */
  directionName: string
  /** 方向代码 */
  directionCode?: string
  /** 描述 */
  description?: string
  /** 负责人ID */
  leaderId?: string
  /** 排序 */
  sortOrder?: number
}
```

### 3.4 前端表单状态初始化

**修改文件：** `DirectionFormModal.vue`

```typescript
// ✅ 表单数据包含enterpriseId
const formState = reactive<MajorDirectionDTO>({
  enterpriseId: undefined,  // 新增字段
  directionName: '',
  directionCode: '',
  leaderId: undefined,
  sortOrder: 0,
  description: ''
})

// ✅ 重置表单时设置企业ID
const resetForm = () => {
  formState.enterpriseId = props.enterpriseId || undefined  // 从props获取
  formState.directionName = ''
  formState.directionCode = ''
  formState.leaderId = undefined
  formState.sortOrder = 0
  formState.description = ''
  formRef.value?.clearValidate()
  
  // 新建模式：自动生成代码
  if (!isEdit.value && enterpriseCode.value) {
    formState.directionCode = generateDirectionCode()
  }
}

// ✅ 填充表单时包含企业ID
const fillFormData = (data: MajorDirectionVO) => {
  formState.enterpriseId = data.enterpriseId  // 保存企业ID
  formState.directionName = data.directionName
  formState.directionCode = data.directionCode || ''
  formState.leaderId = data.leaderId
  formState.sortOrder = data.sortOrder || 0
  formState.description = data.description || ''
}
```

## 四、验证步骤

### 4.1 后端验证
```bash
cd complete-backend
mvn compile -q
```
✅ 编译通过，无错误

### 4.2 前端验证
```bash
cd complete-frontend
npm run type-check
```
✅ 类型检查通过

### 4.3 功能验证
1. **系统管理员操作：**
   - 登录系统管理员账号
   - 进入"专业管理"页面
   - 点击"IBM"企业节点的"添加专业方向"按钮
   - 填写方向名称（如"软件工程"）
   - 提交表单
   - ✅ 预期：创建成功，数据正确关联到IBM企业

2. **企业负责人操作：**
   - 登录企业负责人账号
   - 点击"新建专业方向"按钮
   - 填写表单并提交
   - ✅ 预期：创建成功，自动关联到当前用户的企业

## 五、关键技术点

### 5.1 前后端数据传递链路

```
MajorList (点击企业节点)
  ↓ 传递 enterpriseId
DirectionFormModal (组件props)
  ↓ 初始化 formState.enterpriseId
表单提交
  ↓ POST /major/direction
后端 MajorDirectionDTO
  ↓ dto.getEnterpriseId()
MajorServiceImpl.addDirection()
  ↓ 使用enterpriseId创建方向
```

### 5.2 兼容性设计

解决方案采用了**优雅降级**策略：
- 系统管理员：必须指定enterpriseId（从前端传递）
- 企业负责人：可选，未指定时自动使用当前用户的企业ID
- 向后兼容：旧的getCurrentUserEnterpriseId()逻辑保留

## 六、经验总结

### 6.1 问题类型
- **数据流断层**：前端有数据但未传递给后端
- **DTO设计不完整**：缺少必要的业务字段
- **权限逻辑过于严格**：没有提供指定参数的途径

### 6.2 最佳实践
1. **DTO字段设计**：应包含所有可能需要的业务参数，使用`@Schema`标注是否必填
2. **权限控制**：提供多种获取方式（指定参数 > 当前用户信息 > 默认值）
3. **前端表单**：props传递的关键参数必须包含在formState中
4. **错误提示**：明确告知用户缺少什么，而不是直接拒绝

### 6.3 调试技巧
- 使用浏览器Network查看请求payload，确认参数是否发送
- 后端断点调试DTO对象，查看字段值
- 检查前端formState和后端DTO的字段映射关系

---

# 专业代码保存为空问题

- 日期：2026-03-01
- 作者：系统维护者
- 严重程度：中（数据完整性问题）

## 一、问题描述

### 问题表现
用户在"软件工程"专业方向下点击"添加专业"：
1. 弹窗显示"专业代码"字段标注为"自动生成"（禁用状态）
2. 填写专业名称后点击"确定"
3. 系统提示"添加成功"
4. 但数据库中`major_code`字段为空（NULL或空字符串）

### 影响范围
- 所有新创建的专业记录
- 专业代码字段数据缺失
- 影响后续基于代码的查询和统计

### 期望行为
专业代码应该自动生成格式：`{方向代码}M{4位随机数}`，例如：`DEMO001D1234M5678`

## 二、问题根源分析

### 2.1 时序竞态条件（Race Condition）

**问题执行流程：**
```
1. 弹窗打开 → watch 触发
2. loadDirectionList() 异步请求开始 ▶ [异步执行中...]
3. resetForm() 立即执行 → 设置 directionId = "软件工程的ID"
4. watch(directionId) 触发 → 尝试从 directionOptions 查找
5. ❌ directionOptions 还是空数组（异步请求未完成）
6. ❌ 找不到 directionCode，majorCode 保持为空字符串 ''
7. （稍后）loadDirectionList() 完成，但为时已晚
8. 用户点击提交 → majorCode = '' 被发送到后端并保存到数据库
```

### 2.2 代码问题详解

#### 问题代码1：loadDirectionList()
```typescript
// ❌ 问题代码：加载完成后没有触发代码生成
const loadDirectionList = async () => {
  directionLoading.value = true
  try {
    const response = await majorApi.getDirectionList()
    directionOptions.value = response.data.map((item: MajorDirectionVO) => ({
      value: item.directionId,
      label: item.directionName,
      directionCode: item.directionCode
    }))
    // 缺少：检查是否需要生成专业代码
  } catch (error) {
    console.error('加载专业方向列表失败:', error)
  } finally {
    directionLoading.value = false
  }
}
```

#### 问题代码2：watch执行顺序
```typescript
// ❌ 问题代码：先启动异步请求，再设置directionId
watch(
  () => [props.open, props.majorData],
  ([newOpen, newData]) => {
    if (newOpen) {
      loadDirectionList()  // 异步，立即返回
      if (newData) {
        fillFormData(newData as MajorVO)
      } else {
        resetForm()  // 设置directionId，触发另一个watch
      }
    }
  },
  { immediate: true }
)
```

#### 问题代码3：directionId的watch
```typescript
// ⚠️ 依赖directionOptions已加载，但不保证时序
watch(
  () => formState.directionId,
  (newDirectionId) => {
    if (!isEdit.value && newDirectionId) {
      const direction = directionOptions.value.find(opt => opt.value === newDirectionId)
      if (direction && direction.directionCode) {  // ❌ 找不到，因为数组还是空的
        currentDirectionCode.value = direction.directionCode
        formState.majorCode = generateMajorCode(direction.directionCode)
      }
    }
  }
)
```

### 2.3 时间线分析

```
T0: 弹窗打开
T1: watch触发 → loadDirectionList()开始（异步）
T2: resetForm()执行 → directionId = "xxx"
T3: watch(directionId)触发 → 尝试find(...) 
T4: directionOptions = [] （空数组）❌
T5: majorCode保持为 ''
T6: （500ms后）loadDirectionList()完成
T7: directionOptions = [完整数据] （但太晚了）
T8: 用户提交 → majorCode = '' 被保存
```

## 三、解决方案

### 3.1 增强loadDirectionList方法

**修改文件：** `MajorFormModal.vue`

```typescript
/**
 * 加载专业方向列表
 */
const loadDirectionList = async () => {
  directionLoading.value = true
  try {
    const response = await majorApi.getDirectionList()
    directionOptions.value = response.data.map((item: MajorDirectionVO) => ({
      value: item.directionId,
      label: item.directionName,
      directionCode: item.directionCode
    }))
    
    // ✅ 关键修复：加载完成后，如果已有选中的方向，生成专业代码
    if (!isEdit.value && formState.directionId) {
      const direction = directionOptions.value.find(opt => opt.value === formState.directionId)
      if (direction && direction.directionCode) {
        currentDirectionCode.value = direction.directionCode
        formState.majorCode = generateMajorCode(direction.directionCode)
        console.log('✅ 自动生成专业代码:', formState.majorCode)
      }
    }
  } catch (error) {
    console.error('加载专业方向列表失败:', error)
  } finally {
    directionLoading.value = false
  }
}
```

### 3.2 调整watch执行顺序

```typescript
/**
 * 监听弹窗开关和数据变化
 */
watch(
  () => [props.open, props.majorData],
  async ([newOpen, newData]) => {  // ✅ 改为 async 函数
    if (newOpen) {
      // ✅ 先设置表单数据（包括directionId）
      if (newData) {
        fillFormData(newData as MajorVO)
      } else {
        resetForm()  // 这里会设置 directionId
      }
      // ✅ 然后等待加载完成（它会自动检查并生成代码）
      await loadDirectionList()
    }
  },
  { immediate: true }
)
```

### 3.3 修复后的执行流程

```
T0: 弹窗打开
T1: watch触发（async函数）
T2: resetForm()执行 → directionId = "软件工程ID"
T3: await loadDirectionList() 开始
T4: 异步请求发送...
T5: 异步请求返回 → directionOptions = [完整数据] ✅
T6: 检测到 formState.directionId 有值
T7: 从 directionOptions 找到对应的 directionCode ✅
T8: 生成 majorCode = "DEMO001D1234M5678" ✅
T9: watch继续执行（但已完成核心逻辑）
T10: 用户看到自动生成的代码
T11: 用户提交 → majorCode 正确保存到数据库 ✅
```

## 四、验证步骤

### 4.1 代码验证
```bash
cd complete-frontend
npm run type-check
```
✅ TypeScript类型检查通过

### 4.2 功能验证步骤

1. **清空测试数据**
   ```sql
   DELETE FROM major WHERE major_name = '测试专业';
   ```

2. **创建专业**
   - 登录系统管理员账号
   - 进入"专业管理"页面
   - 点击"软件工程"方向的"添加专业"按钮
   - 填写专业名称："测试专业"
   - 观察"专业代码"字段：应显示类似 `DEMO001D1234M5678` 的代码
   - 点击"确定"提交

3. **验证数据库**
   ```sql
   SELECT major_id, major_name, major_code, direction_id
   FROM major
   WHERE major_name = '测试专业';
   ```
   ✅ 预期结果：`major_code` 字段有值，格式正确

4. **多次测试**
   - 创建多个专业，确保每次代码都不同（随机数部分）
   - 编辑专业时，代码应保持不变（只读）

## 五、关键技术点

### 5.1 异步编程最佳实践

**问题模式（避免）：**
```typescript
// ❌ 错误：启动异步任务后立即执行依赖它的代码
async function badPattern() {
  loadData()  // 异步，立即返回
  useData()   // ❌ 数据可能还未加载
}
```

**正确模式：**
```typescript
// ✅ 正确：等待异步任务完成
async function goodPattern() {
  await loadData()  // 等待完成
  useData()         // ✅ 数据已就绪
}
```

### 5.2 Vue3 Watch的异步处理

```typescript
// ✅ watch回调可以是async函数
watch(
  () => someRef.value,
  async (newValue) => {
    await doSomethingAsync()
    // 继续处理
  }
)
```

### 5.3 数据加载后的二次处理模式

```typescript
const loadData = async () => {
  const data = await fetchData()
  // ✅ 关键：加载完成后立即处理依赖该数据的逻辑
  if (needsProcessing) {
    processData(data)
  }
}
```

## 六、经验总结

### 6.1 问题类型识别

**时序竞态条件的典型特征：**
- ✓ 某些时候工作正常（异步任务快速完成时）
- ✓ 某些时候失败（网络慢或数据量大时）
- ✓ 难以复现（取决于异步任务完成时间）
- ✓ 调试时问题消失（断点延迟了执行）

### 6.2 调试技巧

1. **添加时间戳日志**
   ```typescript
   console.log('[T0] 弹窗打开:', Date.now())
   console.log('[T1] 开始加载:', Date.now())
   console.log('[T2] 加载完成:', Date.now(), directionOptions.value.length)
   console.log('[T3] 生成代码:', Date.now(), formState.majorCode)
   ```

2. **检查数据状态**
   ```typescript
   console.log('directionOptions:', directionOptions.value)
   console.log('formState.directionId:', formState.directionId)
   console.log('computed majorCode:', formState.majorCode)
   ```

3. **使用Vue DevTools**
   - 查看组件props和state的实时值
   - 追踪watch的触发时机和顺序

### 6.3 预防措施

1. **异步依赖明确化**：确保依赖异步数据的代码使用`await`
2. **数据加载后处理**：在异步加载完成后立即检查并处理相关逻辑
3. **watch顺序控制**：使用`async/await`控制watch回调中的执行顺序
4. **防御性编程**：检查数组不为空再进行查找操作

### 6.4 测试建议

1. **慢速网络测试**：使用Chrome DevTools限速，模拟慢速网络
2. **延迟注入**：人为添加延迟，验证时序处理是否正确
   ```typescript
   await new Promise(resolve => setTimeout(resolve, 1000))
   ```
3. **并发测试**：快速打开关闭弹窗，测试并发场景

---

# 覆盖检查误报未覆盖 — MyBatis-Plus eq(null)生成IS NULL条件

- 日期：2026-03-08
- 作者：系统维护者
- 严重程度：高（数据展示完全错误，直接影响业务判断）
- 状态：已解决

## 一、问题描述

### 问题表现

企业教师"王三泉"已在**精确配对**页签中与高校老师完成配对（状态：启用），但在**覆盖检查**页签中仍显示为"未覆盖"，高校教师栏显示 `-`。

### 影响范围

- 覆盖检查页面（`/teacher-relation` → 覆盖检查 Tab）
- 覆盖率统计数字（已覆盖 / 未覆盖 count）
- 不传届别时所有精确配对的企业教师均被误判为未覆盖

## 二、问题根源分析

### 2.1 核心代码位置

`TeacherRelationServiceImpl.java` → 私有方法 `findDirectPair`：

```java
// ❌ 修复前
private TeacherRelationship findDirectPair(String enterpriseTeacherId, String cohort) {
    LambdaQueryWrapper<TeacherRelationship> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(TeacherRelationship::getEnterpriseTeacherId, enterpriseTeacherId)
            .eq(TeacherRelationship::getCohort, cohort)          // ← 问题所在
            .eq(TeacherRelationship::getRelationType, "DIRECT")
            .eq(TeacherRelationship::getIsEnabled, 1);
    return teacherRelationshipMapper.selectOne(wrapper);
}
```

### 2.2 MyBatis-Plus eq(null) 行为

MyBatis-Plus 的 `eq(column, value)` 是**无条件**等值匹配：

| value 值 | 生成的 SQL 片段 |
|----------|----------------|
| `"2026届"` | `AND cohort = '2026届'` ✅ |
| `null` | `AND cohort IS NULL` ❌ |
| `""` (空串) | `AND cohort = ''` ❌ |

当覆盖检查页面**不填届别**时，`cohort` 传入为 `null`，MyBatis-Plus 将其翻译为 `cohort IS NULL`，而数据库中王三泉的配对记录 `cohort = '2026届'`（非 NULL），因此查询结果为空，`findUniversityTeacher` 误判为无配对，覆盖状态被错误设为"未覆盖"。

### 2.3 调用链路

```
getCoverageList(enterpriseId, cohort=null)
  └─ findUniversityTeacher(teacherId, directionId, cohort=null)
       └─ findDirectPair(teacherId, cohort=null)
            └─ SQL: WHERE enterprise_teacher_id=? AND cohort IS NULL AND ...
               → 返回 null（找不到记录）
  └─ univTeacherId = null → coverageVO.setCovered(false) ← 误判!
```

## 三、解决方案

使用 MyBatis-Plus 的**条件三参数写法** `eq(condition, column, value)`，仅当 `cohort` 有值时才加入该过滤条件：

```java
// ✅ 修复后
private TeacherRelationship findDirectPair(String enterpriseTeacherId, String cohort) {
    LambdaQueryWrapper<TeacherRelationship> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(TeacherRelationship::getEnterpriseTeacherId, enterpriseTeacherId)
            .eq(StringUtils.hasText(cohort), TeacherRelationship::getCohort, cohort)
            .eq(TeacherRelationship::getRelationType, "DIRECT")
            .eq(TeacherRelationship::getIsEnabled, 1)
            .last("LIMIT 1");  // 防止多届配对存在时 selectOne 报 TooManyResultsException
    return teacherRelationshipMapper.selectOne(wrapper);
}
```

**修复效果对比：**

| 调用场景 | 修复前 SQL | 修复后 SQL |
|----------|-----------|------------|
| 不传届别 | `cohort IS NULL`（查不到） | 不含 cohort 条件（能查到任意届的配对） |
| 传 `2026届` | `cohort = '2026届'`（正常） | `cohort = '2026届'`（行为不变） |

## 四、修改的文件

- `complete-backend/.../service/impl/TeacherRelationServiceImpl.java`
  - 方法：`findDirectPair`
  - 改 `.eq(CohortField, cohort)` → `.eq(StringUtils.hasText(cohort), CohortField, cohort)` + `.last("LIMIT 1")`

## 五、经验总结

### ⚠️ MyBatis-Plus 使用规范

> **所有可为 null 的查询参数，必须使用条件三参数写法，不得直接 `.eq(column, nullableValue)`**

```java
// ❌ 错误写法 —— value 为 null 时生成 IS NULL
wrapper.eq(Entity::getField, value);

// ✅ 正确写法 —— value 为 null / 空时跳过该条件
wrapper.eq(StringUtils.hasText(value), Entity::getField, value);  // String 类型
wrapper.eq(value != null, Entity::getField, value);               // 任意类型
```

### 规范适用场景

- 所有带 `@RequestParam(required = false)` 的可选参数透传至 QueryWrapper 的场景
- 服务间调用时参数可能为 null 的场景
- 覆盖检查、统计查询等"不传则查全部"的业务逻辑

---

# 专业管理编辑弹窗企业老师下拉框显示跨企业教师问题

- 日期：2026-03-08
- 作者：系统维护者
- 严重程度：中（数据隔离安全问题，同一教师可被多个企业同时选择）
- 状态：已解决

## 一、问题描述

### 问题表现

展开不同企业的专业进行编辑时，两个企业的专业都可以选择同一名企业老师，导致同一教师同时属于多个企业的错误数据状态。

### 影响范围

- 专业管理 — 编辑专业弹窗中的企业老师多选下拉框
- 企业A的教师可在企业B的选择列表中出现，反之亦然

## 二、问题根源分析

### 2.1 前端：弹窗缺少 `enterpriseId` prop，API 永远不传企业参数

**`MajorFormModal.vue`** 没有 `enterpriseId` prop，`fetchTeachers` 调用 API 时不传 `enterpriseId`，导致后端永远返回全库教师。

### 2.2 前端：`MajorList.vue` 未更新 `currentEnterpriseId`

- `handleEditMajor`：获取专业详情后没有转存 `response.data.enterpriseId` 到 `currentEnterpriseId`
- `handleAddMajorToDirection`：新建专业时未通过方向ID反查父级企业ID
- 模板中 `MajorFormModal` 缺少 `:enterprise-id="currentEnterpriseId"` 绑定

### 2.3 后端：`searchMajorTeachers` 未进行企业隔离

后端在 `enterpriseId` 为空时直接返回所有企业的全局教师列表。

## 三、解决方案

### 3.1 前端 — `MajorFormModal.vue`

新增 `enterpriseId?: string` prop，`fetchTeachers` 调用时将其传入 `majorApi.searchTeachers(keyword, props.enterpriseId)`。

### 3.2 前端 — `MajorList.vue`

- `handleEditMajor`：获取详情后立即赋值 `currentEnterpriseId.value = response.data.enterpriseId`
- `handleAddMajorToDirection`：新增辅助函数 `findEnterpriseIdForDirection`，通过遍历树节点从方向ID反查父企业ID
- 模板绑定 `:enterprise-id="currentEnterpriseId"`

### 3.3 后端 — `MajorServiceImpl.java`

当 `enterpriseId` 有值时，按 `major_teacher → major.enterprise_id` 关系链过滤：排除已被其他企业占用的教师。

```java
// 第一步：取得「其他企业」的所有专业ID
List<String> otherEnterpriseMajorIds = majorMapper.selectList(
    new LambdaQueryWrapper<Major>().ne(Major::getEnterpriseId, enterpriseId)
        .select(Major::getMajorId))
    .stream().map(Major::getMajorId).collect(Collectors.toList());

// 第二步：找出已被其他企业占用的教师ID
List<String> occupied = majorTeacherMapper.selectList(
    new LambdaQueryWrapper<MajorTeacher>()
        .in(MajorTeacher::getMajorId, otherEnterpriseMajorIds)
        .select(MajorTeacher::getUserId))
    .stream().map(MajorTeacher::getUserId).distinct().collect(Collectors.toList());

// 第三步：排除这些教师
if (!occupied.isEmpty()) {
    userWrapper.notIn(User::getUserId, occupied);
}
```

## 四、业务规则

| 教师状态 | 含义 | 可见范围 |
|----------|------|---------|
| `major_teacher` 中无记录 | 未被任何企业占用 | 对所有企业可见 |
| 记录指向企业X的专业 | 属于企业X | 只对X可见 |
| 被其他企业Y占用 | 已与Y绑定 | 对X不可见 |
| 从X的专业中移除后 | 物理删除记录 | 重新对所有企业可见 ✅ |

## 五、修改的文件

- `complete-frontend/.../components/major/MajorFormModal.vue`
  - 新增 `enterpriseId` prop，`fetchTeachers` 调用时传入该参数
- `complete-frontend/.../views/major/MajorList.vue`
  - `handleEditMajor`、`handleAddMajorToDirection` 正确设置 `currentEnterpriseId`
  - 模板绑定 `:enterprise-id` 到 `MajorFormModal`
- `complete-backend/.../service/impl/MajorServiceImpl.java`
  - `searchMajorTeachers` 增加三步排除逻辑

---

# 企业老师过滤错误导致下拉框为空 — department字段过滤错误

- 日期：2026-03-08
- 作者：系统维护者
- 严重程度：高（企业老师下拉框完全为空，无法选择任何教师）
- 状态：已解决

## 一、问题描述

### 问题表现

在修复条目18（企业老师跨企业显示问题）时，后端 `searchMajorTeachers`方法被改为通过 `User.department == enterprise.enterpriseName` 进行过滤。修改后所有企业的编辑/新建专业模态框中，企业老师下拉框变为空，显示"未找到匹配的企业老师"。

### 影响范围

- 专业管理所有编辑/新建专业模态框的企业老师选择功能全部失效
- 两个企业均无法选取任何教师

## 二、问题根源分析

### 2.1 错误过滤方式

`User.department` 是用户注册时的文本字段，与企业主数据没有强关联：

```java
// ❌ 错误写法：用 department 匹配企业名称
Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
if (enterprise != null) {
    userWrapper.eq(User::getDepartment, enterprise.getEnterpriseName());
}
```

问题原因：
- `department` 内容由用户自行填写，存在大小写、全半角、多余空格、简写等差异
- 测试数据中教师的 `department` 字段值与企业的正式名称不一致
- 结果：`WHERE department = '企业名'` 匹配 0 条记录 → 空列表

## 三、解决方案

**完全放弃基于 `department` 字段的过滤，改为基于 `major_teacher → major.enterprise_id` 关系链的排除逻辑。**

正确的教师归属判断依据：
- 不靠 `User.department`（文本字段，不可靠）
- 靠 `major_teacher.major_id` 关联 `major.enterprise_id`（业务关系表，可靠）

详细实现见条目18第三节后端修改。

## 四、经验总结

### ⚠️ 禁止用文本字段进行实体关联处理

> `User.department`、`User.company` 等第三字段文本字段仅供展示用途，**不得用于实体过滤、归属判断等业务逻辑**。

正确做法：所有实体关联判断必须基于关系表的 ID 字段进行查询。

| 场景 | 错误做法 | 正确做法 |
|------|---------|----------|
| 教师属于哪个企业 | `WHERE department = '...'` | `JOIN major_teacher ON major.enterprise_id` |
| 用户属于哪个部门 | `WHERE department = '...'` | `WHERE department_id = 'xxx'`（ID字段） |

## 五、修改的文件

- `complete-backend/.../service/impl/MajorServiceImpl.java`
  - 方法：`searchMajorTeachers`
  - 删除 `department` 字段过滤逻辑，改为 `major_teacher → major.enterprise_id` 三步排除逻辑

---

# 企业负责人双选审核 TooManyResultsException 异常

> **问题类型：** BUG 修复  
> **问题日期：** 2026-03-14  
> **影响范围：** 企业负责人角色登录系统后，访问"双选管理 → 双选审核"功能时报错，无法正常开展课题审核工作

## 一、问题描述

### 用户操作场景
1. 使用企业负责人账号（`TEST_EL_001` / `李企业负责人`）登录系统
2. 点击左侧菜单 "双选管理 → 双选审核"
3. 页面加载失败，前端提示错误信息

### 后端报错信息
```
org.apache.ibatis.exceptions.TooManyResultsException: Expected one result (or null) to be returned by selectOne(), but found: 2
	at org.apache.ibatis.session.defaults.DefaultSqlSession.selectOne(DefaultSqlSession.java:83)
	at com.baomidou.mybatisplus.core.override.MybatisMapperProxy.invoke(MybatisMapperProxy.java:67)
	...
	at com.yuwan.completebackend.service.impl.TopicSelectionServiceImpl.resolveCurrentUserEnterpriseId(TopicSelectionServiceImpl.java:578)
	at com.yuwan.completebackend.service.impl.TopicSelectionServiceImpl.getReviewStatistics(TopicSelectionServiceImpl.java:98)
```

### 前端表现
- 双选审核页面无法加载统计信息
- 控制台显示 500 错误
- 用户无法进行任何审核操作

## 二、问题分析

### 1. 错误定位
查看 `TopicSelectionServiceImpl.java` 第 578 行代码：

```java
// 路径 4：enterprise_info.leader_id（企业负责人专用）
Enterprise enterprise = enterpriseMapper.selectOne(
        new LambdaQueryWrapper<Enterprise>()
                .eq(Enterprise::getLeaderId, userId)
);
```

### 2. 根本原因
**同一个用户被设置为多个企业的负责人**，导致查询条件 `leader_id = userId` 返回多条记录：
- MyBatis-Plus 的 `selectOne()` 方法期望返回 0 或 1 条结果
- 当返回 2 条及以上时，抛出 `TooManyResultsException`

### 3. 业务场景还原
在系统测试或实际使用中，可能存在以下情况：
- 管理员在用户管理中创建了企业负责人用户
- 在创建企业时，将该负责人同时设为多个企业的 `leader_id`
- 或者通过多次编辑企业，将同一用户设为不同企业的负责人

### 4. 问题影响
- 企业负责人无法访问双选审核功能
- 影响企业方对课题选报过程的审核流程
- 降低系统可用性和用户体验

## 三、解决方案

### 方案选择
在 `selectOne()` 查询中添加 `.last("LIMIT 1")` 限制，确保只返回一条记录。

**理由：**
- 简单直接，最小化代码改动
- 符合业务逻辑：企业负责人通常只关联一个主要企业
- 即使关联多个企业，取第一个也满足审核需求
- 避免修改整体架构和查询逻辑

### 详细实现

**文件：** `complete-backend/src/main/java/com/yuwan/completebackend/service/impl/TopicSelectionServiceImpl.java`

**修改位置：** `resolveCurrentUserEnterpriseId()` 方法，第 575-580 行

```java
// 路径 4：enterprise_info.leader_id（企业负责人专用）
Enterprise enterprise = enterpriseMapper.selectOne(
        new LambdaQueryWrapper<Enterprise>()
                .eq(Enterprise::getLeaderId, userId)
                .last("LIMIT 1")  // 修复：防止同一用户关联多企业时 TooManyResultsException
);
if (enterprise != null) {
    return enterprise.getEnterpriseId();
}
```

### 其他备选方案（未采用）

| 方案 | 优点 | 缺点 |
|------|------|------|
| 使用 `list()` 后取第一条 | 灵活，可处理多条 | 代码冗余，性能略低 |
| 添加数据库唯一约束 | 从源头防止重复 | 可能影响现有数据，需要数据清洗 |
| 修改为 `selectList()` + 人工校验 | 可控性强 | 代码复杂，增加维护成本 |

## 四、验证结果

### 测试场景
1. **单企业负责人**：用户只关联一个企业 → ✅ 正常返回
2. **多企业负责人**：用户关联两个企业 → ✅ 返回第一个企业，不再报错
3. **非企业负责人**：用户未关联任何企业 → ✅ 返回 null，继续后续逻辑

### 功能验证
- 企业负责人可正常访问双选审核页面
- 统计信息正确显示
- 审核流程可正常执行
- 日志中无异常堆栈

## 五、经验总结

### ⚠️ MyBatis-Plus `selectOne()` 潜在风险

> 当查询条件不保证唯一性时，`selectOne()` 可能抛出 `TooManyResultsException`。

**最佳实践：**
- 如果业务上允许存在多条记录，务必添加 `.last("LIMIT 1")` 
- 或者改用 `selectList()` 手动处理结果
- 对于可能重复的关联关系（如一对多），优先使用 `list()` 而非 `one()`

### 数据库设计建议
- 对于 `leader_id` 这类外键字段，考虑是否需要添加唯一索引
- 如果允许多个企业共享同一负责人，应在应用层做好结果集处理

## 六、相关文件

- `complete-backend/src/main/java/com/yuwan/completebackend/service/impl/TopicSelectionServiceImpl.java`
  - 方法：`resolveCurrentUserEnterpriseId()`
  - 修改：添加 `.last("LIMIT 1")`

---

# 学生选报课题无法查看任务书详情

> **问题类型：** 功能优化  
> **问题日期：** 2026-03-14  
> **影响范围：** 学生在"课题选报"页面只能看到课题简介，无法查看完整的任务书格式内容

## 一、问题描述

### 用户反馈
学生在"双选管理 → 课题选报"页面浏览可选课题时：
- 鼠标悬停在课题名称上时，只能通过 Tooltip 看到简短的 `contentSummary`（内容简述）
- 没有入口可以查看完整的任务书内容（选题背景、任务要求、参考文献等）
- 无法打印任务书进行线下审阅

### 当前表现
- 课题列表仅展示基本信息（名称、类型、企业、教师等）
- 缺少"详情"按钮或链接
- 无法以标准格式预览任务书

### 业务需求
根据毕业设计全过程管理系统的设计规范：
- 学生在选报前应能完整审阅课题任务书
- 任务书应包含：选题背景与意义、课题内容简述、专业知识综合训练、开发环境、工作量、任务与进度要求、主要参考文献等
- 应支持打印功能，便于学生线下阅读和确认

## 二、问题分析

### 技术层面
1. **前端组件缺失**
   - `TopicSelectionList.vue` 只有简单的表格展示
   - 课题名称使用 `<span>` 标签，无点击交互
   - 没有任务书详情查看组件（抽屉、弹窗或独立页面）

2. **API 接口已就绪**
   - 后端已有 `/topic/{topicId}` 接口（`getTopicDetail`）
   - 返回 `TopicVO` 包含完整的任务书字段
   - 前端已有 `TopicDetail.vue` 组件（用于教师端查看）

3. **数据结构差异**
   - 列表接口返回 `TopicForSelectionVO`（简化版，仅含基本信息）
   - 详情接口返回 `TopicVO`（完整版，含所有任务书字段）

### 用户体验问题
- 学生无法全面了解课题要求
- 降低选报决策的科学性
- 不符合"审慎选报"的业务流程设计

## 三、解决方案

### 方案设计
在前端 `TopicSelectionList.vue` 中新增任务书详情抽屉，提供两种触发方式：
1. **点击课题名称**：蓝色超链接样式，点击打开右侧抽屉
2. **操作列"详情"按钮**：独立的详情按钮，与"选报"按钮并列

### 详细实现

#### 1. 前端模板修改

**文件：** `complete-frontend/src/views/topic-selection/TopicSelectionList.vue`

**修改位置：** Template 部分

**(1) 添加任务书详情抽屉组件**
```vue
<!-- 任务书详情抽屉 -->
<a-drawer
  v-model:open="detailDrawerVisible"
  title="课题任务书"
  placement="right"
  :width="760"
  :body-style="{ padding: '20px' }"
>
  <div v-if="detailLoading" style="text-align:center;padding:60px 0">
    <a-spin size="large" tip="加载中..." />
  </div>
  <template v-else-if="detailData">
    <!-- 打印按钮 -->
    <div style="margin-bottom:16px;text-align:right">
      <a-button @click="handleDetailPrint">
        <template #icon><PrinterOutlined /></template>
        打印
      </a-button>
    </div>

    <!-- 任务书内容 -->
    <div id="task-book-print-area">
      <div class="task-book-title">毕业设计（论文）任务书</div>
      <table class="task-book-table" cellpadding="0" cellspacing="0">
        <!-- 题目、基本信息、各章节内容... -->
      </table>

      <!-- 签名栏 -->
      <div class="task-book-signature">
        <div class="sig-item">
          <span class="sig-label">学院负责人</span>
          <span class="sig-line"></span>
        </div>
        <!-- ... -->
      </div>
    </div>
  </template>
</a-drawer>
```

**(2) 修改课题名称列渲染**
```vue
<!-- 课题名称 -->
<template v-if="column.dataIndex === 'topicTitle'">
  <a class="topic-title" @click="openDetailDrawer(record.topicId)">
    {{ record.topicTitle }}
  </a>
</template>
```

**(3) 修改操作列，添加"详情"按钮**
```vue
<!-- 操作 -->
<template v-if="column.dataIndex === 'action'">
  <a-space>
    <a-button
      type="link"
      size="small"
      @click="openDetailDrawer(record.topicId)"
    >
      详情
    </a-button>
    <a-button
      type="link"
      size="small"
      :disabled="record.alreadyApplied || hasSelected || activeCount >= 3"
      @click="openApplyModal(record)"
    >
      {{ record.alreadyApplied ? '已选报' : '选报' }}
    </a-button>
  </a-space>
</template>
```

#### 2. 前端脚本修改

**(1) 导入依赖**
```typescript
import { PrinterOutlined } from '@ant-design/icons-vue'
import { topicApi } from '@/api/topic'
import type { TopicVO } from '@/types/topic'
```

**(2) 新增状态定义**
```typescript
// 任务书详情抽屉
const detailDrawerVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<TopicVO | null>(null)
```

**(3) 新增事件处理函数**
```typescript
/** 打开任务书详情抽屉 */
const openDetailDrawer = async (topicId: string) => {
  detailDrawerVisible.value = true
  detailData.value = null
  detailLoading.value = true
  try {
    const result = await topicApi.getTopicDetail(topicId)
    detailData.value = result.data
  } catch (error) {
    console.error('获取课题详情失败:', error)
    message.error('获取课题详情失败')
  } finally {
    detailLoading.value = false
  }
}

/** 打印任务书 */
const handleDetailPrint = () => {
  const el = document.getElementById('task-book-print-area')
  if (!el) return
  const win = window.open('', '_blank')
  if (!win) return
  win.document.write(`
    <html><head><title>课题任务书</title>
    <style>...打印样式...</style>
    </head><body>${el.innerHTML}</body></html>
  `)
  win.document.close()
  win.focus()
  win.print()
  win.close()
}
```

**(4) 新增计算属性（解析富文本字段）**
```typescript
const parsedDevelopmentEnvironment = computed(() => {
  const data = detailData.value?.developmentEnvironment
  if (!data) return '-'
  if (typeof data === 'string') return data || '-'
  if ((data as any).content) return (data as any).content
  return '-'
})

// 类似处理 workloadDetail, scheduleRequirements, topicReferences
```

#### 3. 样式补充

```scss
/* 任务书样式 */
.task-book-title {
  font-size: 20px;
  font-weight: bold;
  text-align: center;
  padding: 16px 0 20px;
  color: #000;
}

.task-book-table {
  width: 100%;
  border-collapse: collapse;
  border: 2px solid #000;
  margin-bottom: 20px;
  table-layout: fixed;
}

.task-book-table td {
  border: 1px solid #000;
  padding: 10px 12px;
  vertical-align: top;
  font-size: 14px;
  line-height: 1.8;
}

.tbl-label {
  background-color: #fafafa;
  font-weight: 500;
  text-align: center;
  width: 100px;
  vertical-align: middle !important;
}
```

#### 4. 附加修复

**文件：** `complete-frontend/src/api/auth.ts`

**问题：** `UserInfo` 接口缺少 `majorId` 字段，导致 TypeScript 编译报错

**修复：**
```typescript
export interface UserInfo {
  // ... existing fields ...
  major?: string
  majorId?: string  // 新增
  studentNo?: string
  // ...
}
```

## 四、功能效果

### 用户操作流程
1. 学生访问"课题选报"页面
2. **方式一**：点击蓝色课题名称链接
3. **方式二**：点击操作列"详情"按钮
4. 右侧滑出抽屉，展示完整任务书
5. 可点击"打印"按钮打印任务书

### 任务书内容展示
- ✅ 题目、课题类型/来源、指导方向、归属企业、指导教师
- ✅ 选题背景与意义
- ✅ 课题内容简述
- ✅ 专业知识综合训练
- ✅ 开发环境（工具）
- ✅ 工作量（预计周数）
- ✅ 任务与进度要求
- ✅ 主要参考文献
- ✅ 起止日期、备注
- ✅ 签名栏（学院负责人、企业负责人、企业指导教师）

### 交互体验
- 抽屉宽度 760px，适配桌面端阅读
- 支持滚动查看所有内容
- 打印功能独立窗口，不影响当前页面
- 加载状态有 Spin 提示
- 错误状态有 Message 提示

## 五、技术亮点

### 1. 组件复用
- 复用后端已有的 `/topic/{topicId}` 接口
- 参考 `TopicDetail.vue` 的展示逻辑
- 保持与教师端一致的任务书格式

### 2. 打印功能
- 使用 `window.open()` 新开窗口
- 动态生成 HTML 文档
- 内联打印样式，确保格式统一
- 调用 `window.print()` 浏览器原生打印

### 3. 富文本字段解析
- 兼容字符串和对象两种格式
- 提取 `content` 属性进行展示
- 使用 `computed` 自动响应式更新

### 4. 样式隔离
- 使用 `scoped`  scoped 样式
- 任务书表格独立命名空间
- 打印样式特殊处理

## 六、修改的文件

- `complete-frontend/src/views/topic-selection/TopicSelectionList.vue`
  - 新增：任务书详情抽屉组件
  - 修改：课题名称列渲染（`<span>` → `<a>`）
  - 修改：操作列（新增"详情"按钮）
  - 新增：`openDetailDrawer`、`handleDetailPrint` 等方法
  - 新增：`parsedDevelopmentEnvironment` 等计算属性
  - 新增：任务书相关样式

- `complete-frontend/src/api/auth.ts`
  - 修改：`UserInfo` 接口新增 `majorId?: string` 字段

## 七、经验总结

### 📋 列表页查看详情是常见需求
> 在列表页提供快捷入口查看完整详情，是提升用户体验的重要手段。

**实现方式对比：**

| 方式 | 适用场景 | 优点 | 缺点 |
|------|---------|------|------|
| 抽屉（Drawer） | 中等复杂度内容 | 不离开当前页，上下文保持 | 空间有限 |
| 弹窗（Modal） | 简单内容 | 快速查看 | 不适合大段文本 |
| 独立页面 | 复杂内容 | 空间充足，可打印 | 需要路由跳转 |
| Tooltip | 极简信息 | 轻量 | 无法展示结构化内容 |

本次选择抽屉方案，平衡了**查看体验**和**操作效率**。

### 🖨️ 打印功能的正确实现
- 使用新窗口打印，避免污染原页面
- 内联完整 HTML 和 CSS，确保打印效果
- 调用浏览器原生 `print()` API

### 🔧 细节决定成败
- 课题名称从 `<span>` 改为 `<a>`，提升可点击感
- 操作列使用 `a-space` 布局，按钮间距统一
- 加载状态、错误提示完备
- 打印按钮放在右上角，符合用户习惯

## 八、后续优化建议

1. **课题名称高亮**：已选报的课题可使用不同颜色区分
2. **批量打印**：支持一次性打印多个课题任务书
3. **下载 PDF**：提供任务书 PDF 下载功能
4. **收藏功能**：学生可收藏感兴趣的课题
5. **对比功能**：支持同时查看多个课题进行对比

---

# 指导记录管理模块 - 问题记录

## 问题一：MySQL 不支持 `NULLS LAST` 语法

**发生位置**：`GuidanceRecordMapper.xml` - `selectStudentsByTeacher` 查询

**错误信息**：
```
SQLSyntaxErrorException: You have an error in your SQL syntax; 
check the manual ... near 'NULLS LAST, stu.real_name ASC'
```

**问题原因**：
`NULLS LAST` 是 PostgreSQL / Oracle 的专有语法，MySQL 不支持该写法。

**错误写法**：
```sql
ORDER BY stat.last_date DESC NULLS LAST, stu.real_name ASC
```

**正确写法**：
```sql
ORDER BY ISNULL(stat.last_date), stat.last_date DESC, stu.real_name ASC
```

**解决原理**：`ISNULL(col)` 在值为 NULL 时返回 1，非 NULL 时返回 0。升序排列后，0（有值）排前，1（NULL）排后，等效于 `NULLS LAST`。

---

## 问题二：前端 `less` 预处理器缺失导致页面白屏

**发生位置**：`LeaderGuidanceOverview.vue` 使用了 `<style scoped lang="less">`

**错误信息**：
```
[plugin:vite:css] Preprocessor dependency "less" not found. 
Did you install it? Try `npm install -D less`.
```

**问题原因**：
Vue 文件中使用了 `lang="less"` 的 scoped 样式，但项目未安装 less 编译依赖。

**解决方案**：
```bash
npm install -D less
```

---

## 问题三：前端 API 返回值未取 `.data` 字段导致 loading 一直转圈

**发生位置**：`TeacherGuidanceList.vue` - `loadStudentList` / `handleViewRecords` / `handleDeleteRecord`

**问题原因**：
`request.get()` 封装返回的是 `ApiResponse<T>` 对象（`{code, message, data}`），而代码直接将整个响应赋值给了数组类型的 ref：
```javascript
// 错误写法
studentList.value = await guidanceApi.getMyStudents()
```
导致 `studentList` 变成对象而非数组，`studentList.length` 为 undefined，空状态无法显示，a-table 渲染异常。

**正确写法**：
```javascript
const res = await guidanceApi.getMyStudents()
studentList.value = res.data || []
```

**修复原则**：所有通过封装 `request` 发出的请求，取数组/对象数据时必须访问 `.data` 属性，并提供默认值防止 null。

---

## 问题四：指导日期选择后仍提示"请选择指导日期"

**发生位置**：`GuidanceFormModal.vue` - 指导日期表单项

**问题原因**：
`a-date-picker` 绑定的是独立的 `guidanceDateValue`（Dayjs 对象），而表单校验规则绑定的是 `formData.guidanceDate`（字符串）。两者是两个独立变量，选择日期后 `formData.guidanceDate` 不会自动更新，导致表单校验始终失败。

**解决方案**：
在 `a-date-picker` 添加 `@change` 事件，手动同步：
```javascript
const handleDateChange = (date: Dayjs | null) => {
  formData.guidanceDate = date ? date.format('YYYY-MM-DD') : ''
  formRef.value?.validateFields(['guidanceDate'])
}
```

**规律总结**：当 `a-date-picker` 的 v-model 绑定的是 Dayjs 对象，而表单 `:model` 中对应字段是字符串时，必须通过 `@change` 手动同步。

---

## 问题五：企业教师用户 `enterprise_id` 为 null 导致指导记录总览报错

**发生位置**：`GuidanceRecordServiceImpl.getLeaderGuidanceOverview`

**错误信息**：
```
业务异常: 未找到您所属的企业信息
```

**问题原因**：
拥有 `ENTERPRISE_LEADER` 角色的测试用户，其 `user_info.enterprise_id` 字段为 `null`，未关联具体企业。后端查企业总览时以 `enterprise_id` 为条件，值为 null 则抛出业务异常。

**解决方案**：
执行 SQL 补充该用户的企业关联（根据该用户创建课题所属企业确定）：
```sql
UPDATE user_info 
SET enterprise_id = 'E001' 
WHERE user_id = '2024759015287861249';
```

**规律总结**：企业相关角色（`ENTERPRISE_TEACHER`、`ENTERPRISE_LEADER`）的用户必须确保 `enterprise_id` 字段不为 null，否则所有依赖企业 ID 的接口都会报错。创建测试用户时需同步设置 `enterprise_id`。

---

# 答辩安排创建后报系统错误 — MySQL 排序规则冲突

- 日期：2026-03-19
- 作者：系统维护者
- 严重程度：高（创建后列表刷新失败，用户感知为“创建失败”）
- 状态：已修复（代码热修 + 数据库迁移脚本）

## 一、问题描述

### 问题表现

- 企业负责人在“新建答辩安排”点击保存后，前端提示系统错误。
- 后端日志报错：
  `Illegal mix of collations (utf8mb4_general_ci,IMPLICIT) and (utf8mb4_unicode_ci,IMPLICIT) for operation '='`

### 关键现象

- 创建动作本身已成功写入。
- 报错发生在创建后自动刷新列表的分页查询阶段。

## 二、问题根源分析（核心）

### 2.1 触发 SQL

答辩安排分页查询中存在跨表关联：

- `da.enterprise_id = e.enterprise_id`
- `da.creator_id = u.user_id`

对应文件：`complete-backend/src/main/resources/mapper/DefenseArrangementMapper.xml`

### 2.2 根因

- `defense_arrangement`（以及 opening_report/opening_task_book）建表脚本使用了 `utf8mb4_general_ci`。
- 系统核心表（如 `user_info`、`enterprise_info`）主要是 `utf8mb4_unicode_ci`。
- 当 JOIN 条件比较不同排序规则字段时，MySQL 直接抛错 1267。

对应脚本：`complete-backend/docs/sql/defense_arrangement.sql`

## 三、修复方案

### 3.1 代码热修（立即止血）

在 Mapper 的 JOIN 条件上显式统一排序规则：

```xml
LEFT JOIN enterprise_info e
  ON da.enterprise_id COLLATE utf8mb4_unicode_ci = e.enterprise_id COLLATE utf8mb4_unicode_ci
LEFT JOIN user_info u
  ON da.creator_id COLLATE utf8mb4_unicode_ci = u.user_id COLLATE utf8mb4_unicode_ci
```

已修改文件：

- `complete-backend/src/main/resources/mapper/DefenseArrangementMapper.xml`

### 3.2 数据库修复（长期稳定）

新增迁移脚本，统一 defense 模块三张表为 `utf8mb4_unicode_ci`：

- `complete-backend/docs/sql/fix_defense_collation.sql`

核心 SQL：

```sql
ALTER TABLE defense_arrangement CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE opening_report CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE opening_task_book CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

同时，已将模块建表脚本中的默认排序规则改为 `utf8mb4_unicode_ci`，避免新环境再次引入冲突。

## 四、验证步骤

1. 执行迁移脚本：`complete-backend/docs/sql/fix_defense_collation.sql`
2. 重启后端服务
3. 使用企业负责人重新测试“新建答辩安排”
4. 观察结果：
   - 创建成功后不再报系统错误
   - 列表自动刷新正常

## 五、经验总结

- 新模块建表必须与主库统一字符集与排序规则，禁止混用 `utf8mb4_general_ci` 与 `utf8mb4_unicode_ci`。
- 对跨表 JOIN 频繁的 ID 字段（如 `enterprise_id`、`creator_id`、`user_id`），应优先保证表结构层面排序规则一致。
- 热修可通过 SQL `COLLATE` 快速止血，但根治应通过表结构统一。

---