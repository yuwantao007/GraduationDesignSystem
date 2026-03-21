<template>
  <div class="report-list">
    <a-card title="开题报告管理">
      <!-- 搜索栏 -->
      <a-form layout="inline" :model="queryParams" class="search-form">
        <a-form-item label="学生姓名">
          <a-input v-model:value="queryParams.studentName" placeholder="请输入" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item label="审查状态">
          <a-select v-model:value="queryParams.status" placeholder="请选择" allow-clear style="width: 140px">
            <a-select-option v-for="(name, key) in OpeningReportStatusMap" :key="key" :value="Number(key)">
              {{ name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="reportId"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'reviewStatus'">
            <a-tag :color="OpeningReportStatusColor[record.status as OpeningReportStatus]">
              {{ record.statusName }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">详情</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { defenseApi } from '@/api/defense'
import { OpeningReportStatus, OpeningReportStatusMap, OpeningReportStatusColor } from '@/types/defense'
import type { OpeningReportVO, ReportQueryDTO } from '@/types/defense'

const loading = ref(false)
const tableData = ref<OpeningReportVO[]>([])

const queryParams = reactive<ReportQueryDTO>({
  studentName: undefined,
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const columns = [
  { title: '学生姓名', dataIndex: 'studentName', key: 'studentName', width: 100 },
  { title: '学号', dataIndex: 'studentNo', key: 'studentNo', width: 120 },
  { title: '课题名称', dataIndex: 'topicName', key: 'topicName', width: 200, ellipsis: true },
  { title: '提交时间', dataIndex: 'submitTime', key: 'submitTime', width: 160 },
  { title: '审查状态', dataIndex: 'reviewStatus', key: 'reviewStatus', width: 100 },
  { title: '指导教师', dataIndex: 'advisorNames', key: 'advisorNames', width: 180 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await defenseApi.pageReports({
      ...queryParams,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  queryParams.studentName = undefined
  queryParams.status = undefined
  handleSearch()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const handleView = (record: OpeningReportVO) => {
  message.info('查看详情: ' + record.reportId)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.report-list {
  padding: 16px;
}
.search-form {
  margin-bottom: 16px;
}
</style>
