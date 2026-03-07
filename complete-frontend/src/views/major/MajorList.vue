<template>
  <div class="major-list">
    <a-card class="major-list__card">
      <!-- 页面标题和操作按钮 -->
      <div class="major-list__header">
        <h3 class="major-list__title">专业管理</h3>
        <a-space>
          <a-button @click="handleImportExcel">
            <template #icon><ImportOutlined /></template>
            Excel 导入
          </a-button>
          <a-button type="primary" @click="handleAddDirection">
            <template #icon><PlusOutlined /></template>
            新建专业方向
          </a-button>
          <a-button @click="handleAddMajor">
            <template #icon><PlusOutlined /></template>
            新建专业
          </a-button>
        </a-space>
      </div>

      <!-- 搜索区域 -->
      <div class="major-list__search">
        <a-form layout="inline" :model="searchForm">
          <a-form-item label="企业" v-if="userStore.hasAnyRole(['SYSTEM_ADMIN'])">
            <a-select
              v-model:value="searchForm.enterpriseId"
              placeholder="全部企业"
              allow-clear
              style="width: 200px"
              :options="enterpriseOptions"
              :loading="enterpriseLoading"
              @change="handleSearch"
            />
          </a-form-item>
          
          <a-form-item label="状态">
            <a-select
              v-model:value="searchForm.status"
              placeholder="全部状态"
              allow-clear
              style="width: 120px"
              @change="handleSearch"
            >
              <a-select-option :value="1">启用</a-select-option>
              <a-select-option :value="0">禁用</a-select-option>
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
              <a-button @click="handleExpandAll">
                {{ expandAll ? '全部收起' : '全部展开' }}
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>

      <!-- 树型结构表格 -->
      <a-spin :spinning="loading">
        <a-table
          :columns="columns"
          :data-source="treeData"
          :pagination="false"
          :expanded-row-keys="expandedKeys"
          row-key="id"
          @expand="handleExpand"
        >
          <!-- 名称列 -->
          <template #name="{ record }">
            <span :class="`node-${record.type}`">
              <BankOutlined v-if="record.type === 'enterprise'" class="icon-enterprise" />
              <FolderOutlined v-else-if="record.type === 'direction'" class="icon-direction" />
              <BookOutlined v-else class="icon-major" />
              {{ record.label }}
            </span>
          </template>

          <!-- 类型列 -->
          <template #type="{ record }">
            <a-tag v-if="record.type === 'enterprise'" color="purple">企业</a-tag>
            <a-tag v-else-if="record.type === 'direction'" color="blue">专业方向</a-tag>
            <a-tag v-else color="green">专业</a-tag>
          </template>

          <!-- 状态列 -->
          <template #status="{ record }">
            <a-tag :color="MAJOR_STATUS_COLORS[record.status]">
              {{ MAJOR_STATUS_LABELS[record.status] }}
            </a-tag>
          </template>

          <!-- 操作列 -->
          <template #action="{ record }">
            <a-space>
              <!-- 企业节点操作 -->
              <template v-if="record.type === 'enterprise'">
                <a-button type="link" size="small" @click="handleAddDirectionToEnterprise(record)">
                  <template #icon><PlusOutlined /></template>
                  添加专业方向
                </a-button>
              </template>
              
              <!-- 专业方向操作 -->
              <template v-else-if="record.type === 'direction'">
                <a-button type="link" size="small" @click="handleAddMajorToDirection(record)">
                  <template #icon><PlusOutlined /></template>
                  添加专业
                </a-button>
                <a-button type="link" size="small" @click="handleEditDirection(record)">
                  <template #icon><EditOutlined /></template>
                  编辑
                </a-button>
                <a-popconfirm
                  title="确定要删除该专业方向吗？"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="handleDeleteDirection(record)"
                >
                  <a-button type="link" size="small" danger>
                    <template #icon><DeleteOutlined /></template>
                    删除
                  </a-button>
                </a-popconfirm>
                <a-button
                  type="link"
                  size="small"
                  @click="handleToggleDirectionStatus(record)"
                >
                  {{ record.status === 1 ? '禁用' : '启用' }}
                </a-button>
              </template>

              <!-- 专业操作 -->
              <template v-else>
                <a-button type="link" size="small" @click="handleViewMajor(record)">
                  <template #icon><EyeOutlined /></template>
                  查看
                </a-button>
                <a-button type="link" size="small" @click="handleEditMajor(record)">
                  <template #icon><EditOutlined /></template>
                  编辑
                </a-button>
                <a-popconfirm
                  title="确定要删除该专业吗？"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="handleDeleteMajor(record)"
                >
                  <a-button type="link" size="small" danger>
                    <template #icon><DeleteOutlined /></template>
                    删除
                  </a-button>
                </a-popconfirm>
                <a-button
                  type="link"
                  size="small"
                  @click="handleToggleMajorStatus(record)"
                >
                  {{ record.status === 1 ? '禁用' : '启用' }}
                </a-button>
              </template>
            </a-space>
          </template>
        </a-table>

        <!-- 空状态 -->
        <a-empty
          v-if="!loading && treeData.length === 0"
          description="暂无专业数据，请先添加专业方向"
          class="major-list__empty"
        >
          <a-button type="primary" @click="handleAddDirection">
            <template #icon><PlusOutlined /></template>
            新建专业方向
          </a-button>
        </a-empty>
      </a-spin>
    </a-card>

    <!-- 专业方向表单弹窗 -->
    <DirectionFormModal
      v-model:open="directionModalVisible"
      :direction-data="currentDirection"
      :enterprise-id="currentEnterpriseId"
      @success="loadMajorTree"
    />

    <!-- 专业表单弹窗 -->
    <MajorFormModal
      v-model:open="majorModalVisible"
      :major-data="currentMajor"
      :pre-selected-direction-id="preSelectedDirectionId"
      @success="loadMajorTree"
    />

    <!-- Excel 导入弹窗 -->
    <ImportMajorModal
      v-model:open="importModalVisible"
      @success="loadMajorTree"
    />

    <!-- 专业详情抽屉 -->
    <a-drawer
      v-model:open="detailDrawerVisible"
      title="专业详情"
      width="500"
      :body-style="{ paddingBottom: '80px' }"
    >
      <a-descriptions bordered :column="1" v-if="majorDetail">
        <a-descriptions-item label="专业名称">
          {{ majorDetail.majorName }}
        </a-descriptions-item>
        <a-descriptions-item label="专业代码">
          {{ majorDetail.majorCode || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="所属方向">
          {{ majorDetail.directionName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="学位类型">
          {{ majorDetail.degreeType ? DEGREE_TYPE_LABELS[majorDetail.degreeType] : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="学制">
          {{ majorDetail.educationYears ? `${majorDetail.educationYears}年` : '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="描述">
          {{ majorDetail.description || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="MAJOR_STATUS_COLORS[majorDetail.status]">
            {{ MAJOR_STATUS_LABELS[majorDetail.status] }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ majorDetail.createTime || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ majorDetail.updateTime || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="企业老师">
          <template v-if="majorDetail.teachers && majorDetail.teachers.length > 0">
            <a-space wrap>
              <a-tag
                v-for="teacher in majorDetail.teachers"
                :key="teacher.userId"
                color="blue"
              >
                {{ teacher.realName }}
                <span v-if="teacher.userCode" style="font-size: 11px; color: #91caff">
                  ({{ teacher.userCode }})
                </span>
              </a-tag>
            </a-space>
          </template>
          <span v-else style="color: #bfbfbf">暂未关联企业老师</span>
        </a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
/**
 * 专业管理页面
 * @description 专业方向和专业的树型结构管理，支持CRUD操作
 * @author YuWan
 * @date 2026-03-01
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { TableColumnsType } from 'ant-design-vue'
import {
  PlusOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  EditOutlined,
  EyeOutlined,
  FolderOutlined,
  BookOutlined,
  BankOutlined,
  ImportOutlined
} from '@ant-design/icons-vue'
import { majorApi } from '@/api/major'
import { enterpriseApi } from '@/api/enterprise'
import type { MajorTreeVO, MajorVO, MajorDirectionVO } from '@/types/major'
import type { EnterpriseVO } from '@/types/enterprise'
import {
  MAJOR_STATUS_LABELS,
  MAJOR_STATUS_COLORS,
  DEGREE_TYPE_LABELS
} from '@/types/major'
import DirectionFormModal from '@/components/major/DirectionFormModal.vue'
import MajorFormModal from '@/components/major/MajorFormModal.vue'
import ImportMajorModal from '@/components/major/ImportMajorModal.vue'
import { useUserStore } from '@/stores/user'

defineOptions({
  name: 'MajorList'
})

const userStore = useUserStore()

// 搜索表单
const searchForm = reactive({
  enterpriseId: undefined as string | undefined,
  status: undefined as number | undefined
})

// 企业选项
const enterpriseOptions = ref<{ value: string; label: string }[]>([])
const enterpriseLoading = ref(false)

// 树型数据
const treeData = ref<MajorTreeVO[]>([])
const loading = ref(false)

// 展开控制
const expandedKeys = ref<string[]>([])
const expandAll = ref(false)

// 表格列配置
const columns: TableColumnsType = [
  {
    title: '名称',
    dataIndex: 'label',
    key: 'label',
    width: 300,
    slots: { customRender: 'name' }
  },
  {
    title: '类型',
    dataIndex: 'type',
    key: 'type',
    width: 120,
    slots: { customRender: 'type' }
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    slots: { customRender: 'status' }
  },
  {
    title: '操作',
    key: 'action',
    width: 300,
    slots: { customRender: 'action' }
  }
]

// 弹窗控制
const directionModalVisible = ref(false)
const majorModalVisible = ref(false)
const detailDrawerVisible = ref(false)
const importModalVisible = ref(false)

// 当前编辑数据
const currentDirection = ref<MajorDirectionVO | null>(null)
const currentMajor = ref<MajorVO | null>(null)
const majorDetail = ref<MajorVO | null>(null)
const preSelectedDirectionId = ref('')

// 当前操作的企业ID（用于生成代码）
const currentEnterpriseId = ref('')

/**
 * 加载专业树型结构
 */
const loadMajorTree = async () => {
  loading.value = true
  try {
    const response = await majorApi.getMajorTree(
      searchForm.enterpriseId,
      searchForm.status
    )
    treeData.value = response.data

    // 默认展开所有企业节点（第一层）
    if (treeData.value.length > 0 && expandedKeys.value.length === 0) {
      expandedKeys.value = treeData.value
        .filter(item => item.type === 'enterprise')
        .map(item => item.id)
    }
  } catch (error) {
    message.error('加载专业数据失败')
    console.error('加载专业数据失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载企业选项（系统管理员用）
 */
const loadEnterpriseOptions = async () => {
  if (!userStore.hasAnyRole(['SYSTEM_ADMIN'])) {
    return
  }
  
  enterpriseLoading.value = true
  try {
    const response = await enterpriseApi.getAllEnterprises()
    enterpriseOptions.value = response.data.map((item: EnterpriseVO) => ({
      value: item.enterpriseId,
      label: item.enterpriseName
    }))
  } catch (error) {
    console.error('加载企业列表失败:', error)
  } finally {
    enterpriseLoading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  loadMajorTree()
}

/**
 * 重置
 */
const handleReset = () => {
  searchForm.enterpriseId = undefined
  searchForm.status = undefined
  loadMajorTree()
}

/**
 * 展开/收起节点
 */
const handleExpand = (expanded: boolean, record: MajorTreeVO) => {
  if (expanded) {
    expandedKeys.value = [...expandedKeys.value, record.id]
  } else {
    expandedKeys.value = expandedKeys.value.filter(key => key !== record.id)
  }
}

/**
 * 全部展开/收起
 */
const handleExpandAll = () => {
  if (expandAll.value) {
    expandedKeys.value = []
  } else {
    // 展开所有企业和专业方向节点
    const allKeys: string[] = []
    const collectKeys = (nodes: MajorTreeVO[]) => {
      nodes.forEach(node => {
        if (node.type === 'enterprise' || node.type === 'direction') {
          allKeys.push(node.id)
          if (node.children) {
            collectKeys(node.children)
          }
        }
      })
    }
    collectKeys(treeData.value)
    expandedKeys.value = allKeys
  }
  expandAll.value = !expandAll.value
}

// ==================== 企业节点操作 ====================

/**
 * 在指定企业下添加专业方向
 */
const handleAddDirectionToEnterprise = (record: MajorTreeVO) => {
  currentDirection.value = null
  // 设置当前企业ID（从企业节点获取）
  currentEnterpriseId.value = record.id
  directionModalVisible.value = true
}

// ==================== 专业方向操作 ====================

/**
 * 打开 Excel 导入弹窗
 */
const handleImportExcel = () => {
  importModalVisible.value = true
}

/**
 * 新建专业方向
 */
const handleAddDirection = () => {
  currentDirection.value = null
  // 使用搜索表单中的企业ID（如果有）
  currentEnterpriseId.value = searchForm.enterpriseId || ''
  directionModalVisible.value = true
}

/**
 * 编辑专业方向
 */
const handleEditDirection = async (record: MajorTreeVO) => {
  try {
    const response = await majorApi.getDirectionDetail(record.id)
    currentDirection.value = response.data
    // 编辑时也需要企业ID（从方向数据中获取）
    currentEnterpriseId.value = response.data.enterpriseId || ''
    directionModalVisible.value = true
  } catch (error) {
    console.error('获取专业方向详情失败:', error)
  }
}

/**
 * 删除专业方向
 */
const handleDeleteDirection = async (record: MajorTreeVO) => {
  try {
    await majorApi.deleteDirection(record.id)
    message.success('删除成功')
    loadMajorTree()
  } catch (error) {
    console.error('删除专业方向失败:', error)
  }
}

/**
 * 切换专业方向状态
 */
const handleToggleDirectionStatus = async (record: MajorTreeVO) => {
  try {
    const newStatus = record.status === 1 ? 0 : 1
    await majorApi.updateDirectionStatus(record.id, newStatus)
    message.success(`${newStatus === 1 ? '启用' : '禁用'}成功`)
    loadMajorTree()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

// ==================== 专业操作 ====================

/**
 * 新建专业
 */
const handleAddMajor = () => {
  currentMajor.value = null
  preSelectedDirectionId.value = ''
  majorModalVisible.value = true
}

/**
 * 在指定方向下新建专业
 */
const handleAddMajorToDirection = (record: MajorTreeVO) => {
  currentMajor.value = null
  preSelectedDirectionId.value = record.id
  majorModalVisible.value = true
}

/**
 * 查看专业详情
 */
const handleViewMajor = async (record: MajorTreeVO) => {
  try {
    const response = await majorApi.getMajorDetail(record.id)
    majorDetail.value = response.data
    detailDrawerVisible.value = true
  } catch (error) {
    console.error('获取专业详情失败:', error)
  }
}

/**
 * 编辑专业
 */
const handleEditMajor = async (record: MajorTreeVO) => {
  try {
    const response = await majorApi.getMajorDetail(record.id)
    currentMajor.value = response.data
    majorModalVisible.value = true
  } catch (error) {
    console.error('获取专业详情失败:', error)
  }
}

/**
 * 删除专业
 */
const handleDeleteMajor = async (record: MajorTreeVO) => {
  try {
    await majorApi.deleteMajor(record.id)
    message.success('删除成功')
    loadMajorTree()
  } catch (error) {
    console.error('删除专业失败:', error)
  }
}

/**
 * 切换专业状态
 */
const handleToggleMajorStatus = async (record: MajorTreeVO) => {
  try {
    const newStatus = record.status === 1 ? 0 : 1
    await majorApi.updateMajorStatus(record.id, newStatus)
    message.success(`${newStatus === 1 ? '启用' : '禁用'}成功`)
    loadMajorTree()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

// 页面加载
onMounted(() => {
  loadEnterpriseOptions()
  loadMajorTree()
})
</script>

<style scoped lang="scss">
.major-list {
  padding: 24px;

  &__card {
    background-color: #fff;
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  &__title {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #000;
  }

  &__search {
    padding: 16px 0;
    border-top: 1px solid #f0f0f0;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 16px;
  }

  &__empty {
    padding: 60px 0;
  }
}

// 节点样式
.node-enterprise {
  font-weight: 700;
  font-size: 15px;
  color: #722ed1;
}

.node-direction {
  font-weight: 600;
  color: #1890ff;
}

.node-major {
  color: #52c41a;
  padding-left: 8px;
}

.icon-enterprise {
  margin-right: 8px;
  color: #722ed1;
  font-size: 16px;
}

.icon-direction {
  margin-right: 8px;
  color: #1890ff;
}

.icon-major {
  margin-right: 8px;
  color: #52c41a;
}
</style>
