/**
 * 课题双选API接口
 * 覆盖：学生选报 / 企业教师确认人选 / 企业负责人双选审核
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */

import request from './request'
import type {
  ApplyTopicDTO,
  AssignTeacherDTO,
  SelectionForTeacherVO,
  SelectionForUnivTeacherVO,
  SelectionOverviewVO,
  SelectionQueryParams,
  TeacherAssignmentVO,
  TopicForSelectionVO,
  TopicSelectionVO,
  UnivTeacherPairingVO,
  UnselectedStudentVO
} from '@/types/topicSelection'
import type { PageResult } from '@/types/common'

/**
 * 课题双选 API（学生选报 / 教师确认 / 负责人审核）
 */
export const topicSelectionApi = {

  // ==================== 学生选报 ====================

  /**
   * 查询可选课题列表（终审通过，分页）
   * @param params 筛选参数
   */
  getAvailableTopics(params: SelectionQueryParams) {
    return request.get<PageResult<TopicForSelectionVO>>('/topic-selection/available', { params })
  },

  /**
   * 选报课题（最多3个，需先完善手机号）
   * @param data 选报参数
   */
  applyTopic(data: ApplyTopicDTO) {
    return request.post<TopicSelectionVO>('/topic-selection', data)
  },

  /**
   * 删除选报记录（仅落选状态可删除）
   * @param selectionId 选报记录ID
   */
  deleteSelection(selectionId: string) {
    return request.delete<void>(`/topic-selection/${selectionId}`)
  },

  /**
   * 查询当前学生的选报记录列表
   */
  getMySelections() {
    return request.get<TopicSelectionVO[]>('/topic-selection/my')
  },

  // ==================== 教师确认子模块 ====================

  /**
   * 获取选报了自己课题的学生列表（企业教师）
   * @param selectionStatus 状态过滤：0=待确认 1=中选 2=落选，不传返回全部
   */
  getSelectionsForTeacher(selectionStatus?: number) {
    return request.get<SelectionForTeacherVO[]>('/topic-selection/teacher', {
      params: selectionStatus !== undefined ? { selectionStatus } : {}
    })
  },

  /**
   * 确认人选（待确认 → 中选，自动落选该学生其他待确认记录）
   * @param selectionId 选报记录ID
   */
  confirmSelection(selectionId: string) {
    return request.post<SelectionForTeacherVO>(`/topic-selection/${selectionId}/confirm`)
  },

  /**
   * 拒绝人选（待确认 → 落选）
   * @param selectionId 选报记录ID
   */
  rejectSelection(selectionId: string) {
    return request.post<SelectionForTeacherVO>(`/topic-selection/${selectionId}/reject`)
  },

  /**
   * 导出已确认学生信息（Excel 文件下载）
   * 调用方需通过 window.open 或创建 <a> 标签触发下载
   */
  getExportConfirmedUrl() {
    return '/api/topic-selection/teacher/export'
  },

  // ==================== 双选审核子模块 ====================

  /**
   * 获取双选结果概览（课题维度，企业负责人）
   */
  getSelectionOverview() {
    return request.get<SelectionOverviewVO[]>('/topic-selection/leader/overview')
  },

  /**
   * 获取未选报任何课题的学生列表
   */
  getUnselectedStudents() {
    return request.get<UnselectedStudentVO[]>('/topic-selection/leader/unselected')
  },

  /**
   * 导出全部选题信息 URL（Excel 文件下载）
   */
  getExportSelectionUrl() {
    return '/api/topic-selection/leader/export'
  },

  /**
   * 指派企业指导教师（校外协同开发课题中选学生）
   * @param data 指派参数
   */
  assignTeacher(data: AssignTeacherDTO) {
    return request.post<TeacherAssignmentVO>('/topic-selection/leader/assign', data)
  },

  /**
   * 取消指派
   * @param assignmentId 指派记录ID
   */
  cancelAssignment(assignmentId: string) {
    return request.delete<void>(`/topic-selection/leader/assign/${assignmentId}`)
  },

  /**
   * 查询企业内指派记录列表
   */
  getAssignmentList() {
    return request.get<TeacherAssignmentVO[]>('/topic-selection/leader/assignments')
  },

  // ==================== 高校教师查看选题 ====================

  /**
   * 查询高校教师的配对关系（与是否有选报数据无关，配对存在即可查到）
   */
  getUnivTeacherPairings() {
    return request.get<UnivTeacherPairingVO[]>('/topic-selection/univ-teacher/pairings')
  },

  /**
   * 高校教师查看配对企业教师名下学生的选报结果
   * @param selectionStatus 状态过滤：0=待确认 1=中选 2=落选，不传返回全部
   */
  getSelectionsForUnivTeacher(selectionStatus?: number) {
    return request.get<SelectionForUnivTeacherVO[]>('/topic-selection/univ-teacher', {
      params: selectionStatus !== undefined ? { selectionStatus } : {}
    })
  },

  /**
   * 高校教师导出指导学生选题结果 URL（Excel 文件下载）
   */
  getExportUnivTeacherUrl() {
    return '/api/topic-selection/univ-teacher/export'
  }
}
