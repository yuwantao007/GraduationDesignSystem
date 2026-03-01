/**
 * 专业管理相关类型定义
 * @description 包含专业方向、专业信息、树型结构、级联选择器等类型
 * @author YuWan
 * @date 2026-03-01
 */

/**
 * 状态枚举
 */
export enum MajorStatus {
  /** 禁用 */
  DISABLED = 0,
  /** 启用 */
  ENABLED = 1
}

/**
 * 状态标签映射
 */
export const MAJOR_STATUS_LABELS: Record<number, string> = {
  [MajorStatus.DISABLED]: '禁用',
  [MajorStatus.ENABLED]: '启用'
}

/**
 * 状态颜色映射
 */
export const MAJOR_STATUS_COLORS: Record<number, string> = {
  [MajorStatus.DISABLED]: 'red',
  [MajorStatus.ENABLED]: 'green'
}

/**
 * 学位类型枚举
 */
export enum DegreeType {
  /** 学术学位 */
  ACADEMIC = 'academic',
  /** 专业学位 */
  PROFESSIONAL = 'professional'
}

/**
 * 学位类型标签映射
 */
export const DEGREE_TYPE_LABELS: Record<string, string> = {
  [DegreeType.ACADEMIC]: '学术学位',
  [DegreeType.PROFESSIONAL]: '专业学位'
}

/**
 * 专业方向 VO
 */
export interface MajorDirectionVO {
  /** 专业方向ID */
  directionId: string
  /** 企业ID */
  enterpriseId: string
  /** 企业名称 */
  enterpriseName?: string
  /** 方向名称 */
  directionName: string
  /** 方向代码 */
  directionCode?: string
  /** 描述 */
  description?: string
  /** 负责人ID */
  leaderId?: string
  /** 负责人姓名 */
  leaderName?: string
  /** 排序 */
  sortOrder?: number
  /** 状态（0-禁用 1-启用） */
  status: number
  /** 下属专业数量 */
  majorCount?: number
  /** 创建时间 */
  createTime?: string
  /** 更新时间 */
  updateTime?: string
}

/**
 * 专业 VO
 */
export interface MajorVO {
  /** 专业ID */
  majorId: string
  /** 专业方向ID */
  directionId: string
  /** 专业方向名称 */
  directionName?: string
  /** 企业ID */
  enterpriseId: string
  /** 企业名称 */
  enterpriseName?: string
  /** 专业名称 */
  majorName: string
  /** 专业代码 */
  majorCode?: string
  /** 学位类型 */
  degreeType?: string
  /** 学制（年） */
  educationYears?: number
  /** 描述 */
  description?: string
  /** 排序 */
  sortOrder?: number
  /** 状态（0-禁用 1-启用） */
  status: number
  /** 创建时间 */
  createTime?: string
  /** 更新时间 */
  updateTime?: string
}

/**
 * 专业树型结构 VO
 */
export interface MajorTreeVO {
  /** 节点ID */
  id: string
  /** 显示标签 */
  label: string
  /** 节点类型：enterprise-企业 direction-专业方向 major-专业 */
  type: 'enterprise' | 'direction' | 'major'
  /** 节点代码 */
  code?: string
  /** 状态 */
  status?: number
  /** 状态描述 */
  statusDesc?: string
  /** 排序 */
  sortOrder?: number
  /** 描述 */
  description?: string
  /** 学位类型（专业节点） */
  degreeType?: string
  /** 学制（专业节点） */
  educationYears?: number
  /** 负责人姓名（方向节点） */
  leaderName?: string
  /** 是否叶子节点 */
  isLeaf?: boolean
  /** 子节点 */
  children?: MajorTreeVO[]
}

/**
 * 级联选择器 VO
 */
export interface MajorCascadeVO {
  /** 选项值 */
  value: string
  /** 显示标签 */
  label: string
  /** 是否禁用 */
  disabled?: boolean
  /** 子选项 */
  children?: MajorCascadeVO[]
}

/**
 * 专业方向表单 DTO
 */
export interface MajorDirectionDTO {
  /** 企业ID（系统管理员创建时必填） */
  enterpriseId?: string
  /** 方向名称 */
  directionName: string
  /** 方向代码 */
  directionCode?: string
  /** 描述 */
  description?: string
  /** 负责人ID */
  leaderId?: string
  /** 负责人姓名 */
  leaderName?: string
  /** 排序 */
  sortOrder?: number
}

/**
 * 专业表单 DTO
 */
export interface MajorDTO {
  /** 专业方向ID */
  directionId: string
  /** 企业ID */
  enterpriseId?: string
  /** 专业名称 */
  majorName: string
  /** 专业代码 */
  majorCode?: string
  /** 学位类型 */
  degreeType?: string
  /** 学制（年） */
  educationYears?: number
  /** 描述 */
  description?: string
  /** 排序 */
  sortOrder?: number
}

/**
 * 查询参数 VO
 */
export interface MajorQueryVO {
  /** 企业ID */
  enterpriseId?: string
  /** 专业方向ID */
  directionId?: string
  /** 关键词 */
  keyword?: string
  /** 状态 */
  status?: number
  /** 页码 */
  pageNum?: number
  /** 每页大小 */
  pageSize?: number
}
