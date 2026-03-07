/**
 * 系统阶段管理模块类型定义
 * 对应后端SystemPhase相关的DTO和VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */

/**
 * 系统阶段枚举
 */
export enum SystemPhase {
  /** 课题申报 */
  TOPIC_DECLARATION = 'TOPIC_DECLARATION',
  /** 课题双选 */
  TOPIC_SELECTION = 'TOPIC_SELECTION',
  /** 课题指导 */
  TOPIC_GUIDANCE = 'TOPIC_GUIDANCE',
  /** 毕设答辩 */
  GRADUATION_DEFENSE = 'GRADUATION_DEFENSE'
}

/**
 * 阶段状态
 */
export type PhaseStatus = 'COMPLETED' | 'ACTIVE' | 'PENDING'

/**
 * 阶段项VO - 进度条中的单个阶段
 */
export interface PhaseItemVO {
  /** 阶段代码 */
  phaseCode: string
  /** 阶段名称 */
  phaseName: string
  /** 阶段序号 */
  order: number
  /** 阶段状态：COMPLETED/ACTIVE/PENDING */
  status: PhaseStatus
  /** 切换时间 */
  switchTime?: string
  /** 阶段描述 */
  description?: string
  /** 图标 */
  icon?: string
  /** 颜色 */
  color?: string
}

/**
 * 阶段状态VO - 当前系统阶段完整信息
 */
export interface PhaseStatusVO {
  /** 当前阶段代码 */
  phaseCode?: string
  /** 当前阶段名称 */
  phaseName?: string
  /** 当前阶段序号 */
  phaseOrder?: number
  /** 总阶段数 */
  totalPhases: number
  /** 切换时间 */
  switchTime?: string
  /** 操作人姓名 */
  operatorName?: string
  /** 毕业届别（如：2026届） */
  cohort?: string
  /** 进度百分比 */
  progressPercent: number
  /** 是否已初始化 */
  initialized: boolean
  /** 阶段列表 */
  phaseList: PhaseItemVO[]
}

/**
 * 阶段切换记录VO
 */
export interface PhaseRecordVO {
  /** 记录ID */
  recordId: string
  /** 阶段代码 */
  phaseCode: string
  /** 阶段名称 */
  phaseName: string
  /** 阶段序号 */
  phaseOrder: number
  /** 上一阶段代码 */
  previousPhaseCode?: string
  /** 上一阶段名称 */
  previousPhaseName?: string
  /** 切换时间 */
  switchTime: string
  /** 操作人ID */
  operatorId: string
  /** 操作人姓名 */
  operatorName: string
  /** 切换原因 */
  switchReason: string
  /** 是否当前阶段 */
  isCurrent: boolean
  /** 毕业届别 */
  cohort: string
}

/**
 * 初始化阶段DTO
 */
export interface InitPhaseDTO {
  /** 毕业届别，如：2026届 */
  cohort: string
  /** 初始化原因 */
  reason?: string
}

/**
 * 切换阶段DTO
 */
export interface SwitchPhaseDTO {
  /** 目标阶段代码 */
  targetPhaseCode: string
  /** 切换原因 */
  switchReason: string
}
