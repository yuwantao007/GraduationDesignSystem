<template>
  <div class="process-monitor">
    <a-page-header
      title="流程实例监控"
      sub-title="课题审查 Flowable 流程管理"
      style="padding: 0 0 16px 0"
    />

    <!-- 状态筛选 -->
    <a-card size="small" style="margin-bottom: 16px">
      <a-space>
        <span>流程状态：</span>
        <a-radio-group v-model:value="filterStatus" button-style="solid" @change="loadList">
          <a-radio-button :value="null">全部</a-radio-button>
          <a-radio-button :value="0">运行中</a-radio-button>
          <a-radio-button :value="1">已完成</a-radio-button>
          <a-radio-button :value="2">已终止</a-radio-button>
        </a-radio-group>
        <a-button type="primary" ghost @click="loadList">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </a-space>
    </a-card>

    <!-- 实例列表 -->
    <a-card :loading="loading">
      <a-table
        :data-source="instances"
        :columns="columns"
        row-key="processInstanceId"
        :pagination="{
          total: total,
          current: pageNum,
          pageSize: pageSize,
          showTotal: (t: number) => `共 ${t} 条`,
          onChange: handlePageChange
        }"
        size="small"
        :scroll="{ x: 1100 }"
      >
        <template #bodyCell="{ column, record }">
          <!-- 课题信息 -->
          <template v-if="column.key === 'topicInfo'">
            <div>
              <a-tag :color="getCategoryColor(record.topicCategory)" size="small">
                {{ record.topicCategoryDesc }}
              </a-tag>
              <span style="font-weight: 500; margin-left: 4px">{{ record.topicTitle }}</span>
            </div>
            <div style="font-size: 12px; color: #8c8c8c; margin-top: 2px">
              创建人：{{ record.creatorName }}
            </div>
          </template>

          <!-- 审查状态 -->
          <template v-else-if="column.key === 'reviewStatus'">
            <a-tag :color="getReviewStatusColor(record.reviewStatus)">
              {{ record.reviewStatusDesc }}
            </a-tag>
          </template>

          <!-- 流程状态 -->
          <template v-else-if="column.key === 'processStatus'">
            <a-badge
              :status="getProcessStatusBadge(record.processStatus)"
              :text="record.processStatusDesc"
            />
          </template>

          <!-- 当前任务 -->
          <template v-else-if="column.key === 'currentTask'">
            <span v-if="record.currentTaskName" style="color: #faad14">
              {{ record.currentTaskName }}
            </span>
            <span v-else style="color: #8c8c8c">—</span>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a-button size="small" @click="viewDiagram(record)">
                <template #icon><ApartmentOutlined /></template>
                流程图
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- BPMN 流程图模态框 -->
    <a-modal
      v-model:open="diagramModalVisible"
      :title="`流程图 - ${currentInstance?.topicTitle}`"
      width="900"
      :footer="null"
      destroy-on-close
    >
      <template v-if="currentInstance">
        <!-- 流程信息 -->
        <a-descriptions :column="3" size="small" bordered style="margin-bottom: 12px">
          <a-descriptions-item label="流程状态">
            <a-badge
              :status="getProcessStatusBadge(currentInstance.processStatus)"
              :text="currentInstance.processStatusDesc"
            />
          </a-descriptions-item>
          <a-descriptions-item label="审查状态">
            <a-tag :color="getReviewStatusColor(currentInstance.reviewStatus)">
              {{ currentInstance.reviewStatusDesc }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="运行时长">
            {{ currentInstance.durationDesc || '—' }}
          </a-descriptions-item>
          <a-descriptions-item label="当前任务" :span="2">
            {{ currentInstance.currentTaskName || '已完成' }}
          </a-descriptions-item>
          <a-descriptions-item label="等待角色">
            {{ currentInstance.waitingRole || '—' }}
          </a-descriptions-item>
        </a-descriptions>

        <!-- BPMN 图 -->
        <div style="height: 420px">
          <BpmnViewer
            :bpmn-xml="bpmnXml"
            :active-activity-ids="activeActivityIds"
            :completed-activity-ids="completedActivityIds"
          />
        </div>

        <!-- 历史节点时间线 -->
        <a-divider orientation="left" style="margin: 12px 0">流程历史</a-divider>
        <a-timeline style="max-height: 200px; overflow-y: auto; padding: 0 8px">
          <a-timeline-item
            v-for="node in historyNodes"
            :key="node.activityId + node.startTime"
            :color="node.active ? '#faad14' : (node.endTime ? '#52c41a' : '#d9d9d9')"
          >
            <span style="font-weight: 500">{{ node.activityName }}</span>
            <span v-if="node.assigneeName" style="margin-left: 8px; color: #1677ff; font-size: 12px">
              {{ node.assigneeName }}
            </span>
            <a-badge v-if="node.active" status="processing" text="进行中" style="margin-left: 8px" />
            <div style="font-size: 11px; color: #8c8c8c">
              {{ node.startTime ? formatDateTime(node.startTime) : '' }}
            </div>
          </a-timeline-item>
        </a-timeline>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { ReloadOutlined, ApartmentOutlined } from '@ant-design/icons-vue'
import { workflowApi } from '@/api/workflow'
import type { ProcessInstanceVO, HistoryNodeVO } from '@/types/workflow'
import BpmnViewer from '@/components/workflow/BpmnViewer.vue'

const loading = ref(false)
const instances = ref<ProcessInstanceVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const filterStatus = ref<number | null>(null)

const diagramModalVisible = ref(false)
const currentInstance = ref<ProcessInstanceVO | null>(null)
const bpmnXml = ref<string | null>(null)
const activeActivityIds = ref<string[]>([])
const completedActivityIds = ref<string[]>([])
const historyNodes = ref<HistoryNodeVO[]>([])

const columns = [
  { title: '课题信息', key: 'topicInfo', width: 260 },
  { title: '审查状态', key: 'reviewStatus', width: 120 },
  { title: '流程状态', key: 'processStatus', width: 110 },
  { title: '当前任务', key: 'currentTask', width: 140 },
  { title: '启动时间', dataIndex: 'startTime', key: 'startTime', width: 160 },
  { title: '运行时长', dataIndex: 'durationDesc', key: 'durationDesc', width: 110 },
  { title: '操作', key: 'action', width: 90, fixed: 'right' }
]

function getCategoryColor(category: number): string {
  return { 1: 'blue', 2: 'green', 3: 'purple' }[category] ?? 'default'
}

function getReviewStatusColor(status: number): string {
  return { 0: 'default', 1: 'processing', 2: 'cyan', 3: 'orange', 4: 'geekblue', 5: 'orange', 6: 'success', 7: 'error' }[status] ?? 'default'
}

function getProcessStatusBadge(status: number): 'processing' | 'success' | 'error' | 'default' {
  return ({ 0: 'processing', 1: 'success', 2: 'error' } as const)[status] ?? 'default'
}

function formatDateTime(dateStr: string): string {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

async function loadList() {
  loading.value = true
  try {
    const res = await workflowApi.listProcessInstances({
      processStatus: filterStatus.value ?? undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    instances.value = res.records
    total.value = Number(res.total)
  } catch {
    message.error('加载流程实例列表失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  pageNum.value = page
  loadList()
}

async function viewDiagram(instance: ProcessInstanceVO) {
  currentInstance.value = instance
  bpmnXml.value = null
  activeActivityIds.value = []
  completedActivityIds.value = []
  historyNodes.value = []
  diagramModalVisible.value = true

  try {
    // 并发加载 BPMN XML 和历史节点
    const [xml, history] = await Promise.all([
      workflowApi.getProcessDiagram(instance.processInstanceId),
      workflowApi.getProcessHistory(instance.processInstanceId)
    ])
    bpmnXml.value = xml
    historyNodes.value = history.filter(n =>
      n.activityType === 'userTask' || n.activityType === 'startEvent' || n.activityType === 'endEvent'
    )
    // 区分活跃节点和已完成节点
    activeActivityIds.value = history
      .filter(n => n.active)
      .map(n => n.activityId)
    completedActivityIds.value = history
      .filter(n => !n.active && n.endTime)
      .map(n => n.activityId)
  } catch (e) {
    message.error('加载流程图失败')
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.process-monitor {
  padding: 24px;
}
</style>
