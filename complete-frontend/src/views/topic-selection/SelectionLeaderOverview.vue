<!--
  双选审核概览页面（企业负责人）
  功能：
  1. 顶部统计：总课题数、已全选课题、未选报学生数
  2. Tab 切换：双选概览 / 教师指派
  3. 双选概览 Tab：课题维度汇总（选报/确认/待确认/落选人数），支持导出
  4. 教师指派 Tab：校外协同课题中选学生指派教师、查看指派记录

  @author 系统架构师
  @version 1.0
  @since 2026-03-14
-->
<template>
  <div class="leader-overview">

    <!-- 顶部统计卡片 -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic title="总课题数" :value="overviewList.length" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="有学生确认的课题"
            :value="confirmedTopicCount"
            :value-style="{ color: '#52c41a' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="有待确认学生的课题"
            :value="pendingTopicCount"
            :value-style="{ color: '#1890ff' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="未选报任何课题的学生"
            :value="unselectedStudents.length"
            :value-style="unselectedStudents.length > 0 ? { color: '#ff4d4f' } : {}"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 主内容 Tab -->
    <a-card :bordered="false">
      <a-tabs v-model:activeKey="activeTab">

        <!-- ========== 双选概览 Tab ========== -->
        <a-tab-pane key="overview" tab="双选概览">
          <div class="leader-overview__toolbar">
            <span class="leader-overview__tip">
              <InfoCircleOutlined style="margin-right: 4px; color: #1890ff" />
              以下为本企业所有终审通过课题的双选结果统计
            </span>
            <a-button @click="handleExportSelection">
              <template #icon><DownloadOutlined /></template>
              导出选题信息
            </a-button>
          </div>

          <a-table
            :columns="overviewColumns"
            :data-source="overviewList"
            :loading="overviewLoading"
            row-key="topicId"
            :pagination="{ pageSize: 10, showTotal: (t: number) => `共 ${t} 条` }"
          >
            <template #bodyCell="{ column, record }">
              <!-- 课题大类 -->
              <template v-if="column.dataIndex === 'topicCategoryDesc'">
                <a-tag :color="getCategoryColor(record.topicCategory)">
                  {{ record.topicCategoryDesc }}
                </a-tag>
              </template>

              <!-- 课题来源 -->
              <template v-else-if="column.dataIndex === 'topicSourceDesc'">
                <a-tag :color="record.topicSource === 2 ? 'orange' : 'blue'">
                  {{ record.topicSourceDesc }}
                </a-tag>
              </template>

              <!-- 选报总数 -->
              <template v-else-if="column.dataIndex === 'totalApplicants'">
                <a-space>
                  <a-tag color="blue">{{ record.totalApplicants }} 人</a-tag>
                </a-space>
              </template>

              <!-- 已确认 -->
              <template v-else-if="column.dataIndex === 'confirmedCount'">
                <a-tag color="green">{{ record.confirmedCount }}</a-tag>
              </template>

              <!-- 待确认 -->
              <template v-else-if="column.dataIndex === 'pendingCount'">
                <a-tag :color="record.pendingCount > 0 ? 'processing' : 'default'">
                  {{ record.pendingCount }}
                </a-tag>
              </template>

              <!-- 落选 -->
              <template v-else-if="column.dataIndex === 'rejectedCount'">
                <a-tag color="default">{{ record.rejectedCount }}</a-tag>
              </template>
            </template>
          </a-table>

          <!-- 未选报学生提示 -->
          <a-divider />
          <div class="leader-overview__unselected">
            <div class="leader-overview__unselected-title">
              <WarningOutlined v-if="unselectedStudents.length > 0" style="color: #fa8c16; margin-right: 6px" />
              未选报任何课题的学生
              <a-badge :count="unselectedStudents.length" :offset="[6, 0]" />
            </div>

            <a-table
              v-if="unselectedStudents.length > 0"
              :columns="unselectedColumns"
              :data-source="unselectedStudents"
              :loading="unselectedLoading"
              row-key="userId"
              size="small"
              :pagination="false"
              style="margin-top: 12px"
            />
            <a-empty v-else description="所有学生均已选报课题" style="margin: 16px 0" />
          </div>
        </a-tab-pane>

        <!-- ========== 教师指派 Tab ========== -->
        <a-tab-pane key="assign" tab="教师指派">

          <!-- 指派记录列表 -->
          <div class="leader-overview__section-title">
            <span>指派记录</span>
            <a-button type="primary" @click="showAssignModal = true">
              <template #icon><PlusOutlined /></template>
              新增指派
            </a-button>
          </div>

          <a-table
            :columns="assignmentColumns"
            :data-source="assignmentList"
            :loading="assignmentLoading"
            row-key="assignmentId"
            :pagination="{ pageSize: 10, showTotal: (t: number) => `共 ${t} 条` }"
          >
            <template #bodyCell="{ column, record }">
              <!-- 课题来源 -->
              <template v-if="column.dataIndex === 'topicSourceDesc'">
                <a-tag :color="record.topicSource === 2 ? 'orange' : 'blue'">
                  {{ record.topicSourceDesc }}
                </a-tag>
              </template>

              <!-- 指派状态 -->
              <template v-else-if="column.dataIndex === 'assignStatus'">
                <a-tag :color="AssignStatusColorMap[record.assignStatus]">
                  {{ record.assignStatusDesc }}
                </a-tag>
              </template>

              <!-- 操作列 -->
              <template v-else-if="column.key === 'action'">
                <a-popconfirm
                  v-if="record.assignStatus === 1"
                  title="确认取消该指派？"
                  ok-text="确认"
                  cancel-text="取消"
                  :ok-button-props="{ danger: true }"
                  @confirm="handleCancelAssign(record.assignmentId)"
                >
                  <a-button danger size="small">取消指派</a-button>
                </a-popconfirm>
                <span v-else class="leader-overview__no-action">—</span>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

      </a-tabs>
    </a-card>

    <!-- ========== 指派教师弹窗 ========== -->
    <a-modal
      v-model:open="showAssignModal"
      title="指派企业指导教师"
      :confirm-loading="assignSubmitting"
      ok-text="确认指派"
      cancel-text="取消"
      @ok="handleAssignSubmit"
      @cancel="resetAssignForm"
    >
      <a-form
        ref="assignFormRef"
        :model="assignForm"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item
          label="课题名称"
          name="topicId"
          :rules="[{ required: true, message: '请输入选报记录ID' }]"
        >
          <a-input
            v-model:value="assignForm.selectionId"
            placeholder="请输入中选学生的选报记录ID"
            allow-clear
          />
          <div style="font-size: 12px; color: #888; margin-top: 4px">
            可从双选概览中复制对应学生的选报ID（仅校外协同开发课题）
          </div>
        </a-form-item>

        <a-form-item
          label="指派教师"
          name="assignedTeacherId"
          :rules="[{ required: true, message: '请输入企业教师用户ID' }]"
        >
          <a-input
            v-model:value="assignForm.assignedTeacherId"
            placeholder="请输入企业教师用户ID"
            allow-clear
          />
        </a-form-item>
      </a-form>
    </a-modal>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  DownloadOutlined,
  InfoCircleOutlined,
  WarningOutlined,
  PlusOutlined
} from '@ant-design/icons-vue'
import { topicSelectionApi } from '@/api/topicSelection'
import { AssignStatusColorMap } from '@/types/topicSelection'
import type {
  SelectionOverviewVO,
  TeacherAssignmentVO,
  UnselectedStudentVO
} from '@/types/topicSelection'
import type { TableColumnsType, FormInstance } from 'ant-design-vue'

// ==================== 状态 ====================

const activeTab = ref<string>('overview')

/** 双选概览数据 */
const overviewList = ref<SelectionOverviewVO[]>([])
const overviewLoading = ref(false)

/** 未选报学生 */
const unselectedStudents = ref<UnselectedStudentVO[]>([])
const unselectedLoading = ref(false)

/** 指派记录 */
const assignmentList = ref<TeacherAssignmentVO[]>([])
const assignmentLoading = ref(false)

/** 指派弹窗 */
const showAssignModal = ref(false)
const assignSubmitting = ref(false)
const assignFormRef = ref<FormInstance>()

/** 指派表单（简化：直接输入 selectionId 和 teacherId） */
const assignForm = reactive({
  selectionId: '',
  assignedTeacherId: ''
})

// ==================== 计算属性 ====================

/** 有确认学生的课题数 */
const confirmedTopicCount = computed(() =>
  overviewList.value.filter(t => t.confirmedCount > 0).length
)

/** 有待确认学生的课题数 */
const pendingTopicCount = computed(() =>
  overviewList.value.filter(t => t.pendingCount > 0).length
)

// ==================== 表格列定义 ====================

const overviewColumns: TableColumnsType = [
  { title: '课题名称', dataIndex: 'topicTitle', ellipsis: true },
  { title: '课题大类', dataIndex: 'topicCategoryDesc', width: 100 },
  { title: '课题来源', dataIndex: 'topicSourceDesc', width: 110 },
  { title: '企业教师', dataIndex: 'creatorName', width: 100 },
  { title: '指导方向', dataIndex: 'guidanceDirection', width: 120, ellipsis: true },
  { title: '选报总数', dataIndex: 'totalApplicants', width: 90, align: 'center' },
  { title: '已确认', dataIndex: 'confirmedCount', width: 80, align: 'center' },
  { title: '待确认', dataIndex: 'pendingCount', width: 80, align: 'center' },
  { title: '落选', dataIndex: 'rejectedCount', width: 70, align: 'center' }
]

const unselectedColumns: TableColumnsType = [
  { title: '学生姓名', dataIndex: 'realName', width: 100 },
  { title: '学号', dataIndex: 'studentNo', width: 120 },
  { title: '手机号', dataIndex: 'userPhone', width: 130 },
  { title: '邮箱', dataIndex: 'userEmail', ellipsis: true },
  { title: '所属院系', dataIndex: 'department', width: 140 }
]

const assignmentColumns: TableColumnsType = [
  { title: '学生姓名', dataIndex: 'studentName', width: 100 },
  { title: '学号', dataIndex: 'studentNo', width: 120 },
  { title: '课题名称', dataIndex: 'topicTitle', ellipsis: true },
  { title: '课题来源', dataIndex: 'topicSourceDesc', width: 110 },
  { title: '指派教师', dataIndex: 'assignedTeacherName', width: 100 },
  { title: '指派人', dataIndex: 'assignedByName', width: 100 },
  { title: '指派时间', dataIndex: 'assignTime', width: 160 },
  { title: '状态', dataIndex: 'assignStatus', width: 90 },
  { title: '操作', key: 'action', fixed: 'right', width: 100 }
]

// ==================== 数据加载 ====================

/**
 * 加载双选概览
 */
const loadOverview = async () => {
  overviewLoading.value = true
  try {
    const res = await topicSelectionApi.getSelectionOverview()
    overviewList.value = res.data || []
  } catch (error) {
    console.error('加载双选概览失败', error)
    message.error('加载双选概览失败')
  } finally {
    overviewLoading.value = false
  }
}

/**
 * 加载未选报学生
 */
const loadUnselected = async () => {
  unselectedLoading.value = true
  try {
    const res = await topicSelectionApi.getUnselectedStudents()
    unselectedStudents.value = res.data || []
  } catch (error) {
    console.error('加载未选报学生失败', error)
  } finally {
    unselectedLoading.value = false
  }
}

/**
 * 加载指派记录
 */
const loadAssignments = async () => {
  assignmentLoading.value = true
  try {
    const res = await topicSelectionApi.getAssignmentList()
    assignmentList.value = res.data || []
  } catch (error) {
    console.error('加载指派记录失败', error)
    message.error('加载指派记录失败')
  } finally {
    assignmentLoading.value = false
  }
}

// ==================== 事件处理 ====================

/**
 * 导出选题信息 Excel
 */
const handleExportSelection = () => {
  const url = topicSelectionApi.getExportSelectionUrl()
  const link = document.createElement('a')
  link.href = url
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

/**
 * 取消指派
 */
const handleCancelAssign = async (assignmentId: string) => {
  try {
    await topicSelectionApi.cancelAssignment(assignmentId)
    message.success('已取消指派')
    await loadAssignments()
  } catch (error) {
    console.error('取消指派失败', error)
  }
}

/**
 * 提交指派
 */
const handleAssignSubmit = async () => {
  try {
    await assignFormRef.value?.validate()
  } catch {
    return
  }

  assignSubmitting.value = true
  try {
    // 注意：简化版本直接输入 selectionId，后端会验证
    // 实际上 studentId 和 topicId 需要从 selectionId 关联获取，此处通过后端校验
    await topicSelectionApi.assignTeacher({
      studentId: '',
      topicId: '',
      selectionId: assignForm.selectionId,
      assignedTeacherId: assignForm.assignedTeacherId
    })
    message.success('指派成功')
    showAssignModal.value = false
    resetAssignForm()
    await loadAssignments()
  } catch (error) {
    console.error('指派失败', error)
  } finally {
    assignSubmitting.value = false
  }
}

/**
 * 重置指派表单
 */
const resetAssignForm = () => {
  assignForm.selectionId = ''
  assignForm.assignedTeacherId = ''
  assignFormRef.value?.resetFields()
}

// ==================== 工具方法 ====================

/**
 * 课题大类颜色映射
 */
const getCategoryColor = (category: number): string => {
  const colorMap: Record<number, string> = { 1: 'blue', 2: 'green', 3: 'purple' }
  return colorMap[category] || 'default'
}

// ==================== 生命周期 ====================

onMounted(async () => {
  await Promise.all([loadOverview(), loadUnselected(), loadAssignments()])
})
</script>

<style scoped lang="scss">
.leader-overview {
  padding: 20px;

  &__toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  &__tip {
    color: #595959;
    font-size: 13px;
  }

  &__section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
    font-size: 14px;
    margin-bottom: 12px;
  }

  &__unselected {
    padding: 0 4px;

    &-title {
      font-weight: 600;
      font-size: 14px;
      color: #262626;
    }
  }

  &__no-action {
    color: #bfbfbf;
  }
}
</style>
