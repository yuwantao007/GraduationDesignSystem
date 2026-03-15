<!--
  高校教师查看指导学生选题结果页面
  功能：
  1. 顶部"我的配对关系"卡片：从 teacher_relationship 独立查询，
     无论是否有选报数据都能展示（解决"明明已配对却显示未配对"的误导问题）
  2. 统计卡片：总记录数 / 中选 / 待确认 / 落选
  3. 状态 Tab 快速过滤 + 关键词搜索
  4. 支持导出 Excel
  5. 空状态区分两种情况：①未建立配对 ②已配对但暂无选报数据

  @author 系统架构师
  @version 1.0
  @since 2026-03-14
-->
<template>
  <div class="univ-selection">

    <!-- ===== 配对关系面板 ===== -->
    <a-card
      :bordered="false"
      style="margin-bottom: 16px"
      :loading="pairingLoading"
    >
      <template #title>
        <TeamOutlined style="margin-right: 6px; color: #1890ff" />
        我的配对关系
        <a-tag
          v-if="pairingList.length > 0"
          color="success"
          style="margin-left: 8px; font-weight: normal"
        >
          已配对 {{ enabledPairingCount }} 位企业教师
        </a-tag>
      </template>

      <!-- 有配对时展示 -->
      <a-row v-if="pairingList.length > 0" :gutter="[16, 12]">
        <a-col
          v-for="pairing in pairingList"
          :key="pairing.relationId"
          :xs="24"
          :sm="12"
          :lg="8"
          :xl="6"
        >
          <a-card
            size="small"
            :bordered="true"
            :class="['univ-selection__pairing-card', pairing.isEnabled !== 1 ? 'univ-selection__pairing-card--disabled' : '']"
          >
            <div class="univ-selection__pairing-header">
              <span class="univ-selection__pairing-name">{{ pairing.enterpriseTeacherName }}</span>
              <a-tag :color="pairing.isEnabled === 1 ? 'success' : 'default'" style="margin: 0">
                {{ pairing.isEnabled === 1 ? '启用' : '停用' }}
              </a-tag>
            </div>
            <div class="univ-selection__pairing-meta">
              <div><span class="label">企业：</span>{{ pairing.enterpriseName || '—' }}</div>
              <div><span class="label">方向：</span>{{ pairing.directionName || '—' }}</div>
              <div><span class="label">届别：</span>{{ pairing.cohort || '—' }}</div>
              <div><span class="label">类型：</span>{{ pairing.relationTypeDesc }}</div>
            </div>
            <div class="univ-selection__pairing-stats">
              <a-statistic-group>
                <a-row :gutter="8" style="margin-top: 10px; text-align: center">
                  <a-col :span="8">
                    <div class="stat-num">{{ pairing.approvedTopicCount }}</div>
                    <div class="stat-label">终审课题</div>
                  </a-col>
                  <a-col :span="8">
                    <div class="stat-num" style="color: #1890ff">{{ pairing.selectionTopicCount }}</div>
                    <div class="stat-label">有选报课题</div>
                  </a-col>
                  <a-col :span="8">
                    <div class="stat-num" style="color: #52c41a">{{ pairing.confirmedStudentCount }}</div>
                    <div class="stat-label">中选学生</div>
                  </a-col>
                </a-row>
              </a-statistic-group>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 无配对时提示 -->
      <a-empty
        v-else-if="!pairingLoading"
        description="您尚未与任何企业教师建立配对关系，请联系管理员在「教师配对管理」中进行配置。"
      />
    </a-card>

    <!-- ===== 统计卡片 ===== -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic title="选报总记录数" :value="allList.length" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic title="已中选" :value="confirmedCount" :value-style="{ color: '#52c41a' }" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic title="待确认" :value="pendingCount" :value-style="{ color: '#1890ff' }" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic title="落选" :value="rejectedCount" :value-style="{ color: '#ff4d4f' }" />
        </a-card>
      </a-col>
    </a-row>

    <!-- ===== 选报结果表格 ===== -->
    <a-card :bordered="false">

      <!-- 工具栏 -->
      <div class="univ-selection__toolbar">
        <a-space>
          <a-radio-group v-model:value="activeStatus" button-style="solid">
            <a-radio-button :value="undefined">全部</a-radio-button>
            <a-radio-button :value="1">中选</a-radio-button>
            <a-radio-button :value="0">待确认</a-radio-button>
            <a-radio-button :value="2">落选</a-radio-button>
          </a-radio-group>
          <a-input-search
            v-model:value="searchKeyword"
            placeholder="搜索学生姓名 / 课题名称 / 学号"
            allow-clear
            style="width: 260px"
          />
        </a-space>
        <a-button @click="handleExport">
          <template #icon><DownloadOutlined /></template>
          导出选题结果
        </a-button>
      </div>

      <!-- 有配对但暂无选报数据 -->
      <a-empty
        v-if="!tableLoading && allList.length === 0 && pairingList.length > 0"
        style="padding: 40px 0"
      >
        <template #description>
          <span>
            配对关系已建立，暂无学生选报数据。<br />
            请等待系统进入
            <a-tag color="blue">课题双选阶段</a-tag>
            后，学生完成选报再查看。
          </span>
        </template>
      </a-empty>

      <!-- 无配对且无数据（此情况不显示表格） -->
      <a-empty
        v-else-if="!tableLoading && !pairingLoading && allList.length === 0 && pairingList.length === 0"
        description="暂无数据。请先完成教师配对配置。"
        style="padding: 40px 0"
      />

      <!-- 数据表格 -->
      <a-table
        v-else
        :columns="columns"
        :data-source="filteredList"
        :loading="tableLoading"
        row-key="selectionId"
        :pagination="{
          pageSize: 15,
          showTotal: (total: number) => `共 ${total} 条`,
          showSizeChanger: true,
          pageSizeOptions: ['10', '15', '30', '50']
        }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'topicCategoryDesc'">
            <a-tag :color="getCategoryColor(record.topicCategory)">
              {{ record.topicCategoryDesc }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'topicSourceDesc'">
            <a-tag :color="record.topicSource === 2 ? 'orange' : 'blue'">
              {{ record.topicSourceDesc }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'selectionStatus'">
            <a-tag :color="SelectionStatusColorMap[record.selectionStatus]">
              {{ record.selectionStatusDesc }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'selectionReason'">
            <a-tooltip :title="record.selectionReason" placement="topLeft">
              <span class="univ-selection__ellipsis">{{ record.selectionReason }}</span>
            </a-tooltip>
          </template>
        </template>
      </a-table>
    </a-card>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { DownloadOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { topicSelectionApi } from '@/api/topicSelection'
import { SelectionStatusColorMap } from '@/types/topicSelection'
import type { SelectionForUnivTeacherVO, UnivTeacherPairingVO } from '@/types/topicSelection'
import type { TableColumnsType } from 'ant-design-vue'

// ==================== 状态 ====================

const pairingLoading = ref(false)
const tableLoading = ref(false)
const pairingList = ref<UnivTeacherPairingVO[]>([])
const allList = ref<SelectionForUnivTeacherVO[]>([])
const activeStatus = ref<number | undefined>(undefined)
const searchKeyword = ref('')

// ==================== 计算属性 ====================

/** 启用的配对数量 */
const enabledPairingCount = computed(() =>
  pairingList.value.filter(p => p.isEnabled === 1).length
)

const confirmedCount = computed(() => allList.value.filter(r => r.selectionStatus === 1).length)
const pendingCount = computed(() => allList.value.filter(r => r.selectionStatus === 0).length)
const rejectedCount = computed(() => allList.value.filter(r => r.selectionStatus === 2).length)

const filteredList = computed(() => {
  let result = allList.value
  if (activeStatus.value !== undefined) {
    result = result.filter(r => r.selectionStatus === activeStatus.value)
  }
  const kw = searchKeyword.value.trim()
  if (kw) {
    result = result.filter(r =>
      r.studentName?.includes(kw) ||
      r.topicTitle?.includes(kw) ||
      r.studentNo?.includes(kw)
    )
  }
  return result
})

// ==================== 表格列定义 ====================

const columns: TableColumnsType = [
  { title: '学生姓名', dataIndex: 'studentName', width: 90 },
  { title: '学号', dataIndex: 'studentNo', width: 120 },
  { title: '手机号', dataIndex: 'studentPhone', width: 125 },
  { title: '课题名称', dataIndex: 'topicTitle', ellipsis: true },
  { title: '课题大类', dataIndex: 'topicCategoryDesc', width: 100 },
  { title: '课题来源', dataIndex: 'topicSourceDesc', width: 110 },
  { title: '指导方向', dataIndex: 'guidanceDirection', width: 120, ellipsis: true },
  { title: '企业教师', dataIndex: 'enterpriseTeacherName', width: 100 },
  { title: '企业名称', dataIndex: 'enterpriseName', width: 130, ellipsis: true },
  { title: '选报理由', dataIndex: 'selectionReason', width: 150, ellipsis: true },
  {
    title: '状态',
    dataIndex: 'selectionStatus',
    width: 90,
    filters: [
      { text: '待确认', value: 0 },
      { text: '中选', value: 1 },
      { text: '落选', value: 2 }
    ],
    onFilter: (value: unknown, record: SelectionForUnivTeacherVO) =>
      record.selectionStatus === value
  },
  { title: '选报时间', dataIndex: 'applyTime', width: 160 },
  { title: '确认时间', dataIndex: 'confirmTime', width: 160 }
]

// ==================== 数据加载 ====================

/** 加载配对关系（独立查询，与选报数据无关） */
const loadPairings = async () => {
  pairingLoading.value = true
  try {
    const res = await topicSelectionApi.getUnivTeacherPairings()
    pairingList.value = res.data || []
  } catch (error) {
    console.error('加载配对关系失败', error)
    message.error('加载配对关系失败')
  } finally {
    pairingLoading.value = false
  }
}

/** 加载选报结果 */
const loadSelections = async () => {
  tableLoading.value = true
  try {
    const res = await topicSelectionApi.getSelectionsForUnivTeacher()
    allList.value = res.data || []
  } catch (error) {
    console.error('加载选题结果失败', error)
    message.error('加载选题结果失败')
  } finally {
    tableLoading.value = false
  }
}

// ==================== 事件处理 ====================

const handleExport = () => {
  const url = topicSelectionApi.getExportUnivTeacherUrl()
  const link = document.createElement('a')
  link.href = url
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// ==================== 工具方法 ====================

const getCategoryColor = (category: number): string => {
  const colorMap: Record<number, string> = { 1: 'blue', 2: 'green', 3: 'purple' }
  return colorMap[category] || 'default'
}

// ==================== 生命周期 ====================

onMounted(() => {
  // 两个请求并发，配对关系和选报数据互不依赖
  Promise.all([loadPairings(), loadSelections()])
})
</script>

<style scoped lang="scss">
.univ-selection {
  padding: 20px;

  &__toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  &__pairing-card {
    height: 100%;
    border-radius: 6px;
    transition: box-shadow 0.2s;

    &:hover {
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
    }

    &--disabled {
      opacity: 0.6;
    }
  }

  &__pairing-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }

  &__pairing-name {
    font-weight: 600;
    font-size: 15px;
    color: #262626;
  }

  &__pairing-meta {
    font-size: 12px;
    color: #595959;
    line-height: 1.8;

    .label {
      color: #8c8c8c;
    }
  }

  &__pairing-stats {
    .stat-num {
      font-size: 18px;
      font-weight: 600;
      color: #262626;
    }

    .stat-label {
      font-size: 11px;
      color: #8c8c8c;
      margin-top: 2px;
    }
  }

  &__ellipsis {
    display: inline-block;
    max-width: 130px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    vertical-align: middle;
  }
}
</style>
