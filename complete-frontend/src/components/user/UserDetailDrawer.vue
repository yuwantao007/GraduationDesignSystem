<template>
  <a-drawer
    :open="open"
    title="用户详情"
    :width="600"
    placement="right"
    @close="handleClose"
  >
    <a-spin :spinning="loading">
      <div v-if="userDetail" class="user-detail">
        <!-- 基本信息头部 -->
        <div class="user-detail__header">
          <a-avatar :size="72" :style="{ backgroundColor: '#1890ff', fontSize: '28px' }">
            {{ userDetail.realName?.charAt(0) || 'U' }}
          </a-avatar>
          <div class="user-detail__header-info">
            <h3 class="user-detail__name">{{ userDetail.realName }}</h3>
            <p class="user-detail__username">@{{ userDetail.username }}</p>
            <div class="user-detail__tags">
              <a-tag
                v-for="role in userDetail.roles"
                :key="role.roleId"
                :color="getRoleColor(role.roleCode)"
              >
                {{ role.roleName }}
              </a-tag>
              <a-tag :color="userDetail.userStatus === 1 ? 'green' : 'red'">
                {{ userDetail.userStatus === 1 ? '正常' : '禁用' }}
              </a-tag>
            </div>
          </div>
        </div>

        <a-divider />

        <!-- 详细信息 -->
        <a-descriptions
          title="基本信息"
          :column="2"
          bordered
          size="middle"
          class="user-detail__descriptions"
        >
          <a-descriptions-item label="用户ID" :span="2">
            <a-typography-text copyable>{{ userDetail.userId }}</a-typography-text>
          </a-descriptions-item>
          <a-descriptions-item label="用户名">
            {{ userDetail.username }}
          </a-descriptions-item>
          <a-descriptions-item label="真实姓名">
            {{ userDetail.realName }}
          </a-descriptions-item>
          <a-descriptions-item label="性别">
            {{ getGenderLabel(userDetail.gender) }}
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-badge
              :status="userDetail.userStatus === 1 ? 'success' : 'error'"
              :text="userDetail.userStatus === 1 ? '正常' : '禁用'"
            />
          </a-descriptions-item>
          <a-descriptions-item label="邮箱" :span="2">
            {{ userDetail.userEmail || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="手机号">
            {{ userDetail.userPhone || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="学号/工号">
            {{ userDetail.userCode || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="部门" :span="2">
            {{ userDetail.department || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="专业方向" :span="2">
            {{ userDetail.major || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="简介" :span="2">
            {{ userDetail.biography || '-' }}
          </a-descriptions-item>
        </a-descriptions>

        <a-descriptions
          title="角色信息"
          :column="1"
          bordered
          size="middle"
          class="user-detail__descriptions"
          style="margin-top: 24px"
        >
          <a-descriptions-item label="拥有角色">
            <a-tag
              v-for="role in userDetail.roles"
              :key="role.roleId"
              :color="getRoleColor(role.roleCode)"
              style="margin-bottom: 4px"
            >
              {{ role.roleName }}
            </a-tag>
            <span v-if="!userDetail.roles?.length">-</span>
          </a-descriptions-item>
        </a-descriptions>

        <a-descriptions
          title="时间信息"
          :column="1"
          bordered
          size="middle"
          class="user-detail__descriptions"
          style="margin-top: 24px"
        >
          <a-descriptions-item label="创建时间">
            {{ userDetail.createTime || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="更新时间">
            {{ userDetail.updateTime || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="最后登录时间">
            {{ userDetail.lastLoginTime || '-' }}
          </a-descriptions-item>
        </a-descriptions>
      </div>

      <!-- 空状态 -->
      <a-empty v-else-if="!loading" description="未找到用户信息" />
    </a-spin>
  </a-drawer>
</template>

<script setup lang="ts">
/**
 * 用户详情抽屉组件
 * @description 以抽屉形式展示用户的完整详细信息
 * @author YuWan
 * @date 2026-02-21
 */
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { userApi } from '@/api/user'
import type { UserVO } from '@/types/user'
import { USER_ROLE_COLORS, GENDER_LABELS } from '@/types/user'

defineOptions({
  name: 'UserDetailDrawer'
})

// Props 定义
interface Props {
  /** 抽屉是否显示 */
  open: boolean
  /** 用户ID */
  userId?: string
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  userId: ''
})

// Emits 定义
const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

// 加载状态
const loading = ref(false)

// 用户详情数据
const userDetail = ref<UserVO | null>(null)

/**
 * 获取角色颜色
 * @param roleCode - 角色代码
 */
const getRoleColor = (roleCode: string): string => {
  return USER_ROLE_COLORS[roleCode] || 'default'
}

/**
 * 获取性别标签
 * @param gender - 性别代码（0-女 1-男）
 */
const getGenderLabel = (gender?: number): string => {
  if (gender === undefined || gender === null) return '未知'
  return GENDER_LABELS[gender] || '未知'
}

/**
 * 加载用户详情
 */
const loadUserDetail = async () => {
  if (!props.userId) return

  loading.value = true
  try {
    const response = await userApi.getUserDetail(props.userId)
    userDetail.value = response.data
  } catch (error) {
    message.error('获取用户详情失败')
    console.error('获取用户详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 监听弹窗开启和userId变化
watch(
  () => [props.open, props.userId],
  ([newOpen]) => {
    if (newOpen && props.userId) {
      loadUserDetail()
    } else if (!newOpen) {
      userDetail.value = null
    }
  }
)

/**
 * 关闭抽屉
 */
const handleClose = () => {
  emit('update:open', false)
}
</script>

<style scoped lang="scss">
.user-detail {
  &__header {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 8px 0;
  }

  &__header-info {
    flex: 1;
  }

  &__name {
    margin: 0 0 4px;
    font-size: 20px;
    font-weight: 600;
    color: #262626;
  }

  &__username {
    margin: 0 0 8px;
    font-size: 14px;
    color: #8c8c8c;
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }

  &__descriptions {
    :deep(.ant-descriptions-item-label) {
      width: 120px;
      font-weight: 500;
    }
  }
}
</style>
