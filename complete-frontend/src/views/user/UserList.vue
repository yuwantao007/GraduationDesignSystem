<template>
  <div class="user-list">
    <a-card class="user-list__card">
      <!-- 页面标题和操作按钮 -->
      <div class="user-list__header">
        <h3 class="user-list__title">用户管理</h3>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新建用户
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
      <div class="user-list__search">
        <a-form layout="inline" :model="searchForm">
          <a-form-item label="用户名">
            <a-input
              v-model:value="searchForm.username"
              placeholder="请输入用户名"
              allow-clear
              style="width: 180px"
              @press-enter="handleSearch"
            />
          </a-form-item>

          <a-form-item label="真实姓名">
            <a-input
              v-model:value="searchForm.realName"
              placeholder="请输入真实姓名"
              allow-clear
              style="width: 180px"
              @press-enter="handleSearch"
            />
          </a-form-item>

          <a-form-item label="手机号">
            <a-input
              v-model:value="searchForm.userPhone"
              placeholder="请输入手机号"
              allow-clear
              style="width: 180px"
              @press-enter="handleSearch"
            />
          </a-form-item>

          <a-form-item label="角色">
            <a-select
              v-model:value="searchForm.roleCode"
              placeholder="请选择角色"
              allow-clear
              style="width: 160px"
              :options="roleFilterOptions"
            />
          </a-form-item>

          <a-form-item label="状态">
            <a-select
              v-model:value="searchForm.userStatus"
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
        row-key="userId"
        :scroll="{ x: 1400 }"
        size="middle"
        @change="handleTableChange"
      >
        <!-- 用户信息列 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'userInfo'">
            <div class="user-list__user-info">
              <a-avatar :size="36" :style="{ backgroundColor: getAvatarColor(record.username) }">
                {{ record.realName?.charAt(0) || 'U' }}
              </a-avatar>
              <div class="user-list__user-info-text">
                <span class="user-list__user-name">{{ record.realName }}</span>
                <span class="user-list__user-account">@{{ record.username }}</span>
              </div>
            </div>
          </template>

          <!-- 角色列 -->
          <template v-else-if="column.key === 'roles'">
            <template v-if="record.roles && record.roles.length > 0">
              <a-tag
                v-for="role in record.roles.slice(0, 2)"
                :key="role.roleId"
                :color="getRoleColor(role.roleCode)"
                style="margin-bottom: 2px"
              >
                {{ role.roleName }}
              </a-tag>
              <a-tooltip
                v-if="record.roles.length > 2"
                :title="record.roles.slice(2).map((r: any) => r.roleName).join('、')"
              >
                <a-tag>+{{ record.roles.length - 2 }}</a-tag>
              </a-tooltip>
            </template>
            <span v-else class="user-list__empty-text">-</span>
          </template>

          <!-- 状态列 -->
          <template v-else-if="column.key === 'userStatus'">
            <a-switch
              :checked="record.userStatus === 1"
              checked-children="正常"
              un-checked-children="禁用"
              @change="(checked: boolean) => handleStatusChange(record, checked)"
            />
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-tooltip title="查看详情">
                <a-button type="link" size="small" @click="handleView(record)">
                  <template #icon><EyeOutlined /></template>
                </a-button>
              </a-tooltip>
              <a-tooltip title="编辑">
                <a-button type="link" size="small" @click="handleEdit(record)">
                  <template #icon><EditOutlined /></template>
                </a-button>
              </a-tooltip>
              <a-tooltip title="重置密码">
                <a-popconfirm
                  title="确定要重置该用户密码吗？"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="handleResetPassword(record)"
                >
                  <a-button type="link" size="small">
                    <template #icon><KeyOutlined /></template>
                  </a-button>
                </a-popconfirm>
              </a-tooltip>
              <a-tooltip title="删除">
                <a-popconfirm
                  title="确定要删除该用户吗？此操作不可恢复。"
                  ok-text="确定"
                  cancel-text="取消"
                  ok-type="danger"
                  @confirm="handleDelete(record)"
                >
                  <a-button type="link" size="small" danger>
                    <template #icon><DeleteOutlined /></template>
                  </a-button>
                </a-popconfirm>
              </a-tooltip>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新建/编辑用户弹窗 -->
    <UserFormModal
      v-model:open="formModalVisible"
      :user-data="currentUser"
      @success="handleFormSuccess"
    />

    <!-- 用户详情抽屉 -->
    <UserDetailDrawer
      v-model:open="detailDrawerVisible"
      :user-id="currentUserId"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * 用户管理列表页面
 * @description 用户列表展示、搜索、CRUD 操作、状态管理、批量删除等功能
 * @author YuWan
 * @date 2026-02-21
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  DeleteOutlined,
  EyeOutlined,
  EditOutlined,
  KeyOutlined
} from '@ant-design/icons-vue'
import { userApi } from '@/api/user'
import type { UserVO, UserQueryVO } from '@/types/user'
import { USER_ROLE_COLORS, USER_ROLE_LABELS } from '@/types/user'
import UserFormModal from '@/components/user/UserFormModal.vue'
import UserDetailDrawer from '@/components/user/UserDetailDrawer.vue'

defineOptions({
  name: 'UserList'
})

// ======================== 搜索相关 ========================

// 搜索表单
const searchForm = reactive<UserQueryVO>({
  username: '',
  realName: '',
  userPhone: '',
  roleCode: undefined,
  userStatus: undefined,
  pageNum: 1,
  pageSize: 10
})

// 角色筛选选项
const roleFilterOptions = computed(() => {
  return Object.entries(USER_ROLE_LABELS).map(([value, label]) => ({
    label,
    value
  }))
})

// ======================== 表格相关 ========================

// 表格列配置
const columns = [
  {
    title: '用户信息',
    key: 'userInfo',
    width: 220,
    fixed: 'left' as const
  },
  {
    title: '邮箱',
    dataIndex: 'userEmail',
    key: 'userEmail',
    width: 200,
    ellipsis: true
  },
  {
    title: '手机号',
    dataIndex: 'userPhone',
    key: 'userPhone',
    width: 140
  },
  {
    title: '角色',
    key: 'roles',
    width: 220
  },
  {
    title: '状态',
    key: 'userStatus',
    width: 100,
    align: 'center' as const
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
    sorter: true
  },
  {
    title: '最后登录',
    dataIndex: 'lastLoginTime',
    key: 'lastLoginTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    fixed: 'right' as const,
    width: 180,
    align: 'center' as const
  }
]

// 表格数据源
const dataSource = ref<UserVO[]>([])

// 加载状态
const loading = ref(false)

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  pageSizeOptions: ['10', '20', '50', '100'],
  showTotal: (total: number) => `共 ${total} 条记录`
})

// 选中行 key 列表
const selectedRowKeys = ref<string[]>([])

// 行选择配置
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  }
}))

// ======================== 弹窗/抽屉状态 ========================

// 新建/编辑弹窗
const formModalVisible = ref(false)
const currentUser = ref<UserVO | null>(null)

// 详情抽屉
const detailDrawerVisible = ref(false)
const currentUserId = ref('')

// ======================== 颜色工具 ========================

/**
 * 根据用户名生成头像背景色
 * @param username - 用户名
 */
const getAvatarColor = (username: string): string => {
  const colors = ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2', '#eb2f96']
  let hash = 0
  for (let i = 0; i < username.length; i++) {
    hash = username.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length] || '#1890ff'
}

/**
 * 获取角色标签颜色
 * @param roleCode - 角色代码
 */
const getRoleColor = (roleCode: string): string => {
  return USER_ROLE_COLORS[roleCode] || 'default'
}

// ======================== 数据加载 ========================

/**
 * 加载用户列表数据
 */
const loadUserList = async () => {
  loading.value = true
  try {
    const response = await userApi.getUserList({
      ...searchForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    })

    dataSource.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    message.error('加载用户列表失败')
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// ======================== 搜索操作 ========================

/**
 * 执行搜索
 */
const handleSearch = () => {
  pagination.current = 1
  selectedRowKeys.value = []
  loadUserList()
}

/**
 * 重置搜索条件
 */
const handleReset = () => {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.userPhone = ''
  searchForm.roleCode = undefined
  searchForm.userStatus = undefined
  pagination.current = 1
  selectedRowKeys.value = []
  loadUserList()
}

// ======================== 表格事件 ========================

/**
 * 表格分页、排序改变
 * @param pag - 分页信息
 */
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadUserList()
}

// ======================== CRUD 操作 ========================

/**
 * 新建用户
 */
const handleAdd = () => {
  currentUser.value = null
  formModalVisible.value = true
}

/**
 * 查看用户详情
 * @param record - 用户记录
 */
const handleView = (record: UserVO) => {
  currentUserId.value = record.userId
  detailDrawerVisible.value = true
}

/**
 * 编辑用户
 * @param record - 用户记录
 */
const handleEdit = (record: UserVO) => {
  currentUser.value = record
  formModalVisible.value = true
}

/**
 * 删除用户
 * @param record - 用户记录
 */
const handleDelete = async (record: UserVO) => {
  try {
    await userApi.deleteUser(record.userId)
    message.success('删除成功')
    loadUserList()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

/**
 * 批量删除用户
 */
const handleBatchDelete = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要删除的用户')
    return
  }

  Modal.confirm({
    title: '批量删除确认',
    content: `确定要删除已选中的 ${selectedRowKeys.value.length} 个用户吗？此操作不可恢复。`,
    okText: '确定删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await userApi.batchDeleteUsers(selectedRowKeys.value)
        message.success(`成功删除 ${selectedRowKeys.value.length} 个用户`)
        selectedRowKeys.value = []
        loadUserList()
      } catch (error) {
        console.error('批量删除失败:', error)
      }
    }
  })
}

/**
 * 切换用户状态
 * @param record - 用户记录
 * @param checked - 是否启用
 */
const handleStatusChange = async (record: UserVO, checked: boolean) => {
  const newStatus = checked ? 1 : 0
  const statusText = checked ? '启用' : '禁用'

  try {
    await userApi.updateUserStatus(record.userId, newStatus)
    record.userStatus = newStatus
    message.success(`已${statusText}用户 ${record.realName}`)
  } catch (error) {
    console.error('状态更新失败:', error)
  }
}

/**
 * 重置用户密码
 * @param record - 用户记录
 */
const handleResetPassword = async (record: UserVO) => {
  try {
    await userApi.resetPassword(record.userId)
    message.success(`用户 ${record.realName} 的密码已重置为默认密码`)
  } catch (error) {
    console.error('重置密码失败:', error)
  }
}

/**
 * 表单操作成功回调（新建/编辑）
 */
const handleFormSuccess = () => {
  loadUserList()
}

// ======================== 生命周期 ========================

onMounted(() => {
  loadUserList()
})
</script>

<style scoped lang="scss">
.user-list {
  &__card {
    border-radius: 8px;
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  &__title {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #262626;
  }

  &__search {
    margin-bottom: 20px;
    padding: 16px 20px;
    background-color: #fafafa;
    border-radius: 6px;
  }

  &__user-info {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  &__user-info-text {
    display: flex;
    flex-direction: column;
    line-height: 1.4;
  }

  &__user-name {
    font-size: 14px;
    font-weight: 500;
    color: #262626;
  }

  &__user-account {
    font-size: 12px;
    color: #8c8c8c;
  }

  &__empty-text {
    color: #bfbfbf;
  }
}
</style>
