/**
 * 应用状态管理
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏折叠状态
  const collapsed = ref(false)
  
  // 面包屑列表
  const breadcrumbs = ref<Array<{ title: string; path?: string }>>([])
  
  // 切换侧边栏
  const toggleCollapsed = () => {
    collapsed.value = !collapsed.value
  }
  
  // 设置面包屑
  const setBreadcrumbs = (items: Array<{ title: string; path?: string }>) => {
    breadcrumbs.value = items
  }
  
  return {
    collapsed,
    breadcrumbs,
    toggleCollapsed,
    setBreadcrumbs
  }
})
