<template>
  <div class="dashboard">
    <!-- 当前阶段进度卡片 -->
    <a-card style="margin-bottom: 16px" :loading="phaseLoading">
      <template #title>
        <FieldTimeOutlined style="margin-right: 8px" />
        当前阶段进度
      </template>
      <template v-if="phaseStatus?.initialized">
        <a-row :gutter="24" style="margin-bottom: 12px">
          <a-col :span="8">
            <a-statistic title="当前阶段" :value="phaseStatus.phaseName || '--'">
              <template #suffix>
                <a-tag :color="currentPhaseColor" style="margin-left: 4px">
                  {{ phaseStatus.phaseOrder }} / {{ phaseStatus.totalPhases }}
                </a-tag>
              </template>
            </a-statistic>
          </a-col>
          <a-col :span="8">
            <a-statistic title="毕业届别" :value="phaseStatus.cohort || '--'" />
          </a-col>
          <a-col :span="8">
            <a-statistic title="整体进度" :value="phaseStatus.progressPercent" suffix="%" />
          </a-col>
        </a-row>
        <a-progress :percent="phaseStatus.progressPercent" :stroke-color="progressColor" :show-info="false" />
        <a-steps size="small" :current="currentStepIndex" style="margin-top: 16px">
          <a-step
            v-for="phase in phaseStatus.phaseList"
            :key="phase.phaseCode"
            :title="phase.phaseName"
          />
        </a-steps>
      </template>
      <a-empty v-else description="系统阶段尚未初始化" />
    </a-card>

    <a-row :gutter="16">
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="总用户数"
            :value="statistics.totalUsers"
            :prefix="() => h(UserOutlined)"
          />
        </a-card>
      </a-col>
      
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="课题总数"
            :value="statistics.totalTopics"
            :prefix="() => h(FileTextOutlined)"
          />
        </a-card>
      </a-col>
      
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="待审批"
            :value="statistics.pendingApprovals"
            :prefix="() => h(ClockCircleOutlined)"
          />
        </a-card>
      </a-col>
      
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="今日访问"
            :value="statistics.todayVisits"
            :prefix="() => h(EyeOutlined)"
          />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="12">
        <a-card title="最近活动">
          <a-list
            :data-source="recentActivities"
            :loading="loading"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta
                  :title="item.title"
                  :description="item.time"
                />
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
      
      <a-col :span="12">
        <a-card title="待办事项">
          <a-list
            :data-source="todoList"
            :loading="loading"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta
                  :title="item.title"
                  :description="item.deadline"
                />
                <template #extra>
                  <a-tag :color="item.priority === 'high' ? 'red' : 'blue'">
                    {{ item.priority === 'high' ? '紧急' : '普通' }}
                  </a-tag>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h } from 'vue'
import {
  UserOutlined,
  FileTextOutlined,
  ClockCircleOutlined,
  EyeOutlined,
  FieldTimeOutlined
} from '@ant-design/icons-vue'
import { phaseApi } from '@/api/phase'
import type { PhaseStatusVO } from '@/types/phase'

// 阶段状态
const phaseStatus = ref<PhaseStatusVO | null>(null)
const phaseLoading = ref(false)

const progressColor = computed(() => {
  const percent = phaseStatus.value?.progressPercent || 0
  if (percent < 30) return { from: '#108ee9', to: '#87d068' }
  if (percent < 60) return { from: '#87d068', to: '#faad14' }
  return { from: '#faad14', to: '#52c41a' }
})

const currentPhaseColor = computed(() => {
  const order = phaseStatus.value?.phaseOrder || 0
  const colors = ['', 'blue', 'cyan', 'orange', 'green']
  return colors[order] || 'blue'
})

const currentStepIndex = computed(() => {
  if (!phaseStatus.value?.phaseOrder) return -1
  return phaseStatus.value.phaseOrder - 1
})

// 统计数据
const statistics = ref({
  totalUsers: 0,
  totalTopics: 0,
  pendingApprovals: 0,
  todayVisits: 0
})

// 最近活动
const recentActivities = ref([
  { title: '张三提交了开题报告', time: '2小时前' },
  { title: '李四上传了毕业论文', time: '5小时前' },
  { title: '王五申请了答辩', time: '1天前' },
  { title: '赵六完成了中期检查', time: '2天前' }
])

// 待办事项
const todoList = ref([
  { title: '审批课题申请', deadline: '今天 18:00', priority: 'high' },
  { title: '评阅学生论文', deadline: '明天 12:00', priority: 'normal' },
  { title: '安排答辩时间', deadline: '后天 10:00', priority: 'normal' }
])

const loading = ref(false)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 模拟加载数据
    setTimeout(() => {
      statistics.value = {
        totalUsers: 1234,
        totalTopics: 567,
        pendingApprovals: 23,
        todayVisits: 456
      }
      loading.value = false
    }, 1000)
  } catch (error) {
    console.error('加载数据失败:', error)
    loading.value = false
  }
}

const loadPhaseStatus = async () => {
  phaseLoading.value = true
  try {
    const res = await phaseApi.getCurrentPhaseStatus()
    phaseStatus.value = res.data
  } catch {
    // 阶段数据加载失败，不影响其他功能
  } finally {
    phaseLoading.value = false
  }
}

onMounted(() => {
  loadData()
  loadPhaseStatus()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}
</style>
