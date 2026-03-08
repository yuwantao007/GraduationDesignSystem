/**
 * 教师配对管理相关类型定义
 * @description 包含方向级分配、精确配对、覆盖检查等类型
 * @author YuWan
 * @date 2026-03-08
 */

/**
 * 配对类型枚举
 */
export enum RelationType {
  /** 直接配对 */
  DIRECT = 'DIRECT',
  /** 辅助支持 */
  ASSIST = 'ASSIST'
}

/**
 * 配对类型标签映射
 */
export const RELATION_TYPE_LABELS: Record<string, string> = {
  [RelationType.DIRECT]: '直接配对',
  [RelationType.ASSIST]: '辅助支持'
}

/**
 * 配对类型颜色映射
 */
export const RELATION_TYPE_COLORS: Record<string, string> = {
  [RelationType.DIRECT]: 'blue',
  [RelationType.ASSIST]: 'orange'
}

/**
 * 覆盖来源标签映射
 */
export const COVERAGE_SOURCE_LABELS: Record<string, string> = {
  DIRECTION: '方向级分配',
  DIRECT: '精确配对'
}

/**
 * 覆盖来源颜色映射
 */
export const COVERAGE_SOURCE_COLORS: Record<string, string> = {
  DIRECTION: 'cyan',
  DIRECT: 'blue'
}

// ==================== 方向级分配 ====================

/**
 * 方向级分配 VO
 */
export interface UnivTeacherMajorVO {
  /** 主键ID */
  id: string
  /** 高校教师 user_id */
  univTeacherId: string
  /** 高校教师姓名 */
  univTeacherName: string
  /** 高校教师工号 */
  univTeacherEmployeeNo: string
  /** 专业方向ID */
  directionId: string
  /** 专业方向名称 */
  directionName: string
  /** 所属企业ID */
  enterpriseId: string
  /** 企业名称 */
  enterpriseName: string
  /** 届别 */
  cohort: string
  /** 是否启用（1-启用 0-停用） */
  isEnabled: number
  /** 备注 */
  remark: string
  /** 创建时间 */
  createTime: string
}

/**
 * 方向级分配 DTO
 */
export interface UnivTeacherMajorDTO {
  /** 高校教师 user_id */
  univTeacherId: string
  /** 专业方向ID */
  directionId: string
  /** 所属企业ID */
  enterpriseId: string
  /** 届别 */
  cohort: string
  /** 备注 */
  remark?: string
}

// ==================== 精确配对 ====================

/**
 * 精确配对 VO
 */
export interface TeacherRelationVO {
  /** 主键ID */
  relationId: string
  /** 高校教师 user_id */
  univTeacherId: string
  /** 高校教师姓名 */
  univTeacherName: string
  /** 高校教师工号 */
  univTeacherEmployeeNo: string
  /** 企业教师 user_id */
  enterpriseTeacherId: string
  /** 企业教师姓名 */
  enterpriseTeacherName: string
  /** 企业教师工号 */
  enterpriseTeacherEmployeeNo: string
  /** 企业ID */
  enterpriseId: string
  /** 企业名称 */
  enterpriseName: string
  /** 所属专业方向ID */
  directionId: string
  /** 专业方向名称 */
  directionName: string
  /** 届别 */
  cohort: string
  /** 配对类型 */
  relationType: string
  /** 是否启用（1-启用 0-停用） */
  isEnabled: number
  /** 备注 */
  remark: string
  /** 创建时间 */
  createTime: string
}

/**
 * 精确配对 DTO
 */
export interface TeacherRelationDTO {
  /** 高校教师 user_id */
  univTeacherId: string
  /** 企业教师 user_id */
  enterpriseTeacherId: string
  /** 企业ID */
  enterpriseId: string
  /** 所属专业方向（可选） */
  directionId?: string
  /** 届别 */
  cohort: string
  /** 配对类型（DIRECT/ASSIST） */
  relationType?: string
  /** 备注 */
  remark?: string
}

// ==================== 覆盖检查 ====================

/**
 * 教师配对覆盖率统计 VO
 */
export interface TeacherCoverageVO {
  /** 企业教师 user_id */
  enterpriseTeacherId: string
  /** 企业教师姓名 */
  enterpriseTeacherName: string
  /** 企业教师工号 */
  enterpriseTeacherEmployeeNo: string
  /** 所属企业ID */
  enterpriseId: string
  /** 企业名称 */
  enterpriseName: string
  /** 所属专业方向ID */
  directionId: string
  /** 专业方向名称 */
  directionName: string
  /** 对应的高校教师 user_id */
  univTeacherId: string | null
  /** 对应的高校教师姓名 */
  univTeacherName: string | null
  /** 配对来源（DIRECTION/DIRECT/null） */
  coverageSource: string | null
  /** 是否已覆盖 */
  covered: boolean
}

/**
 * 覆盖率统计
 */
export interface CoverageStats {
  /** 企业教师总数 */
  totalCount: number
  /** 已覆盖数 */
  coveredCount: number
  /** 未覆盖数 */
  uncoveredCount: number
}
