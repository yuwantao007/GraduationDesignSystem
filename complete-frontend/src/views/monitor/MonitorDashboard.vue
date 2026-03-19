<template>
  <div class="monitor-dashboard">
    <!-- 标题行 + 刷新按钮 -->
    <div class="page-header">
      <h2>质量监控仪表盘</h2>
      <a-button type="primary" size="small" :loading="loading" @click="loadAll">
        <template #icon><ReloadOutlined /></template>
        刷新数据
      </a-button>
    </div>

    <!-- 统计卡片 + 整体进度合并为一行 -->
    <a-row :gutter="8" class="top-row">
      <!-- 4 个统计小卡片 -->
      <a-col :xs="24" :sm="12" :lg="5">
        <a-card class="stat-card stat-card--blue" size="small">
          <a-statistic
            title="课题总数"
            :value="overview?.totalTopics ?? '-'"
            suffix="个"
            :value-style="{ fontSize: '22px' }"
          >
            <template #prefix><FileTextOutlined /></template>
          </a-statistic>
          <div class="stat-sub">终审通过 {{ overview?.approvedTopics ?? 0 }} 个</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="5">
        <a-card class="stat-card stat-card--green" size="small">
          <a-statistic
            title="中选学生数"
            :value="overview?.selectedStudents ?? '-'"
            suffix="人"
            :value-style="{ fontSize: '22px' }"
          >
            <template #prefix><UserOutlined /></template>
          </a-statistic>
          <div class="stat-sub">共 {{ overview?.totalStudents ?? 0 }} 名学生</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="4">
        <a-card class="stat-card stat-card--orange" size="small">
          <a-statistic
            title="选报率"
            :value="overview?.selectionRate ?? '-'"
            suffix="%"
            :precision="1"
            :value-style="{ fontSize: '22px' }"
          >
            <template #prefix><RiseOutlined /></template>
          </a-statistic>
          <div class="stat-sub">届别：{{ overview?.currentCohort ?? '-' }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="4">
        <a-card class="stat-card" size="small" :class="(overview?.unreadAlerts ?? 0) > 0 ? 'stat-card--red' : 'stat-card--gray'">
          <a-statistic
            title="未读预警"
            :value="overview?.unreadAlerts ?? '-'"
            suffix="条"
            :value-style="{ fontSize: '22px' }"
          >
            <template #prefix><BellOutlined /></template>
          </a-statistic>
          <div class="stat-sub">{{ overview?.currentPhaseName ?? '未初始化' }}</div>
        </a-card>
      </a-col>
      <!-- 整体进度 -->
      <a-col :xs="24" :lg="6">
        <a-card class="progress-card" size="small" :loading="loading">
          <template #title>
            <span style="font-size:13px">整体进度</span>
            <a-tag color="blue" style="margin-left:6px;font-size:11px">{{ overview?.currentCohort ?? '-' }}</a-tag>
          </template>
          <a-progress
            :percent="overview?.overallProgress ?? 0"
            :stroke-color="{ '0%': '#108ee9', '100%': '#87d068' }"
            size="small"
            status="active"
          />
          <a-steps :current="phaseStepCurrent" size="small" class="mini-steps">
            <a-step title="申报" />
            <a-step title="双选" />
            <a-step title="指导" />
            <a-step title="答辩" />
          </a-steps>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域：4 张图排两行，每张高度紧凑 -->
    <a-row :gutter="8" class="chart-row">
      <a-col :xs="24" :lg="12">
        <a-card title="课题审查状态趋势" size="small" :loading="loading">
          <div v-if="topicStatusDist.length > 0" ref="pieChartRef" class="chart-container"></div>
          <a-empty v-else description="暂无课题数据" class="chart-empty" />
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="12">
        <a-card title="各企业课题数量（Top 10）" size="small" :loading="loading">
          <div v-if="enterpriseTopicCount.length > 0" ref="barChartRef" class="chart-container"></div>
          <a-empty v-else description="暂无企业课题数据" class="chart-empty" />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="8" class="chart-row">
      <a-col :xs="24" :lg="12">
        <a-card title="选报漏斗图" size="small" :loading="loading">
          <div v-if="selectionStats && selectionStats.totalSelections > 0" ref="funnelChartRef" class="chart-container"></div>
          <a-empty v-else description="暂无选报数据" class="chart-empty" />
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="12">
        <a-card title="学生选报覆盖趋势" size="small" :loading="loading">
          <div v-if="selectionStats && selectionStats.totalStudents > 0" class="student-coverage-row">
            <div ref="pieStudentRef" class="chart-container"></div>
            <div class="coverage-stats">
              <a-statistic title="已选报" :value="selectionStats.studentsWithSelection" suffix="人"
                :value-style="{ fontSize: '18px', color: '#52c41a' }" />
              <a-statistic title="未选报" :value="selectionStats.totalStudents - selectionStats.studentsWithSelection" suffix="人"
                :value-style="{ fontSize: '18px', color: '#ff4d4f' }" style="margin-top:8px" />
              <a-statistic title="覆盖率" :value="selectionStats.studentCoverageRate.toFixed(1)" suffix="%"
                :value-style="{ fontSize: '18px' }" style="margin-top:8px" />
              <a-statistic title="中选率" :value="selectionStats.confirmRate.toFixed(1)" suffix="%"
                :value-style="{ fontSize: '18px' }" style="margin-top:8px" />
            </div>
          </div>
          <a-empty v-else description="暂无学生数据" class="chart-empty" />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  FileTextOutlined, UserOutlined, RiseOutlined,
  BellOutlined, ReloadOutlined
} from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import { monitorApi } from '@/api/monitor'
import type { MonitorOverviewVO, TopicStatusDistVO, SelectionStatsVO, EnterpriseTopicCountVO } from '@/types/monitor'

// ======================== 响应式数据 ========================
const loading = ref(false)
const overview = ref<MonitorOverviewVO | null>(null)
const topicStatusDist = ref<TopicStatusDistVO[]>([])
const enterpriseTopicCount = ref<EnterpriseTopicCountVO[]>([])
const selectionStats = ref<SelectionStatsVO | null>(null)

// ECharts 容器 ref
const pieChartRef = ref<HTMLDivElement>()
const barChartRef = ref<HTMLDivElement>()
const funnelChartRef = ref<HTMLDivElement>()
const pieStudentRef = ref<HTMLDivElement>()

// ECharts 实例
let pieChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null
let funnelChart: echarts.ECharts | null = null
let pieStudentChart: echarts.ECharts | null = null

// ======================== 计算属性 ========================
const phaseStepCurrent = computed(() => {
  const name = overview.value?.currentPhaseName ?? ''
  if (name.includes('申报')) return 0
  if (name.includes('双选') || name.includes('选题')) return 1
  if (name.includes('过程') || name.includes('指导')) return 2
  if (name.includes('答辩') || name.includes('评审')) return 3
  return 0
})

// ======================== 数据加载 ========================
const disposeCharts = () => {
  pieChart?.dispose(); pieChart = null
  barChart?.dispose(); barChart = null
  funnelChart?.dispose(); funnelChart = null
  pieStudentChart?.dispose(); pieStudentChart = null
}

const loadAll = async () => {
  disposeCharts()
  loading.value = true
  try {
    const [overviewRes, statusRes, enterpriseRes, selectionRes] = await Promise.all([
      monitorApi.getOverview(),
      monitorApi.getTopicStatusDist(),
      monitorApi.getTopicCountByEnterprise(),
      monitorApi.getSelectionStats()
    ])
    overview.value = overviewRes.data
    topicStatusDist.value = statusRes.data
    enterpriseTopicCount.value = enterpriseRes.data as unknown as EnterpriseTopicCountVO[]
    selectionStats.value = selectionRes.data

    // 必须先关闭 loading，让 a-card 退出骨架屏状态，
    // 卡片内容区恢复正常尺寸后，再等一次 nextTick 确保 DOM 完全展开，
    // 最后才能让 ECharts 以正确尺寸初始化图表。
    // 若在 loading=true 时初始化，容器尺寸为 0，图表渲染为空白。
    loading.value = false
    await nextTick()
    renderCharts()
  } catch (e) {
    loading.value = false
    message.error('加载监控数据失败')
  }
}

// ======================== 图表渲染 ========================
const renderCharts = () => {
  renderPieChart()
  renderBarChart()
  renderFunnelChart()
  renderStudentPieChart()
}

/** 课题状态分布折线图 */
const renderPieChart = () => {
  if (!pieChartRef.value) return
  if (!pieChart) pieChart = echarts.init(pieChartRef.value)
  const xData = topicStatusDist.value.map(item => item.statusName)
  const yData = topicStatusDist.value.map(item => item.count)
  pieChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: xData,
      axisLabel: { interval: 0, rotate: 20 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      name: '课题数'
    },
    series: [{
      name: '课题数',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: yData,
      lineStyle: { width: 3, color: '#1677ff' },
      itemStyle: { color: '#1677ff' },
      areaStyle: { color: 'rgba(22,119,255,0.15)' },
      label: { show: true, position: 'top' }
    }]
  })
}

/** 企业课题数量横向柱状图 */
const renderBarChart = () => {
  if (!barChartRef.value) return
  if (!barChart) barChart = echarts.init(barChartRef.value)
  const names = enterpriseTopicCount.value.map((d: any) => d.enterpriseName)
  const counts = enterpriseTopicCount.value.map((d: any) => d.count)
  barChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: names, axisLabel: { width: 100, overflow: 'truncate' } },
    series: [{
      type: 'bar',
      data: counts,
      itemStyle: { color: '#1677ff', borderRadius: [0, 4, 4, 0] },
      label: { show: true, position: 'right' }
    }]
  })
}

/** 选报漏斗图 */
const renderFunnelChart = () => {
  if (!funnelChartRef.value || !selectionStats.value) return
  if (!funnelChart) funnelChart = echarts.init(funnelChartRef.value)
  const s = selectionStats.value
  funnelChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 条' },
    series: [{
      type: 'funnel',
      left: '10%',
      width: '80%',
      label: { formatter: '{b}\n{c} 条' },
      data: [
        { name: '总选报', value: s.totalSelections },
        { name: '待确认', value: s.pendingCount },
        { name: '中选', value: s.selectedCount }
      ]
    }]
  })
}

/** 学生覆盖率折线图 */
const renderStudentPieChart = () => {
  if (!pieStudentRef.value || !selectionStats.value) return
  if (!pieStudentChart) pieStudentChart = echarts.init(pieStudentRef.value)
  const s = selectionStats.value
  const notSelected = s.totalStudents - s.studentsWithSelection
  pieStudentChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['已选报', '未选报'],
      axisLabel: { interval: 0 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      name: '人数'
    },
    series: [{
      name: '学生人数',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: [
        s.studentsWithSelection,
        notSelected > 0 ? notSelected : 0
      ],
      lineStyle: { width: 3, color: '#13c2c2' },
      itemStyle: { color: '#13c2c2' },
      areaStyle: { color: 'rgba(19,194,194,0.15)' },
      label: { show: true, position: 'top' }
    }]
  })
}

// ======================== 生命周期 ========================
const handleResize = () => {
  pieChart?.resize()
  barChart?.resize()
  funnelChart?.resize()
  pieStudentChart?.resize()
}

onMounted(() => {
  loadAll()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})
</script>

<style scoped>
.monitor-dashboard {
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* ── 标题行 ── */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
}

/* ── 顶部行（统计卡 + 进度）── */
.top-row {
  align-items: stretch;
}

/* ── 统计小卡 ── */
.stat-card {
  border-radius: 6px;
  border-left: 3px solid #d9d9d9;
  height: 100%;
}

.stat-card--blue   { border-left-color: #1677ff; }
.stat-card--green  { border-left-color: #52c41a; }
.stat-card--orange { border-left-color: #fa8c16; }
.stat-card--red    { border-left-color: #ff4d4f; }
.stat-card--gray   { border-left-color: #8c8c8c; }

/* 减小 ant-card small size 的内边距 */
.stat-card :deep(.ant-card-body) {
  padding: 10px 12px;
}

.stat-sub {
  margin-top: 4px;
  font-size: 11px;
  color: #8c8c8c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ── 进度卡 ── */
.progress-card {
  border-radius: 6px;
  height: 100%;
}

.progress-card :deep(.ant-card-body) {
  padding: 8px 12px;
}

.progress-card :deep(.ant-card-head) {
  min-height: 36px;
  padding: 0 12px;
}

.mini-steps {
  margin-top: 8px;
}

/* 步骤条文字再小一号 */
.mini-steps :deep(.ant-steps-item-title) {
  font-size: 11px !important;
  line-height: 1.4;
}

.mini-steps :deep(.ant-steps-item-description) {
  display: none;
}

/* ── 图表行 ── */
.chart-row {
  /* gap 由 monitor-dashboard 的 gap:8px 控制 */
}

/* 图表卡头高度压缩 */
.chart-row :deep(.ant-card-head) {
  min-height: 36px;
  padding: 0 12px;
}

.chart-row :deep(.ant-card-head-title) {
  font-size: 13px;
  padding: 6px 0;
}

.chart-row :deep(.ant-card-body) {
  padding: 8px 12px;
}

.chart-container {
  height: 210px;
  width: 100%;
}

.chart-empty {
  height: 210px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* ── 学生覆盖率：图+统计数字左右并排 ── */
.student-coverage-row {
  display: flex;
  align-items: stretch;
  gap: 12px;
}

.coverage-stats {
  flex: 0 0 180px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding-top: 4px;
}

.coverage-stats :deep(.ant-statistic-title) {
  font-size: 11px;
}

.coverage-stats :deep(.ant-statistic-content) {
  line-height: 1.3;
}
</style>
