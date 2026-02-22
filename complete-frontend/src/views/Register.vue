<template>
  <div class="register-page">
    <!-- 背景层 -->
    <div class="register-bg" />
    <div class="register-bg-overlay" />

    <!-- 内容区域 -->
    <div class="register-content">
      <div class="register-card">
        <!-- 头部 -->
        <div class="card-header">
          <img src="/pictureResource/logo.png" alt="Logo" class="card-logo" />
          <h2 class="card-title">注册账号</h2>
          <p class="card-desc">请填写以下信息完成注册</p>
        </div>

        <a-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          layout="vertical"
          class="register-form"
          @finish="handleRegister"
        >
          <!-- 第一行：角色选择 + 真实姓名 -->
          <div class="form-row">
            <a-form-item name="roleCode" label="角色身份" class="form-col">
              <a-select
                v-model:value="registerForm.roleCode"
                size="large"
                placeholder="请选择您的角色身份"
                @change="handleRoleChange"
              >
                <a-select-option
                  v-for="role in roleOptions"
                  :key="role.value"
                  :value="role.value"
                >
                  {{ role.label }}
                </a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item name="realName" label="真实姓名" class="form-col">
              <a-input
                v-model:value="registerForm.realName"
                size="large"
                placeholder="请输入真实姓名"
                allow-clear
              >
                <template #prefix>
                  <IdcardOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>
          </div>

          <!-- 第二行：学号/工号（根据角色显示） -->
          <div class="form-row" v-if="registerForm.roleCode">
            <a-form-item
              v-if="registerForm.roleCode === 'STUDENT'"
              name="studentNo"
              label="学号"
              class="form-col"
            >
              <a-input
                v-model:value="registerForm.studentNo"
                size="large"
                placeholder="请输入学号"
                allow-clear
              >
                <template #prefix>
                  <UserOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item
              v-else
              name="employeeNo"
              label="工号"
              class="form-col"
            >
              <a-input
                v-model:value="registerForm.employeeNo"
                size="large"
                placeholder="请输入工号"
                allow-clear
              >
                <template #prefix>
                  <UserOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>
          </div>

          <!-- 第三行：密码 + 确认密码 -->
          <div class="form-row">
            <a-form-item name="password" label="密码" class="form-col">
              <a-input-password
                v-model:value="registerForm.password"
                size="large"
                placeholder="请输入密码（6-20位）"
              >
                <template #prefix>
                  <LockOutlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>

            <a-form-item name="confirmPassword" label="确认密码" class="form-col">
              <a-input-password
                v-model:value="registerForm.confirmPassword"
                size="large"
                placeholder="请再次输入密码"
              >
                <template #prefix>
                  <LockOutlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>
          </div>

          <!-- 第四行：邮箱 + 手机号 -->
          <div class="form-row">
            <a-form-item name="userEmail" label="邮箱" class="form-col">
              <a-input
                v-model:value="registerForm.userEmail"
                size="large"
                placeholder="请输入邮箱地址"
                allow-clear
              >
                <template #prefix>
                  <MailOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item name="userPhone" label="手机号" class="form-col">
              <a-input
                v-model:value="registerForm.userPhone"
                size="large"
                placeholder="请输入手机号"
                :maxlength="11"
                allow-clear
              >
                <template #prefix>
                  <PhoneOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>
          </div>

          <!-- 验证码 -->
          <a-form-item name="captcha" label="验证码">
            <div class="captcha-row">
              <a-input
                v-model:value="registerForm.captcha"
                size="large"
                placeholder="请输入验证码"
                :maxlength="4"
                allow-clear
                class="captcha-input"
              >
                <template #prefix>
                  <SafetyCertificateOutlined class="input-icon" />
                </template>
              </a-input>
              <CaptchaCanvas
                ref="captchaRef"
                :width="120"
                :height="40"
                @change="handleCaptchaChange"
              />
            </div>
          </a-form-item>

          <!-- 注册按钮 -->
          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              :loading="loading"
              block
              class="register-btn"
            >
              <template #icon>
                <UserAddOutlined />
              </template>
              注 册
            </a-button>
          </a-form-item>
        </a-form>

        <!-- 底部引导 -->
        <div class="card-footer">
          <span class="footer-text">已有账号？</span>
          <router-link to="/login" class="login-link">返回登录</router-link>
        </div>
      </div>
    </div>

    <!-- 底部版权 -->
    <div class="register-footer">
      <p>Copyright &copy; {{ currentYear }} 毕业设计全过程管理系统 All Rights Reserved</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import {
  UserOutlined,
  LockOutlined,
  IdcardOutlined,
  MailOutlined,
  PhoneOutlined,
  SafetyCertificateOutlined,
  UserAddOutlined
} from '@ant-design/icons-vue'
import { authApi } from '@/api/auth'
import type { RegisterDTO } from '@/api/auth'
import CaptchaCanvas from '@/components/CaptchaCanvas.vue'

defineOptions({
  name: 'RegisterPage'
})

const router = useRouter()

// 表单引用
const registerFormRef = ref<FormInstance>()
const captchaRef = ref<InstanceType<typeof CaptchaCanvas>>()

// 状态
const currentCaptcha = ref('')
const loading = ref(false)
const currentYear = computed(() => new Date().getFullYear())

// 角色选项
const roleOptions = [
  { label: '学生', value: 'STUDENT' },
  { label: '高校指导教师', value: 'UNIVERSITY_TEACHER' },
  { label: '企业指导教师', value: 'ENTERPRISE_TEACHER' }
]

// 表单数据
const registerForm = reactive({
  studentNo: '',
  employeeNo: '',
  password: '',
  confirmPassword: '',
  realName: '',
  userEmail: '',
  userPhone: '',
  roleCode: undefined as string | undefined,
  captcha: ''
})

// 自定义校验：确认密码
const validateConfirmPassword = (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject('请再次输入密码')
  }
  if (value !== registerForm.password) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

// 自定义校验：验证码
const validateCaptcha = (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject('请输入验证码')
  }
  if (value.length !== 4) {
    return Promise.reject('验证码为4位字符')
  }
  if (value.toLowerCase() !== currentCaptcha.value.toLowerCase()) {
    return Promise.reject('验证码不正确')
  }
  return Promise.resolve()
}

// 表单验证规则
const registerRules: Record<string, Rule[]> = {
  roleCode: [
    { required: true, message: '请选择角色身份', trigger: 'change' }
  ],
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  employeeNo: [
    { required: true, message: '请输入工号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  userEmail: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  userPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  captcha: [
    { required: true, validator: validateCaptcha, trigger: 'blur' }
  ]
}

/** 角色切换时清空学号/工号 */
const handleRoleChange = () => {
  registerForm.studentNo = ''
  registerForm.employeeNo = ''
}

/** 验证码变更 */
const handleCaptchaChange = (code: string) => {
  currentCaptcha.value = code
}

/** 注册处理 */
const handleRegister = async () => {
  try {
    await registerFormRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const registerData: RegisterDTO = {
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      realName: registerForm.realName,
      userEmail: registerForm.userEmail,
      userPhone: registerForm.userPhone,
      roleCode: registerForm.roleCode!
    }
    // 根据角色设置学号或工号
    if (registerForm.roleCode === 'STUDENT') {
      registerData.studentNo = registerForm.studentNo
    } else {
      registerData.employeeNo = registerForm.employeeNo
    }
    await authApi.register(registerData)
    message.success('注册成功！请使用学号/工号登录')
    router.push('/login')
  } catch (error: any) {
    captchaRef.value?.refreshCaptcha()
    registerForm.captcha = ''
    message.error(error.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.register-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

/* 背景图 */
.register-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('/pictureResource/background .png') no-repeat center center;
  background-size: cover;
  z-index: 0;
}

.register-bg-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    135deg,
    rgba(22, 52, 102, 0.88) 0%,
    rgba(45, 80, 145, 0.78) 50%,
    rgba(22, 52, 102, 0.88) 100%
  );
  z-index: 1;
}

/* 内容区域 */
.register-content {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 640px;
  padding: 20px;
}

/* 注册卡片 */
.register-card {
  padding: 40px 44px 32px;
  background: rgba(255, 255, 255, 0.97);
  border-radius: 16px;
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.2),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
}

.card-header {
  text-align: center;
  margin-bottom: 28px;
}

.card-logo {
  height: 48px;
  width: auto;
  margin-bottom: 12px;
}

.card-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 6px;
}

.card-desc {
  font-size: 14px;
  color: #8c8c8c;
}

/* 表单行 */
.form-row {
  display: flex;
  gap: 16px;
}

.form-col {
  flex: 1;
  min-width: 0;
}

/* 表单样式 */
.register-form {
  :deep(.ant-input-affix-wrapper) {
    border-radius: 8px;
    border-color: #e0e0e0;
    padding: 6px 12px;
    transition: all 0.3s ease;

    &:hover,
    &:focus {
      border-color: #1a56db;
      box-shadow: 0 0 0 2px rgba(26, 86, 219, 0.1);
    }
  }

  :deep(.ant-input-affix-wrapper-focused) {
    border-color: #1a56db;
    box-shadow: 0 0 0 2px rgba(26, 86, 219, 0.1);
  }

  :deep(.ant-select-selector) {
    border-radius: 8px !important;
    border-color: #e0e0e0 !important;
    height: 40px !important;
    padding: 4px 12px !important;

    &:hover {
      border-color: #1a56db !important;
    }
  }

  :deep(.ant-select-focused .ant-select-selector) {
    border-color: #1a56db !important;
    box-shadow: 0 0 0 2px rgba(26, 86, 219, 0.1) !important;
  }

  :deep(.ant-form-item-label > label) {
    font-size: 13px;
    font-weight: 500;
    color: #4a4a5a;
  }

  :deep(.ant-input) {
    font-size: 14px;
  }

  .input-icon {
    color: #bfbfbf;
    font-size: 15px;
  }
}

/* 验证码行 */
.captcha-row {
  display: flex;
  align-items: center;
  gap: 12px;

  .captcha-input {
    flex: 1;
  }
}

/* 注册按钮 */
.register-btn {
  height: 46px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 4px;
  border-radius: 8px;
  background: linear-gradient(135deg, #1a56db 0%, #2563eb 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(26, 86, 219, 0.35);
  transition: all 0.3s ease;

  &:hover {
    background: linear-gradient(135deg, #1e40af 0%, #1a56db 100%);
    box-shadow: 0 6px 20px rgba(26, 86, 219, 0.45);
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(0);
  }
}

/* 底部登录引导 */
.card-footer {
  text-align: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.footer-text {
  font-size: 14px;
  color: #8c8c8c;
}

.login-link {
  font-size: 14px;
  color: #1a56db;
  font-weight: 500;
  margin-left: 6px;
  transition: color 0.3s;

  &:hover {
    color: #3b82f6;
  }
}

/* 底部版权 */
.register-footer {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  z-index: 2;
  text-align: center;

  p {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.55);
    margin: 0;
  }
}

/* 响应式适配 */
@media (max-width: 680px) {
  .form-row {
    flex-direction: column;
    gap: 0;
  }

  .register-card {
    padding: 32px 24px 28px;
  }

  .register-content {
    max-width: 480px;
  }
}
</style>
