<template>
  <div class="enterprise-list">
    <a-card class="enterprise-list__card">
      <!-- 页面标题和操作按钮 -->
      <div class="enterprise-list__header">
        <h3 class="enterprise-list__title">企业管理</h3>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新建企业
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
      <div class="enterprise-list__search">
        <a-form layout="inline" :model="searchForm">
          <a-form-item label="企业名称">
            <a-input
              v-model:value="searchForm.enterpriseName"
              placeholder="请输入企业名称"
              allow-clear
              style="width: 180px"
              @press-enter="handleSearch"
            />
          </a-form-item>

          <a-form-item label="企业编码">
            <a-input
              v-model:value="searchForm.enterpriseCode"
              placeholder="请输入企业编码"
              allow-clear
              style="width: 180px"
              @press-enter="handleSearch"
            />
          </a-form-item>

          <a-form-item label="状态">
            <a-select
              v-model:value="searchForm.enterpriseStatus"
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
        row-key="enterpriseId"
        @change="handleTableChange"
      >
        <!-- 状态列 -->
        <template #status="{ record }">
          <a-tag :color="ENTERPRISE_STATUS_COLORS[record.enterpriseStatus]">
            {{ ENTERPRISE_STATUS_LABELS[record.enterpriseStatus] }}
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
              title="确定要删除该企业吗？"
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
              {{ record.enterpriseStatus === 1 ? '禁用' : '启用' }}
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 企业表单弹窗 -->
    <EnterpriseFormModal
      v-model:open="formModalVisible"
      :enterprise-data="currentEnterprise"
      @success="loadEnterpriseList"
    />

    <!-- 企业详情抽屉 -->
    <a-drawer
      v-model:open="detailDrawerVisible"
      title="企业详情"
      width="600"
      :body-style="{ paddingBottom: '80px' }"
    >
      <a-descriptions bordered :column="1" v-if="currentEnterprise">
        <a-descriptions-item label="企业名称">
          {{ currentEnterprise.enterpriseName }}
        </a-descriptions-item>
        <a-descriptions-item label="企业编码">
          {{ currentEnterprise.enterpriseCode || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="联系人">
          {{ currentEnterprise.contactPerson || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="联系电话">
          {{ currentEnterprise.contactPhone || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="联系邮箱">
          {{ currentEnterprise.contactEmail || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="企业地址">
          {{ currentEnterprise.address || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="企业简介">
          {{ currentEnterprise.description || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="ENTERPRISE_STATUS_COLORS[currentEnterprise.enterpriseStatus]">
            {{ ENTERPRISE_STATUS_LABELS[currentEnterprise.enterpriseStatus] }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ currentEnterprise.createTime }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ currentEnterprise.updateTime || '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
/**
 * 企业列表页面
 * @description 企业信息管理，支持 CRUD 操作
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
import { enterpriseApi } from '@/api/enterprise'
import type { EnterpriseVO, EnterpriseQueryVO } from '@/types/enterprise'
import { ENTERPRISE_STATUS_LABELS, ENTERPRISE_STATUS_COLORS } from '@/types/enterprise'
import EnterpriseFormModal from '@/components/enterprise/EnterpriseFormModal.vue'

defineOptions({
  name: 'EnterpriseList'
})

// 搜索表单
const searchForm = reactive<EnterpriseQueryVO>({
  enterpriseName: undefined,
  enterpriseCode: undefined,
  enterpriseStatus: undefined,
  pageNum: 1,
  pageSize: 10
})

// 数据列表
const dataSource = ref<EnterpriseVO[]>([])
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
    title: '企业名称',
    dataIndex: 'enterpriseName',
    key: 'enterpriseName',
    width: 200
  },
  {
    title: '企业编码',
    dataIndex: 'enterpriseCode',
    key: 'enterpriseCode',
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
    width: 120
  },
  {
    title: '联系邮箱',
    dataIndex: 'contactEmail',
    key: 'contactEmail',
    width: 180
  },
  {
    title: '状态',
    dataIndex: 'enterpriseStatus',
    key: 'enterpriseStatus',
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
const currentEnterprise = ref<EnterpriseVO | null>(null)

/**
 * 加载企业列表
 */
const loadEnterpriseList = async () => {
  loading.value = true
  try {
    const response = await enterpriseApi.getEnterpriseList({
      ...searchForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    })
    dataSource.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    message.error('加载企业列表失败')
    console.error('加载企业列表失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.current = 1
  loadEnterpriseList()
}

/**
 * 重置
 */
const handleReset = () => {
  searchForm.enterpriseName = undefined
  searchForm.enterpriseCode = undefined
  searchForm.enterpriseStatus = undefined
  pagination.current = 1
  loadEnterpriseList()
}

/**
 * 表格变化处理
 */
const handleTableChange: TableProps['onChange'] = (pag) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  loadEnterpriseList()
}

/**
 * 新建企业
 */
const handleAdd = () => {
  currentEnterprise.value = null
  formModalVisible.value = true
}

/**
 * 查看企业详情
 * @param record - 企业记录
 */
const handleView = (record: EnterpriseVO) => {
  currentEnterprise.value = record
  detailDrawerVisible.value = true
}

/**
 * 编辑企业
 * @param record - 企业记录
 */
const handleEdit = (record: EnterpriseVO) => {
  currentEnterprise.value = record
  formModalVisible.value = true
}

/**
 * 删除企业
 * @param record - 企业记录
 */
const handleDelete = async (record: EnterpriseVO) => {
  try {
    await enterpriseApi.deleteEnterprise(record.enterpriseId)
    message.success('删除成功')
    loadEnterpriseList()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

/**
 * 批量删除企业
 */
const handleBatchDelete = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要删除的企业')
    return
  }

  Modal.confirm({
    title: '批量删除确认',
    content: `确定要删除已选中的 ${selectedRowKeys.value.length} 个企业吗？此操作不可恢复。`,
    okText: '确定删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        // 依次删除选中的企业
        for (const enterpriseId of selectedRowKeys.value) {
          await enterpriseApi.deleteEnterprise(enterpriseId)
        }
        message.success(`成功删除 ${selectedRowKeys.value.length} 个企业`)
        selectedRowKeys.value = []
        loadEnterpriseList()
      } catch (error) {
        console.error('批量删除失败:', error)
      }
    }
  })
}

/**
 * 切换企业状态
 * @param record - 企业记录
 */
const handleToggleStatus = async (record: EnterpriseVO) => {
  try {
    const newStatus = record.enterpriseStatus === 1 ? 0 : 1
    await enterpriseApi.updateEnterpriseStatus(record.enterpriseId, newStatus)
    message.success(`${newStatus === 1 ? '启用' : '禁用'}成功`)
    loadEnterpriseList()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

// 页面加载
onMounted(() => {
  loadEnterpriseList()
})
</script>

<style scoped lang="scss">
.enterprise-list {
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
}
</style>
