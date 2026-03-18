<!--
  教师指导列表页面（企业教师/高校教师）
  功能：查看我的学生列表、添加指导记录、查看学生指导详情
  @author 系统架构师
  @version 1.0
  @since 2026-03-16
-->
<template>
  <div class="teacher-guidance-list">
    <a-card title="学生指导" :bordered="false">
      <!-- 操作栏 -->
      <div class="action-bar">
        <a-button type="primary" @click="handleAdd" :disabled="studentList.length === 0">
          <template #icon><PlusOutlined /></template>
          新增指导记录
        </a-button>
        <a-button @click="loadStudentList">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </div>

      <!-- 学生列表 -->
      <a-table
        :columns="columns"
        :data-source="studentList"
        :loading="loading"
        :pagination="false"
        row-key="studentId"
      >
        <!-- 学生信息 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'student'">
            <div>
              <div style="font-weight: 500">{{ record.studentName }}</div>
              <div style="color: #8c8c8c; font-size: 12px">{{ record.studentNo }}</div>
            </div>
          </template>

          <template v-else-if="column.key === 'contact'">
            <div style="font-size: 12px">
              <div v-if="record.studentPhone">{{ record.studentPhone }}</div>
              <div v-if="record.studentEmail" style="color: #8c8c8c">{{ record.studentEmail }}</div>
            </div>
          </template>

          <template v-else-if="column.key === 'topic'">
            <a-tooltip :title="record.topicTitle">
              <span style="cursor: pointer">
                {{ record.topicTitle?.length > 20 ? record.topicTitle.substring(0, 20) + '...' : record.topicTitle }}
              </span>
            </a-tooltip>
          </template>

          <template v-else-if="column.key === 'stats'">
            <div>
              <a-tag color="blue">{{ record.recordCount }} 次指导</a-tag>
              <div v-if="record.totalHours" style="font-size: 12px; color: #8c8c8c; margin-top: 4px">
                累计 {{ record.totalHours }} 小时
              </div>
            </div>
          </template>

          <template v-else-if="column.key === 'lastDate'">
            <span v-if="record.lastGuidanceDate">{{ record.lastGuidanceDate }}</span>
            <span v-else style="color: #bfbfbf">暂无记录</span>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleViewRecords(record)">
                查看记录
              </a-button>
              <a-button type="link" size="small" @click="handleAddForStudent(record)">
                添加指导
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <!-- 空状态 -->
      <a-empty v-if="!loading && studentList.length === 0" description="暂无指导学生">
        <template #image>
          <TeamOutlined style="font-size: 64px; color: #d9d9d9" />
        </template>
      </a-empty>
    </a-card>

    <!-- 指导记录抽屉 -->
    <a-drawer
      v-model:open="recordDrawerVisible"
      :title="currentStudent?.studentName + ' 的指导记录'"
      placement="right"
      :width="700"
    >
      <a-spin :spinning="recordLoading">
        <a-timeline v-if="studentRecords.length > 0">
          <a-timeline-item
            v-for="record in studentRecords"
            :key="record.recordId"
            :color="record.guidanceType === 1 ? 'blue' : 'green'"
          >
            <template #dot>
              <SolutionOutlined v-if="record.guidanceType === 1" />
              <FileTextOutlined v-else />
            </template>
            <a-card size="small" :bordered="false" style="background: #fafafa">
              <div style="display: flex; justify-content: space-between; align-items: flex-start">
                <div>
                  <a-tag :color="record.guidanceType === 1 ? 'blue' : 'green'">
                    {{ record.guidanceTypeDesc }}
                  </a-tag>
                  <span style="margin-left: 8px; color: #8c8c8c">{{ record.guidanceDate }}</span>
                  <span v-if="record.guidanceMethod" style="margin-left: 8px">
                    <a-tag>{{ record.guidanceMethod }}</a-tag>
                  </span>
                  <span v-if="record.durationHours" style="margin-left: 8px; color: #8c8c8c">
                    {{ record.durationHours }}h
                  </span>
                </div>
                <a-popconfirm
                  title="确定删除该指导记录？"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="handleDeleteRecord(record.recordId)"
                >
                  <a-button type="link" danger size="small">删除</a-button>
                </a-popconfirm>
              </div>
              <div style="margin-top: 8px; color: #595959">
                {{ record.contentSummary }}
              </div>
              <div style="margin-top: 8px; font-size: 12px; color: #bfbfbf">
                指导教师：{{ record.teacherName }} | {{ record.createTime }}
              </div>
            </a-card>
          </a-timeline-item>
        </a-timeline>
        <a-empty v-else description="暂无指导记录" />
      </a-spin>
    </a-drawer>

    <!-- 新增指导记录弹窗 -->
    <GuidanceFormModal
      v-model="formModalVisible"
      :student-list="studentList"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  ReloadOutlined,
  TeamOutlined,
  SolutionOutlined,
  FileTextOutlined
} from '@ant-design/icons-vue'
import { guidanceApi } from '@/api/guidance'
import type { GuidanceStudentVO, GuidanceListVO } from '@/types/guidance'
import GuidanceFormModal from '@/components/guidance/GuidanceFormModal.vue'

// 学生列表
const studentList = ref<GuidanceStudentVO[]>([])
const loading = ref(false)

// 指导记录抽屉
const recordDrawerVisible = ref(false)
const currentStudent = ref<GuidanceStudentVO | null>(null)
const studentRecords = ref<GuidanceListVO[]>([])
const recordLoading = ref(false)

// 新增弹窗
const formModalVisible = ref(false)

// 表格列定义
const columns = [
  { title: '学生', key: 'student', width: 150 },
  { title: '联系方式', key: 'contact', width: 160 },
  { title: '课题', key: 'topic', ellipsis: true },
  { title: '指导统计', key: 'stats', width: 140 },
  { title: '最近指导', key: 'lastDate', width: 120 },
  { title: '操作', key: 'action', width: 160, fixed: 'right' }
]

// 加载学生列表
const loadStudentList = async () => {
  loading.value = true
  try {
    const res = await guidanceApi.getMyStudents()
    studentList.value = res.data || []
  } catch (error: any) {
    message.error(error.message || '加载学生列表失败')
    studentList.value = []
  } finally {
    loading.value = false
  }
}

// 查看学生指导记录
const handleViewRecords = async (student: GuidanceStudentVO) => {
  currentStudent.value = student
  recordDrawerVisible.value = true
  recordLoading.value = true
  try {
    const res = await guidanceApi.getStudentGuidanceRecords(student.studentId)
    studentRecords.value = res.data || []
  } catch (error: any) {
    message.error(error.message || '加载指导记录失败')
  } finally {
    recordLoading.value = false
  }
}

// 新增指导记录
const handleAdd = () => {
  formModalVisible.value = true
}

// 为特定学生添加指导
const handleAddForStudent = (student: GuidanceStudentVO) => {
  // 预选学生后打开弹窗
  formModalVisible.value = true
}

// 删除指导记录
const handleDeleteRecord = async (recordId: string) => {
  try {
    await guidanceApi.deleteGuidanceRecord(recordId)
    message.success('删除成功')
    // 刷新当前学生的记录
    if (currentStudent.value) {
      const res = await guidanceApi.getStudentGuidanceRecords(currentStudent.value.studentId)
      studentRecords.value = res.data || []
    }
    // 刷新学生列表统计
    loadStudentList()
  } catch (error: any) {
    message.error(error.message || '删除失败')
  }
}

// 表单提交成功
const handleFormSuccess = () => {
  loadStudentList()
  if (currentStudent.value && recordDrawerVisible.value) {
    handleViewRecords(currentStudent.value)
  }
}

// 初始化
onMounted(() => {
  loadStudentList()
})
</script>

<style scoped>
.teacher-guidance-list {
  padding: 24px;
}

.action-bar {
  margin-bottom: 16px;
  display: flex;
  gap: 8px;
}
</style>
