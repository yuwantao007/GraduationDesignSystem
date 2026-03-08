/**
 * 教师配对管理 API
 * @description 方向级分配、精确配对、覆盖检查等接口
 * @author YuWan
 * @date 2026-03-08
 */
import request from './request'
import type {
  UnivTeacherMajorVO,
  UnivTeacherMajorDTO,
  TeacherRelationVO,
  TeacherRelationDTO,
  TeacherCoverageVO,
  CoverageStats
} from '@/types/teacherRelation'

/**
 * 教师配对管理 API
 */
export const teacherRelationApi = {
  // ==================== 方向级分配 ====================

  /**
   * 查询方向级分配列表
   * @param enterpriseId - 企业ID（可选）
   * @param cohort - 届别（可选）
   */
  listMajorAssignments(enterpriseId?: string, cohort?: string) {
    const params: Record<string, string> = {}
    if (enterpriseId) params.enterpriseId = enterpriseId
    if (cohort) params.cohort = cohort
    return request.get<UnivTeacherMajorVO[]>('/teacher-relation/major-assignment/list', { params })
  },

  /**
   * 新增方向级分配
   * @param data - 分配参数
   */
  addMajorAssignment(data: UnivTeacherMajorDTO) {
    return request.post<UnivTeacherMajorVO>('/teacher-relation/major-assignment', data)
  },

  /**
   * 编辑方向级分配
   * @param id - 分配ID
   * @param data - 修改参数
   */
  updateMajorAssignment(id: string, data: UnivTeacherMajorDTO) {
    return request.put<UnivTeacherMajorVO>(`/teacher-relation/major-assignment/${id}`, data)
  },

  /**
   * 删除方向级分配
   * @param id - 分配ID
   */
  deleteMajorAssignment(id: string) {
    return request.delete(`/teacher-relation/major-assignment/${id}`)
  },

  // ==================== 精确配对 ====================

  /**
   * 查询精确配对列表
   * @param enterpriseId - 企业ID（可选）
   * @param cohort - 届别（可选）
   */
  listTeacherPairs(enterpriseId?: string, cohort?: string) {
    const params: Record<string, string> = {}
    if (enterpriseId) params.enterpriseId = enterpriseId
    if (cohort) params.cohort = cohort
    return request.get<TeacherRelationVO[]>('/teacher-relation/teacher-pair/list', { params })
  },

  /**
   * 新增精确配对
   * @param data - 配对参数
   */
  addTeacherPair(data: TeacherRelationDTO) {
    return request.post<TeacherRelationVO>('/teacher-relation/teacher-pair', data)
  },

  /**
   * 编辑精确配对
   * @param relationId - 配对ID
   * @param data - 修改参数
   */
  updateTeacherPair(relationId: string, data: TeacherRelationDTO) {
    return request.put<TeacherRelationVO>(`/teacher-relation/teacher-pair/${relationId}`, data)
  },

  /**
   * 删除精确配对
   * @param relationId - 配对ID
   */
  deleteTeacherPair(relationId: string) {
    return request.delete(`/teacher-relation/teacher-pair/${relationId}`)
  },

  // ==================== 覆盖检查 ====================

  /**
   * 获取配对覆盖情况列表
   * @param enterpriseId - 企业ID（可选）
   * @param cohort - 届别（可选）
   */
  getCoverageList(enterpriseId?: string, cohort?: string) {
    const params: Record<string, string> = {}
    if (enterpriseId) params.enterpriseId = enterpriseId
    if (cohort) params.cohort = cohort
    return request.get<TeacherCoverageVO[]>('/teacher-relation/coverage/list', { params })
  },

  /**
   * 获取配对覆盖率统计
   * @param cohort - 届别（可选）
   */
  getCoverageStats(cohort?: string) {
    const params: Record<string, string> = {}
    if (cohort) params.cohort = cohort
    return request.get<CoverageStats>('/teacher-relation/coverage/stats', { params })
  },

  /**
   * 查找企业教师对应的高校教师（双层查找）
   * @param enterpriseTeacherId - 企业教师ID
   * @param directionId - 专业方向ID（可选）
   * @param cohort - 届别（可选）
   */
  findUniversityTeacher(enterpriseTeacherId: string, directionId?: string, cohort?: string) {
    const params: Record<string, string> = { enterpriseTeacherId }
    if (directionId) params.directionId = directionId
    if (cohort) params.cohort = cohort
    return request.get<string>('/teacher-relation/find-univ-teacher', { params })
  }
}
