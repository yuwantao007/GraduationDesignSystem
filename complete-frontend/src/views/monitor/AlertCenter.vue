<template>
  <div class="alert-center">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>
        预警中心
        <a-badge v-if="unreadCount > 0" :count="unreadCount" :overflow-count="99" style="margin-left: 8px" />
      </h2>
      <a-space>
        <a-button @click="handleMarkAllRead" :loading="markingAll">
          全部已读
        </a-button>
        <a-button type="primary" :loading="loading" @click="loadAlerts">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </a-space>
    </div>

    <!-- 过滤 Tabs + 筛选栏 -->
    <a-card :body-style="{ padding: '0' }">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="all" tab="全部" />
        <a-tab-pane key="unread">
          <template #tab>
            未读
            <a-badge v-if="unreadCount > 0" :count="unreadCount" :overflow-count="99" style="margin-left: 4px" />
          </template>
        </a-tab-pane>
        <a-tab-pane key="critical" tab="严重" />
        <a-tab-pane key="resolved" tab="已处理" />
      </a-tabs>

      <!-- 高级筛选 -->
      <div class="filter-bar">
        <a-space wrap>
          <a-select
            v-model:value="filterAlertType"
            placeholder="预警类型"
            allowClear
            style="width: 160px"
            @change="loadAlerts"
          >
            <a-select-option v-for="(label, key) in AlertTypeLabel" :key="key" :value="key">
              {{ label }}
            </a-select-option>
          </a-select>
          <a-select
            v-model:value="filterAlertLevel"
            placeholder="预警级别"
            allowClear
            style="width: 120px"
            @change="loadAlerts"
          >
            <a-select-option :value="1">提示</a-select-option>
            <a-select-option :value="2">警告</a-select-option>
            <a-select-option :value="3">严重</a-select-option>
          </a-select>
        </a-space>
      </div>

      <!-- 预警列表 -->
      <a-list
        :loading="loading"
        :data-source="alertList"
        item-layout="horizontal"
        class="alert-list"
      >
        <template #renderItem="{ item }">
          <a-list-item :class="{ 'alert-item--unread': item.isRead === 0 }">
            <template #actions>
              <a v-if="item.isRead === 0" @click="handleMarkRead(item)">标记已读</a>
              <a
                v-if="item.isResolved === 0 && canResolve"
                @click="handleResolve(item)"
              >标记处理</a>
              <span v-if="item.isResolved === 1" class="resolved-tag">已处理</span>
            </template>
            <a-list-item-meta>
              <template #avatar>
                <a-tag :color="AlertLevelColor[item.alertLevel]" style="font-size: 12px; margin-top: 4px">
                  {{ AlertLevelLabel[item.alertLevel] }}
                </a-tag>
              </template>
              <template #title>
                <span :class="{ 'title-bold': item.isRead === 0 }">{{ item.alertTitle }}</span>
                <a-tag v-if="item.isRead === 0" color="red" style="margin-left: 8px; font-size: 11px">未读</a-tag>
              </template>
              <template #description>
                <div class="alert-content">{{ item.alertContent }}</div>
                <div class="alert-meta">
                  <a-tag color="default" style="font-size: 11px">{{ item.alertTypeDesc }}</a-tag>
                  <span class="alert-time">{{ formatTime(item.createTime) }}</span>
                  <span v-if="item.isResolved === 1 && item.resolvedAt" class="resolved-time">
                    处理时间：{{ formatTime(item.resolvedAt) }}
                  </span>
                </div>
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
        <template #empty>
          <a-empty description="暂无预警记录" />
        </template>
      </a-list>

      <!-- 分页 -->
      <div class="pagination-bar">
        <a-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :show-size-changer="true"
          :page-size-options="['10', '20', '50']"
          show-quick-jumper
          :show-total="(total) => `共 ${total} 条预警`"
          @change="handlePageChange"
          @showSizeChange="handleSizeChange"
        />
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { alertApi } from '@/api/alert'
import { AlertLevelColor, AlertLevelLabel, AlertTypeLabel } from '@/types/alert'
import type { AlertVO } from '@/types/alert'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

// ======================== 状态 ========================
const loading = ref(false)
const markingAll = ref(false)
const alertList = ref<AlertVO[]>([])
const unreadCount = ref(0)
const activeTab = ref<string>('all')
const filterAlertType = ref<string | undefined>(undefined)
const filterAlertLevel = ref<number | undefined>(undefined)

const pagination = ref({ current: 1, pageSize: 20, total: 0 })

const userStore = useUserStore()
const canResolve = computed(() => userStore.hasPermission('monitor:alert:resolve'))

// ======================== Tab 过滤映射 ========================
const tabToFilter = {
  all:      { isRead: undefined, isResolved: undefined, alertLevel: undefined },
  unread:   { isRead: 0, isResolved: undefined, alertLevel: undefined },
  critical: { isRead: undefined, isResolved: undefined, alertLevel: 3 },
  resolved: { isRead: undefined, isResolved: 1, alertLevel: undefined }
}

// ======================== 数据加载 ========================
const loadAlerts = async () => {
  loading.value = true
  try {
    const tabFilter = tabToFilter[activeTab.value as keyof typeof tabToFilter] ?? {}
    const res = await alertApi.listAlerts({
      alertType: filterAlertType.value,
      alertLevel: filterAlertLevel.value ?? tabFilter.alertLevel,
      isRead: tabFilter.isRead,
      isResolved: tabFilter.isResolved,
      page: pagination.value.current,
      size: pagination.value.pageSize
    })
    alertList.value = res.data.records
    pagination.value.total = res.data.total as unknown as number
  } catch {
    message.error('加载预警列表失败')
  } finally {
    loading.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const res = await alertApi.getUnreadCount()
    unreadCount.value = res.data as unknown as number
  } catch {
    // 静默失败，不影响主流程
  }
}

// ======================== 操作 ========================
const handleTabChange = () => {
  pagination.value.current = 1
  loadAlerts()
}

const handlePageChange = (page: number) => {
  pagination.value.current = page
  loadAlerts()
}

const handleSizeChange = (_: number, size: number) => {
  pagination.value.current = 1
  pagination.value.pageSize = size
  loadAlerts()
}

const handleMarkRead = async (item: AlertVO) => {
  try {
    await alertApi.markAsRead(item.alertId)
    item.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    message.success('已标记为已读')
  } catch {
    message.error('操作失败')
  }
}

const handleResolve = async (item: AlertVO) => {
  try {
    await alertApi.markAsResolved(item.alertId)
    item.isResolved = 1
    if (item.isRead === 0) {
      item.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
    message.success('已标记为已处理')
    loadAlerts()
  } catch {
    message.error('操作失败')
  }
}

const handleMarkAllRead = async () => {
  markingAll.value = true
  try {
    await alertApi.markAllAsRead()
    unreadCount.value = 0
    message.success('已全部标记为已读')
    loadAlerts()
  } catch {
    message.error('操作失败')
  } finally {
    markingAll.value = false
  }
}

// ======================== 工具函数 ========================
const formatTime = (time: string | undefined) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

// ======================== 生命周期 ========================
onMounted(() => {
  loadAlerts()
  loadUnreadCount()
})
</script>

<style scoped>
.alert-center {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.filter-bar {
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.alert-list {
  padding: 0 16px;
}

.alert-item--unread {
  background: #e6f4ff;
}

.title-bold {
  font-weight: 600;
}

.alert-content {
  color: #595959;
  margin-bottom: 6px;
  line-height: 1.5;
}

.alert-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.alert-time {
  font-size: 12px;
  color: #8c8c8c;
}

.resolved-time {
  font-size: 12px;
  color: #52c41a;
}

.resolved-tag {
  color: #52c41a;
  font-size: 12px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
