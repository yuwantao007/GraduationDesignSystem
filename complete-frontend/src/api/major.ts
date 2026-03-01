/**
 * 专业管理 API
 * @description 专业方向和专业的CRUD接口
 * @author YuWan
 * @date 2026-03-01
 */
import request from './request'
import type {
  MajorDirectionVO,
  MajorVO,
  MajorTreeVO,
  MajorCascadeVO,
  MajorDirectionDTO,
  MajorDTO
} from '@/types/major'

/**
 * 专业管理 API
 */
export const majorApi = {
  // ==================== 树型结构查询 ====================

  /**
   * 获取专业树型结构
   * @param enterpriseId - 企业ID（可选，系统管理员可指定）
   * @param status - 状态筛选（可选）
   */
  getMajorTree(enterpriseId?: string, status?: number) {
    return request.get<MajorTreeVO[]>('/major/tree', {
      params: { enterpriseId, status }
    })
  },

  /**
   * 获取级联选择器数据
   * @param enterpriseId - 企业ID（可选）
   */
  getCascadeData(enterpriseId?: string) {
    return request.get<MajorCascadeVO[]>('/major/cascade', {
      params: { enterpriseId }
    })
  },

  // ==================== 专业方向管理 ====================

  /**
   * 获取专业方向列表（下拉选择用）
   * @param enterpriseId - 企业ID（可选）
   */
  getDirectionList(enterpriseId?: string) {
    return request.get<MajorDirectionVO[]>('/major/direction/list', {
      params: { enterpriseId }
    })
  },

  /**
   * 获取专业方向详情
   * @param directionId - 专业方向ID
   */
  getDirectionDetail(directionId: string) {
    return request.get<MajorDirectionVO>(`/major/direction/${directionId}`)
  },

  /**
   * 添加专业方向
   * @param data - 专业方向表单数据
   */
  addDirection(data: MajorDirectionDTO) {
    return request.post<MajorDirectionVO>('/major/direction', data)
  },

  /**
   * 编辑专业方向
   * @param directionId - 专业方向ID
   * @param data - 专业方向表单数据
   */
  updateDirection(directionId: string, data: MajorDirectionDTO) {
    return request.put<MajorDirectionVO>(`/major/direction/${directionId}`, data)
  },

  /**
   * 删除专业方向
   * @param directionId - 专业方向ID
   */
  deleteDirection(directionId: string) {
    return request.delete(`/major/direction/${directionId}`)
  },

  /**
   * 切换专业方向状态
   * @param directionId - 专业方向ID
   * @param status - 目标状态（0-禁用 1-启用）
   */
  updateDirectionStatus(directionId: string, status: number) {
    return request.put(`/major/direction/${directionId}/status`, null, {
      params: { status }
    })
  },

  // ==================== 专业管理 ====================

  /**
   * 获取专业详情
   * @param majorId - 专业ID
   */
  getMajorDetail(majorId: string) {
    return request.get<MajorVO>(`/major/${majorId}`)
  },

  /**
   * 添加专业
   * @param data - 专业表单数据
   */
  addMajor(data: MajorDTO) {
    return request.post<MajorVO>('/major', data)
  },

  /**
   * 编辑专业
   * @param majorId - 专业ID
   * @param data - 专业表单数据
   */
  updateMajor(majorId: string, data: MajorDTO) {
    return request.put<MajorVO>(`/major/${majorId}`, data)
  },

  /**
   * 删除专业
   * @param majorId - 专业ID
   */
  deleteMajor(majorId: string) {
    return request.delete(`/major/${majorId}`)
  },

  /**
   * 切换专业状态
   * @param majorId - 专业ID
   * @param status - 目标状态（0-禁用 1-启用）
   */
  updateMajorStatus(majorId: string, status: number) {
    return request.put(`/major/${majorId}/status`, null, {
      params: { status }
    })
  }
}
