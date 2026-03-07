<!--
  系统阶段概览页面
  展示当前系统所处阶段、进度条、阶段详情
  系统管理员可执行初始化和阶段切换操作

  @author 系统架构师
  @version 1.0
  @since 2026-03-07
-->
<template>
  <div class="phase-overview">
    <!-- 当前阶段状态卡片 -->
    <a-card class="phase-overview__status" :bordered="false" :loading="loading">
      <template #title>
        <FieldTimeOutlined style="margin-right: 8px" />
        系统阶段管理
      </template>
      <template v-if="isAdmin" #extra>
        <a-space>
          <a-button
            v-if="!phaseStatus?.initialized"
            type="primary"
            @click="showInitModal"
          >
            <template #icon><PlayCircleOutlined /></template>
            初始化阶段
          </a-button>
          <a-button
            v-if="phaseStatus?.initialized && !isLastPhase"
            type="primary"
            @click="showSwitchModal"
          >
            <template #icon><SwapOutlined /></template>
            切换阶段
          </a-button>
        </a-space>
      </template>

      <!-- 未初始化提示 -->
      <a-empty
        v-if="!phaseStatus?.initialized"
        description="系统阶段尚未初始化"
      >
        <template #image>
          <FieldTimeOutlined style="font-size: 64px; color: #d9d9d9" />
        </template>
        <p v-if="isAdmin" style="color: #8c8c8c">
          请点击右上角「初始化阶段」按钮，设置当前届别并激活第一阶段
        </p>
        <p v-else style="color: #8c8c8c">
          请等待系统管理员完成阶段初始化
        </p>
      </a-empty>

      <!-- 已初始化：阶段信息 -->
      <template v-if="phaseStatus?.initialized">
        <!-- 学期和当前阶段信息 -->
        <a-row :gutter="24" class="phase-overview__info">
          <a-col :span="6">
            <a-statistic title="当前阶段" :value="phaseStatus.phaseName || '--'">
              <template #suffix>
                <a-tag :color="currentPhaseColor">
                  {{ phaseStatus.phaseOrder }} / {{ phaseStatus.totalPhases }}
                </a-tag>
              </template>
            </a-statistic>
          </a-col>
          <a-col :span="6">
            <a-statistic title="毕业届别" :value="phaseStatus.cohort || '--'" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="切换时间" :value="phaseStatus.switchTime || '--'" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="操作人" :value="phaseStatus.operatorName || '--'" />
          </a-col>
        </a-row>

        <!-- 进度条 -->
        <div class="phase-overview__progress">
          <div class="phase-overview__progress-label">
            <span>整体进度</span>
            <span>{{ phaseStatus.progressPercent }}%</span>
          </div>
          <a-progress
            :percent="phaseStatus.progressPercent"
            :stroke-color="progressColor"
            :show-info="false"
            size="default"
          />
        </div>

        <!-- 阶段步骤条 -->
        <div class="phase-overview__steps">
          <a-steps :current="currentStepIndex" :status="stepsStatus">
            <a-step
              v-for="phase in phaseStatus.phaseList"
              :key="phase.phaseCode"
              :title="phase.phaseName"
              :description="getStepDescription(phase)"
            >
              <template #icon>
                <CheckCircleOutlined v-if="phase.status === 'COMPLETED'" style="color: #52c41a" />
                <SyncOutlined v-else-if="phase.status === 'ACTIVE'" spin style="color: #1890ff" />
                <ClockCircleOutlined v-else style="color: #d9d9d9" />
              </template>
            </a-step>
          </a-steps>
        </div>

        <!-- 阶段详情卡片 -->
        <a-row :gutter="16" class="phase-overview__cards">
          <a-col v-for="phase in phaseStatus.phaseList" :key="phase.phaseCode" :span="6">
            <a-card
              size="small"
              :bordered="true"
              :class="['phase-overview__card', `phase-overview__card--${phase.status.toLowerCase()}`]"
            >
              <template #title>
                <span :style="{ color: getPhaseStatusColor(phase.status) }">
                  {{ phase.phaseName }}
                </span>
              </template>
              <template #extra>
                <a-tag :color="getPhaseTagColor(phase.status)">
                  {{ getPhaseStatusText(phase.status) }}
                </a-tag>
              </template>
              <p class="phase-overview__card-desc">{{ phase.description || '暂无描述' }}</p>
              <p v-if="phase.switchTime" class="phase-overview__card-time">
                <ClockCircleOutlined style="margin-right: 4px" />
                {{ phase.switchTime }}
              </p>
            </a-card>
          </a-col>
        </a-row>
      </template>
    </a-card>

    <!-- 初始化弹窗 -->
    <a-modal
      v-model:open="initModalVisible"
      title="初始化系统阶段"
      :confirm-loading="submitLoading"
      @ok="handleInit"
      @cancel="resetInitForm"
    >
      <a-form
        ref="initFormRef"
        :model="initForm"
        :rules="initRules"
        layout="vertical"
      >
        <a-form-item label="毕业届别" name="cohort">
          <a-input
            v-model:value="initForm.cohort"
            placeholder="请输入毕业届别，如：2026届"
            :maxlength="20"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="初始化原因" name="reason">
          <a-textarea
            v-model:value="initForm.reason"
            placeholder="请输入初始化原因（可选）"
            :rows="3"
          />
        </a-form-item>
      </a-form>
      <a-alert
        type="info"
        show-icon
        message="初始化后将激活第一阶段「课题申报」，确认后不可撤销。"
        style="margin-top: 8px"
      />
    </a-modal>

    <!-- 切换阶段弹窗 -->
    <a-modal
      v-model:open="switchModalVisible"
      title="切换系统阶段"
      :confirm-loading="submitLoading"
      @ok="handleSwitch"
      @cancel="resetSwitchForm"
    >
      <a-alert
        type="warning"
        show-icon
        style="margin-bottom: 16px"
      >
        <template #message>
          阶段切换后不可回滚，切换后非管理员的已过阶段写操作将被冻结。
        </template>
      </a-alert>
      <a-descriptions :column="1" bordered size="small" style="margin-bottom: 16px">
        <a-descriptions-item label="当前阶段">
          <a-tag color="blue">{{ phaseStatus?.phaseName }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="目标阶段">
          <a-tag color="green">{{ nextPhaseName }}</a-tag>
        </a-descriptions-item>
      </a-descriptions>
      <a-form
        ref="switchFormRef"
        :model="switchForm"
        :rules="switchRules"
        layout="vertical"
      >
        <a-form-item label="切换原因" name="switchReason">
          <a-textarea
            v-model:value="switchForm.switchReason"
            placeholder="请输入阶段切换原因"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import {
  FieldTimeOutlined,
  PlayCircleOutlined,
  SwapOutlined,
  CheckCircleOutlined,
  SyncOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import { phaseApi } from '@/api/phase'
import type { PhaseStatusVO, PhaseItemVO, InitPhaseDTO, SwitchPhaseDTO } from '@/types/phase'
import { SystemPhase } from '@/types/phase'

defineOptions({
  name: 'PhaseOverview'
})

const userStore = useUserStore()

// 是否为系统管理员
const isAdmin = computed(() => userStore.hasAnyRole(['SYSTEM_ADMIN']))

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)

// 阶段状态数据
const phaseStatus = ref<PhaseStatusVO | null>(null)

// 阶段顺序映射
const PHASE_ORDER: Record<string, string> = {
  [SystemPhase.TOPIC_DECLARATION]: SystemPhase.TOPIC_SELECTION,
  [SystemPhase.TOPIC_SELECTION]: SystemPhase.TOPIC_GUIDANCE,
  [SystemPhase.TOPIC_GUIDANCE]: SystemPhase.GRADUATION_DEFENSE
}

// 阶段名称映射
const PHASE_NAME_MAP: Record<string, string> = {
  [SystemPhase.TOPIC_DECLARATION]: '课题申报',
  [SystemPhase.TOPIC_SELECTION]: '课题双选',
  [SystemPhase.TOPIC_GUIDANCE]: '课题指导',
  [SystemPhase.GRADUATION_DEFENSE]: '毕设答辩'
}

// 是否是最后阶段
const isLastPhase = computed(() => {
  return phaseStatus.value?.phaseCode === SystemPhase.GRADUATION_DEFENSE
})

// 下一阶段代码
const nextPhaseCode = computed(() => {
  if (!phaseStatus.value?.phaseCode) return ''
  return PHASE_ORDER[phaseStatus.value.phaseCode] || ''
})

// 下一阶段名称
const nextPhaseName = computed(() => {
  return PHASE_NAME_MAP[nextPhaseCode.value] || '--'
})

// 当前阶段颜色
const currentPhaseColor = computed(() => {
  const order = phaseStatus.value?.phaseOrder || 0
  const colors = ['', 'blue', 'cyan', 'orange', 'green']
  return colors[order] || 'blue'
})

// 进度条颜色渐变
const progressColor = computed(() => {
  const percent = phaseStatus.value?.progressPercent || 0
  if (percent < 30) return { from: '#108ee9', to: '#87d068' }
  if (percent < 60) return { from: '#87d068', to: '#faad14' }
  return { from: '#faad14', to: '#52c41a' }
})

// Steps组件的当前索引
const currentStepIndex = computed(() => {
  if (!phaseStatus.value?.phaseOrder) return -1
  return phaseStatus.value.phaseOrder - 1
})

// Steps状态
const stepsStatus = computed<'wait' | 'process' | 'finish' | 'error'>(() => {
  return isLastPhase.value ? 'finish' : 'process'
})

// ==================== 初始化弹窗 ====================

const initModalVisible = ref(false)
const initFormRef = ref<FormInstance>()
const initForm = reactive<InitPhaseDTO>({
  cohort: '',
  reason: ''
})
const initRules = {
  cohort: [{ required: true, message: '请输入毕业届别', trigger: 'blur' }]
}

const showInitModal = () => {
  initModalVisible.value = true
}

const resetInitForm = () => {
  initForm.cohort = ''
  initForm.reason = ''
  initFormRef.value?.resetFields()
}

const handleInit = async () => {
  try {
    await initFormRef.value?.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    const res = await phaseApi.initPhase(initForm)
    phaseStatus.value = res.data
    initModalVisible.value = false
    resetInitForm()
    message.success('系统阶段初始化成功')
  } catch (error: any) {
    // 错误信息由request拦截器统一处理
  } finally {
    submitLoading.value = false
  }
}

// ==================== 切换阶段弹窗 ====================

const switchModalVisible = ref(false)
const switchFormRef = ref<FormInstance>()
const switchForm = reactive<SwitchPhaseDTO>({
  targetPhaseCode: '',
  switchReason: ''
})
const switchRules = {
  switchReason: [{ required: true, message: '请输入切换原因', trigger: 'blur' }]
}

const showSwitchModal = () => {
  switchForm.targetPhaseCode = nextPhaseCode.value
  switchModalVisible.value = true
}

const resetSwitchForm = () => {
  switchForm.targetPhaseCode = ''
  switchForm.switchReason = ''
  switchFormRef.value?.resetFields()
}

const handleSwitch = async () => {
  try {
    await switchFormRef.value?.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    const res = await phaseApi.switchPhase(switchForm)
    phaseStatus.value = res.data
    switchModalVisible.value = false
    resetSwitchForm()
    message.success('阶段切换成功')
  } catch (error: any) {
    // 错误信息由request拦截器统一处理
  } finally {
    submitLoading.value = false
  }
}

// ==================== 辅助方法 ====================

/**
 * 获取步骤描述文字
 */
const getStepDescription = (phase: PhaseItemVO): string => {
  if (phase.status === 'COMPLETED') return phase.switchTime || '已完成'
  if (phase.status === 'ACTIVE') return '进行中'
  return '未开始'
}

/**
 * 获取阶段状态颜色
 */
const getPhaseStatusColor = (status: string): string => {
  const colorMap: Record<string, string> = {
    COMPLETED: '#52c41a',
    ACTIVE: '#1890ff',
    PENDING: '#d9d9d9'
  }
  return colorMap[status] || '#d9d9d9'
}

/**
 * 获取阶段标签颜色
 */
const getPhaseTagColor = (status: string): string => {
  const colorMap: Record<string, string> = {
    COMPLETED: 'green',
    ACTIVE: 'processing',
    PENDING: 'default'
  }
  return colorMap[status] || 'default'
}

/**
 * 获取阶段状态文字
 */
const getPhaseStatusText = (status: string): string => {
  const textMap: Record<string, string> = {
    COMPLETED: '已完成',
    ACTIVE: '进行中',
    PENDING: '未开始'
  }
  return textMap[status] || '未知'
}

/**
 * 加载阶段状态数据
 */
const fetchPhaseStatus = async () => {
  loading.value = true
  try {
    const res = await phaseApi.getCurrentPhaseStatus()
    phaseStatus.value = res.data
  } catch (error: any) {
    // 错误由拦截器处理
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchPhaseStatus()
})
</script>

<style scoped lang="scss">
.phase-overview {
  &__info {
    margin-bottom: 24px;
  }

  &__progress {
    margin-bottom: 32px;

    &-label {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      color: #595959;
      font-size: 14px;
    }
  }

  &__steps {
    margin-bottom: 32px;
    padding: 0 24px;
  }

  &__cards {
    margin-top: 16px;
  }

  &__card {
    height: 100%;

    &--completed {
      border-left: 3px solid #52c41a;
    }

    &--active {
      border-left: 3px solid #1890ff;
      box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
    }

    &--pending {
      border-left: 3px solid #d9d9d9;
      opacity: 0.7;
    }

    &-desc {
      color: #8c8c8c;
      font-size: 13px;
      margin-bottom: 8px;
    }

    &-time {
      color: #bfbfbf;
      font-size: 12px;
      margin-bottom: 0;
    }
  }
}
</style>
