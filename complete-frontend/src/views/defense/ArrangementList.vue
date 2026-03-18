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
          <a-input v-model:value="queryParams.topicCategory" placeholder="请输入" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item label="毕业届别">
          <a-input v-model:value="queryParams.cohort" placeholder="如：2026届" allow-clear style="width: 140px" />
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
        <a-button type="primary" @click="handleCreate">
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
            <span v-for="(teacher, index) in record.panelTeacherInfos" :key="teacher.userId">
              {{ teacher.realName }}<span v-if="index < record.panelTeacherInfos.length - 1">、</span>
            </span>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">详情</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-popconfirm title="确定删除该安排？" @confirm="handleDelete(record.arrangementId)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 创建/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑答辩安排' : '新建答辩安排'"
      :confirm-loading="submitLoading"
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
          <a-input v-model:value="formData.topicCategory" placeholder="如：高职升本、3+1、实验班" />
        </a-form-item>
        <a-form-item label="毕业届别" name="cohort">
          <a-input v-model:value="formData.cohort" placeholder="如：2026届" />
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
        <a-form-item label="答辩小组教师" name="panelTeachers">
          <a-select
            v-model:value="formData.panelTeachers"
            mode="multiple"
            placeholder="请选择答辩小组教师"
            :options="teacherOptions"
          />
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="formData.remark" placeholder="请输入备注（可选）" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { defenseApi } from '@/api/defense'
import { DefenseType, DefenseTypeMap } from '@/types/defense'
import type { DefenseArrangementVO, ArrangementQueryDTO, CreateArrangementDTO } from '@/types/defense'
import type { FormInstance } from 'ant-design-vue'
import dayjs from 'dayjs'

const loading = ref(false)
const submitLoading = ref(false)
const modalVisible = ref(false)
const isEdit = ref(false)
const tableData = ref<DefenseArrangementVO[]>([])
const formRef = ref<FormInstance>()
const teacherOptions = ref<{ label: string; value: string }[]>([])

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

const formData = reactive<CreateArrangementDTO & { arrangementId?: string }>({
  defenseType: DefenseType.OPENING,
  topicCategory: '',
  cohort: '',
  defenseTime: '',
  defenseLocation: '',
  panelTeachers: [],
  deadline: undefined,
  remark: ''
})

const formRules = {
  defenseType: [{ required: true, message: '请选择答辩类型' }],
  topicCategory: [{ required: true, message: '请输入课题类别' }],
  cohort: [{ required: true, message: '请输入毕业届别' }],
  defenseTime: [{ required: true, message: '请选择答辩时间' }],
  defenseLocation: [{ required: true, message: '请输入答辩地点' }],
  panelTeachers: [{ required: true, message: '请选择答辩小组教师', type: 'array' }]
}

const columns = [
  { title: '答辩类型', dataIndex: 'defenseType', key: 'defenseType', width: 100 },
  { title: '课题类别', dataIndex: 'topicCategory', key: 'topicCategory', width: 100 },
  { title: '毕业届别', dataIndex: 'cohort', key: 'cohort', width: 100 },
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
    cohort: '',
    defenseTime: '',
    defenseLocation: '',
    panelTeachers: [],
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
    cohort: record.cohort,
    defenseTime: record.defenseTime ? dayjs(record.defenseTime) : undefined,
    defenseLocation: record.defenseLocation,
    panelTeachers: record.panelTeachers,
    deadline: record.deadline ? dayjs(record.deadline) : undefined,
    remark: record.remark
  })
  modalVisible.value = true
}

const handleView = (record: DefenseArrangementVO) => {
  // 可跳转到详情页或弹窗查看
  message.info('查看详情: ' + record.arrangementId)
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

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true
    const submitData = {
      ...formData,
      defenseTime: formData.defenseTime ? dayjs(formData.defenseTime).format('YYYY-MM-DD HH:mm:ss') : '',
      deadline: formData.deadline ? dayjs(formData.deadline).format('YYYY-MM-DD HH:mm:ss') : undefined
    }
    if (isEdit.value) {
      const res = await defenseApi.updateArrangement(submitData as any)
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

onMounted(() => {
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
