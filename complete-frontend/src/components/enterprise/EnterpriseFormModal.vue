<template>
  <a-modal
    :open="open"
    :title="isEdit ? '编辑企业' : '新建企业'"
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
      class="enterprise-form"
    >
      <!-- 企业名称 -->
      <a-form-item label="企业名称" name="enterpriseName">
        <a-input
          v-model:value="formState.enterpriseName"
          placeholder="请输入企业名称"
          :maxlength="100"
        />
      </a-form-item>

      <!-- 企业编码 -->
      <a-form-item label="企业编码" name="enterpriseCode">
        <a-input
          v-model:value="formState.enterpriseCode"
          placeholder="请输入企业编码"
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
          placeholder="请输入联系电话"
          :maxlength="11"
        />
      </a-form-item>

      <!-- 联系邮箱 -->
      <a-form-item label="联系邮箱" name="contactEmail">
        <a-input
          v-model:value="formState.contactEmail"
          placeholder="请输入联系邮箱"
          :maxlength="100"
        />
      </a-form-item>

      <!-- 企业地址 -->
      <a-form-item label="企业地址" name="address">
        <a-textarea
          v-model:value="formState.address"
          placeholder="请输入企业地址"
          :maxlength="200"
          :rows="2"
        />
      </a-form-item>

      <!-- 企业简介 -->
      <a-form-item label="企业简介" name="description">
        <a-textarea
          v-model:value="formState.description"
          placeholder="请输入企业简介"
          :maxlength="500"
          :rows="4"
          show-count
        />
      </a-form-item>

      <!-- 状态（仅编辑时显示） -->
      <a-form-item v-if="isEdit" label="状态" name="enterpriseStatus">
        <a-radio-group v-model:value="formState.enterpriseStatus">
          <a-radio :value="1">正常</a-radio>
          <a-radio :value="0">禁用</a-radio>
        </a-radio-group>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 企业新建/编辑弹窗组件
 * @description 支持创建和编辑企业信息，包含表单验证
 * @author YuWan
 * @date 2026-02-22
 */
import { ref, reactive, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { enterpriseApi } from '@/api/enterprise'
import type { EnterpriseVO, CreateEnterpriseDTO, UpdateEnterpriseDTO } from '@/types/enterprise'

defineOptions({
  name: 'EnterpriseFormModal'
})

// Props 定义
interface Props {
  /** 弹窗是否显示 */
  open: boolean
  /** 编辑时传入的企业数据 */
  enterpriseData?: EnterpriseVO | null
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  enterpriseData: null
})

// Emits 定义
const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
  cancel: []
}>()

// 是否为编辑模式
const isEdit = computed(() => !!props.enterpriseData)

// 表单引用
const formRef = ref<FormInstance>()

// 提交加载状态
const submitLoading = ref(false)

// 表单数据
const formState = reactive<CreateEnterpriseDTO & { enterpriseStatus?: number }>({
  enterpriseName: '',
  enterpriseCode: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  description: '',
  enterpriseStatus: 1
})

// 邮箱格式校验
const validateEmail = async (_rule: Rule, value: string) => {
  if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    return Promise.reject('请输入正确的邮箱地址')
  }
  return Promise.resolve()
}

// 手机号格式校验
const validatePhone = async (_rule: Rule, value: string) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    return Promise.reject('请输入正确的手机号')
  }
  return Promise.resolve()
}

// 表单校验规则
const formRules: Record<string, Rule[]> = {
  enterpriseName: [
    { required: true, message: '请输入企业名称', trigger: 'blur' },
    { max: 100, message: '企业名称长度不能超过100个字符', trigger: 'blur' }
  ],
  enterpriseCode: [
    { max: 50, message: '企业编码长度不能超过50个字符', trigger: 'blur' }
  ],
  contactPerson: [
    { max: 50, message: '联系人长度不能超过50个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  contactEmail: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  address: [
    { max: 200, message: '企业地址长度不能超过200个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '企业简介长度不能超过500个字符', trigger: 'blur' }
  ]
}

/**
 * 重置表单数据
 */
const resetForm = () => {
  formState.enterpriseName = ''
  formState.enterpriseCode = ''
  formState.contactPerson = ''
  formState.contactPhone = ''
  formState.contactEmail = ''
  formState.address = ''
  formState.description = ''
  formState.enterpriseStatus = 1
  formRef.value?.clearValidate()
}

/**
 * 填充编辑数据
 */
const fillFormData = (enterprise: EnterpriseVO) => {
  formState.enterpriseName = enterprise.enterpriseName
  formState.enterpriseCode = enterprise.enterpriseCode || ''
  formState.contactPerson = enterprise.contactPerson || ''
  formState.contactPhone = enterprise.contactPhone || ''
  formState.contactEmail = enterprise.contactEmail || ''
  formState.address = enterprise.address || ''
  formState.description = enterprise.description || ''
  formState.enterpriseStatus = enterprise.enterpriseStatus
}

// 监听弹窗状态变化
watch(
  () => props.open,
  (newVal) => {
    if (newVal) {
      if (props.enterpriseData) {
        fillFormData(props.enterpriseData)
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
    if (isEdit.value && props.enterpriseData) {
      // 编辑模式
      const updateData: UpdateEnterpriseDTO = {
        enterpriseName: formState.enterpriseName,
        enterpriseCode: formState.enterpriseCode,
        contactPerson: formState.contactPerson,
        contactPhone: formState.contactPhone,
        contactEmail: formState.contactEmail,
        address: formState.address,
        description: formState.description,
        enterpriseStatus: formState.enterpriseStatus
      }
      await enterpriseApi.updateEnterprise(props.enterpriseData.enterpriseId, updateData)
      message.success('企业信息更新成功')
    } else {
      // 新建模式
      const createData: CreateEnterpriseDTO = {
        enterpriseName: formState.enterpriseName,
        enterpriseCode: formState.enterpriseCode,
        contactPerson: formState.contactPerson,
        contactPhone: formState.contactPhone,
        contactEmail: formState.contactEmail,
        address: formState.address,
        description: formState.description
      }
      await enterpriseApi.createEnterprise(createData)
      message.success('企业创建成功')
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
.enterprise-form {
  padding-top: 16px;
}
</style>
