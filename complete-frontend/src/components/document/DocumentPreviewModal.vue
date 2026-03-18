<template>
  <a-modal
    v-model:open="visible"
    :title="previewData?.fileName || '文档预览'"
    :width="900"
    :footer="null"
    :body-style="{ padding: 0, height: '70vh' }"
  >
    <div class="preview-container">
      <!-- 加载中状态 -->
      <div v-if="loading" class="preview-loading">
        <a-spin size="large" tip="正在加载文档..." />
      </div>

      <!-- 不支持预览 -->
      <div v-else-if="previewData && !previewData.supportPreview" class="preview-not-support">
        <div class="icon">
          <file-unknown-outlined style="font-size: 64px; color: #999" />
        </div>
        <p class="message">该文件格式暂不支持在线预览</p>
        <p class="file-name">{{ previewData.fileName }}</p>
        <a-button type="primary" @click="handleDownload">
          <download-outlined />
          下载文件
        </a-button>
      </div>

      <!-- 支持预览 -->
      <div v-else-if="previewData && previewData.supportPreview" class="preview-content">
        <!-- PDF 预览 -->
        <iframe
          v-if="isPdf"
          :src="previewData.previewUrl"
          class="preview-iframe"
          frameborder="0"
        />

        <!-- 图片预览 -->
        <div v-else-if="isImage" class="preview-image">
          <img :src="previewData.previewUrl" :alt="previewData.fileName" />
        </div>

        <!-- 文本预览 -->
        <iframe
          v-else-if="isText"
          :src="previewData.previewUrl"
          class="preview-iframe"
          frameborder="0"
        />

        <!-- 其他格式 -->
        <div v-else class="preview-not-support">
          <div class="icon">
            <file-unknown-outlined style="font-size: 64px; color: #999" />
          </div>
          <p class="message">该文件格式暂不支持在线预览</p>
          <a-button type="primary" @click="handleDownload">
            <download-outlined />
            下载文件
          </a-button>
        </div>
      </div>

      <!-- 加载失败 -->
      <div v-else class="preview-error">
        <div class="icon">
          <close-circle-outlined style="font-size: 64px; color: #ff4d4f" />
        </div>
        <p class="message">加载失败</p>
        <a-button type="primary" @click="loadPreview">重新加载</a-button>
      </div>
    </div>

    <!-- 底部工具栏 -->
    <div v-if="previewData" class="preview-toolbar">
      <span class="file-info">
        {{ previewData.fileName }} | 有效期：{{ Math.floor(previewData.expiresIn / 60) }}分钟
      </span>
      <a-space>
        <a-button @click="handleDownload">
          <download-outlined />
          下载
        </a-button>
        <a-button v-if="previewData.supportPreview" @click="openInNewTab">
          <fullscreen-outlined />
          新窗口打开
        </a-button>
      </a-space>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  FileUnknownOutlined,
  DownloadOutlined,
  CloseCircleOutlined,
  FullscreenOutlined
} from '@ant-design/icons-vue'
import { documentApi } from '@/api/document'
import type { DocumentPreviewVO } from '@/types/document'

interface Props {
  open: boolean
  documentId: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const visible = computed({
  get: () => props.open,
  set: (val) => emit('update:open', val)
})

const loading = ref(false)
const previewData = ref<DocumentPreviewVO | null>(null)

// 判断文件类型
const isPdf = computed(() => {
  return previewData.value?.fileSuffix?.toLowerCase() === 'pdf'
})

const isImage = computed(() => {
  const imageSuffixes = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp']
  return imageSuffixes.includes(previewData.value?.fileSuffix?.toLowerCase() || '')
})

const isText = computed(() => {
  return previewData.value?.fileSuffix?.toLowerCase() === 'txt'
})

// 加载预览信息
const loadPreview = async () => {
  if (!props.documentId) return

  loading.value = true
  previewData.value = null

  try {
    const res = await documentApi.getPreviewUrl(props.documentId)
    previewData.value = res.data
  } catch (error: any) {
    console.error('获取预览URL失败:', error)
    message.error(error.message || '获取预览URL失败')
  } finally {
    loading.value = false
  }
}

// 下载文件
const handleDownload = async () => {
  try {
    message.loading('正在下载...', 0)
    const blob = await documentApi.downloadDocument(props.documentId)

    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = previewData.value?.fileName || 'download'
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

// 新窗口打开
const openInNewTab = () => {
  if (previewData.value?.previewUrl) {
    window.open(previewData.value.previewUrl, '_blank')
  }
}

// 监听弹窗打开加载预览
watch(
  () => props.open,
  (val) => {
    if (val && props.documentId) {
      loadPreview()
    } else {
      previewData.value = null
    }
  }
)
</script>

<style scoped>
.preview-container {
  height: calc(70vh - 60px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.preview-loading,
.preview-not-support,
.preview-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
}

.preview-not-support .icon,
.preview-error .icon {
  margin-bottom: 24px;
}

.preview-not-support .message,
.preview-error .message {
  margin-bottom: 16px;
  font-size: 16px;
  color: #666;
}

.preview-not-support .file-name {
  margin-bottom: 24px;
  font-size: 14px;
  color: #999;
}

.preview-content {
  width: 100%;
  height: 100%;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: auto;
  padding: 16px;
}

.preview-image img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.preview-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}

.file-info {
  font-size: 13px;
  color: #666;
}
</style>
