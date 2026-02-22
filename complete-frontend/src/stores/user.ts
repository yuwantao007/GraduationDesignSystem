/**
 * 用户状态管理
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api/auth'
import type { LoginDTO, RoleVO, UserInfo } from '@/api/auth'
import { setToken, setRefreshToken, setUserInfo, clearAuth, getToken, getUserInfo } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  // 从 localStorage 读取初始数据
  const storedUserInfo = getUserInfo()
  
  // 状态
  const token = ref<string>(getToken() || '')
  const userInfo = ref<UserInfo | null>(storedUserInfo)
  // 如果有存储的用户信息，提取 roles 和 permissions
  const roles = ref<string[]>(
    storedUserInfo?.roles?.map((role: RoleVO) => role.roleCode) || []
  )
  const permissions = ref<string[]>(storedUserInfo?.permissions || [])

  // 登录
  const login = async (loginData: LoginDTO) => {
    try {
      const response = await authApi.login(loginData)
      const data = response.data
      
      // 保存 token
      token.value = data.accessToken
      setToken(data.accessToken)
      setRefreshToken(data.refreshToken)
      
      // 保存用户信息
      userInfo.value = data.userInfo
      // 将 RoleVO 数组转换为 roleCode 字符串数组
      roles.value = data.userInfo.roles?.map((role: RoleVO) => role.roleCode) || []
      permissions.value = data.userInfo.permissions || []
      setUserInfo(data.userInfo)
      
      return data
    } catch (error) {
      return Promise.reject(error)
    }
  }

  // 登出
  const logout = async () => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      // 清除本地数据
      token.value = ''
      userInfo.value = null
      roles.value = []
      permissions.value = []
      clearAuth()
    }
  }

  // 获取用户信息
  const getUserInfoData = async () => {
    try {
      const response = await authApi.getCurrentUser()
      userInfo.value = response.data
      // 将 RoleVO 数组转换为 roleCode 字符串数组
      roles.value = response.data.roles?.map((role: RoleVO) => role.roleCode) || []
      permissions.value = response.data.permissions || []
      setUserInfo(response.data)
      return response.data
    } catch (error) {
      return Promise.reject(error)
    }
  }

  // 检查权限（超管 SYSTEM_ADMIN 拥有所有权限）
  const hasPermission = (permission: string): boolean => {
    if (roles.value.includes('SYSTEM_ADMIN')) {
      return true
    }
    return permissions.value.includes(permission)
  }

  // 检查角色（超管 SYSTEM_ADMIN 拥有所有角色权限）
  const hasRole = (role: string): boolean => {
    if (roles.value.includes('SYSTEM_ADMIN')) {
      return true
    }
    return roles.value.includes(role)
  }

  return {
    token,
    userInfo,
    roles,
    permissions,
    login,
    logout,
    getUserInfoData,
    hasPermission,
    hasRole
  }
})
