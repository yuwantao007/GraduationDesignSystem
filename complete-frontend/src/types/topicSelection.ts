/**
 * 课题双选相关类型定义
 * 覆盖：学生选报 / 企业教师确认 / 企业负责人双选审核
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */

// ==================== 枚举与映射 ====================

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

/** 选报状态颜色映射（Ant Design Tag color） */
export const SelectionStatusColorMap: Record<number, string> = {
  [SelectionStatus.PENDING]: 'processing',
  [SelectionStatus.SELECTED]: 'success',
  [SelectionStatus.REJECTED]: 'error'
}

/** 指派状态枚举 */
export enum AssignStatus {
  /** 已取消 */
  CANCELLED = 0,
  /** 已指派 */
  ASSIGNED = 1
}

/** 指派状态描述映射 */
export const AssignStatusMap: Record<number, string> = {
  [AssignStatus.CANCELLED]: '已取消',
  [AssignStatus.ASSIGNED]: '已指派'
}

/** 指派状态颜色映射 */
export const AssignStatusColorMap: Record<number, string> = {
  [AssignStatus.CANCELLED]: 'default',
  [AssignStatus.ASSIGNED]: 'success'
}

// ==================== 学生选报 ====================

/** 选报课题请求参数 */
export interface ApplyTopicDTO {
  /** 课题ID */
  topicId: string
  /** 选报理由（必填） */
  selectionReason: string
}

/** 学生选报记录响应 */
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
  /** 选报状态（0-待确认 1-中选 2-落选） */
  selectionStatus: number
  /** 选报状态描述 */
  selectionStatusDesc: string
  /** 选报时间 */
  applyTime: string
  /** 确认时间 */
  confirmTime: string | null
}

/** 可选课题列表项 */
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
  /** 指导方向 */
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

/** 可选课题列表查询参数 */
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

// ==================== 教师确认子模块 ====================

/** 企业教师视角的选报记录 */
export interface SelectionForTeacherVO {
  /** 选报ID */
  selectionId: string
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
  /** 课题大类 */
  topicCategory: number
  /** 课题大类描述 */
  topicCategoryDesc: string
  /** 课题来源（1-校内 2-校外协同开发） */
  topicSource: number
  /** 选报理由 */
  selectionReason: string
  /** 选报状态（0-待确认 1-中选 2-落选） */
  selectionStatus: number
  /** 选报状态描述 */
  selectionStatusDesc: string
  /** 选报时间 */
  applyTime: string
  /** 确认时间 */
  confirmTime: string | null
}

// ==================== 双选审核子模块 ====================

/** 双选结果概览（课题维度，企业负责人视角） */
export interface SelectionOverviewVO {
  /** 课题ID */
  topicId: string
  /** 课题名称 */
  topicTitle: string
  /** 课题大类 */
  topicCategory: number
  /** 课题大类描述 */
  topicCategoryDesc: string
  /** 课题来源 */
  topicSource: number
  /** 课题来源描述 */
  topicSourceDesc: string
  /** 创建人（企业教师）ID */
  creatorId: string
  /** 创建人姓名 */
  creatorName: string
  /** 指导方向 */
  guidanceDirection: string
  /** 选报总人数（待确认+中选） */
  totalApplicants: number
  /** 已确认人数（中选） */
  confirmedCount: number
  /** 待确认人数 */
  pendingCount: number
  /** 落选人数 */
  rejectedCount: number
}

/** 未选报任何课题的学生信息 */
export interface UnselectedStudentVO {
  /** 学生用户ID */
  userId: string
  /** 学生姓名 */
  realName: string
  /** 学号 */
  studentNo: string
  /** 手机号 */
  userPhone: string
  /** 邮箱 */
  userEmail: string
  /** 所属部门/企业 */
  department: string
}

/** 指派教师请求参数 */
export interface AssignTeacherDTO {
  /** 学生用户ID */
  studentId: string
  /** 课题ID */
  topicId: string
  /** 关联选报记录ID */
  selectionId: string
  /** 指派教师ID（企业教师） */
  assignedTeacherId: string
}

// ==================== 高校教师配对信息 ====================

/** 高校教师的教师配对信息（与是否有选报数据无关） */
export interface UnivTeacherPairingVO {
  /** 配对关系ID */
  relationId: string
  /** 企业教师用户ID */
  enterpriseTeacherId: string
  /** 企业教师姓名 */
  enterpriseTeacherName: string
  /** 企业教师工号 */
  enterpriseTeacherEmployeeNo: string
  /** 企业ID */
  enterpriseId: string
  /** 企业名称 */
  enterpriseName: string
  /** 专业方向ID */
  directionId: string | null
  /** 专业方向名称 */
  directionName: string | null
  /** 届别 */
  cohort: string
  /** 配对类型（DIRECT/ASSIST） */
  relationType: string
  /** 配对类型描述 */
  relationTypeDesc: string
  /** 是否启用（1-启用 0-停用） */
  isEnabled: number
  /** 该企业教师终审通过的课题数 */
  approvedTopicCount: number
  /** 该企业教师名下有学生选报的课题数 */
  selectionTopicCount: number
  /** 该企业教师名下已中选学生总数 */
  confirmedStudentCount: number
}

// ==================== 高校教师查看选题 ====================

/** 高校教师视角的指导学生选报结果 */
export interface SelectionForUnivTeacherVO {
  /** 选报ID */
  selectionId: string
  /** 选报状态（0-待确认 1-中选 2-落选） */
  selectionStatus: number
  /** 选报状态描述 */
  selectionStatusDesc: string
  /** 选报理由 */
  selectionReason: string
  /** 选报时间 */
  applyTime: string
  /** 确认时间 */
  confirmTime: string | null
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
  /** 课题大类 */
  topicCategory: number
  /** 课题大类描述 */
  topicCategoryDesc: string
  /** 课题来源（1-校内 2-校外协同开发） */
  topicSource: number
  /** 课题来源描述 */
  topicSourceDesc: string
  /** 指导方向 */
  guidanceDirection: string
  /** 企业教师ID */
  enterpriseTeacherId: string
  /** 企业教师姓名 */
  enterpriseTeacherName: string
  /** 企业名称 */
  enterpriseName: string
}

/** 教师指派记录响应 */
export interface TeacherAssignmentVO {
  /** 指派ID */
  assignmentId: string
  /** 学生用户ID */
  studentId: string
  /** 学生姓名 */
  studentName: string
  /** 学号 */
  studentNo: string
  /** 课题ID */
  topicId: string
  /** 课题名称 */
  topicTitle: string
  /** 课题来源（1-校内 2-校外协同开发） */
  topicSource: number
  /** 课题来源描述 */
  topicSourceDesc: string
  /** 指派教师ID */
  assignedTeacherId: string
  /** 指派教师姓名 */
  assignedTeacherName: string
  /** 指派人姓名（企业负责人） */
  assignedByName: string
  /** 指派时间 */
  assignTime: string
  /** 取消时间 */
  cancelTime: string | null
  /** 指派状态（1-已指派 0-已取消） */
  assignStatus: number
  /** 指派状态描述 */
  assignStatusDesc: string
}
