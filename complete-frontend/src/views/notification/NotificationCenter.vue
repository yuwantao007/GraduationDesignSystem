<template>
  <div class="notification-center">
    <div class="page-header">
      <h2>
        消息中心
        <a-badge v-if="unreadCount > 0" :count="unreadCount" :overflow-count="99" style="margin-left: 8px" />
      </h2>
      <a-space>
        <a-button @click="handleMarkAllRead" :loading="markingAll">全部已读</a-button>
        <a-button type="primary" :loading="loading" @click="loadNotifications">刷新</a-button>
      </a-space>
    </div>

    <a-card :body-style="{ padding: '0' }">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="all" tab="全部" />
        <a-tab-pane key="unread">
          <template #tab>
            未读
            <a-badge v-if="unreadCount > 0" :count="unreadCount" :overflow-count="99" style="margin-left: 4px" />
          </template>
        </a-tab-pane>
        <a-tab-pane key="processed" tab="已处理" />
      </a-tabs>

      <div class="filter-bar">
        <a-space wrap>
          <a-select
            v-model:value="filterCategory"
            placeholder="消息分类"
            allowClear
            style="width: 160px"
            @change="loadNotifications"
          >
            <a-select-option v-for="(label, key) in NotificationCategoryLabel" :key="key" :value="key">
              {{ label }}
            </a-select-option>
          </a-select>
          <a-select
            v-model:value="filterLevel"
            placeholder="消息级别"
            allowClear
            style="width: 120px"
            @change="loadNotifications"
          >
            <a-select-option :value="1">普通</a-select-option>
            <a-select-option :value="2">提醒</a-select-option>
            <a-select-option :value="3">重要</a-select-option>
            <a-select-option :value="4">紧急</a-select-option>
          </a-select>
          <a-input-search
            v-model:value="keyword"
            placeholder="按标题/内容搜索"
            style="width: 260px"
            allow-clear
            @search="handleSearch"
          />
        </a-space>
      </div>

      <a-list :loading="loading" :data-source="notificationList" item-layout="horizontal" class="message-list">
        <template #renderItem="{ item }">
          <a-list-item :class="{ 'message-item--unread': item.messageStatus === 0 }">
            <template #actions>
              <a v-if="item.messageStatus === 0" @click="handleMarkRead(item)">标记已读</a>
              <a v-if="item.messageStatus !== 2" @click="handleMarkProcessed(item)">标记处理</a>
              <a @click="handleDelete(item)">删除</a>
              <a v-if="item.businessRoute" @click="handleOpenBusiness(item)">查看详情</a>
            </template>
            <a-list-item-meta>
              <template #avatar>
                <a-tag :color="NotificationLevelColor[item.level]">
                  {{ NotificationLevelLabel[item.level] || '普通通知' }}
                </a-tag>
              </template>
              <template #title>
                <span :class="{ 'title-bold': item.messageStatus === 0 }">{{ item.title }}</span>
                <a-tag v-if="item.messageStatus === 0" color="red" style="margin-left: 8px">未读</a-tag>
                <a-tag v-else-if="item.messageStatus === 2" color="green" style="margin-left: 8px">已处理</a-tag>
                <a-tag v-else color="default" style="margin-left: 8px">已读</a-tag>
              </template>
              <template #description>
                <div class="message-content">{{ item.content }}</div>
                <div class="message-meta">
                  <a-tag>{{ item.categoryDesc || NotificationCategoryLabel[item.category] || '系统通知' }}</a-tag>
                  <span class="message-time">{{ formatTime(item.createTime) }}</span>
                </div>
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
        <template #empty>
          <a-empty description="暂无站内消息" />
        </template>
      </a-list>

      <div class="pagination-bar">
        <a-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :show-size-changer="true"
          :page-size-options="['10', '20', '50']"
          show-quick-jumper
          :show-total="showTotal"
          @change="handlePageChange"
          @showSizeChange="handleSizeChange"
        />
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notification'
import { notificationApi } from '@/api/notification'
import {
  NotificationCategoryLabel,
  NotificationLevelColor,
  NotificationLevelLabel
} from '@/types/notification'
import type { NotificationVO } from '@/types/notification'

const router = useRouter()
const notificationStore = useNotificationStore()
const loading = ref(false)
const markingAll = ref(false)
const unreadCount = ref(0)
const notificationList = ref<NotificationVO[]>([])
const activeTab = ref('all')
const filterCategory = ref<string | undefined>(undefined)
const filterLevel = ref<number | undefined>(undefined)
const keyword = ref('')

const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0
})

const tabToStatus: Record<string, number | undefined> = {
  all: undefined,
  unread: 0,
  processed: 2
}

const loadUnreadCount = async () => {
  try {
    await notificationStore.loadUnreadCount()
    unreadCount.value = notificationStore.unreadCount
  } catch {
    // 静默失败
  }
}

const loadNotifications = async () => {
  loading.value = true
  try {
    const res = await notificationApi.listNotifications({
      category: filterCategory.value,
      level: filterLevel.value,
      status: tabToStatus[activeTab.value],
      keyword: keyword.value || undefined,
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize
    })
    notificationList.value = res.data.records
    pagination.value.total = res.data.total as unknown as number
  } catch {
    message.error('加载消息失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  pagination.value.current = 1
  loadNotifications()
}

const handleSearch = () => {
  pagination.value.current = 1
  loadNotifications()
}

const handlePageChange = (page: number) => {
  pagination.value.current = page
  loadNotifications()
}

const handleSizeChange = (_: number, size: number) => {
  pagination.value.current = 1
  pagination.value.pageSize = size
  loadNotifications()
}

const handleMarkRead = async (item: NotificationVO) => {
  try {
    await notificationApi.markAsRead(item.messageId)
    if (item.messageStatus === 0) {
      item.messageStatus = 1
      notificationStore.decreaseUnreadCount(1)
      unreadCount.value = notificationStore.unreadCount
    }
    message.success('已标记为已读')
  } catch {
    message.error('操作失败')
  }
}

const handleMarkProcessed = async (item: NotificationVO) => {
  try {
    await notificationApi.markAsProcessed(item.messageId)
    if (item.messageStatus === 0) {
      notificationStore.decreaseUnreadCount(1)
      unreadCount.value = notificationStore.unreadCount
    }
    item.messageStatus = 2
    message.success('已标记为已处理')
  } catch {
    message.error('操作失败')
  }
}

const handleDelete = (item: NotificationVO) => {
  Modal.confirm({
    title: '确认删除消息',
    content: '删除后将不再显示此消息，是否继续？',
    onOk: async () => {
      try {
        await notificationApi.deleteMessage(item.messageId)
        if (item.messageStatus === 0) {
          notificationStore.decreaseUnreadCount(1)
          unreadCount.value = notificationStore.unreadCount
        }
        message.success('删除成功')
        loadNotifications()
      } catch {
        message.error('删除失败')
      }
    }
  })
}

const handleMarkAllRead = async () => {
  markingAll.value = true
  try {
    await notificationApi.markAllAsRead()
    notificationStore.setUnreadCount(0)
    unreadCount.value = notificationStore.unreadCount
    notificationList.value.forEach((item) => {
      if (item.messageStatus === 0) {
        item.messageStatus = 1
      }
    })
    message.success('已全部标记为已读')
  } catch {
    message.error('操作失败')
  } finally {
    markingAll.value = false
  }
}

const handleOpenBusiness = async (item: NotificationVO) => {
  try {
    if (item.messageStatus === 0) {
      await notificationApi.markAsRead(item.messageId)
      notificationStore.decreaseUnreadCount(1)
      unreadCount.value = notificationStore.unreadCount
    }
    if (item.businessType === 'DEFENSE_ARRANGEMENT' && item.businessId) {
      router.push(`/defense/arrangement/detail/${item.businessId}`)
      return
    }
    if (item.businessRoute) {
      router.push(item.businessRoute)
    }
  } catch {
    message.error('跳转失败')
  }
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const showTotal = (total: number) => `共 ${total} 条消息`

onMounted(async () => {
  unreadCount.value = notificationStore.unreadCount
  await Promise.all([loadUnreadCount(), loadNotifications()])
})
</script>

<style scoped>
.notification-center {
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

.message-list {
  padding: 0 16px;
}

.message-item--unread {
  background: #e6f4ff;
}

.title-bold {
  font-weight: 600;
}

.message-content {
  color: #595959;
  margin-bottom: 6px;
  line-height: 1.5;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.message-time {
  font-size: 12px;
  color: #8c8c8c;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
