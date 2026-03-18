/**
 * 中期检查类型定义
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */

/**
 * 提交状态枚举
 */
export enum MidtermSubmitStatus {
  /** 草稿 */
  DRAFT = 0,
  /** 已提交 */
  SUBMITTED = 1
}

/**
 * 审查状态枚举
 */
export enum MidtermReviewStatus {
  /** 未审查 */
  NOT_REVIEWED = 0,
  /** 合格 */
  QUALIFIED = 1,
  /** 不合格 */
  UNQUALIFIED = 2
}

/**
 * 提交状态选项
 */
export const MidtermSubmitStatusOptions = [
  { value: MidtermSubmitStatus.DRAFT, label: '草稿', color: 'default' },
  { value: MidtermSubmitStatus.SUBMITTED, label: '已提交', color: 'processing' }
]

/**
 * 审查状态选项
 */
export const MidtermReviewStatusOptions = [
  { value: MidtermReviewStatus.NOT_REVIEWED, label: '未审查', color: 'default' },
  { value: MidtermReviewStatus.QUALIFIED, label: '合格', color: 'success' },
  { value: MidtermReviewStatus.UNQUALIFIED, label: '不合格', color: 'error' }
]

/**
 * 获取提交状态描述
 */
export function getSubmitStatusLabel(status: number): string {
  const option = MidtermSubmitStatusOptions.find(o => o.value === status)
  return option ? option.label : '未知'
}

/**
 * 获取审查状态描述
 */
export function getReviewStatusLabel(status: number): string {
  const option = MidtermReviewStatusOptions.find(o => o.value === status)
  return option ? option.label : '未知'
}

/**
 * 获取提交状态颜色
 */
export function getSubmitStatusColor(status: number): string {
  const option = MidtermSubmitStatusOptions.find(o => o.value === status)
  return option ? option.color : 'default'
}

/**
 * 获取审查状态颜色
 */
export function getReviewStatusColor(status: number): string {
  const option = MidtermReviewStatusOptions.find(o => o.value === status)
  return option ? option.color : 'default'
}

/**
 * 中期检查详情VO
 */
export interface MidtermCheckVO {
  checkId: string
  studentId: string
  studentName?: string
  studentNo?: string
  topicId: string
  topicName?: string
  arrangementId?: string
  enterpriseTeacherId: string
  enterpriseTeacherName?: string
  completionStatus?: string
  existingProblems?: string
  nextPlan?: string
  documentId?: string
  documentFileName?: string
  submitStatus: number
  submitStatusDesc?: string
  reviewStatus: number
  reviewStatusDesc?: string
  reviewComment?: string
  reviewerId?: string
  reviewerName?: string
  reviewTime?: string
  createTime?: string
  updateTime?: string
}

/**
 * 中期检查列表VO
 */
export interface MidtermCheckListVO {
  checkId: string
  studentId: string
  studentName?: string
  studentNo?: string
  topicName?: string
  enterpriseTeacherName?: string
  submitStatus: number
  submitStatusDesc?: string
  reviewStatus: number
  reviewStatusDesc?: string
  reviewerName?: string
  reviewTime?: string
  createTime?: string
  updateTime?: string
}

/**
 * 创建/编辑中期检查表DTO
 */
export interface CreateMidtermCheckDTO {
  checkId?: string
  studentId: string
  topicId: string
  arrangementId?: string
  completionStatus?: string
  existingProblems?: string
  nextPlan?: string
  documentId?: string
}

/**
 * 审查中期检查表DTO
 */
export interface ReviewMidtermCheckDTO {
  checkId: string
  reviewStatus: number
  reviewComment?: string
}

/**
 * 中期检查查询参数
 */
export interface MidtermCheckQueryParams {
  studentName?: string
  submitStatus?: number
  reviewStatus?: number
  arrangementId?: string
  pageNum?: number
  pageSize?: number
}
