<!--
  课题选报页面（学生）
  提供终审通过课题的浏览、筛选和选报功能

  @author 系统架构师
  @version 1.0
  @since 2026-03-08
-->
<template>
  <div class="topic-selection-list">
    <!-- 搜索表单 -->
    <a-card class="topic-selection-list__search" :bordered="false">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="课题名称">
          <a-input
            v-model:value="searchForm.topicTitle"
            placeholder="请输入课题名称"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item label="课题大类">
          <a-select
            v-model:value="searchForm.topicCategory"
            placeholder="请选择"
            allow-clear
            style="width: 150px"
          >
            <a-select-option :value="1">高职升本</a-select-option>
            <a-select-option :value="2">3+1</a-select-option>
            <a-select-option :value="3">实验班</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="指导方向">
          <a-input
            v-model:value="searchForm.guidanceDirection"
            placeholder="请输入指导方向"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>

        <a-form-item label="只看本专业">
          <a-switch v-model:checked="onlyMyMajor" @change="handleSearch" />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 选报提示 -->
    <a-alert
      v-if="selectionTip"
      :message="selectionTip"
      type="info"
      show-icon
      closable
      style="margin-bottom: 16px"
    />

    <!-- 课题列表 -->
    <a-card :bordered="false">
      <a-table
        :columns="columns"
        :data-source="topicList"
        :loading="loading"
        :pagination="pagination"
        row-key="topicId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <!-- 课题名称 -->
          <template v-if="column.dataIndex === 'topicTitle'">
            <a-tooltip :title="record.contentSummary">
              <span class="topic-title">{{ record.topicTitle }}</span>
            </a-tooltip>
          </template>

          <!-- 课题大类 -->
          <template v-if="column.dataIndex === 'topicCategoryDesc'">
            <a-tag :color="getCategoryColor(record.topicCategory)">
              {{ record.topicCategoryDesc }}
            </a-tag>
          </template>

          <!-- 已选报人数 -->
          <template v-if="column.dataIndex === 'selectedCount'">
            <a-badge :count="record.selectedCount" :number-style="{ backgroundColor: '#1890ff' }" show-zero />
          </template>

          <!-- 状态 -->
          <template v-if="column.dataIndex === 'alreadyApplied'">
            <a-tag v-if="record.alreadyApplied" color="green">已选报</a-tag>
            <a-tag v-else color="default">未选报</a-tag>
          </template>

          <!-- 操作 -->
          <template v-if="column.dataIndex === 'action'">
            <a-button
              type="link"
              size="small"
              :disabled="record.alreadyApplied || hasSelected || activeCount >= 3"
              @click="openApplyModal(record)"
            >
              {{ record.alreadyApplied ? '已选报' : '选报' }}
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 选报对话框 -->
    <a-modal
      v-model:open="applyModalVisible"
      title="选报课题"
      :confirm-loading="applyLoading"
      @ok="handleApply"
      @cancel="applyModalVisible = false"
    >
      <a-descriptions :column="1" bordered size="small" style="margin-bottom: 16px">
        <a-descriptions-item label="课题名称">{{ currentTopic?.topicTitle }}</a-descriptions-item>
        <a-descriptions-item label="归属企业">{{ currentTopic?.enterpriseName }}</a-descriptions-item>
        <a-descriptions-item label="指导教师">{{ currentTopic?.creatorName }}</a-descriptions-item>
        <a-descriptions-item label="指导方向">{{ currentTopic?.guidanceDirection }}</a-descriptions-item>
      </a-descriptions>

      <a-form :model="applyForm" layout="vertical">
        <a-form-item
          label="选报理由"
          :rules="[{ required: true, message: '请输入选报理由' }]"
        >
          <a-textarea
            v-model:value="applyForm.selectionReason"
            :rows="4"
            :maxlength="500"
            show-count
            placeholder="请输入选报理由（必填，不超过500字）"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { topicSelectionApi } from '@/api/topicSelection'
import { useUserStore } from '@/stores/user'
import type { TopicForSelectionVO, ApplyTopicDTO } from '@/types/topicSelection'

const userStore = useUserStore()

// ==================== 状态定义 ====================

const loading = ref(false)
const topicList = ref<TopicForSelectionVO[]>([])
const applyModalVisible = ref(false)
const applyLoading = ref(false)
const currentTopic = ref<TopicForSelectionVO | null>(null)
const onlyMyMajor = ref(true)

// 搜索表单
const searchForm = reactive({
  topicTitle: '',
  topicCategory: undefined as number | undefined,
  guidanceDirection: ''
})

// 选报表单
const applyForm = reactive<ApplyTopicDTO>({
  topicId: '',
  selectionReason: ''
})

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// ==================== 计算属性 ====================

/** 是否已有中选记录 */
const hasSelected = computed(() => false)

/** 活跃选报数（前端提示用，实际校验在后端） */
const activeCount = computed(() => {
  return topicList.value.filter(t => t.alreadyApplied).length
})

/** 选报提示信息 */
const selectionTip = computed(() => {
  return `您最多可选报 3 个课题。当前可选课题均为终审通过课题。${onlyMyMajor.value ? '（已开启本专业过滤）' : ''}`
})

// ==================== 表格列配置 ====================

const columns = [
  { title: '课题名称', dataIndex: 'topicTitle', key: 'topicTitle', width: 250, ellipsis: true },
  { title: '大类', dataIndex: 'topicCategoryDesc', key: 'topicCategoryDesc', width: 90, align: 'center' as const },
  { title: '类型', dataIndex: 'topicTypeDesc', key: 'topicTypeDesc', width: 70, align: 'center' as const },
  { title: '归属企业', dataIndex: 'enterpriseName', key: 'enterpriseName', width: 140, ellipsis: true },
  { title: '专业', dataIndex: 'majorName', key: 'majorName', width: 120, ellipsis: true },
  { title: '指导方向', dataIndex: 'guidanceDirection', key: 'guidanceDirection', width: 120, ellipsis: true },
  { title: '指导教师', dataIndex: 'creatorName', key: 'creatorName', width: 100 },
  { title: '选报人数', dataIndex: 'selectedCount', key: 'selectedCount', width: 90, align: 'center' as const },
  { title: '状态', dataIndex: 'alreadyApplied', key: 'alreadyApplied', width: 80, align: 'center' as const },
  { title: '操作', dataIndex: 'action', key: 'action', width: 80, align: 'center' as const, fixed: 'right' as const }
]

// ==================== 工具函数 ====================

const getCategoryColor = (category: number): string => {
  const colorMap: Record<number, string> = { 1: 'blue', 2: 'green', 3: 'orange' }
  return colorMap[category] || 'default'
}

// ==================== 数据加载 ====================

const loadTopics = async () => {
  loading.value = true
  try {
    const majorId = onlyMyMajor.value ? (userStore.userInfo?.majorId || undefined) : undefined
    const response = await topicSelectionApi.getAvailableTopics({
      topicTitle: searchForm.topicTitle || undefined,
      topicCategory: searchForm.topicCategory,
      guidanceDirection: searchForm.guidanceDirection || undefined,
      majorId,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    })
    const data = response.data
    topicList.value = data.records || []
    pagination.total = Number(data.total) || 0
  } catch (error) {
    console.error('加载可选课题列表失败:', error)
  } finally {
    loading.value = false
  }
}

// ==================== 事件处理 ====================

const handleSearch = () => {
  pagination.current = 1
  loadTopics()
}

const handleReset = () => {
  searchForm.topicTitle = ''
  searchForm.topicCategory = undefined
  searchForm.guidanceDirection = ''
  onlyMyMajor.value = true
  handleSearch()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadTopics()
}

/** 打开选报对话框 */
const openApplyModal = (topic: TopicForSelectionVO) => {
  // 前置校验：手机号是否填写
  if (!userStore.userInfo?.userPhone) {
    message.warning('请先在个人中心完善手机号信息，再进行课题选报')
    return
  }
  currentTopic.value = topic
  applyForm.topicId = topic.topicId
  applyForm.selectionReason = ''
  applyModalVisible.value = true
}

/** 提交选报 */
const handleApply = async () => {
  if (!applyForm.selectionReason || !applyForm.selectionReason.trim()) {
    message.warning('请输入选报理由')
    return
  }
  applyLoading.value = true
  try {
    await topicSelectionApi.applyTopic({
      topicId: applyForm.topicId,
      selectionReason: applyForm.selectionReason.trim()
    })
    message.success('选报成功')
    applyModalVisible.value = false
    loadTopics()
  } catch (error) {
    console.error('选报失败:', error)
  } finally {
    applyLoading.value = false
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadTopics()
})
</script>

<style scoped>
.topic-selection-list__search {
  margin-bottom: 16px;
}

.topic-title {
  color: #1890ff;
  cursor: pointer;
}

.topic-title:hover {
  text-decoration: underline;
}
</style>
