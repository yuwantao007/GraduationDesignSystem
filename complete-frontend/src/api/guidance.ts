/**
 * 指导记录API接口
 * 覆盖：教师指导 / 学生查看 / 企业负责人概览
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-16
 */

import request from './request'
import type {
  CreateGuidanceDTO,
  GuidanceQueryParams,
  GuidanceRecordVO,
  GuidanceListVO,
  GuidanceStudentVO
} from '@/types/guidance'
import type { PageResult } from '@/types/common'

/**
 * 指导记录 API
 */
export const guidanceApi = {

  // ==================== 教师操作 ====================

  /**
   * 新增指导记录（企业教师/高校教师）
   * @param data 创建参数
   */
  createGuidanceRecord(data: CreateGuidanceDTO) {
    return request.post<GuidanceRecordVO>('/guidance/record', data)
  },

  /**
   * 删除指导记录（本人可删）
   * @param recordId 记录ID
   */
  deleteGuidanceRecord(recordId: string) {
    return request.delete<void>(`/guidance/record/${recordId}`)
  },

  /**
   * 我的学生列表（教师视角，含最新指导时间）
   */
  getMyStudents() {
    return request.get<GuidanceStudentVO[]>('/guidance/record/my-students')
  },

  /**
   * 查看某学生的全部指导记录
   * @param studentId 学生ID
   */
  getStudentGuidanceRecords(studentId: string) {
    return request.get<GuidanceListVO[]>(`/guidance/record/student/${studentId}`)
  },

  // ==================== 学生操作 ====================

  /**
   * 我的被指导记录（学生视角）
   */
  getMyGuidanceRecords() {
    return request.get<GuidanceListVO[]>('/guidance/record/my')
  },

  // ==================== 企业负责人操作 ====================

  /**
   * 指导记录总览（企业负责人，支持筛选分页）
   * @param params 查询参数
   */
  getLeaderGuidanceOverview(params: GuidanceQueryParams) {
    return request.get<PageResult<GuidanceListVO>>('/guidance/record/leader/list', { params })
  },

  /**
   * 导出指导记录 Excel URL（企业负责人）
   * 调用方需通过 window.open 或创建 <a> 标签触发下载
   */
  getExportGuidanceUrl() {
    return '/api/guidance/record/leader/export'
  },

  // ==================== 通用查询 ====================

  /**
   * 指导记录详情
   * @param recordId 记录ID
   */
  getGuidanceRecordDetail(recordId: string) {
    return request.get<GuidanceRecordVO>(`/guidance/record/${recordId}`)
  }
}
