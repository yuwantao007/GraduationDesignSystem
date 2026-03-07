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
          @blur="handleNameBlur"
        />
      </a-form-item>

      <!-- 企业编码（自动生成，可手动覆盖） -->
      <a-form-item label="企业编码" name="enterpriseCode">
        <a-input-group compact>
          <a-input
            v-model:value="formState.enterpriseCode"
            placeholder="可手动输入或点击右侧自动生成"
            :maxlength="50"
            style="width: calc(100% - 90px)"
          />
          <a-button
            type="default"
            :loading="codeGenerating"
            style="width: 90px"
            @click="handleGenerateCode"
          >
            自动生成
          </a-button>
        </a-input-group>
      </a-form-item>

      <!-- 企业负责人（搜索+下拉选择） -->
      <a-form-item label="企业负责人" name="leaderId">
        <a-select
          v-model:value="formState.leaderId"
          show-search
          placeholder="输入姓名或账号搜索企业负责人"
          :filter-option="false"
          :not-found-content="leaderSearching ? undefined : '未找到匹配的企业负责人'"
          allow-clear
          @search="handleLeaderSearch"
          @change="handleLeaderChange"
          @focus="handleLeaderFocus"
        >
          <template v-if="leaderSearching" #notFoundContent>
            <a-spin size="small" />
          </template>
          <a-select-option
            v-for="leader in leaderOptions"
            :key="leader.userId"
            :value="leader.userId"
            :label="leader.realName"
          >
            <div class="leader-option">
              <span class="leader-name">{{ leader.realName }}</span>
              <span class="leader-meta">{{ leader.userCode || leader.username }}</span>
            </div>
          </a-select-option>
        </a-select>
      </a-form-item>

      <!-- 联系电话（负责人选定后自动填充） -->
      <a-form-item label="联系电话" name="contactPhone">
        <a-input
          v-model:value="formState.contactPhone"
          placeholder="请输入联系电话"
          :maxlength="11"
        />
      </a-form-item>

      <!-- 联系邮箱（负责人选定后自动填充） -->
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
 * @description 支持创建和编辑企业信息，企业编码自动生成，负责人从下拉搜索中选择
 * @author YuWan
 * @date 2026-03-07
 */
import { ref, reactive, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { enterpriseApi } from '@/api/enterprise'
import type { EnterpriseVO, CreateEnterpriseDTO, UpdateEnterpriseDTO } from '@/types/enterprise'
import type { UserVO } from '@/types/user'

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
// 编码生成加载状态
const codeGenerating = ref(false)
// 负责人搜索加载状态
const leaderSearching = ref(false)
// 负责人下拉选项
const leaderOptions = ref<UserVO[]>([])
// 搜索防抖 timer
let leaderSearchTimer: ReturnType<typeof setTimeout> | null = null

// 表单数据
const formState = reactive<CreateEnterpriseDTO & { enterpriseStatus?: number; leaderId?: string }>({
  enterpriseName: '',
  enterpriseCode: '',
  leaderId: undefined,
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
 * 企业名称失焦时，若编码为空则自动生成
 */
const handleNameBlur = async () => {
  if (!formState.enterpriseName || formState.enterpriseCode) return
  await doGenerateCode(formState.enterpriseName)
}

/**
 * 点击"自动生成"按钮
 */
const handleGenerateCode = async () => {
  if (!formState.enterpriseName) {
    message.warning('请先输入企业名称')
    return
  }
  await doGenerateCode(formState.enterpriseName)
}

/**
 * 调用后端接口生成企业编码
 */
const doGenerateCode = async (name: string) => {
  codeGenerating.value = true
  try {
    const res = await enterpriseApi.generateCode(name)
    formState.enterpriseCode = res.data || ''
  } catch {
    message.error('编码生成失败，请手动输入')
  } finally {
    codeGenerating.value = false
  }
}

/**
 * 负责人搜索框获取焦点时加载全部（首次）
 */
const handleLeaderFocus = () => {
  if (leaderOptions.value.length === 0) {
    fetchLeaders('')
  }
}

/**
 * 负责人搜索（防抖 300ms）
 */
const handleLeaderSearch = (keyword: string) => {
  if (leaderSearchTimer) clearTimeout(leaderSearchTimer)
  leaderSearchTimer = setTimeout(() => fetchLeaders(keyword), 300)
}

/**
 * 调用后端搜索企业负责人
 */
const fetchLeaders = async (keyword: string) => {
  leaderSearching.value = true
  try {
    const res = await enterpriseApi.searchLeaders(keyword || undefined)
    leaderOptions.value = res.data || []
  } catch {
    // ignore
  } finally {
    leaderSearching.value = false
  }
}

/**
 * 选中负责人后自动填充联系方式
 */
const handleLeaderChange = (userId: string | undefined) => {
  if (!userId) {
    formState.contactPhone = ''
    formState.contactEmail = ''
    return
  }
  const selected = leaderOptions.value.find(l => l.userId === userId)
  if (selected) {
    formState.contactPhone = selected.userPhone || ''
    formState.contactEmail = selected.userEmail || ''
  }
}

/**
 * 重置表单数据
 */
const resetForm = () => {
  formState.enterpriseName = ''
  formState.enterpriseCode = ''
  formState.leaderId = undefined
  formState.contactPhone = ''
  formState.contactEmail = ''
  formState.address = ''
  formState.description = ''
  formState.enterpriseStatus = 1
  leaderOptions.value = []
  formRef.value?.clearValidate()
}

/**
 * 填充编辑数据
 */
const fillFormData = (enterprise: EnterpriseVO) => {
  formState.enterpriseName = enterprise.enterpriseName
  formState.enterpriseCode = enterprise.enterpriseCode || ''
  formState.leaderId = enterprise.leaderId || undefined
  formState.contactPhone = enterprise.contactPhone || ''
  formState.contactEmail = enterprise.contactEmail || ''
  formState.address = enterprise.address || ''
  formState.description = enterprise.description || ''
  formState.enterpriseStatus = enterprise.enterpriseStatus

  // 若已有负责人，预填选项以便回显名称
  if (enterprise.leaderId && enterprise.leaderName) {
    leaderOptions.value = [{
      userId: enterprise.leaderId,
      realName: enterprise.leaderName,
      username: '',
      userPhone: enterprise.leaderPhone || '',
      userEmail: enterprise.leaderEmail || '',
      userCode: '',
      userStatus: 1,
      createTime: '',
      roles: []
    } as UserVO]
  }
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
        leaderId: formState.leaderId,
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
        leaderId: formState.leaderId,
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

.leader-option {
  display: flex;
  align-items: center;
  gap: 10px;

  .leader-name {
    font-weight: 500;
    color: rgba(0, 0, 0, 0.88);
  }

  .leader-meta {
    color: rgba(0, 0, 0, 0.45);
    font-size: 12px;
  }
}
</style>
