/**
 * 开题答辩管理模块类型定义
 * @since 2026-03-17
 */

// ==================== 枚举类型 ====================

/** 答辩类型枚举 */
export enum DefenseType {
  OPENING = 1,    // 开题答辩
  MIDTERM = 2,    // 中期答辩
  FINAL = 3,      // 正式答辩
  SECONDARY = 4   // 二次答辩
}

/** 答辩类型名称映射 */
export const DefenseTypeMap: Record<DefenseType, string> = {
  [DefenseType.OPENING]: '开题答辩',
  [DefenseType.MIDTERM]: '中期答辩',
  [DefenseType.FINAL]: '正式答辩',
  [DefenseType.SECONDARY]: '二次答辩'
}

/** 开题报告审查状态枚举 */
export enum OpeningReportStatus {
  NOT_SUBMITTED = 0,  // 未提交
  SUBMITTED = 1,      // 已提交待审
  PASSED = 2,         // 通过
  FAILED = 3          // 不合格
}

/** 开题报告状态名称映射 */
export const OpeningReportStatusMap: Record<OpeningReportStatus, string> = {
  [OpeningReportStatus.NOT_SUBMITTED]: '未提交',
  [OpeningReportStatus.SUBMITTED]: '已提交待审',
  [OpeningReportStatus.PASSED]: '通过',
  [OpeningReportStatus.FAILED]: '不合格'
}

/** 状态颜色映射 */
export const OpeningReportStatusColor: Record<OpeningReportStatus, string> = {
  [OpeningReportStatus.NOT_SUBMITTED]: 'default',
  [OpeningReportStatus.SUBMITTED]: 'processing',
  [OpeningReportStatus.PASSED]: 'success',
  [OpeningReportStatus.FAILED]: 'error'
}

// ==================== 答辩安排相关类型 ====================

/** 教师简要信息 */
export interface TeacherInfo {
  userId: string
  realName: string
}

/** 答辩安排VO */
export interface DefenseArrangementVO {
  arrangementId: string
  defenseType: DefenseType
  defenseTypeName: string
  topicCategory: string
  majorId: string
  majorName?: string
  defenseTime: string
  defenseLocation: string
  panelTeachers: string[]
  panelTeacherInfos: TeacherInfo[]
  deadline?: string
  cohort: string
  enterpriseId: string
  enterpriseName: string
  creatorId: string
  creatorName: string
  status: number
  remark?: string
  createTime: string
  updateTime: string
}

/** 创建答辩安排DTO */
export interface CreateArrangementDTO {
  defenseType: DefenseType
  topicCategory: string
  majorId: string
  defenseTime: string
  defenseLocation: string
  panelTeachers: string[]
  deadline?: string
  cohort: string
  remark?: string
}

/** 更新答辩安排DTO */
export interface UpdateArrangementDTO extends CreateArrangementDTO {
  arrangementId: string
}

/** 答辩安排查询DTO */
export interface ArrangementQueryDTO {
  defenseType?: DefenseType
  topicCategory?: string
  majorId?: string
  cohort?: string
  status?: number
  pageNum?: number
  pageSize?: number
}

// ==================== 开题任务书相关类型 ====================

/** 开题任务书VO */
export interface OpeningTaskBookVO {
  taskBookId: string
  studentId: string
  studentName: string
  studentNo: string
  topicId: string
  topicName: string
  teacherId: string
  teacherName: string
  content?: string
  documentId?: string
  documentName?: string
  documentUrl?: string
  createTime: string
  updateTime: string
}

/** 保存任务书DTO */
export interface SaveTaskBookDTO {
  studentId: string
  topicId: string
  content?: string
  documentId?: string
}

// ==================== 开题报告相关类型 ====================

/** 开题报告VO */
export interface OpeningReportVO {
  reportId: string
  studentId: string
  studentName: string
  studentNo: string
  topicId: string
  topicName: string
  arrangementId?: string
  documentId?: string
  documentName?: string
  documentUrl?: string
  submitTime?: string
  reviewStatus: OpeningReportStatus
  reviewStatusName: string
  reviewComment?: string
  reviewerId?: string
  reviewerName?: string
  reviewTime?: string
  createTime: string
  updateTime: string
}

/** 提交开题报告DTO */
export interface SubmitReportDTO {
  topicId: string
  arrangementId?: string
  documentId: string
}

/** 审查开题报告DTO */
export interface ReviewReportDTO {
  reportId: string
  reviewStatus: OpeningReportStatus.PASSED | OpeningReportStatus.FAILED
  reviewComment?: string
}

/** 开题报告查询DTO */
export interface ReportQueryDTO {
  studentName?: string
  reviewStatus?: OpeningReportStatus
  arrangementId?: string
  pageNum?: number
  pageSize?: number
}

// ==================== 分页响应类型 ====================

/** 分页响应 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
