/**
 * 系统预警API接口
 * 对应后端 AlertController 的接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */

import request from './request'
import type { AlertVO, AlertQueryParams } from '@/types/alert'
import type { PageResult } from '@/types/common'

export const alertApi = {
  /**
   * 分页查询预警列表
   * @param params 查询参数（类型/级别/已读/已处理/分页）
   */
  listAlerts(params?: AlertQueryParams) {
    return request.get<PageResult<AlertVO>>('/alert/list', { params })
  },

  /**
   * 获取未读预警数量（供导航栏徽标使用）
   */
  getUnreadCount() {
    return request.get<number>('/alert/unread-count')
  },

  /**
   * 标记单条预警为已读
   * @param id 预警ID
   */
  markAsRead(id: string) {
    return request.post<void>(`/alert/${id}/read`)
  },

  /**
   * 标记单条预警为已处理
   * @param id 预警ID
   */
  markAsResolved(id: string) {
    return request.post<void>(`/alert/${id}/resolve`)
  },

  /**
   * 全部标记为已读
   */
  markAllAsRead() {
    return request.post<void>('/alert/read-all')
  }
}
