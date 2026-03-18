/**
 * 文档管理类型定义
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */

/**
 * 文档类型枚举
 */
export enum DocumentType {
  /** 项目代码 */
  PROJECT_CODE = 1,
  /** 论文文档 */
  THESIS_DOCUMENT = 2,
  /** 开题报告 */
  OPENING_REPORT = 3,
  /** 中期检查表 */
  MIDTERM_CHECK = 4,
  /** 教师批注文档 */
  TEACHER_ANNOTATION = 5
}

/**
 * 文档类型选项
 */
export const DocumentTypeOptions = [
  { value: DocumentType.PROJECT_CODE, label: '项目代码' },
  { value: DocumentType.THESIS_DOCUMENT, label: '论文文档' },
  { value: DocumentType.OPENING_REPORT, label: '开题报告' },
  { value: DocumentType.MIDTERM_CHECK, label: '中期检查表' },
  { value: DocumentType.TEACHER_ANNOTATION, label: '教师批注文档' }
]

/**
 * 获取文档类型描述
 */
export function getDocumentTypeLabel(type: number): string {
  const option = DocumentTypeOptions.find(o => o.value === type)
  return option ? option.label : '未知类型'
}

/**
 * 文档信息VO
 */
export interface DocumentInfoVO {
  documentId: string
  studentId: string
  studentName?: string
  topicId: string
  topicTitle?: string
  documentType: number
  documentTypeDesc?: string
  fileName: string
  filePath: string
  fileSize: number
  fileSizeDesc?: string
  fileSuffix: string
  uploaderId: string
  uploaderName?: string
  version: number
  isLatest: number
  remark?: string
  createTime: string
  updateTime?: string
}

/**
 * 文档预览VO
 */
export interface DocumentPreviewVO {
  documentId: string
  fileName: string
  fileSuffix: string
  previewUrl: string
  expiresIn: number
  supportPreview: boolean
}

/**
 * 课题文档总览VO
 */
export interface TopicDocumentOverviewVO {
  topicId: string
  topicTitle: string
  studentId: string
  studentName?: string
  projectCodeDocs: DocumentInfoVO[]
  thesisDocs: DocumentInfoVO[]
  openingReportDocs: DocumentInfoVO[]
  midtermCheckDocs: DocumentInfoVO[]
  teacherAnnotationDocs: DocumentInfoVO[]
  totalCount: number
}

/**
 * 文档查询参数
 */
export interface DocumentQueryParams {
  studentId?: string
  topicId?: string
  documentType?: number
  latestOnly?: boolean
  keyword?: string
  pageNum?: number
  pageSize?: number
}

/**
 * 上传文档参数
 */
export interface UploadDocumentParams {
  file: File
  documentType: number
  remark?: string
  studentId?: string  // 教师上传批注时必填
}
