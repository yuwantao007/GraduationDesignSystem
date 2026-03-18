<template>
  <div class="my-report">
    <a-card title="我的开题报告">
      <template v-if="reportData">
        <!-- 报告状态 -->
        <a-descriptions bordered :column="2">
          <a-descriptions-item label="课题名称" :span="2">
            {{ reportData.topicName }}
          </a-descriptions-item>
          <a-descriptions-item label="审查状态">
            <a-tag :color="OpeningReportStatusColor[reportData.reviewStatus as OpeningReportStatus]">
              {{ reportData.reviewStatusName }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="提交时间">
            {{ reportData.submitTime || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="报告文件">
            <a v-if="reportData.documentId" :href="reportData.documentUrl" target="_blank">
              {{ reportData.documentName || '查看文件' }}
            </a>
            <span v-else>-</span>
          </a-descriptions-item>
          <a-descriptions-item label="审查人">
            {{ reportData.reviewerName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="审查时间" :span="2">
            {{ reportData.reviewTime || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="审查意见" :span="2" v-if="reportData.reviewComment">
            <a-alert
              :type="reportData.reviewStatus === OpeningReportStatus.PASSED ? 'success' : 'error'"
              :message="reportData.reviewComment"
              show-icon
            />
          </a-descriptions-item>
        </a-descriptions>

        <!-- 重新提交（不合格时显示） -->
        <div v-if="reportData.reviewStatus === OpeningReportStatus.FAILED" class="resubmit-section">
          <a-alert message="您的开题报告审查不合格，请根据审查意见修改后重新提交" type="warning" show-icon />
          <a-button type="primary" @click="showUploadModal" style="margin-top: 16px">
            重新提交开题报告
          </a-button>
        </div>
      </template>

      <!-- 未提交状态 -->
      <template v-else>
        <a-empty description="您还未提交开题报告">
          <a-button type="primary" @click="showUploadModal">
            提交开题报告
          </a-button>
        </a-empty>
      </template>
    </a-card>

    <!-- 上传弹窗 -->
    <a-modal
      v-model:open="uploadVisible"
      title="提交开题报告"
      :confirm-loading="submitLoading"
      @ok="handleSubmit"
    >
      <a-form ref="formRef" :model="submitForm" :rules="formRules" :label-col="{ span: 6 }">
        <a-form-item label="课题" name="topicId">
          <a-select v-model:value="submitForm.topicId" placeholder="请选择课题" :options="topicOptions" />
        </a-form-item>
        <a-form-item label="答辩安排" name="arrangementId">
          <a-select
            v-model:value="submitForm.arrangementId"
            placeholder="请选择答辩安排（可选）"
            :options="arrangementOptions"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="报告文件" name="documentId">
          <a-upload
            v-model:file-list="fileList"
            :max-count="1"
            :before-upload="beforeUpload"
            accept=".doc,.docx,.pdf"
          >
            <a-button>
              <template #icon><UploadOutlined /></template>
              选择文件
            </a-button>
          </a-upload>
          <div class="upload-tip">支持 .doc, .docx, .pdf 格式</div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import { defenseApi } from '@/api/defense'
import { OpeningReportStatus, OpeningReportStatusColor } from '@/types/defense'
import type { OpeningReportVO, SubmitReportDTO } from '@/types/defense'
import type { FormInstance, UploadProps } from 'ant-design-vue'

const loading = ref(false)
const submitLoading = ref(false)
const uploadVisible = ref(false)
const reportData = ref<OpeningReportVO | null>(null)
const formRef = ref<FormInstance>()
const fileList = ref<any[]>([])

const topicOptions = ref<{ label: string; value: string }[]>([])
const arrangementOptions = ref<{ label: string; value: string }[]>([])

const submitForm = reactive<SubmitReportDTO>({
  topicId: '',
  arrangementId: undefined,
  documentId: ''
})

const formRules = {
  topicId: [{ required: true, message: '请选择课题' }],
  documentId: [{ required: true, message: '请上传报告文件' }]
}

const fetchMyReport = async () => {
  loading.value = true
  try {
    const res = await defenseApi.getMyReport()
    if (res.code === 200) {
      reportData.value = res.data
    }
  } finally {
    loading.value = false
  }
}

const showUploadModal = () => {
  submitForm.topicId = reportData.value?.topicId || ''
  submitForm.arrangementId = undefined
  submitForm.documentId = ''
  fileList.value = []
  uploadVisible.value = true
}

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  // 模拟上传获取documentId，实际应调用文档上传接口
  message.info('文件将在提交时上传: ' + file.name)
  return false
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    if (fileList.value.length === 0) {
      message.error('请选择报告文件')
      return
    }
    submitLoading.value = true

    // 实际项目中应先上传文件获取documentId
    // 这里模拟documentId
    submitForm.documentId = 'mock-document-id'

    const res = await defenseApi.submitReport(submitForm)
    if (res.code === 200) {
      message.success('提交成功')
      uploadVisible.value = false
      fetchMyReport()
    }
  } catch (err) {
    // 验证失败
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchMyReport()
})
</script>

<style scoped>
.my-report {
  padding: 16px;
}
.resubmit-section {
  margin-top: 24px;
}
.upload-tip {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}
</style>
