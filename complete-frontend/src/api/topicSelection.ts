/**
 * 课题选报API接口
 * 对应后端TopicSelectionController的接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */

import request from './request'
import type {
  TopicForSelectionVO,
  TopicSelectionVO,
  ApplyTopicDTO,
  SelectionQueryParams
} from '@/types/topicSelection'
import type { PageResult } from '@/types/common'

/**
 * 课题选报相关API
 */
export const topicSelectionApi = {
  /**
   * 查询可选课题列表（终审通过）
   * @param params 查询参数
   * @returns 分页课题列表
   */
  getAvailableTopics(params: SelectionQueryParams) {
    return request.get<PageResult<TopicForSelectionVO>>('/topic-selection/available', { params })
  },

  /**
   * 选报课题
   * @param data 选报参数
   * @returns 选报记录
   */
  applyTopic(data: ApplyTopicDTO) {
    return request.post<TopicSelectionVO>('/topic-selection', data)
  },

  /**
   * 删除选报记录（仅落选后可删除）
   * @param selectionId 选报记录ID
   */
  deleteSelection(selectionId: string) {
    return request.delete<void>(`/topic-selection/${selectionId}`)
  },

  /**
   * 查询当前学生的选报记录
   * @returns 选报记录列表
   */
  getMySelections() {
    return request.get<TopicSelectionVO[]>('/topic-selection/my')
  }
}
