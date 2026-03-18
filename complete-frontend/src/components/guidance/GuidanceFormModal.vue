<!--
  新增/编辑指导记录弹窗组件
  @author 系统架构师
  @version 1.0
  @since 2026-03-16
-->
<template>
  <a-modal
    v-model:open="visible"
    :title="'新增指导记录'"
    :width="640"
    :confirm-loading="loading"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 5 }"
      :wrapper-col="{ span: 18 }"
    >
      <a-form-item label="学生" name="studentId">
        <a-select
          v-model:value="formData.studentId"
          placeholder="请选择学生"
          show-search
          :filter-option="filterOption"
          @change="handleStudentChange"
        >
          <a-select-option
            v-for="student in studentList"
            :key="student.studentId"
            :value="student.studentId"
          >
            {{ student.studentName }} ({{ student.studentNo }})
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="课题" name="topicId">
        <a-input v-model:value="selectedTopicTitle" disabled placeholder="选择学生后自动填充" />
        <input type="hidden" v-model="formData.topicId" />
      </a-form-item>

      <a-form-item label="指导类型" name="guidanceType">
        <a-radio-group v-model:value="formData.guidanceType" :disabled="typeDisabled">
          <a-radio :value="1">项目指导</a-radio>
          <a-radio :value="2">论文指导</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="指导日期" name="guidanceDate">
        <a-date-picker
          v-model:value="guidanceDateValue"
          style="width: 100%"
          placeholder="请选择指导日期"
          format="YYYY-MM-DD"
          :disabled-date="disabledDate"
          @change="handleDateChange"
        />
      </a-form-item>

      <a-form-item label="指导方式" name="guidanceMethod">
        <a-select v-model:value="formData.guidanceMethod" placeholder="请选择指导方式">
          <a-select-option value="线下">线下</a-select-option>
          <a-select-option value="线上">线上</a-select-option>
          <a-select-option value="邮件">邮件</a-select-option>
          <a-select-option value="电话">电话</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="指导时长" name="durationHours">
        <a-input-number
          v-model:value="formData.durationHours"
          :min="0.5"
          :max="8"
          :step="0.5"
          placeholder="小时"
          style="width: 120px"
        />
        <span style="margin-left: 8px">小时</span>
      </a-form-item>

      <a-form-item label="指导内容" name="guidanceContent">
        <a-textarea
          v-model:value="formData.guidanceContent"
          :rows="5"
          placeholder="请填写本次指导的具体内容（10-2000字）"
          :maxlength="2000"
          show-count
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import dayjs, { Dayjs } from 'dayjs'
import { guidanceApi } from '@/api/guidance'
import type { CreateGuidanceDTO, GuidanceStudentVO } from '@/types/guidance'
import { useUserStore } from '@/stores/user'

// Props
interface Props {
  modelValue: boolean
  studentList: GuidanceStudentVO[]
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

// 用户Store
const userStore = useUserStore()

// 弹窗可见性
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)

// 选中的课题名称
const selectedTopicTitle = ref('')

// 指导日期（dayjs格式）
const guidanceDateValue = ref<Dayjs | null>(null)

// 指导类型是否禁用（根据角色自动设置）
const typeDisabled = computed(() => {
  // 企业教师只能选项目指导，高校教师只能选论文指导
  return userStore.hasAnyRole(['ENTERPRISE_TEACHER']) || userStore.hasAnyRole(['UNIVERSITY_TEACHER'])
})

// 表单数据
const formData = reactive<CreateGuidanceDTO>({
  studentId: '',
  topicId: '',
  guidanceType: 1,
  guidanceDate: '',
  guidanceContent: '',
  guidanceMethod: undefined,
  durationHours: undefined
})

// 表单校验规则
const rules: Record<string, Rule[]> = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  guidanceType: [{ required: true, message: '请选择指导类型', trigger: 'change' }],
  guidanceDate: [{ required: true, message: '请选择指导日期', trigger: 'change' }],
  guidanceContent: [
    { required: true, message: '请填写指导内容', trigger: 'blur' },
    { min: 10, max: 2000, message: '指导内容长度应在10-2000字之间', trigger: 'blur' }
  ]
}

// 监听弹窗打开，初始化默认指导类型
watch(visible, (val) => {
  if (val) {
    // 根据角色设置默认指导类型
    if (userStore.hasAnyRole(['ENTERPRISE_TEACHER'])) {
      formData.guidanceType = 1  // 项目指导
    } else if (userStore.hasAnyRole(['UNIVERSITY_TEACHER'])) {
      formData.guidanceType = 2  // 论文指导
    }
  }
})

// 学生选择变化
const handleStudentChange = (studentId: string) => {
  const student = props.studentList.find(s => s.studentId === studentId)
  if (student) {
    formData.topicId = student.topicId
    selectedTopicTitle.value = student.topicTitle
  } else {
    formData.topicId = ''
    selectedTopicTitle.value = ''
  }
}

// 下拉搜索过滤
const filterOption = (input: string, option: any) => {
  return option.children[0].children.toLowerCase().includes(input.toLowerCase())
}

// 禁用未来日期
const disabledDate = (current: Dayjs) => {
  return current && current > dayjs().endOf('day')
}

// 日期选择变化，同步更新 formData.guidanceDate 供表单校验使用
const handleDateChange = (date: Dayjs | null) => {
  formData.guidanceDate = date ? date.format('YYYY-MM-DD') : ''
  formRef.value?.validateFields(['guidanceDate'])
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()

    // 转换日期格式
    if (guidanceDateValue.value) {
      formData.guidanceDate = guidanceDateValue.value.format('YYYY-MM-DD')
    }

    loading.value = true
    await guidanceApi.createGuidanceRecord(formData)
    message.success('指导记录创建成功')
    emit('success')
    handleCancel()
  } catch (error: any) {
    if (error.errorFields) {
      // 表单校验失败
      return
    }
    message.error(error.message || '创建失败')
  } finally {
    loading.value = false
  }
}

// 取消/关闭
const handleCancel = () => {
  visible.value = false
  formRef.value?.resetFields()
  selectedTopicTitle.value = ''
  guidanceDateValue.value = null
}
</script>

<style scoped>
/* 组件样式 */
</style>
