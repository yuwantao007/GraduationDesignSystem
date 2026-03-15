<template>
  <a-card
    class="process-status-card"
    size="small"
    title="审查流程状态"
    :loading="loading"
  >
    <template #extra>
      <a-button type="link" size="small" @click="loadStatus">
        <ReloadOutlined />
      </a-button>
    </template>

    <template v-if="status">
      <!-- 未启动流程 -->
      <a-empty
        v-if="!status.processStarted"
        description="审查流程尚未启动（课题未提交）"
        :image-style="{ height: '40px' }"
      />

      <!-- 已启动流程 -->
      <template v-else>
        <!-- 当前状态标签 -->
        <div class="status-header">
          <a-tag :color="getStatusColor(status.reviewStatus)" style="font-size: 13px; padding: 2px 8px">
            {{ status.reviewStatusDesc }}
          </a-tag>
          <span v-if="status.waitingRole" style="font-size: 12px; color: #8c8c8c; margin-left: 8px">
            等待：{{ status.waitingRole }}
          </span>
          <a-tag v-if="status.processFinished" color="success" style="margin-left: 8px">
            流程已结束
          </a-tag>
        </div>

        <!-- 历史节点时间线 -->
        <a-timeline
          v-if="status.historyNodes?.length"
          style="margin-top: 12px; margin-bottom: 0"
        >
          <a-timeline-item
            v-for="node in filteredNodes"
            :key="node.activityId"
            :color="getNodeColor(node)"
          >
            <div class="timeline-node">
              <span class="node-name">{{ node.activityName }}</span>
              <span v-if="node.assigneeName" class="node-assignee">
                {{ node.assigneeName }}
              </span>
              <span v-if="node.active" class="node-active-badge">
                <a-badge status="processing" text="进行中" />
              </span>
              <div v-if="node.startTime" class="node-time">
                {{ formatTime(node.startTime) }}
                <template v-if="node.endTime">
                  → {{ formatTime(node.endTime) }}
                </template>
              </div>
            </div>
          </a-timeline-item>
        </a-timeline>
      </template>
    </template>
  </a-card>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { workflowApi } from '@/api/workflow'
import type { ProcessStatusVO, HistoryNodeVO } from '@/types/workflow'

const props = defineProps<{
  topicId: string
}>()

const loading = ref(false)
const status = ref<ProcessStatusVO | null>(null)

const filteredNodes = computed(() => {
  return (status.value?.historyNodes ?? [])
    .filter(n => n.activityType === 'userTask'
      || n.activityType === 'startEvent'
      || n.activityType === 'endEvent')
})

function getStatusColor(reviewStatus: number): string {
  const map: Record<number, string> = {
    0: 'default',
    1: 'processing',
    2: 'cyan',
    3: 'orange',
    4: 'geekblue',
    5: 'orange',
    6: 'success',
    7: 'error'
  }
  return map[reviewStatus] ?? 'default'
}

function getNodeColor(node: HistoryNodeVO): string {
  if (node.active) return '#faad14'
  if (node.endTime) return '#52c41a'
  return '#d9d9d9'
}

function formatTime(timeStr: string): string {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function loadStatus() {
  if (!props.topicId) return
  loading.value = true
  try {
    status.value = await workflowApi.getProcessStatus(props.topicId)
  } catch {
    // 静默失败
  } finally {
    loading.value = false
  }
}

watch(() => props.topicId, (newVal) => {
  if (newVal) loadStatus()
})

onMounted(() => {
  if (props.topicId) loadStatus()
})
</script>

<style scoped>
.process-status-card {
  border-radius: 6px;
}

.status-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 4px;
}

.timeline-node {
  line-height: 1.4;
}

.node-name {
  font-weight: 500;
  margin-right: 8px;
}

.node-assignee {
  font-size: 12px;
  color: #1677ff;
  margin-right: 8px;
}

.node-time {
  font-size: 11px;
  color: #8c8c8c;
  margin-top: 2px;
}

.node-active-badge {
  margin-left: 4px;
}
</style>
