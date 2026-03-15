<template>
  <a-modal
    :open="open"
    :title="isEdit ? '编辑用户' : '新建用户'"
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
      class="user-form"
    >
      <!-- 用户名 -->
      <a-form-item label="用户名" name="username">
        <a-input
          v-model:value="formState.username"
          :disabled="isEdit"
          placeholder="请输入用户名"
          :maxlength="50"
        >
          <template #prefix><UserOutlined /></template>
        </a-input>
      </a-form-item>

      <!-- 密码（仅新建时显示） -->
      <a-form-item v-if="!isEdit" label="密码" name="password">
        <a-input-password
          v-model:value="formState.password"
          placeholder="请输入密码"
          :maxlength="32"
        >
          <template #prefix><LockOutlined /></template>
        </a-input-password>
      </a-form-item>

      <!-- 真实姓名 -->
      <a-form-item label="真实姓名" name="realName">
        <a-input
          v-model:value="formState.realName"
          placeholder="请输入真实姓名"
          :maxlength="50"
        />
      </a-form-item>

      <!-- 性别 -->
      <a-form-item label="性别" name="gender">
        <a-radio-group v-model:value="formState.gender">
          <a-radio :value="1">男</a-radio>
          <a-radio :value="0">女</a-radio>
        </a-radio-group>
      </a-form-item>

      <!-- 邮箱 -->
      <a-form-item label="邮箱" name="userEmail">
        <a-input
          v-model:value="formState.userEmail"
          placeholder="请输入邮箱地址"
          :maxlength="100"
        >
          <template #prefix><MailOutlined /></template>
        </a-input>
      </a-form-item>

      <!-- 手机号 -->
      <a-form-item label="手机号" name="userPhone">
        <a-input
          v-model:value="formState.userPhone"
          placeholder="请输入手机号"
          :maxlength="11"
        >
          <template #prefix><PhoneOutlined /></template>
        </a-input>
      </a-form-item>

      <!-- 学号/工号 -->
      <a-form-item label="学号/工号" name="userCode">
        <a-input
          v-model:value="formState.userCode"
          placeholder="请输入学号或工号"
          :maxlength="30"
        />
      </a-form-item>

      <!-- 部门 -->
      <a-form-item label="部门" name="department">
        <a-input
          v-model:value="formState.department"
          placeholder="请输入所在部门"
          :maxlength="100"
        />
      </a-form-item>

      <!-- 专业方向 -->
      <a-form-item label="专业方向" name="major">
        <a-input
          v-model:value="formState.major"
          placeholder="请输入专业方向"
          :maxlength="100"
        />
      </a-form-item>

      <!-- 角色 -->
      <a-form-item label="角色" name="roleIds">
        <a-select
          v-model:value="formState.roleIds"
          mode="multiple"
          placeholder="请选择角色"
          :options="roleOptions"
          :loading="roleLoading"
        />
      </a-form-item>

      <!-- 所属专业（仅学生角色显示） -->
      <template v-if="isStudentRoleSelected">
        <a-form-item label="所属企业">
          <a-select
            v-model:value="selectedEnterpriseId"
            placeholder="请选择企业"
            :options="enterpriseOptions"
            :loading="enterpriseLoading"
            allow-clear
            @change="handleEnterpriseChange"
          />
        </a-form-item>
        <a-form-item label="专业方向">
          <a-select
            v-model:value="selectedDirectionId"
            placeholder="请先选择企业"
            :options="directionOptions"
            :loading="directionLoading"
            :disabled="!selectedEnterpriseId"
            allow-clear
            @change="handleDirectionChange"
          />
        </a-form-item>
        <a-form-item label="所属专业">
          <a-select
            v-model:value="formState.majorId"
            placeholder="请先选择专业方向"
            :options="majorOptions"
            :loading="majorLoading"
            :disabled="!selectedDirectionId"
            allow-clear
          />
        </a-form-item>
      </template>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 用户新建/编辑弹窗组件
 * @description 支持创建和编辑用户信息，包含表单验证
 * @author YuWan
 * @date 2026-02-21
 */
import { ref, reactive, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import {
  UserOutlined,
  LockOutlined,
  MailOutlined,
  PhoneOutlined
} from '@ant-design/icons-vue'
import { userApi } from '@/api/user'
import { roleApi } from '@/api/role'
import { enterpriseApi } from '@/api/enterprise'
import { majorApi } from '@/api/major'
import type { UserVO, CreateUserDTO, UpdateUserDTO, RoleInfo } from '@/types/user'
import type { EnterpriseVO } from '@/types/enterprise'
import type { MajorDirectionVO, MajorVO } from '@/types/major'

defineOptions({
  name: 'UserFormModal'
})

// Props 定义
interface Props {
  /** 弹窗是否显示 */
  open: boolean
  /** 编辑时传入的用户数据 */
  userData?: UserVO | null
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  userData: null
})

// Emits 定义
const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
  cancel: []
}>()

// 是否为编辑模式
const isEdit = computed(() => !!props.userData)

// 表单引用
const formRef = ref<FormInstance>()

// 提交加载状态
const submitLoading = ref(false)

// 角色选项
const roleOptions = ref<Array<{ label: string; value: string }>>([])
const roleLoading = ref(false)
// 全部角色数据（用于判断是否含学生角色）
const allRoles = ref<RoleInfo[]>([])

// 企业/方向/专业级联选项
const selectedEnterpriseId = ref<string>('')
const selectedDirectionId = ref<string>('')
const enterpriseList = ref<EnterpriseVO[]>([])
const directionList = ref<MajorDirectionVO[]>([])
const majorList = ref<MajorVO[]>([])
const enterpriseLoading = ref(false)
const directionLoading = ref(false)
const majorLoading = ref(false)

// 表单数据
const formState = reactive<CreateUserDTO & { userCode?: string }>({
  username: '',
  password: '',
  realName: '',
  userEmail: '',
  userPhone: '',
  gender: 1,
  department: '',
  major: '',
  userCode: '',
  roleIds: [],
  majorId: '',
  enterpriseId: ''
})

// 是否选中了学生角色
const isStudentRoleSelected = computed(() =>
  formState.roleIds.some(id => allRoles.value.find(r => r.roleId === id)?.roleCode === 'STUDENT')
)

// 级联选项数据
const enterpriseOptions = computed(() =>
  enterpriseList.value.map(e => ({ label: e.enterpriseName, value: e.enterpriseId }))
)
const directionOptions = computed(() =>
  directionList.value.map(d => ({ label: d.directionName, value: d.directionId }))
)
const majorOptions = computed(() =>
  majorList.value.map(m => ({ label: m.majorName, value: m.majorId }))
)

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

// 密码强度校验
const validatePassword = async (_rule: Rule, value: string) => {
  if (!isEdit.value && value) {
    if (value.length < 6) {
      return Promise.reject('密码长度不能少于6位')
    }
    if (value.length > 32) {
      return Promise.reject('密码长度不能超过32位')
    }
    if (!/^(?=.*[a-zA-Z])(?=.*\d)/.test(value)) {
      return Promise.reject('密码必须包含字母和数字')
    }
  }
  return Promise.resolve()
}

// 表单校验规则
const formRules: Record<string, Rule[]> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
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
  ],
  roleIds: [
    { required: true, message: '请选择至少一个角色', trigger: 'change', type: 'array' }
  ]
}

/**
 * 加载角色列表（下拉选项）
 */
const loadRoleOptions = async () => {
  roleLoading.value = true
  try {
    const response = await roleApi.getAllRoles()
    allRoles.value = response.data || []
    roleOptions.value = allRoles.value.map((role: RoleInfo) => ({
      label: role.roleName,
      value: role.roleId
    }))
  } catch (error) {
    console.error('加载角色列表失败:', error)
  } finally {
    roleLoading.value = false
  }
}

/**
 * 加载企业列表
 */
const loadEnterpriseList = async () => {
  enterpriseLoading.value = true
  try {
    const res = await enterpriseApi.getAllEnterprises()
    enterpriseList.value = res.data || []
  } catch (error) {
    console.error('加载企业列表失败:', error)
  } finally {
    enterpriseLoading.value = false
  }
}

/**
 * 企业选择变化时加载专业方向
 */
const handleEnterpriseChange = async (enterpriseId: string) => {
  directionList.value = []
  majorList.value = []
  selectedDirectionId.value = ''
  formState.majorId = ''
  // 同步设置企业ID到表单数据
  formState.enterpriseId = enterpriseId || ''
  if (!enterpriseId) return
  directionLoading.value = true
  try {
    const res = await majorApi.getDirectionList(enterpriseId)
    directionList.value = res.data || []
  } catch (error) {
    console.error('加载专业方向失败:', error)
  } finally {
    directionLoading.value = false
  }
}

/**
 * 专业方向选择变化时加载专业
 */
const handleDirectionChange = async (directionId: string) => {
  majorList.value = []
  formState.majorId = ''
  if (!directionId) return
  majorLoading.value = true
  try {
    const res = await majorApi.getMajorList(directionId)
    majorList.value = res.data || []
  } catch (error) {
    console.error('加载专业列表失败:', error)
  } finally {
    majorLoading.value = false
  }
}

/**
 * 重置表单数据
 */
const resetForm = () => {
  formState.username = ''
  formState.password = ''
  formState.realName = ''
  formState.userEmail = ''
  formState.userPhone = ''
  formState.gender = 1
  formState.department = ''
  formState.major = ''
  formState.userCode = ''
  formState.roleIds = []
  formState.majorId = ''
  formState.enterpriseId = ''
  selectedEnterpriseId.value = ''
  selectedDirectionId.value = ''
  directionList.value = []
  majorList.value = []
  formRef.value?.clearValidate()
}

/**
 * 填充编辑数据
 */
const fillFormData = async (user: UserVO) => {
  formState.username = user.username
  formState.realName = user.realName
  formState.userEmail = user.userEmail
  formState.userPhone = user.userPhone
  formState.gender = user.gender || 1
  formState.department = user.department || ''
  formState.major = user.major || ''
  formState.userCode = user.userCode || ''
  formState.roleIds = (user.roles || []).map(role => role.roleId)
  formState.majorId = user.majorId || ''
  formState.enterpriseId = user.enterpriseId || ''
  // 如果有所属专业，预加载级联数据以便展示
  if (user.majorId) {
    try {
      const res = await majorApi.getMajorDetail(user.majorId)
      const major = res.data
      if (major) {
        selectedEnterpriseId.value = major.enterpriseId
        selectedDirectionId.value = major.directionId
        await Promise.all([
          majorApi.getDirectionList(major.enterpriseId).then(r => {
            directionList.value = r.data || []
          }),
          majorApi.getMajorList(major.directionId).then(r => {
            majorList.value = r.data || []
          })
        ])
      }
    } catch (error) {
      console.error('加载用户专业信息失败:', error)
    }
  }
}

// 监听弹窗状态变化
watch(
  () => props.open,
  async (newVal) => {
    if (newVal) {
      loadRoleOptions()
      loadEnterpriseList()
      if (props.userData) {
        await fillFormData(props.userData)
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
    if (isEdit.value && props.userData) {
      // 编辑模式
      const updateData: UpdateUserDTO = {
        realName: formState.realName,
        userEmail: formState.userEmail,
        userPhone: formState.userPhone,
        gender: formState.gender,
        department: formState.department,
        major: formState.major,
        userCode: formState.userCode,
        roleIds: formState.roleIds,
        majorId: formState.majorId || undefined,
        enterpriseId: formState.enterpriseId || undefined
      }
      await userApi.updateUser(props.userData.userId, updateData)
      message.success('用户信息更新成功')
    } else {
      // 新建模式
      const createData: CreateUserDTO = {
        username: formState.username,
        password: formState.password,
        realName: formState.realName,
        userEmail: formState.userEmail,
        userPhone: formState.userPhone,
        gender: formState.gender,
        department: formState.department,
        major: formState.major,
        userCode: formState.userCode,
        roleIds: formState.roleIds,
        majorId: formState.majorId || undefined,
        enterpriseId: formState.enterpriseId || undefined
      }
      await userApi.createUser(createData)
      message.success('用户创建成功')
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
.user-form {
  padding-top: 16px;
}
</style>
