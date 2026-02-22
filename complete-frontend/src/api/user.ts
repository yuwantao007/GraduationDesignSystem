/**
 * 用户管理 API
 * @description 用户 CRUD、状态管理、密码管理、个人信息管理等接口
 * @author YuWan
 * @date 2026-02-21
 */
import request from './request'
import type { PageResult } from '@/types/common'
import type {
  UserVO,
  UserQueryVO,
  CreateUserDTO,
  UpdateUserDTO,
  UpdateProfileDTO,
  ChangePasswordDTO,
  AssignRoleDTO
} from '@/types/user'

/**
 * 用户管理 API
 */
export const userApi = {
  /**
   * 查询用户列表（分页）
   * @param params - 查询参数
   */
  getUserList(params: UserQueryVO) {
    return request.get<PageResult<UserVO>>('/user/list', { params })
  },

  /**
   * 获取用户详情
   * @param userId - 用户ID
   */
  getUserDetail(userId: string) {
    return request.get<UserVO>(`/user/${userId}`)
  },

  /**
   * 创建用户
   * @param data - 创建用户请求参数
   */
  createUser(data: CreateUserDTO) {
    return request.post<UserVO>('/user/create', data)
  },

  /**
   * 更新用户信息
   * @param userId - 用户ID
   * @param data - 更新用户请求参数
   */
  updateUser(userId: string, data: UpdateUserDTO) {
    return request.put<UserVO>(`/user/${userId}`, data)
  },

  /**
   * 删除用户
   * @param userId - 用户ID
   */
  deleteUser(userId: string) {
    return request.delete(`/user/${userId}`)
  },

  /**
   * 批量删除用户
   * @param userIds - 用户ID列表
   */
  batchDeleteUsers(userIds: string[]) {
    return request.post('/user/batch-delete', { userIds })
  },

  /**
   * 更新用户状态（启用/禁用）
   * @param userId - 用户ID
   * @param status - 用户状态（0: 禁用, 1: 正常）
   */
  updateUserStatus(userId: string, status: number) {
    return request.put(`/user/${userId}/status`, { status })
  },

  /**
   * 分配角色
   * @param data - 分配角色请求参数
   */
  assignRoles(data: AssignRoleDTO) {
    return request.post('/user/assign-roles', data)
  },

  /**
   * 修改密码
   * @param data - 修改密码请求参数
   */
  changePassword(data: ChangePasswordDTO) {
    return request.put('/user/change-password', data)
  },

  /**
   * 管理员重置用户密码
   * @param userId - 用户ID
   */
  resetPassword(userId: string) {
    return request.put(`/user/${userId}/reset-password`)
  },

  /**
   * 获取当前登录用户信息
   */
  getCurrentUserInfo() {
    return request.get<UserVO>('/user/current')
  },

  /**
   * 更新个人信息
   * @param data - 更新个人信息请求参数
   */
  updateProfile(data: UpdateProfileDTO) {
    return request.put<UserVO>('/user/profile', data)
  },

  /**
   * 上传头像
   * @param formData - 包含头像文件的 FormData
   */
  uploadAvatar(formData: FormData) {
    return request.upload<{ url: string }>('/user/avatar', formData)
  }
}
