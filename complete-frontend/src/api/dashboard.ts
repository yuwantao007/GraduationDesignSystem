import request from './request'
import type { DashboardStatsVO } from '@/types/dashboard'

export const dashboardApi = {
  getStats() {
    return request.get<DashboardStatsVO>('/dashboard/stats')
  }
}
