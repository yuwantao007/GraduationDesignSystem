/**
 * 企业负责人指导记录概览页面
 * 功能：企业负责人查看本企业所有指导记录，支持筛选、分页、导出
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-16
 */
<template>
  <div class="leader-guidance-container">
    <!-- 页面标题 -->
    <a-page-header
      title="指导记录总览"
      sub-title="查看企业内所有教师的指导记录"
    />

    <!-- 搜索筛选区域 -->
    <a-card class="filter-card" :bordered="false">
      <a-form layout="inline" :model="queryParams">
        <a-form-item label="学生姓名">
          <a-input
            v-model:value="queryParams.studentName"
            placeholder="请输入学生姓名"
            allowClear
            style="width: 160px"
          />
        </a-form-item>
        <a-form-item label="指导类型">
          <a-select
            v-model:value="queryParams.guidanceType"
            placeholder="全部类型"
            allowClear
            style="width: 140px"
          >
            <a-select-option :value="GuidanceType.PROJECT">项目指导</a-select-option>
            <a-select-option :value="GuidanceType.THESIS">论文指导</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="指导日期">
          <a-range-picker
            v-model:value="dateRange"
            format="YYYY-MM-DD"
            style="width: 240px"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <SearchOutlined />
              查询
            </a-button>
            <a-button @click="handleReset">
              <ReloadOutlined />
              重置
            </a-button>
            <a-button type="default" @click="handleExport">
              <DownloadOutlined />
              导出Excel
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 统计卡片 -->
    <a-row :gutter="16" class="statistics-row">
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="总指导记录"
            :value="statistics.totalRecords"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <FileTextOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="项目指导"
            :value="statistics.projectRecords"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <BookOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="论文指导"
            :value="statistics.thesisRecords"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <FormOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="总指导时长"
            :value="statistics.totalHours"
            suffix="小时"
            :value-style="{ color: '#faad14' }"
          >
            <template #prefix>
              <ClockCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 数据表格 -->
    <a-card :bordered="false">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        row-key="recordId"
        @change="handleTableChange"
      >
        <!-- 学生信息列 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'studentInfo'">
            <div class="student-info">
              <span class="name">{{ record.studentName }}</span>
              <span class="no">（{{ record.studentNo }}）</span>
            </div>
          </template>

          <!-- 指导类型列 -->
          <template v-else-if="column.key === 'guidanceType'">
            <a-tag :color="GuidanceTypeColorMap[record.guidanceType]">
              {{ GuidanceTypeMap[record.guidanceType] }}
            </a-tag>
          </template>

          <!-- 指导教师列 -->
          <template v-else-if="column.key === 'teacherName'">
            <span>{{ record.teacherName }}</span>
          </template>

          <!-- 课题名称列 -->
          <template v-else-if="column.key === 'topicTitle'">
            <a-tooltip :title="record.topicTitle">
              <span class="topic-title">{{ record.topicTitle }}</span>
            </a-tooltip>
          </template>

          <!-- 指导方式列 -->
          <template v-else-if="column.key === 'guidanceMethod'">
            <span>{{ record.guidanceMethod || '-' }}</span>
          </template>

          <!-- 指导时长列 -->
          <template v-else-if="column.key === 'durationHours'">
            <span>{{ record.durationHours ? `${record.durationHours}小时` : '-' }}</span>
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="showDetail(record)">
              <EyeOutlined />
              查看详情
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情抽屉 -->
    <a-drawer
      v-model:open="detailDrawerVisible"
      title="指导记录详情"
      placement="right"
      width="550"
    >
      <template v-if="currentRecord">
        <a-descriptions :column="1" bordered size="middle">
          <a-descriptions-item label="学生姓名">
            {{ currentRecord.studentName }}（{{ currentRecord.studentNo }}）
          </a-descriptions-item>
          <a-descriptions-item label="指导教师">
            {{ currentRecord.teacherName }}
          </a-descriptions-item>
          <a-descriptions-item label="指导类型">
            <a-tag :color="GuidanceTypeColorMap[currentRecord.guidanceType]">
              {{ GuidanceTypeMap[currentRecord.guidanceType] }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="关联课题">
            {{ currentRecord.topicTitle }}
          </a-descriptions-item>
          <a-descriptions-item label="指导日期">
            {{ currentRecord.guidanceDate }}
          </a-descriptions-item>
          <a-descriptions-item label="指导方式" v-if="currentRecord.guidanceMethod">
            {{ currentRecord.guidanceMethod }}
          </a-descriptions-item>
          <a-descriptions-item label="指导时长" v-if="currentRecord.durationHours">
            {{ currentRecord.durationHours }} 小时
          </a-descriptions-item>
        </a-descriptions>

        <a-divider orientation="left">指导内容</a-divider>
        <div class="content-detail">
          {{ currentRecord.guidanceContent }}
        </div>

        <a-divider />
        <div class="record-meta">
          <p><ClockCircleOutlined /> 创建时间：{{ currentRecord.createTime }}</p>
          <p><EditOutlined /> 更新时间：{{ currentRecord.updateTime }}</p>
        </div>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
/**
 * 企业负责人指导记录概览页面逻辑
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { TablePaginationConfig, TableProps } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  DownloadOutlined,
  FileTextOutlined,
  BookOutlined,
  FormOutlined,
  ClockCircleOutlined,
  EyeOutlined,
  EditOutlined
} from '@ant-design/icons-vue'
import { guidanceApi } from '@/api/guidance'
import {
  GuidanceType,
  GuidanceTypeMap,
  GuidanceTypeColorMap,
  type GuidanceListVO,
  type GuidanceRecordVO,
  type GuidanceQueryParams
} from '@/types/guidance'
import dayjs, { type Dayjs } from 'dayjs'

// ==================== 响应式状态 ====================

/** 加载状态 */
const loading = ref<boolean>(false)

/** 表格数据 */
const tableData = ref<GuidanceListVO[]>([])

/** 查询参数 */
const queryParams = reactive<GuidanceQueryParams>({
  studentName: undefined,
  guidanceType: undefined,
  startDate: undefined,
  endDate: undefined,
  pageNum: 1,
  pageSize: 10
})

/** 日期范围选择器 */
const dateRange = ref<[Dayjs, Dayjs] | null>(null)

/** 分页配置 */
const pagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

/** 统计数据 */
const statistics = reactive({
  totalRecords: 0,
  projectRecords: 0,
  thesisRecords: 0,
  totalHours: 0
})

/** 详情抽屉可见性 */
const detailDrawerVisible = ref<boolean>(false)

/** 当前查看的记录详情 */
const currentRecord = ref<GuidanceRecordVO | null>(null)

// ==================== 表格列定义 ====================

const columns = [
  {
    title: '学生信息',
    key: 'studentInfo',
    width: 160
  },
  {
    title: '指导教师',
    key: 'teacherName',
    dataIndex: 'teacherName',
    width: 100
  },
  {
    title: '指导类型',
    key: 'guidanceType',
    width: 100,
    align: 'center' as const
  },
  {
    title: '课题名称',
    key: 'topicTitle',
    ellipsis: true,
    width: 200
  },
  {
    title: '指导日期',
    dataIndex: 'guidanceDate',
    key: 'guidanceDate',
    width: 110,
    sorter: true
  },
  {
    title: '指导方式',
    key: 'guidanceMethod',
    width: 90
  },
  {
    title: '指导时长',
    key: 'durationHours',
    width: 90,
    align: 'center' as const
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
    fixed: 'right' as const
  }
]

// ==================== 方法定义 ====================

/**
 * 加载指导记录列表
 */
const loadGuidanceList = async () => {
  loading.value = true
  try {
    // 处理日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      queryParams.startDate = dateRange.value[0].format('YYYY-MM-DD')
      queryParams.endDate = dateRange.value[1].format('YYYY-MM-DD')
    } else {
      queryParams.startDate = undefined
      queryParams.endDate = undefined
    }

    const res = await guidanceApi.getLeaderGuidanceOverview(queryParams)
    
    if (res.data) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
      
      // 更新统计数据
      calculateStatistics()
    }
  } catch (error: any) {
    console.error('加载指导记录失败:', error)
    message.error(error.message || '加载指导记录失败')
  } finally {
    loading.value = false
  }
}

/**
 * 计算统计数据
 */
const calculateStatistics = () => {
  statistics.totalRecords = pagination.total as number
  
  // 从当前页数据计算（实际项目中可能需要后端提供统计API）
  const projectCount = tableData.value.filter(
    item => item.guidanceType === GuidanceType.PROJECT
  ).length
  const thesisCount = tableData.value.filter(
    item => item.guidanceType === GuidanceType.THESIS
  ).length
  const hours = tableData.value.reduce(
    (sum, item) => sum + (item.durationHours || 0), 0
  )
  
  // 注意：此处统计仅为当前页数据，完整统计需要后端API支持
  statistics.projectRecords = projectCount
  statistics.thesisRecords = thesisCount
  statistics.totalHours = hours
}

/**
 * 查询按钮点击
 */
const handleSearch = () => {
  queryParams.pageNum = 1
  pagination.current = 1
  loadGuidanceList()
}

/**
 * 重置查询条件
 */
const handleReset = () => {
  queryParams.studentName = undefined
  queryParams.guidanceType = undefined
  dateRange.value = null
  queryParams.startDate = undefined
  queryParams.endDate = undefined
  queryParams.pageNum = 1
  pagination.current = 1
  loadGuidanceList()
}

/**
 * 表格分页变化
 */
const handleTableChange: TableProps['onChange'] = (pag) => {
  queryParams.pageNum = pag.current || 1
  queryParams.pageSize = pag.pageSize || 10
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  loadGuidanceList()
}

/**
 * 导出Excel
 */
const handleExport = () => {
  const exportUrl = guidanceApi.getExportGuidanceUrl()
  window.open(exportUrl, '_blank')
  message.success('正在导出，请稍候...')
}

/**
 * 查看详情
 */
const showDetail = async (record: GuidanceListVO) => {
  try {
    const res = await guidanceApi.getGuidanceRecordDetail(record.recordId)
    currentRecord.value = res.data
    detailDrawerVisible.value = true
  } catch (error: any) {
    console.error('加载详情失败:', error)
    message.error(error.message || '加载详情失败')
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadGuidanceList()
})
</script>

<style scoped lang="less">
.leader-guidance-container {
  padding: 24px;

  .filter-card {
    margin-bottom: 16px;
  }

  .statistics-row {
    margin-bottom: 16px;
  }

  .student-info {
    .name {
      font-weight: 500;
    }
    .no {
      color: #999;
      font-size: 12px;
    }
  }

  .topic-title {
    display: inline-block;
    max-width: 180px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .content-detail {
    padding: 16px;
    background: #f9f9f9;
    border-radius: 4px;
    line-height: 1.8;
    white-space: pre-wrap;
    word-break: break-word;
    min-height: 100px;
  }

  .record-meta {
    color: #999;
    font-size: 13px;

    p {
      margin: 4px 0;
    }
  }
}
</style>
