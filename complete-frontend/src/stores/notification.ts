import { defineStore } from 'pinia'
import { ref } from 'vue'
import { notificationApi } from '@/api/notification'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)

  const setUnreadCount = (count: number) => {
    unreadCount.value = Math.max(0, count || 0)
  }

  const decreaseUnreadCount = (delta = 1) => {
    unreadCount.value = Math.max(0, unreadCount.value - delta)
  }

  const loadUnreadCount = async () => {
    const res = await notificationApi.getUnreadCount()
    setUnreadCount(res.data as unknown as number)
  }

  const resetUnreadCount = () => {
    unreadCount.value = 0
  }

  return {
    unreadCount,
    setUnreadCount,
    decreaseUnreadCount,
    loadUnreadCount,
    resetUnreadCount
  }
})
