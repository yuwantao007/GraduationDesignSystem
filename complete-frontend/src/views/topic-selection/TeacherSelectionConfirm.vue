<!--
  教师确认人选页面（企业教师）
  企业教师查看选报了自己课题的学生列表，进行确认或拒绝操作
  确认人选时自动将该学生其他待确认记录置为落选

  @author 系统架构师
  @version 1.0
  @since 2026-03-14
-->
<template>
  <div class="teacher-confirm">
    <!-- 顶部统计卡片 -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="待确认"
            :value="pendingCount"
            :value-style="{ color: '#1890ff' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="已确认（中选）"
            :value="confirmedCount"
            :value-style="{ color: '#52c41a' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="已落选"
            :value="rejectedCount"
            :value-style="{ color: '#ff4d4f' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="已用名额 / 上限"
            :value="confirmedCount"
            :suffix="`/ ${MAX_CONFIRM}`"
            :value-style="confirmedCount >= MAX_CONFIRM ? { color: '#ff4d4f' } : { color: '#52c41a' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 选报记录表格 -->
    <a-card :bordered="false">
      <!-- 工具栏 -->
      <div class="teacher-confirm__toolbar">
        <!-- 状态 Tab 筛选 -->
        <a-radio-group
          v-model:value="activeStatus"
          button-style="solid"
        >
          <a-radio-button :value="undefined">全部</a-radio-button>
          <a-radio-button :value="0">
            <span>待确认</span>
            <a-badge
              v-if="pendingCount > 0"
              :count="pendingCount"
              :offset="[6, -2]"
              :overflow-count="99"
            />
          </a-radio-button>
          <a-radio-button :value="1">中选</a-radio-button>
          <a-radio-button :value="2">落选</a-radio-button>
        </a-radio-group>

        <!-- 导出按钮 -->
        <a-button @click="handleExport">
          <template #icon><DownloadOutlined /></template>
          导出已确认学生
        </a-button>
      </div>

      <a-table
        :columns="columns"
        :data-source="filteredList"
        :loading="loading"
        row-key="selectionId"
        :pagination="{ pageSize: 10, showTotal: (total: number) => `共 ${total} 条` }"
      >
        <template #bodyCell="{ column, record }">
          <!-- 课题大类 -->
          <template v-if="column.dataIndex === 'topicCategoryDesc'">
            <a-tag :color="getCategoryColor(record.topicCategory)">
              {{ record.topicCategoryDesc }}
            </a-tag>
          </template>

          <!-- 课题来源 -->
          <template v-else-if="column.dataIndex === 'topicSource'">
            <a-tag :color="record.topicSource === 2 ? 'orange' : 'blue'">
              {{ record.topicSource === 2 ? '校外协同' : '校内' }}
            </a-tag>
          </template>

          <!-- 选报状态 -->
          <template v-else-if="column.dataIndex === 'selectionStatus'">
            <a-tag :color="SelectionStatusColorMap[record.selectionStatus]">
              {{ record.selectionStatusDesc }}
            </a-tag>
          </template>

          <!-- 选报理由（气泡展示完整内容） -->
          <template v-else-if="column.dataIndex === 'selectionReason'">
            <a-tooltip :title="record.selectionReason" placement="topLeft">
              <span class="teacher-confirm__reason-text">{{ record.selectionReason }}</span>
            </a-tooltip>
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-popconfirm
                v-if="record.selectionStatus === 0"
                title="确认该学生中选？确认后，该学生的其他待确认记录将自动落选。"
                ok-text="确认"
                cancel-text="取消"
                :ok-button-props="{ type: 'primary' }"
                @confirm="handleConfirm(record.selectionId)"
              >
                <a-button
                  type="primary"
                  size="small"
                  :disabled="confirmedCount >= MAX_CONFIRM"
                >
                  确认
                </a-button>
              </a-popconfirm>

              <a-popconfirm
                v-if="record.selectionStatus === 0"
                title="确认拒绝该学生的选报？"
                ok-text="拒绝"
                cancel-text="取消"
                :ok-button-props="{ type: 'primary', danger: true }"
                @confirm="handleReject(record.selectionId)"
              >
                <a-button danger size="small">拒绝</a-button>
              </a-popconfirm>

              <span v-if="record.selectionStatus !== 0" class="teacher-confirm__no-action">—</span>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { DownloadOutlined } from '@ant-design/icons-vue'
import { topicSelectionApi } from '@/api/topicSelection'
import { SelectionStatusColorMap } from '@/types/topicSelection'
import type { SelectionForTeacherVO } from '@/types/topicSelection'
import type { TableColumnsType } from 'ant-design-vue'

/** 企业教师最大确认人数 */
const MAX_CONFIRM = 16

const loading = ref(false)
const selectionList = ref<SelectionForTeacherVO[]>([])

/** 当前激活的状态过滤（undefined=全部） */
const activeStatus = ref<number | undefined>(undefined)

// ==================== 计算属性 ====================

/** 待确认人数（从全量数据实时计算，不随 Tab 变动） */
const pendingCount = computed(() =>
  selectionList.value.filter(s => s.selectionStatus === 0).length
)

/** 已确认（中选）人数 */
const confirmedCount = computed(() =>
  selectionList.value.filter(s => s.selectionStatus === 1).length
)

/** 落选人数 */
const rejectedCount = computed(() =>
  selectionList.value.filter(s => s.selectionStatus === 2).length
)

/** Tab 过滤后的列表（activeStatus=undefined 时返回全部） */
const filteredList = computed(() => {
  if (activeStatus.value === undefined) return selectionList.value
  return selectionList.value.filter(s => s.selectionStatus === activeStatus.value)
})

// ==================== 表格列定义 ====================

const columns: TableColumnsType = [
  { title: '学生姓名', dataIndex: 'studentName', width: 90 },
  { title: '学号', dataIndex: 'studentNo', width: 110 },
  { title: '手机号', dataIndex: 'studentPhone', width: 120 },
  { title: '课题名称', dataIndex: 'topicTitle', ellipsis: true },
  { title: '课题大类', dataIndex: 'topicCategoryDesc', width: 100 },
  { title: '课题来源', dataIndex: 'topicSource', width: 100 },
  { title: '选报理由', dataIndex: 'selectionReason', width: 160, ellipsis: true },
  { title: '选报时间', dataIndex: 'applyTime', width: 160 },
  {
    title: '状态',
    dataIndex: 'selectionStatus',
    width: 90,
    filters: [
      { text: '待确认', value: 0 },
      { text: '中选', value: 1 },
      { text: '落选', value: 2 }
    ],
    onFilter: (value: unknown, record: SelectionForTeacherVO) =>
      record.selectionStatus === value
  },
  { title: '操作', key: 'action', fixed: 'right', width: 140 }
]

// ==================== 数据加载 ====================

/**
 * 加载选报列表（加载所有数据，由前端 Tab 控制显示）
 */
const loadSelections = async () => {
  loading.value = true
  try {
    const res = await topicSelectionApi.getSelectionsForTeacher()
    selectionList.value = res.data || []
  } catch (error) {
    console.error('加载选报列表失败', error)
    message.error('加载选报列表失败')
  } finally {
    loading.value = false
  }
}

// ==================== 事件处理 ====================

/**
 * 确认人选
 */
const handleConfirm = async (selectionId: string) => {
  try {
    await topicSelectionApi.confirmSelection(selectionId)
    message.success('确认成功，该学生已中选')
    await loadSelections()
  } catch (error) {
    console.error('确认人选失败', error)
  }
}

/**
 * 拒绝人选
 */
const handleReject = async (selectionId: string) => {
  try {
    await topicSelectionApi.rejectSelection(selectionId)
    message.success('已拒绝该学生的选报')
    await loadSelections()
  } catch (error) {
    console.error('拒绝人选失败', error)
  }
}

/**
 * 导出已确认学生 Excel
 */
const handleExport = () => {
  const url = topicSelectionApi.getExportConfirmedUrl()
  const link = document.createElement('a')
  link.href = url
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// ==================== 工具方法 ====================

/**
 * 课题大类颜色映射
 */
const getCategoryColor = (category: number): string => {
  const colorMap: Record<number, string> = { 1: 'blue', 2: 'green', 3: 'purple' }
  return colorMap[category] || 'default'
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadSelections()
})
</script>

<style scoped lang="scss">
.teacher-confirm {
  padding: 20px;

  &__toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  &__reason-text {
    display: inline-block;
    max-width: 140px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    vertical-align: middle;
  }

  &__no-action {
    color: #bfbfbf;
  }
}
</style>
