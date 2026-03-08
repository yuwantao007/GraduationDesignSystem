/**
 * 用户管理相关类型定义
 * @description 包含用户、角色、权限等相关的 TypeScript 类型
 * @author YuWan
 * @date 2026-02-21
 */

/**
 * 用户角色枚举
 */
export enum UserRole {
  /** 系统管理员 */
  SYSTEM_ADMIN = 'SYSTEM_ADMIN',
  /** 督导教师 */
  SUPERVISOR_TEACHER = 'SUPERVISOR_TEACHER',
  /** 高校教师 */
  UNIVERSITY_TEACHER = 'UNIVERSITY_TEACHER',
  /** 企业负责人 */
  ENTERPRISE_LEADER = 'ENTERPRISE_LEADER',
  /** 专业方向主管 */
  MAJOR_DIRECTOR = 'MAJOR_DIRECTOR',
  /** 企业教师 */
  ENTERPRISE_TEACHER = 'ENTERPRISE_TEACHER',
  /** 学生 */
  STUDENT = 'STUDENT'
}

/**
 * 用户角色中文映射
 */
export const USER_ROLE_LABELS: Record<string, string> = {
  [UserRole.SYSTEM_ADMIN]: '系统管理员',
  [UserRole.SUPERVISOR_TEACHER]: '督导教师',
  [UserRole.UNIVERSITY_TEACHER]: '高校教师',
  [UserRole.ENTERPRISE_LEADER]: '企业负责人',
  [UserRole.MAJOR_DIRECTOR]: '专业方向主管',
  [UserRole.ENTERPRISE_TEACHER]: '企业教师',
  [UserRole.STUDENT]: '学生'
}

/**
 * 用户角色颜色映射
 */
export const USER_ROLE_COLORS: Record<string, string> = {
  [UserRole.SYSTEM_ADMIN]: 'red',
  [UserRole.SUPERVISOR_TEACHER]: 'purple',
  [UserRole.UNIVERSITY_TEACHER]: 'blue',
  [UserRole.ENTERPRISE_LEADER]: 'orange',
  [UserRole.MAJOR_DIRECTOR]: 'cyan',
  [UserRole.ENTERPRISE_TEACHER]: 'green',
  [UserRole.STUDENT]: 'geekblue'
}

/**
 * 用户状态枚举
 */
export enum UserStatus {
  /** 禁用 */
  DISABLED = 0,
  /** 正常 */
  ACTIVE = 1
}

/**
 * 用户状态标签映射
 */
export const USER_STATUS_LABELS: Record<number, string> = {
  [UserStatus.DISABLED]: '禁用',
  [UserStatus.ACTIVE]: '正常'
}

/**
 * 用户状态颜色映射
 */
export const USER_STATUS_COLORS: Record<number, string> = {
  [UserStatus.DISABLED]: 'red',
  [UserStatus.ACTIVE]: 'green'
}

/**
 * 性别枚举（与后端保持一致）
 */
export enum Gender {
  /** 女 */
  FEMALE = 0,
  /** 男 */
  MALE = 1
}

/**
 * 性别标签映射
 */
export const GENDER_LABELS: Record<number, string> = {
  [Gender.FEMALE]: '女',
  [Gender.MALE]: '男'
}

/**
 * 角色信息接口
 */
export interface RoleInfo {
  /** 角色ID */
  roleId: string
  /** 角色名称 */
  roleName: string
  /** 角色代码 */
  roleCode: string
  /** 角色描述 */
  roleDesc?: string
  /** 排序号 */
  sortOrder?: number
  /** 角色状态（0-禁用 1-启用） */
  status?: number
  /** 创建时间 */
  createTime?: string
}

/**
 * 权限信息接口
 */
export interface PermissionInfo {
  /** 权限ID */
  permissionId: string
  /** 权限名称 */
  permissionName: string
  /** 权限代码 */
  permissionCode: string
  /** 权限描述 */
  description?: string
  /** 权限类型 */
  permissionType?: string
  /** 父级权限ID */
  parentId?: string
}

/**
 * 用户信息 VO
 */
export interface UserVO {
  /** 用户ID */
  userId: string
  /** 用户名 */
  username: string
  /** 真实姓名 */
  realName: string
  /** 邮箱 */
  userEmail: string
  /** 手机号 */
  userPhone: string
  /** 用户状态 */
  userStatus: number
  /** 头像地址 */
  avatar?: string
  /** 性别（0-女 1-男） */
  gender?: number
  /** 部门 */
  department?: string
  /** 专业方向 */
  major?: string
  /** 属学生琒精确专业ID */
  majorId?: string
  /** 学号/工号 */
  userCode?: string
  /** 简介 */
  biography?: string
  /** 创建时间 */
  createTime: string
  /** 更新时间 */
  updateTime?: string
  /** 最后登录时间 */
  lastLoginTime?: string
  /** 角色列表 */
  roles: RoleInfo[]
}

/**
 * 用户查询参数 QueryVO
 */
export interface UserQueryVO {
  /** 用户名 */
  username?: string
  /** 真实姓名 */
  realName?: string
  /** 手机号 */
  userPhone?: string
  /** 用户状态 */
  userStatus?: number
  /** 部门 */
  department?: string
  /** 角色代码 */
  roleCode?: string
  /** 页码 */
  pageNum?: number
  /** 每页条数 */
  pageSize?: number
}

/**
 * 创建用户请求 DTO
 */
export interface CreateUserDTO {
  /** 用户名 */
  username: string
  /** 密码 */
  password: string
  /** 真实姓名 */
  realName: string
  /** 邮箱 */
  userEmail: string
  /** 手机号 */
  userPhone: string
  /** 性别（0-女 1-男） */
  gender?: number
  /** 部门 */
  department?: string
  /** 专业方向 */
  major?: string
  /** 学生精确专业ID（仅学生角色使用） */
  majorId?: string
  /** 学号/工号 */
  userCode?: string
  /** 角色ID列表 */
  roleIds: string[]
}

/**
 * 更新用户请求 DTO
 */
export interface UpdateUserDTO {
  /** 真实姓名 */
  realName?: string
  /** 邮箱 */
  userEmail?: string
  /** 手机号 */
  userPhone?: string
  /** 头像地址 */
  avatar?: string
  /** 性别（0-女 1-男） */
  gender?: number
  /** 部门 */
  department?: string
  /** 专业方向 */
  major?: string
  /** 学生精确专业ID（仅学生角色使用） */
  majorId?: string
  /** 学号/工号 */
  userCode?: string
  /** 简介 */
  biography?: string
  /** 角色ID列表 */
  roleIds?: string[]
}

/**
 * 更新个人信息请求 DTO
 */
export interface UpdateProfileDTO {
  /** 真实姓名 */
  realName?: string
  /** 邮箱 */
  userEmail?: string
  /** 手机号 */
  userPhone?: string
  /** 头像地址 */
  avatar?: string
  /** 性别（0-女 1-男） */
  gender?: number
  /** 简介 */
  biography?: string
}

/**
 * 修改密码请求 DTO
 */
export interface ChangePasswordDTO {
  /** 原密码 */
  oldPassword: string
  /** 新密码 */
  newPassword: string
  /** 确认密码 */
  confirmPassword: string
}

/**
 * 分配角色请求 DTO
 */
export interface AssignRoleDTO {
  /** 用户ID */
  userId: string
  /** 角色ID列表 */
  roleIds: string[]
}

/**
 * 角色查询参数 QueryVO
 */
export interface RoleQueryVO {
  /** 角色名称 */
  roleName?: string
  /** 角色代码 */
  roleCode?: string
  /** 角色状态 */
  status?: number
  /** 页码 */
  pageNum?: number
  /** 每页条数 */
  pageSize?: number
}

/**
 * 创建角色请求 DTO
 */
export interface CreateRoleDTO {
  /** 角色名称 */
  roleName: string
  /** 角色代码 */
  roleCode: string
  /** 角色描述 */
  description?: string
  /** 权限ID列表 */
  permissionIds?: string[]
}

/**
 * 更新角色请求 DTO
 */
export interface UpdateRoleDTO {
  /** 角色名称 */
  roleName?: string
  /** 角色描述 */
  description?: string
  /** 权限ID列表 */
  permissionIds?: string[]
}
