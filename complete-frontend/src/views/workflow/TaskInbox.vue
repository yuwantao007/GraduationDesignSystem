<template>
  <div class="task-inbox">
    <!-- 页面头部 -->
    <a-page-header
      title="待办任务"
      sub-title="工作流任务收件箱"
      style="padding: 0 0 16px 0"
    />

    <!-- 统计卡片 -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="8">
        <a-card size="small">
          <a-statistic
            title="待签收任务"
            :value="unclaimedCount"
            value-style="color: #faad14"
          >
            <template #suffix><span style="font-size:13px">个</span></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card size="small">
          <a-statistic
            title="已签收待处理"
            :value="claimedCount"
            value-style="color: #1677ff"
          >
            <template #suffix><span style="font-size:13px">个</span></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card size="small">
          <a-statistic
            title="修改待重提"
            :value="modifyCount"
            value-style="color: #ff4d4f"
          >
            <template #suffix><span style="font-size:13px">个</span></template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 任务列表 -->
    <a-card title="任务列表" :loading="loading">
      <template #extra>
        <a-button type="primary" ghost size="small" @click="loadTasks">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </template>

      <a-table
        :data-source="tasks"
        :columns="columns"
        row-key="taskId"
        :pagination="false"
        size="small"
      >
        <!-- 课题信息 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'topicInfo'">
            <div>
              <a-tag :color="getCategoryColor(record.topicCategory)" size="small">
                {{ record.topicCategoryDesc }}
              </a-tag>
              <span style="margin-left: 8px; font-weight: 500">{{ record.topicTitle }}</span>
            </div>
            <div style="margin-top: 4px; font-size: 12px; color: #8c8c8c">
              创建人：{{ record.creatorName }}
            </div>
          </template>

          <!-- 任务类型 -->
          <template v-else-if="column.key === 'taskName'">
            <a-tag :color="record.isModifyTask ? 'orange' : 'blue'">
              {{ record.taskName }}
            </a-tag>
          </template>

          <!-- 签收状态 -->
          <template v-else-if="column.key === 'status'">
            <a-badge
              v-if="record.isModifyTask"
              status="warning"
              text="待修改重提"
            />
            <a-badge
              v-else-if="record.claimedByMe"
              status="processing"
              text="已签收（待处理）"
            />
            <a-badge
              v-else-if="record.assignee"
              status="default"
              text="他人已签收"
            />
            <a-badge
              v-else
              status="warning"
              text="待签收"
            />
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <!-- 修改任务：仅提示，不需要签收，通过重新提交课题完成 -->
              <a-tooltip v-if="record.isModifyTask" title="请前往课题列表修改并重新提交课题">
                <a-button size="small" disabled>修改重提</a-button>
              </a-tooltip>
              <!-- 审核任务 -->
              <template v-else>
                <a-button
                  v-if="!record.assignee"
                  size="small"
                  type="primary"
                  ghost
                  :loading="claimingTaskId === record.taskId"
                  @click="handleClaim(record)"
                >
                  签收
                </a-button>
                <a-button
                  v-if="record.claimedByMe || !record.assignee"
                  size="small"
                  type="primary"
                  @click="openReviewDrawer(record)"
                >
                  提交审核
                </a-button>
                <a-button
                  size="small"
                  @click="viewTopic(record.topicId)"
                >
                  查看课题
                </a-button>
              </template>
            </a-space>
          </template>
        </template>
      </a-table>

      <a-empty v-if="!loading && tasks.length === 0" description="暂无待办任务" />
    </a-card>

    <!-- 审核意见抽屉 -->
    <a-drawer
      v-model:open="drawerVisible"
      :title="`提交审核意见 - ${currentTask?.taskName}`"
      width="500"
      :destroy-on-close="true"
      @close="closeDrawer"
    >
      <template v-if="currentTask">
        <a-descriptions :column="1" size="small" bordered style="margin-bottom: 20px">
          <a-descriptions-item label="课题标题">{{ currentTask.topicTitle }}</a-descriptions-item>
          <a-descriptions-item label="课题类型">
            <a-tag :color="getCategoryColor(currentTask.topicCategory)">
              {{ currentTask.topicCategoryDesc }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="课题创建人">{{ currentTask.creatorName }}</a-descriptions-item>
          <a-descriptions-item label="当前任务">{{ currentTask.taskName }}</a-descriptions-item>
        </a-descriptions>

        <a-form :model="reviewForm" layout="vertical">
          <a-form-item label="审核结果" required>
            <a-radio-group v-model:value="reviewForm.outcome" button-style="solid">
              <a-radio-button value="PASS">
                <CheckCircleOutlined /> 通过
              </a-radio-button>
              <a-radio-button value="NEED_MODIFY">
                <EditOutlined /> 需修改
              </a-radio-button>
              <a-radio-button
                v-if="isFinalTask(currentTask.taskDefKey)"
                value="REJECT"
              >
                <CloseCircleOutlined /> 终审不通过
              </a-radio-button>
            </a-radio-group>
          </a-form-item>

          <a-form-item
            label="审核意见"
            :required="reviewForm.outcome !== 'PASS'"
          >
            <a-textarea
              v-model:value="reviewForm.opinion"
              :rows="4"
              placeholder="请输入审核意见（通过时可不填，需修改或不通过时建议填写具体原因）"
              :maxlength="500"
              show-count
            />
          </a-form-item>
        </a-form>
      </template>

      <template #footer>
        <a-space style="float: right">
          <a-button @click="closeDrawer">取消</a-button>
          <a-button
            type="primary"
            :loading="submitting"
            :disabled="!reviewForm.outcome"
            @click="handleCompleteTask"
          >
            提交审核
          </a-button>
        </a-space>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ReloadOutlined,
  CheckCircleOutlined,
  EditOutlined,
  CloseCircleOutlined
} from '@ant-design/icons-vue'
import { workflowApi } from '@/api/workflow'
import type { FlowTaskVO } from '@/types/workflow'

const router = useRouter()

const loading = ref(false)
const tasks = ref<FlowTaskVO[]>([])
const drawerVisible = ref(false)
const currentTask = ref<FlowTaskVO | null>(null)
const claimingTaskId = ref<string | null>(null)
const submitting = ref(false)

const reviewForm = ref<{ outcome: 'PASS' | 'NEED_MODIFY' | 'REJECT' | null; opinion: string }>({
  outcome: null,
  opinion: ''
})

const columns = [
  {
    title: '课题信息',
    key: 'topicInfo',
    width: 280
  },
  {
    title: '任务类型',
    key: 'taskName',
    width: 160
  },
  {
    title: '签收状态',
    key: 'status',
    width: 140
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 160
  },
  {
    title: '操作',
    key: 'action',
    width: 200
  }
]

const unclaimedCount = computed(() =>
  tasks.value.filter(t => !t.isModifyTask && !t.assignee).length
)
const claimedCount = computed(() =>
  tasks.value.filter(t => !t.isModifyTask && t.claimedByMe).length
)
const modifyCount = computed(() =>
  tasks.value.filter(t => t.isModifyTask).length
)

function getCategoryColor(category: number): string {
  const colors: Record<number, string> = {
    1: 'blue',
    2: 'green',
    3: 'purple'
  }
  return colors[category] || 'default'
}

function isFinalTask(taskDefKey: string): boolean {
  return taskDefKey?.startsWith('final')
}

async function loadTasks() {
  loading.value = true
  try {
    tasks.value = await workflowApi.getMyTasks()
  } catch {
    message.error('加载待办任务失败')
  } finally {
    loading.value = false
  }
}

async function handleClaim(task: FlowTaskVO) {
  claimingTaskId.value = task.taskId
  try {
    await workflowApi.claimTask(task.taskId)
    message.success('签收成功')
    await loadTasks()
  } catch {
    message.error('签收失败')
  } finally {
    claimingTaskId.value = null
  }
}

function openReviewDrawer(task: FlowTaskVO) {
  currentTask.value = task
  reviewForm.value = { outcome: null, opinion: '' }
  drawerVisible.value = true
}

function closeDrawer() {
  drawerVisible.value = false
  currentTask.value = null
}

async function handleCompleteTask() {
  if (!currentTask.value || !reviewForm.value.outcome) return
  if (reviewForm.value.outcome !== 'PASS' && !reviewForm.value.opinion) {
    message.warning('需修改或不通过时，请填写审核意见')
    return
  }
  submitting.value = true
  try {
    await workflowApi.completeReviewTask(currentTask.value.taskId, {
      outcome: reviewForm.value.outcome,
      opinion: reviewForm.value.opinion || undefined
    })
    message.success('审核意见已提交，流程已推进')
    closeDrawer()
    await loadTasks()
  } catch {
    message.error('提交审核失败，请重试')
  } finally {
    submitting.value = false
  }
}

function viewTopic(topicId: string) {
  router.push(`/topic/detail/${topicId}`)
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped>
.task-inbox {
  padding: 24px;
}
</style>
