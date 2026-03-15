/**
 * 认证相关 API
 */
import request from './request'

// 登录请求参数
export interface LoginDTO {
  identifier: string
  password: string
}

// 注册请求参数
export interface RegisterDTO {
  studentNo?: string
  employeeNo?: string
  password: string
  confirmPassword: string
  realName: string
  userEmail: string
  userPhone: string
  roleCode: string
}

// 角色信息
export interface RoleVO {
  roleId: string
  roleName: string
  roleCode: string
  roleDesc?: string
  sortOrder?: number
  roleStatus?: number
}

// 用户信息
export interface UserInfo {
  userId: string
  username: string
  realName: string
  userEmail: string
  userPhone?: string
  avatar?: string
  gender?: number
  department?: string
  major?: string
  majorId?: string
  studentNo?: string
  employeeNo?: string
  title?: string
  userStatus?: number
  remark?: string
  lastLoginTime?: string
  createTime?: string
  roles: RoleVO[]
  permissions: string[]
}

// 登录响应数据
export interface LoginVO {
  accessToken: string
  refreshToken: string
  tokenType?: string
  expiresIn: number
  userInfo: UserInfo
}

// 用户认证 API
export const authApi = {
  // 用户登录
  login(data: LoginDTO) {
    return request.post<LoginVO>('/auth/login', data)
  },

  // 用户注册
  register(data: RegisterDTO) {
    return request.post('/auth/register', data)
  },

  // 用户登出
  logout() {
    return request.post('/auth/logout')
  },

  // 刷新 Token
  refreshToken(refreshToken: string) {
    return request.post<LoginVO>('/auth/refresh-token', { refreshToken })
  },

  // 获取当前用户信息
  getCurrentUser() {
    return request.get('/user/current')
  }
}
