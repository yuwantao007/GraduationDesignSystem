<template>
  <div class="teacher-document-view">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>学生文档查看</h2>
    </div>

    <!-- 学生选择 -->
    <div class="student-selector">
      <a-card title="选择学生" :bordered="false">
        <a-spin :spinning="loadingStudents">
          <div v-if="studentList.length === 0 && !loadingStudents" class="empty-hint">
            暂无可查看的学生
          </div>
          <div v-else class="student-grid">
            <div
              v-for="student in studentList"
              :key="student.studentId"
              class="student-item"
              :class="{ active: selectedStudentId === student.studentId }"
              @click="selectStudent(student)"
            >
              <a-avatar :size="48" style="background-color: #1890ff">
                {{ student.studentName?.charAt(0) }}
              </a-avatar>
              <div class="student-info">
                <div class="student-name">{{ student.studentName }}</div>
                <div class="student-no">{{ student.studentNo }}</div>
                <div class="topic-title" v-if="student.topicTitle">
                  {{ student.topicTitle }}
                </div>
              </div>
            </div>
          </div>
        </a-spin>
      </a-card>
    </div>

    <!-- 文档列表 -->
    <div v-if="selectedStudentId" class="document-section">
      <a-card :bordered="false">
        <template #title>
          <div class="card-title">
            <span>{{ selectedStudent?.studentName }} 的文档</span>
            <a-button type="primary" @click="showUploadModal = true">
              <upload-outlined />
              上传批注
            </a-button>
          </div>
        </template>

        <a-spin :spinning="loadingDocuments">
          <a-tabs v-model:activeKey="activeTab">
            <a-tab-pane key="all" tab="全部文档">
              <DocumentTable
                :documents="documentList"
                @preview="handlePreview"
                @download="handleDownload"
              />
            </a-tab-pane>
            <a-tab-pane key="project" :tab="`项目代码 (${projectCodeDocs.length})`">
              <DocumentTable
                :documents="projectCodeDocs"
                @preview="handlePreview"
                @download="handleDownload"
              />
            </a-tab-pane>
            <a-tab-pane key="thesis" :tab="`论文文档 (${thesisDocs.length})`">
              <DocumentTable
                :documents="thesisDocs"
                @preview="handlePreview"
                @download="handleDownload"
              />
            </a-tab-pane>
            <a-tab-pane key="opening" :tab="`开题报告 (${openingReportDocs.length})`">
              <DocumentTable
                :documents="openingReportDocs"
                @preview="handlePreview"
                @download="handleDownload"
              />
            </a-tab-pane>
            <a-tab-pane key="midterm" :tab="`中期检查表 (${midtermCheckDocs.length})`">
              <DocumentTable
                :documents="midtermCheckDocs"
                @preview="handlePreview"
                @download="handleDownload"
              />
            </a-tab-pane>
            <a-tab-pane key="annotation" :tab="`我的批注 (${teacherAnnotationDocs.length})`">
              <DocumentTable
                :documents="teacherAnnotationDocs"
                @preview="handlePreview"
                @download="handleDownload"
              />
            </a-tab-pane>
          </a-tabs>
        </a-spin>
      </a-card>
    </div>

    <!-- 上传弹窗 -->
    <DocumentUploadModal
      v-model:open="showUploadModal"
      :is-teacher-mode="true"
      :student-list="studentListForUpload"
      @success="handleUploadSuccess"
    />

    <!-- 预览弹窗 -->
    <DocumentPreviewModal
      v-model:open="showPreviewModal"
      :document-id="previewDocumentId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h, resolveComponent } from 'vue'
import { message } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { documentApi } from '@/api/document'
import { guidanceApi } from '@/api/guidance'
import { DocumentType, getDocumentTypeLabel } from '@/types/document'
import type { DocumentInfoVO } from '@/types/document'
import DocumentUploadModal from '@/components/document/DocumentUploadModal.vue'
import DocumentPreviewModal from '@/components/document/DocumentPreviewModal.vue'

interface StudentItem {
  studentId: string
  studentName: string
  studentNo: string
  topicId?: string
  topicTitle?: string
}

// 文档表格组件
const DocumentTable = {
  props: {
    documents: {
      type: Array as () => DocumentInfoVO[],
      default: () => []
    }
  },
  emits: ['preview', 'download'],
  setup(props: any, { emit }: any) {
    const columns = [
      {
        title: '文件名',
        dataIndex: 'fileName',
        key: 'fileName',
        ellipsis: true
      },
      {
        title: '类型',
        dataIndex: 'documentTypeDesc',
        key: 'documentType',
        width: 120
      },
      {
        title: '版本',
        key: 'version',
        width: 80,
        customRender: ({ record }: any) => {
          return `v${record.version}${record.isLatest === 1 ? ' (最新)' : ''}`
        }
      },
      {
        title: '大小',
        dataIndex: 'fileSizeDesc',
        key: 'fileSize',
        width: 100
      },
      {
        title: '上传人',
        dataIndex: 'uploaderName',
        key: 'uploaderName',
        width: 100
      },
      {
        title: '上传时间',
        key: 'createTime',
        width: 160,
        customRender: ({ record }: any) => {
          return dayjs(record.createTime).format('YYYY-MM-DD HH:mm')
        }
      },
      {
        title: '操作',
        key: 'action',
        width: 120,
        customRender: ({ record }: any) => {
          return h('div', { class: 'action-buttons' }, [
            h('a', {
              onClick: () => emit('preview', record),
              style: { marginRight: '16px' }
            }, '预览'),
            h('a', {
              onClick: () => emit('download', record)
            }, '下载')
          ])
        }
      }
    ]

    return () => h(resolveComponent('a-table'), {
      columns,
      dataSource: props.documents,
      rowKey: 'documentId',
      size: 'small',
      pagination: { pageSize: 10 }
    })
  }
}

const loadingStudents = ref(false)
const loadingDocuments = ref(false)
const studentList = ref<StudentItem[]>([])
const selectedStudentId = ref<string>('')
const documentList = ref<DocumentInfoVO[]>([])
const activeTab = ref('all')
const showUploadModal = ref(false)
const showPreviewModal = ref(false)
const previewDocumentId = ref('')

// 选中的学生
const selectedStudent = computed(() => {
  return studentList.value.find(s => s.studentId === selectedStudentId.value)
})

// 按类型筛选的文档
const projectCodeDocs = computed(() =>
  documentList.value.filter(d => d.documentType === DocumentType.PROJECT_CODE)
)
const thesisDocs = computed(() =>
  documentList.value.filter(d => d.documentType === DocumentType.THESIS_DOCUMENT)
)
const openingReportDocs = computed(() =>
  documentList.value.filter(d => d.documentType === DocumentType.OPENING_REPORT)
)
const midtermCheckDocs = computed(() =>
  documentList.value.filter(d => d.documentType === DocumentType.MIDTERM_CHECK)
)
const teacherAnnotationDocs = computed(() =>
  documentList.value.filter(d => d.documentType === DocumentType.TEACHER_ANNOTATION)
)

// 上传弹窗需要的学生列表格式
const studentListForUpload = computed(() => {
  return studentList.value.map(s => ({
    studentId: s.studentId,
    studentName: s.studentName,
    studentNo: s.studentNo
  }))
})

// 加载学生列表
const loadStudents = async () => {
  loadingStudents.value = true
  try {
    // 复用指导记录模块的学生列表接口
    const res = await guidanceApi.getMyStudents()
    studentList.value = (res.data || []).map((item: any) => ({
      studentId: item.studentId,
      studentName: item.studentName,
      studentNo: item.studentNo,
      topicId: item.topicId,
      topicTitle: item.topicTitle
    }))
  } catch (error: any) {
    console.error('加载学生列表失败:', error)
    message.error(error.message || '加载学生列表失败')
  } finally {
    loadingStudents.value = false
  }
}

// 选择学生
const selectStudent = (student: StudentItem) => {
  selectedStudentId.value = student.studentId
  loadDocuments()
}

// 加载文档列表
const loadDocuments = async () => {
  if (!selectedStudentId.value) return

  loadingDocuments.value = true
  try {
    const res = await documentApi.getStudentDocuments(selectedStudentId.value)
    documentList.value = res.data || []
  } catch (error: any) {
    console.error('加载文档列表失败:', error)
    message.error(error.message || '加载文档列表失败')
  } finally {
    loadingDocuments.value = false
  }
}

// 预览
const handlePreview = (doc: DocumentInfoVO) => {
  previewDocumentId.value = doc.documentId
  showPreviewModal.value = true
}

// 下载
const handleDownload = async (doc: DocumentInfoVO) => {
  try {
    message.loading('正在下载...', 0)
    const blob = await documentApi.downloadDocument(doc.documentId)

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = doc.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    message.destroy()
    message.success('下载成功')
  } catch (error: any) {
    message.destroy()
    message.error(error.message || '下载失败')
  }
}

// 上传成功
const handleUploadSuccess = () => {
  if (selectedStudentId.value) {
    loadDocuments()
  }
}

onMounted(() => {
  loadStudents()
})
</script>

<style scoped>
.teacher-document-view {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.student-selector {
  margin-bottom: 24px;
}

.empty-hint {
  text-align: center;
  color: #999;
  padding: 40px;
}

.student-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.student-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.student-item:hover {
  background: #f0f0f0;
}

.student-item.active {
  background: #e6f7ff;
  border-color: #1890ff;
}

.student-info {
  flex: 1;
  min-width: 0;
}

.student-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

.student-no {
  font-size: 13px;
  color: #999;
}

.topic-title {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.document-section {
  margin-top: 24px;
}

.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-buttons a {
  color: #1890ff;
}

.action-buttons a:hover {
  color: #40a9ff;
}
</style>
