<template>
  <a-modal
    :open="open"
    title="Excel 批量导入专业"
    width="680px"
    :footer="null"
    :destroy-on-close="true"
    @cancel="handleClose"
  >
    <!-- 步骤导航 -->
    <a-steps :current="currentStep" size="small" style="margin-bottom: 24px">
      <a-step title="上传文件" />
      <a-step title="导入结果" />
    </a-steps>

    <!-- Step 0: 上传文件 -->
    <div v-if="currentStep === 0">
      <!-- 下载模板提示 -->
      <a-alert
        type="info"
        show-icon
        style="margin-bottom: 16px"
      >
        <template #message>
          请先下载 Excel 模板，按格式填写后上传。
          <a-button type="link" size="small" :loading="templateDownloading" @click="handleDownloadTemplate">
            <DownloadOutlined /> 下载模板
          </a-button>
        </template>
      </a-alert>

      <!-- 系统管理员可选择限定企业 -->
      <a-form-item
        v-if="userStore.hasAnyRole(['SYSTEM_ADMIN'])"
        label="限定企业（可选）"
        :label-col="{ span: 6 }"
        style="margin-bottom: 16px"
      >
        <a-select
          v-model:value="selectedEnterpriseId"
          placeholder="不限制（Excel中企业名称列生效）"
          allow-clear
          show-search
          :options="enterpriseOptions"
          :loading="enterpriseLoading"
          style="width: 100%"
          option-filter-prop="label"
        />
      </a-form-item>

      <!-- 上传区域 -->
      <a-upload-dragger
        v-model:file-list="fileList"
        accept=".xlsx,.xls"
        :max-count="1"
        :before-upload="beforeUpload"
        :show-upload-list="true"
        :disabled="uploading"
      >
        <p class="ant-upload-drag-icon">
          <InboxOutlined />
        </p>
        <p class="ant-upload-text">点击或拖拽 Excel 文件到此处</p>
        <p class="ant-upload-hint">仅支持 .xlsx / .xls 格式，文件大小不超过 5MB</p>
      </a-upload-dragger>

      <div style="margin-top: 16px; text-align: right">
        <a-space>
          <a-button @click="handleClose">取消</a-button>
          <a-button
            type="primary"
            :disabled="fileList.length === 0"
            :loading="uploading"
            @click="handleImport"
          >
            <UploadOutlined /> 开始导入
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- Step 1: 导入结果 -->
    <div v-else-if="currentStep === 1 && importResult">
      <!-- 统计卡片 -->
      <a-row :gutter="12" style="margin-bottom: 20px">
        <a-col :span="8">
          <a-statistic
            title="创建专业方向"
            :value="importResult.directionCreatedCount"
            :value-style="{ color: '#1677ff' }"
          />
        </a-col>
        <a-col :span="8">
          <a-statistic
            title="创建专业"
            :value="importResult.majorCreatedCount"
            :value-style="{ color: '#52c41a' }"
          />
        </a-col>
        <a-col :span="8">
          <a-statistic
            title="关联教师"
            :value="importResult.teacherLinkedCount"
            :value-style="{ color: '#722ed1' }"
          />
        </a-col>
      </a-row>
      <a-row :gutter="12" style="margin-bottom: 20px">
        <a-col :span="8">
          <a-statistic
            title="已跳过（已存在）"
            :value="importResult.skipCount"
            :value-style="{ color: '#8c8c8c' }"
          />
        </a-col>
        <a-col :span="8">
          <a-statistic
            title="失败行数"
            :value="importResult.failureCount"
            :value-style="{ color: importResult.failureCount > 0 ? '#ff4d4f' : '#52c41a' }"
          />
        </a-col>
        <a-col :span="8">
          <a-statistic
            title="总处理行数"
            :value="importResult.totalCount"
          />
        </a-col>
      </a-row>

      <!-- 成功提示 -->
      <a-alert
        v-if="importResult.failureCount === 0"
        type="success"
        message="导入完成，所有数据均已成功处理！"
        show-icon
        style="margin-bottom: 16px"
      />

      <!-- 错误列表 -->
      <template v-if="importResult.errors && importResult.errors.length > 0">
        <a-alert
          type="warning"
          :message="`以下 ${importResult.errors.length} 行数据导入失败，请检查修正后重新导入`"
          show-icon
          style="margin-bottom: 12px"
        />
        <a-table
          :columns="errorColumns"
          :data-source="importResult.errors"
          :pagination="importResult.errors.length > 10 ? { pageSize: 10 } : false"
          size="small"
          :scroll="{ y: 240 }"
          row-key="rowNum"
        />
      </template>

      <div style="margin-top: 20px; text-align: right">
        <a-space>
          <a-button @click="handleReset">继续导入</a-button>
          <a-button type="primary" @click="handleFinish">完成</a-button>
        </a-space>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * Excel 批量导入专业弹窗组件
 * @author YuWan
 * @date 2026-03-07
 */
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { UploadFile } from 'ant-design-vue'
import {
  DownloadOutlined,
  UploadOutlined,
  InboxOutlined
} from '@ant-design/icons-vue'
import { majorApi } from '@/api/major'
import { enterpriseApi } from '@/api/enterprise'
import type { ImportMajorResultVO } from '@/types/major'
import { useUserStore } from '@/stores/user'

defineOptions({ name: 'ImportMajorModal' })

// Props
const props = defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const userStore = useUserStore()

// 步骤状态
const currentStep = ref(0)

// 文件列表
const fileList = ref<UploadFile[]>([])
const uploading = ref(false)
const templateDownloading = ref(false)

// 系统管理员企业选择
const selectedEnterpriseId = ref<string | undefined>(undefined)
const enterpriseOptions = ref<{ value: string; label: string }[]>([])
const enterpriseLoading = ref(false)

// 导入结果
const importResult = ref<ImportMajorResultVO | null>(null)

// 错误表格列
const errorColumns = [
  { title: '行号', dataIndex: 'rowNum', width: 80 },
  { title: '错误信息', dataIndex: 'errorMsg', ellipsis: true }
]

/**
 * 加载企业选项（系统管理员）
 */
const loadEnterpriseOptions = async () => {
  if (!userStore.hasAnyRole(['SYSTEM_ADMIN'])) return
  enterpriseLoading.value = true
  try {
    const res = await enterpriseApi.getAllEnterprises()
    enterpriseOptions.value = (res.data || []).map((e: any) => ({
      value: e.enterpriseId,
      label: e.enterpriseName
    }))
  } catch {
    // 静默失败
  } finally {
    enterpriseLoading.value = false
  }
}

onMounted(loadEnterpriseOptions)

/**
 * 拦截上传，返回 false 阻止自动上传
 */
const beforeUpload = (file: File): boolean => {
  const isExcel =
    file.name.endsWith('.xlsx') || file.name.endsWith('.xls')
  if (!isExcel) {
    message.error('只能上传 Excel 文件（.xlsx 或 .xls）')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    message.error('文件大小不能超过 5MB')
    return false
  }
  // 替换 fileList（因为 max-count=1）
  fileList.value = [file as unknown as UploadFile]
  return false
}

/**
 * 下载导入模板
 */
const handleDownloadTemplate = async () => {
  templateDownloading.value = true
  try {
    const response = await majorApi.downloadImportTemplate()
    const blob = new Blob([response as unknown as BlobPart], {
      type: 'application/vnd.ms-excel'
    })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '专业导入模板.xls'
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    message.error('模板下载失败，请稍后重试')
  } finally {
    templateDownloading.value = false
  }
}

/**
 * 执行导入
 */
const handleImport = async () => {
  if (fileList.value.length === 0) {
    message.warning('请先选择要导入的 Excel 文件')
    return
  }

  uploading.value = true
  try {
    const file = (fileList.value[0] as any).originFileObj ?? fileList.value[0]
    const res = await majorApi.importMajors(file as File, selectedEnterpriseId.value)
    importResult.value = res.data
    currentStep.value = 1
    // 只要有成功创建的数据就触发刷新
    if (
      (res.data.directionCreatedCount ?? 0) > 0 ||
      (res.data.majorCreatedCount ?? 0) > 0
    ) {
      emit('success')
    }
  } catch (err: any) {
    message.error(err?.response?.data?.message || '导入失败，请检查文件格式')
  } finally {
    uploading.value = false
  }
}

/**
 * 重置到上传步骤以继续导入
 */
const handleReset = () => {
  currentStep.value = 0
  fileList.value = []
  importResult.value = null
}

/**
 * 完成并关闭弹窗
 */
const handleFinish = () => {
  emit('update:open', false)
  setTimeout(handleReset, 300)
}

/**
 * 关闭弹窗
 */
const handleClose = () => {
  emit('update:open', false)
  setTimeout(handleReset, 300)
}
</script>
