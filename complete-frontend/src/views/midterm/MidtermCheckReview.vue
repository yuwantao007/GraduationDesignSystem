<!--
  中期检查审查页面（高校教师）
  功能：审查学生的中期检查表
  @author 系统架构师
  @version 1.0
  @since 2026-03-17
-->
<template>
  <div class="midterm-check-review">
    <a-card title="中期检查审查" :bordered="false">
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
          placeholder="审查状态"
          style="width: 150px"
          allow-clear
          @change="handleFilterChange"
        >
          <a-select-option :value="0">未审查</a-select-option>
          <a-select-option :value="1">合格</a-select-option>
          <a-select-option :value="2">不合格</a-select-option>
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

          <template v-else-if="column.key === 'enterpriseTeacher'">
            <span>{{ record.enterpriseTeacherName }}</span>
          </template>

          <template v-else-if="column.key === 'reviewStatus'">
            <a-tag :color="getReviewStatusColor(record.reviewStatus)">
              {{ record.reviewStatusDesc }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">
                {{ record.reviewStatus === 0 ? '审查' : '查看' }}
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>

      <!-- 空状态 -->
      <a-empty v-if="!loading && checkList.length === 0" description="暂无待审查数据">
        <template #image>
          <FileTextOutlined style="font-size: 64px; color: #d9d9d9" />
        </template>
      </a-empty>
    </a-card>

    <!-- 审查抽屉 -->
    <a-drawer
      v-model:open="drawerVisible"
      :title="currentRecord?.reviewStatus === 0 ? '审查中期检查表' : '查看中期检查表'"
      placement="right"
      :width="700"
      :maskClosable="false"
    >
      <a-spin :spinning="detailLoading">
        <a-descriptions v-if="detail" :column="1" bordered>
          <a-descriptions-item label="学生">
            {{ detail.studentName }} ({{ detail.studentNo }})
          </a-descriptions-item>
          <a-descriptions-item label="课题">
            {{ detail.topicName }}
          </a-descriptions-item>
          <a-descriptions-item label="企业教师">
            {{ detail.enterpriseTeacherName }}
          </a-descriptions-item>
          <a-descriptions-item label="完成情况">
            <a-typography-paragraph
              v-if="detail.completionStatus"
              :ellipsis="{ rows: 4, expandable: true }"
            >
              {{ detail.completionStatus }}
            </a-typography-paragraph>
            <span v-else style="color: #bfbfbf">未填写</span>
          </a-descriptions-item>
          <a-descriptions-item label="存在问题">
            <a-typography-paragraph
              v-if="detail.existingProblems"
              :ellipsis="{ rows: 4, expandable: true }"
            >
              {{ detail.existingProblems }}
            </a-typography-paragraph>
            <span v-else style="color: #bfbfbf">未填写</span>
          </a-descriptions-item>
          <a-descriptions-item label="下一步计划">
            <a-typography-paragraph
              v-if="detail.nextPlan"
              :ellipsis="{ rows: 4, expandable: true }"
            >
              {{ detail.nextPlan }}
            </a-typography-paragraph>
            <span v-else style="color: #bfbfbf">未填写</span>
          </a-descriptions-item>
          <a-descriptions-item label="提交时间">
            {{ detail.updateTime }}
          </a-descriptions-item>
        </a-descriptions>

        <!-- 审查表单（未审查时显示） -->
        <template v-if="currentRecord?.reviewStatus === 0">
          <a-divider>审查意见</a-divider>
          <a-form :model="reviewForm" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
            <a-form-item label="审查结果" required>
              <a-radio-group v-model:value="reviewForm.reviewStatus">
                <a-radio :value="1">
                  <a-tag color="success">合格</a-tag>
                </a-radio>
                <a-radio :value="2">
                  <a-tag color="error">不合格</a-tag>
                </a-radio>
              </a-radio-group>
            </a-form-item>
            <a-form-item label="审查意见">
              <a-textarea
                v-model:value="reviewForm.reviewComment"
                placeholder="请输入审查意见（选填）"
                :rows="4"
                :maxlength="500"
                show-count
              />
            </a-form-item>
          </a-form>
        </template>

        <!-- 已审查信息 -->
        <template v-else-if="detail?.reviewStatus !== 0">
          <a-divider>审查结果</a-divider>
          <a-descriptions :column="1" bordered>
            <a-descriptions-item label="审查状态">
              <a-tag :color="getReviewStatusColor(detail?.reviewStatus || 0)">
                {{ detail?.reviewStatusDesc }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item v-if="detail?.reviewerName" label="审查人">
              {{ detail?.reviewerName }}
            </a-descriptions-item>
            <a-descriptions-item v-if="detail?.reviewTime" label="审查时间">
              {{ detail?.reviewTime }}
            </a-descriptions-item>
            <a-descriptions-item v-if="detail?.reviewComment" label="审查意见">
              {{ detail?.reviewComment }}
            </a-descriptions-item>
          </a-descriptions>
        </template>
      </a-spin>

      <template #footer>
        <div style="text-align: right">
          <a-space>
            <a-button @click="drawerVisible = false">取消</a-button>
            <a-button
              v-if="currentRecord?.reviewStatus === 0"
              type="primary"
              :loading="submitting"
              @click="handleSubmitReview"
            >
              提交审查
            </a-button>
          </a-space>
        </div>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { ReloadOutlined, FileTextOutlined } from '@ant-design/icons-vue'
import { midtermApi } from '@/api/midterm'
import type { MidtermCheckListVO, MidtermCheckVO, ReviewMidtermCheckDTO } from '@/types/midterm'
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
const submitting = ref(false)

// 审查表单
const reviewForm = ref<ReviewMidtermCheckDTO>({
  checkId: '',
  reviewStatus: 1,
  reviewComment: ''
})

// 表格列定义
const columns = [
  { title: '学生', key: 'student', width: 150 },
  { title: '课题', key: 'topic', ellipsis: true },
  { title: '企业教师', key: 'enterpriseTeacher', width: 120 },
  { title: '审查状态', key: 'reviewStatus', width: 100 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
  { title: '操作', key: 'action', width: 100, fixed: 'right' }
]

// 加载列表
const loadList = async () => {
  loading.value = true
  try {
    const res = await midtermApi.getUnivList({
      studentName: searchText.value || undefined,
      reviewStatus: filterStatus.value,
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

// 查看/审查
const handleView = async (record: MidtermCheckListVO) => {
  currentRecord.value = record
  drawerVisible.value = true
  detailLoading.value = true

  // 重置表单
  reviewForm.value = {
    checkId: record.checkId,
    reviewStatus: 1,
    reviewComment: ''
  }

  try {
    const res = await midtermApi.getUnivDetail(record.checkId)
    detail.value = res.data || null
  } catch (error: any) {
    message.error(error.message || '加载详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 提交审查
const handleSubmitReview = async () => {
  if (!reviewForm.value.reviewStatus) {
    message.warning('请选择审查结果')
    return
  }

  submitting.value = true
  try {
    await midtermApi.reviewCheck(reviewForm.value)
    message.success('审查提交成功')
    drawerVisible.value = false
    loadList()
  } catch (error: any) {
    message.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

// 初始化
onMounted(() => {
  loadList()
})
</script>

<style scoped>
.midterm-check-review {
  padding: 24px;
}

.action-bar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
