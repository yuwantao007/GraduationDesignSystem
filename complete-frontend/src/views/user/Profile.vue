<template>
  <div class="profile">
    <a-row :gutter="24">
      <!-- 左侧：个人信息卡片 -->
      <a-col :xs="24" :sm="24" :md="8" :lg="7" :xl="6">
        <a-card class="profile__info-card">
          <div class="profile__avatar-section">
            <a-upload
              :show-upload-list="false"
              :before-upload="beforeAvatarUpload"
              :custom-request="handleAvatarUpload"
              accept="image/*"
            >
              <a-tooltip title="点击更换头像">
                <a-avatar
                  :size="96"
                  :src="userInfo?.avatar"
                  :style="{ backgroundColor: '#1890ff', cursor: 'pointer', fontSize: '36px' }"
                >
                  {{ userInfo?.realName?.charAt(0) || 'U' }}
                </a-avatar>
              </a-tooltip>
            </a-upload>
            <h2 class="profile__name">{{ userInfo?.realName || '未设置' }}</h2>
            <p class="profile__account">@{{ userInfo?.username }}</p>
            <div class="profile__role-tags">
              <a-tag
                v-for="role in userInfo?.roles"
                :key="role.roleId"
                :color="getRoleColor(role.roleCode)"
              >
                {{ role.roleName }}
              </a-tag>
            </div>
          </div>

          <a-divider />

          <div class="profile__quick-info">
            <div class="profile__quick-item">
              <MailOutlined />
              <span>{{ userInfo?.userEmail || '未设置' }}</span>
            </div>
            <div class="profile__quick-item">
              <PhoneOutlined />
              <span>{{ userInfo?.userPhone || '未设置' }}</span>
            </div>
            <div class="profile__quick-item">
              <IdcardOutlined />
              <span>{{ userInfo?.userCode || '未设置' }}</span>
            </div>
            <div class="profile__quick-item">
              <BankOutlined />
              <span>{{ userInfo?.department || '未设置' }}</span>
            </div>
          </div>

          <a-divider />

          <div class="profile__actions">
            <a-button type="primary" block @click="showPasswordModal = true">
              <template #icon><LockOutlined /></template>
              修改密码
            </a-button>
          </div>
        </a-card>
      </a-col>

      <!-- 右侧：详细信息编辑 -->
      <a-col :xs="24" :sm="24" :md="16" :lg="17" :xl="18">
        <a-card class="profile__detail-card">
          <a-tabs v-model:activeKey="activeTab">
            <!-- 基本信息 Tab -->
            <a-tab-pane key="basic" tab="基本信息">
              <a-form
                ref="basicFormRef"
                :model="basicForm"
                :rules="basicFormRules"
                :label-col="{ span: 4 }"
                :wrapper-col="{ span: 16 }"
                class="profile__form"
              >
                <a-form-item label="用户名">
                  <a-input :value="userInfo?.username" disabled />
                </a-form-item>

                <a-form-item label="真实姓名" name="realName">
                  <a-input
                    v-model:value="basicForm.realName"
                    placeholder="请输入真实姓名"
                    :maxlength="50"
                  />
                </a-form-item>

                <a-form-item label="性别" name="gender">
                  <a-radio-group v-model:value="basicForm.gender">
                    <a-radio :value="1">男</a-radio>
                    <a-radio :value="0">女</a-radio>
                  </a-radio-group>
                </a-form-item>

                <a-form-item label="邮箱" name="userEmail">
                  <a-input
                    v-model:value="basicForm.userEmail"
                    placeholder="请输入邮箱地址"
                    :maxlength="100"
                  />
                </a-form-item>

                <a-form-item label="手机号" name="userPhone">
                  <a-input
                    v-model:value="basicForm.userPhone"
                    placeholder="请输入手机号"
                    :maxlength="11"
                  />
                </a-form-item>

                <a-form-item label="简介" name="biography">
                  <a-textarea
                    v-model:value="basicForm.biography"
                    placeholder="请输入个人简介"
                    :rows="4"
                    :maxlength="500"
                    show-count
                  />
                </a-form-item>

                <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
                  <a-space>
                    <a-button
                      type="primary"
                      :loading="submitLoading"
                      @click="handleSaveBasicInfo"
                    >
                      保存修改
                    </a-button>
                    <a-button @click="resetBasicForm">重置</a-button>
                  </a-space>
                </a-form-item>
              </a-form>
            </a-tab-pane>

            <!-- 安全设置 Tab -->
            <a-tab-pane key="security" tab="安全设置">
              <a-list item-layout="horizontal" class="profile__security-list">
                <a-list-item>
                  <a-list-item-meta
                    title="账户密码"
                    description="定期修改密码可以提高账户安全性"
                  />
                  <template #actions>
                    <a-button type="link" @click="showPasswordModal = true">修改</a-button>
                  </template>
                </a-list-item>

                <a-list-item>
                  <a-list-item-meta
                    title="绑定邮箱"
                    :description="userInfo?.userEmail ? `已绑定邮箱：${userInfo.userEmail}` : '未绑定邮箱'"
                  />
                  <template #actions>
                    <a-button type="link" @click="activeTab = 'basic'">
                      {{ userInfo?.userEmail ? '修改' : '绑定' }}
                    </a-button>
                  </template>
                </a-list-item>

                <a-list-item>
                  <a-list-item-meta
                    title="绑定手机"
                    :description="userInfo?.userPhone ? `已绑定手机：${maskPhone(userInfo.userPhone)}` : '未绑定手机'"
                  />
                  <template #actions>
                    <a-button type="link" @click="activeTab = 'basic'">
                      {{ userInfo?.userPhone ? '修改' : '绑定' }}
                    </a-button>
                  </template>
                </a-list-item>
              </a-list>
            </a-tab-pane>

            <!-- 登录日志 Tab -->
            <a-tab-pane key="logs" tab="登录日志">
              <a-table
                :columns="logColumns"
                :data-source="loginLogs"
                :loading="logLoading"
                :pagination="logPagination"
                size="small"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'status'">
                    <a-tag :color="record.status === 1 ? 'green' : 'red'">
                      {{ record.status === 1 ? '成功' : '失败' }}
                    </a-tag>
                  </template>
                </template>
              </a-table>
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>
    </a-row>

    <!-- 修改密码弹窗 -->
    <ChangePasswordModal
      v-model:open="showPasswordModal"
      @success="handlePasswordChanged"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * 个人中心页面
 * @description 个人信息展示与编辑、安全设置、登录日志查看
 * @author YuWan
 * @date 2026-02-21
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import type { UploadProps } from 'ant-design-vue'
import {
  MailOutlined,
  PhoneOutlined,
  IdcardOutlined,
  BankOutlined,
  LockOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { userApi } from '@/api/user'
import type { UserVO, UpdateProfileDTO } from '@/types/user'
import { USER_ROLE_COLORS } from '@/types/user'
import ChangePasswordModal from '@/components/user/ChangePasswordModal.vue'

defineOptions({
  name: 'Profile'
})

const router = useRouter()
const userStore = useUserStore()

// 当前激活的 Tab
const activeTab = ref('basic')

// 用户信息
const userInfo = ref<UserVO | null>(null)

// 修改密码弹窗
const showPasswordModal = ref(false)

// 提交加载状态
const submitLoading = ref(false)

// 基本信息表单
const basicFormRef = ref<FormInstance>()
const basicForm = reactive<UpdateProfileDTO>({
  realName: '',
  userEmail: '',
  userPhone: '',
  gender: 1,
  biography: ''
})

// 邮箱校验
const validateEmail = async (_rule: Rule, value: string) => {
  if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    return Promise.reject('请输入正确的邮箱地址')
  }
  return Promise.resolve()
}

// 手机号校验
const validatePhone = async (_rule: Rule, value: string) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    return Promise.reject('请输入正确的手机号')
  }
  return Promise.resolve()
}

// 基本信息表单校验规则
const basicFormRules: Record<string, Rule[]> = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度为2-50个字符', trigger: 'blur' }
  ],
  userEmail: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
  ],
  userPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: validatePhone, trigger: 'blur' }
  ]
}

// ======================== 登录日志 ========================

const logLoading = ref(false)
const loginLogs = ref<any[]>([])
const logPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total: number) => `共 ${total} 条记录`
})

const logColumns = [
  { title: '登录时间', dataIndex: 'loginTime', key: 'loginTime', width: 180 },
  { title: '登录IP', dataIndex: 'loginIp', key: 'loginIp', width: 140 },
  { title: '登录地点', dataIndex: 'loginLocation', key: 'loginLocation', width: 160 },
  { title: '设备信息', dataIndex: 'deviceInfo', key: 'deviceInfo', ellipsis: true },
  { title: '状态', key: 'status', width: 80, align: 'center' as const }
]

// ======================== 工具方法 ========================

/**
 * 获取角色标签颜色
 * @param roleCode - 角色代码
 */
const getRoleColor = (roleCode: string): string => {
  return USER_ROLE_COLORS[roleCode] || 'default'
}

/**
 * 手机号脱敏
 * @param phone - 手机号
 */
const maskPhone = (phone: string): string => {
  if (!phone || phone.length < 7) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// ======================== 数据加载 ========================

/**
 * 加载当前用户信息
 */
const loadUserInfo = async () => {
  try {
    const response = await userApi.getCurrentUserInfo()
    userInfo.value = response.data
    fillBasicForm(response.data)
  } catch (error) {
    message.error('获取用户信息失败')
    console.error('获取用户信息失败:', error)
  }
}

/**
 * 填充基本信息表单
 * @param user - 用户信息
 */
const fillBasicForm = (user: UserVO) => {
  basicForm.realName = user.realName || ''
  basicForm.userEmail = user.userEmail || ''
  basicForm.userPhone = user.userPhone || ''
  basicForm.gender = user.gender || 1
  basicForm.biography = user.biography || ''
}

/**
 * 重置基本信息表单
 */
const resetBasicForm = () => {
  if (userInfo.value) {
    fillBasicForm(userInfo.value)
  }
  basicFormRef.value?.clearValidate()
}

// ======================== 头像上传 ========================

/**
 * 头像上传前校验
 * @param file - 上传文件
 */
const beforeAvatarUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

/**
 * 自定义头像上传
 * @param options - 上传选项
 */
const handleAvatarUpload = async (options: any) => {
  const formData = new FormData()
  formData.append('file', options.file)

  try {
    await userApi.uploadAvatar(formData)
    // 上传成功后拉取一次最新用户信息，确保右上角、首页等全局头像同步刷新
    const latestUser = await userStore.getUserInfoData()
    userInfo.value = latestUser
    message.success('头像上传成功')
  } catch (error) {
    message.error('头像上传失败')
    console.error('头像上传失败:', error)
  }
}

// ======================== 表单操作 ========================

/**
 * 保存基本信息
 */
const handleSaveBasicInfo = async () => {
  try {
    await basicFormRef.value?.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    const response = await userApi.updateProfile({
      realName: basicForm.realName,
      userEmail: basicForm.userEmail,
      userPhone: basicForm.userPhone,
      gender: basicForm.gender,
      biography: basicForm.biography
    })
    userInfo.value = response.data
    // 同步更新全局用户状态
    await userStore.getUserInfoData()
    message.success('个人信息更新成功')
  } catch (error) {
    console.error('更新个人信息失败:', error)
  } finally {
    submitLoading.value = false
  }
}

/**
 * 密码修改成功回调
 */
const handlePasswordChanged = async () => {
  message.info('密码已修改，请重新登录')
  await userStore.logout()
  router.push('/login')
}

// ======================== 生命周期 ========================

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped lang="scss">
.profile {
  &__info-card {
    border-radius: 8px;
    margin-bottom: 24px;
  }

  &__avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16px 0;
  }

  &__name {
    margin: 12px 0 4px;
    font-size: 22px;
    font-weight: 600;
    color: #262626;
  }

  &__account {
    margin: 0 0 12px;
    font-size: 14px;
    color: #8c8c8c;
  }

  &__role-tags {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 4px;
  }

  &__quick-info {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 0 8px;
  }

  &__quick-item {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    color: #595959;

    .anticon {
      color: #8c8c8c;
      font-size: 16px;
    }
  }

  &__actions {
    padding: 0 8px;
  }

  &__detail-card {
    border-radius: 8px;
    min-height: 560px;
  }

  &__form {
    max-width: 600px;
    padding-top: 16px;
  }

  &__security-list {
    :deep(.ant-list-item-meta-title) {
      font-weight: 500;
    }

    :deep(.ant-list-item-meta-description) {
      font-size: 13px;
    }
  }
}
</style>
