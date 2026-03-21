import request from './request'
import type { BatchReadNotificationDTO, NotificationQueryVO, NotificationVO } from '@/types/notification'
import type { PageResult } from '@/types/common'

/**
 * 站内消息 API
 */
export const notificationApi = {
  /**
   * 消息列表
   */
  listNotifications(params: NotificationQueryVO) {
    return request.get<PageResult<NotificationVO>>('/notification/list', { params })
  },

  /**
   * 未读数量
   */
  getUnreadCount() {
    return request.get<number>('/notification/unread-count')
  },

  /**
   * 消息详情
   */
  getDetail(messageId: string) {
    return request.get<NotificationVO>(`/notification/${messageId}`)
  },

  /**
   * 单条已读
   */
  markAsRead(messageId: string) {
    return request.post<void>(`/notification/${messageId}/read`)
  },

  /**
   * 批量已读
   */
  batchMarkAsRead(data: BatchReadNotificationDTO) {
    return request.post<void>('/notification/read-batch', data)
  },

  /**
   * 全部已读
   */
  markAllAsRead() {
    return request.post<void>('/notification/read-all')
  },

  /**
   * 标记处理
   */
  markAsProcessed(messageId: string) {
    return request.post<void>(`/notification/${messageId}/process`)
  },

  /**
   * 删除消息
   */
  deleteMessage(messageId: string) {
    return request.delete<void>(`/notification/${messageId}`)
  }
}
