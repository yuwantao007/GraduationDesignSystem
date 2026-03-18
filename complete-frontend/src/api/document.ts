/**
 * 文档管理API
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */

import request from './request'
import type { ApiResponse } from '@/types/common'
import type {
  DocumentInfoVO,
  DocumentPreviewVO,
  TopicDocumentOverviewVO
} from '@/types/document'

/**
 * 文档管理API
 */
export const documentApi = {
  /**
   * 上传文档（学生）
   * @param file 文件
   * @param documentType 文档类型
   * @param remark 备注
   */
  uploadDocument(file: File, documentType: number, remark?: string): Promise<ApiResponse<DocumentInfoVO>> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('documentType', documentType.toString())
    if (remark) {
      formData.append('remark', remark)
    }
    return request.post('/document/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 教师上传批注文档
   * @param file 文件
   * @param studentId 学生ID
   * @param remark 备注
   */
  uploadTeacherAnnotation(file: File, studentId: string, remark?: string): Promise<ApiResponse<DocumentInfoVO>> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('studentId', studentId)
    if (remark) {
      formData.append('remark', remark)
    }
    return request.post('/document/upload/teacher', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 查看某学生的文档列表
   * @param studentId 学生ID
   * @param documentType 文档类型（可选）
   * @param latestOnly 是否只显示最新版本
   */
  getStudentDocuments(
    studentId: string,
    documentType?: number,
    latestOnly?: boolean
  ): Promise<ApiResponse<DocumentInfoVO[]>> {
    return request.get(`/document/student/${studentId}`, {
      params: { documentType, latestOnly }
    })
  },

  /**
   * 我的文档列表（学生视角）
   * @param documentType 文档类型（可选）
   */
  getMyDocuments(documentType?: number): Promise<ApiResponse<DocumentInfoVO[]>> {
    return request.get('/document/my', {
      params: { documentType }
    })
  },

  /**
   * 获取文档预览URL
   * @param documentId 文档ID
   */
  getPreviewUrl(documentId: string): Promise<ApiResponse<DocumentPreviewVO>> {
    return request.get(`/document/${documentId}/preview`)
  },

  /**
   * 下载文档（返回blob）
   * @param documentId 文档ID
   */
  downloadDocument(documentId: string): Promise<Blob> {
    return request.get(`/document/${documentId}/download`, {
      responseType: 'blob'
    })
  },

  /**
   * 删除文档
   * @param documentId 文档ID
   */
  deleteDocument(documentId: string): Promise<ApiResponse<void>> {
    return request.delete(`/document/${documentId}`)
  },

  /**
   * 设置为最新版本
   * @param documentId 文档ID
   */
  setAsLatest(documentId: string): Promise<ApiResponse<void>> {
    return request.put(`/document/${documentId}/latest`)
  },

  /**
   * 课题文档总览
   * @param topicId 课题ID
   */
  getTopicDocumentOverview(topicId: string): Promise<ApiResponse<TopicDocumentOverviewVO>> {
    return request.get(`/document/topic/${topicId}`)
  }
}
