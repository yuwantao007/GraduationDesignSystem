<template>
  <div class="role-list">
    <a-card class="role-list__card">
      <!-- 页面标题和操作按钮 -->
      <div class="role-list__header">
        <h3 class="role-list__title">角色权限管理</h3>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新建角色
        </a-button>
      </div>

      <!-- 搜索区域 -->
      <div class="role-list__search">
        <a-form layout="inline" :model="searchForm">
          <a-form-item label="角色名称">
            <a-input
              v-model:value="searchForm.roleName"
              placeholder="请输入角色名称"
              allow-clear
              style="width: 200px"
              @press-enter="handleSearch"
            />
          </a-form-item>

          <a-form-item label="角色代码">
            <a-input
              v-model:value="searchForm.roleCode"
              placeholder="请输入角色代码"
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
              <a-select-option :value="1">启用</a-select-option>
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

      <!-- 角色列表 -->
      <a-row :gutter="[16, 16]">
        <a-col
          v-for="role in roleList"
          :key="role.roleId"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="8"
          :xl="6"
        >
          <a-card
            hoverable
            class="role-list__role-card"
            :class="{ 'role-list__role-card--disabled': role.status === 0 }"
          >
            <template #actions>
              <a-tooltip title="编辑">
                <EditOutlined @click="handleEdit(role)" />
              </a-tooltip>
              <a-tooltip title="权限配置">
                <SafetyCertificateOutlined @click="handlePermission(role)" />
              </a-tooltip>
              <a-tooltip title="删除">
                <a-popconfirm
                  title="确定要删除该角色吗？"
                  ok-text="确定"
                  cancel-text="取消"
                  ok-type="danger"
                  @confirm="handleDelete(role)"
                >
                  <DeleteOutlined style="color: #ff4d4f" />
                </a-popconfirm>
              </a-tooltip>
            </template>

            <a-card-meta>
              <template #title>
                <div class="role-list__role-title">
                  <span>{{ role.roleName }}</span>
                  <a-badge :status="role.status === 1 ? 'success' : 'error'" />
                </div>
              </template>
              <template #description>
                <div class="role-list__role-meta">
                  <a-tag color="blue" class="role-list__role-code">{{ role.roleCode }}</a-tag>
                  <p class="role-list__role-desc">{{ role.roleDesc || '暂无描述' }}</p>
                  <p class="role-list__role-time">
                    <ClockCircleOutlined />
                    <span>{{ role.createTime || '-' }}</span>
                  </p>
                </div>
              </template>
              <template #avatar>
                <a-avatar
                  :size="48"
                  :style="{
                    backgroundColor: getRoleCardColor(role.roleCode),
                    fontSize: '20px'
                  }"
                >
                  {{ role.roleName?.charAt(0) || 'R' }}
                </a-avatar>
              </template>
            </a-card-meta>
          </a-card>
        </a-col>

        <!-- 空状态 -->
        <a-col v-if="!loading && roleList.length === 0" :span="24">
          <a-empty description="暂无角色数据" />
        </a-col>
      </a-row>

      <!-- 加载状态 -->
      <div v-if="loading" class="role-list__loading">
        <a-spin size="large" />
      </div>

      <!-- 分页 -->
      <div v-if="roleList.length > 0" class="role-list__pagination">
        <a-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :show-total="(total: number) => `共 ${total} 条记录`"
          show-size-changer
          show-quick-jumper
          @change="handlePageChange"
        />
      </div>
    </a-card>

    <!-- 新建/编辑角色弹窗 -->
    <a-modal
      :open="formVisible"
      :title="isEditMode ? '编辑角色' : '新建角色'"
      :width="520"
      :confirm-loading="formLoading"
      :mask-closable="false"
      @ok="handleFormSubmit"
      @cancel="handleFormCancel"
    >
      <a-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleFormRules"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 17 }"
        style="padding-top: 16px"
      >
        <a-form-item label="角色名称" name="roleName">
          <a-input
            v-model:value="roleForm.roleName"
            placeholder="请输入角色名称"
            :maxlength="50"
          />
        </a-form-item>

        <a-form-item label="角色代码" name="roleCode">
          <a-input
            v-model:value="roleForm.roleCode"
            :disabled="isEditMode"
            placeholder="如：SYSTEM_ADMIN"
            :maxlength="50"
          />
        </a-form-item>

        <a-form-item label="角色描述" name="description">
          <a-textarea
            v-model:value="roleForm.description"
            placeholder="请输入角色描述信息"
            :rows="3"
            :maxlength="200"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 权限配置弹窗 -->
    <a-modal
      :open="permissionVisible"
      title="权限配置"
      :width="560"
      :confirm-loading="permissionLoading"
      :mask-closable="false"
      @ok="handlePermissionSubmit"
      @cancel="permissionVisible = false"
    >
      <div class="role-list__permission-header">
        <span>为角色 <a-tag color="blue">{{ currentRole?.roleName }}</a-tag> 配置权限</span>
      </div>
      <a-tree
        v-model:checkedKeys="checkedPermissionIds"
        :tree-data="permissionTree"
        :field-names="{ title: 'permissionName', key: 'permissionId', children: 'children' }"
        checkable
        default-expand-all
        :selectable="false"
        class="role-list__permission-tree"
      />
      <a-empty
        v-if="permissionTree.length === 0 && !permissionLoading"
        description="暂无权限数据"
        :image-style="{ height: '60px' }"
      />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
/**
 * 角色权限管理页面
 * @description 角色 CRUD、权限配置、角色状态管理
 * @author YuWan
 * @date 2026-02-21
 */
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  EditOutlined,
  DeleteOutlined,
  SafetyCertificateOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue'
import { roleApi } from '@/api/role'
import type { RoleInfo, PermissionInfo, RoleQueryVO, CreateRoleDTO, UpdateRoleDTO } from '@/types/user'
import { USER_ROLE_COLORS } from '@/types/user'

defineOptions({
  name: 'RoleList'
})

// ======================== 搜索相关 ========================

const searchForm = reactive<RoleQueryVO>({
  roleName: '',
  roleCode: '',
  status: undefined,
  pageNum: 1,
  pageSize: 12
})

// ======================== 列表数据 ========================

const roleList = ref<RoleInfo[]>([])
const loading = ref(false)

const pagination = reactive({
  current: 1,
  pageSize: 12,
  total: 0
})

// ======================== 表单弹窗 ========================

const formVisible = ref(false)
const formLoading = ref(false)
const isEditMode = ref(false)
const currentRole = ref<RoleInfo | null>(null)
const roleFormRef = ref<FormInstance>()

const roleForm = reactive<CreateRoleDTO>({
  roleName: '',
  roleCode: '',
  description: ''
})

// 角色代码格式校验
const validateRoleCode = async (_rule: Rule, value: string) => {
  if (value && !/^[A-Z][A-Z_]*[A-Z]$/.test(value)) {
    return Promise.reject('角色代码应为大写字母和下划线组成')
  }
  return Promise.resolve()
}

const roleFormRules: Record<string, Rule[]> = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '角色名称长度为2-50个字符', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色代码', trigger: 'blur' },
    { validator: validateRoleCode, trigger: 'blur' }
  ]
}

// ======================== 权限配置弹窗 ========================

const permissionVisible = ref(false)
const permissionLoading = ref(false)
const permissionTree = ref<PermissionInfo[]>([])
const checkedPermissionIds = ref<string[]>([])

// ======================== 工具方法 ========================

/**
 * 获取角色卡片颜色
 * @param roleCode - 角色代码
 */
const getRoleCardColor = (roleCode: string): string => {
  const colorMap: Record<string, string> = {
    SYSTEM_ADMIN: '#f5222d',
    SUPERVISOR_TEACHER: '#722ed1',
    UNIVERSITY_TEACHER: '#1890ff',
    ENTERPRISE_LEADER: '#fa8c16',
    MAJOR_DIRECTOR: '#13c2c2',
    ENTERPRISE_TEACHER: '#52c41a',
    STUDENT: '#2f54eb'
  }
  return colorMap[roleCode] || '#8c8c8c'
}

// ======================== 数据加载 ========================

/**
 * 加载角色列表
 */
const loadRoleList = async () => {
  loading.value = true
  try {
    const response = await roleApi.getRoleList({
      ...searchForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    })
    // 后端返回的是数组，需要适配为前端分页格式
    let allRoles = response.data || []
    
    // 前端实现搜索过滤（因为后端不支持搜索参数）
    if (searchForm.roleName) {
      allRoles = allRoles.filter(role => 
        role.roleName?.includes(searchForm.roleName!)
      )
    }
    if (searchForm.roleCode) {
      allRoles = allRoles.filter(role => 
        role.roleCode?.includes(searchForm.roleCode!)
      )
    }
    if (searchForm.status !== undefined && searchForm.status !== null) {
      allRoles = allRoles.filter(role => 
        role.status === searchForm.status
      )
    }
    
    // 前端实现分页逻辑（因为后端返回的是全量数据）
    const startIndex = (pagination.current - 1) * pagination.pageSize
    const endIndex = startIndex + pagination.pageSize
    roleList.value = allRoles.slice(startIndex, endIndex)
    pagination.total = allRoles.length
  } catch (error) {
    message.error('加载角色列表失败')
    console.error('加载角色列表失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载权限树
 */
const loadPermissionTree = async () => {
  try {
    const response = await roleApi.getAllPermissions()
    permissionTree.value = response.data || []
  } catch (error) {
    message.error('加载权限列表失败')
    console.error('加载权限列表失败:', error)
  }
}

// ======================== 搜索操作 ========================

/**
 * 执行搜索
 */
const handleSearch = () => {
  pagination.current = 1
  loadRoleList()
}

/**
 * 重置搜索
 */
const handleReset = () => {
  searchForm.roleName = ''
  searchForm.roleCode = ''
  searchForm.status = undefined
  pagination.current = 1
  loadRoleList()
}

/**
 * 分页改变
 */
const handlePageChange = (page: number, pageSize: number) => {
  pagination.current = page
  pagination.pageSize = pageSize
  loadRoleList()
}

// ======================== CRUD 操作 ========================

/**
 * 新建角色
 */
const handleAdd = () => {
  isEditMode.value = false
  currentRole.value = null
  roleForm.roleName = ''
  roleForm.roleCode = ''
  roleForm.description = ''
  roleFormRef.value?.clearValidate()
  formVisible.value = true
}

/**
 * 编辑角色
 * @param role - 角色信息
 */
const handleEdit = (role: RoleInfo) => {
  isEditMode.value = true
  currentRole.value = role
  roleForm.roleName = role.roleName
  roleForm.roleCode = role.roleCode
  roleForm.description = role.roleDesc || ''
  roleFormRef.value?.clearValidate()
  formVisible.value = true
}

/**
 * 删除角色
 * @param role - 角色信息
 */
const handleDelete = async (role: RoleInfo) => {
  try {
    await roleApi.deleteRole(role.roleId)
    message.success('角色删除成功')
    loadRoleList()
  } catch (error) {
    console.error('删除角色失败:', error)
  }
}

/**
 * 提交角色表单
 */
const handleFormSubmit = async () => {
  try {
    await roleFormRef.value?.validate()
  } catch {
    return
  }

  formLoading.value = true
  try {
    if (isEditMode.value && currentRole.value) {
      const updateData: UpdateRoleDTO = {
        roleName: roleForm.roleName,
        description: roleForm.description
      }
      await roleApi.updateRole(currentRole.value.roleId, updateData)
      message.success('角色更新成功')
    } else {
      await roleApi.createRole({
        roleName: roleForm.roleName,
        roleCode: roleForm.roleCode,
        description: roleForm.description
      })
      message.success('角色创建成功')
    }
    formVisible.value = false
    loadRoleList()
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    formLoading.value = false
  }
}

/**
 * 取消表单操作
 */
const handleFormCancel = () => {
  formVisible.value = false
  roleFormRef.value?.clearValidate()
}

// ======================== 权限配置 ========================

/**
 * 打开权限配置弹窗
 * @param role - 角色信息
 */
const handlePermission = async (role: RoleInfo) => {
  currentRole.value = role
  permissionVisible.value = true
  permissionLoading.value = true

  try {
    // 并行加载权限树和角色已有权限
    await loadPermissionTree()
    const response = await roleApi.getRolePermissions(role.roleId)
    checkedPermissionIds.value = (response.data || []).map((p: PermissionInfo) => p.permissionId)
  } catch (error) {
    console.error('加载权限数据失败:', error)
  } finally {
    permissionLoading.value = false
  }
}

/**
 * 提交权限配置
 */
const handlePermissionSubmit = async () => {
  if (!currentRole.value) return

  permissionLoading.value = true
  try {
    await roleApi.updateRolePermissions(currentRole.value.roleId, checkedPermissionIds.value)
    message.success('权限配置更新成功')
    permissionVisible.value = false
  } catch (error) {
    console.error('更新权限配置失败:', error)
  } finally {
    permissionLoading.value = false
  }
}

// ======================== 生命周期 ========================

onMounted(() => {
  loadRoleList()
})
</script>

<style scoped lang="scss">
.role-list {
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

  &__role-card {
    height: 100%;
    transition: all 0.3s ease;
    border-radius: 8px;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    &--disabled {
      opacity: 0.65;
    }
  }

  &__role-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__role-meta {
    min-height: 80px;
  }

  &__role-code {
    margin-bottom: 8px;
  }

  &__role-desc {
    margin: 8px 0;
    font-size: 13px;
    color: #8c8c8c;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    line-clamp: 2;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  &__role-time {
    margin: 0;
    font-size: 12px;
    color: #bfbfbf;
    display: flex;
    align-items: center;
    gap: 4px;
  }

  &__loading {
    display: flex;
    justify-content: center;
    padding: 48px 0;
  }

  &__pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 24px;
  }

  &__permission-header {
    margin-bottom: 16px;
    padding: 8px 12px;
    background-color: #f6f6f6;
    border-radius: 4px;
    font-size: 14px;
  }

  &__permission-tree {
    max-height: 400px;
    overflow-y: auto;
    padding: 8px;
    border: 1px solid #d9d9d9;
    border-radius: 6px;
  }
}
</style>
