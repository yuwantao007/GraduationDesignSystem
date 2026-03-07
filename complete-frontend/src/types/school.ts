/**
 * 学校管理相关类型定义
 * @description 包含学校信息、查询参数、DTO等类型
 * @author YuWan
 * @date 2026-02-22
 */

/**
 * 学校状态枚举
 */
export enum SchoolStatus {
  /** 禁用 */
  DISABLED = 0,
  /** 正常 */
  ACTIVE = 1
}

/**
 * 学校状态标签映射
 */
export const SCHOOL_STATUS_LABELS: Record<number, string> = {
  [SchoolStatus.DISABLED]: '禁用',
  [SchoolStatus.ACTIVE]: '正常'
}

/**
 * 学校状态颜色映射
 */
export const SCHOOL_STATUS_COLORS: Record<number, string> = {
  [SchoolStatus.DISABLED]: 'red',
  [SchoolStatus.ACTIVE]: 'green'
}

/**
 * 学校下拉选项 VO（精简版，所有登录用户可用）
 */
export interface SchoolOptionVO {
  /** 学校ID */
  schoolId: string
  /** 学校名称 */
  schoolName: string
}

/**
 * 学校信息 VO
 */
export interface SchoolVO {
  /** 学校ID */
  schoolId: string
  /** 学校名称 */
  schoolName: string
  /** 学校编码 */
  schoolCode?: string
  /** 详细地址 */
  address?: string
  /** 联系人 */
  contactPerson?: string
  /** 联系电话 */
  contactPhone?: string
  /** 学校邮箱 */
  schoolEmail?: string
  /** 学校简介 */
  description?: string
  /** 状态（0-禁用 1-正常） */
  schoolStatus: number
  /** 状态描述 */
  schoolStatusDesc?: string
  /** 创建时间 */
  createTime: string
  /** 更新时间 */
  updateTime?: string
}

/**
 * 学校查询参数 QueryVO
 */
export interface SchoolQueryVO {
  /** 学校名称（模糊查询） */
  schoolName?: string
  /** 学校编码 */
  schoolCode?: string
  /** 状态（0-禁用 1-正常） */
  schoolStatus?: number
  /** 页码 */
  pageNum?: number
  /** 每页大小 */
  pageSize?: number
}

/**
 * 创建学校请求 DTO
 */
export interface CreateSchoolDTO {
  /** 学校名称 */
  schoolName: string
  /** 学校编码 */
  schoolCode?: string
  /** 详细地址 */
  address?: string
  /** 联系人 */
  contactPerson?: string
  /** 联系电话 */
  contactPhone?: string
  /** 学校邮箱 */
  schoolEmail?: string
  /** 学校简介 */
  description?: string
}

/**
 * 更新学校请求 DTO
 */
export interface UpdateSchoolDTO {
  /** 学校名称 */
  schoolName?: string
  /** 学校编码 */
  schoolCode?: string
  /** 详细地址 */
  address?: string
  /** 联系人 */
  contactPerson?: string
  /** 联系电话 */
  contactPhone?: string
  /** 学校邮箱 */
  schoolEmail?: string
  /** 学校简介 */
  description?: string
  /** 状态（0-禁用 1-正常） */
  schoolStatus?: number
}
