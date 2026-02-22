/**
 * 角色管理 API
 * @description 角色 CRUD、权限分配等接口
 * @author YuWan
 * @date 2026-02-21
 */
import request from './request'
import type { PageResult } from '@/types/common'
import type {
  RoleInfo,
  PermissionInfo,
  RoleQueryVO,
  CreateRoleDTO,
  UpdateRoleDTO
} from '@/types/user'

/**
 * 角色管理 API
 */
export const roleApi = {
  /**
   * 查询角色列表（分页）
   * @param params - 查询参数
   * @description 注意：后端实际返回的是全部角色列表（数组），前端需要自行处理分页
   */
  getRoleList(params: RoleQueryVO) {
    return request.get<RoleInfo[]>('/role/list', { params })
  },

  /**
   * 获取全部角色列表（不分页，用于下拉选择）
   */
  getAllRoles() {
    return request.get<RoleInfo[]>('/role/list')
  },

  /**
   * 获取角色详情
   * @param roleId - 角色ID
   */
  getRoleDetail(roleId: string) {
    return request.get<RoleInfo & { permissions: PermissionInfo[] }>(`/role/${roleId}`)
  },

  /**
   * 创建角色
   * @param data - 创建角色请求参数
   */
  createRole(data: CreateRoleDTO) {
    return request.post<RoleInfo>('/role/create', data)
  },

  /**
   * 更新角色
   * @param roleId - 角色ID
   * @param data - 更新角色请求参数
   */
  updateRole(roleId: string, data: UpdateRoleDTO) {
    return request.put<RoleInfo>(`/role/${roleId}`, data)
  },

  /**
   * 删除角色
   * @param roleId - 角色ID
   */
  deleteRole(roleId: string) {
    return request.delete(`/role/${roleId}`)
  },

  /**
   * 获取角色的权限列表
   * @param roleId - 角色ID
   */
  getRolePermissions(roleId: string) {
    return request.get<PermissionInfo[]>(`/role/${roleId}/permissions`)
  },

  /**
   * 更新角色权限
   * @param roleId - 角色ID
   * @param permissionIds - 权限ID列表
   */
  updateRolePermissions(roleId: string, permissionIds: string[]) {
    return request.put(`/role/${roleId}/permissions`, { permissionIds })
  },

  /**
   * 获取全部权限列表（树形结构）
   */
  getAllPermissions() {
    return request.get<PermissionInfo[]>('/permission/tree')
  }
}
