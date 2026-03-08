/**
 * 课题选报相关类型定义
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */

/** 选报状态枚举 */
export enum SelectionStatus {
  /** 待确认 */
  PENDING = 0,
  /** 中选 */
  SELECTED = 1,
  /** 落选 */
  REJECTED = 2
}

/** 选报状态描述映射 */
export const SelectionStatusMap: Record<number, string> = {
  [SelectionStatus.PENDING]: '待确认',
  [SelectionStatus.SELECTED]: '中选',
  [SelectionStatus.REJECTED]: '落选'
}

/** 选报状态颜色映射 */
export const SelectionStatusColorMap: Record<number, string> = {
  [SelectionStatus.PENDING]: 'processing',
  [SelectionStatus.SELECTED]: 'success',
  [SelectionStatus.REJECTED]: 'error'
}

/** 课题选报请求参数 */
export interface ApplyTopicDTO {
  /** 课题ID */
  topicId: string
  /** 选报理由 */
  selectionReason: string
}

/** 课题选报记录响应 */
export interface TopicSelectionVO {
  /** 选报ID */
  selectionId: string
  /** 课题ID */
  topicId: string
  /** 课题名称 */
  topicTitle: string
  /** 课题大类 */
  topicCategory: number
  /** 课题大类描述 */
  topicCategoryDesc: string
  /** 归属企业名称 */
  enterpriseName: string
  /** 指导方向/专业 */
  guidanceDirection: string
  /** 企业教师姓名 */
  creatorName: string
  /** 选报理由 */
  selectionReason: string
  /** 选报状态 */
  selectionStatus: number
  /** 选报状态描述 */
  selectionStatusDesc: string
  /** 选报时间 */
  applyTime: string
  /** 确认时间 */
  confirmTime: string | null
}

/** 可选课题列表项响应 */
export interface TopicForSelectionVO {
  /** 课题ID */
  topicId: string
  /** 课题名称 */
  topicTitle: string
  /** 课题大类 */
  topicCategory: number
  /** 课题大类描述 */
  topicCategoryDesc: string
  /** 课题类型 */
  topicType: number
  /** 课题类型描述 */
  topicTypeDesc: string
  /** 课题来源 */
  topicSource: number
  /** 课题来源描述 */
  topicSourceDesc: string
  /** 归属企业名称 */
  enterpriseName: string
  /** 专业ID */
  majorId: string
  /** 专业名称 */
  majorName: string
  /** 指导方向/专业 */
  guidanceDirection: string
  /** 企业教师姓名 */
  creatorName: string
  /** 课题内容简述 */
  contentSummary: string
  /** 已选报人数 */
  selectedCount: number
  /** 当前学生是否已选报 */
  alreadyApplied: boolean
}

/** 可选课题查询参数 */
export interface SelectionQueryParams {
  /** 课题大类 */
  topicCategory?: number
  /** 指导方向 */
  guidanceDirection?: string
  /** 课题名称 */
  topicTitle?: string
  /** 专业ID */
  majorId?: string
  /** 页码 */
  pageNum?: number
  /** 每页条数 */
  pageSize?: number
}
