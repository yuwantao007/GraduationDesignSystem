/**
 * 工作流（Flowable 流程引擎）相关类型定义
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */

/** 待办任务 */
export interface FlowTaskVO {
  /** Flowable 任务ID */
  taskId: string
  /** 任务定义Key */
  taskDefKey: string
  /** 任务名称 */
  taskName: string
  /** 流程实例ID */
  processInstanceId: string
  /** 课题ID */
  topicId: string
  /** 课题标题 */
  topicTitle: string
  /** 课题大类（1-高职升本 2-3+1 3-实验班） */
  topicCategory: number
  /** 课题大类描述 */
  topicCategoryDesc: string
  /** 课题创建人ID */
  creatorId: string
  /** 课题创建人姓名 */
  creatorName: string
  /** 任务签收人userId */
  assignee: string | null
  /** 任务签收人姓名 */
  assigneeName: string | null
  /** 候选组描述 */
  candidateGroup: string
  /** 任务创建时间 */
  createTime: string
  /** 是否已被当前用户签收 */
  claimedByMe: boolean
  /** 是否是修改任务 */
  isModifyTask: boolean
}

/** 历史流程节点 */
export interface HistoryNodeVO {
  activityId: string
  activityName: string
  activityType: string
  assignee: string | null
  assigneeName: string | null
  startTime: string
  endTime: string | null
  active: boolean
}

/** 课题流程状态 */
export interface ProcessStatusVO {
  topicId: string
  processInstanceId: string | null
  processStarted: boolean
  processFinished: boolean
  reviewStatus: number
  reviewStatusDesc: string
  activeTaskNames: string[]
  waitingRole: string | null
  startTime: string | null
  endTime: string | null
  historyNodes: HistoryNodeVO[]
}

/** 流程实例监控信息 */
export interface ProcessInstanceVO {
  processInstanceId: string
  topicId: string
  topicTitle: string
  topicCategory: number
  topicCategoryDesc: string
  creatorId: string
  creatorName: string
  reviewStatus: number
  reviewStatusDesc: string
  currentTaskName: string | null
  waitingRole: string | null
  processStatus: number
  processStatusDesc: string
  startTime: string | null
  endTime: string | null
  durationDesc: string | null
}

/** 完成审查任务请求参数 */
export interface CompleteReviewTaskDTO {
  /** 审查结果（PASS | NEED_MODIFY | REJECT） */
  outcome: 'PASS' | 'NEED_MODIFY' | 'REJECT'
  /** 审查意见 */
  opinion?: string
}
