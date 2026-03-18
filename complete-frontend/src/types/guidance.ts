/**
 * 指导记录相关类型定义
 * 覆盖：教师指导 / 学生查看 / 企业负责人概览
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-16
 */

// ==================== 枚举与映射 ====================

/** 指导类型枚举 */
export enum GuidanceType {
  /** 项目指导（企业教师） */
  PROJECT = 1,
  /** 论文指导（高校教师） */
  THESIS = 2
}

/** 指导类型描述映射 */
export const GuidanceTypeMap: Record<number, string> = {
  [GuidanceType.PROJECT]: '项目指导',
  [GuidanceType.THESIS]: '论文指导'
}

/** 指导类型颜色映射（Ant Design Tag color） */
export const GuidanceTypeColorMap: Record<number, string> = {
  [GuidanceType.PROJECT]: 'blue',
  [GuidanceType.THESIS]: 'green'
}

// ==================== 请求参数 ====================

/** 创建指导记录请求参数 */
export interface CreateGuidanceDTO {
  /** 学生用户ID */
  studentId: string
  /** 关联课题ID */
  topicId: string
  /** 指导类型（1-项目指导 2-论文指导） */
  guidanceType: number
  /** 指导日期 */
  guidanceDate: string
  /** 指导内容（必填） */
  guidanceContent: string
  /** 指导方式（线上/线下/邮件等） */
  guidanceMethod?: string
  /** 指导时长（小时） */
  durationHours?: number
}

/** 指导记录查询参数 */
export interface GuidanceQueryParams {
  /** 学生用户ID */
  studentId?: string
  /** 课题ID */
  topicId?: string
  /** 指导类型 */
  guidanceType?: number
  /** 学生姓名（模糊查询） */
  studentName?: string
  /** 指导教师ID */
  teacherId?: string
  /** 开始日期 */
  startDate?: string
  /** 结束日期 */
  endDate?: string
  /** 页码 */
  pageNum?: number
  /** 每页大小 */
  pageSize?: number
}

// ==================== 响应数据 ====================

/** 指导记录详情响应 */
export interface GuidanceRecordVO {
  /** 指导记录ID */
  recordId: string
  /** 学生用户ID */
  studentId: string
  /** 学生姓名 */
  studentName: string
  /** 学号 */
  studentNo: string
  /** 指导教师ID */
  teacherId: string
  /** 指导教师姓名 */
  teacherName: string
  /** 关联课题ID */
  topicId: string
  /** 课题名称 */
  topicTitle: string
  /** 指导类型 */
  guidanceType: number
  /** 指导类型描述 */
  guidanceTypeDesc: string
  /** 指导日期 */
  guidanceDate: string
  /** 指导内容 */
  guidanceContent: string
  /** 指导方式 */
  guidanceMethod: string | null
  /** 指导时长（小时） */
  durationHours: number | null
  /** 创建时间 */
  createTime: string
  /** 更新时间 */
  updateTime: string
}

/** 指导记录列表项响应 */
export interface GuidanceListVO {
  /** 指导记录ID */
  recordId: string
  /** 学生用户ID */
  studentId: string
  /** 学生姓名 */
  studentName: string
  /** 学号 */
  studentNo: string
  /** 学生手机号 */
  studentPhone: string
  /** 学生邮箱 */
  studentEmail: string
  /** 指导教师ID */
  teacherId: string
  /** 指导教师姓名 */
  teacherName: string
  /** 关联课题ID */
  topicId: string
  /** 课题名称 */
  topicTitle: string
  /** 指导类型 */
  guidanceType: number
  /** 指导类型描述 */
  guidanceTypeDesc: string
  /** 指导日期 */
  guidanceDate: string
  /** 指导方式 */
  guidanceMethod: string | null
  /** 指导时长（小时） */
  durationHours: number | null
  /** 指导内容摘要 */
  contentSummary: string | null
  /** 创建时间 */
  createTime: string
}

/** 教师视角的学生信息（含指导统计） */
export interface GuidanceStudentVO {
  /** 学生用户ID */
  studentId: string
  /** 学生姓名 */
  studentName: string
  /** 学号 */
  studentNo: string
  /** 学生手机号 */
  studentPhone: string
  /** 学生邮箱 */
  studentEmail: string
  /** 课题ID */
  topicId: string
  /** 课题名称 */
  topicTitle: string
  /** 指导记录数量 */
  recordCount: number
  /** 最新指导日期 */
  lastGuidanceDate: string | null
  /** 总指导时长（小时） */
  totalHours: string | null
}
