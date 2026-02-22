/**
 * 认证工具函数
 */

const TOKEN_KEY = 'access_token'
const REFRESH_TOKEN_KEY = 'refresh_token'
const USER_INFO_KEY = 'user_info'

// 获取 Token
export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

// 设置 Token
export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

// 移除 Token
export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

// 获取 Refresh Token
export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

// 设置 Refresh Token
export function setRefreshToken(token: string): void {
  localStorage.setItem(REFRESH_TOKEN_KEY, token)
}

// 移除 Refresh Token
export function removeRefreshToken(): void {
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

// 获取用户信息
export function getUserInfo(): any {
  const userInfo = localStorage.getItem(USER_INFO_KEY)
  return userInfo ? JSON.parse(userInfo) : null
}

// 设置用户信息
export function setUserInfo(userInfo: any): void {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

// 移除用户信息
export function removeUserInfo(): void {
  localStorage.removeItem(USER_INFO_KEY)
}

// 清除所有认证信息
export function clearAuth(): void {
  removeToken()
  removeRefreshToken()
  removeUserInfo()
}

// 检查是否已登录
export function isAuthenticated(): boolean {
  return !!getToken()
}

// 检查权限
export function hasPermission(permission: string): boolean {
  const userInfo = getUserInfo()
  if (!userInfo || !userInfo.permissions) {
    return false
  }
  return userInfo.permissions.includes(permission)
}

// 检查角色
export function hasRole(role: string): boolean {
  const userInfo = getUserInfo()
  if (!userInfo || !userInfo.roles) {
    return false
  }
  return userInfo.roles.includes(role)
}
