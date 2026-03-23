/**
 * 开题答辩管理模块API
 * @since 2026-03-17
 */

import request from './request'
import type { ApiResponse } from './request'
import type {
  DefenseArrangementVO,
  CreateArrangementDTO,
  UpdateArrangementDTO,
  ArrangementQueryDTO,
  OpeningTaskBookVO,
  SaveTaskBookDTO,
  OpeningReportVO,
  SubmitReportDTO,
  ReportQueryDTO,
  PageResult
} from '@/types/defense'

/**
 * 开题答辩管理API
 */
export const defenseApi = {
  // ==================== 答辩安排管理 ====================

  /**
   * 创建答辩安排
   */
  createArrangement(data: CreateArrangementDTO): Promise<ApiResponse<string>> {
    return request.post('/defense/arrangement', data)
  },

  /**
   * 更新答辩安排
   */
  updateArrangement(data: UpdateArrangementDTO): Promise<ApiResponse<boolean>> {
    return request.put('/defense/arrangement', data)
  },

  /**
   * 删除答辩安排
   */
  deleteArrangement(arrangementId: string): Promise<ApiResponse<boolean>> {
    return request.delete(`/defense/arrangement/${arrangementId}`)
  },

  /**
   * 分页查询答辩安排列表
   */
  pageArrangements(params: ArrangementQueryDTO): Promise<ApiResponse<PageResult<DefenseArrangementVO>>> {
    return request.get('/defense/arrangement/page', { params })
  },

  /**
   * 获取答辩安排详情
   */
  getArrangementDetail(arrangementId: string): Promise<ApiResponse<DefenseArrangementVO>> {
    return request.get(`/defense/arrangement/${arrangementId}`, { silentError: true })
  },

  // ==================== 开题任务书管理 ====================

  /**
   * 保存任务书
   */
  saveTaskBook(data: SaveTaskBookDTO): Promise<ApiResponse<string>> {
    return request.post('/defense/taskbook', data)
  },

  /**
   * 根据学生ID获取任务书
   */
  getTaskBookByStudent(studentId: string): Promise<ApiResponse<OpeningTaskBookVO>> {
    return request.get(`/defense/taskbook/student/${studentId}`)
  },

  /**
   * 根据任务书ID获取详情
   */
  getTaskBookDetail(taskBookId: string): Promise<ApiResponse<OpeningTaskBookVO>> {
    return request.get(`/defense/taskbook/${taskBookId}`)
  },

  // ==================== 开题报告管理 ====================

  /**
   * 学生提交开题报告
   */
  submitReport(data: SubmitReportDTO): Promise<ApiResponse<string>> {
    return request.post('/defense/report', data)
  },

  /**
   * 获取我的开题报告
   */
  getMyReport(): Promise<ApiResponse<OpeningReportVO>> {
    return request.get('/defense/report/my')
  },

  /**
   * 分页查询开题报告列表
   */
  pageReports(params: ReportQueryDTO): Promise<ApiResponse<PageResult<OpeningReportVO>>> {
    return request.get('/defense/report/page', { params })
  },

  /**
   * 获取开题报告详情
   */
  getReportDetail(reportId: string): Promise<ApiResponse<OpeningReportVO>> {
    return request.get(`/defense/report/${reportId}`)
  },

}
