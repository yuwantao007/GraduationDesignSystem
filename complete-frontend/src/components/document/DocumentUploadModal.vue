<template>
  <a-modal
    v-model:open="visible"
    :title="isTeacherMode ? '上传批注文档' : '上传文档'"
    :width="520"
    :confirm-loading="uploading"
    @ok="handleUpload"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item v-if="isTeacherMode" label="选择学生" name="studentId">
        <a-select
          v-model:value="formData.studentId"
          placeholder="请选择学生"
          show-search
          :filter-option="filterStudentOption"
        >
          <a-select-option
            v-for="student in studentList"
            :key="student.studentId"
            :value="student.studentId"
          >
            {{ student.studentName }}（{{ student.studentNo }}）
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item v-if="!isTeacherMode" label="文档类型" name="documentType">
        <a-select v-model:value="formData.documentType" placeholder="请选择文档类型">
          <a-select-option
            v-for="option in documentTypeOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="选择文件" name="file">
        <a-upload
          v-model:file-list="fileList"
          :before-upload="beforeUpload"
          :max-count="1"
          :accept="acceptFileTypes"
        >
          <a-button>
            <upload-outlined />
            点击选择文件
          </a-button>
        </a-upload>
        <div class="upload-tip">
          支持的文件格式：{{ acceptFileTypes }}，单个文件最大 50MB
        </div>
      </a-form-item>

      <a-form-item label="备注" name="remark">
        <a-textarea
          v-model:value="formData.remark"
          placeholder="请输入备注信息（可选）"
          :rows="3"
          :maxlength="500"
          show-count
        />
      </a-form-item>
    </a-form>

    <div v-if="uploadProgress > 0 && uploadProgress < 100" class="upload-progress">
      <a-progress :percent="uploadProgress" />
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import type { UploadProps, FormInstance } from 'ant-design-vue'
import { documentApi } from '@/api/document'
import { DocumentType, DocumentTypeOptions } from '@/types/document'
import type { DocumentInfoVO } from '@/types/document'

interface Props {
  open: boolean
  isTeacherMode?: boolean  // 教师模式（上传批注）
  studentList?: { studentId: string; studentName: string; studentNo: string }[]
}

const props = withDefaults(defineProps<Props>(), {
  isTeacherMode: false,
  studentList: () => []
})

const emit = defineEmits<{
  'update:open': [value: boolean]
  'success': [document: DocumentInfoVO]
}>()

const visible = computed({
  get: () => props.open,
  set: (val) => emit('update:open', val)
})

const formRef = ref<FormInstance>()
const uploading = ref(false)
const uploadProgress = ref(0)
const fileList = ref<UploadProps['fileList']>([])

const formData = reactive({
  studentId: '',
  documentType: undefined as number | undefined,
  file: null as File | null,
  remark: ''
})

// 学生上传时排除教师批注类型
const documentTypeOptions = computed(() => {
  if (props.isTeacherMode) {
    return []  // 教师模式下不需要选择类型
  }
  return DocumentTypeOptions.filter(o => o.value !== DocumentType.TEACHER_ANNOTATION)
})

// 接受的文件类型
const acceptFileTypes = computed(() => {
  if (props.isTeacherMode) {
    return '.pdf,.doc,.docx'
  }
  switch (formData.documentType) {
    case DocumentType.PROJECT_CODE:
      return '.zip,.rar,.7z,.tar,.gz'
    case DocumentType.THESIS_DOCUMENT:
    case DocumentType.OPENING_REPORT:
    case DocumentType.MIDTERM_CHECK:
      return '.pdf,.doc,.docx'
    default:
      return '.pdf,.doc,.docx,.zip,.rar'
  }
})

const rules = computed(() => ({
  studentId: props.isTeacherMode ? [{ required: true, message: '请选择学生' }] : [],
  documentType: props.isTeacherMode ? [] : [{ required: true, message: '请选择文档类型' }],
  file: [{ required: true, message: '请选择文件', validator: validateFile }]
}))

// 文件验证
const validateFile = async () => {
  if (!fileList.value || fileList.value.length === 0) {
    return Promise.reject('请选择文件')
  }
  return Promise.resolve()
}

// 上传前处理
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  // 检查文件大小（最大50MB）
  const maxSize = 50 * 1024 * 1024
  if (file.size > maxSize) {
    message.error('文件大小不能超过50MB')
    return false
  }

  formData.file = file
  return false  // 阻止自动上传
}

// 筛选学生选项
const filterStudentOption = (input: string, option: any) => {
  const student = props.studentList.find(s => s.studentId === option.value)
  if (!student) return false
  return student.studentName.includes(input) || student.studentNo.includes(input)
}

// 上传文档
const handleUpload = async () => {
  try {
    await formRef.value?.validate()

    if (!formData.file) {
      message.error('请选择文件')
      return
    }

    uploading.value = true
    uploadProgress.value = 10

    let result
    if (props.isTeacherMode) {
      result = await documentApi.uploadTeacherAnnotation(
        formData.file,
        formData.studentId,
        formData.remark
      )
    } else {
      result = await documentApi.uploadDocument(
        formData.file,
        formData.documentType!,
        formData.remark
      )
    }

    uploadProgress.value = 100

    if (result.data) {
      message.success('上传成功')
      emit('success', result.data)
      handleCancel()
    }
  } catch (error: any) {
    console.error('上传失败:', error)
    message.error(error.message || '上传失败')
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

// 取消
const handleCancel = () => {
  formRef.value?.resetFields()
  fileList.value = []
  formData.file = null
  formData.remark = ''
  formData.studentId = ''
  formData.documentType = undefined
  visible.value = false
}

// 监听弹窗关闭重置表单
watch(visible, (val) => {
  if (!val) {
    handleCancel()
  }
})
</script>

<style scoped>
.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

.upload-progress {
  margin-top: 16px;
}
</style>
