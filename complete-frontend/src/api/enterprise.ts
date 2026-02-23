/**
 * 企业管理 API
 * @description 企业信息 CRUD、状态管理等接口
 * @author YuWan
 * @date 2026-02-22
 */
import request from './request'
import type { PageResult } from '@/types/common'
import type {
  EnterpriseVO,
  EnterpriseQueryVO,
  CreateEnterpriseDTO,
  UpdateEnterpriseDTO
} from '@/types/enterprise'

/**
 * 企业管理 API
 */
export const enterpriseApi = {
  /**
   * 分页查询企业列表
   * @param params - 查询参数
   */
  getEnterpriseList(params: EnterpriseQueryVO) {
    return request.get<PageResult<EnterpriseVO>>('/enterprise/list', { params })
  },

  /**
   * 获取全部启用企业（下拉选择用）
   */
  getAllEnterprises() {
    return request.get<EnterpriseVO[]>('/enterprise/all')
  },

  /**
   * 获取企业详情
   * @param enterpriseId - 企业ID
   */
  getEnterpriseDetail(enterpriseId: string) {
    return request.get<EnterpriseVO>(`/enterprise/${enterpriseId}`)
  },

  /**
   * 创建企业
   * @param data - 创建企业请求参数
   */
  createEnterprise(data: CreateEnterpriseDTO) {
    return request.post<EnterpriseVO>('/enterprise', data)
  },

  /**
   * 更新企业信息
   * @param enterpriseId - 企业ID
   * @param data - 更新企业请求参数
   */
  updateEnterprise(enterpriseId: string, data: UpdateEnterpriseDTO) {
    return request.put<EnterpriseVO>(`/enterprise/${enterpriseId}`, data)
  },

  /**
   * 删除企业
   * @param enterpriseId - 企业ID
   */
  deleteEnterprise(enterpriseId: string) {
    return request.delete(`/enterprise/${enterpriseId}`)
  },

  /**
   * 更新企业状态（启用/禁用）
   * @param enterpriseId - 企业ID
   * @param status - 企业状态（0: 禁用, 1: 正常）
   */
  updateEnterpriseStatus(enterpriseId: string, status: number) {
    return request.put(`/enterprise/${enterpriseId}/status`, null, { params: { status } })
  }
}
