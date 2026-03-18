<template>
  <div class="report-list">
    <a-card title="开题报告管理">
      <!-- 搜索栏 -->
      <a-form layout="inline" :model="queryParams" class="search-form">
        <a-form-item label="学生姓名">
          <a-input v-model:value="queryParams.studentName" placeholder="请输入" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item label="审查状态">
          <a-select v-model:value="queryParams.reviewStatus" placeholder="请选择" allow-clear style="width: 140px">
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
            <a-tag :color="OpeningReportStatusColor[record.reviewStatus as OpeningReportStatus]">
              {{ record.reviewStatusName }}
            </a-tag>
          </template>
          <template v-if="column.key === 'documentName'">
            <a v-if="record.documentId" :href="record.documentUrl" target="_blank">
              {{ record.documentName || '查看文件' }}
            </a>
            <span v-else>-</span>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">详情</a-button>
              <a-button
                v-if="record.reviewStatus === OpeningReportStatus.SUBMITTED"
                type="link"
                size="small"
                @click="handleReview(record)"
              >
                审查
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 审查弹窗 -->
    <a-modal
      v-model:open="reviewVisible"
      title="审查开题报告"
      :confirm-loading="submitLoading"
      @ok="handleSubmitReview"
    >
      <a-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" :label-col="{ span: 6 }">
        <a-form-item label="学生姓名">
          <span>{{ currentRecord?.studentName }}</span>
        </a-form-item>
        <a-form-item label="课题名称">
          <span>{{ currentRecord?.topicName }}</span>
        </a-form-item>
        <a-form-item label="报告文件">
          <a v-if="currentRecord?.documentId" :href="currentRecord?.documentUrl" target="_blank">
            {{ currentRecord?.documentName || '查看文件' }}
          </a>
        </a-form-item>
        <a-form-item label="审查结果" name="reviewStatus">
          <a-radio-group v-model:value="reviewForm.reviewStatus">
            <a-radio :value="OpeningReportStatus.PASSED">通过</a-radio>
            <a-radio :value="OpeningReportStatus.FAILED">不合格</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="审查意见" name="reviewComment">
          <a-textarea v-model:value="reviewForm.reviewComment" placeholder="请输入审查意见" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { defenseApi } from '@/api/defense'
import { OpeningReportStatus, OpeningReportStatusMap, OpeningReportStatusColor } from '@/types/defense'
import type { OpeningReportVO, ReportQueryDTO, ReviewReportDTO } from '@/types/defense'
import type { FormInstance } from 'ant-design-vue'

const loading = ref(false)
const submitLoading = ref(false)
const reviewVisible = ref(false)
const tableData = ref<OpeningReportVO[]>([])
const reviewFormRef = ref<FormInstance>()
const currentRecord = ref<OpeningReportVO | null>(null)

const queryParams = reactive<ReportQueryDTO>({
  studentName: undefined,
  reviewStatus: undefined,
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

const reviewForm = reactive<ReviewReportDTO>({
  reportId: '',
  reviewStatus: OpeningReportStatus.PASSED,
  reviewComment: ''
})

const reviewRules = {
  reviewStatus: [{ required: true, message: '请选择审查结果' }]
}

const columns = [
  { title: '学生姓名', dataIndex: 'studentName', key: 'studentName', width: 100 },
  { title: '学号', dataIndex: 'studentNo', key: 'studentNo', width: 120 },
  { title: '课题名称', dataIndex: 'topicName', key: 'topicName', width: 200, ellipsis: true },
  { title: '报告文件', dataIndex: 'documentName', key: 'documentName', width: 150 },
  { title: '提交时间', dataIndex: 'submitTime', key: 'submitTime', width: 160 },
  { title: '审查状态', dataIndex: 'reviewStatus', key: 'reviewStatus', width: 100 },
  { title: '审查人', dataIndex: 'reviewerName', key: 'reviewerName', width: 100 },
  { title: '审查时间', dataIndex: 'reviewTime', key: 'reviewTime', width: 160 },
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
  queryParams.reviewStatus = undefined
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

const handleReview = (record: OpeningReportVO) => {
  currentRecord.value = record
  reviewForm.reportId = record.reportId
  reviewForm.reviewStatus = OpeningReportStatus.PASSED
  reviewForm.reviewComment = ''
  reviewVisible.value = true
}

const handleSubmitReview = async () => {
  try {
    await reviewFormRef.value?.validate()
    submitLoading.value = true
    const res = await defenseApi.reviewReport(reviewForm)
    if (res.code === 200) {
      message.success('审查完成')
      reviewVisible.value = false
      fetchData()
    }
  } catch (err) {
    // 验证失败
  } finally {
    submitLoading.value = false
  }
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
