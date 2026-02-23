/**
 * 课题管理API接口
 * 对应后端TopicController的接口
 * 
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-22
 */

import request from './request'
import type {
  TopicVO,
  TopicListVO,
  CreateTopicDTO,
  UpdateTopicDTO,
  TopicQueryVO,
  SubmitTopicDTO,
  TopicSignDTO
} from '@/types/topic'
import type { PageResult } from '@/types/common'

/**
 * 课题管理相关API
 */
export const topicApi = {
  /**
   * 创建课题
   * @param data 创建课题请求参数
   * @returns 课题信息
   */
  createTopic(data: CreateTopicDTO) {
    return request.post<TopicVO>('/topic', data)
  },

  /**
   * 更新课题
   * @param topicId 课题ID
   * @param data 更新课题请求参数
   * @returns 课题信息
   */
  updateTopic(topicId: string, data: UpdateTopicDTO) {
    return request.put<TopicVO>(`/topic/${topicId}`, data)
  },

  /**
   * 获取课题详情
   * @param topicId 课题ID
   * @returns 课题详细信息
   */
  getTopicDetail(topicId: string) {
    return request.get<TopicVO>(`/topic/${topicId}`)
  },

  /**
   * 分页查询课题列表
   * @param params 查询参数
   * @returns 分页课题列表
   */
  getTopicList(params: TopicQueryVO) {
    return request.get<PageResult<TopicListVO>>('/topic/list', { params })
  },

  /**
   * 获取我的课题列表
   * @param params 查询参数
   * @returns 分页课题列表
   */
  getMyTopics(params: TopicQueryVO) {
    return request.get<PageResult<TopicListVO>>('/topic/my', { params })
  },

  /**
   * 删除课题
   * @param topicId 课题ID
   * @returns 操作结果
   */
  deleteTopic(topicId: string) {
    return request.delete<void>(`/topic/${topicId}`)
  },

  /**
   * 提交课题
   * @param data 提交请求参数
   * @returns 课题信息
   */
  submitTopic(data: SubmitTopicDTO) {
    return request.post<TopicVO>('/topic/submit', data)
  },

  /**
   * 撤回课题
   * @param topicId 课题ID
   * @returns 课题信息
   */
  withdrawTopic(topicId: string) {
    return request.post<TopicVO>(`/topic/${topicId}/withdraw`)
  },

  /**
   * 课题签名
   * @param data 签名请求参数
   * @returns 课题信息
   */
  signTopic(data: TopicSignDTO) {
    return request.post<TopicVO>('/topic/sign', data)
  },

  /**
   * 统计通过终审的课题数
   * @param creatorId 创建人ID
   * @returns 通过终审的课题数量
   */
  countPassedTopics(creatorId: string) {
    return request.get<number>('/topic/count/passed', {
      params: { creatorId }
    })
  }
}

export default topicApi
