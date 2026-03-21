/**
 * 站内消息类型定义
 */

export interface NotificationVO {
  messageId: string
  receiverId: string
  category: string
  categoryDesc: string
  level: number
  levelDesc: string
  title: string
  content: string
  businessType?: string
  businessId?: string
  businessRoute?: string
  messageStatus: number
  readTime?: string
  processedTime?: string
  expireTime?: string
  createTime: string
}

export interface NotificationQueryVO {
  category?: string
  level?: number
  status?: number
  keyword?: string
  pageNum?: number
  pageSize?: number
}

export interface BatchReadNotificationDTO {
  messageIds: string[]
}

export const NotificationLevelLabel: Record<number, string> = {
  1: '普通通知',
  2: '一般提醒',
  3: '重要提醒',
  4: '紧急预警'
}

export const NotificationLevelColor: Record<number, string> = {
  1: 'blue',
  2: 'gold',
  3: 'orange',
  4: 'red'
}

export const NotificationStatusLabel: Record<number, string> = {
  0: '未读',
  1: '已读',
  2: '已处理'
}

export const NotificationCategoryLabel: Record<string, string> = {
  REVIEW: '审查通知',
  DEFENSE: '答辩通知',
  GUIDANCE: '指导通知',
  SELECTION: '双选通知',
  MIDTERM: '中期通知',
  SYSTEM: '系统通知'
}
