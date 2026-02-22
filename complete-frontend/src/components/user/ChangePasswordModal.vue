<template>
  <a-modal
    :open="open"
    title="修改密码"
    :width="480"
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
      class="password-form"
    >
      <a-form-item label="原密码" name="oldPassword">
        <a-input-password
          v-model:value="formState.oldPassword"
          placeholder="请输入原密码"
          :maxlength="32"
        >
          <template #prefix><LockOutlined /></template>
        </a-input-password>
      </a-form-item>

      <a-form-item label="新密码" name="newPassword">
        <a-input-password
          v-model:value="formState.newPassword"
          placeholder="请输入新密码"
          :maxlength="32"
        >
          <template #prefix><LockOutlined /></template>
        </a-input-password>
      </a-form-item>

      <a-form-item label="确认密码" name="confirmPassword">
        <a-input-password
          v-model:value="formState.confirmPassword"
          placeholder="请再次输入新密码"
          :maxlength="32"
        >
          <template #prefix><LockOutlined /></template>
        </a-input-password>
      </a-form-item>

      <!-- 密码强度提示 -->
      <a-form-item :wrapper-col="{ offset: 6, span: 16 }">
        <a-alert
          type="info"
          show-icon
          class="password-form__tip"
        >
          <template #message>
            <div class="password-form__tip-text">
              密码要求：6-32位，必须包含字母和数字
            </div>
          </template>
        </a-alert>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 修改密码弹窗组件
 * @description 用户修改密码的表单弹窗，包含密码强度校验
 * @author YuWan
 * @date 2026-02-21
 */
import { ref, reactive, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { LockOutlined } from '@ant-design/icons-vue'
import { userApi } from '@/api/user'
import type { ChangePasswordDTO } from '@/types/user'

defineOptions({
  name: 'ChangePasswordModal'
})

// Props 定义
interface Props {
  /** 弹窗是否显示 */
  open: boolean
}

const props = withDefaults(defineProps<Props>(), {
  open: false
})

// Emits 定义
const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

// 表单引用
const formRef = ref<FormInstance>()

// 提交加载状态
const submitLoading = ref(false)

// 表单数据
const formState = reactive<ChangePasswordDTO>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 新密码校验
const validateNewPassword = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject('请输入新密码')
  }
  if (value.length < 6) {
    return Promise.reject('密码长度不能少于6位')
  }
  if (value.length > 32) {
    return Promise.reject('密码长度不能超过32位')
  }
  if (!/^(?=.*[a-zA-Z])(?=.*\d)/.test(value)) {
    return Promise.reject('密码必须包含字母和数字')
  }
  if (value === formState.oldPassword) {
    return Promise.reject('新密码不能与原密码相同')
  }
  // 如果确认密码已填写，触发确认密码的重新校验
  if (formState.confirmPassword) {
    formRef.value?.validateFields(['confirmPassword'])
  }
  return Promise.resolve()
}

// 确认密码校验
const validateConfirmPassword = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject('请再次输入新密码')
  }
  if (value !== formState.newPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

// 表单校验规则
const formRules: Record<string, Rule[]> = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

/**
 * 重置表单
 */
const resetForm = () => {
  formState.oldPassword = ''
  formState.newPassword = ''
  formState.confirmPassword = ''
  formRef.value?.clearValidate()
}

// 监听弹窗状态
watch(
  () => props.open,
  (newVal) => {
    if (newVal) {
      resetForm()
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
    await userApi.changePassword({
      oldPassword: formState.oldPassword,
      newPassword: formState.newPassword,
      confirmPassword: formState.confirmPassword
    })
    message.success('密码修改成功，请重新登录')
    emit('success')
    handleCancel()
  } catch (error) {
    console.error('修改密码失败:', error)
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
}
</script>

<style scoped lang="scss">
.password-form {
  padding-top: 16px;

  &__tip {
    margin-top: -8px;
  }

  &__tip-text {
    font-size: 12px;
    color: #8c8c8c;
  }
}
</style>
