<!--
  中期检查填写页面（企业教师）
  功能：为指导学生填写中期检查表、提交审查
  @author 系统架构师
  @version 1.0
  @since 2026-03-17
-->
<template>
  <div class="midterm-check-form">
    <a-card title="中期检查表填写" :bordered="false">
      <!-- 操作栏 -->
      <div class="action-bar">
        <a-input-search
          v-model:value="searchText"
          placeholder="搜索学生姓名"
          style="width: 250px"
          allow-clear
          @search="handleSearch"
        />
        <a-select
          v-model:value="filterStatus"
          placeholder="提交状态"
          style="width: 150px"
          allow-clear
          @change="handleFilterChange"
        >
          <a-select-option :value="0">草稿</a-select-option>
          <a-select-option :value="1">已提交</a-select-option>
        </a-select>
        <a-button @click="loadList">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </div>

      <!-- 列表 -->
      <a-table
        :columns="columns"
        :data-source="checkList"
        :loading="loading"
        :pagination="pagination"
        row-key="checkId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'student'">
            <div>
              <div style="font-weight: 500">{{ record.studentName }}</div>
              <div style="color: #8c8c8c; font-size: 12px">{{ record.studentNo }}</div>
            </div>
          </template>

          <template v-else-if="column.key === 'topic'">
            <a-tooltip :title="record.topicName">
              <span style="cursor: pointer">
                {{ record.topicName?.length > 20 ? record.topicName.substring(0, 20) + '...' : record.topicName }}
              </span>
            </a-tooltip>
          </template>

          <template v-else-if="column.key === 'submitStatus'">
            <a-tag :color="record.submitStatus === 1 ? 'processing' : 'default'">
              {{ record.submitStatusDesc }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'reviewStatus'">
            <a-tag v-if="record.submitStatus === 1"
                   :color="getReviewStatusColor(record.reviewStatus)">
              {{ record.reviewStatusDesc }}
            </a-tag>
            <span v-else style="color: #bfbfbf">--</span>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                {{ record.submitStatus === 0 ? '编辑' : '查看' }}
              </a-button>
              <a-button
                v-if="record.submitStatus === 0"
                type="link"
                size="small"
                @click="handleSubmit(record)"
              >
                提交
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <!-- 空状态 -->
      <a-empty v-if="!loading && checkList.length === 0" description="暂无中期检查数据">
        <template #image>
          <FileTextOutlined style="font-size: 64px; color: #d9d9d9" />
        </template>
      </a-empty>
    </a-card>

    <!-- 编辑抽屉 -->
    <a-drawer
      v-model:open="drawerVisible"
      :title="currentRecord?.submitStatus === 0 ? '编辑中期检查表' : '查看中期检查表'"
      placement="right"
      :width="700"
      :maskClosable="false"
    >
      <a-spin :spinning="detailLoading">
        <a-form
          v-if="formData"
          :model="formData"
          :label-col="{ span: 4 }"
          :wrapper-col="{ span: 20 }"
          :disabled="currentRecord?.submitStatus === 1"
        >
          <a-form-item label="学生">
            <span>{{ detail?.studentName }} ({{ detail?.studentNo }})</span>
          </a-form-item>
          <a-form-item label="课题">
            <span>{{ detail?.topicName }}</span>
          </a-form-item>
          <a-form-item label="完成情况" required>
            <a-textarea
              v-model:value="formData.completionStatus"
              placeholder="请描述学生的毕业设计完成情况"
              :rows="4"
              :maxlength="1000"
              show-count
            />
          </a-form-item>
          <a-form-item label="存在问题">
            <a-textarea
              v-model:value="formData.existingProblems"
              placeholder="请描述当前存在的问题（选填）"
              :rows="4"
              :maxlength="1000"
              show-count
            />
          </a-form-item>
          <a-form-item label="下一步计划">
            <a-textarea
              v-model:value="formData.nextPlan"
              placeholder="请描述下一步工作计划（选填）"
              :rows="4"
              :maxlength="1000"
              show-count
            />
          </a-form-item>

          <!-- 审查结果（已提交时显示） -->
          <template v-if="currentRecord?.submitStatus === 1">
            <a-divider>审查信息</a-divider>
            <a-form-item label="审查状态">
              <a-tag :color="getReviewStatusColor(detail?.reviewStatus || 0)">
                {{ detail?.reviewStatusDesc }}
              </a-tag>
            </a-form-item>
            <a-form-item v-if="detail?.reviewerName" label="审查人">
              <span>{{ detail?.reviewerName }}</span>
            </a-form-item>
            <a-form-item v-if="detail?.reviewTime" label="审查时间">
              <span>{{ detail?.reviewTime }}</span>
            </a-form-item>
            <a-form-item v-if="detail?.reviewComment" label="审查意见">
              <a-typography-paragraph>{{ detail?.reviewComment }}</a-typography-paragraph>
            </a-form-item>
          </template>
        </a-form>
      </a-spin>

      <template #footer>
        <div style="text-align: right">
          <a-space>
            <a-button @click="drawerVisible = false">取消</a-button>
            <a-button
              v-if="currentRecord?.submitStatus === 0"
              type="primary"
              :loading="saving"
              @click="handleSave"
            >
              保存草稿
            </a-button>
          </a-space>
        </div>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { ReloadOutlined, FileTextOutlined } from '@ant-design/icons-vue'
import { midtermApi } from '@/api/midterm'
import type { MidtermCheckListVO, MidtermCheckVO, CreateMidtermCheckDTO } from '@/types/midterm'
import { getReviewStatusColor } from '@/types/midterm'

// 列表数据
const checkList = ref<MidtermCheckListVO[]>([])
const loading = ref(false)
const searchText = ref('')
const filterStatus = ref<number | undefined>(undefined)

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 抽屉
const drawerVisible = ref(false)
const currentRecord = ref<MidtermCheckListVO | null>(null)
const detail = ref<MidtermCheckVO | null>(null)
const detailLoading = ref(false)
const saving = ref(false)
const formData = ref<CreateMidtermCheckDTO | null>(null)

// 表格列定义
const columns = [
  { title: '学生', key: 'student', width: 150 },
  { title: '课题', key: 'topic', ellipsis: true },
  { title: '提交状态', key: 'submitStatus', width: 100 },
  { title: '审查状态', key: 'reviewStatus', width: 100 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
  { title: '操作', key: 'action', width: 140, fixed: 'right' }
]

// 加载列表
const loadList = async () => {
  loading.value = true
  try {
    const res = await midtermApi.getEnterpriseList({
      studentName: searchText.value || undefined,
      submitStatus: filterStatus.value,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    })
    const data = res.data
    if (data) {
      checkList.value = data.records || []
      pagination.total = data.total || 0
    }
  } catch (error: any) {
    message.error(error.message || '加载列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadList()
}

// 筛选变化
const handleFilterChange = () => {
  pagination.current = 1
  loadList()
}

// 表格变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadList()
}

// 编辑/查看
const handleEdit = async (record: MidtermCheckListVO) => {
  currentRecord.value = record
  drawerVisible.value = true
  detailLoading.value = true
  try {
    const res = await midtermApi.getEnterpriseDetail(record.checkId)
    detail.value = res.data || null
    if (detail.value) {
      formData.value = {
        checkId: detail.value.checkId,
        studentId: detail.value.studentId,
        topicId: detail.value.topicId,
        completionStatus: detail.value.completionStatus || '',
        existingProblems: detail.value.existingProblems || '',
        nextPlan: detail.value.nextPlan || '',
        documentId: detail.value.documentId
      }
    }
  } catch (error: any) {
    message.error(error.message || '加载详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 保存草稿
const handleSave = async () => {
  if (!formData.value) return

  if (!formData.value.completionStatus?.trim()) {
    message.warning('请填写完成情况')
    return
  }

  saving.value = true
  try {
    await midtermApi.saveCheck(formData.value)
    message.success('保存成功')
    drawerVisible.value = false
    loadList()
  } catch (error: any) {
    message.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 提交
const handleSubmit = (record: MidtermCheckListVO) => {
  Modal.confirm({
    title: '确认提交',
    content: `确定提交 ${record.studentName} 的中期检查表吗？提交后将无法修改。`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await midtermApi.submitCheck(record.checkId)
        message.success('提交成功')
        loadList()
      } catch (error: any) {
        message.error(error.message || '提交失败')
      }
    }
  })
}

// 初始化
onMounted(() => {
  loadList()
})
</script>

<style scoped>
.midterm-check-form {
  padding: 24px;
}

.action-bar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
