/**
 * API 统一导出
 * 这里是自动生成 API 的包装层
 * 
 * 使用方式：
 * import { api, OpenAPI } from '@/api'
 * 或导入类型：
 * import type { UserVO, LoginDTO } from '@/api'
 * 
 * 生成命令：npm run gen:api
 * 
 * ⚠️ 注意：src/api/generated 目录由工具自动生成，请勿手动修改
 */

import service from './request'
import { OpenAPI, Service } from './generated'

// 导入类型定义
export type { ApiResponse } from './request'

// 配置自动生成的 API
OpenAPI.BASE = import.meta.env.VITE_API_BASE_URL || '/api'

// 配置 Token 自动注入
// 从 localStorage 读取 token 并添加到请求头
OpenAPI.TOKEN = async () => {
  const token = localStorage.getItem('token')
  return token || ''
}

// 配置请求凭证
OpenAPI.WITH_CREDENTIALS = false
OpenAPI.CREDENTIALS = 'include'

/**
 * 导出生成的服务类
 * 所有 API 方法都在 Service 类中
 * 
 * 使用示例：
 * import { api } from '@/api'
 * const result = await api.login({ username: 'admin', password: '123456' })
 */
export const api = Service

/**
 * 导出 OpenAPI 配置对象
 * 可用于运行时修改配置（如设置 TOKEN）
 * 
 * 使用示例：
 * import { OpenAPI } from '@/api'
 * OpenAPI.TOKEN = 'your-token-here'
 */
export { OpenAPI }

/**
 * 导出所有自动生成的类型定义
 */
export * from './generated'

// 默认导出
export default {
  service,
  api,
  OpenAPI
}

