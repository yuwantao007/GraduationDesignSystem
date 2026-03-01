/**
 * 课题审查API接口
 * 对应后端TopicReviewController的接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-22
 */

import request from './request'
import type {
  TopicReviewListVO,
  TopicReviewRecordVO,
  ReviewTopicDTO,
  BatchReviewDTO,
  BatchReviewResultVO,
  TeacherPassedCountVO,
  GeneralOpinionDTO,
  GeneralOpinionVO,
  ModifyReviewDTO,
  ReviewQueryVO
} from '@/types/topic'
import type { PageResult } from '@/types/common'

/**
 * 课题审查相关API
 */
export const topicReviewApi = {
  /**
   * 获取待审查课题列表
   * @param params 查询参数
   * @returns 分页待审查课题列表
   */
  getPendingReviewList(params: ReviewQueryVO) {
    return request.get<PageResult<TopicReviewListVO>>('/topic/review/pending', { params })
  },

  /**
   * 单个课题审批
   * @param data 审批请求参数
   * @returns 审查记录
   */
  reviewTopic(data: ReviewTopicDTO) {
    return request.post<TopicReviewRecordVO>('/topic/review', data)
  },

  /**
   * 批量课题审批
   * @param data 批量审批请求参数
   * @returns 批量审批结果
   */
  batchReviewTopics(data: BatchReviewDTO) {
    return request.post<BatchReviewResultVO>('/topic/review/batch', data)
  },

  /**
   * 获取课题审查历史记录
   * @param topicId 课题ID
   * @returns 审查记录列表
   */
  getReviewHistory(topicId: string) {
    return request.get<TopicReviewRecordVO[]>(`/topic/review/${topicId}/history`)
  },

  /**
   * 提交综合意见
   * @param data 综合意见请求参数
   * @returns 综合意见记录
   */
  submitGeneralOpinion(data: GeneralOpinionDTO) {
    return request.post<GeneralOpinionVO>('/topic/review/general-opinion', data)
  },

  /**
   * 获取综合意见列表
   * @param guidanceDirection 专业方向（可选）
   * @returns 综合意见列表
   */
  getGeneralOpinions(guidanceDirection?: string) {
    return request.get<GeneralOpinionVO[]>('/topic/review/general-opinions', {
      params: guidanceDirection ? { guidanceDirection } : undefined
    })
  },

  /**
   * 修改审查结果
   * @param data 修改审查请求参数
   * @returns 修改后的审查记录
   */
  modifyReviewResult(data: ModifyReviewDTO) {
    return request.put<TopicReviewRecordVO>('/topic/review/modify', data)
  },

  /**
   * 删除综合意见
   * @param opinionId 意见ID
   * @returns 操作结果
   */
  deleteGeneralOpinion(opinionId: string) {
    return request.delete<void>(`/topic/review/general-opinion/${opinionId}`)
  },

  /**
   * 获取教师通过终审课题数统计
   * @param teacherId 教师ID（可选，不传则查当前用户）
   * @returns 统计信息
   */
  getTeacherPassedCount(teacherId?: string) {
    return request.get<TeacherPassedCountVO>('/topic/review/stats/passed-count', {
      params: teacherId ? { teacherId } : undefined
    })
  },

  /**
   * 检查课题是否可以提交
   * @param topicId 课题ID
   * @returns 是否可提交
   */
  checkCanSubmit(topicId: string) {
    return request.get<boolean>('/topic/review/check-submit', {
      params: { topicId }
    })
  },

  /**
   * 检查当前用户是否可以审查指定课题
   * @param topicId 课题ID
   * @returns 是否可审查
   */
  checkCanReview(topicId: string) {
    return request.get<boolean>(`/topic/review/${topicId}/can-review`)
  },

  /**
   * 获取可修改的审查记录
   * @param topicId 课题ID
   * @returns 可修改的审查记录（如果存在）
   */
  getModifiableRecord(topicId: string) {
    return request.get<TopicReviewRecordVO | null>(`/topic/review/${topicId}/modifiable-record`)
  }
}

export default topicReviewApi
