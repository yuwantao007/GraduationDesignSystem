<template>
  <div class="student-document-center">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的文档中心</h2>
      <a-button type="primary" @click="showUploadModal = true">
        <upload-outlined />
        上传文档
      </a-button>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-bar">
      <a-space>
        <a-select
          v-model:value="filterType"
          placeholder="全部类型"
          style="width: 160px"
          allow-clear
          @change="loadDocuments"
        >
          <a-select-option
            v-for="option in documentTypeOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </a-select-option>
        </a-select>
        <a-button @click="loadDocuments">
          <reload-outlined />
          刷新
        </a-button>
      </a-space>
    </div>

    <!-- 文档列表 -->
    <a-spin :spinning="loading">
      <div v-if="documentList.length === 0 && !loading" class="empty-container">
        <a-empty description="暂无文档">
          <a-button type="primary" @click="showUploadModal = true">
            立即上传
          </a-button>
        </a-empty>
      </div>

      <div v-else class="document-grid">
        <!-- 按类型分组展示 -->
        <template v-for="(group, type) in groupedDocuments" :key="type">
          <div v-if="group.length > 0" class="document-group">
            <div class="group-title">
              <folder-outlined />
              {{ getDocumentTypeLabel(Number(type)) }}
              <a-tag color="blue">{{ group.length }}</a-tag>
            </div>
            <div class="document-list">
              <div
                v-for="doc in group"
                :key="doc.documentId"
                class="document-card"
                :class="{ 'is-latest': doc.isLatest === 1 }"
              >
                <div class="card-icon">
                  <file-pdf-outlined v-if="doc.fileSuffix === 'pdf'" />
                  <file-word-outlined v-else-if="['doc', 'docx'].includes(doc.fileSuffix)" />
                  <file-zip-outlined v-else-if="['zip', 'rar', '7z'].includes(doc.fileSuffix)" />
                  <file-outlined v-else />
                </div>
                <div class="card-content">
                  <div class="file-name" :title="doc.fileName">
                    {{ doc.fileName }}
                    <a-tag v-if="doc.isLatest === 1" color="green" size="small">最新</a-tag>
                  </div>
                  <div class="file-meta">
                    <span>v{{ doc.version }}</span>
                    <span>{{ doc.fileSizeDesc }}</span>
                    <span>{{ formatDate(doc.createTime) }}</span>
                  </div>
                  <div v-if="doc.remark" class="file-remark">
                    {{ doc.remark }}
                  </div>
                </div>
                <div class="card-actions">
                  <a-tooltip title="预览">
                    <a-button type="text" size="small" @click="handlePreview(doc)">
                      <eye-outlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="下载">
                    <a-button type="text" size="small" @click="handleDownload(doc)">
                      <download-outlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip v-if="doc.isLatest !== 1" title="设为最新">
                    <a-button type="text" size="small" @click="handleSetLatest(doc)">
                      <check-outlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip v-if="doc.isLatest !== 1" title="删除">
                    <a-popconfirm
                      title="确定要删除此文档吗？"
                      @confirm="handleDelete(doc)"
                    >
                      <a-button type="text" size="small" danger>
                        <delete-outlined />
                      </a-button>
                    </a-popconfirm>
                  </a-tooltip>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </a-spin>

    <!-- 上传弹窗 -->
    <DocumentUploadModal
      v-model:open="showUploadModal"
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
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  UploadOutlined,
  ReloadOutlined,
  FolderOutlined,
  FilePdfOutlined,
  FileWordOutlined,
  FileZipOutlined,
  FileOutlined,
  EyeOutlined,
  DownloadOutlined,
  CheckOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { documentApi } from '@/api/document'
import { DocumentType, DocumentTypeOptions, getDocumentTypeLabel } from '@/types/document'
import type { DocumentInfoVO } from '@/types/document'
import DocumentUploadModal from '@/components/document/DocumentUploadModal.vue'
import DocumentPreviewModal from '@/components/document/DocumentPreviewModal.vue'

// 排除教师批注类型
const documentTypeOptions = DocumentTypeOptions.filter(
  o => o.value !== DocumentType.TEACHER_ANNOTATION
)

const loading = ref(false)
const documentList = ref<DocumentInfoVO[]>([])
const filterType = ref<number | undefined>(undefined)
const showUploadModal = ref(false)
const showPreviewModal = ref(false)
const previewDocumentId = ref('')

// 按类型分组的文档
const groupedDocuments = computed(() => {
  const groups: Record<number, DocumentInfoVO[]> = {}
  documentList.value.forEach(doc => {
    if (!groups[doc.documentType]) {
      groups[doc.documentType] = []
    }
    groups[doc.documentType].push(doc)
  })
  return groups
})

// 加载文档列表
const loadDocuments = async () => {
  loading.value = true
  try {
    const res = await documentApi.getMyDocuments(filterType.value)
    documentList.value = res.data || []
  } catch (error: any) {
    console.error('加载文档列表失败:', error)
    message.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
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

// 设为最新版本
const handleSetLatest = async (doc: DocumentInfoVO) => {
  try {
    await documentApi.setAsLatest(doc.documentId)
    message.success('设置成功')
    loadDocuments()
  } catch (error: any) {
    message.error(error.message || '设置失败')
  }
}

// 删除
const handleDelete = async (doc: DocumentInfoVO) => {
  try {
    await documentApi.deleteDocument(doc.documentId)
    message.success('删除成功')
    loadDocuments()
  } catch (error: any) {
    message.error(error.message || '删除失败')
  }
}

// 上传成功
const handleUploadSuccess = () => {
  loadDocuments()
}

onMounted(() => {
  loadDocuments()
})
</script>

<style scoped>
.student-document-center {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.filter-bar {
  margin-bottom: 24px;
}

.empty-container {
  padding: 80px 0;
  text-align: center;
}

.document-grid {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.document-group {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.group-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 16px;
  color: #333;
}

.document-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.document-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.2s;
}

.document-card:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
}

.document-card.is-latest {
  border-color: #52c41a;
  background: #f6ffed;
}

.card-icon {
  font-size: 32px;
  color: #1890ff;
  flex-shrink: 0;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.file-remark {
  font-size: 12px;
  color: #666;
  background: #f5f5f5;
  padding: 4px 8px;
  border-radius: 4px;
  margin-top: 8px;
}

.card-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
</style>
