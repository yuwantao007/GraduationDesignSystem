<template>
  <a-modal
    :open="open"
    :title="isEdit ? '编辑学校' : '新建学校'"
    :width="640"
    :confirm-loading="submitLoading"
    :mask-closable="false"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="formRules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
      class="school-form"
    >
      <!-- 学校名称 -->
      <a-form-item label="学校名称" name="schoolName">
        <a-input
          v-model:value="formState.schoolName"
          placeholder="请输入学校名称"
          :maxlength="100"
        />
      </a-form-item>

      <!-- 学校编码 -->
      <a-form-item label="学校编码" name="schoolCode">
        <a-input
          v-model:value="formState.schoolCode"
          placeholder="请输入学校编码"
          :maxlength="50"
        />
      </a-form-item>

      <!-- 联系人 -->
      <a-form-item label="联系人" name="contactPerson">
        <a-input
          v-model:value="formState.contactPerson"
          placeholder="请输入联系人姓名"
          :maxlength="50"
        />
      </a-form-item>

      <!-- 联系电话 -->
      <a-form-item label="联系电话" name="contactPhone">
        <a-input
          v-model:value="formState.contactPhone"
          placeholder="请输入联系电话（手机或座机）"
          :maxlength="20"
        />
      </a-form-item>

      <!-- 学校邮箱 -->
      <a-form-item label="学校邮箱" name="schoolEmail">
        <a-input
          v-model:value="formState.schoolEmail"
          placeholder="请输入学校邮箱"
          :maxlength="100"
        />
      </a-form-item>

      <!-- 详细地址 -->
      <a-form-item label="详细地址" name="address">
        <a-textarea
          v-model:value="formState.address"
          placeholder="请输入详细地址"
          :maxlength="200"
          :rows="2"
        />
      </a-form-item>

      <!-- 学校简介 -->
      <a-form-item label="学校简介" name="description">
        <a-textarea
          v-model:value="formState.description"
          placeholder="请输入学校简介"
          :maxlength="500"
          :rows="4"
          show-count
        />
      </a-form-item>

      <!-- 状态（仅编辑时显示） -->
      <a-form-item v-if="isEdit" label="状态" name="schoolStatus">
        <a-radio-group v-model:value="formState.schoolStatus">
          <a-radio :value="1">正常</a-radio>
          <a-radio :value="0">禁用</a-radio>
        </a-radio-group>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 学校新建/编辑弹窗组件
 * @description 支持创建和编辑学校信息，包含表单验证
 * @author YuWan
 * @date 2026-02-22
 */
import { ref, reactive, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { schoolApi } from '@/api/school'
import type { SchoolVO, CreateSchoolDTO, UpdateSchoolDTO } from '@/types/school'

defineOptions({
  name: 'SchoolFormModal'
})

// Props 定义
interface Props {
  /** 弹窗是否显示 */
  open: boolean
  /** 编辑时传入的学校数据 */
  schoolData?: SchoolVO | null
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  schoolData: null
})

// Emits 定义
const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
  cancel: []
}>()

// 是否为编辑模式
const isEdit = computed(() => !!props.schoolData)

// 表单引用
const formRef = ref<FormInstance>()

// 提交加载状态
const submitLoading = ref(false)

// 表单数据
const formState = reactive<CreateSchoolDTO & { schoolStatus?: number }>({
  schoolName: '',
  schoolCode: '',
  contactPerson: '',
  contactPhone: '',
  schoolEmail: '',
  address: '',
  description: '',
  schoolStatus: 1
})

// 邮箱格式校验
const validateEmail = async (_rule: Rule, value: string) => {
  if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    return Promise.reject('请输入正确的邮箱地址')
  }
  return Promise.resolve()
}

// 电话号码格式校验（支持手机和座机）
const validatePhone = async (_rule: Rule, value: string) => {
  if (value && !/^(1[3-9]\d{9}|0\d{2,3}-?\d{7,8})$/.test(value)) {
    return Promise.reject('请输入正确的电话号码')
  }
  return Promise.resolve()
}

// 表单校验规则
const formRules: Record<string, Rule[]> = {
  schoolName: [
    { required: true, message: '请输入学校名称', trigger: 'blur' },
    { max: 100, message: '学校名称长度不能超过100个字符', trigger: 'blur' }
  ],
  schoolCode: [
    { max: 50, message: '学校编码长度不能超过50个字符', trigger: 'blur' }
  ],
  contactPerson: [
    { max: 50, message: '联系人长度不能超过50个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  schoolEmail: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  address: [
    { max: 200, message: '详细地址长度不能超过200个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '学校简介长度不能超过500个字符', trigger: 'blur' }
  ]
}

/**
 * 重置表单数据
 */
const resetForm = () => {
  formState.schoolName = ''
  formState.schoolCode = ''
  formState.contactPerson = ''
  formState.contactPhone = ''
  formState.schoolEmail = ''
  formState.address = ''
  formState.description = ''
  formState.schoolStatus = 1
  formRef.value?.clearValidate()
}

/**
 * 填充编辑数据
 */
const fillFormData = (school: SchoolVO) => {
  formState.schoolName = school.schoolName
  formState.schoolCode = school.schoolCode || ''
  formState.contactPerson = school.contactPerson || ''
  formState.contactPhone = school.contactPhone || ''
  formState.schoolEmail = school.schoolEmail || ''
  formState.address = school.address || ''
  formState.description = school.description || ''
  formState.schoolStatus = school.schoolStatus
}

// 监听弹窗状态变化
watch(
  () => props.open,
  (newVal) => {
    if (newVal) {
      if (props.schoolData) {
        fillFormData(props.schoolData)
      } else {
        resetForm()
      }
    }
  }
)

/**
 * 提交表单
 */
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    if (isEdit.value && props.schoolData) {
      // 编辑模式
      const updateData: UpdateSchoolDTO = {
        schoolName: formState.schoolName,
        schoolCode: formState.schoolCode,
        contactPerson: formState.contactPerson,
        contactPhone: formState.contactPhone,
        schoolEmail: formState.schoolEmail,
        address: formState.address,
        description: formState.description,
        schoolStatus: formState.schoolStatus
      }
      await schoolApi.updateSchool(props.schoolData.schoolId, updateData)
      message.success('学校信息更新成功')
    } else {
      // 新建模式
      const createData: CreateSchoolDTO = {
        schoolName: formState.schoolName,
        schoolCode: formState.schoolCode,
        contactPerson: formState.contactPerson,
        contactPhone: formState.contactPhone,
        schoolEmail: formState.schoolEmail,
        address: formState.address,
        description: formState.description
      }
      await schoolApi.createSchool(createData)
      message.success('学校创建成功')
    }
    emit('success')
    handleCancel()
  } catch (error) {
    // 错误已在拦截器中处理
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

/**
 * 取消操作
 */
const handleCancel = () => {
  resetForm()
  emit('update:open', false)
  emit('cancel')
}
</script>

<style scoped lang="scss">
.school-form {
  padding-top: 16px;
}
</style>
