<!--
  阶段切换记录页面
  展示系统阶段切换的历史记录，包含时间线和表格两种视图
  仅系统管理员可访问

  @author 系统架构师
  @version 1.0
  @since 2026-03-07
-->
<template>
  <div class="phase-records">
    <a-card :bordered="false" :loading="loading">
      <template #title>
        <HistoryOutlined style="margin-right: 8px" />
        阶段切换记录
      </template>
      <template #extra>
        <a-radio-group v-model:value="viewMode" button-style="solid" size="small">
          <a-radio-button value="timeline">
            <UnorderedListOutlined /> 时间线
          </a-radio-button>
          <a-radio-button value="table">
            <TableOutlined /> 表格
          </a-radio-button>
        </a-radio-group>
      </template>

      <!-- 无数据提示 -->
      <a-empty v-if="records.length === 0" description="暂无切换记录" />

      <!-- 时间线视图 -->
      <a-timeline v-if="viewMode === 'timeline' && records.length > 0" mode="left">
        <a-timeline-item
          v-for="record in records"
          :key="record.recordId"
          :color="record.isCurrent ? 'blue' : 'green'"
        >
          <template #dot>
            <SyncOutlined v-if="record.isCurrent" spin style="font-size: 16px" />
          </template>
          <a-card size="small" :bordered="true" class="phase-records__timeline-card">
            <div class="phase-records__timeline-header">
              <a-tag :color="record.isCurrent ? 'processing' : 'success'">
                {{ record.phaseName }}
              </a-tag>
              <span class="phase-records__timeline-time">{{ record.switchTime }}</span>
            </div>
            <div class="phase-records__timeline-body">
              <p v-if="record.previousPhaseName">
                <span class="phase-records__label">切换路径：</span>
                <a-tag color="default">{{ record.previousPhaseName }}</a-tag>
                <SwapRightOutlined style="margin: 0 4px" />
                <a-tag :color="record.isCurrent ? 'blue' : 'green'">{{ record.phaseName }}</a-tag>
              </p>
              <p v-else>
                <span class="phase-records__label">操作类型：</span>
                <a-tag color="purple">系统初始化</a-tag>
              </p>
              <p>
                <span class="phase-records__label">操作人：</span>
                {{ record.operatorName }}
              </p>
              <p>
                <span class="phase-records__label">毕业届别：</span>
                {{ record.cohort }}
              </p>
              <p>
                <span class="phase-records__label">原因：</span>
                {{ record.switchReason }}
              </p>
            </div>
          </a-card>
        </a-timeline-item>
      </a-timeline>

      <!-- 表格视图 -->
      <a-table
        v-if="viewMode === 'table' && records.length > 0"
        :columns="columns"
        :data-source="records"
        :row-key="(record: PhaseRecordVO) => record.recordId"
        :pagination="false"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'phaseName'">
            <a-tag :color="record.isCurrent ? 'processing' : 'success'">
              {{ record.phaseName }}
            </a-tag>
          </template>
          <template v-if="column.dataIndex === 'previousPhaseName'">
            <a-tag v-if="record.previousPhaseName" color="default">
              {{ record.previousPhaseName }}
            </a-tag>
            <span v-else style="color: #8c8c8c">--</span>
          </template>
          <template v-if="column.dataIndex === 'isCurrent'">
            <a-badge v-if="record.isCurrent" status="processing" text="当前阶段" />
            <a-badge v-else status="default" text="历史阶段" />
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  HistoryOutlined,
  UnorderedListOutlined,
  TableOutlined,
  SyncOutlined,
  SwapRightOutlined
} from '@ant-design/icons-vue'
import { phaseApi } from '@/api/phase'
import type { PhaseRecordVO } from '@/types/phase'

defineOptions({
  name: 'PhaseRecords'
})

// 视图模式
const viewMode = ref<'timeline' | 'table'>('timeline')

// 加载状态
const loading = ref(false)

// 记录数据
const records = ref<PhaseRecordVO[]>([])

// 表格列配置
const columns = [
  {
    title: '阶段序号',
    dataIndex: 'phaseOrder',
    key: 'phaseOrder',
    width: 90,
    align: 'center' as const
  },
  {
    title: '当前阶段',
    dataIndex: 'phaseName',
    key: 'phaseName',
    width: 120
  },
  {
    title: '上一阶段',
    dataIndex: 'previousPhaseName',
    key: 'previousPhaseName',
    width: 120
  },
  {
    title: '毕业届别',
    dataIndex: 'cohort',
    key: 'cohort',
    width: 100
  },
  {
    title: '操作人',
    dataIndex: 'operatorName',
    key: 'operatorName',
    width: 100
  },
  {
    title: '切换原因',
    dataIndex: 'switchReason',
    key: 'switchReason',
    ellipsis: true
  },
  {
    title: '切换时间',
    dataIndex: 'switchTime',
    key: 'switchTime',
    width: 180
  },
  {
    title: '状态',
    dataIndex: 'isCurrent',
    key: 'isCurrent',
    width: 110,
    align: 'center' as const
  }
]

/**
 * 加载切换记录
 */
const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await phaseApi.getPhaseRecords()
    records.value = res.data
  } catch (error: any) {
    // 错误由拦截器处理
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped lang="scss">
.phase-records {
  &__timeline-card {
    max-width: 500px;
  }

  &__timeline-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }

  &__timeline-time {
    color: #8c8c8c;
    font-size: 13px;
  }

  &__timeline-body {
    p {
      margin-bottom: 4px;
      font-size: 13px;
      color: #595959;
    }
  }

  &__label {
    color: #8c8c8c;
    display: inline-block;
    width: 70px;
  }
}
</style>
