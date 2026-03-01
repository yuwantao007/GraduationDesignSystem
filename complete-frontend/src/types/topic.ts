/**
 * 课题模块类型定义
 * 对应后端Topic相关的DTO和VO
 * 
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-22
 */

/**
 * 课题大类枚举
 */
export enum TopicCategory {
  /** 高职升本 */
  UPGRADE = 1,
  /** 3+1联合培养 */
  THREE_PLUS_ONE = 2,
  /** 实验班 */
  EXPERIMENTAL = 3
}

/**
 * 课题类型枚举
 */
export enum TopicType {
  /** 设计 */
  DESIGN = 1,
  /** 论文 */
  THESIS = 2
}

/**
 * 课题来源枚举
 */
export enum TopicSource {
  /** 校内 */
  INTERNAL = 1,
  /** 校外协同开发 */
  EXTERNAL = 2
}

/**
 * 审查状态枚举
 */
export enum TopicReviewStatus {
  /** 待预审 */
  PENDING_PRE_REVIEW = 1,
  /** 预审通过 */
  PRE_REVIEW_PASSED = 2,
  /** 预审不通过 */
  PRE_REVIEW_REJECTED = 3,
  /** 待终审 */
  PENDING_FINAL_REVIEW = 4,
  /** 终审通过 */
  FINAL_REVIEW_PASSED = 5,
  /** 终审不通过 */
  FINAL_REVIEW_REJECTED = 6
}

/**
 * 签名类型枚举
 */
export enum TopicSignType {
  /** 企业负责人签名 */
  ENTERPRISE_LEADER = 1,
  /** 学院负责人签名 */
  COLLEGE_LEADER = 2,
  /** 高校指导教师签名 */
  UNIVERSITY_TEACHER = 3
}

/**
 * 工作量明细项
 */
export interface WorkloadItem {
  /** 阶段名称 */
  stage?: string
  /** 工作内容 */
  content?: string
  /** 周数 */
  weeks?: number
}

/**
 * 任务进度要求项
 */
export interface ScheduleItem {
  /** 阶段 */
  stage?: string
  /** 时间期限 */
  deadline?: string
  /** 任务内容 */
  task?: string
  /** 文本内容（纯文本存储时使用） */
  content?: string
}

/**
 * 参考文献项
 */
export interface ReferenceItem {
  /** 序号 */
  index?: number
  /** 文献标题 */
  title?: string
  /** 作者 */
  author?: string
  /** 出版信息 */
  publication?: string
  /** 文本内容（纯文本存储时使用） */
  content?: string
}

/**
 * 课题信息实体
 */
export interface Topic {
  /** 课题ID */
  topicId: string
  /** 课题名称/题目 */
  topicTitle: string
  /** 课题大类 */
  topicCategory: TopicCategory
  /** 课题类型 */
  topicType: TopicType
  /** 课题来源 */
  topicSource: TopicSource
  /** 适用学校 */
  applicableSchool?: string
  /** 归属企业ID */
  enterpriseId?: string
  /** 归属学校ID */
  schoolId?: string
  /** 指导方向/专业 */
  guidanceDirection?: string
  /** 选题背景与意义 */
  backgroundSignificance: string
  /** 课题内容简述 */
  contentSummary: string
  /** 专业知识综合训练情况 */
  professionalTraining: string
  /** 开发环境(工具) */
  developmentEnvironment?: Record<string, string>
  /** 工作量总周数 */
  workloadWeeks?: number
  /** 工作量明细 */
  workloadDetail?: WorkloadItem[]
  /** 任务与进度要求 */
  scheduleRequirements?: ScheduleItem[]
  /** 主要参考文献 */
  topicReferences?: ReferenceItem[]
  /** 起止日期-开始 */
  startDate?: string
  /** 起止日期-结束 */
  endDate?: string
  /** 备注 */
  remark?: string
  /** 创建人ID */
  creatorId: string
  /** 学院负责人签名 */
  collegeLeaderSign?: string
  /** 学院负责人签名时间 */
  collegeLeaderSignTime?: string
  /** 企业负责人签名 */
  enterpriseLeaderSign?: string
  /** 企业负责人签名时间 */
  enterpriseLeaderSignTime?: string
  /** 高校指导教师签名 */
  universityTeacherSign?: string
  /** 高校指导教师签名时间 */
  universityTeacherSignTime?: string
  /** 审查状态 */
  reviewStatus?: TopicReviewStatus
  /** 是否已提交 */
  isSubmitted?: number
  /** 创建时间 */
  createTime?: string
  /** 更新时间 */
  updateTime?: string
}

/**
 * 课题详情VO
 */
export interface TopicVO extends Topic {
  /** 课题大类描述 */
  topicCategoryDesc?: string
  /** 课题类型描述 */
  topicTypeDesc?: string
  /** 课题来源描述 */
  topicSourceDesc?: string
  /** 归属企业名称 */
  enterpriseName?: string
  /** 归属学校名称 */
  schoolName?: string
  /** 创建人姓名 */
  creatorName?: string
  /** 审查状态描述 */
  reviewStatusDesc?: string
}

/**
 * 课题列表项VO
 */
export interface TopicListVO {
  /** 课题ID */
  topicId: string
  /** 课题名称 */
  topicTitle: string
  /** 课题大类 */
  topicCategory: TopicCategory
  /** 课题大类描述 */
  topicCategoryDesc: string
  /** 课题类型 */
  topicType: TopicType
  /** 课题类型描述 */
  topicTypeDesc: string
  /** 课题来源 */
  topicSource: TopicSource
  /** 课题来源描述 */
  topicSourceDesc: string
  /** 归属企业ID */
  enterpriseId?: string
  /** 归属企业名称 */
  enterpriseName?: string
  /** 归属学校ID */
  schoolId?: string
  /** 归属学校名称 */
  schoolName?: string
  /** 指导方向/专业 */
  guidanceDirection?: string
  /** 创建人ID */
  creatorId: string
  /** 创建人姓名 */
  creatorName?: string
  /** 审查状态 */
  reviewStatus?: TopicReviewStatus
  /** 审查状态描述 */
  reviewStatusDesc?: string
  /** 是否已提交 */
  isSubmitted: number
  /** 创建时间 */
  createTime: string
  /** 更新时间 */
  updateTime?: string
}

/**
 * 创建课题请求DTO
 */
export interface CreateTopicDTO {
  /** 课题名称/题目 */
  topicTitle: string
  /** 课题大类 */
  topicCategory: TopicCategory
  /** 课题类型 */
  topicType: TopicType
  /** 课题来源 */
  topicSource: TopicSource
  /** 适用学校 */
  applicableSchool?: string
  /** 归属企业ID */
  enterpriseId?: string
  /** 归属学校ID */
  schoolId?: string
  /** 指导方向/专业 */
  guidanceDirection?: string
  /** 选题背景与意义 */
  backgroundSignificance: string
  /** 课题内容简述 */
  contentSummary: string
  /** 专业知识综合训练情况 */
  professionalTraining: string
  /** 开发环境(工具) */
  developmentEnvironment?: Record<string, string>
  /** 工作量总周数 */
  workloadWeeks?: number
  /** 工作量明细 */
  workloadDetail?: WorkloadItem[]
  /** 任务与进度要求 */
  scheduleRequirements?: ScheduleItem[]
  /** 主要参考文献 */
  topicReferences?: ReferenceItem[]
  /** 起止日期-开始 */
  startDate?: string
  /** 起止日期-结束 */
  endDate?: string
  /** 备注 */
  remark?: string
}

/**
 * 更新课题请求DTO
 */
export interface UpdateTopicDTO {
  /** 课题名称/题目 */
  topicTitle?: string
  /** 课题大类 */
  topicCategory?: TopicCategory
  /** 课题类型 */
  topicType?: TopicType
  /** 课题来源 */
  topicSource?: TopicSource
  /** 适用学校 */
  applicableSchool?: string
  /** 归属企业ID */
  enterpriseId?: string
  /** 归属学校ID */
  schoolId?: string
  /** 指导方向/专业 */
  guidanceDirection?: string
  /** 选题背景与意义 */
  backgroundSignificance?: string
  /** 课题内容简述 */
  contentSummary?: string
  /** 专业知识综合训练情况 */
  professionalTraining?: string
  /** 开发环境(工具) */
  developmentEnvironment?: Record<string, string>
  /** 工作量总周数 */
  workloadWeeks?: number
  /** 工作量明细 */
  workloadDetail?: WorkloadItem[]
  /** 任务与进度要求 */
  scheduleRequirements?: ScheduleItem[]
  /** 主要参考文献 */
  topicReferences?: ReferenceItem[]
  /** 起止日期-开始 */
  startDate?: string
  /** 起止日期-结束 */
  endDate?: string
  /** 备注 */
  remark?: string
}

/**
 * 课题查询参数VO
 */
export interface TopicQueryVO {
  /** 课题名称（模糊查询） */
  topicTitle?: string
  /** 课题大类 */
  topicCategory?: TopicCategory
  /** 课题类型 */
  topicType?: TopicType
  /** 课题来源 */
  topicSource?: TopicSource
  /** 归属企业ID */
  enterpriseId?: string
  /** 指导方向/专业（模糊查询） */
  guidanceDirection?: string
  /** 创建人ID */
  creatorId?: string
  /** 审查状态 */
  reviewStatus?: TopicReviewStatus
  /** 是否已提交 */
  isSubmitted?: number
  /** 页码 */
  pageNum?: number
  /** 每页大小 */
  pageSize?: number
}

/**
 * 提交课题请求DTO
 */
export interface SubmitTopicDTO {
  /** 课题ID */
  topicId: string
}

/**
 * 课题签名请求DTO
 */
export interface TopicSignDTO {
  /** 课题ID */
  topicId: string
  /** 签名类型 */
  signType: TopicSignType
  /** 签名图片URL */
  signatureUrl: string
}

/**
 * 课题表单数据（用于表单页面）
 */
export interface TopicFormData extends Omit<CreateTopicDTO, 'startDate' | 'endDate'> {
  /** 起止日期范围 */
  dateRange?: [string, string]
}

// ======================== 课题审查相关类型定义 ========================

/**
 * 审查阶段枚举
 */
export enum ReviewStage {
  /** 预审 */
  PRE_REVIEW = 1,
  /** 初审 */
  INITIAL_REVIEW = 2,
  /** 终审 */
  FINAL_REVIEW = 3
}

/**
 * 审查结果枚举
 */
export enum ReviewResult {
  /** 通过 */
  PASSED = 1,
  /** 需修改 */
  NEED_MODIFY = 2,
  /** 不通过 */
  REJECTED = 3
}

/**
 * 待审查课题列表VO
 */
export interface TopicReviewListVO {
  /** 课题ID */
  topicId: string
  /** 课题名称 */
  topicTitle: string
  /** 课题大类（1-高职升本 2-3+1 3-实验班） */
  topicCategory: TopicCategory
  /** 课题大类名称 */
  topicCategoryName: string
  /** 课题类型（1-设计 2-论文） */
  topicType: TopicType
  /** 课题类型名称 */
  topicTypeName: string
  /** 指导方向/专业 */
  guidanceDirection?: string
  /** 归属企业ID */
  enterpriseId?: string
  /** 归属企业名称 */
  enterpriseName?: string
  /** 创建人ID（企业教师） */
  creatorId: string
  /** 创建人姓名 */
  creatorName?: string
  /** 当前审查状态 */
  reviewStatus: number
  /** 当前审查状态名称 */
  reviewStatusName: string
  /** 是否可审批 */
  canReview: boolean
  /** 提交时间 */
  submitTime?: string
  /** 最近审查时间 */
  lastReviewTime?: string
  /** 已有审查记录数 */
  reviewCount: number
  /** 最近审查意见（简略） */
  lastReviewOpinion?: string
  /** 历史审查记录列表 */
  reviewHistory?: TopicReviewRecordVO[]
}

/**
 * 课题审查记录VO
 */
export interface TopicReviewRecordVO {
  /** 审查记录ID */
  reviewId: string
  /** 课题ID */
  topicId: string
  /** 课题名称 */
  topicTitle?: string
  /** 审查阶段（1-预审 2-初审 3-终审） */
  reviewStage: ReviewStage
  /** 审查阶段名称 */
  reviewStageName: string
  /** 审查人ID */
  reviewerId: string
  /** 审查人角色代码 */
  reviewerRole: string
  /** 审查人角色名称 */
  reviewerRoleName: string
  /** 审查人姓名 */
  reviewerName: string
  /** 审查结果（1-通过 2-需修改 3-不通过） */
  reviewResult: ReviewResult
  /** 审查结果名称 */
  reviewResultName: string
  /** 审查意见 */
  reviewOpinion?: string
  /** 是否批量审查 */
  isBatchReview: boolean
  /** 是否被修改过 */
  isModified: boolean
  /** 修改人姓名 */
  modifiedByName?: string
  /** 修改时间 */
  modifiedTime?: string
  /** 审查时间 */
  createTime: string
}

/**
 * 综合意见VO
 */
export interface GeneralOpinionVO {
  /** 意见ID */
  opinionId: string
  /** 审查阶段 */
  reviewStage: ReviewStage
  /** 审查阶段名称 */
  reviewStageName: string
  /** 专业方向 */
  guidanceDirection: string
  /** 意见内容 */
  opinionContent: string
  /** 提交人ID */
  submitterId: string
  /** 提交人姓名 */
  submitterName: string
  /** 提交人角色名称 */
  submitterRoleName: string
  /** 提交时间 */
  createTime: string
}

/**
 * 批量审查结果VO
 */
export interface BatchReviewResultVO {
  /** 成功数量 */
  successCount: number
  /** 失败数量 */
  failCount: number
  /** 失败详情列表 */
  failDetails: BatchReviewFailDetail[]
}

/**
 * 批量审查失败详情
 */
export interface BatchReviewFailDetail {
  /** 课题ID */
  topicId: string
  /** 课题名称 */
  topicTitle?: string
  /** 失败原因 */
  reason: string
}

/**
 * 教师通过终审课题统计VO
 */
export interface TeacherPassedCountVO {
  /** 教师ID */
  teacherId: string
  /** 教师姓名 */
  teacherName: string
  /** 已通过终审课题数 */
  passedCount: number
  /** 剩余可提交数（上限18个） */
  remainingCount: number
  /** 是否已达上限 */
  reachedLimit: boolean
}

/**
 * 审查查询参数VO
 */
export interface ReviewQueryVO {
  /** 课题名称（模糊查询） */
  topicTitle?: string
  /** 课题大类 */
  topicCategory?: TopicCategory
  /** 课题类型 */
  topicType?: TopicType
  /** 指导方向/专业 */
  guidanceDirection?: string
  /** 归属企业ID */
  enterpriseId?: string
  /** 创建人姓名（模糊查询） */
  creatorName?: string
  /** 审查状态 */
  reviewStatus?: number
  /** 页码 */
  pageNum?: number
  /** 每页大小 */
  pageSize?: number
}

/**
 * 单个课题审批请求DTO
 */
export interface ReviewTopicDTO {
  /** 课题ID */
  topicId: string
  /** 审查结果（1-通过 2-需修改 3-不通过） */
  reviewResult: ReviewResult
  /** 审查意见（最多500字） */
  reviewOpinion?: string
}

/**
 * 批量课题审批请求DTO
 */
export interface BatchReviewDTO {
  /** 课题ID列表 */
  topicIds: string[]
  /** 审查结果（1-通过 2-需修改 3-不通过） */
  reviewResult: ReviewResult
  /** 批量审查意见（最多500字） */
  reviewOpinion?: string
}

/**
 * 修改审查结果请求DTO
 */
export interface ModifyReviewDTO {
  /** 审查记录ID */
  reviewId: string
  /** 新的审查结果（1-通过 2-需修改 3-不通过） */
  newReviewResult: ReviewResult
  /** 新的审查意见（最多500字） */
  newReviewOpinion?: string
}

/**
 * 综合意见提交请求DTO
 */
export interface GeneralOpinionDTO {
  /** 审查阶段（2-初审 3-终审） */
  reviewStage: ReviewStage
  /** 专业方向 */
  guidanceDirection: string
  /** 意见内容（最多200字） */
  opinionContent: string
}

