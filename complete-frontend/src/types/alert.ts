/**
 * 系统预警相关类型定义
 * @description 智能预警中心所需的数据类型定义
 * @author 系统架构师
 * @date 2026-03-14
 */

/** 预警类型枚举 */
export enum AlertType {
  STUDENT_NOT_SELECTED = 'STUDENT_NOT_SELECTED',
  TOPIC_NO_APPLICANT   = 'TOPIC_NO_APPLICANT',
  REVIEW_BACKLOG       = 'REVIEW_BACKLOG',
  PHASE_DEADLINE_NEAR  = 'PHASE_DEADLINE_NEAR',
  SELECTION_RATE_LOW   = 'SELECTION_RATE_LOW'
}

/** 预警类型中文映射 */
export const AlertTypeLabel: Record<string, string> = {
  STUDENT_NOT_SELECTED: '学生未选报课题',
  TOPIC_NO_APPLICANT:   '课题无人选报',
  REVIEW_BACKLOG:       '课题审查积压',
  PHASE_DEADLINE_NEAR:  '阶段截止临近',
  SELECTION_RATE_LOW:   '选报率偏低'
}

/** 预警级别标签颜色映射（Ant Design Vue Tag 颜色） */
export const AlertLevelColor: Record<number, string> = {
  1: 'blue',
  2: 'orange',
  3: 'red'
}

/** 预警级别描述映射 */
export const AlertLevelLabel: Record<number, string> = {
  1: '提示',
  2: '警告',
  3: '严重'
}

/** 预警记录 VO */
export interface AlertVO {
  /** 预警ID */
  alertId: string
  /** 预警类型代码 */
  alertType: string
  /** 预警类型描述 */
  alertTypeDesc: string
  /** 预警级别（1-提示 2-警告 3-严重） */
  alertLevel: number
  /** 预警级别描述 */
  alertLevelDesc: string
  /** 预警标题 */
  alertTitle: string
  /** 预警详情 */
  alertContent: string
  /** 关联对象ID */
  targetId?: string
  /** 关联对象类型 */
  targetType?: string
  /** 是否已读（0-未读 1-已读） */
  isRead: number
  /** 是否已处理（0-未处理 1-已处理） */
  isResolved: number
  /** 处理时间 */
  resolvedAt?: string
  /** 创建时间 */
  createTime: string
  /** 更新时间 */
  updateTime?: string
}

/** 预警查询参数 */
export interface AlertQueryParams {
  alertType?: string
  alertLevel?: number
  isRead?: number
  isResolved?: number
  page?: number
  size?: number
}
