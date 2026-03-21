<template>
  <div class="my-report">
    <a-card title="我的开题报告" :loading="loading" class="report-shell">
      <template #extra>
        <a-space class="action-bar">
          <a-tag v-if="submitForm.status !== undefined" :color="OpeningReportStatusColor[submitForm.status]">
            {{ OpeningReportStatusMap[submitForm.status] }}
          </a-tag>
          <a-button @click="handlePrintDraft">打印</a-button>
          <a-button @click="handleSaveDraft" :loading="submitLoading">保存草稿</a-button>
          <a-button type="primary" @click="handleFinalize" :loading="submitLoading">提交定稿</a-button>
        </a-space>
      </template>

      <a-form ref="formRef" :model="submitForm" :rules="formRules" :label-col="{ span: 0 }" class="report-form">
        <section class="report-section report-section--meta">
          <div class="section-heading">基础信息</div>
          <a-row :gutter="16">
            <a-col :xs="24" :md="24">
              <a-form-item name="topicId" class="field-item">
                <div class="field-label">课题</div>
                <a-select
                  v-model:value="submitForm.topicId"
                  placeholder="请选择课题"
                  :options="topicOptions"
                  @change="handleTopicChange"
                />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :md="24">
              <a-form-item class="field-item">
                <div class="field-label">报告日期</div>
                <a-date-picker v-model:value="reportDateValue" style="width: 100%" disabled />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :xs="24" :md="24">
              <a-form-item class="field-item">
                <div class="field-label">姓名</div>
                <a-input v-model:value="submitForm.studentName" disabled />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :md="24">
              <a-form-item class="field-item">
                <div class="field-label">专业</div>
                <a-input v-model:value="submitForm.majorName" disabled />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :md="24">
              <a-form-item class="field-item">
                <div class="field-label">班级</div>
                <a-input v-model:value="submitForm.className" disabled />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :xs="24" :md="24">
              <a-form-item class="field-item">
                <div class="field-label">题目快照</div>
                <a-input v-model:value="submitForm.topicTitle" disabled />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :md="24">
              <a-form-item class="field-item">
                <div class="field-label">指导教师</div>
                <a-input v-model:value="submitForm.advisorNames" disabled />
              </a-form-item>
            </a-col>
          </a-row>
        </section>

        <section class="report-section">
          <div class="section-heading">开题报告正文</div>
          <div class="editor-grid">
            <div class="editor-item">
              <a-form-item name="researchStatus" class="field-item">
                <div class="field-label">一-1 研究现状</div>
                <a-textarea v-model:value="submitForm.researchStatus" :rows="5" :maxlength="3000" show-count />
              </a-form-item>
            </div>
            <div class="editor-item">
              <a-form-item name="purposeSignificance" class="field-item">
                <div class="field-label">一-2 目的意义</div>
                <a-textarea v-model:value="submitForm.purposeSignificance" :rows="5" :maxlength="3000" show-count />
              </a-form-item>
            </div>
            <div class="editor-item">
              <a-form-item name="researchContent" class="field-item">
                <div class="field-label">一-3 研究内容</div>
                <a-textarea v-model:value="submitForm.researchContent" :rows="5" :maxlength="3000" show-count />
              </a-form-item>
            </div>
            <div class="editor-item">
              <a-form-item name="innovationPoints" class="field-item">
                <div class="field-label">一-4 创新点</div>
                <a-textarea v-model:value="submitForm.innovationPoints" :rows="5" :maxlength="3000" show-count />
              </a-form-item>
            </div>
            <div class="editor-item editor-item--full">
              <a-form-item name="problemsToSolve" class="field-item">
                <div class="field-label">一-5 拟解决问题</div>
                <a-textarea v-model:value="submitForm.problemsToSolve" :rows="5" :maxlength="3000" show-count />
              </a-form-item>
            </div>
            <div class="editor-item editor-item--full">
              <a-form-item name="progressExpectation" class="field-item">
                <div class="field-label">二 进度及预期结果</div>
                <a-textarea v-model:value="submitForm.progressExpectation" :rows="7" :maxlength="8000" show-count />
              </a-form-item>
            </div>
            <div class="editor-item editor-item--full">
              <a-form-item class="field-item">
                <div class="field-label">现有条件</div>
                <a-textarea v-model:value="submitForm.currentConditions" :rows="5" :maxlength="5000" show-count />
              </a-form-item>
            </div>
          </div>
        </section>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import dayjs, { type Dayjs } from 'dayjs'
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { defenseApi } from '@/api/defense'
import { topicApi } from '@/api/topic'
import { topicSelectionApi } from '@/api/topicSelection'
import { useUserStore } from '@/stores/user'
import { OpeningReportStatus, OpeningReportStatusColor, OpeningReportStatusMap } from '@/types/defense'
import type { OpeningReportVO, SubmitReportDTO } from '@/types/defense'
import type { FormInstance } from 'ant-design-vue'
import type { TopicVO } from '@/types/topic'
import type { TopicSelectionVO } from '@/types/topicSelection'

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const reportData = ref<OpeningReportVO | null>(null)
const formRef = ref<FormInstance>()
const selectedTopic = ref<TopicVO | null>(null)

const topicOptions = ref<{ label: string; value: string }[]>([])

const submitForm = reactive<SubmitReportDTO>({
  topicId: '',
  arrangementId: undefined,
  studentName: '',
  majorName: '',
  className: '',
  topicTitle: '',
  advisorNames: '',
  reportDate: undefined,
  researchStatus: '',
  purposeSignificance: '',
  researchContent: '',
  innovationPoints: '',
  problemsToSolve: '',
  progressExpectation: '',
  currentConditions: '',
  advisorOpinion: '',
  collegeOpinion: '',
  status: OpeningReportStatus.DRAFT
})

const formRules = {
  topicId: [{ required: true, message: '请选择课题' }],
  researchStatus: [{ required: true, message: '请填写研究现状' }],
  purposeSignificance: [{ required: true, message: '请填写研究目的与意义' }],
  researchContent: [{ required: true, message: '请填写研究内容' }],
  innovationPoints: [{ required: true, message: '请填写创新点' }],
  problemsToSolve: [{ required: true, message: '请填写拟解决问题' }],
  progressExpectation: [{ required: true, message: '请填写进度及预期结果' }]
}

const reportDateValue = computed<Dayjs | undefined>({
  get: () => (submitForm.reportDate ? dayjs(submitForm.reportDate) : undefined),
  set: () => {}
})

const fillReadonlyFieldsFromCurrentUser = () => {
  const userInfo = userStore.userInfo
  if (!submitForm.studentName) {
    submitForm.studentName = userInfo?.realName || ''
  }
  if (!submitForm.majorName) {
    submitForm.majorName = userInfo?.major || ''
  }
  if (!submitForm.className) {
    submitForm.className = userInfo?.department || ''
  }
  if (!submitForm.reportDate) {
    submitForm.reportDate = dayjs().format('YYYY-MM-DD')
  }
}

const syncTopicSnapshot = () => {
  const topicTitle = selectedTopic.value?.topicTitle || getTopicLabel(submitForm.topicId)
  submitForm.topicTitle = topicTitle || ''
  submitForm.advisorNames = selectedTopic.value?.creatorName || ''
}

const fetchMyReport = async () => {
  loading.value = true
  try {
    const res = await defenseApi.getMyReport()
    if (res.code === 200) {
      reportData.value = res.data
      if (res.data) {
        patchFormFromReport(res.data)
      } else {
        fillReadonlyFieldsFromCurrentUser()
      }
    }
  } finally {
    loading.value = false
  }
}

const loadTopicOptions = async () => {
  try {
    const res = await topicSelectionApi.getMySelections()
    const records = (res.data || []) as TopicSelectionVO[]
    const preferred = records.filter(item => item.selectionStatus === 1)
    const source = preferred.length > 0 ? preferred : records
    topicOptions.value = source.map((item) => ({
      label: item.topicTitle,
      value: item.topicId
    }))

    if (!submitForm.topicId && source.length > 0) {
      submitForm.topicId = source[0].topicId
      await handleTopicChange(submitForm.topicId)
    }
  } catch (error) {
    console.error('加载课题列表失败:', error)
  }
}

const handleTopicChange = async (topicId: string) => {
  if (!topicId) {
    selectedTopic.value = null
    return
  }

  try {
    const result = await topicApi.getTopicDetail(topicId)
    selectedTopic.value = result.data
    syncTopicSnapshot()
  } catch (error) {
    selectedTopic.value = null
    syncTopicSnapshot()
    console.error('获取课题详情失败:', error)
  }
}

const patchFormFromReport = (report: OpeningReportVO) => {
  submitForm.topicId = report.topicId || ''
  submitForm.arrangementId = report.arrangementId
  submitForm.studentName = report.studentName || ''
  submitForm.majorName = report.majorName || ''
  submitForm.className = report.className || ''
  submitForm.topicTitle = report.topicName || ''
  submitForm.advisorNames = report.advisorNames || ''
  submitForm.reportDate = report.reportDate || undefined
  submitForm.researchStatus = report.researchStatus || ''
  submitForm.purposeSignificance = report.purposeSignificance || ''
  submitForm.researchContent = report.researchContent || ''
  submitForm.innovationPoints = report.innovationPoints || ''
  submitForm.problemsToSolve = report.problemsToSolve || ''
  submitForm.progressExpectation = report.progressExpectation || ''
  submitForm.currentConditions = report.currentConditions || ''
  submitForm.advisorOpinion = report.advisorOpinion || ''
  submitForm.collegeOpinion = report.collegeOpinion || ''
  submitForm.status = report.status

  fillReadonlyFieldsFromCurrentUser()
}

const ensureFinalizeValid = () => {
  if ((submitForm.researchStatus || '').trim().length < 150) {
    message.error('研究现状不少于150字')
    return false
  }
  if ((submitForm.purposeSignificance || '').trim().length < 150) {
    message.error('研究目的与意义不少于150字')
    return false
  }
  if ((submitForm.researchContent || '').trim().length < 150) {
    message.error('研究内容不少于150字')
    return false
  }
  return true
}

const handleSubmit = async (status: OpeningReportStatus) => {
  try {
    await formRef.value?.validate()
    if (status === OpeningReportStatus.FINALIZED && !ensureFinalizeValid()) {
      return
    }

    submitLoading.value = true

    const payload: SubmitReportDTO = {
      ...submitForm,
      status,
      topicTitle: submitForm.topicTitle || selectedTopic.value?.topicTitle || getTopicLabel(submitForm.topicId),
      reportDate: submitForm.reportDate || dayjs().format('YYYY-MM-DD'),
      studentName: submitForm.studentName || userStore.userInfo?.realName || '',
      majorName: submitForm.majorName || userStore.userInfo?.major || '',
      className: submitForm.className || userStore.userInfo?.department || '',
      advisorNames: submitForm.advisorNames || selectedTopic.value?.creatorName || ''
    }

    const res = await defenseApi.submitReport(payload)
    if (res.code === 200) {
      message.success(status === OpeningReportStatus.DRAFT ? '草稿保存成功' : '定稿提交成功')
      await fetchMyReport()
    }
  } catch (err) {
    // 验证失败
  } finally {
    submitLoading.value = false
  }
}

const handleSaveDraft = async () => {
  await handleSubmit(OpeningReportStatus.DRAFT)
}

const handleFinalize = async () => {
  await handleSubmit(OpeningReportStatus.FINALIZED)
}

const handlePrintDraft = () => {
  const win = window.open('', '_blank')
  if (!win) {
    message.error('无法打开打印窗口，请检查浏览器拦截设置')
    return
  }

  const topicTitle = submitForm.topicTitle || selectedTopic.value?.topicTitle || getTopicLabel(submitForm.topicId) || '-'
  const topicType = selectedTopic.value?.topicTypeDesc || '-'
  const topicSource = selectedTopic.value?.topicSourceDesc || '-'
  const guidanceDirection = selectedTopic.value?.guidanceDirection || '-'

  const html = `
    <html>
      <head>
        <title>开题报告填写稿</title>
        <style>
          body { font-family: '宋体', serif; margin: 20px; color: #000; }
          .title { font-size: 22px; font-weight: bold; text-align: center; padding: 12px 0 20px; }
          table { width: 100%; border-collapse: collapse; border: 2px solid #000; }
          td { border: 1px solid #000; padding: 10px 12px; vertical-align: top; line-height: 1.8; font-size: 14px; }
          .label { width: 110px; text-align: center; font-weight: 500; background: #fafafa; vertical-align: middle; }
          .text { white-space: pre-wrap; word-break: break-word; min-height: 56px; }
          .section-title { font-weight: 700; margin-bottom: 8px; }
        </style>
      </head>
      <body>
        <div class="title">毕业设计（论文）开题报告表</div>
        <table>
          <tr>
            <td class="label">题目</td>
            <td colspan="5">${escapeHtml(topicTitle)}</td>
          </tr>
          <tr>
            <td class="label">课题类型</td>
            <td>${escapeHtml(topicType)}</td>
            <td class="label">课题来源</td>
            <td colspan="3">${escapeHtml(topicSource)}</td>
          </tr>
          <tr>
            <td class="label">指导方向</td>
            <td colspan="5">${escapeHtml(guidanceDirection)}</td>
          </tr>
          <tr>
            <td class="label">开题报告内容</td>
            <td colspan="5">
              <div class="section-title">1、国内外研究现状</div>
              <div class="text">${escapeHtml(submitForm.researchStatus || '')}</div>
              <div class="section-title">2、研究目的、意义</div>
              <div class="text">${escapeHtml(submitForm.purposeSignificance || '')}</div>
              <div class="section-title">3、研究内容</div>
              <div class="text">${escapeHtml(submitForm.researchContent || '')}</div>
              <div class="section-title">4、课题研究创新点</div>
              <div class="text">${escapeHtml(submitForm.innovationPoints || '')}</div>
              <div class="section-title">5、拟解决问题</div>
              <div class="text">${escapeHtml(submitForm.problemsToSolve || '')}</div>
              <div class="section-title">6、进度及预期结果</div>
              <div class="text">${escapeHtml(submitForm.progressExpectation || '')}</div>
              <div class="section-title">7、完成题目的现有条件</div>
              <div class="text">${escapeHtml(submitForm.currentConditions || '')}</div>
            </td>
          </tr>
        </table>
      </body>
    </html>
  `

  win.document.write(html)
  win.document.close()
  win.focus()
  win.print()
  win.close()
}

const getTopicLabel = (topicId: string) => {
  return topicOptions.value.find(item => item.value === topicId)?.label || ''
}

const escapeHtml = (raw: string) => {
  if (!raw) return '-'
  return raw
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

onMounted(() => {
  fillReadonlyFieldsFromCurrentUser()
  fetchMyReport()
  loadTopicOptions()
})
</script>

<style scoped>
.my-report {
  --report-accent: #0f766e;
  --report-soft: #ecfeff;
  --report-border: #d9f0ef;
  padding: 18px;
  background: linear-gradient(180deg, #f5fbfb 0%, #f9f9f9 100%);
}

.report-shell {
  border-radius: 14px;
  overflow: hidden;
}

.action-bar :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #0f766e 0%, #0ea5a8 100%);
  border-color: #0f766e;
}

.report-form {
  padding-top: 4px;
}

.report-section {
  margin-bottom: 18px;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  padding: 16px;
  background: #fff;
}

.report-section--meta {
  border-color: var(--report-border);
  background: linear-gradient(180deg, var(--report-soft) 0%, #ffffff 70%);
}

.section-heading {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 14px;
  color: #1f2937;
  padding-left: 10px;
  border-left: 4px solid var(--report-accent);
}

.field-item {
  margin-bottom: 12px;
}

.field-label {
  font-size: 13px;
  color: #4b5563;
  margin-bottom: 6px;
  font-weight: 600;
}

.editor-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.editor-item--full {
  grid-column: 1;
}

:deep(.ant-input[disabled]),
:deep(.ant-picker-disabled) {
  color: #374151;
  background-color: #f8fafc;
  border-color: #e5e7eb;
}

@media (max-width: 992px) {
  .editor-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .my-report {
    padding: 12px;
  }

  .report-section {
    padding: 12px;
    border-radius: 10px;
  }
}

</style>
