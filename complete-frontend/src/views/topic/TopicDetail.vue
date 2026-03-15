<!--
  课题详情页面
  以只读形式展示课题的完整信息（类似纸质报告格式）
  
  @author 系统架构师
  @version 1.0
  @since 2026-02-22
-->
<template>
  <div class="topic-detail">
    <!-- 操作按钮区 -->
    <a-card class="topic-detail__actions" :bordered="false">
      <a-space>
        <a-button @click="handleBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回
        </a-button>
        <a-button v-if="topicData?.isSubmitted === 0" type="primary" @click="handleEdit">
          <template #icon><EditOutlined /></template>
          编辑
        </a-button>
        <a-button type="primary" @click="handlePrint">
          <template #icon><PrinterOutlined /></template>
          打印
        </a-button>
      </a-space>
    </a-card>

    <!-- 审查流程状态卡片（仅课题已提交时显示） -->
    <ProcessStatusCard
      v-if="topicData?.isSubmitted === 1 && topicData?.topicId"
      :topic-id="topicData.topicId"
      class="topic-detail__flow-status"
    />

    <!-- 详情内容区 -->
    <a-card v-if="topicData" class="topic-detail__content" :bordered="false" :loading="loading">
      <!-- 文档标题 -->
      <div class="topic-detail-title">毕业设计（论文）任务书</div>

      <!-- 主表格 - 包含所有内容 -->
      <table class="main-table" cellpadding="0" cellspacing="0">
        <!-- 题目行 -->
        <tr>
          <td class="main-table-label" style="width: 100px">题目</td>
          <td class="main-table-value" colspan="5">{{ topicData.topicTitle }}</td>
        </tr>
        
        <!-- 基本信息行1 -->
        <tr>
          <td class="main-table-label">学生姓名</td>
          <td class="main-table-value">{{ topicData.creatorName || '-' }}</td>
          <td class="main-table-label" style="width: 80px">专业</td>
          <td class="main-table-value">{{ topicData.guidanceDirection || '-' }}</td>
          <td class="main-table-label" style="width: 80px">班级</td>
          <td class="main-table-value" style="width: 120px">{{ classInfo || '-' }}</td>
        </tr>
        
        <!-- 基本信息行2 -->
        <tr>
          <td class="main-table-label">课题类型</td>
          <td class="main-table-value">{{ topicData.topicTypeDesc || '-' }}</td>
          <td class="main-table-label">课题来源</td>
          <td class="main-table-value" colspan="3">{{ topicData.topicSourceDesc || '-' }}</td>
        </tr>
        
        <!-- 课题综述（包含1-5及任务与进度要求、主要参考文献） -->
        <tr>
          <td class="main-table-label section-label">课题综述</td>
          <td class="main-table-value section-content" colspan="5">
            <div class="section-item">
              <div class="section-header">1、选题背景与意义</div>
              <div class="section-text">{{ topicData.backgroundSignificance || '-' }}</div>
            </div>
            
            <div class="section-item">
              <div class="section-header">2、课题内容简述</div>
              <div class="section-text">{{ topicData.contentSummary || '-' }}</div>
            </div>
            
            <div class="section-item">
              <div class="section-header">3、对专业知识的综合训练情况</div>
              <div class="section-text">{{ topicData.professionalTraining || '-' }}</div>
            </div>
            
            <div class="section-item">
              <div class="section-header">4、开发环境（工具）</div>
              <div class="section-text">{{ parsedDevelopmentEnvironment }}</div>
            </div>
            
            <div class="section-item">
              <div class="section-header">5、工作量（预计完成所需周数）</div>
              <div class="section-text">{{ parsedWorkloadDetail }}</div>
            </div>
          </td>
        </tr>
        
        <!-- 任务与进度要求 -->
        <tr>
          <td class="main-table-label section-label">任务与进度要求</td>
          <td class="main-table-value section-content" colspan="5">
            <div class="section-text">{{ parsedScheduleRequirements }}</div>
          </td>
        </tr>
        
        <!-- 主要参考文献 -->
        <tr>
          <td class="main-table-label section-label">主要参考文献</td>
          <td class="main-table-value section-content" colspan="5">
            <div class="section-text">{{ parsedTopicReferences }}</div>
          </td>
        </tr>
        
        <!-- 起止日期 -->
        <tr>
          <td class="main-table-label">起止日期</td>
          <td class="main-table-value" colspan="5">
            {{ topicData.startDate && topicData.endDate
              ? `${topicData.startDate} ~ ${topicData.endDate}`
              : '-'
            }}
          </td>
        </tr>
        
        <!-- 备注 -->
        <tr>
          <td class="main-table-label">备注</td>
          <td class="main-table-value" colspan="5">{{ topicData.remark || '-' }}</td>
        </tr>
      </table>

      <!-- 签名栏 -->
      <div class="signature-section">
        <div class="signature-item">
          <span class="signature-label">学院负责人</span>
          <span class="signature-line"></span>
        </div>
        <div class="signature-item">
          <span class="signature-label">企业（负责人）</span>
          <span class="signature-line"></span>
        </div>
        <div class="signature-item">
          <span class="signature-label">企业指导教师</span>
          <span class="signature-line"></span>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  EditOutlined,
  PrinterOutlined
} from '@ant-design/icons-vue'
import { topicApi } from '@/api/topic'
import type { TopicVO } from '@/types/topic'
import { TopicReviewStatus } from '@/types/topic'
import ProcessStatusCard from '@/components/workflow/ProcessStatusCard.vue'

// 定义组件选项
defineOptions({
  name: 'TopicDetail'
})

// 路由
const router = useRouter()
const route = useRoute()

// 数据
const topicData = ref<TopicVO>()
const loading = ref(false)

// 附加字段
const classInfo = ref('24实战优才')

/**
 * 解析开发环境内容
 */
const parsedDevelopmentEnvironment = computed(() => {
  const data = topicData.value?.developmentEnvironment
  if (!data) return '-'
  if (typeof data === 'string') return data || '-'
  if (data.content) return data.content
  return '-'
})

/**
 * 解析工作量明细内容
 */
const parsedWorkloadDetail = computed(() => {
  const data = topicData.value?.workloadDetail
  if (!data) return '-'
  if (typeof data === 'string') return data || '-'
  if (Array.isArray(data) && data[0]?.content) return data[0].content
  return '-'
})

/**
 * 解析任务与进度要求内容
 */
const parsedScheduleRequirements = computed(() => {
  const data = topicData.value?.scheduleRequirements
  if (!data) return '-'
  if (typeof data === 'string') return data || '-'
  if (Array.isArray(data) && data[0]?.content) return data[0].content
  return '-'
})

/**
 * 解析参考文献内容
 */
const parsedTopicReferences = computed(() => {
  const data = topicData.value?.topicReferences
  if (!data) return '-'
  if (typeof data === 'string') return data || '-'
  if (Array.isArray(data) && data[0]?.content) return data[0].content
  return '-'
})

/**
 * 获取审查状态颜色
 */
const getReviewStatusColor = (status?: TopicReviewStatus): string => {
  if (!status) return 'default'
  
  const colorMap: Record<TopicReviewStatus, string> = {
    [TopicReviewStatus.PENDING_PRE_REVIEW]: 'processing',
    [TopicReviewStatus.PRE_REVIEW_PASSED]: 'success',
    [TopicReviewStatus.PRE_REVIEW_REJECTED]: 'error',
    [TopicReviewStatus.PENDING_FINAL_REVIEW]: 'processing',
    [TopicReviewStatus.FINAL_REVIEW_PASSED]: 'success',
    [TopicReviewStatus.FINAL_REVIEW_REJECTED]: 'error'
  }
  
  return colorMap[status] || 'default'
}

/**
 * 获取课题详情
 */
const getTopicDetail = async () => {
  try {
    loading.value = true
    const topicId = route.params.id as string
    const result = await topicApi.getTopicDetail(topicId)
    topicData.value = result.data
  } catch (error) {
    console.error('获取课题详情失败', error)
    message.error('获取课题详情失败')
  } finally {
    loading.value = false
  }
}

/**
 * 返回
 */
const handleBack = () => {
  router.back()
}

/**
 * 编辑
 */
const handleEdit = () => {
  router.push(`/topic/edit/${route.params.id}`)
}

/**
 * 打印
 */
const handlePrint = () => {
  window.print()
}

// 页面加载
onMounted(() => {
  getTopicDetail()
})
</script>

<style scoped lang="scss">
.topic-detail {
  padding: 20px;

  &__actions {
    margin-bottom: 16px;

    @media print {
      display: none;
    }
  }

  &__flow-status {
    max-width: 1000px;
    margin: 0 auto 16px auto;

    @media print {
      display: none;
    }
  }

  &__content {
    max-width: 1000px;
    margin: 0 auto;
  }
}

// 文档标题样式
.topic-detail-title {
  font-size: 22px;
  font-weight: bold;
  text-align: center;
  padding: 20px 0;
  color: #000;
}

// 主表格样式 - 黑色边框
.main-table {
  width: 100%;
  border-collapse: collapse;
  border: 2px solid #000;
  margin-bottom: 20px;
  table-layout: fixed;

  td {
    border: 1px solid #000;
    padding: 10px 12px;
    vertical-align: top;
    color: #000;
    font-size: 14px;
    line-height: 1.8;
  }

  &-label {
    background-color: #fff;
    font-weight: 500;
    text-align: center;
    width: 100px;
    vertical-align: middle !important;
  }

  &-value {
    background-color: #fff;
    word-break: break-word;
  }
}

// 大段落标签样式（课题综述、任务与进度要求、主要参考文献）
.section-label {
  writing-mode: horizontal-tb;
  text-align: center;
  vertical-align: middle !important;
  font-weight: bold;
}

// 大段落内容样式
.section-content {
  padding: 15px !important;
}

// 子章节样式
.section-item {
  margin-bottom: 20px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.section-header {
  font-weight: bold;
  color: #000;
  font-size: 14px;
  margin-bottom: 8px;
}

.section-text {
  color: #000;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  text-align: justify;
}

// 签名栏样式
.signature-section {
  display: flex;
  justify-content: space-around;
  padding: 40px 0 20px;
  margin-top: 20px;
  
  .signature-item {
    display: flex;
    align-items: center;
    
    .signature-label {
      color: #000;
      margin-right: 10px;
      white-space: nowrap;
      font-weight: 500;
    }
    
    .signature-line {
      display: inline-block;
      width: 120px;
      border-bottom: 1px solid #000;
      height: 20px;
    }
  }
}

// 打印样式
@media print {
  .topic-detail {
    padding: 0;
    
    &__actions {
      display: none;
    }
    
    &__content {
      max-width: 100%;
      box-shadow: none !important;
      background: transparent !important;
      border: none !important;
      padding: 0 !important;
      
      // 隐藏 Ant Design Card 的所有样式
      :deep(.ant-card-body) {
        padding: 0 !important;
        background: transparent !important;
      }
    }
  }
  
  .main-table {
    border: 2px solid #000 !important;
    
    td {
      border: 1px solid #000 !important;
      -webkit-print-color-adjust: exact;
      print-color-adjust: exact;
    }
  }
  
  .signature-section {
    page-break-inside: avoid;
  }
  
  // 避免内容被截断
  .section-item {
    page-break-inside: avoid;
  }
}
</style>

<!-- 全局打印样式（不使用 scoped，用于隐藏布局元素） -->
<style lang="scss">
@media print {
  // 隐藏侧边栏
  .ant-layout-sider {
    display: none !important;
  }
  
  // 隐藏顶部栏
  .ant-layout-header {
    display: none !important;
  }
  
  // 隐藏底部栏
  .ant-layout-footer {
    display: none !important;
  }
  
  // 隐藏面包屑
  .ant-breadcrumb {
    display: none !important;
  }
  
  // 主内容区去掉边距和背景
  .ant-layout-content {
    margin: 0 !important;
    padding: 0 !important;
    
    > div {
      padding: 0 !important;
      background: transparent !important;
    }
  }
  
  // 确保主布局全宽
  .ant-layout {
    background: transparent !important;
  }
  
  // 确保页面无边距
  body {
    margin: 0 !important;
    padding: 0 !important;
  }
  
  // 确保打印内容占据全宽
  .topic-detail {
    width: 100% !important;
    max-width: 100% !important;
  }
}
</style>
