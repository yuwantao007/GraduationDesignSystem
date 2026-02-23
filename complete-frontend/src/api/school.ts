/**
 * 学校管理 API
 * @description 学校信息 CRUD、状态管理等接口
 * @author YuWan
 * @date 2026-02-22
 */
import request from './request'
import type { PageResult } from '@/types/common'
import type {
  SchoolVO,
  SchoolQueryVO,
  CreateSchoolDTO,
  UpdateSchoolDTO
} from '@/types/school'

/**
 * 学校管理 API
 */
export const schoolApi = {
  /**
   * 分页查询学校列表
   * @param params - 查询参数
   */
  getSchoolList(params: SchoolQueryVO) {
    return request.get<PageResult<SchoolVO>>('/school/list', { params })
  },

  /**
   * 获取全部启用学校（下拉选择用）
   */
  getAllSchools() {
    return request.get<SchoolVO[]>('/school/all')
  },

  /**
   * 获取学校详情
   * @param schoolId - 学校ID
   */
  getSchoolDetail(schoolId: string) {
    return request.get<SchoolVO>(`/school/${schoolId}`)
  },

  /**
   * 创建学校
   * @param data - 创建学校请求参数
   */
  createSchool(data: CreateSchoolDTO) {
    return request.post<SchoolVO>('/school', data)
  },

  /**
   * 更新学校信息
   * @param schoolId - 学校ID
   * @param data - 更新学校请求参数
   */
  updateSchool(schoolId: string, data: UpdateSchoolDTO) {
    return request.put<SchoolVO>(`/school/${schoolId}`, data)
  },

  /**
   * 删除学校
   * @param schoolId - 学校ID
   */
  deleteSchool(schoolId: string) {
    return request.delete(`/school/${schoolId}`)
  },

  /**
   * 更新学校状态（启用/禁用）
   * @param schoolId - 学校ID
   * @param status - 学校状态（0: 禁用, 1: 正常）
   */
  updateSchoolStatus(schoolId: string, status: number) {
    return request.put(`/school/${schoolId}/status`, null, { params: { status } })
  }
}
