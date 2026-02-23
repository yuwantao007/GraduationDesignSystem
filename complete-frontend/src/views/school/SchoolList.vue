<template>
  <div class="school-list">
    <a-card class="school-list__card">
      <!-- 页面标题和操作按钮 -->
      <div class="school-list__header">
        <h3 class="school-list__title">学校管理</h3>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新建学校
          </a-button>
          <a-button
            danger
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDelete"
          >
            <template #icon><DeleteOutlined /></template>
            批量删除
          </a-button>
        </a-space>
      </div>

      <!-- 搜索区域 -->
      <div class="school-list__search">
        <a-form layout="inline" :model="searchForm">
          <a-form-item label="学校名称">
            <a-input
              v-model:value="searchForm.schoolName"
              placeholder="请输入学校名称"
              allow-clear
              style="width: 180px"
              @press-enter="handleSearch"
            />
          </a-form-item>

          <a-form-item label="学校编码">
            <a-input
              v-model:value="searchForm.schoolCode"
              placeholder="请输入学校编码"
              allow-clear
              style="width: 180px"
              @press-enter="handleSearch"
            />
          </a-form-item>

          <a-form-item label="状态">
            <a-select
              v-model:value="searchForm.schoolStatus"
              placeholder="请选择状态"
              allow-clear
              style="width: 120px"
            >
              <a-select-option :value="1">正常</a-select-option>
              <a-select-option :value="0">禁用</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" @click="handleSearch">
                <template #icon><SearchOutlined /></template>
                搜索
              </a-button>
              <a-button @click="handleReset">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>

      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        row-key="schoolId"
        @change="handleTableChange"
      >
        <!-- 状态列 -->
        <template #status="{ record }">
          <a-tag :color="SCHOOL_STATUS_COLORS[record.schoolStatus]">
            {{ SCHOOL_STATUS_LABELS[record.schoolStatus] }}
          </a-tag>
        </template>

        <!-- 操作列 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="handleView(record)">
              <template #icon><EyeOutlined /></template>
              查看
            </a-button>
            <a-button type="link" size="small" @click="handleEdit(record)">
              <template #icon><EditOutlined /></template>
              编辑
            </a-button>
            <a-popconfirm
              title="确定要删除该学校吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDelete(record)"
            >
              <a-button type="link" size="small" danger>
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
            </a-popconfirm>
            <a-button
              type="link"
              size="small"
              @click="handleToggleStatus(record)"
            >
              {{ record.schoolStatus === 1 ? '禁用' : '启用' }}
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 学校表单弹窗 -->
    <SchoolFormModal
      v-model:open="formModalVisible"
      :school-data="currentSchool"
      @success="loadSchoolList"
    />

    <!-- 学校详情抽屉 -->
    <a-drawer
      v-model:open="detailDrawerVisible"
      title="学校详情"
      width="600"
      :body-style="{ paddingBottom: '80px' }"
    >
      <a-descriptions bordered :column="1" v-if="currentSchool">
        <a-descriptions-item label="学校名称">
          {{ currentSchool.schoolName }}
        </a-descriptions-item>
        <a-descriptions-item label="学校编码">
          {{ currentSchool.schoolCode || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="联系人">
          {{ currentSchool.contactPerson || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="联系电话">
          {{ currentSchool.contactPhone || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="学校邮箱">
          {{ currentSchool.schoolEmail || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="详细地址">
          {{ currentSchool.address || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="学校简介">
          {{ currentSchool.description || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="SCHOOL_STATUS_COLORS[currentSchool.schoolStatus]">
            {{ SCHOOL_STATUS_LABELS[currentSchool.schoolStatus] }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ currentSchool.createTime }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ currentSchool.updateTime || '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
/**
 * 学校列表页面
 * @description 学校信息管理，支持 CRUD 操作
 * @author YuWan
 * @date 2026-02-22
 */
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { TableColumnsType, TableProps } from 'ant-design-vue'
import {
  PlusOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  EyeOutlined,
  EditOutlined
} from '@ant-design/icons-vue'
import { schoolApi } from '@/api/school'
import type { SchoolVO, SchoolQueryVO } from '@/types/school'
import { SCHOOL_STATUS_LABELS, SCHOOL_STATUS_COLORS } from '@/types/school'
import SchoolFormModal from '@/components/school/SchoolFormModal.vue'

defineOptions({
  name: 'SchoolList'
})

// 搜索表单
const searchForm = reactive<SchoolQueryVO>({
  schoolName: undefined,
  schoolCode: undefined,
  schoolStatus: undefined,
  pageNum: 1,
  pageSize: 10
})

// 数据列表
const dataSource = ref<SchoolVO[]>([])
const loading = ref(false)

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

// 表格列配置
const columns: TableColumnsType = [
  {
    title: '学校名称',
    dataIndex: 'schoolName',
    key: 'schoolName',
    width: 200
  },
  {
    title: '学校编码',
    dataIndex: 'schoolCode',
    key: 'schoolCode',
    width: 150
  },
  {
    title: '联系人',
    dataIndex: 'contactPerson',
    key: 'contactPerson',
    width: 120
  },
  {
    title: '联系电话',
    dataIndex: 'contactPhone',
    key: 'contactPhone',
    width: 140
  },
  {
    title: '学校邮箱',
    dataIndex: 'schoolEmail',
    key: 'schoolEmail',
    width: 180
  },
  {
    title: '状态',
    dataIndex: 'schoolStatus',
    key: 'schoolStatus',
    width: 100,
    slots: { customRender: 'status' }
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
    width: 280,
    slots: { customRender: 'action' }
  }
]

// 行选择
const selectedRowKeys = ref<string[]>([])
const rowSelection = reactive({
  selectedRowKeys,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  }
})

// 弹窗控制
const formModalVisible = ref(false)
const detailDrawerVisible = ref(false)
const currentSchool = ref<SchoolVO | null>(null)

/**
 * 加载学校列表
 */
const loadSchoolList = async () => {
  loading.value = true
  try {
    const response = await schoolApi.getSchoolList({
      ...searchForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    })
    dataSource.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    message.error('加载学校列表失败')
    console.error('加载学校列表失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.current = 1
  loadSchoolList()
}

/**
 * 重置
 */
const handleReset = () => {
  searchForm.schoolName = undefined
  searchForm.schoolCode = undefined
  searchForm.schoolStatus = undefined
  pagination.current = 1
  loadSchoolList()
}

/**
 * 表格变化处理
 */
const handleTableChange: TableProps['onChange'] = (pag) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  loadSchoolList()
}

/**
 * 新建学校
 */
const handleAdd = () => {
  currentSchool.value = null
  formModalVisible.value = true
}

/**
 * 查看学校详情
 */
const handleView = (record: SchoolVO) => {
  currentSchool.value = record
  detailDrawerVisible.value = true
}

/**
 * 编辑学校
 */
const handleEdit = (record: SchoolVO) => {
  currentSchool.value = record
  formModalVisible.value = true
}

/**
 * 删除学校
 */
const handleDelete = async (record: SchoolVO) => {
  try {
    await schoolApi.deleteSchool(record.schoolId)
    message.success('删除成功')
    loadSchoolList()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

/**
 * 批量删除
 */
const handleBatchDelete = () => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个学校吗？`,
    okText: '确定',
    cancelText: '取消',
    async onOk() {
      try {
        // 依次删除选中的学校
        for (const id of selectedRowKeys.value) {
          await schoolApi.deleteSchool(id)
        }
        message.success('批量删除成功')
        selectedRowKeys.value = []
        loadSchoolList()
      } catch (error) {
        console.error('批量删除失败:', error)
      }
    }
  })
}

/**
 * 切换学校状态
 */
const handleToggleStatus = async (record: SchoolVO) => {
  const newStatus = record.schoolStatus === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'
  
  Modal.confirm({
    title: '确认操作',
    content: `确定要${statusText}学校"${record.schoolName}"吗？`,
    okText: '确定',
    cancelText: '取消',
    async onOk() {
      try {
        await schoolApi.updateSchoolStatus(record.schoolId, newStatus)
        message.success(`${statusText}成功`)
        loadSchoolList()
      } catch (error) {
        console.error('状态更新失败:', error)
      }
    }
  })
}

// 页面加载时获取数据
onMounted(() => {
  loadSchoolList()
})
</script>

<style scoped lang="scss">
.school-list {
  &__card {
    min-height: calc(100vh - 200px);
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
  }

  &__search {
    margin-bottom: 16px;
    padding: 16px;
    background-color: #fafafa;
    border-radius: 4px;
  }
}
</style>
