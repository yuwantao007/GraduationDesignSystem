<template>
  <div class="taskbook-list">
    <a-card title="任务书管理">
      <!-- 搜索栏 -->
      <a-form layout="inline" :model="queryParams" class="search-form">
        <a-form-item label="学生姓名">
          <a-input v-model:value="queryParams.studentName" placeholder="请输入" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 学生列表 -->
      <a-table
        :columns="columns"
        :data-source="studentList"
        :loading="loading"
        row-key="studentId"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'hasTaskBook'">
            <a-tag v-if="record.taskBookId" color="green">已编写</a-tag>
            <a-tag v-else color="orange">未编写</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                {{ record.taskBookId ? '编辑任务书' : '编写任务书' }}
              </a-button>
              <a-button v-if="record.taskBookId" type="link" size="small" @click="handleView(record)">
                查看
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 编辑任务书弹窗 -->
    <a-modal
      v-model:open="editVisible"
      :title="currentStudent?.taskBookId ? '编辑任务书' : '编写任务书'"
      :confirm-loading="submitLoading"
      @ok="handleSubmit"
      width="800px"
    >
      <a-form ref="formRef" :model="taskBookForm" :rules="formRules" :label-col="{ span: 4 }">
        <a-form-item label="学生">
          <span>{{ currentStudent?.studentName }} ({{ currentStudent?.studentNo }})</span>
        </a-form-item>
        <a-form-item label="课题">
          <span>{{ currentStudent?.topicName || '-' }}</span>
        </a-form-item>
        <a-form-item label="任务书内容" name="content">
          <a-textarea
            v-model:value="taskBookForm.content"
            placeholder="请输入任务书内容"
            :rows="10"
          />
        </a-form-item>
        <a-form-item label="附件">
          <a-upload
            v-model:file-list="fileList"
            :max-count="1"
            :before-upload="beforeUpload"
            accept=".doc,.docx,.pdf"
          >
            <a-button>
              <template #icon><UploadOutlined /></template>
              上传附件
            </a-button>
          </a-upload>
          <div class="upload-tip">支持 .doc, .docx, .pdf 格式（可选）</div>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 查看任务书弹窗 -->
    <a-modal
      v-model:open="viewVisible"
      title="查看任务书"
      :footer="null"
      width="800px"
    >
      <a-descriptions bordered :column="1">
        <a-descriptions-item label="学生">
          {{ currentTaskBook?.studentName }} ({{ currentTaskBook?.studentNo }})
        </a-descriptions-item>
        <a-descriptions-item label="课题">
          {{ currentTaskBook?.topicName }}
        </a-descriptions-item>
        <a-descriptions-item label="编辑教师">
          {{ currentTaskBook?.teacherName }}
        </a-descriptions-item>
        <a-descriptions-item label="任务书内容">
          <div v-html="currentTaskBook?.content" class="taskbook-content"></div>
        </a-descriptions-item>
        <a-descriptions-item label="附件" v-if="currentTaskBook?.documentId">
          <a :href="currentTaskBook?.documentUrl" target="_blank">
            {{ currentTaskBook?.documentName || '下载附件' }}
          </a>
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ currentTaskBook?.updateTime }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import { defenseApi } from '@/api/defense'
import type { OpeningTaskBookVO, SaveTaskBookDTO } from '@/types/defense'
import type { FormInstance, UploadProps } from 'ant-design-vue'

interface StudentWithTaskBook {
  studentId: string
  studentName: string
  studentNo: string
  topicId?: string
  topicName?: string
  taskBookId?: string
}

const loading = ref(false)
const submitLoading = ref(false)
const editVisible = ref(false)
const viewVisible = ref(false)
const studentList = ref<StudentWithTaskBook[]>([])
const formRef = ref<FormInstance>()
const fileList = ref<any[]>([])
const currentStudent = ref<StudentWithTaskBook | null>(null)
const currentTaskBook = ref<OpeningTaskBookVO | null>(null)

const queryParams = reactive({
  studentName: ''
})

const taskBookForm = reactive<SaveTaskBookDTO>({
  studentId: '',
  topicId: '',
  content: '',
  documentId: undefined
})

const formRules = {
  content: [{ required: true, message: '请输入任务书内容' }]
}

const columns = [
  { title: '学生姓名', dataIndex: 'studentName', key: 'studentName', width: 100 },
  { title: '学号', dataIndex: 'studentNo', key: 'studentNo', width: 120 },
  { title: '课题名称', dataIndex: 'topicName', key: 'topicName', width: 250, ellipsis: true },
  { title: '任务书状态', dataIndex: 'hasTaskBook', key: 'hasTaskBook', width: 100 },
  { title: '操作', key: 'action', width: 180 }
]

const fetchStudents = async () => {
  loading.value = true
  try {
    // 实际应调用获取学生列表的接口
    // 这里使用模拟数据
    studentList.value = [
      { studentId: '1', studentName: '张三', studentNo: '20260001', topicId: 't1', topicName: '基于Vue的毕业设计管理系统', taskBookId: 'tb1' },
      { studentId: '2', studentName: '李四', studentNo: '20260002', topicId: 't2', topicName: '智能推荐算法研究', taskBookId: undefined }
    ]
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  fetchStudents()
}

const handleReset = () => {
  queryParams.studentName = ''
  fetchStudents()
}

const handleEdit = async (record: StudentWithTaskBook) => {
  currentStudent.value = record
  taskBookForm.studentId = record.studentId
  taskBookForm.topicId = record.topicId || ''
  taskBookForm.content = ''
  taskBookForm.documentId = undefined
  fileList.value = []

  if (record.taskBookId) {
    // 获取已有任务书内容
    const res = await defenseApi.getTaskBookByStudent(record.studentId)
    if (res.code === 200 && res.data) {
      taskBookForm.content = res.data.content || ''
      taskBookForm.documentId = res.data.documentId
    }
  }
  editVisible.value = true
}

const handleView = async (record: StudentWithTaskBook) => {
  const res = await defenseApi.getTaskBookByStudent(record.studentId)
  if (res.code === 200) {
    currentTaskBook.value = res.data
    viewVisible.value = true
  }
}

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  message.info('文件将在提交时上传: ' + file.name)
  return false
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true

    const res = await defenseApi.saveTaskBook(taskBookForm)
    if (res.code === 200) {
      message.success('保存成功')
      editVisible.value = false
      fetchStudents()
    }
  } catch (err) {
    // 验证失败
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchStudents()
})
</script>

<style scoped>
.taskbook-list {
  padding: 16px;
}
.search-form {
  margin-bottom: 16px;
}
.upload-tip {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}
.taskbook-content {
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
