<template>
  <div class="arrangement-list">
    <a-card title="答辩安排管理">
      <!-- 搜索栏 -->
      <a-form layout="inline" :model="queryParams" class="search-form">
        <a-form-item label="答辩类型">
          <a-select v-model:value="queryParams.defenseType" placeholder="请选择" allow-clear style="width: 140px">
            <a-select-option v-for="(name, key) in DefenseTypeMap" :key="key" :value="Number(key)">
              {{ name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="课题类别">
          <a-select v-model:value="queryParams.topicCategory" placeholder="请选择" allow-clear style="width: 140px">
            <a-select-option v-for="item in topicCategoryOptions" :key="item" :value="item">
              {{ item }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 操作栏 -->
      <div class="table-operations">
        <a-button type="primary" @click="handleCreate" html-type="button">
          <template #icon><PlusOutlined /></template>
          新建答辩安排
        </a-button>
      </div>

      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="arrangementId"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'defenseType'">
            <a-tag :color="getDefenseTypeColor(record.defenseType)">
              {{ record.defenseTypeName }}
            </a-tag>
          </template>
          <template v-if="column.key === 'panelTeachers'">
            {{ formatPanelTeacherText(record.panelTeacherInfos || []) }}
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click.stop="handleView(record)">详情</a-button>
              <a-button type="link" size="small" @click.stop="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" danger @click.stop="handleDeleteClick(record.arrangementId)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 创建/编辑弹窗 -->
    <a-modal
      :open="modalVisible"
      :title="isEdit ? '编辑答辩安排' : '新建答辩安排'"
      :confirm-loading="submitLoading"
      :mask-closable="false"
      @update:open="(val) => (modalVisible = val)"
      @cancel="() => (modalVisible = false)"
      @ok="handleSubmit"
      width="600px"
    >
      <a-form ref="formRef" :model="formData" :rules="formRules" :label-col="{ span: 6 }">
        <a-form-item label="答辩类型" name="defenseType">
          <a-select v-model:value="formData.defenseType" placeholder="请选择答辩类型">
            <a-select-option v-for="(name, key) in DefenseTypeMap" :key="key" :value="Number(key)">
              {{ name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="课题类别" name="topicCategory">
          <a-select v-model:value="formData.topicCategory" placeholder="请选择课题类别">
            <a-select-option v-for="item in topicCategoryOptions" :key="item" :value="item">
              {{ item }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="对应专业" name="majorId">
          <a-select
            v-model:value="formData.majorId"
            placeholder="请选择对应专业"
            :options="majorOptions"
            :loading="majorOptionsLoading"
            show-search
            :filter-option="filterSelectOption"
          />
        </a-form-item>
        <a-form-item label="答辩时间" name="defenseTime">
          <a-date-picker v-model:value="formData.defenseTime" show-time placeholder="请选择答辩时间" style="width: 100%" />
        </a-form-item>
        <a-form-item label="答辩地点" name="defenseLocation">
          <a-input v-model:value="formData.defenseLocation" placeholder="请输入答辩地点" />
        </a-form-item>
        <a-form-item label="报告截止时间" name="deadline">
          <a-date-picker v-model:value="formData.deadline" show-time placeholder="请选择截止时间（可选）" style="width: 100%" />
        </a-form-item>
        <a-form-item label="答辩组长" name="panelLeader">
          <a-select
            v-model:value="formData.panelLeader"
            placeholder="请选择答辩组长"
            :options="teacherOptions"
            :loading="teacherOptionsLoading"
            show-search
            :filter-option="filterSelectOption"
          />
        </a-form-item>
        <a-form-item label="答辩老师" name="panelMembers">
          <a-select
            v-model:value="formData.panelMembers"
            mode="multiple"
            placeholder="请选择2位答辩老师"
            :options="teacherOptions"
            :loading="teacherOptionsLoading"
            show-search
            :filter-option="filterSelectOption"
          />
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="formData.remark" placeholder="请输入备注（可选）" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 详情弹窗 -->
    <a-modal
      :open="detailVisible"
      title="答辩安排详情"
      :footer="null"
      @update:open="(val) => (detailVisible = val)"
      @cancel="() => (detailVisible = false)"
      width="640px"
    >
      <div v-if="detailLoading" style="text-align: center; padding: 24px 0">
        <a-spin tip="加载中..." />
      </div>
      <a-descriptions v-else-if="detailData" :column="2" bordered size="small">
        <a-descriptions-item label="答辩类型">{{ detailData.defenseTypeName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="课题类别">{{ detailData.topicCategory || '-' }}</a-descriptions-item>
        <a-descriptions-item label="对应专业">{{ detailData.majorName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="答辩时间">{{ detailData.defenseTime || '-' }}</a-descriptions-item>
        <a-descriptions-item label="答辩地点">{{ detailData.defenseLocation || '-' }}</a-descriptions-item>
        <a-descriptions-item label="创建人">{{ detailData.creatorName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="报告截止时间" :span="2">{{ detailData.deadline || '-' }}</a-descriptions-item>
        <a-descriptions-item label="答辩小组" :span="2">{{ panelTeacherText }}</a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</a-descriptions-item>
      </a-descriptions>
      <a-empty v-else description="暂无详情数据" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { defenseApi } from '@/api/defense'
import { userApi } from '@/api/user'
import { majorApi } from '@/api/major'
import { phaseApi } from '@/api/phase'
import { useUserStore } from '@/stores/user'
import { DefenseType, DefenseTypeMap } from '@/types/defense'
import { UserRole } from '@/types/user'
import type { DefenseArrangementVO, ArrangementQueryDTO, CreateArrangementDTO } from '@/types/defense'
import type { UserVO } from '@/types/user'
import type { FormInstance } from 'ant-design-vue'
import dayjs from 'dayjs'

const loading = ref(false)
const submitLoading = ref(false)
const modalVisible = ref(false)
const isEdit = ref(false)
const tableData = ref<DefenseArrangementVO[]>([])
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<DefenseArrangementVO | null>(null)
const formRef = ref<FormInstance>()
const teacherOptions = ref<{ label: string; value: string }[]>([])
const teacherOptionsLoading = ref(false)
const majorOptions = ref<{ label: string; value: string }[]>([])
const majorOptionsLoading = ref(false)
const currentCohort = ref(`${dayjs().year()}届`)
const userStore = useUserStore()

const topicCategoryOptions = ['高职升本', '3+1', '实验班']

const queryParams = reactive<ArrangementQueryDTO>({
  defenseType: undefined,
  topicCategory: undefined,
  cohort: undefined,
  pageNum: 1,
  pageSize: 10
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

type ArrangementFormModel = Omit<CreateArrangementDTO, 'panelTeachers'> & {
  arrangementId?: string
  panelLeader?: string
  panelMembers: string[]
}

const formData = reactive<ArrangementFormModel>({
  defenseType: DefenseType.OPENING,
  topicCategory: '',
  majorId: '',
  cohort: '',
  defenseTime: '',
  defenseLocation: '',
  panelLeader: undefined,
  panelMembers: [],
  deadline: undefined,
  remark: ''
})

const formRules = {
  defenseType: [{ required: true, message: '请选择答辩类型' }],
  topicCategory: [{ required: true, message: '请选择课题类别' }],
  majorId: [{ required: true, message: '请选择对应专业' }],
  defenseTime: [{ required: true, message: '请选择答辩时间' }],
  defenseLocation: [{ required: true, message: '请输入答辩地点' }],
  panelLeader: [{ required: true, message: '请选择答辩组长' }],
  panelMembers: [
    { required: true, message: '请选择2位答辩老师', type: 'array' },
    {
      validator: (_rule: unknown, value: string[]) => {
        const members = value || []
        if (members.length !== 2) {
          return Promise.reject(new Error('答辩老师必须选择2位'))
        }
        if (formData.panelLeader && members.includes(formData.panelLeader)) {
          return Promise.reject(new Error('答辩组长不能同时出现在答辩老师中'))
        }
        return Promise.resolve()
      }
    }
  ]
}

const filterSelectOption = (input: string, option: { label: string; value: string }) => {
  return (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
}

const panelTeacherText = computed(() => {
  if (!detailData.value?.panelTeacherInfos?.length) {
    return '-'
  }
  return formatPanelTeacherText(detailData.value.panelTeacherInfos)
})

const columns = [
  { title: '答辩类型', dataIndex: 'defenseType', key: 'defenseType', width: 100 },
  { title: '课题类别', dataIndex: 'topicCategory', key: 'topicCategory', width: 100 },
  { title: '对应专业', dataIndex: 'majorName', key: 'majorName', width: 160 },
  { title: '答辩时间', dataIndex: 'defenseTime', key: 'defenseTime', width: 160 },
  { title: '答辩地点', dataIndex: 'defenseLocation', key: 'defenseLocation', width: 150 },
  { title: '答辩小组', dataIndex: 'panelTeachers', key: 'panelTeachers', width: 200 },
  { title: '创建人', dataIndex: 'creatorName', key: 'creatorName', width: 100 },
  { title: '操作', key: 'action', width: 160, fixed: 'right' }
]

const getDefenseTypeColor = (type: DefenseType) => {
  const colors: Record<DefenseType, string> = {
    [DefenseType.OPENING]: 'blue',
    [DefenseType.MIDTERM]: 'green',
    [DefenseType.FINAL]: 'orange',
    [DefenseType.SECONDARY]: 'red'
  }
  return colors[type] || 'default'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await defenseApi.pageArrangements({
      ...queryParams,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  queryParams.defenseType = undefined
  queryParams.topicCategory = undefined
  queryParams.cohort = undefined
  handleSearch()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const handleCreate = () => {
  isEdit.value = false
  Object.assign(formData, {
    arrangementId: undefined,
    defenseType: DefenseType.OPENING,
    topicCategory: '',
    majorId: '',
    cohort: currentCohort.value,
    defenseTime: '',
    defenseLocation: '',
    panelLeader: undefined,
    panelMembers: [],
    deadline: undefined,
    remark: ''
  })
  modalVisible.value = true
}

const handleEdit = (record: DefenseArrangementVO) => {
  isEdit.value = true
  Object.assign(formData, {
    arrangementId: record.arrangementId,
    defenseType: record.defenseType,
    topicCategory: record.topicCategory,
    majorId: record.majorId,
    cohort: record.cohort,
    defenseTime: record.defenseTime ? dayjs(record.defenseTime) : undefined,
    defenseLocation: record.defenseLocation,
    panelLeader: record.panelTeachers?.[0],
    panelMembers: record.panelTeachers?.slice(1, 3) || [],
    deadline: record.deadline ? dayjs(record.deadline) : undefined,
    remark: record.remark
  })
  modalVisible.value = true
}

const handleView = async (record: DefenseArrangementVO) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await defenseApi.getArrangementDetail(record.arrangementId)
    if (res.code === 200) {
      detailData.value = res.data
    }
  } catch (error) {
    console.error('加载答辩安排详情失败', error)
    message.error('加载答辩安排详情失败')
  } finally {
    detailLoading.value = false
  }
}

const handleDelete = async (arrangementId: string) => {
  try {
    const res = await defenseApi.deleteArrangement(arrangementId)
    if (res.code === 200) {
      message.success('删除成功')
      fetchData()
    }
  } catch (err) {
    message.error('删除失败')
  }
}

const handleDeleteClick = (arrangementId: string) => {
  Modal.confirm({
    title: '确定删除该安排？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await handleDelete(arrangementId)
    }
  })
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true
    const panelTeachers = [formData.panelLeader, ...formData.panelMembers].filter(Boolean) as string[]
    const submitData = {
      defenseType: formData.defenseType,
      topicCategory: formData.topicCategory,
      majorId: formData.majorId,
      defenseLocation: formData.defenseLocation,
      panelTeachers,
      remark: formData.remark,
      cohort: formData.cohort || currentCohort.value,
      defenseTime: formData.defenseTime ? dayjs(formData.defenseTime).format('YYYY-MM-DD HH:mm:ss') : '',
      deadline: formData.deadline ? dayjs(formData.deadline).format('YYYY-MM-DD HH:mm:ss') : undefined
    }
    if (isEdit.value) {
      const res = await defenseApi.updateArrangement({
        arrangementId: formData.arrangementId as string,
        ...submitData
      })
      if (res.code === 200) {
        message.success('更新成功')
        modalVisible.value = false
        fetchData()
      }
    } else {
      const res = await defenseApi.createArrangement(submitData)
      if (res.code === 200) {
        message.success('创建成功')
        modalVisible.value = false
        fetchData()
      }
    }
  } catch (err) {
    // 表单验证失败
  } finally {
    submitLoading.value = false
  }
}

const loadTeacherOptions = async () => {
  teacherOptionsLoading.value = true
  try {
    const teacherRoleCodes = [
      UserRole.SUPERVISOR_TEACHER,
      UserRole.UNIVERSITY_TEACHER,
      UserRole.MAJOR_DIRECTOR,
      UserRole.ENTERPRISE_TEACHER
    ]

    const responses = await Promise.all(
      teacherRoleCodes.map(roleCode =>
        userApi.getUserList({
          roleCode,
          userStatus: 1,
          pageNum: 1,
          pageSize: 500
        })
      )
    )

    const teacherMap = new Map<string, UserVO>()
    responses.forEach(res => {
      ;(res.data.records || []).forEach(user => {
        teacherMap.set(user.userId, user)
      })
    })

    const roleLabelMap: Record<string, string> = {
      [UserRole.SUPERVISOR_TEACHER]: '督导教师',
      [UserRole.UNIVERSITY_TEACHER]: '高校教师',
      [UserRole.MAJOR_DIRECTOR]: '专业方向主管',
      [UserRole.ENTERPRISE_TEACHER]: '企业教师'
    }

    teacherOptions.value = Array.from(teacherMap.values()).map(user => {
      const teacherRoleNames = (user.roles || [])
        .map(role => role.roleCode)
        .filter(roleCode => teacherRoleCodes.includes(roleCode as UserRole))
        .map(roleCode => roleLabelMap[roleCode] || roleCode)
      return {
        label: teacherRoleNames.length
          ? `${user.realName}（${teacherRoleNames.join(' / ')}）`
          : user.realName,
        value: user.userId
      }
    })
  } catch (error) {
    console.error('加载教师列表失败', error)
    message.error('加载答辩小组教师失败')
  } finally {
    teacherOptionsLoading.value = false
  }
}

const loadMajorOptions = async () => {
  majorOptionsLoading.value = true
  try {
    const res = await majorApi.getMajorTree(userStore.userInfo?.enterpriseId, 1)
    const options: { label: string; value: string }[] = []
    const walk = (nodes: Array<{ id: string; label: string; type: string; children?: any[] }>) => {
      nodes.forEach(node => {
        if (node.type === 'major') {
          options.push({ label: node.label, value: node.id })
        }
        if (Array.isArray(node.children) && node.children.length > 0) {
          walk(node.children)
        }
      })
    }
    walk(res.data || [])
    majorOptions.value = options
  } catch (error) {
    console.error('加载专业列表失败', error)
    message.error('加载专业列表失败')
  } finally {
    majorOptionsLoading.value = false
  }
}

const formatPanelTeacherText = (teachers: Array<{ realName: string }>) => {
  if (!teachers.length) {
    return '-'
  }
  const leader = teachers[0]?.realName || '-'
  const members = teachers.slice(1).map(item => item.realName).join('、')
  if (!members) {
    return `组长：${leader}`
  }
  return `组长：${leader}；答辩老师：${members}`
}

const loadCurrentCohort = async () => {
  try {
    const res = await phaseApi.getCurrentPhaseStatus()
    if (res.data?.cohort) {
      currentCohort.value = res.data.cohort
    }
  } catch (error) {
    console.warn('加载当前届别失败，使用默认届别', error)
  }
}

onMounted(() => {
  Promise.all([loadCurrentCohort(), loadTeacherOptions(), loadMajorOptions()])
  fetchData()
})
</script>

<style scoped>
.arrangement-list {
  padding: 16px;
}
.search-form {
  margin-bottom: 16px;
}
.table-operations {
  margin-bottom: 16px;
}
</style>
