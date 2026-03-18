/**
 * 首页统计类型定义
 */
export interface DashboardStatsVO {
  /** 总用户数 */
  totalUsers: number
  /** 课题总数 */
  totalTopics: number
  /** 待审审批数 */
  pendingApprovals: number
  /** 今日访问数 */
  todayVisits: number
}
