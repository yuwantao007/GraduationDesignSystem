<!--
  课题审查列表页面
  提供待审核课题的查询、单个/批量审批、审查历史查看等功能
  
  @author 系统架构师
  @version 1.0
  @since 2026-02-22
-->
<template>
  <div class="topic-review">
    <!-- 搜索表单 - 仅审批角色可见 -->
    <a-card v-if="canReview" class="topic-review__search" :bordered="false">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="课题名称">
          <a-input
            v-model:value="searchForm.topicTitle"
            placeholder="请输入课题名称"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>

        <a-form-item label="课题大类">
          <a-select
            v-model:value="searchForm.topicCategory"
            placeholder="请选择"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="TopicCategory.UPGRADE">高职升本</a-select-option>
            <a-select-option :value="TopicCategory.THREE_PLUS_ONE">3+1</a-select-option>
            <a-select-option :value="TopicCategory.EXPERIMENTAL">实验班</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="课题类型">
          <a-select
            v-model:value="searchForm.topicType"
            placeholder="请选择"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="TopicType.DESIGN">设计</a-select-option>
            <a-select-option :value="TopicType.THESIS">论文</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="专业方向">
          <a-input
            v-model:value="searchForm.guidanceDirection"
            placeholder="请输入专业方向"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>

        <a-form-item label="创建人">
          <a-input
            v-model:value="searchForm.creatorName"
            placeholder="请输入创建人"
            allow-clear
            style="width: 120px"
          />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 操作按钮 -->
    <a-card class="topic-review__actions" :bordered="false">
      <a-space>
        <a-button
          v-if="canReview"
          type="primary"
          :disabled="selectedRowKeys.length === 0"
          @click="handleBatchReview"
        >
          <template #icon><CheckCircleOutlined /></template>
          批量审批 ({{ selectedRowKeys.length }})
        </a-button>
        <a-button @click="showOpinionModal">
          <template #icon><MessageOutlined /></template>
          综合意见管理
        </a-button>
        <a-button v-if="canViewStats" @click="showStatsModal">
          <template #icon><BarChartOutlined /></template>
          我的审批统计
        </a-button>
      </a-space>
    </a-card>

    <!-- 数据表格 - 仅审批角色可见 -->
    <a-card v-if="canReview" class="topic-review__table" :bordered="false">
      <a-table
        :columns="columns"
        :data-source="reviewList"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        row-key="topicId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <!-- 课题名称 -->
          <template v-if="column.key === 'topicTitle'">
            <a @click="handleViewTopic(record)">{{ record.topicTitle }}</a>
          </template>

          <!-- 可审批状态 -->
          <template v-else-if="column.key === 'canReview'">
            <a-tag :color="record.canReview ? 'green' : 'default'">
              {{ record.canReview ? '可审批' : '不可审批' }}
            </a-tag>
          </template>

          <!-- 审查状态 -->
          <template v-else-if="column.key === 'reviewStatus'">
            <a-tag :color="getReviewStatusColor(record.reviewStatus)">
              {{ record.reviewStatusName }}
            </a-tag>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                v-if="record.canReview"
                type="link"
                size="small"
                @click="handleReview(record)"
              >
                审批
              </a-button>
              <a-button type="link" size="small" @click="handleViewHistory(record)">
                历史
              </a-button>
              <a-button type="link" size="small" @click="handleViewTopic(record)">
                详情
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 单个审批模态框 -->
    <a-modal
      v-model:open="reviewModalVisible"
      title="课题审批"
      :confirm-loading="reviewSubmitting"
      @ok="handleReviewSubmit"
      @cancel="handleReviewCancel"
    >
      <a-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewRules"
        layout="vertical"
      >
        <a-form-item label="课题名称">
          <span class="review-topic-title">{{ currentReviewTopic?.topicTitle }}</span>
        </a-form-item>

        <a-form-item label="审批结果" name="reviewResult" required>
          <a-radio-group v-model:value="reviewForm.reviewResult">
            <a-radio-button :value="ReviewResult.PASSED">
              <CheckOutlined /> 通过
            </a-radio-button>
            <a-radio-button :value="ReviewResult.NEED_MODIFY">
              <EditOutlined /> 需修改
            </a-radio-button>
            <a-radio-button :value="ReviewResult.REJECTED">
              <CloseOutlined /> 不通过
            </a-radio-button>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="审批意见" name="reviewOpinion">
          <a-textarea
            v-model:value="reviewForm.reviewOpinion"
            :rows="4"
            :maxlength="500"
            show-count
            placeholder="请输入审批意见（最多500字）"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 批量审批模态框 -->
    <a-modal
      v-model:open="batchReviewModalVisible"
      title="批量课题审批"
      :confirm-loading="batchReviewSubmitting"
      @ok="handleBatchReviewSubmit"
      @cancel="handleBatchReviewCancel"
    >
      <a-alert
        :message="`您已选择 ${selectedRowKeys.length} 个课题进行批量审批`"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />

      <a-form
        ref="batchReviewFormRef"
        :model="batchReviewForm"
        :rules="reviewRules"
        layout="vertical"
      >
        <a-form-item label="审批结果" name="reviewResult" required>
          <a-radio-group v-model:value="batchReviewForm.reviewResult">
            <a-radio-button :value="ReviewResult.PASSED">
              <CheckOutlined /> 通过
            </a-radio-button>
            <a-radio-button :value="ReviewResult.NEED_MODIFY">
              <EditOutlined /> 需修改
            </a-radio-button>
            <a-radio-button :value="ReviewResult.REJECTED">
              <CloseOutlined /> 不通过
            </a-radio-button>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="审批意见" name="reviewOpinion">
          <a-textarea
            v-model:value="batchReviewForm.reviewOpinion"
            :rows="4"
            :maxlength="500"
            show-count
            placeholder="请输入批量审批意见（最多500字）"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 审查历史抽屉 -->
    <a-drawer
      v-model:open="historyDrawerVisible"
      title="审查历史记录"
      width="600"
      placement="right"
    >
      <div v-if="currentHistoryTopic" class="history-topic-info">
        <a-descriptions :column="1" bordered size="small">
          <a-descriptions-item label="课题名称">
            {{ currentHistoryTopic.topicTitle }}
          </a-descriptions-item>
          <a-descriptions-item label="课题大类">
            {{ currentHistoryTopic.topicCategoryName }}
          </a-descriptions-item>
          <a-descriptions-item label="当前状态">
            <a-tag :color="getReviewStatusColor(currentHistoryTopic.reviewStatus)">
              {{ currentHistoryTopic.reviewStatusName }}
            </a-tag>
          </a-descriptions-item>
        </a-descriptions>
      </div>

      <a-divider>审查记录</a-divider>

      <a-spin :spinning="historyLoading">
        <a-timeline v-if="reviewHistory.length > 0">
          <a-timeline-item
            v-for="record in reviewHistory"
            :key="record.reviewId"
            :color="getTimelineColor(record.reviewResult)"
          >
            <div class="history-record">
              <div class="history-record__header">
                <a-tag :color="getReviewResultColor(record.reviewResult)">
                  {{ record.reviewResultName }}
                </a-tag>
                <span class="history-record__stage">{{ record.reviewStageName }}</span>
                <span v-if="record.isBatchReview" class="history-record__batch">
                  <a-tag color="blue" size="small">批量审批</a-tag>
                </span>
                <span v-if="record.isModified" class="history-record__modified">
                  <a-tag color="orange" size="small">已修改</a-tag>
                </span>
              </div>
              <div class="history-record__reviewer">
                审查人：{{ record.reviewerName }}（{{ record.reviewerRoleName }}）
              </div>
              <div v-if="record.reviewOpinion" class="history-record__opinion">
                意见：{{ record.reviewOpinion }}
              </div>
              <div class="history-record__time">
                时间：{{ record.createTime }}
              </div>
              <div v-if="record.isModified && record.modifiedByName" class="history-record__modified-info">
                修改人：{{ record.modifiedByName }} | 修改时间：{{ record.modifiedTime }}
              </div>
            </div>
          </a-timeline-item>
        </a-timeline>
        <a-empty v-else description="暂无审查记录" />
      </a-spin>
    </a-drawer>

    <!-- 综合意见管理模态框 -->
    <a-modal
      v-model:open="opinionModalVisible"
      title="综合意见管理"
      :width="660"
      :footer="null"
      centered
    >
      <div class="opinion-modal-content">
        <a-tabs v-model:activeKey="opinionActiveTab" size="small">
        <a-tab-pane key="list" tab="意见列表">
          <a-spin :spinning="opinionLoading">
            <a-list
              v-if="generalOpinions.length > 0"
              :data-source="generalOpinions"
              item-layout="horizontal"
            >
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta
                    :title="`${item.reviewStageName} - ${item.guidanceDirection}`"
                    :description="item.opinionContent"
                  >
                    <template #avatar>
                      <a-avatar style="background-color: #1890ff">
                        {{ item.submitterName?.charAt(0) }}
                      </a-avatar>
                    </template>
                  </a-list-item-meta>
                  <template #actions>
                    <span>{{ item.createTime }}</span>
                    <a-popconfirm
                      title="确定要删除该意见吗？"
                      @confirm="handleDeleteOpinion(item.opinionId)"
                    >
                      <a-button type="link" danger size="small">删除</a-button>
                    </a-popconfirm>
                  </template>
                </a-list-item>
              </template>
            </a-list>
            <a-empty v-else description="暂无综合意见" />
          </a-spin>
        </a-tab-pane>

        <a-tab-pane key="add" tab="提交意见">
          <a-form
            ref="opinionFormRef"
            :model="opinionForm"
            :rules="opinionRules"
            layout="vertical"
          >
            <a-form-item label="审查阶段" name="reviewStage" required>
              <a-select v-model:value="opinionForm.reviewStage" placeholder="请选择">
                <a-select-option :value="ReviewStage.INITIAL_REVIEW">初审</a-select-option>
                <a-select-option :value="ReviewStage.FINAL_REVIEW">终审</a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item label="专业方向" name="guidanceDirection" required>
              <a-input
                v-model:value="opinionForm.guidanceDirection"
                placeholder="请输入专业方向"
              />
            </a-form-item>

            <a-form-item label="意见内容" name="opinionContent" required>
              <a-textarea
                v-model:value="opinionForm.opinionContent"
                :rows="4"
                :maxlength="200"
                show-count
                placeholder="请输入综合意见内容（最多200字）"
              />
            </a-form-item>

            <a-form-item>
              <a-button
                type="primary"
                :loading="opinionSubmitting"
                @click="handleSubmitOpinion"
              >
                提交意见
              </a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>
      </div>
    </a-modal>

    <!-- 统计信息模态框 -->
    <a-modal
      v-model:open="statsModalVisible"
      title="我的审批统计"
      :footer="null"
    >
      <a-spin :spinning="statsLoading">
        <a-descriptions v-if="passedCountStats" :column="1" bordered>
          <a-descriptions-item label="教师姓名">
            {{ passedCountStats.teacherName }}
          </a-descriptions-item>
          <a-descriptions-item label="已通过终审课题数">
            <a-statistic
              :value="passedCountStats.passedCount"
              :value-style="{ color: passedCountStats.reachedLimit ? '#f5222d' : '#3f8600' }"
            />
          </a-descriptions-item>
          <a-descriptions-item label="剩余可提交数">
            <a-statistic
              :value="passedCountStats.remainingCount"
              :value-style="{ color: passedCountStats.remainingCount === 0 ? '#f5222d' : '#1890ff' }"
            />
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="passedCountStats.reachedLimit ? 'error' : 'success'">
              {{ passedCountStats.reachedLimit ? '已达上限' : '可继续提交' }}
            </a-tag>
          </a-descriptions-item>
        </a-descriptions>
        <a-empty v-else description="暂无统计数据" />
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import {
  SearchOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  MessageOutlined,
  BarChartOutlined,
  CheckOutlined,
  EditOutlined,
  CloseOutlined
} from '@ant-design/icons-vue'
import { topicReviewApi } from '@/api/topicReview'
import type {
  TopicReviewListVO,
  TopicReviewRecordVO,
  ReviewQueryVO,
  ReviewTopicDTO,
  BatchReviewDTO,
  GeneralOpinionVO,
  GeneralOpinionDTO,
  TeacherPassedCountVO
} from '@/types/topic'
import { TopicCategory, TopicType, ReviewStage, ReviewResult } from '@/types/topic'
import type { TableProps, FormInstance } from 'ant-design-vue'

// 定义组件选项
defineOptions({
  name: 'TopicReview'
})

// 路由
const router = useRouter()

// 用户状态
const userStore = useUserStore()

// 是否有审批权限（企业教师只能查看历史和综合意见，不能审批）
const canReview = computed(() => {
  return userStore.hasAnyRole(['UNIVERSITY_TEACHER', 'MAJOR_DIRECTOR', 'SUPERVISOR_TEACHER'])
})

// 是否可以查看审批统计（仅企业教师、督导教师、企业负责人有权限）
const canViewStats = computed(() => {
  return userStore.hasAnyRole(['ENTERPRISE_TEACHER', 'SUPERVISOR_TEACHER', 'ENTERPRISE_LEADER'])
})

// ==================== 搜索相关 ====================
const searchForm = reactive<ReviewQueryVO>({
  topicTitle: '',
  topicCategory: undefined,
  topicType: undefined,
  guidanceDirection: '',
  creatorName: '',
  pageNum: 1,
  pageSize: 10
})

// ==================== 列表相关 ====================
const reviewList = ref<TopicReviewListVO[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 表格列定义
const columns: TableProps['columns'] = [
  {
    title: '课题名称',
    dataIndex: 'topicTitle',
    key: 'topicTitle',
    width: 200,
    ellipsis: true
  },
  {
    title: '课题大类',
    dataIndex: 'topicCategoryName',
    key: 'topicCategoryName',
    width: 100
  },
  {
    title: '课题类型',
    dataIndex: 'topicTypeName',
    key: 'topicTypeName',
    width: 80
  },
  {
    title: '专业方向',
    dataIndex: 'guidanceDirection',
    key: 'guidanceDirection',
    width: 120,
    ellipsis: true
  },
  {
    title: '创建人',
    dataIndex: 'creatorName',
    key: 'creatorName',
    width: 100
  },
  {
    title: '归属企业',
    dataIndex: 'enterpriseName',
    key: 'enterpriseName',
    width: 120,
    ellipsis: true
  },
  {
    title: '审查状态',
    dataIndex: 'reviewStatus',
    key: 'reviewStatus',
    width: 100
  },
  {
    title: '可审批',
    dataIndex: 'canReview',
    key: 'canReview',
    width: 90
  },
  {
    title: '提交时间',
    dataIndex: 'submitTime',
    key: 'submitTime',
    width: 160
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    width: 180
  }
]

// 选择相关
const selectedRowKeys = ref<string[]>([])
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: string[]) => {
    // 只选择可审批的课题
    selectedRowKeys.value = keys.filter(key => {
      const topic = reviewList.value.find(t => t.topicId === key)
      return topic?.canReview
    })
  },
  getCheckboxProps: (record: TopicReviewListVO) => ({
    disabled: !record.canReview
  })
}))

// ==================== 单个审批相关 ====================
const reviewModalVisible = ref(false)
const reviewSubmitting = ref(false)
const reviewFormRef = ref<FormInstance>()
const currentReviewTopic = ref<TopicReviewListVO | null>(null)
const reviewForm = reactive<ReviewTopicDTO>({
  topicId: '',
  reviewResult: ReviewResult.PASSED,
  reviewOpinion: ''
})

const reviewRules = {
  reviewResult: [{ required: true, message: '请选择审批结果', trigger: 'change' }]
}

// ==================== 批量审批相关 ====================
const batchReviewModalVisible = ref(false)
const batchReviewSubmitting = ref(false)
const batchReviewFormRef = ref<FormInstance>()
const batchReviewForm = reactive<Omit<BatchReviewDTO, 'topicIds'>>({
  reviewResult: ReviewResult.PASSED,
  reviewOpinion: ''
})

// ==================== 历史记录相关 ====================
const historyDrawerVisible = ref(false)
const historyLoading = ref(false)
const currentHistoryTopic = ref<TopicReviewListVO | null>(null)
const reviewHistory = ref<TopicReviewRecordVO[]>([])

// ==================== 综合意见相关 ====================
const opinionModalVisible = ref(false)
const opinionLoading = ref(false)
const opinionSubmitting = ref(false)
const opinionActiveTab = ref('list')
const opinionFormRef = ref<FormInstance>()
const generalOpinions = ref<GeneralOpinionVO[]>([])
const opinionForm = reactive<GeneralOpinionDTO>({
  reviewStage: ReviewStage.INITIAL_REVIEW,
  guidanceDirection: '',
  opinionContent: ''
})

const opinionRules = {
  reviewStage: [{ required: true, message: '请选择审查阶段', trigger: 'change' }],
  guidanceDirection: [{ required: true, message: '请输入专业方向', trigger: 'blur' }],
  opinionContent: [{ required: true, message: '请输入意见内容', trigger: 'blur' }]
}

// ==================== 统计相关 ====================
const statsModalVisible = ref(false)
const statsLoading = ref(false)
const passedCountStats = ref<TeacherPassedCountVO | null>(null)

// ==================== 工具方法 ====================

/**
 * 获取审查状态颜色
 */
const getReviewStatusColor = (status?: number): string => {
  if (!status) return 'default'
  const colorMap: Record<number, string> = {
    1: 'processing', // 待预审
    2: 'success',    // 预审通过
    3: 'error',      // 预审不通过
    4: 'processing', // 待终审
    5: 'success',    // 终审通过
    6: 'error'       // 终审不通过
  }
  return colorMap[status] || 'default'
}

/**
 * 获取审批结果颜色
 */
const getReviewResultColor = (result: ReviewResult): string => {
  const colorMap: Record<ReviewResult, string> = {
    [ReviewResult.PASSED]: 'success',
    [ReviewResult.NEED_MODIFY]: 'warning',
    [ReviewResult.REJECTED]: 'error'
  }
  return colorMap[result] || 'default'
}

/**
 * 获取时间线颜色
 */
const getTimelineColor = (result: ReviewResult): string => {
  const colorMap: Record<ReviewResult, string> = {
    [ReviewResult.PASSED]: 'green',
    [ReviewResult.NEED_MODIFY]: 'orange',
    [ReviewResult.REJECTED]: 'red'
  }
  return colorMap[result] || 'gray'
}

// ==================== 列表操作 ====================

/**
 * 获取待审查课题列表
 */
const getReviewList = async () => {
  try {
    loading.value = true
    const params: ReviewQueryVO = {
      ...searchForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    }

    const result = await topicReviewApi.getPendingReviewList(params)
    reviewList.value = result.data.records || []
    pagination.total = result.data.total || 0
  } catch (error) {
    console.error('获取待审查课题列表失败', error)
    message.error('获取待审查课题列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.current = 1
  selectedRowKeys.value = []
  getReviewList()
}

/**
 * 重置
 */
const handleReset = () => {
  searchForm.topicTitle = ''
  searchForm.topicCategory = undefined
  searchForm.topicType = undefined
  searchForm.guidanceDirection = ''
  searchForm.creatorName = ''
  pagination.current = 1
  selectedRowKeys.value = []
  getReviewList()
}

/**
 * 表格变化
 */
const handleTableChange: TableProps['onChange'] = (pag) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  getReviewList()
}

/**
 * 查看课题详情
 */
const handleViewTopic = (record: TopicReviewListVO) => {
  router.push(`/topic/detail/${record.topicId}`)
}

// ==================== 单个审批操作 ====================

/**
 * 打开审批模态框
 */
const handleReview = (record: TopicReviewListVO) => {
  currentReviewTopic.value = record
  reviewForm.topicId = record.topicId
  reviewForm.reviewResult = ReviewResult.PASSED
  reviewForm.reviewOpinion = ''
  reviewModalVisible.value = true
}

/**
 * 提交审批
 */
const handleReviewSubmit = async () => {
  try {
    await reviewFormRef.value?.validate()
    reviewSubmitting.value = true

    await topicReviewApi.reviewTopic(reviewForm)
    message.success('审批成功')
    reviewModalVisible.value = false
    getReviewList()
  } catch (error) {
    console.error('审批失败', error)
    message.error('审批失败')
  } finally {
    reviewSubmitting.value = false
  }
}

/**
 * 取消审批
 */
const handleReviewCancel = () => {
  reviewModalVisible.value = false
  currentReviewTopic.value = null
  reviewFormRef.value?.resetFields()
}

// ==================== 批量审批操作 ====================

/**
 * 打开批量审批模态框
 */
const handleBatchReview = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要审批的课题')
    return
  }
  batchReviewForm.reviewResult = ReviewResult.PASSED
  batchReviewForm.reviewOpinion = ''
  batchReviewModalVisible.value = true
}

/**
 * 提交批量审批
 */
const handleBatchReviewSubmit = async () => {
  try {
    await batchReviewFormRef.value?.validate()
    batchReviewSubmitting.value = true

    const data: BatchReviewDTO = {
      topicIds: selectedRowKeys.value,
      reviewResult: batchReviewForm.reviewResult,
      reviewOpinion: batchReviewForm.reviewOpinion
    }

    const result = await topicReviewApi.batchReviewTopics(data)
    
    if (result.data.failCount === 0) {
      message.success(`批量审批成功，共审批 ${result.data.successCount} 个课题`)
    } else {
      message.warning(
        `审批完成：成功 ${result.data.successCount} 个，失败 ${result.data.failCount} 个`
      )
    }

    batchReviewModalVisible.value = false
    selectedRowKeys.value = []
    getReviewList()
  } catch (error) {
    console.error('批量审批失败', error)
    message.error('批量审批失败')
  } finally {
    batchReviewSubmitting.value = false
  }
}

/**
 * 取消批量审批
 */
const handleBatchReviewCancel = () => {
  batchReviewModalVisible.value = false
  batchReviewFormRef.value?.resetFields()
}

// ==================== 历史记录操作 ====================

/**
 * 查看审查历史
 */
const handleViewHistory = async (record: TopicReviewListVO) => {
  currentHistoryTopic.value = record
  historyDrawerVisible.value = true
  
  try {
    historyLoading.value = true
    const result = await topicReviewApi.getReviewHistory(record.topicId)
    reviewHistory.value = result.data || []
  } catch (error) {
    console.error('获取审查历史失败', error)
    message.error('获取审查历史失败')
  } finally {
    historyLoading.value = false
  }
}

// ==================== 综合意见操作 ====================

/**
 * 显示综合意见模态框
 */
const showOpinionModal = async () => {
  opinionModalVisible.value = true
  opinionActiveTab.value = 'list'
  await loadGeneralOpinions()
}

/**
 * 加载综合意见列表
 */
const loadGeneralOpinions = async () => {
  try {
    opinionLoading.value = true
    const result = await topicReviewApi.getGeneralOpinions()
    generalOpinions.value = result.data || []
  } catch (error) {
    console.error('获取综合意见失败', error)
    message.error('获取综合意见失败')
  } finally {
    opinionLoading.value = false
  }
}

/**
 * 提交综合意见
 */
const handleSubmitOpinion = async () => {
  try {
    await opinionFormRef.value?.validate()
    opinionSubmitting.value = true

    await topicReviewApi.submitGeneralOpinion(opinionForm)
    message.success('意见提交成功')
    
    // 重置表单并切换到列表
    opinionForm.reviewStage = ReviewStage.INITIAL_REVIEW
    opinionForm.guidanceDirection = ''
    opinionForm.opinionContent = ''
    opinionFormRef.value?.resetFields()
    opinionActiveTab.value = 'list'
    await loadGeneralOpinions()
  } catch (error) {
    console.error('提交意见失败', error)
    message.error('提交意见失败')
  } finally {
    opinionSubmitting.value = false
  }
}

/**
 * 删除综合意见
 */
const handleDeleteOpinion = async (opinionId: string) => {
  try {
    await topicReviewApi.deleteGeneralOpinion(opinionId)
    message.success('删除成功')
    await loadGeneralOpinions()
  } catch (error) {
    console.error('删除意见失败', error)
    message.error('删除意见失败')
  }
}

// ==================== 统计操作 ====================

/**
 * 显示统计模态框
 */
const showStatsModal = async () => {
  statsModalVisible.value = true
  
  try {
    statsLoading.value = true
    const result = await topicReviewApi.getTeacherPassedCount()
    passedCountStats.value = result.data
  } catch (error) {
    console.error('获取统计数据失败', error)
    message.error('获取统计数据失败')
  } finally {
    statsLoading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  // 只有审批角色才加载待审列表，企业教师只能查看历史和综合意见
  if (canReview.value) {
    getReviewList()
  }
})
</script>

<style scoped lang="scss">
.topic-review {
  padding: 20px;

  &__search {
    margin-bottom: 16px;
  }

  &__actions {
    margin-bottom: 16px;
  }
}

.review-topic-title {
  font-weight: 600;
  color: #1890ff;
}

.history-topic-info {
  margin-bottom: 16px;
}

.history-record {
  &__header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }

  &__stage {
    font-weight: 500;
  }

  &__reviewer,
  &__opinion,
  &__time,
  &__modified-info {
    font-size: 13px;
    color: #666;
    margin-bottom: 4px;
  }

  &__opinion {
    background: #f5f5f5;
    padding: 8px 12px;
    border-radius: 4px;
    margin: 8px 0;
  }

  &__modified-info {
    color: #fa8c16;
    font-style: italic;
  }
}

.opinion-modal-content {
  max-height: 350px;
  overflow-y: auto;
}
</style>
