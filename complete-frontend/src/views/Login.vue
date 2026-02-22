<template>
  <div class="login-page">
    <!-- 背景层 -->
    <div class="login-bg" />

    <!-- 左上角Logo -->
    <div class="top-logo">
      <img src="/pictureResource/schoolLogo.png" alt="天津市大学软件学院" class="school-logo" />
    </div>

    <!-- 内容区域 -->
    <div class="login-content">
      <!-- 左侧品牌区 -->
      <div class="login-brand">
        <div class="brand-container">
          <div class="brand-text">
            <h1 class="brand-title">毕业设计全过程管理系统</h1>
            <p class="brand-subtitle">Graduation Design Full Process Management System</p>
          </div>
          <div class="brand-features">
            <div class="feature-item">
              <div class="feature-dot"></div>
              <span>课题申报与审查</span>
            </div>
            <div class="feature-item">
              <div class="feature-dot"></div>
              <span>双选与指导管理</span>
            </div>
            <div class="feature-item">
              <div class="feature-dot"></div>
              <span>质量监控与答辩</span>
            </div>
            <div class="feature-item">
              <div class="feature-dot"></div>
              <span>全过程数字化管控</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-card">
        <div class="card-header">
          <img src="/pictureResource/logo.png" alt="Logo" class="card-logo" />
          <h2 class="card-title">欢迎登录</h2>
          <p class="card-desc">请使用学号或工号登录</p>
        </div>

        <a-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @finish="handleLogin"
        >
          <!-- 学号/工号 -->
          <a-form-item name="identifier">
            <a-input
              v-model:value="loginForm.identifier"
              size="large"
              placeholder="请输入学号/工号"
              allow-clear
              @pressEnter="handleLogin"
            >
              <template #prefix>
                <UserOutlined class="input-icon" />
              </template>
            </a-input>
          </a-form-item>

          <!-- 密码 -->
          <a-form-item name="password">
            <a-input-password
              v-model:value="loginForm.password"
              size="large"
              placeholder="请输入密码"
              @pressEnter="handleLogin"
            >
              <template #prefix>
                <LockOutlined class="input-icon" />
              </template>
            </a-input-password>
          </a-form-item>

          <!-- 验证码 -->
          <a-form-item name="captcha">
            <div class="captcha-row">
              <a-input
                v-model:value="loginForm.captcha"
                size="large"
                placeholder="请输入验证码"
                :maxlength="4"
                allow-clear
                class="captcha-input"
                @pressEnter="handleLogin"
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

          <!-- 记住密码 -->
          <a-form-item>
            <div class="form-actions">
              <a-checkbox v-model:checked="rememberMe">记住密码</a-checkbox>
              <a class="forget-link" @click="handleForgotPassword">忘记密码？</a>
            </div>
          </a-form-item>

          <!-- 登录按钮 -->
          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              :loading="loading"
              block
              class="login-btn"
            >
              <template #icon>
                <LoginOutlined />
              </template>
              登 录
            </a-button>
          </a-form-item>
        </a-form>

        <!-- 注册引导 -->
        <div class="card-footer">
          <span class="footer-text">还没有账号？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>
      </div>
    </div>

    <!-- 底部版权 -->
    <div class="login-footer">
      <p>Copyright &copy; {{ currentYear }} 毕业设计全过程管理系统 All Rights Reserved</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import {
  UserOutlined,
  LockOutlined,
  SafetyCertificateOutlined,
  LoginOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import type { LoginDTO } from '@/api/auth'
import CaptchaCanvas from '@/components/CaptchaCanvas.vue'

defineOptions({
  name: 'LoginPage'
})

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref<FormInstance>()
const captchaRef = ref<InstanceType<typeof CaptchaCanvas>>()

// 当前验证码
const currentCaptcha = ref('')
const rememberMe = ref(false)
const loading = ref(false)
const currentYear = computed(() => new Date().getFullYear())

// 表单数据
const loginForm = reactive({
  identifier: '',
  password: '',
  captcha: ''
})

// 自定义验证码校验
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
const loginRules: Record<string, Rule[]> = {
  identifier: [
    { required: true, message: '请输入学号/工号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, validator: validateCaptcha, trigger: 'blur' }
  ]
}

/** 验证码变更 */
const handleCaptchaChange = (code: string) => {
  currentCaptcha.value = code
}

/** 登录处理 */
const handleLogin = async () => {
  try {
    // 表单校验
    await loginFormRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const loginData: LoginDTO = {
      identifier: loginForm.identifier,
      password: loginForm.password
    }
    await userStore.login(loginData)
    message.success('登录成功，欢迎回来！')

    // 获取重定向地址
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (error: any) {
    // 登录失败，刷新验证码
    captchaRef.value?.refreshCaptcha()
    loginForm.captcha = ''
    message.error(error.message || '登录失败，请检查学号/工号和密码')
  } finally {
    loading.value = false
  }
}

/** 忘记密码 */
const handleForgotPassword = () => {
  message.info('密码找回功能开发中，请联系管理员重置密码')
}
</script>

<style scoped lang="scss">
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

/* 背景图 */
.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('/pictureResource/background .png') no-repeat center center;
  background-size: cover;
  z-index: 0;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(
      to right,
      rgba(0, 0, 0, 0.35) 0%,
      rgba(0, 0, 0, 0.15) 50%,
      rgba(0, 0, 0, 0.25) 100%
    );
  }
}

/* 左上角Logo */
.top-logo {
  position: fixed;
  top: 2px;
  left: 2px;
  z-index: 10;
  animation: fadeInDown 0.8s ease;
}

.school-logo {
  width: 500px;
  height: auto;
  filter: drop-shadow(0 6px 16px rgba(0, 0, 0, 0.2));
  transition: all 0.3s ease;

  &:hover {
    transform: scale(1.03);
    filter: drop-shadow(0 8px 20px rgba(0, 0, 0, 0.25));
  }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 内容区域 */
.login-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1400px;
  width: 100%;
  height: 100vh;
  padding: 0 80px;
  gap: 120px;
}

/* 左侧品牌区 */
.login-brand {
  flex: 1;
  max-width: 560px;
  animation: fadeInLeft 1s ease;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-40px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.brand-container {
  width: 100%;
}

.brand-text {
  margin-bottom: 60px;
}

.brand-title {
  font-size: 52px;
  font-weight: 300;
  color: #fff;
  margin-bottom: 20px;
  letter-spacing: 6px;
  line-height: 1.3;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.2);
}

.brand-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 2px;
  font-weight: 300;
  text-shadow: 0 1px 10px rgba(0, 0, 0, 0.15);
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 300;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  padding-left: 8px;

  &:hover {
    color: #fff;
    padding-left: 20px;

    .feature-dot {
      background: #fff;
      box-shadow: 0 0 12px rgba(255, 255, 255, 0.6);
    }
  }
}

.feature-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
  flex-shrink: 0;
}

/* 右侧登录卡片 */
.login-card {
  width: 460px;
  flex-shrink: 0;
  padding: 56px 48px 48px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 24px;
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.15),
    0 0 1px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(40px);
  animation: fadeInRight 1s ease;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(40px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.card-header {
  text-align: center;
  margin-bottom: 36px;
}

.card-logo {
  height: 52px;
  width: auto;
  margin-bottom: 16px;
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

/* 表单样式 */
.login-form {
  :deep(.ant-input-affix-wrapper) {
    border-radius: 8px;
    border-color: #e0e0e0;
    padding: 8px 12px;
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

  :deep(.ant-input) {
    font-size: 15px;
  }

  .input-icon {
    color: #bfbfbf;
    font-size: 16px;
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

/* 记住密码 & 忘记密码 */
.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.forget-link {
  color: #1a56db;
  font-size: 13px;
  cursor: pointer;
  transition: color 0.3s;

  &:hover {
    color: #3b82f6;
  }
}

/* 登录按钮 */
.login-btn {
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

/* 底部注册引导 */
.card-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.footer-text {
  font-size: 14px;
  color: #8c8c8c;
}

.register-link {
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
.login-footer {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  z-index: 2;
  text-align: center;

  p {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.65);
    margin: 0;
    text-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
  }
}

/* 响应式适配 */
@media (max-width: 1200px) {
  .login-content {
    flex-direction: column;
    justify-content: center;
    gap: 60px;
    padding: 140px 40px 60px;
    height: auto;
    min-height: 100vh;
  }

  .login-brand {
    max-width: 100%;
    text-align: center;
  }

  .brand-text {
    margin-bottom: 40px;
  }

  .brand-title {
    font-size: 40px;
    letter-spacing: 4px;
  }

  .brand-subtitle {
    font-size: 14px;
  }

  .brand-features {
    max-width: 400px;
    margin: 0 auto;
  }

  .feature-item {
    justify-content: center;
    font-size: 16px;
  }

  .login-card {
    width: 100%;
    max-width: 460px;
  }

  .top-logo {
    top: 32px;
    left: 32px;
  }

  .school-logo {
    width: 280px;
  }
}

@media (max-width: 640px) {
  .login-content {
    padding: 120px 24px 40px;
    gap: 40px;
  }

  .login-card {
    padding: 40px 32px 36px;
  }

  .card-title {
    font-size: 22px;
  }

  .brand-title {
    font-size: 32px;
    letter-spacing: 2px;
  }

  .brand-subtitle {
    font-size: 13px;
    letter-spacing: 1px;
  }

  .brand-text {
    margin-bottom: 32px;
  }

  .feature-item {
    font-size: 15px;
  }

  .top-logo {
    top: 24px;
    left: 24px;
  }

  .school-logo {
    width: 220px;
  }
}

@media (max-width: 480px) {
  .brand-title {
    font-size: 26px;
  }

  .brand-features {
    gap: 16px;
  }

  .feature-item {
    font-size: 14px;
  }

  .login-card {
    padding: 36px 24px 32px;
  }
}
</style>
