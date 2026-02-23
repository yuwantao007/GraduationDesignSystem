/**
 * 企业管理相关类型定义
 * @description 包含企业信息、查询参数、DTO等类型
 * @author YuWan
 * @date 2026-02-22
 */

/**
 * 企业状态枚举
 */
export enum EnterpriseStatus {
  /** 禁用 */
  DISABLED = 0,
  /** 正常 */
  ACTIVE = 1
}

/**
 * 企业状态标签映射
 */
export const ENTERPRISE_STATUS_LABELS: Record<number, string> = {
  [EnterpriseStatus.DISABLED]: '禁用',
  [EnterpriseStatus.ACTIVE]: '正常'
}

/**
 * 企业状态颜色映射
 */
export const ENTERPRISE_STATUS_COLORS: Record<number, string> = {
  [EnterpriseStatus.DISABLED]: 'red',
  [EnterpriseStatus.ACTIVE]: 'green'
}

/**
 * 企业信息 VO
 */
export interface EnterpriseVO {
  /** 企业ID */
  enterpriseId: string
  /** 企业名称 */
  enterpriseName: string
  /** 企业编码 */
  enterpriseCode?: string
  /** 联系人 */
  contactPerson?: string
  /** 联系电话 */
  contactPhone?: string
  /** 联系邮箱 */
  contactEmail?: string
  /** 企业地址 */
  address?: string
  /** 企业简介 */
  description?: string
  /** 状态（0-禁用 1-正常） */
  enterpriseStatus: number
  /** 状态描述 */
  enterpriseStatusDesc?: string
  /** 创建时间 */
  createTime: string
  /** 更新时间 */
  updateTime?: string
}

/**
 * 企业查询参数 QueryVO
 */
export interface EnterpriseQueryVO {
  /** 企业名称（模糊查询） */
  enterpriseName?: string
  /** 企业编码 */
  enterpriseCode?: string
  /** 状态（0-禁用 1-正常） */
  enterpriseStatus?: number
  /** 页码 */
  pageNum?: number
  /** 每页大小 */
  pageSize?: number
}

/**
 * 创建企业请求 DTO
 */
export interface CreateEnterpriseDTO {
  /** 企业名称 */
  enterpriseName: string
  /** 企业编码 */
  enterpriseCode?: string
  /** 联系人 */
  contactPerson?: string
  /** 联系电话 */
  contactPhone?: string
  /** 联系邮箱 */
  contactEmail?: string
  /** 企业地址 */
  address?: string
  /** 企业简介 */
  description?: string
}

/**
 * 更新企业请求 DTO
 */
export interface UpdateEnterpriseDTO {
  /** 企业名称 */
  enterpriseName?: string
  /** 企业编码 */
  enterpriseCode?: string
  /** 联系人 */
  contactPerson?: string
  /** 联系电话 */
  contactPhone?: string
  /** 联系邮箱 */
  contactEmail?: string
  /** 企业地址 */
  address?: string
  /** 企业简介 */
  description?: string
  /** 状态（0-禁用 1-正常） */
  enterpriseStatus?: number
}
