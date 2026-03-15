/**
 * 质量监控API接口
 * 对应后端 MonitorController 的接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */

import request from './request'
import type {
  MonitorOverviewVO,
  TopicStatusDistVO,
  SelectionStatsVO,
  EnterpriseTopicCountVO
} from '@/types/monitor'

export const monitorApi = {
  /**
   * 获取监控总览数据（顶层数字卡片）
   */
  getOverview() {
    return request.get<MonitorOverviewVO>('/monitor/overview')
  },

  /**
   * 获取课题审查状态分布（饼图数据）
   */
  getTopicStatusDist() {
    return request.get<TopicStatusDistVO[]>('/monitor/topic/status')
  },

  /**
   * 获取各企业课题数量统计（横向柱状图数据，取前10）
   */
  getTopicCountByEnterprise() {
    return request.get<EnterpriseTopicCountVO[]>('/monitor/topic/enterprise')
  },

  /**
   * 获取选报漏斗统计数据
   */
  getSelectionStats() {
    return request.get<SelectionStatsVO>('/monitor/selection/stats')
  }
}
