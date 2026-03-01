<template>
  <a-modal
    :open="open"
    :title="isEdit ? '编辑专业方向' : '新建专业方向'"
    :width="560"
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
      class="direction-form"
    >
      <!-- 方向名称 -->
      <a-form-item label="方向名称" name="directionName">
        <a-input
          v-model:value="formState.directionName"
          placeholder="请输入专业方向名称"
          :maxlength="100"
        />
      </a-form-item>

      <!-- 方向代码 -->
      <a-form-item label="方向代码" name="directionCode">
        <a-input
          v-model:value="formState.directionCode"
          placeholder="自动生成"
          :disabled="true"
          :maxlength="50"
        />
      </a-form-item>

      <!-- 负责人选择 -->
      <a-form-item label="负责人" name="leaderId">
        <a-select
          v-model:value="formState.leaderId"
          placeholder="请选择负责人（可选）"
          :options="leaderOptions"
          :loading="leaderLoading"
          show-search
          :filter-option="filterLeaderOption"
          allow-clear
        />
      </a-form-item>

      <!-- 排序 -->
      <a-form-item label="排序" name="sortOrder">
        <a-input-number
          v-model:value="formState.sortOrder"
          :min="0"
          :max="9999"
          placeholder="数字越小越靠前"
          style="width: 100%"
        />
      </a-form-item>

      <!-- 描述 -->
      <a-form-item label="描述" name="description">
        <a-textarea
          v-model:value="formState.description"
          placeholder="请输入方向描述（可选）"
          :maxlength="500"
          :rows="3"
          show-count
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 专业方向新建/编辑弹窗组件
 * @description 支持创建和编辑专业方向信息
 * @author YuWan
 * @date 2026-03-01
 */
import { ref, reactive, watch, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { majorApi } from '@/api/major'
import { userApi } from '@/api/user'
import { enterpriseApi } from '@/api/enterprise'
import type { MajorDirectionVO, MajorDirectionDTO } from '@/types/major'
import type { UserVO } from '@/types/user'
import type { EnterpriseVO } from '@/types/enterprise'

defineOptions({
  name: 'DirectionFormModal'
})

// Props 定义
interface Props {
  /** 弹窗是否显示 */
  open: boolean
  /** 编辑时传入的专业方向数据 */
  directionData?: MajorDirectionVO | null
  /** 所属企业ID（新建时必传，用于生成代码前缀） */
  enterpriseId?: string
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  directionData: null,
  enterpriseId: ''
})

// Emits 定义
const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
  cancel: []
}>()

// 是否为编辑模式
const isEdit = computed(() => !!props.directionData)

// 表单引用
const formRef = ref<FormInstance>()

// 提交加载状态
const submitLoading = ref(false)

// 负责人选项
const leaderOptions = ref<{ value: string; label: string }[]>([])
const leaderLoading = ref(false)

// 企业代码（用于生成方向代码）
const enterpriseCode = ref('')

// 表单数据
const formState = reactive<MajorDirectionDTO>({
  enterpriseId: undefined,
  directionName: '',
  directionCode: '',
  leaderId: undefined,
  sortOrder: 0,
  description: ''
})

// 表单校验规则
const formRules: Record<string, Rule[]> = {
  directionName: [
    { required: true, message: '请输入专业方向名称', trigger: 'blur' },
    { max: 100, message: '方向名称长度不能超过100个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述长度不能超过500个字符', trigger: 'blur' }
  ]
}

/**
 * 加载负责人列表
 */
const loadLeaderList = async () => {
  leaderLoading.value = true
  try {
    const response = await userApi.getLeaderList()
    leaderOptions.value = response.data.map((user: UserVO) => ({
      value: user.userId,
      label: `${user.realName}（${user.username}）`
    }))
  } catch (error) {
    console.error('加载负责人列表失败:', error)
  } finally {
    leaderLoading.value = false
  }
}

/**
 * 负责人搜索过滤
 */
const filterLeaderOption = (input: string, option: any) => {
  return option.label.toLowerCase().includes(input.toLowerCase())
}

/**
 * 生成方向代码
 */
const generateDirectionCode = () => {
  if (!enterpriseCode.value) return ''
  // 生成格式：企业代码 + D + 4位随机数字
  const randomNum = Math.floor(1000 + Math.random() * 9000)
  return `${enterpriseCode.value}D${randomNum}`
}

/**
 * 加载企业信息（获取企业代码）
 */
const loadEnterpriseCode = async () => {
  if (!props.enterpriseId) return
  try {
    const response = await enterpriseApi.getEnterpriseDetail(props.enterpriseId)
    enterpriseCode.value = response.data.enterpriseCode || ''
  } catch (error) {
    console.error('加载企业信息失败:', error)
  }
}

/**
 * 重置表单数据
 */
const resetForm = () => {
  formState.enterpriseId = props.enterpriseId || undefined
  formState.directionName = ''
  formState.directionCode = ''
  formState.leaderId = undefined
  formState.sortOrder = 0
  formState.description = ''
  formRef.value?.clearValidate()
  
  // 新建模式：自动生成代码
  if (!isEdit.value && enterpriseCode.value) {
    formState.directionCode = generateDirectionCode()
  }
}

/**
 * 填充表单数据（编辑模式）
 */
const fillFormData = (data: MajorDirectionVO) => {
  formState.enterpriseId = data.enterpriseId
  formState.directionName = data.directionName
  formState.directionCode = data.directionCode || ''
  formState.leaderId = data.leaderId
  formState.sortOrder = data.sortOrder || 0
  formState.description = data.description || ''
}

/**
 * 监听弹窗开关和数据变化
 */
watch(
  () => [props.open, props.directionData, props.enterpriseId],
  async ([newOpen, newData, newEnterpriseId]) => {
    if (newOpen) {
      // 加载企业代码
      if (newEnterpriseId) {
        await loadEnterpriseCode()
      }
      
      if (newData) {
        fillFormData(newData as MajorDirectionVO)
      } else {
        resetForm()
      }
    }
  },
  { immediate: true }
)

/**
 * 组件挂载时加载负责人列表
 */
onMounted(() => {
  loadLeaderList()
})

/**
 * 提交表单
 */
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true

    if (isEdit.value && props.directionData) {
      // 编辑模式
      await majorApi.updateDirection(props.directionData.directionId, formState)
      message.success('编辑成功')
    } else {
      // 新建模式
      await majorApi.addDirection(formState)
      message.success('创建成功')
    }

    emit('update:open', false)
    emit('success')
  } catch (error: unknown) {
    // 表单校验失败不处理，其他错误已由全局处理
    if (error && typeof error === 'object' && 'errorFields' in error) {
      return
    }
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

/**
 * 取消操作
 */
const handleCancel = () => {
  emit('update:open', false)
  emit('cancel')
}
</script>

<style scoped lang="scss">
.direction-form {
  padding-top: 16px;
}
</style>
