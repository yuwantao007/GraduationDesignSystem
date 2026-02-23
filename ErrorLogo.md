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