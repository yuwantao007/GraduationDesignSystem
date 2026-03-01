<template>
  <div class="enterprise-overview">
    <a-card class="enterprise-overview__card">
      <!-- 页面标题 -->
      <div class="enterprise-overview__header">
        <h3 class="enterprise-overview__title">企业概览</h3>
      </div>

      <!-- 搜索区域 -->
      <div class="enterprise-overview__search">
        <a-form layout="inline" :model="searchForm">
          <a-form-item label="企业名称">
            <a-input
              v-model:value="searchForm.keyword"
              placeholder="请输入企业名称或编码"
              allow-clear
              style="width: 200px"
              @press-enter="handleSearch"
            />
          </a-form-item>

          <a-form-item label="状态">
            <a-select
              v-model:value="searchForm.status"
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
        row-key="enterpriseId"
        @change="handleTableChange"
      >
        <!-- 企业名称列 -->
        <template #enterpriseName="{ record }">
          <span style="font-weight: 500; color: #262626">{{ record.enterpriseName }}</span>
        </template>

        <!-- 状态列 -->
        <template #status="{ record }">
          <a-tag :color="ENTERPRISE_STATUS_COLORS[record.status]">
            {{ ENTERPRISE_STATUS_LABELS[record.status] }}
          </a-tag>
        </template>

        <!-- 统计数据列 -->
        <template #directionCount="{ record }">
          <a-statistic
            :value="record.directionCount"
            suffix="个"
            :value-style="{ fontSize: '14px' }"
          />
        </template>

        <template #majorCount="{ record }">
          <a-statistic
            :value="record.majorCount"
            suffix="个"
            :value-style="{ fontSize: '14px' }"
          />
        </template>

        <template #teacherCount="{ record }">
          <a-statistic
            :value="record.teacherCount"
            suffix="人"
            :value-style="{ fontSize: '14px', color: '#1890ff' }"
          />
        </template>

        <template #studentCount="{ record }">
          <a-statistic
            :value="record.studentCount"
            suffix="人"
            :value-style="{ fontSize: '14px', color: '#52c41a' }"
          />
        </template>

        <!-- 操作列 -->
        <template #action="{ record }">
          <a-button type="link" size="small" @click="handleViewDetail(record)">
            详情
          </a-button>
        </template>
      </a-table>
    </a-card>

    <!-- 详情模态框 -->
    <a-modal
      v-model:open="detailModalVisible"
      :title="`${currentEnterprise.enterpriseName} - 专业方向与专业详情`"
      width="900px"
      :footer="null"
      :bodyStyle="{ padding: '20px' }"
    >
      <div class="detail-content">
        <!-- 统计概览 -->
        <a-row :gutter="16" style="margin-bottom: 20px">
          <a-col :span="6">
            <a-statistic title="专业方向" :value="currentEnterprise.directionCount" suffix="个" />
          </a-col>
          <a-col :span="6">
            <a-statistic title="专业数量" :value="currentEnterprise.majorCount" suffix="个" />
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="教师总数"
              :value="currentEnterprise.teacherCount"
              suffix="人"
              :value-style="{ color: '#1890ff' }"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="学生总数"
              :value="currentEnterprise.studentCount"
              suffix="人"
              :value-style="{ color: '#52c41a' }"
            />
          </a-col>
        </a-row>

        <a-divider />

        <!-- 专业方向与专业表格报表 -->
        <div v-if="detailTableData.length > 0">
          <h4 style="margin-bottom: 16px">专业方向与专业列表</h4>
          <a-table
            :columns="detailColumns"
            :data-source="detailTableData"
            :pagination="false"
            :row-class-name="getRowClassName"
            row-key="key"
            bordered
            size="middle"
            :scroll="{ y: 400 }"
          >
            <!-- 专业方向列 - 支持合并单元格 -->
            <template #directionName="{ record }">
              <span v-if="record.isDirection" style="font-weight: 600; color: #1890ff; font-size: 14px">
                📁 {{ record.directionName }}
              </span>
              <span v-else style="padding-left: 24px; color: #595959">
                📄 {{ record.majorName }}
              </span>
            </template>

            <!-- 教师数列 -->
            <template #teacherCount="{ record }">
              <span v-if="record.teacherCount !== ''" style="font-weight: 500; color: #1890ff">
                {{ record.teacherCount }} 人
              </span>
            </template>

            <!-- 学生数列 -->
            <template #studentCount="{ record }">
              <span v-if="record.studentCount !== ''" style="font-weight: 500; color: #52c41a">
                {{ record.studentCount }} 人
              </span>
            </template>
          </a-table>
        </div>
        <a-empty v-else description="该企业下暂无专业方向和专业数据" :image="simpleImage" />
      </div>
    </a-modal>

    <!-- 旧的查看详情抽屉 - 保留用于其他用途或删除 -->
    <a-drawer
      v-model:open="detailDrawerVisible"
      title="企业详情"
      width="600"
      :body-style="{ paddingBottom: '80px' }"
      style="display: none"
    >
      <!-- 可以删除或保留作为备用 -->
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
/**
 * 企业概览页面
 * @description 展示企业列表及统计数据（方向数、专业数、教师数、学生数）
 * @author YuWan
 * @date 2026-02-22
 */
import { ref, reactive, onMounted } from 'vue'
import { message, Empty } from 'ant-design-vue'
import type { TableColumnsType, TableProps } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { enterpriseApi } from '@/api/enterprise'
import type { EnterpriseOverviewVO, EnterpriseQueryVO } from '@/types/enterprise'
import { ENTERPRISE_STATUS_LABELS, ENTERPRISE_STATUS_COLORS } from '@/types/enterprise'

defineOptions({
  name: 'EnterpriseOverview'
})

const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE

// 搜索表单
const searchForm = reactive<EnterpriseQueryVO>({
  keyword: undefined,
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

// 数据列表
const dataSource = ref<EnterpriseOverviewVO[]>([])
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
    width: 250,
    slots: { customRender: 'enterpriseName' }
  },
  {
    title: '方向数',
    dataIndex: 'directionCount',
    key: 'directionCount',
    width: 120,
    align: 'center',
    slots: { customRender: 'directionCount' }
  },
  {
    title: '专业数',
    dataIndex: 'majorCount',
    key: 'majorCount',
    width: 120,
    align: 'center',
    slots: { customRender: 'majorCount' }
  },
  {
    title: '教师数',
    dataIndex: 'teacherCount',
    key: 'teacherCount',
    width: 120,
    align: 'center',
    slots: { customRender: 'teacherCount' }
  },
  {
    title: '学生数',
    dataIndex: 'studentCount',
    key: 'studentCount',
    width: 120,
    align: 'center',
    slots: { customRender: 'studentCount' }
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    align: 'center',
    slots: { customRender: 'status' }
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    align: 'center',
    slots: { customRender: 'action' }
  }
]

// 当前企业详情
const currentEnterprise = ref<EnterpriseOverviewVO>({} as EnterpriseOverviewVO)
const detailDrawerVisible = ref(false)
const detailModalVisible = ref(false)

// 详情表格数据
const detailTableData = ref<any[]>([])

// 详情表格列配置
const detailColumns: TableColumnsType = [
  {
    title: '专业方向 / 专业名称',
    dataIndex: 'directionName',
    key: 'directionName',
    width: 300,
    slots: { customRender: 'directionName' }
  },
  {
    title: '教师数',
    dataIndex: 'teacherCount',
    key: 'teacherCount',
    width: 120,
    align: 'center',
    slots: { customRender: 'teacherCount' }
  },
  {
    title: '学生数',
    dataIndex: 'studentCount',
    key: 'studentCount',
    width: 120,
    align: 'center',
    slots: { customRender: 'studentCount' }
  }
]

/**
 * 加载企业概览数据
 */
const loadData = async () => {
  try {
    loading.value = true
    const params: EnterpriseQueryVO = {
      ...searchForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await enterpriseApi.getEnterpriseOverview(params)
    dataSource.value = res.data.records
    pagination.total = res.data.total
  } catch (error: any) {
    message.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

/**
 * 重置搜索
 */
const handleReset = () => {
  searchForm.keyword = undefined
  searchForm.status = undefined
  pagination.current = 1
  loadData()
}

/**
 * 表格变化处理
 */
const handleTableChange: TableProps['onChange'] = (pag) => {
  pagination.current = pag.current!
  pagination.pageSize = pag.pageSize!
  loadData()
}

/**
 * 查看企业详情
 */
const handleViewDetail = (record: EnterpriseOverviewVO) => {
  currentEnterprise.value = record
  
  // 构建详情表格数据（扁平化结构）
  const tableData: any[] = []
  let rowIndex = 0
  
  if (record.directions && record.directions.length > 0) {
    record.directions.forEach((direction) => {
      // 添加专业方向行（作为分组标题）
      tableData.push({
        key: `direction-${direction.directionId}`,
        directionName: direction.directionName,
        majorName: '',
        teacherCount: direction.teacherCount,
        studentCount: direction.studentCount,
        isDirection: true,
        rowIndex: rowIndex++
      })
      
      // 添加专业行（缩进显示）
      if (direction.majors && direction.majors.length > 0) {
        direction.majors.forEach((major) => {
          tableData.push({
            key: `major-${major.majorId}`,
            directionName: direction.directionName,
            majorName: major.majorName,
            teacherCount: '',
            studentCount: '',
            isDirection: false,
            rowIndex: rowIndex++
          })
        })
      }
    })
  }
  
  detailTableData.value = tableData
  detailModalVisible.value = true
}

/**
 * 获取表格行的样式类名
 */
const getRowClassName = (record: any) => {
  return record.isDirection ? 'direction-row' : 'major-row'
}

// 组件挂载时加载数据
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.enterprise-overview {
  padding: 20px;

  &__card {
    background: #fff;
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  &__title {
    font-size: 18px;
    font-weight: 600;
    margin: 0;
  }

  &__search {
    margin-bottom: 20px;
    padding: 16px;
    background: #fafafa;
    border-radius: 4px;
  }
}

.detail-content {
  h4 {
    color: #262626;
    font-size: 15px;
    font-weight: 600;
    margin-bottom: 16px;
  }

  :deep(.ant-table) {
    .ant-table-thead > tr > th {
      background: #fafafa;
      font-weight: 600;
    }

    // 专业方向行样式（浅蓝色背景，加粗）
    .ant-table-tbody > tr.direction-row {
      background: #e6f7ff !important;
      
      &:hover > td {
        background: #d6f0ff !important;
      }
      
      td {
        font-weight: 600;
      }
    }

    // 专业行样式（普通样式）
    .ant-table-tbody > tr.major-row {
      &:hover > td {
        background: #f5f5f5 !important;
      }
    }
  }

  :deep(.ant-statistic) {
    .ant-statistic-content {
      font-size: 24px;
      font-weight: 600;
    }
  }
}
</style>
