<template>
  <a-modal
    :open="open"
    :title="isEdit ? '编辑专业' : '新建专业'"
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
      class="major-form"
    >
      <!-- 所属方向 -->
      <a-form-item label="所属方向" name="directionId">
        <a-select
          v-model:value="formState.directionId"
          placeholder="请选择所属专业方向"
          :options="directionOptions"
          :loading="directionLoading"
          :disabled="isEdit"
        />
      </a-form-item>

      <!-- 专业名称 -->
      <a-form-item label="专业名称" name="majorName">
        <a-input
          v-model:value="formState.majorName"
          placeholder="请输入专业名称"
          :maxlength="100"
        />
      </a-form-item>

      <!-- 专业代码 -->
      <a-form-item label="专业代码" name="majorCode">
        <a-input
          v-model:value="formState.majorCode"
          placeholder="自动生成"
          :disabled="true"
          :maxlength="50"
        />
      </a-form-item>

      <!-- 学位类型 -->
      <a-form-item label="学位类型" name="degreeType">
        <a-select
          v-model:value="formState.degreeType"
          placeholder="请选择学位类型"
          allow-clear
        >
          <a-select-option value="academic">学术学位</a-select-option>
          <a-select-option value="professional">专业学位</a-select-option>
        </a-select>
      </a-form-item>

      <!-- 学制 -->
      <a-form-item label="学制" name="educationYears">
        <a-input-number
          v-model:value="formState.educationYears"
          :min="1"
          :max="10"
          :addon-after="'年'"
          placeholder="学制年限"
          style="width: 100%"
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
          placeholder="请输入专业描述（可选）"
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
 * 专业新建/编辑弹窗组件
 * @description 支持创建和编辑专业信息
 * @author YuWan
 * @date 2026-03-01
 */
import { ref, reactive, watch, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { majorApi } from '@/api/major'
import type { MajorVO, MajorDTO, MajorDirectionVO } from '@/types/major'

defineOptions({
  name: 'MajorFormModal'
})

// Props 定义
interface Props {
  /** 弹窗是否显示 */
  open: boolean
  /** 编辑时传入的专业数据 */
  majorData?: MajorVO | null
  /** 预选的专业方向ID（新建时使用） */
  preSelectedDirectionId?: string
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  majorData: null,
  preSelectedDirectionId: ''
})

// Emits 定义
const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
  cancel: []
}>()

// 是否为编辑模式
const isEdit = computed(() => !!props.majorData)

// 表单引用
const formRef = ref<FormInstance>()

// 提交加载状态
const submitLoading = ref(false)

// 专业方向选项（包含方向代码用于生成专业代码）
const directionOptions = ref<{ value: string; label: string; directionCode?: string }[]>([])
const directionLoading = ref(false)

// 表单数据
const formState = reactive<MajorDTO>({
  directionId: '',
  majorName: '',
  majorCode: '',
  degreeType: undefined,
  educationYears: 3,
  sortOrder: 0,
  description: ''
})

// 当前选中的方向代码（用于生成专业代码）
const currentDirectionCode = ref('')

// 表单校验规则
const formRules: Record<string, Rule[]> = {
  directionId: [
    { required: true, message: '请选择所属专业方向', trigger: 'change' }
  ],
  majorName: [
    { required: true, message: '请输入专业名称', trigger: 'blur' },
    { max: 100, message: '专业名称长度不能超过100个字符', trigger: 'blur' }
  ],
  majorCode: [
    { max: 50, message: '专业代码长度不能超过50个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述长度不能超过500个字符', trigger: 'blur' }
  ]
}

/**
 * 加载专业方向列表
 */
const loadDirectionList = async () => {
  directionLoading.value = true
  try {
    const response = await majorApi.getDirectionList()
    directionOptions.value = response.data.map((item: MajorDirectionVO) => ({
      value: item.directionId,
      label: item.directionName,
      directionCode: item.directionCode // 保存方向代码
    }))
    
    // 加载完成后，如果已有选中的方向，生成专业代码
    if (!isEdit.value && formState.directionId) {
      const direction = directionOptions.value.find(opt => opt.value === formState.directionId)
      if (direction && direction.directionCode) {
        currentDirectionCode.value = direction.directionCode
        formState.majorCode = generateMajorCode(direction.directionCode)
      }
    }
  } catch (error) {
    console.error('加载专业方向列表失败:', error)
  } finally {
    directionLoading.value = false
  }
}

/**
 * 生成专业代码
 */
const generateMajorCode = (directionCode: string) => {
  if (!directionCode) return ''
  // 生成格式：方向代码 + M + 4位随机数字
  const randomNum = Math.floor(1000 + Math.random() * 9000)
  return `${directionCode}M${randomNum}`
}

/**
 * 监听专业方向选择变化，自动生成专业代码
 */
watch(
  () => formState.directionId,
  (newDirectionId) => {
    if (!isEdit.value && newDirectionId) {
      // 从选项中获取方向代码
      const direction = directionOptions.value.find(opt => opt.value === newDirectionId)
      if (direction && direction.directionCode) {
        currentDirectionCode.value = direction.directionCode
        formState.majorCode = generateMajorCode(direction.directionCode)
      }
    }
  }
)

/**
 * 重置表单数据
 */
const resetForm = () => {
  formState.directionId = props.preSelectedDirectionId || ''
  formState.majorName = ''
  formState.majorCode = ''
  formState.degreeType = undefined
  formState.educationYears = 3
  formState.sortOrder = 0
  formState.description = ''
  formRef.value?.clearValidate()
}

/**
 * 填充表单数据（编辑模式）
 */
const fillFormData = (data: MajorVO) => {
  formState.directionId = data.directionId
  formState.majorName = data.majorName
  formState.majorCode = data.majorCode || ''
  formState.degreeType = data.degreeType
  formState.educationYears = data.educationYears || 3
  formState.sortOrder = data.sortOrder || 0
  formState.description = data.description || ''
}

/**
 * 监听弹窗开关和数据变化
 */
watch(
  () => [props.open, props.majorData],
  async ([newOpen, newData]) => {
    if (newOpen) {
      // 先重置表单或填充数据
      if (newData) {
        fillFormData(newData as MajorVO)
      } else {
        resetForm()
      }
      // 然后加载方向列表（它会自动生成代码）
      await loadDirectionList()
    }
  },
  { immediate: true }
)

/**
 * 监听预选方向ID变化
 */
watch(
  () => props.preSelectedDirectionId,
  (newId) => {
    if (!isEdit.value && newId) {
      formState.directionId = newId
    }
  }
)

/**
 * 提交表单
 */
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true

    if (isEdit.value && props.majorData) {
      // 编辑模式
      await majorApi.updateMajor(props.majorData.majorId, formState)
      message.success('编辑成功')
    } else {
      // 新建模式
      await majorApi.addMajor(formState)
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
.major-form {
  padding-top: 16px;
}
</style>
