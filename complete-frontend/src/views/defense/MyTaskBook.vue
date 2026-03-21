<template>
  <div class="my-taskbook">
    <a-card title="我的任务书" :loading="loading">
      <template v-if="taskBookData">
        <div class="taskbook-actions">
          <a-button @click="handlePrint">打印</a-button>
        </div>

        <div id="task-book-print-area">
          <div class="task-book-title">毕业设计（论文）任务书</div>

          <table class="task-book-table" cellpadding="0" cellspacing="0" v-if="topicDetail">
            <tr>
              <td class="tbl-label" style="width: 90px">题目</td>
              <td class="tbl-value" colspan="5">{{ topicDetail.topicTitle || taskBookData.topicName || '-' }}</td>
            </tr>
            <tr>
              <td class="tbl-label">学生姓名</td>
              <td class="tbl-value">{{ taskBookData.studentName || '-' }}</td>
              <td class="tbl-label" style="width: 80px">专业</td>
              <td class="tbl-value">{{ topicDetail.guidanceDirection || '-' }}</td>
              <td class="tbl-label" style="width: 80px">班级</td>
              <td class="tbl-value">-</td>
            </tr>
            <tr>
              <td class="tbl-label">课题类型</td>
              <td class="tbl-value">{{ topicDetail.topicTypeDesc || '-' }}</td>
              <td class="tbl-label">课题来源</td>
              <td class="tbl-value" colspan="3">{{ topicDetail.topicSourceDesc || '-' }}</td>
            </tr>
            <tr>
              <td class="tbl-label sec-label">课题综述</td>
              <td class="tbl-value sec-content" colspan="5">
                <div class="section-item">
                  <div class="section-header">1、选题背景与意义</div>
                  <div class="sec-text">{{ topicDetail.backgroundSignificance || '-' }}</div>
                </div>
                <div class="section-item">
                  <div class="section-header">2、课题内容简述</div>
                  <div class="sec-text">{{ topicDetail.contentSummary || '-' }}</div>
                </div>
                <div class="section-item">
                  <div class="section-header">3、对专业知识的综合训练情况</div>
                  <div class="sec-text">{{ topicDetail.professionalTraining || '-' }}</div>
                </div>
                <div class="section-item">
                  <div class="section-header">4、开发环境（工具）</div>
                  <div class="sec-text">{{ parsedDevelopmentEnvironment }}</div>
                </div>
                <div class="section-item">
                  <div class="section-header">5、工作量（预计完成所需周数）</div>
                  <div class="sec-text">{{ parsedWorkloadDetail }}</div>
                </div>
              </td>
            </tr>
            <tr>
              <td class="tbl-label sec-label">任务与进度要求</td>
              <td class="tbl-value sec-content" colspan="5">
                <div class="sec-text">{{ parsedScheduleRequirements }}</div>
              </td>
            </tr>
            <tr>
              <td class="tbl-label sec-label">主要参考文献</td>
              <td class="tbl-value sec-content" colspan="5">
                <div class="sec-text">{{ parsedTopicReferences }}</div>
              </td>
            </tr>
            <tr>
              <td class="tbl-label">起止日期</td>
              <td class="tbl-value" colspan="5">
                {{ topicDetail.startDate && topicDetail.endDate
                  ? `${topicDetail.startDate} ~ ${topicDetail.endDate}`
                  : '-' }}
              </td>
            </tr>
            <tr>
              <td class="tbl-label">备注</td>
              <td class="tbl-value" colspan="5">{{ topicDetail.remark || '-' }}</td>
            </tr>
            <tr v-if="taskBookData.documentId">
              <td class="tbl-label">附件</td>
              <td class="tbl-value" colspan="5">
                <a :href="taskBookData.documentUrl" target="_blank">
                  {{ taskBookData.documentName || '下载附件' }}
                </a>
              </td>
            </tr>
          </table>

          <div v-else class="taskbook-fallback" v-html="taskBookData.content"></div>

          <div class="task-book-signature">
            <div class="sig-item">
              <span class="sig-label">学院负责人</span>
              <span class="sig-line"></span>
            </div>
            <div class="sig-item">
              <span class="sig-label">企业（负责人）</span>
              <span class="sig-line"></span>
            </div>
            <div class="sig-item">
              <span class="sig-label">企业指导教师</span>
              <span class="sig-line"></span>
            </div>
          </div>
        </div>
      </template>
      <template v-else>
        <a-empty description="指导教师尚未为您编写任务书" />
      </template>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { defenseApi } from '@/api/defense'
import { topicApi } from '@/api/topic'
import type { OpeningTaskBookVO } from '@/types/defense'
import type { TopicVO } from '@/types/topic'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const taskBookData = ref<OpeningTaskBookVO | null>(null)
const topicDetail = ref<TopicVO | null>(null)

const parsedDevelopmentEnvironment = computed(() => {
  const data = topicDetail.value?.developmentEnvironment as any
  if (!data) return '-'
  if (typeof data === 'string') return data || '-'
  if (data.content) return data.content
  return '-'
})

const parsedWorkloadDetail = computed(() => {
  const data = topicDetail.value?.workloadDetail as any
  if (!data) return '-'
  if (typeof data === 'string') return data || '-'
  if (Array.isArray(data) && data[0]?.content) return data[0].content
  return '-'
})

const parsedScheduleRequirements = computed(() => {
  const data = topicDetail.value?.scheduleRequirements as any
  if (!data) return '-'
  if (typeof data === 'string') return data || '-'
  if (Array.isArray(data) && data[0]?.content) return data[0].content
  return '-'
})

const parsedTopicReferences = computed(() => {
  const data = topicDetail.value?.topicReferences as any
  if (!data) return '-'
  if (typeof data === 'string') return data || '-'
  if (Array.isArray(data) && data[0]?.content) return data[0].content
  return '-'
})

const fetchMyTaskBook = async () => {
  loading.value = true
  try {
    const userId = userStore.userInfo?.userId
    if (userId) {
      const res = await defenseApi.getTaskBookByStudent(userId)
      if (res.code === 200) {
        taskBookData.value = res.data
        if (res.data?.topicId) {
          await fetchTopicDetail(res.data.topicId)
        }
      }
    }
  } catch (error) {
    console.error('获取我的任务书失败:', error)
    message.error('获取我的任务书失败')
  } finally {
    loading.value = false
  }
}

const fetchTopicDetail = async (topicId: string) => {
  try {
    const result = await topicApi.getTopicDetail(topicId)
    topicDetail.value = result.data
  } catch (error) {
    topicDetail.value = null
    console.error('获取课题详情失败:', error)
  }
}

const handlePrint = () => {
  const el = document.getElementById('task-book-print-area')
  if (!el) return
  const win = window.open('', '_blank')
  if (!win) return
  win.document.write(`
    <html><head><title>我的任务书</title>
    <style>
      body { font-family: '宋体', serif; margin: 20px; color: #000; }
      .task-book-title { font-size:22px; font-weight:bold; text-align:center; padding:20px 0; }
      table { width:100%; border-collapse:collapse; border:2px solid #000; margin-bottom:20px; }
      td { border:1px solid #000; padding:10px 12px; vertical-align:top; font-size:14px; line-height:1.8; }
      .tbl-label { font-weight:500; text-align:center; width:100px; vertical-align:middle; background:#fafafa; }
      .sec-label { text-align:center; font-weight:bold; }
      .sec-content { padding:15px; }
      .sec-text { white-space:pre-wrap; word-break:break-word; text-align:justify; line-height:1.8; }
      .section-item { margin-bottom:16px; }
      .section-item:last-child { margin-bottom:0; }
      .section-header { font-weight:600; margin-bottom:8px; }
      .task-book-signature { display:flex; justify-content:space-around; padding:40px 0 20px; margin-top:20px; }
      .sig-item { display:flex; align-items:center; }
      .sig-label { margin-right:10px; font-weight:500; }
      .sig-line { display:inline-block; width:120px; border-bottom:1px solid #000; height:20px; }
    </style>
    </head><body>${el.innerHTML}</body></html>
  `)
  win.document.close()
  win.focus()
  win.print()
  win.close()
}

onMounted(() => {
  fetchMyTaskBook()
})
</script>

<style scoped>
.my-taskbook {
  padding: 16px;
}

.taskbook-actions {
  margin-bottom: 16px;
  text-align: right;
}

.task-book-title {
  font-size: 20px;
  font-weight: bold;
  text-align: center;
  padding: 16px 0 20px;
  color: #000;
}

.task-book-table {
  width: 100%;
  border-collapse: collapse;
  border: 2px solid #000;
  margin-bottom: 20px;
  table-layout: fixed;
}

.task-book-table td {
  border: 1px solid #000;
  padding: 10px 12px;
  vertical-align: top;
  color: #000;
  font-size: 14px;
  line-height: 1.8;
}

.tbl-label {
  background-color: #fafafa;
  font-weight: 500;
  text-align: center;
  width: 100px;
  vertical-align: middle !important;
}

.tbl-value {
  background-color: #fff;
  word-break: break-word;
}

.sec-label {
  text-align: center;
  vertical-align: middle !important;
  font-weight: bold;
}

.sec-content {
  padding: 15px !important;
}

.section-item {
  margin-bottom: 16px;
}

.section-item:last-child {
  margin-bottom: 0;
}

.section-header {
  font-weight: 600;
  margin-bottom: 8px;
}

.sec-text {
  color: #000;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  text-align: justify;
}

.taskbook-fallback {
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 16px;
  background: #fff;
}

.task-book-signature {
  display: flex;
  justify-content: space-around;
  padding: 24px 0 8px;
}

.sig-item {
  display: flex;
  align-items: center;
}

.sig-label {
  margin-right: 10px;
  font-weight: 500;
}

.sig-line {
  display: inline-block;
  width: 120px;
  border-bottom: 1px solid #000;
  height: 20px;
}
</style>
