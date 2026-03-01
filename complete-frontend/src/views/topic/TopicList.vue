<!--
  课题列表页面
  提供课题的查询、新建、编辑、删除、提交、撤回等功能
  
  @author 系统架构师
  @version 1.0
  @since 2026-02-22
-->
<template>
  <div class="topic-list">
    <!-- 搜索表单 -->
    <a-card class="topic-list__search" :bordered="false">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="课题名称">
          <a-input
            v-model:value="searchForm.topicTitle"
            placeholder="请输入课题名称"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item label="课题大类">
          <a-select
            v-model:value="searchForm.topicCategory"
            placeholder="请选择"
            allow-clear
            style="width: 150px"
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
            style="width: 150px"
          >
            <a-select-option :value="TopicType.DESIGN">设计</a-select-option>
            <a-select-option :value="TopicType.THESIS">论文</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="审查状态">
          <a-select
            v-model:value="searchForm.reviewStatus"
            placeholder="请选择"
            allow-clear
            style="width: 150px"
          >
            <a-select-option :value="TopicReviewStatus.PENDING_PRE_REVIEW">待预审</a-select-option>
            <a-select-option :value="TopicReviewStatus.PRE_REVIEW_PASSED">预审通过</a-select-option>
            <a-select-option :value="TopicReviewStatus.PRE_REVIEW_REJECTED">预审不通过</a-select-option>
            <a-select-option :value="TopicReviewStatus.PENDING_FINAL_REVIEW">待终审</a-select-option>
            <a-select-option :value="TopicReviewStatus.FINAL_REVIEW_PASSED">终审通过</a-select-option>
            <a-select-option :value="TopicReviewStatus.FINAL_REVIEW_REJECTED">终审不通过</a-select-option>
          </a-select>
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
    <a-card class="topic-list__actions" :bordered="false">
      <a-space>
        <!-- 只有企业教师才能新建课题 -->
        <a-button v-if="isEnterpriseTeacher" type="primary" @click="handleCreate">
          <template #icon><PlusOutlined /></template>
          新建课题
        </a-button>
        <!-- 企业教师显示综合意见管理和审批统计按钮 -->
        <a-button v-if="isEnterpriseTeacher" @click="showOpinionModal">
          <template #icon><MessageOutlined /></template>
          综合意见管理
        </a-button>
        <a-button v-if="isEnterpriseTeacher" @click="showStatsModal">
          <template #icon><BarChartOutlined /></template>
          我的审批统计
        </a-button>
      </a-space>
    </a-card>

    <!-- 数据表格 -->
    <a-card class="topic-list__table" :bordered="false">
      <a-table
        :columns="columns"
        :data-source="topicList"
        :loading="loading"
        :pagination="pagination"
        row-key="topicId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <!-- 课题名称 -->
          <template v-if="column.key === 'topicTitle'">
            <a @click="handleView(record)">{{ record.topicTitle }}</a>
          </template>

          <!-- 提交状态 -->
          <template v-else-if="column.key === 'isSubmitted'">
            <a-tag :color="record.isSubmitted === 1 ? 'green' : 'orange'">
              {{ record.isSubmitted === 1 ? '已提交' : '草稿' }}
            </a-tag>
          </template>

          <!-- 审查状态 -->
          <template v-else-if="column.key === 'reviewStatus'">
            <a-tag v-if="record.reviewStatusDesc" :color="getReviewStatusColor(record.reviewStatus)">
              {{ record.reviewStatusDesc }}
            </a-tag>
            <span v-else>-</span>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <!-- 以下操作仅企业教师可见 -->
              <a-button
                v-if="isEnterpriseTeacher && record.isSubmitted === 0"
                type="link"
                size="small"
                @click="handleEdit(record)"
              >
                编辑
              </a-button>
              <a-popconfirm
                v-if="isEnterpriseTeacher && record.isSubmitted === 0"
                title="确定要删除该课题吗？"
                @confirm="handleDelete(record.topicId)"
              >
                <a-button type="link" danger size="small">删除</a-button>
              </a-popconfirm>
              <a-button
                v-if="isEnterpriseTeacher && record.isSubmitted === 0"
                type="link"
                size="small"
                @click="handleSubmit(record.topicId)"
              >
                提交
              </a-button>
              <a-popconfirm
                v-if="isEnterpriseTeacher && record.isSubmitted === 1 && record.reviewStatus === TopicReviewStatus.PENDING_PRE_REVIEW"
                title="确定要撤回该课题吗？"
                @confirm="handleWithdraw(record.topicId)"
              >
                <a-button type="link" size="small">撤回</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 综合意见管理模态框（企业教师专用） -->
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
                    </template>
                  </a-list-item>
                </template>
              </a-list>
              <a-empty v-else description="暂无综合意见" />
            </a-spin>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-modal>

    <!-- 统计信息模态框（企业教师专用） -->
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  MessageOutlined,
  BarChartOutlined
} from '@ant-design/icons-vue'
import { topicApi } from '@/api/topic'
import { topicReviewApi } from '@/api/topicReview'
import { useUserStore } from '@/stores/user'
import type { TopicListVO, TopicQueryVO, GeneralOpinionVO, GeneralOpinionDTO, TeacherPassedCountVO } from '@/types/topic'
import { TopicCategory, TopicType, TopicReviewStatus, ReviewStage } from '@/types/topic'
import type { TableProps, FormInstance } from 'ant-design-vue'

// 定义组件选项
defineOptions({
  name: 'TopicList'
})

// 路由
const router = useRouter()

// 用户状态
const userStore = useUserStore()

// 是否为企业教师（显示综合意见管理和审批统计按钮）
const isEnterpriseTeacher = computed(() => {
  return userStore.hasAnyRole(['ENTERPRISE_TEACHER'])
})

// 搜索表单
const searchForm = reactive<TopicQueryVO>({
  topicTitle: '',
  topicCategory: undefined,
  topicType: undefined,
  reviewStatus: undefined,
  pageNum: 1,
  pageSize: 10
})

// 数据列表
const topicList = ref<TopicListVO[]>([])
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
    width: 250,
    ellipsis: true
  },
  {
    title: '课题大类',
    dataIndex: 'topicCategoryDesc',
    key: 'topicCategoryDesc',
    width: 100
  },
  {
    title: '课题类型',
    dataIndex: 'topicTypeDesc',
    key: 'topicTypeDesc',
    width: 100
  },
  {
    title: '归属企业',
    dataIndex: 'enterpriseName',
    key: 'enterpriseName',
    width: 150,
    ellipsis: true
  },
  {
    title: '创建人',
    dataIndex: 'creatorName',
    key: 'creatorName',
    width: 100
  },
  {
    title: '提交状态',
    dataIndex: 'isSubmitted',
    key: 'isSubmitted',
    width: 100
  },
  {
    title: '审查状态',
    dataIndex: 'reviewStatus',
    key: 'reviewStatus',
    width: 120
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right',
    width: 250
  }
]

// ==================== 综合意见相关（企业教师专用）====================
const opinionModalVisible = ref(false)
const opinionLoading = ref(false)
const opinionActiveTab = ref('list')
const generalOpinions = ref<GeneralOpinionVO[]>([])

// ==================== 统计相关（企业教师专用）====================
const statsModalVisible = ref(false)
const statsLoading = ref(false)
const passedCountStats = ref<TeacherPassedCountVO | null>(null)

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
 * 获取课题列表
 */
const getTopicList = async () => {
  try {
    loading.value = true
    const params: TopicQueryVO = {
      ...searchForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    }
    
    // 修复：课题列表页面应该显示所有课题，而不仅仅是我的课题
    const result = await topicApi.getTopicList(params)
    
    topicList.value = result.data.records || []
    pagination.total = result.data.total || 0
  } catch (error) {
    console.error('获取课题列表失败', error)
    message.error('获取课题列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.current = 1
  getTopicList()
}

/**
 * 重置
 */
const handleReset = () => {
  searchForm.topicTitle = ''
  searchForm.topicCategory = undefined
  searchForm.topicType = undefined
  searchForm.reviewStatus = undefined
  pagination.current = 1
  getTopicList()
}

/**
 * 表格变化
 */
const handleTableChange: TableProps['onChange'] = (pag) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  getTopicList()
}

/**
 * 新建课题
 */
const handleCreate = () => {
  router.push('/topic/create')
}

/**
 * 查看课题
 */
const handleView = (record: TopicListVO) => {
  router.push(`/topic/detail/${record.topicId}`)
}

/**
 * 编辑课题
 */
const handleEdit = (record: TopicListVO) => {
  router.push(`/topic/edit/${record.topicId}`)
}

/**
 * 删除课题
 */
const handleDelete = async (topicId: string) => {
  try {
    await topicApi.deleteTopic(topicId)
    message.success('删除成功')
    getTopicList()
  } catch (error) {
    console.error('删除课题失败', error)
    message.error('删除课题失败')
  }
}

/**
 * 提交课题
 */
const handleSubmit = async (topicId: string) => {
  try {
    await topicApi.submitTopic({ topicId })
    message.success('提交成功')
    getTopicList()
  } catch (error) {
    console.error('提交课题失败', error)
    message.error('提交课题失败')
  }
}

/**
 * 撤回课题
 */
const handleWithdraw = async (topicId: string) => {
  try {
    await topicApi.withdrawTopic(topicId)
    message.success('撤回成功')
    getTopicList()
  } catch (error) {
    console.error('撤回课题失败', error)
    message.error('撤回课题失败')
  }
}

// ==================== 综合意见操作（企业教师专用）====================

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

// ==================== 统计操作（企业教师专用）====================

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
  getTopicList()
})
</script>

<style scoped lang="scss">
.topic-list {
  padding: 20px;

  &__search {
    margin-bottom: 16px;
  }

  &__actions {
    margin-bottom: 16px;
  }
}
</style>
