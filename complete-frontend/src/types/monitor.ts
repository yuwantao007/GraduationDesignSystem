/**
 * 质量监控相关类型定义
 * @description 质量监控仪表盘所需的数据类型定义
 * @author 系统架构师
 * @date 2026-03-14
 */

/** 监控总览数据（顶层数字卡片） */
export interface MonitorOverviewVO {
  /** 课题总数 */
  totalTopics: number
  /** 已通过终审的课题数 */
  approvedTopics: number
  /** 学生总数 */
  totalStudents: number
  /** 中选学生数 */
  selectedStudents: number
  /** 选报率（%） */
  selectionRate: number
  /** 未读预警数 */
  unreadAlerts: number
  /** 当前阶段名称 */
  currentPhaseName: string
  /** 当前毕业届别 */
  currentCohort: string
  /** 当前整体进度（%） */
  overallProgress: number
}

/** 课题状态分布（饼图数据项） */
export interface TopicStatusDistVO {
  /** 审查状态码（0-7） */
  statusCode: number
  /** 状态名称 */
  statusName: string
  /** 该状态课题数量 */
  count: number
}

/** 课题选报漏斗统计 */
export interface SelectionStatsVO {
  /** 总选报记录数 */
  totalSelections: number
  /** 待确认数 */
  pendingCount: number
  /** 中选数 */
  selectedCount: number
  /** 落选数 */
  rejectedCount: number
  /** 中选率（%） */
  confirmRate: number
  /** 已选报学生数 */
  studentsWithSelection: number
  /** 学生总数 */
  totalStudents: number
  /** 学生选报覆盖率（%） */
  studentCoverageRate: number
}

/** 企业课题数量（柱状图数据项） */
export interface EnterpriseTopicCountVO {
  enterpriseName: string
  count: number
}
