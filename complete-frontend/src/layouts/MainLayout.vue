<template>
  <a-layout style="min-height: 100vh">
    <!-- 侧边栏 -->
    <a-layout-sider v-model:collapsed="collapsed" collapsible theme="dark">
      <div class="logo">
        <h2 v-if="!collapsed" style="color: white; text-align: center; padding: 16px 0">
          毕设管理系统
        </h2>
        <h2 v-else style="color: white; text-align: center; padding: 16px 0">MS</h2>
      </div>
      
      <a-menu
        v-model:selectedKeys="selectedKeys"
        v-model:openKeys="openKeys"
        theme="dark"
        mode="inline"
        @click="handleMenuClick"
      >
        <a-menu-item key="/dashboard">
          <template #icon><DashboardOutlined /></template>
          <span>仪表盘</span>
        </a-menu-item>
        
        <a-sub-menu key="user-mgmt">
          <template #icon><TeamOutlined /></template>
          <template #title>用户管理</template>
          <a-menu-item key="/user">
            <template #icon><UserOutlined /></template>
            <span>用户列表</span>
          </a-menu-item>
          <a-menu-item key="/user/role">
            <template #icon><SafetyCertificateOutlined /></template>
            <span>角色权限</span>
          </a-menu-item>
        </a-sub-menu>
        
        <a-sub-menu key="topic">
          <template #icon><FileTextOutlined /></template>
          <template #title>课题管理</template>
          <a-menu-item key="/topic/list">课题列表</a-menu-item>
          <a-menu-item key="/topic/approval">课题审批</a-menu-item>
        </a-sub-menu>
        
        <a-sub-menu key="document">
          <template #icon><FolderOutlined /></template>
          <template #title>文档管理</template>
          <a-menu-item key="/document/list">文档列表</a-menu-item>
          <a-menu-item key="/document/upload">文档上传</a-menu-item>
        </a-sub-menu>
        
        <a-menu-item key="/settings">
          <template #icon><SettingOutlined /></template>
          <span>系统设置</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <!-- 主内容区 -->
    <a-layout>
      <!-- 头部 -->
      <a-layout-header style="background: #fff; padding: 0; display: flex; justify-content: space-between; align-items: center">
        <div style="padding-left: 24px">
          <a-breadcrumb v-if="breadcrumbs.length > 0">
            <a-breadcrumb-item v-for="item in breadcrumbs" :key="item.title">
              <router-link v-if="item.path" :to="item.path">{{ item.title }}</router-link>
              <span v-else>{{ item.title }}</span>
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        
        <div style="padding-right: 24px">
          <a-dropdown>
            <a class="ant-dropdown-link" @click.prevent>
              <a-avatar :size="32" style="background-color: #1890ff">
                {{ userInfo?.realName?.charAt(0) || 'U' }}
              </a-avatar>
              <span style="margin-left: 8px">{{ userInfo?.realName || '用户' }}</span>
              <DownOutlined />
            </a>
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile" @click="handleProfile">
                  <UserOutlined />
                  <span>个人中心</span>
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" @click="handleLogout">
                  <LogoutOutlined />
                  <span>退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <!-- 内容区 -->
      <a-layout-content style="margin: 16px">
        <div style="padding: 24px; background: #fff; min-height: 360px">
          <router-view />
        </div>
      </a-layout-content>

      <!-- 底部 -->
      <a-layout-footer style="text-align: center">
        毕业设计全过程管理系统 ©2026 Created by YourTeam
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  DashboardOutlined,
  UserOutlined,
  TeamOutlined,
  SafetyCertificateOutlined,
  FileTextOutlined,
  FolderOutlined,
  SettingOutlined,
  DownOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

// 侧边栏折叠状态
const collapsed = computed({
  get: () => appStore.collapsed,
  set: (val) => {
    if (val !== appStore.collapsed) {
      appStore.toggleCollapsed()
    }
  }
})

// 选中的菜单项
const selectedKeys = ref<string[]>([route.path])

// 展开的子菜单
const openKeys = ref<string[]>([])

// 面包屑
const breadcrumbs = computed(() => appStore.breadcrumbs)

// 用户信息
const userInfo = computed(() => userStore.userInfo)

/**
 * 根据路径计算需要展开的子菜单
 * @param path - 当前路由路径
 */
const getOpenKeys = (path: string): string[] => {
  if (path.startsWith('/user')) return ['user-mgmt']
  if (path.startsWith('/topic')) return ['topic']
  if (path.startsWith('/document')) return ['document']
  return []
}

// 更新面包屑
const updateBreadcrumbs = (path: string) => {
  const breadcrumbMap: Record<string, Array<{ title: string; path?: string }>> = {
    '/dashboard': [{ title: '首页' }, { title: '仪表盘' }],
    '/user': [{ title: '首页', path: '/' }, { title: '用户管理' }, { title: '用户列表' }],
    '/user/role': [{ title: '首页', path: '/' }, { title: '用户管理' }, { title: '角色权限' }],
    '/profile': [{ title: '首页', path: '/' }, { title: '个人中心' }],
    '/topic/list': [{ title: '首页', path: '/' }, { title: '课题管理' }, { title: '课题列表' }],
    '/topic/approval': [{ title: '首页', path: '/' }, { title: '课题管理' }, { title: '课题审批' }],
    '/document/list': [{ title: '首页', path: '/' }, { title: '文档管理' }, { title: '文档列表' }],
    '/document/upload': [{ title: '首页', path: '/' }, { title: '文档管理' }, { title: '文档上传' }],
    '/settings': [{ title: '首页', path: '/' }, { title: '系统设置' }]
  }
  
  appStore.setBreadcrumbs(breadcrumbMap[path] || [{ title: '首页' }])
}

// 监听路由变化，更新选中的菜单项
watch(
  () => route.path,
  (newPath) => {
    selectedKeys.value = [newPath]
    openKeys.value = getOpenKeys(newPath)
    // 更新面包屑
    updateBreadcrumbs(newPath)
  },
  { immediate: true }
)

// 菜单点击事件
const handleMenuClick = ({ key }: { key: string }) => {
  router.push(key)
}

// 个人中心
const handleProfile = () => {
  router.push('/profile')
}

// 退出登录
const handleLogout = () => {
  Modal.confirm({
    title: '确认退出',
    content: '确定要退出登录吗？',
    onOk: async () => {
      await userStore.logout()
      message.success('已退出登录')
      router.push('/login')
    }
  })
}
</script>

<style scoped>
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.ant-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.ant-dropdown-link:hover {
  color: #1890ff;
}
</style>
