# OpenAPI 自动生成接口代码使用指南

## 概述

本项目使用 **openapi-typescript-codegen** 工具从后端的 OpenAPI (Swagger) 文档自动生成前端 TypeScript API 客户端代码。

### 优势

- ✅ **自动生成**：无需手写接口代码，后端更新后一键同步
- ✅ **类型安全**：完整的 TypeScript 类型定义，编译时即可发现错误
- ✅ **文档同步**：与后端文档保持一致，减少沟通成本
- ✅ **纯 TypeScript**：无需 Java 环境，更轻量级
- ✅ **开箱即用**：与现有 Axios 配置无缝集成

### 技术栈

- **openapi-typescript-codegen**: 0.30.0
- **后端文档**: OpenAPI 3.0.1 (Knife4j 4.4.0)
- **HTTP 客户端**: Axios 1.13.5

---

## 快速开始

### 1. 生成接口代码

#### 方式一：从本地文件生成（推荐）

```bash
npm run gen:api
```

默认从项目根目录的 `openapi.json` 文件生成。

#### 方式二：从远程 URL 生成

```bash
npm run gen:api:remote
```

从 `http://localhost:8080/api/v3/api-docs/default` 获取最新文档并生成。

> **⚠️ 注意**: 使用远程方式前需要确保后端服务已启动

### 2. 更新本地 openapi.json

如果使用本地文件方式，需要先下载最新的 OpenAPI 文档：

```powershell
# Windows PowerShell
Invoke-WebRequest -Uri "http://localhost:8080/api/v3/api-docs/default" -OutFile "openapi.json" -UseBasicParsing
```

```bash
# Linux/Mac
curl http://localhost:8080/api/v3/api-docs/default -o openapi.json
```

### 3. 生成的目录结构

```
src/api/generated/
├── core/            # 核心工具类（不要修改）
│   ├── ApiError.ts
│   ├── CancelablePromise.ts
│   ├── OpenAPI.ts   # 全局配置
│   └── request.ts   # 请求封装
├── models/          # 类型定义
│   ├── UserVO.ts
│   ├── LoginDTO.ts
│   ├── ResultUserVO.ts
│   └── ...
├── services/        # API 服务类
│   └── Service.ts   # 所有 API 方法
└── index.ts         # 统一导出
```

---

## 使用示例

### 基础用法

```typescript
import { api } from '@/api'
import type { LoginDTO, ResultLoginVO } from '@/api'

// 用户登录
const handleLogin = async () => {
  try {
    const result: ResultLoginVO = await api.login({
      username: 'admin',
      password: '123456'
    })
    
    if (result.code === 200) {
      localStorage.setItem('token', result.data.accessToken)
      console.log('登录成功', result.data.userInfo)
    }
  } catch (error) {
    console.error('登录失败', error)
  }
}
```

### 在 Vue 组件中使用

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { api } from '@/api'
import type { UserVO, UserQueryVO } from '@/api'

const users = ref<UserVO[]>([])
const loading = ref(false)

// 查询用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const query: UserQueryVO = {
      pageNum: 1,
      pageSize: 10,
      userStatus: 1
    }
    
    const result = await api.getUserList(query)
    
    if (result.code === 200) {
      users.value = result.data.records
    }
  } catch (error) {
    console.error('查询失败', error)
  } finally {
    loading.value = false
  }
}

// 删除用户
const deleteUser = async (userId: string) => {
  try {
    await api.deleteUser(userId)
    message.success('删除成功')
    fetchUsers() // 刷新列表
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchUsers()
})
</script>
```

### 在 Pinia Store 中使用

```typescript
import { defineStore } from 'pinia'
import { api } from '@/api'
import type { UserVO, LoginDTO } from '@/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null as UserVO | null
  }),

  actions: {
    // 登录
    async login(loginData: LoginDTO) {
      const result = await api.login(loginData)
      
      if (result.code === 200 && result.data) {
        this.token = result.data.accessToken
        this.userInfo = result.data.userInfo
        
        localStorage.setItem('token', result.data.accessToken)
        localStorage.setItem('refreshToken', result.data.refreshToken)
      }
      
      return result
    },

    // 登出
    async logout() {
      await api.logout()
      
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
    },

    // 获取当前用户信息
    async getCurrentUser() {
      const result = await api.getCurrentUserInfo()
      
      if (result.code === 200 && result.data) {
        this.userInfo = result.data
      }
      
      return result
    }
  }
})
```

### 错误处理

```typescript
import { api } from '@/api'
import { ApiError } from '@/api'

const handleApiCall = async () => {
  try {
    const result = await api.getUserDetail('user-id-123')
    console.log(result.data)
  } catch (error) {
    if (error instanceof ApiError) {
      // 处理 API 错误
      console.error('API 错误:', error.status, error.statusText)
      console.error('错误详情:', error.body)
      
      if (error.status === 401) {
        // 未授权，跳转登录
        router.push('/login')
      } else if (error.status === 404) {
        message.error('用户不存在')
      }
    } else {
      // 处理其他错误（网络错误等）
      console.error('请求失败:', error)
    }
  }
}
```

---

## 类型安全对比

### ❌ 手写 API（无类型安全）

```typescript
// 参数名错误、类型错误在运行时才能发现
const result = await request.post('/api/auth/login', {
  userName: 'admin',  // 错误：应该是 username
  pwd: '123456'       // 错误：应该是 password
})

// 返回值类型不明确
console.log(result.data.accesstoken)  // 编译器不会提示错误
```

### ✅ 自动生成 API（完整类型）

```typescript
// 编译时即可发现错误
const result = await api.login({
  userName: 'admin',  // ❌ 编译错误：类型不匹配
  pwd: '123456'       // ❌ 编译错误：缺少必需属性
})

// 正确的写法（有智能提示）
const result = await api.login({
  username: 'admin',  // ✅ 编译通过
  password: '123456'  // ✅ 编译通过
})

// 返回值有完整类型定义
console.log(result.data.accessToken)  // ✅ 智能提示
```

---

## Token 自动注入

项目已配置 Token 自动注入，无需在每次请求时手动添加：

```typescript
// src/api/index.ts (已配置)
OpenAPI.TOKEN = async () => {
  const token = localStorage.getItem('token')
  return token || ''
}
```

所有 API 请求会自动在请求头添加：

```
Authorization: Bearer <your-token>
```

如需临时修改 Token：

```typescript
import { OpenAPI } from '@/api'

// 设置新 Token
OpenAPI.TOKEN = 'new-token-here'

// 或使用函数动态获取
OpenAPI.TOKEN = async () => {
  return await getTokenFromSomewhere()
}
```

---

## 更新流程

当后端 API 发生变更时：

### 1. 下载最新文档

```bash
# Windows
Invoke-WebRequest -Uri "http://localhost:8080/api/v3/api-docs/default" -OutFile "openapi.json" -UseBasicParsing

# Linux/Mac
curl http://localhost:8080/api/v3/api-docs/default -o openapi.json
```

### 2. 重新生成代码

```bash
npm run gen:api
```

### 3. 检查类型错误

```bash
npm run type-check
```

生成后，编译器会自动检测所有使用旧 API 的地方，根据错误提示更新代码即可。

---

## 注意事项

### ⚠️ 不要手动修改生成的代码

`src/api/generated/` 目录下的所有文件都是自动生成的，手动修改会在下次生成时被覆盖。

如需自定义逻辑，在 `src/api/` 目录下创建新文件：

```typescript
// src/api/userHelper.ts
import { api } from '@/api'
import type { UserVO } from '@/api'

/**
 * 辅助函数：格式化用户信息
 */
export const formatUserInfo = (user: UserVO) => {
  return {
    ...user,
    fullName: user.realName,
    statusText: user.userStatus === 1 ? '正常' : '禁用'
  }
}

/**
 * 封装复杂业务逻辑
 */
export const fetchUserWithRoles = async (userId: string) => {
  const [userResult, rolesResult] = await Promise.all([
    api.getUserDetail(userId),
    api.getRolesByUserId(userId)
  ])
  
  return {
    user: userResult.data,
    roles: rolesResult.data
  }
}
```

### ⚠️ Git 忽略生成的代码

生成的代码已添加到 `.gitignore`：

```gitignore
# OpenAPI 自动生成
/src/api/generated/
.openapi-generator/
openapitools.json
```

每个开发者需要在本地执行 `npm run gen:api` 生成代码。

### ⚠️ 响应拦截器

生成的 API 使用的是内置的 Axios 实例，不会自动应用 `src/api/request.ts` 中的响应拦截器。

如需全局错误处理（如 401 跳转），建议：

1. **在路由守卫中处理**：

```typescript
// src/router/index.ts
router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (!token && to.path !== '/login') {
    next('/login')
    return
  }
  
  next()
})
```

2. **或在 API 调用处统一捕获**：

```typescript
// src/utils/apiWrapper.ts
import { ApiError } from '@/api'
import { message } from 'ant-design-vue'
import router from '@/router'

export const handleApiError = (error: any) => {
  if (error instanceof ApiError) {
    if (error.status === 401) {
      message.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      router.push('/login')
    } else if (error.status === 403) {
      message.error('无权访问')
    } else if (error.status === 500) {
      message.error('服务器错误')
    } else {
      message.error(error.statusText || '请求失败')
    }
  } else {
    message.error('网络错误')
  }
}
```

---

## 高级配置

### 自定义生成参数

编辑 `package.json` 中的 `gen:api` 脚本：

```json
{
  "scripts": {
    "gen:api": "openapi --input ./openapi.json --output ./src/api/generated --client axios"
  }
}
```

可用参数：

- `--input`: OpenAPI 文档路径（URL 或本地文件）
- `--output`: 输出目录
- `--client`: HTTP 客户端类型（axios / fetch / xhr / node）
- `--name`: 生成的客户端类名
- `--useOptions`: 使用单个 options 对象作为参数
- `--useUnionTypes`: 使用联合类型代替枚举

示例：

```json
"gen:api": "openapi --input ./openapi.json --output ./src/api/generated --client axios --useOptions --useUnionTypes"
```

### 配置基础路径

```typescript
import { OpenAPI } from '@/api'

// 动态修改基础路径
OpenAPI.BASE = import.meta.env.VITE_API_BASE_URL || '/api'

// 或根据环境切换
if (import.meta.env.MODE === 'development') {
  OpenAPI.BASE = 'http://localhost:8080/api'
} else {
  OpenAPI.BASE = 'https://api.production.com'
}
```

---

## 常见问题

### Q1: 生成失败怎么办？

**A**: 检查以下几点：

1. 确保 `openapi.json` 文件存在且格式正确
2. 确保已安装依赖：`npm install`
3. 查看后端 OpenAPI 文档是否符合规范

### Q2: 如何查看生成了哪些 API？

**A**: 查看 `src/api/generated/services/Service.ts` 文件，所有 API 方法都在这个类中。

或在 VSCode 中输入 `api.` 查看智能提示。

### Q3: 类型定义在哪里？

**A**: 所有类型定义在 `src/api/generated/models/` 目录下。

导入方式：

```typescript
import type { UserVO, LoginDTO, ResultUserVO } from '@/api'
```

### Q4: 如何取消请求？

**A**: 生成的 API 返回 `CancelablePromise`：

```typescript
import { api } from '@/api'

const request = api.getUserList({ pageNum: 1, pageSize: 10 })

// 取消请求
request.cancel()
```

### Q5: 支持并发请求吗？

**A**: 完全支持：

```typescript
const [users, roles, permissions] = await Promise.all([
  api.getUserList({ pageNum: 1, pageSize: 10 }),
  api.getAllRoles(),
  api.getPermissions()
])
```

### Q6: 如何处理后端返回的统一响应格式？

**A**: 后端返回的格式通常是：

```typescript
{
  code: 200,
  message: "成功",
  data: { ... }
}
```

生成的类型会自动包含这个结构（如 `ResultUserVO`），直接使用即可：

```typescript
const result = await api.getUserDetail('user-123')

if (result.code === 200) {
  const user = result.data  // 类型为 UserVO
}
```

---

## 总结

使用 **openapi-typescript-codegen** 可以：

- ✅ 减少 80% 的接口代码编写工作量
- ✅ 100% 类型安全，编译时即可发现错误
- ✅ 后端更新后一键同步
- ✅ 完整的智能提示和自动补全
- ✅ 纯 TypeScript 实现，无需 Java 环境

建议在团队中推广使用，大幅提升开发效率和代码质量！

---

## 相关链接

- [openapi-typescript-codegen 文档](https://github.com/ferdikoomen/openapi-typescript-codegen)
- [OpenAPI 规范](https://swagger.io/specification/)
- [Knife4j 文档](https://doc.xiaominfo.com/)
- [后端 Swagger UI](http://localhost:8080/api/doc.html)
