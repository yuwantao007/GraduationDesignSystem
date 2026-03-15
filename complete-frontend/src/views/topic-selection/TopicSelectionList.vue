<!--
  课题选报页面（学生）
  提供终审通过课题的浏览、筛选和选报功能

  @author 系统架构师
  @version 1.0
  @since 2026-03-08
-->
<template>
  <div class="topic-selection-list">
  <!-- 任务书详情抽屉 -->
  <a-drawer
    v-model:open="detailDrawerVisible"
    title="课题任务书"
    placement="right"
    :width="760"
    :body-style="{ padding: '20px' }"
  >
    <div v-if="detailLoading" style="text-align:center;padding:60px 0">
      <a-spin size="large" tip="加载中..." />
    </div>
    <template v-else-if="detailData">
      <!-- 打印按钮 -->
      <div style="margin-bottom:16px;text-align:right">
        <a-button @click="handleDetailPrint">
          <template #icon><PrinterOutlined /></template>
          打印
        </a-button>
      </div>

      <!-- 任务书内容 -->
      <div id="task-book-print-area">
        <div class="task-book-title">毕业设计（论文）任务书</div>
        <table class="task-book-table" cellpadding="0" cellspacing="0">
          <!-- 题目 -->
          <tr>
            <td class="tbl-label" style="width:90px">题目</td>
            <td class="tbl-value" colspan="5">{{ detailData.topicTitle }}</td>
          </tr>
          <!-- 基本信息1 -->
          <tr>
            <td class="tbl-label">课题类型</td>
            <td class="tbl-value">{{ detailData.topicTypeDesc || '-' }}</td>
            <td class="tbl-label" style="width:80px">课题来源</td>
            <td class="tbl-value" colspan="3">{{ detailData.topicSourceDesc || '-' }}</td>
          </tr>
          <!-- 指导方向 -->
          <tr>
            <td class="tbl-label">指导方向</td>
            <td class="tbl-value" colspan="2">{{ detailData.guidanceDirection || '-' }}</td>
            <td class="tbl-label" style="width:80px">归属企业</td>
            <td class="tbl-value" colspan="2">{{ detailData.enterpriseName || '-' }}</td>
          </tr>
          <!-- 指导教师 -->
          <tr>
            <td class="tbl-label">指导教师</td>
            <td class="tbl-value" colspan="5">{{ detailData.creatorName || '-' }}</td>
          </tr>
          <!-- 选题背景 -->
          <tr>
            <td class="tbl-label sec-label">选题背景与意义</td>
            <td class="tbl-value sec-content" colspan="5">
              <div class="sec-text">{{ detailData.backgroundSignificance || '-' }}</div>
            </td>
          </tr>
          <!-- 内容简述 -->
          <tr>
            <td class="tbl-label sec-label">课题内容简述</td>
            <td class="tbl-value sec-content" colspan="5">
              <div class="sec-text">{{ detailData.contentSummary || '-' }}</div>
            </td>
          </tr>
          <!-- 专业知识训练 -->
          <tr>
            <td class="tbl-label sec-label">专业知识综合训练</td>
            <td class="tbl-value sec-content" colspan="5">
              <div class="sec-text">{{ detailData.professionalTraining || '-' }}</div>
            </td>
          </tr>
          <!-- 开发环境 -->
          <tr>
            <td class="tbl-label sec-label">开发环境（工具）</td>
            <td class="tbl-value sec-content" colspan="5">
              <div class="sec-text">{{ parsedDevelopmentEnvironment }}</div>
            </td>
          </tr>
          <!-- 工作量 -->
          <tr>
            <td class="tbl-label sec-label">工作量（预计周数）</td>
            <td class="tbl-value sec-content" colspan="5">
              <div class="sec-text">{{ parsedWorkloadDetail }}</div>
            </td>
          </tr>
          <!-- 任务与进度要求 -->
          <tr>
            <td class="tbl-label sec-label">任务与进度要求</td>
            <td class="tbl-value sec-content" colspan="5">
              <div class="sec-text">{{ parsedScheduleRequirements }}</div>
            </td>
          </tr>
          <!-- 主要参考文献 -->
          <tr>
            <td class="tbl-label sec-label">主要参考文献</td>
            <td class="tbl-value sec-content" colspan="5">
              <div class="sec-text">{{ parsedTopicReferences }}</div>
            </td>
          </tr>
          <!-- 起止日期 -->
          <tr>
            <td class="tbl-label">起止日期</td>
            <td class="tbl-value" colspan="5">
              {{ detailData.startDate && detailData.endDate
                ? `${detailData.startDate} ~ ${detailData.endDate}`
                : '-' }}
            </td>
          </tr>
          <!-- 备注 -->
          <tr>
            <td class="tbl-label">备注</td>
            <td class="tbl-value" colspan="5">{{ detailData.remark || '-' }}</td>
          </tr>
        </table>

        <!-- 签名栏 -->
        <div class="task-book-signature">
          <div class="sig-item">
            <span class="sig-label">学院负责人</span>
            <span class="sig-line"></span>
          </div>
          <div class="sig-item">
            <span class="sig-label">企业（负责人）</span>
            <span class="sig-line"></span>
          </div>
          <div class="sig-item">
            <span class="sig-label">企业指导教师</span>
            <span class="sig-line"></span>
          </div>
        </div>
      </div>
    </template>
    <a-empty v-else description="暂无数据" />
  </a-drawer>
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
            <a class="topic-title" @click="openDetailDrawer(record.topicId)">{{ record.topicTitle }}</a>
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
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="openDetailDrawer(record.topicId)"
              >
                详情
              </a-button>
              <a-button
                type="link"
                size="small"
                :disabled="record.alreadyApplied || hasSelected || activeCount >= 3"
                @click="openApplyModal(record)"
              >
                {{ record.alreadyApplied ? '已选报' : '选报' }}
              </a-button>
            </a-space>
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
import { SearchOutlined, ReloadOutlined, PrinterOutlined } from '@ant-design/icons-vue'
import { topicSelectionApi } from '@/api/topicSelection'
import { topicApi } from '@/api/topic'
import { useUserStore } from '@/stores/user'
import type { TopicForSelectionVO, ApplyTopicDTO } from '@/types/topicSelection'
import type { TopicVO } from '@/types/topic'

const userStore = useUserStore()

// ==================== 状态定义 ====================

const loading = ref(false)
const topicList = ref<TopicForSelectionVO[]>([])
const applyModalVisible = ref(false)
const applyLoading = ref(false)
const currentTopic = ref<TopicForSelectionVO | null>(null)
const onlyMyMajor = ref(true)

// 任务书详情抽屉
const detailDrawerVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<TopicVO | null>(null)

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

// ==================== 任务书详情 ====================

/** 打开任务书详情抽屉 */
const openDetailDrawer = async (topicId: string) => {
  detailDrawerVisible.value = true
  detailData.value = null
  detailLoading.value = true
  try {
    const result = await topicApi.getTopicDetail(topicId)
    detailData.value = result.data
  } catch (error) {
    console.error('获取课题详情失败:', error)
    message.error('获取课题详情失败')
  } finally {
    detailLoading.value = false
  }
}

/** 打印任务书 */
const handleDetailPrint = () => {
  const el = document.getElementById('task-book-print-area')
  if (!el) return
  const win = window.open('', '_blank')
  if (!win) return
  win.document.write(`
    <html><head><title>课题任务书</title>
    <style>
      body { font-family: '宋体', serif; margin: 20px; color: #000; }
      .task-book-title { font-size:22px; font-weight:bold; text-align:center; padding:20px 0; }
      table { width:100%; border-collapse:collapse; border:2px solid #000; margin-bottom:20px; }
      td { border:1px solid #000; padding:10px 12px; vertical-align:top; font-size:14px; line-height:1.8; }
      .tbl-label { font-weight:500; text-align:center; width:100px; vertical-align:middle; }
      .sec-label { writing-mode:horizontal-tb; text-align:center; font-weight:bold; }
      .sec-content { padding:15px; }
      .sec-text { white-space:pre-wrap; word-break:break-word; text-align:justify; line-height:1.8; }
      .task-book-signature { display:flex; justify-content:space-around; padding:40px 0 20px; margin-top:20px; }
      .sig-item { display:flex; align-items:center; }
      .sig-label { margin-right:10px; font-weight:500; }
      .sig-line { display:inline-block; width:120px; border-bottom:1px solid #000; height:20px; }
    </style>
    </head><body>${el.innerHTML}</body></html>
  `)
  win.document.close()
  win.focus()
  win.print()
  win.close()
}

/** 解析开发环境 */
const parsedDevelopmentEnvironment = computed(() => {
  const data = detailData.value?.developmentEnvironment
  if (!data) return '-'
  if (typeof data === 'string') return data || '-'
  if ((data as any).content) return (data as any).content
  return '-'
})

/** 解析工作量 */
const parsedWorkloadDetail = computed(() => {
  const data = detailData.value?.workloadDetail
  if (!data) return '-'
  if (typeof data === 'string') return data || '-'
  if (Array.isArray(data) && (data[0] as any)?.content) return (data[0] as any).content
  return '-'
})

/** 解析任务与进度要求 */
const parsedScheduleRequirements = computed(() => {
  const data = detailData.value?.scheduleRequirements
  if (!data) return '-'
  if (typeof data === 'string') return data || '-'
  if (Array.isArray(data) && (data[0] as any)?.content) return (data[0] as any).content
  return '-'
})

/** 解析主要参考文献 */
const parsedTopicReferences = computed(() => {
  const data = detailData.value?.topicReferences
  if (!data) return '-'
  if (typeof data === 'string') return data || '-'
  if (Array.isArray(data) && (data[0] as any)?.content) return (data[0] as any).content
  return '-'
})

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
  text-decoration: none;
}

.topic-title:hover {
  text-decoration: underline;
}

/* 任务书样式 */
.task-book-title {
  font-size: 20px;
  font-weight: bold;
  text-align: center;
  padding: 16px 0 20px;
  color: #000;
}

.task-book-table {
  width: 100%;
  border-collapse: collapse;
  border: 2px solid #000;
  margin-bottom: 20px;
  table-layout: fixed;
}

.task-book-table td {
  border: 1px solid #000;
  padding: 10px 12px;
  vertical-align: top;
  color: #000;
  font-size: 14px;
  line-height: 1.8;
}

.tbl-label {
  background-color: #fafafa;
  font-weight: 500;
  text-align: center;
  width: 100px;
  vertical-align: middle !important;
}

.tbl-value {
  background-color: #fff;
  word-break: break-word;
}

.sec-label {
  text-align: center;
  vertical-align: middle !important;
  font-weight: bold;
}

.sec-content {
  padding: 15px !important;
}

.sec-text {
  color: #000;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  text-align: justify;
}

.task-book-signature {
  display: flex;
  justify-content: space-around;
  padding: 40px 0 20px;
  margin-top: 20px;
}

.sig-item {
  display: flex;
  align-items: center;
}

.sig-label {
  color: #000;
  margin-right: 10px;
  white-space: nowrap;
  font-weight: 500;
}

.sig-line {
  display: inline-block;
  width: 120px;
  border-bottom: 1px solid #000;
  height: 20px;
}
</style>
