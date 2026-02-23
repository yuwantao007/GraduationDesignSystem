<!--
  课题任务书表单页面
  按照毕业设计（论文）任务书模板开发
  用于创建和编辑课题
  
  @author 系统架构师
  @version 1.0
  @since 2026-02-22
-->
<template>
  <div class="topic-form">
    <!-- 操作按钮区 -->
    <a-card class="topic-form__actions" :bordered="false">
      <a-space>
        <a-button @click="handleBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回
        </a-button>
        <a-button type="primary" @click="handleSave" :loading="saving">
          <template #icon><SaveOutlined /></template>
          保存
        </a-button>
        <a-button type="primary" @click="handleSubmit" :loading="submitting">
          <template #icon><SendOutlined /></template>
          保存并提交
        </a-button>
      </a-space>
    </a-card>

    <!-- 表单内容区 -->
    <a-card class="topic-form__content" :bordered="false">
      <!-- 文档标题 -->
      <div class="topic-form-title">毕业设计（论文）任务书</div>

      <!-- 表单主体 -->
      <a-form
        ref="formRef"
        :model="formData"
        :label-col="{ span: 24 }"
        class="topic-form-body"
      >
        <!-- 题目 -->
        <div class="topic-title-section">
          <div class="topic-title-label"><span class="required-mark">*</span> 题目：</div>
          <a-form-item name="topicTitle" :rules="[{ required: true, message: '请输入课题名称' }]">
            <a-input 
              v-model:value="formData.topicTitle" 
              placeholder="请输入课题名称" 
              :maxlength="50"
            />
          </a-form-item>
        </div>

        <!-- 基本信息表格 -->
        <table class="form-table" cellpadding="0" cellspacing="0">
          <tr>
            <td class="form-table-label" style="width: 100px">学生姓名</td>
            <td class="form-table-value">
              <a-input v-model:value="studentName" placeholder="请输入学生姓名" />
            </td>
            <td class="form-table-label" style="width: 100px">专业</td>
            <td class="form-table-value">
              <a-input v-model:value="formData.guidanceDirection" placeholder="请输入对应的专业" />
            </td>
            <td class="form-table-label" style="width: 100px">班级</td>
            <td class="form-table-value">
              <a-input v-model:value="classInfo" placeholder="请输入对应的班级" />
            </td>
          </tr>
          <tr>
            <td class="form-table-label"><span class="required-mark">*</span> 课题大类</td>
            <td class="form-table-value">
              <a-form-item 
                name="topicCategory" 
                :rules="[{ required: true, message: '请选择课题大类' }]"
                style="margin-bottom: 0"
              >
                <a-select 
                  v-model:value="formData.topicCategory" 
                  placeholder="请选择课题大类"
                  @change="handleCategoryChange"
                >
                  <a-select-option :value="1">高职升本</a-select-option>
                  <a-select-option :value="2">3+1</a-select-option>
                  <a-select-option :value="3">实验班</a-select-option>
                </a-select>
              </a-form-item>
            </td>
            <!-- 高职升本：显示归属企业 -->
            <template v-if="formData.topicCategory === 1">
              <td class="form-table-label"><span class="required-mark">*</span> 归属企业</td>
              <td class="form-table-value" colspan="3">
                <a-form-item 
                  name="enterpriseId" 
                  :rules="[{ required: true, message: '请选择归属企业' }]"
                  style="margin-bottom: 0"
                >
                  <a-select 
                    v-model:value="formData.enterpriseId" 
                    placeholder="请选择归属企业"
                    show-search
                    :filter-option="filterEnterpriseOption"
                  >
                    <a-select-option 
                      v-for="enterprise in enterpriseList" 
                      :key="enterprise.enterpriseId"
                      :value="enterprise.enterpriseId"
                    >
                      {{ enterprise.enterpriseName }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </td>
            </template>
            <!-- 3+1 或 实验班：显示关联学校下拉框 -->
            <template v-else-if="formData.topicCategory === 2 || formData.topicCategory === 3">
              <td class="form-table-label"><span class="required-mark">*</span> 关联学校</td>
              <td class="form-table-value" colspan="3">
                <a-form-item 
                  name="schoolId" 
                  :rules="[{ required: true, message: '请选择关联学校' }]"
                  style="margin-bottom: 0"
                >
                  <a-select
                    v-model:value="formData.schoolId"
                    placeholder="请选择关联学校"
                    show-search
                    :filter-option="filterSchoolOption"
                  >
                    <a-select-option 
                      v-for="school in schoolList" 
                      :key="school.schoolId"
                      :value="school.schoolId"
                    >
                      {{ school.schoolName }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </td>
            </template>
            <!-- 未选择课题大类时：显示提示 -->
            <template v-else>
              <td class="form-table-label">归属企业/适用学校</td>
              <td class="form-table-value" colspan="3">
                <span style="color: #999">请先选择课题大类</span>
              </td>
            </template>
          </tr>
          <tr>
            <td class="form-table-label"><span class="required-mark">*</span> 课题类型</td>
            <td class="form-table-value">
              <a-form-item 
                name="topicType" 
                :rules="[{ required: true, message: '请选择课题类型' }]"
                style="margin-bottom: 0"
              >
                <a-select v-model:value="formData.topicType" placeholder="请选择课题类型">
                  <a-select-option :value="1">设计</a-select-option>
                  <a-select-option :value="2">论文</a-select-option>
                </a-select>
              </a-form-item>
            </td>
            <td class="form-table-label"><span class="required-mark">*</span> 课题来源</td>
            <td class="form-table-value" colspan="3">
              <a-form-item 
                name="topicSource" 
                :rules="[{ required: true, message: '请选择课题来源' }]"
                style="margin-bottom: 0"
              >
                <a-select v-model:value="formData.topicSource" placeholder="请选择课题来源">
                  <a-select-option :value="1">校内</a-select-option>
                  <a-select-option :value="2">校外</a-select-option>
                </a-select>
              </a-form-item>
            </td>
          </tr>
        </table>

        <!-- 1. 选题背景与意义 -->
        <div class="form-section">
          <div class="form-section-header"><span class="required-mark">*</span> 1、选题背景与意义</div>
          <a-form-item 
            name="backgroundSignificance"
            :rules="[
              { required: true, message: '请输入选题背景与意义' },
              { min: 150, message: '内容不少于150字' }
            ]"
          >
            <a-textarea
              v-model:value="formData.backgroundSignificance"
              :rows="8"
              placeholder="内容不少于150字"
              :maxlength="2000"
              show-count
            />
          </a-form-item>
        </div>

        <!-- 2. 课题内容简述 -->
        <div class="form-section">
          <div class="form-section-header"><span class="required-mark">*</span> 2、课题内容简述</div>
          <a-form-item 
            name="contentSummary"
            :rules="[
              { required: true, message: '请输入课题内容简述' },
              { min: 150, message: '内容不少于150字' }
            ]"
          >
            <a-textarea
              v-model:value="formData.contentSummary"
              :rows="10"
              placeholder="内容不少于150字"
              :maxlength="2000"
              show-count
            />
          </a-form-item>
        </div>

        <!-- 3. 对专业知识的综合训练情况 -->
        <div class="form-section">
          <div class="form-section-header"><span class="required-mark">*</span> 3、对专业知识的综合训练情况</div>
          <a-form-item 
            name="professionalTraining"
            :rules="[
              { required: true, message: '请输入对专业知识的综合训练情况' },
              { min: 100, message: '内容不少于100字' }
            ]"
          >
            <a-textarea
              v-model:value="formData.professionalTraining"
              :rows="6"
              placeholder="内容不少于100字"
              :maxlength="2000"
              show-count
            />
          </a-form-item>
        </div>

        <!-- 4. 开发环境（工具） -->
        <div class="form-section">
          <div class="form-section-header">4、开发环境（工具）</div>
          <a-form-item name="developmentEnvironment">
            <a-textarea
              v-model:value="formData.developmentEnvironment"
              placeholder="请输入开发环境和工具"
              :rows="5"
              :maxlength="2000"
            />
          </a-form-item>
        </div>

        <!-- 5. 工作量（预计完成所需周数） -->
        <div class="form-section">
          <div class="form-section-header">5、工作量（预计完成所需周数）</div>
          <a-form-item name="workloadDetail">
            <a-textarea
              v-model:value="formData.workloadDetail"
              placeholder="请输入工作量安排"
              :rows="8"
              :maxlength="2000"
            />
          </a-form-item>
        </div>

        <!-- 任务与进度要求 -->
        <div class="form-section">
          <div class="form-section-header">任务与进度要求</div>
          <a-form-item name="scheduleRequirements">
            <a-textarea
              v-model:value="formData.scheduleRequirements"
              placeholder="请输入任务与进度要求"
              :rows="8"
              :maxlength="2000"
            />
          </a-form-item>
        </div>

        <!-- 主要参考文献 -->
        <div class="form-section">
          <div class="form-section-header">主要参考文献</div>
          <a-form-item name="topicReferences">
            <a-textarea
              v-model:value="formData.topicReferences"
              placeholder="请输入主要参考文献，每条文献单独一行"
              :rows="10"
              :maxlength="3000"
            />
          </a-form-item>
        </div>

        <!-- 起止日期和备注 -->
        <table class="form-table" cellpadding="0" cellspacing="0">
          <tr>
            <td class="form-table-label" style="width: 100px">起止日期</td>
            <td class="form-table-value">
              <a-range-picker
                v-model:value="dateRange"
                format="YYYY-MM-DD"
                style="width: 100%"
              />
            </td>
          </tr>
          <tr>
            <td class="form-table-label">备注</td>
            <td class="form-table-value">
              <a-textarea v-model:value="formData.remark" :rows="2" placeholder="请输入备注" />
            </td>
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
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  SaveOutlined,
  SendOutlined
} from '@ant-design/icons-vue'
import dayjs, { type Dayjs } from 'dayjs'
import { topicApi } from '@/api/topic'
import { enterpriseApi } from '@/api/enterprise'
import { schoolApi } from '@/api/school'
import { useUserStore } from '@/stores/user'
import type { CreateTopicDTO } from '@/types/topic'
import type { EnterpriseVO } from '@/types/enterprise'
import type { SchoolVO } from '@/types/school'

// 定义组件选项
defineOptions({
  name: 'TopicForm'
})

// 路由
const router = useRouter()
const route = useRoute()

// Store
const userStore = useUserStore()

// 表单引用
const formRef = ref<FormInstance>()

// 是否编辑模式
const isEditMode = computed(() => !!route.params.id)

// 表单数据
const formData = reactive({
  topicTitle: '',
  topicCategory: undefined as number | undefined,
  topicType: undefined as number | undefined,
  topicSource: undefined as number | undefined,
  applicableSchool: '',
  enterpriseId: '',
  schoolId: '',
  guidanceDirection: '',
  backgroundSignificance: '',
  contentSummary: '',
  professionalTraining: '',
  developmentEnvironment: '',
  workloadWeeks: 16,
  workloadDetail: '',
  scheduleRequirements: '',
  topicReferences: '',
  startDate: '',
  endDate: '',
  remark: ''
})

// 日期范围
const dateRange = ref<[Dayjs, Dayjs]>()

// 学生姓名和班级信息
const studentName = ref('')
const classInfo = ref('')

// 企业列表
const enterpriseList = ref<EnterpriseVO[]>([])

// 学校列表
const schoolList = ref<SchoolVO[]>([])

// 加载状态
const saving = ref(false)
const submitting = ref(false)

/**
 * 获取企业列表
 */
const getEnterpriseList = async () => {
  try {
    const result = await enterpriseApi.getAllEnterprises()
    enterpriseList.value = result.data || []
  } catch (error) {
    console.error('获取企业列表失败', error)
  }
}

/**
 * 获取学校列表
 */
const getSchoolList = async () => {
  try {
    const result = await schoolApi.getAllSchools()
    schoolList.value = result.data || []
  } catch (error) {
    console.error('获取学校列表失败', error)
  }
}

/**
 * 课题大类切换处理
 */
const handleCategoryChange = (value: number) => {
  // 切换课题大类时，清空归属企业、学校和适用学校
  formData.enterpriseId = ''
  formData.schoolId = ''
  formData.applicableSchool = ''
}

/**
 * 企业选择过滤
 */
const filterEnterpriseOption = (input: string, option: any) => {
  return option.children?.[0]?.children?.toLowerCase().includes(input.toLowerCase())
}

/**
 * 学校选择过滤
 */
const filterSchoolOption = (input: string, option: any) => {
  return option.children?.[0]?.children?.toLowerCase().includes(input.toLowerCase())
}

/** * 获取课题详情
 */
const getTopicDetail = async (topicId: string) => {
  try {
    const result = await topicApi.getTopicDetail(topicId)
    const topic = result.data
    
    // 解析开发环境（Map格式转字符串）
    let devEnvStr = ''
    if (topic.developmentEnvironment) {
      if (typeof topic.developmentEnvironment === 'string') {
        devEnvStr = topic.developmentEnvironment
      } else if (topic.developmentEnvironment.content) {
        devEnvStr = topic.developmentEnvironment.content
      }
    }
    
    // 解析工作量明细（List格式转字符串）
    let workloadStr = ''
    if (topic.workloadDetail) {
      if (typeof topic.workloadDetail === 'string') {
        workloadStr = topic.workloadDetail
      } else if (Array.isArray(topic.workloadDetail) && topic.workloadDetail[0]?.content) {
        workloadStr = topic.workloadDetail[0].content
      }
    }
    
    // 解析任务与进度要求（List格式转字符串）
    let scheduleStr = ''
    if (topic.scheduleRequirements) {
      if (typeof topic.scheduleRequirements === 'string') {
        scheduleStr = topic.scheduleRequirements
      } else if (Array.isArray(topic.scheduleRequirements) && topic.scheduleRequirements[0]?.content) {
        scheduleStr = topic.scheduleRequirements[0].content
      }
    }
    
    // 解析参考文献（List格式转字符串）
    let referencesStr = ''
    if (topic.topicReferences) {
      if (typeof topic.topicReferences === 'string') {
        referencesStr = topic.topicReferences
      } else if (Array.isArray(topic.topicReferences) && topic.topicReferences[0]?.content) {
        referencesStr = topic.topicReferences[0].content
      }
    }
    
    // 填充表单数据
    Object.assign(formData, {
      topicTitle: topic.topicTitle,
      topicCategory: topic.topicCategory,
      topicType: topic.topicType,
      topicSource: topic.topicSource,
      applicableSchool: topic.applicableSchool,
      enterpriseId: topic.enterpriseId,
      schoolId: topic.schoolId,
      guidanceDirection: topic.guidanceDirection,
      backgroundSignificance: topic.backgroundSignificance,
      contentSummary: topic.contentSummary,
      professionalTraining: topic.professionalTraining,
      developmentEnvironment: devEnvStr,
      workloadWeeks: topic.workloadWeeks,
      workloadDetail: workloadStr,
      scheduleRequirements: scheduleStr,
      topicReferences: referencesStr,
      remark: topic.remark
    })
    
    // 设置日期范围
    if (topic.startDate && topic.endDate) {
      dateRange.value = [
        dayjs(topic.startDate),
        dayjs(topic.endDate)
      ]
    }
  } catch (error) {
    console.error('获取课题详情失败', error)
    message.error('获取课题详情失败')
  }
}

/**
 * 保存课题
 */
const handleSave = async () => {
  try {
    // 表单验证
    await formRef.value?.validate()
    
    // 验证必填文本字段的字数
    if (formData.backgroundSignificance.length < 150) {
      message.warning('选题背景与意义不少于150字')
      return
    }
    if (formData.contentSummary.length < 150) {
      message.warning('课题内容简述不少于150字')
      return
    }
    if (formData.professionalTraining.length < 100) {
      message.warning('对专业知识的综合训练情况不少于100字')
      return
    }
    
    saving.value = true
    
    // 处理日期和数据转换
    // 根据课题大类决定使用企业ID还是学校ID
    let finalEnterpriseId = ''
    let finalSchoolId = ''
    let finalApplicableSchool = ''
    
    if (formData.topicCategory === 1) {
      // 高职升本：使用企业ID
      if (!formData.enterpriseId) {
        message.error('请选择归属企业')
        saving.value = false
        return
      }
      finalEnterpriseId = formData.enterpriseId
    } else if (formData.topicCategory === 2 || formData.topicCategory === 3) {
      // 3+1/实验班：使用学校ID
      if (!formData.schoolId) {
        message.error('请选择关联学校')
        saving.value = false
        return
      }
      finalSchoolId = formData.schoolId
      // 获取选中的学校名称
      const selectedSchool = schoolList.value.find(s => s.schoolId === formData.schoolId)
      finalApplicableSchool = selectedSchool?.schoolName || ''
    }
    
    const submitData = {
      topicTitle: formData.topicTitle,
      topicCategory: formData.topicCategory!,
      topicType: formData.topicType!,
      topicSource: formData.topicSource!,
      applicableSchool: finalApplicableSchool,
      enterpriseId: finalEnterpriseId || undefined,
      schoolId: finalSchoolId || undefined,
      guidanceDirection: formData.guidanceDirection,
      backgroundSignificance: formData.backgroundSignificance,
      contentSummary: formData.contentSummary,
      professionalTraining: formData.professionalTraining,
      developmentEnvironment: formData.developmentEnvironment ? { content: formData.developmentEnvironment } : {},
      workloadWeeks: formData.workloadWeeks,
      workloadDetail: formData.workloadDetail ? [{ content: formData.workloadDetail }] : [],
      scheduleRequirements: formData.scheduleRequirements ? [{ content: formData.scheduleRequirements }] : [],
      topicReferences: formData.topicReferences ? [{ content: formData.topicReferences }] : [],
      startDate: dateRange.value?.[0]?.format('YYYY-MM-DD'),
      endDate: dateRange.value?.[1]?.format('YYYY-MM-DD'),
      remark: formData.remark
    } as CreateTopicDTO
    
    if (isEditMode.value) {
      // 编辑模式
      await topicApi.updateTopic(route.params.id as string, submitData)
      message.success('保存成功')
    } else {
      // 新建模式
      await topicApi.createTopic(submitData)
      message.success('创建成功')
    }
    
    // 返回列表页
    router.push('/topic/list')
  } catch (error: any) {
    if (error.errorFields) {
      message.warning('请填写必填项')
    } else {
      console.error('保存课题失败', error)
      message.error('保存课题失败')
    }
  } finally {
    saving.value = false
  }
}

/**
 * 保存并提交
 */
const handleSubmit = async () => {
  try {
    // 表单验证
    await formRef.value?.validate()
    
    // 验证必填文本字段的字数
    if (formData.backgroundSignificance.length < 150) {
      message.warning('选题背景与意义不少于150字')
      return
    }
    if (formData.contentSummary.length < 150) {
      message.warning('课题内容简述不少于150字')
      return
    }
    if (formData.professionalTraining.length < 100) {
      message.warning('对专业知识的综合训练情况不少于100字')
      return
    }
    
    submitting.value = true
    
    // 处理日期和数据转换
    // 根据课题大类决定使用企业ID还是学校ID
    let finalEnterpriseId = ''
    let finalSchoolId = ''
    let finalApplicableSchool = ''
    
    if (formData.topicCategory === 1) {
      // 高职升本：使用企业ID
      if (!formData.enterpriseId) {
        message.error('请选择归属企业')
        submitting.value = false
        return
      }
      finalEnterpriseId = formData.enterpriseId
    } else if (formData.topicCategory === 2 || formData.topicCategory === 3) {
      // 3+1/实验班：使用学校ID
      if (!formData.schoolId) {
        message.error('请选择关联学校')
        submitting.value = false
        return
      }
      finalSchoolId = formData.schoolId
      // 获取选中的学校名称
      const selectedSchool = schoolList.value.find(s => s.schoolId === formData.schoolId)
      finalApplicableSchool = selectedSchool?.schoolName || ''
    }
    
    const submitData = {
      topicTitle: formData.topicTitle,
      topicCategory: formData.topicCategory!,
      topicType: formData.topicType!,
      topicSource: formData.topicSource!,
      applicableSchool: finalApplicableSchool,
      enterpriseId: finalEnterpriseId || undefined,
      schoolId: finalSchoolId || undefined,
      guidanceDirection: formData.guidanceDirection,
      backgroundSignificance: formData.backgroundSignificance,
      contentSummary: formData.contentSummary,
      professionalTraining: formData.professionalTraining,
      developmentEnvironment: formData.developmentEnvironment ? { content: formData.developmentEnvironment } : {},
      workloadWeeks: formData.workloadWeeks,
      workloadDetail: formData.workloadDetail ? [{ content: formData.workloadDetail }] : [],
      scheduleRequirements: formData.scheduleRequirements ? [{ content: formData.scheduleRequirements }] : [],
      topicReferences: formData.topicReferences ? [{ content: formData.topicReferences }] : [],
      startDate: dateRange.value?.[0]?.format('YYYY-MM-DD'),
      endDate: dateRange.value?.[1]?.format('YYYY-MM-DD'),
      remark: formData.remark
    } as CreateTopicDTO
    
    let topicId: string
    
    if (isEditMode.value) {
      // 编辑模式
      await topicApi.updateTopic(route.params.id as string, submitData)
      topicId = route.params.id as string
    } else {
      // 新建模式
      const result = await topicApi.createTopic(submitData)
      topicId = result.data.topicId
    }
    
    // 提交课题
    await topicApi.submitTopic({ topicId })
    
    message.success('提交成功')
    
    // 返回列表页
    router.push('/topic/list')
  } catch (error: any) {
    if (error.errorFields) {
      message.warning('请填写必填项')
    } else {
      console.error('提交课题失败', error)
      message.error('提交课题失败')
    }
  } finally {
    submitting.value = false
  }
}

/**
 * 返回
 */
const handleBack = () => {
  router.back()
}

// 页面加载
onMounted(() => {
  // 加载企业列表
  getEnterpriseList()
  // 加载学校列表
  getSchoolList()
  
  // 如果是编辑模式，加载课题详情
  if (isEditMode.value) {
    getTopicDetail(route.params.id as string)
  }
})
</script>

<style scoped lang="scss">
.topic-form {
  padding: 20px;

  &__actions {
    margin-bottom: 16px;
  }

  &__content {
    max-width: 1200px;
    margin: 0 auto;
  }
}

// 必填项星号样式
.required-mark {
  color: #ff4d4f;
  margin-right: 4px;
}

// 文档标题样式
.topic-form-title {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  padding: 20px 0;
  color: #000;
}

// 题目区域样式
.topic-title-section {
  margin-bottom: 20px;
  
  .topic-title-label {
    font-weight: bold;
    color: #000;
    margin-bottom: 8px;
  }
}

// 表格样式
.form-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #d9d9d9;
  margin-bottom: 20px;

  td {
    border: 1px solid #d9d9d9;
    padding: 8px 12px;
  }

  &-label {
    background-color: #fafafa;
    font-weight: 500;
    text-align: center;
    color: #000;
  }

  &-value {
    :deep(.ant-input),
    :deep(.ant-select),
    :deep(.ant-picker),
    :deep(.ant-textarea) {
      border: none;
      box-shadow: none;

      &:focus {
        border: 1px solid #1890ff;
      }
    }
  }
}

// 章节样式
.form-section {
  margin-bottom: 20px;

  &-header {
    font-weight: bold;
    padding: 10px 0;
    color: #000;
    font-size: 14px;
  }
}

// 签名栏样式
.signature-section {
  display: flex;
  justify-content: space-around;
  padding: 40px 0 20px;
  margin-top: 30px;
  
  .signature-item {
    display: flex;
    align-items: center;
    
    .signature-label {
      color: #000;
      margin-right: 10px;
      white-space: nowrap;
    }
    
    .signature-line {
      display: inline-block;
      width: 120px;
      border-bottom: 1px solid #000;
      height: 20px;
    }
  }
}

// 表单项标签加粗
:deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #000;
}

// textarea placeholder颜色
:deep(.ant-input::placeholder),
:deep(.ant-input-textarea textarea::placeholder) {
  color: #bfbfbf;
}
</style>
