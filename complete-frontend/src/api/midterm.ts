/**
 * 中期检查API
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */

import request, { type ApiResponse } from './request'
import type { PageResult } from '@/types/common'
import type {
  MidtermCheckVO,
  MidtermCheckListVO,
  CreateMidtermCheckDTO,
  ReviewMidtermCheckDTO,
  MidtermCheckQueryParams
} from '@/types/midterm'

/**
 * 中期检查API
 */
export const midtermApi = {
  // ==================== 企业教师接口 ====================

  /**
   * 企业教师-创建/编辑中期检查表
   * @param data 创建/编辑数据
   */
  saveCheck(data: CreateMidtermCheckDTO): Promise<ApiResponse<string>> {
    return request.post('/midterm/enterprise/save', data)
  },

  /**
   * 企业教师-提交中期检查表
   * @param checkId 检查表ID
   */
  submitCheck(checkId: string): Promise<ApiResponse<boolean>> {
    return request.post(`/midterm/enterprise/submit/${checkId}`)
  },

  /**
   * 企业教师-查询负责的中期检查列表
   * @param params 查询参数
   */
  getEnterpriseList(params?: MidtermCheckQueryParams): Promise<ApiResponse<PageResult<MidtermCheckListVO>>> {
    return request.get('/midterm/enterprise/list', { params })
  },

  /**
   * 企业教师-获取中期检查表详情
   * @param checkId 检查表ID
   */
  getEnterpriseDetail(checkId: string): Promise<ApiResponse<MidtermCheckVO>> {
    return request.get(`/midterm/enterprise/detail/${checkId}`)
  },

  // ==================== 高校教师接口 ====================

  /**
   * 高校教师-审查中期检查表
   * @param data 审查数据
   */
  reviewCheck(data: ReviewMidtermCheckDTO): Promise<ApiResponse<boolean>> {
    return request.post('/midterm/univ/review', data)
  },

  /**
   * 高校教师-查询待审查的中期检查列表
   * @param params 查询参数
   */
  getUnivList(params?: MidtermCheckQueryParams): Promise<ApiResponse<PageResult<MidtermCheckListVO>>> {
    return request.get('/midterm/univ/list', { params })
  },

  /**
   * 高校教师-获取中期检查表详情
   * @param checkId 检查表ID
   */
  getUnivDetail(checkId: string): Promise<ApiResponse<MidtermCheckVO>> {
    return request.get(`/midterm/univ/detail/${checkId}`)
  },

  // ==================== 学生接口 ====================

  /**
   * 学生-查看自己的中期检查表
   */
  getStudentCheck(): Promise<ApiResponse<MidtermCheckVO>> {
    return request.get('/midterm/student/my')
  },

  // ==================== 管理员接口 ====================

  /**
   * 管理员-查询所有中期检查列表
   * @param params 查询参数
   */
  getAdminList(params?: MidtermCheckQueryParams): Promise<ApiResponse<PageResult<MidtermCheckListVO>>> {
    return request.get('/midterm/admin/list', { params })
  },

  /**
   * 管理员-获取中期检查表详情
   * @param checkId 检查表ID
   */
  getAdminDetail(checkId: string): Promise<ApiResponse<MidtermCheckVO>> {
    return request.get(`/midterm/admin/detail/${checkId}`)
  }
}
