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
  /** 企业负责人用户ID */
  leaderId?: string
  /** 企业负责人姓名 */
  leaderName?: string
  /** 企业负责人手机 */
  leaderPhone?: string
  /** 企业负责人邮箱 */
  leaderEmail?: string
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
 * 专业概览 VO
 */
export interface MajorOverviewVO {
  /** 专业ID */
  majorId: string
  /** 专业名称 */
  majorName: string
  /** 专业代码 */
  majorCode?: string
  /** 学位类型 */
  degreeType?: string
  /** 该专业关联的企业老师姓名列表 */
  teacherNames?: string[]
}

/**
 * 专业方向概览 VO
 */
export interface DirectionOverviewVO {
  /** 专业方向ID */
  directionId: string
  /** 专业方向名称 */
  directionName: string
  /** 专业方向代码 */
  directionCode?: string
  /** 方向负责人 */
  leaderName?: string
  /** 该方向下的教师数量 */
  teacherCount: number
  /** 该方向下的学生数量 */
  studentCount: number
  /** 该方向下的专业列表 */
  majors: MajorOverviewVO[]
}

/**
 * 企业概览 VO（包含统计数据）
 */
export interface EnterpriseOverviewVO {
  /** 企业ID */
  enterpriseId: string
  /** 企业名称 */
  enterpriseName: string
  /** 企业编码 */
  enterpriseCode?: string
  /** 负责人 */
  leaderName?: string
  /** 负责人电话 */
  leaderPhone?: string
  /** 企业地址 */
  address?: string
  /** 状态（0-禁用 1-正常） */
  status: number
  /** 状态描述 */
  statusDesc?: string
  /** 创建时间 */
  createTime: string
  /** 方向数量 */
  directionCount: number
  /** 专业数量 */
  majorCount: number
  /** 教师数量 */
  teacherCount: number
  /** 学生数量 */
  studentCount: number
  /** 专业方向列表（包含专业详情） */
  directions: DirectionOverviewVO[]
}

/**
 * 企业查询参数 QueryVO
 */
export interface EnterpriseQueryVO {
  /** 企业名称（模糊查询） */
  enterpriseName?: string
  /** 企业编码 */
  enterpriseCode?: string
  /** 关键词（企业名称或编码，用于概览页面） */
  keyword?: string
  /** 状态（0-禁用 1-正常） */
  enterpriseStatus?: number
  /** 状态（用于概览页面，0-禁用 1-正常） */
  status?: number
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
  /** 企业编码（可自动生成） */
  enterpriseCode?: string
  /** 企业负责人用户ID */
  leaderId?: string
  /** 联系人（可由负责人自动填充） */
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
  /** 企业负责人用户ID */
  leaderId?: string
  /** 联系人（可由负责人自动填充） */
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
