<!--
  我的选报页面（学生）
  展示学生的选报记录列表，支持删除落选记录

  @author 系统架构师
  @version 1.0
  @since 2026-03-08
-->
<template>
  <div class="my-selections">
    <!-- 统计卡片 -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic title="选报课题数" :value="selectionList.length" suffix="/ 3" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic title="待确认" :value="pendingCount" :value-style="{ color: '#1890ff' }" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic title="已中选" :value="selectedCount" :value-style="{ color: '#52c41a' }" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic title="已落选" :value="rejectedCount" :value-style="{ color: '#ff4d4f' }" />
        </a-card>
      </a-col>
    </a-row>

    <!-- 选报记录表格 -->
    <a-card :bordered="false">
      <a-table
        :columns="columns"
        :data-source="selectionList"
        :loading="loading"
        :pagination="false"
        row-key="selectionId"
      >
        <template #bodyCell="{ column, record }">
          <!-- 课题大类 -->
          <template v-if="column.dataIndex === 'topicCategoryDesc'">
            <a-tag :color="getCategoryColor(record.topicCategory)">
              {{ record.topicCategoryDesc }}
            </a-tag>
          </template>

          <!-- 选报理由 -->
          <template v-if="column.dataIndex === 'selectionReason'">
            <a-tooltip :title="record.selectionReason">
              <span class="reason-text">{{ record.selectionReason }}</span>
            </a-tooltip>
          </template>

          <!-- 选报状态 -->
          <template v-if="column.dataIndex === 'selectionStatusDesc'">
            <a-tag :color="SelectionStatusColorMap[record.selectionStatus]">
              {{ record.selectionStatusDesc }}
            </a-tag>
          </template>

          <!-- 操作 -->
          <template v-if="column.dataIndex === 'action'">
            <a-popconfirm
              v-if="record.selectionStatus === SelectionStatus.REJECTED"
              title="确定要删除该选报记录吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDelete(record.selectionId)"
            >
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
            <span v-else class="disabled-text">-</span>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { topicSelectionApi } from '@/api/topicSelection'
import { SelectionStatus, SelectionStatusColorMap } from '@/types/topicSelection'
import type { TopicSelectionVO } from '@/types/topicSelection'

// ==================== 状态定义 ====================

const loading = ref(false)
const selectionList = ref<TopicSelectionVO[]>([])

// ==================== 计算属性 ====================

const pendingCount = computed(() =>
  selectionList.value.filter(s => s.selectionStatus === SelectionStatus.PENDING).length
)

const selectedCount = computed(() =>
  selectionList.value.filter(s => s.selectionStatus === SelectionStatus.SELECTED).length
)

const rejectedCount = computed(() =>
  selectionList.value.filter(s => s.selectionStatus === SelectionStatus.REJECTED).length
)

// ==================== 表格列配置 ====================

const columns = [
  { title: '课题名称', dataIndex: 'topicTitle', key: 'topicTitle', width: 220, ellipsis: true },
  { title: '大类', dataIndex: 'topicCategoryDesc', key: 'topicCategoryDesc', width: 90, align: 'center' as const },
  { title: '归属企业', dataIndex: 'enterpriseName', key: 'enterpriseName', width: 140, ellipsis: true },
  { title: '指导方向', dataIndex: 'guidanceDirection', key: 'guidanceDirection', width: 120, ellipsis: true },
  { title: '指导教师', dataIndex: 'creatorName', key: 'creatorName', width: 100 },
  { title: '选报理由', dataIndex: 'selectionReason', key: 'selectionReason', width: 160, ellipsis: true },
  { title: '选报时间', dataIndex: 'applyTime', key: 'applyTime', width: 160 },
  { title: '确认时间', dataIndex: 'confirmTime', key: 'confirmTime', width: 160 },
  { title: '状态', dataIndex: 'selectionStatusDesc', key: 'selectionStatusDesc', width: 80, align: 'center' as const },
  { title: '操作', dataIndex: 'action', key: 'action', width: 80, align: 'center' as const, fixed: 'right' as const }
]

// ==================== 工具函数 ====================

const getCategoryColor = (category: number): string => {
  const colorMap: Record<number, string> = { 1: 'blue', 2: 'green', 3: 'orange' }
  return colorMap[category] || 'default'
}

// ==================== 数据加载 ====================

const loadSelections = async () => {
  loading.value = true
  try {
    const response = await topicSelectionApi.getMySelections()
    selectionList.value = response.data || []
  } catch (error) {
    console.error('加载选报记录失败:', error)
  } finally {
    loading.value = false
  }
}

// ==================== 事件处理 ====================

/** 删除落选记录 */
const handleDelete = async (selectionId: string) => {
  try {
    await topicSelectionApi.deleteSelection(selectionId)
    message.success('删除成功')
    loadSelections()
  } catch (error) {
    console.error('删除选报记录失败:', error)
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadSelections()
})
</script>

<style scoped>
.reason-text {
  max-width: 150px;
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.disabled-text {
  color: #d9d9d9;
}
</style>
