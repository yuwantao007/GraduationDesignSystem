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
        
        <a-sub-menu v-if="userStore.hasAnyRole(['SYSTEM_ADMIN'])" key="user-mgmt">
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
        
        <a-sub-menu v-if="(userStore.hasPermission('enterprise:view') || userStore.hasPermission('enterprise:major:view')) && !userStore.hasAnyRole(['STUDENT'])" key="enterprise-mgmt">
          <template #icon><BankOutlined /></template>
          <template #title>企业管理</template>
          <a-menu-item v-if="userStore.hasPermission('enterprise:view')" key="/enterprise/overview">
            企业概览
          </a-menu-item>
          <a-menu-item v-if="userStore.hasPermission('enterprise:view')" key="/enterprise/list">
            企业列表
          </a-menu-item>
          <a-menu-item v-if="userStore.hasPermission('enterprise:major:view') || userStore.hasAnyRole(['SYSTEM_ADMIN', 'ENTERPRISE_LEADER'])" key="/enterprise/major">
            专业管理
          </a-menu-item>
        </a-sub-menu>
        
        <a-menu-item v-if="userStore.hasPermission('school:view')" key="/school">
          <template #icon><ReadOutlined /></template>
          <span>学校管理</span>
        </a-menu-item>
        
        <!-- 教师配对管理 -->
        <a-menu-item v-if="userStore.hasAnyRole(['SYSTEM_ADMIN', 'ENTERPRISE_LEADER', 'UNIVERSITY_TEACHER'])" key="/teacher-relation">
          <template #icon><UsergroupAddOutlined /></template>
          <span>教师配对</span>
        </a-menu-item>
        
        <!-- 课题管理：根据角色显示不同子菜单 -->
        <a-sub-menu v-if="userStore.hasAnyRole(['ENTERPRISE_TEACHER', 'SYSTEM_ADMIN', 'UNIVERSITY_TEACHER', 'MAJOR_DIRECTOR', 'SUPERVISOR_TEACHER'])" key="topic">
          <template #icon><FileTextOutlined /></template>
          <template #title>课题管理</template>
          <!-- 课题列表：仅企业教师和系统管理员可见（课题创建/编辑/删除） -->
          <a-menu-item v-if="userStore.hasPermission('topic:view') && userStore.hasAnyRole(['ENTERPRISE_TEACHER', 'SYSTEM_ADMIN'])" key="/topic/list">课题列表</a-menu-item>
          <!-- 课题审查：仅审查角色可见（高校教师/专业方向主管/督导教师） -->
          <a-menu-item v-if="userStore.hasPermission('topic:review') && userStore.hasAnyRole(['UNIVERSITY_TEACHER', 'MAJOR_DIRECTOR', 'SUPERVISOR_TEACHER'])" key="/topic/review">课题审查</a-menu-item>
        </a-sub-menu>
        
        <!-- 双选管理：学生选报 -->
        <a-sub-menu v-if="userStore.hasAnyRole(['STUDENT']) && userStore.hasPermission('selection:manage')" key="selection">
          <template #icon><SelectOutlined /></template>
          <template #title>双选管理</template>
          <a-menu-item v-if="userStore.hasPermission('selection:available')" key="/topic-selection/list">课题选报</a-menu-item>
          <a-menu-item v-if="userStore.hasPermission('selection:my')" key="/topic-selection/my">我的选报</a-menu-item>
        </a-sub-menu>

        <!-- 双选管理：企业教师确认人选 -->
        <a-sub-menu v-if="userStore.hasAnyRole(['ENTERPRISE_TEACHER']) && userStore.hasPermission('selection:teacher:list')" key="selection">
          <template #icon><CheckCircleOutlined /></template>
          <template #title>双选管理</template>
          <a-menu-item key="/topic-selection/teacher">选报确认</a-menu-item>
        </a-sub-menu>

        <!-- 双选管理：企业负责人审核 -->
        <a-sub-menu v-if="userStore.hasAnyRole(['ENTERPRISE_LEADER']) && userStore.hasPermission('selection:leader:overview')" key="selection">
          <template #icon><AuditOutlined /></template>
          <template #title>双选管理</template>
          <a-menu-item key="/topic-selection/leader">双选审核</a-menu-item>
        </a-sub-menu>

        <!-- 双选管理：高校教师查看指导学生选题 -->
        <a-sub-menu v-if="userStore.hasAnyRole(['UNIVERSITY_TEACHER']) && userStore.hasPermission('selection:univ:view')" key="selection">
          <template #icon><ReadOutlined /></template>
          <template #title>双选管理</template>
          <a-menu-item key="/topic-selection/univ-teacher">指导学生选题</a-menu-item>
        </a-sub-menu>
        
        <a-sub-menu key="document">
          <template #icon><FolderOutlined /></template>
          <template #title>文档管理</template>
          <a-menu-item key="/document/list">文档列表</a-menu-item>
          <a-menu-item key="/document/upload">文档上传</a-menu-item>
        </a-sub-menu>
        
        <!-- 阶段管理：管理员显示含切换记录的子菜单，其他角色直接显示阶段概览 -->
        <a-sub-menu v-if="userStore.hasAnyRole(['SYSTEM_ADMIN']) && userStore.hasPermission('phase:view')" key="phase-mgmt">
          <template #icon><FieldTimeOutlined /></template>
          <template #title>阶段管理</template>
          <a-menu-item key="/system/phase/overview">阶段概览</a-menu-item>
          <a-menu-item key="/system/phase/records">切换记录</a-menu-item>
        </a-sub-menu>
        <a-menu-item v-if="!userStore.hasAnyRole(['SYSTEM_ADMIN']) && userStore.hasPermission('phase:view')" key="/system/phase/overview">
          <template #icon><FieldTimeOutlined /></template>
          <span>阶段概览</span>
        </a-menu-item>
        
        <a-menu-item key="/settings">
          <template #icon><SettingOutlined /></template>
          <span>系统设置</span>
        </a-menu-item>

        <!-- 质量监控：系统管理员和企业负责人可见 -->
        <a-sub-menu
          v-if="userStore.hasPermission('monitor:dashboard:view')"
          key="monitor"
        >
          <template #icon><BarChartOutlined /></template>
          <template #title>质量监控</template>
          <a-menu-item v-if="userStore.hasPermission('monitor:dashboard:view')" key="/monitor">
            监控仪表盘
          </a-menu-item>
          <a-menu-item v-if="userStore.hasPermission('monitor:alert:view')" key="/monitor/alerts">
            预警中心
          </a-menu-item>
        </a-sub-menu>

        <!-- 工作流（Flowable 审查流程）-->
        <a-sub-menu
          v-if="userStore.hasAnyRole(['UNIVERSITY_TEACHER', 'MAJOR_DIRECTOR', 'SUPERVISOR_TEACHER', 'SYSTEM_ADMIN', 'ENTERPRISE_TEACHER'])"
          key="workflow"
        >
          <template #icon><ApartmentOutlined /></template>
          <template #title>工作流</template>
          <!-- 待办任务收件箱：审核角色 + 企业教师（修改任务）都可见 -->
          <a-menu-item
            v-if="userStore.hasAnyRole(['UNIVERSITY_TEACHER', 'MAJOR_DIRECTOR', 'SUPERVISOR_TEACHER', 'ENTERPRISE_TEACHER'])"
            key="/workflow/tasks"
          >
            待办任务
          </a-menu-item>
          <!-- 流程定义：仅系统管理员可查看 -->
          <a-menu-item
            v-if="userStore.hasAnyRole(['SYSTEM_ADMIN'])"
            key="/workflow/definition"
          >
            流程定义
          </a-menu-item>
          <!-- 流程监控：仅管理员 -->
          <a-menu-item
            v-if="userStore.hasPermission('monitor:dashboard:view')"
            key="/workflow/monitor"
          >
            流程监控
          </a-menu-item>
        </a-sub-menu>
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
import { ref, computed, watch, onMounted, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message, Modal, notification } from 'ant-design-vue'
import {
  DashboardOutlined,
  UserOutlined,
  TeamOutlined,
  SafetyCertificateOutlined,
  FileTextOutlined,
  FolderOutlined,
  SettingOutlined,
  DownOutlined,
  LogoutOutlined,
  BankOutlined,
  ReadOutlined,
  FieldTimeOutlined,
  UsergroupAddOutlined,
  SelectOutlined,
  CheckCircleOutlined,
  AuditOutlined,
  BarChartOutlined,
  BellOutlined,
  ApartmentOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { phaseApi } from '@/api/phase'

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
  if (path.startsWith('/enterprise')) return ['enterprise-mgmt']
  if (path.startsWith('/topic-selection')) return ['selection']
  if (path.startsWith('/topic')) return ['topic']
  if (path.startsWith('/document')) return ['document']
  if (path.startsWith('/system/phase')) return userStore.hasAnyRole(['SYSTEM_ADMIN']) ? ['phase-mgmt'] : []
  if (path.startsWith('/school')) return []
  if (path.startsWith('/teacher-relation')) return []
  if (path.startsWith('/monitor')) return ['monitor']
  if (path.startsWith('/workflow')) return ['workflow']
  return []
}

// 更新面包屑
const updateBreadcrumbs = (path: string) => {
  const breadcrumbMap: Record<string, Array<{ title: string; path?: string }>> = {
    '/dashboard': [{ title: '首页' }, { title: '仪表盘' }],
    '/user': [{ title: '首页', path: '/' }, { title: '用户管理' }, { title: '用户列表' }],
    '/user/role': [{ title: '首页', path: '/' }, { title: '用户管理' }, { title: '角色权限' }],
    '/enterprise/list': [{ title: '首页', path: '/' }, { title: '企业管理' }, { title: '企业列表' }],
    '/enterprise/major': [{ title: '首页', path: '/' }, { title: '企业管理' }, { title: '专业管理' }],
    '/school': [{ title: '首页', path: '/' }, { title: '学校管理' }],
    '/teacher-relation': [{ title: '首页', path: '/' }, { title: '教师配对' }],
    '/profile': [{ title: '首页', path: '/' }, { title: '个人中心' }],
    '/topic/list': [{ title: '首页', path: '/' }, { title: '课题管理' }, { title: '课题列表' }],
    '/topic/review': [{ title: '首页', path: '/' }, { title: '课题管理' }, { title: '课题审查' }],
    '/topic-selection/list': [{ title: '首页', path: '/' }, { title: '双选管理' }, { title: '课题选报' }],
    '/topic-selection/my': [{ title: '首页', path: '/' }, { title: '双选管理' }, { title: '我的选报' }],
    '/topic-selection/teacher': [{ title: '首页', path: '/' }, { title: '双选管理' }, { title: '选报确认' }],
    '/topic-selection/leader': [{ title: '首页', path: '/' }, { title: '双选管理' }, { title: '双选审核' }],
    '/topic-selection/univ-teacher': [{ title: '首页', path: '/' }, { title: '双选管理' }, { title: '指导学生选题' }],
    '/system/phase/overview': userStore.hasAnyRole(['SYSTEM_ADMIN'])
      ? [{ title: '首页', path: '/' }, { title: '阶段管理' }, { title: '阶段概览' }]
      : [{ title: '首页', path: '/' }, { title: '阶段概览' }],
    '/system/phase/records': [{ title: '首页', path: '/' }, { title: '阶段管理' }, { title: '切换记录' }],
    '/document/list': [{ title: '首页', path: '/' }, { title: '文档管理' }, { title: '文档列表' }],
    '/document/upload': [{ title: '首页', path: '/' }, { title: '文档管理' }, { title: '文档上传' }],
    '/settings': [{ title: '首页', path: '/' }, { title: '系统设置' }],
    '/monitor': [{ title: '首页', path: '/' }, { title: '质量监控' }, { title: '监控仪表盘' }],
    '/monitor/alerts': [{ title: '首页', path: '/' }, { title: '质量监控' }, { title: '预警中心' }],
    '/workflow/tasks': [{ title: '首页', path: '/' }, { title: '工作流' }, { title: '待办任务' }],
    '/workflow/definition': [{ title: '首页', path: '/' }, { title: '工作流' }, { title: '流程定义' }],
    '/workflow/monitor': [{ title: '首页', path: '/' }, { title: '工作流' }, { title: '流程监控' }]
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

// 登录后阶段提示（非管理员角色，每次会话仅提示一次）
onMounted(async () => {
  if (!userStore.hasAnyRole(['SYSTEM_ADMIN']) && userStore.hasPermission('phase:view')) {
    const shown = sessionStorage.getItem('phaseNotificationShown')
    if (!shown) {
      // 延迟 600ms 确保页面及通知容器完全渲染后再弹窗
      setTimeout(async () => {
        try {
          const res = await phaseApi.getCurrentPhaseStatus()
          const status = res.data
          if (status?.initialized) {
            notification.info({
              key: 'phase-login-tip',
              message: `当前系统阶段：${status.phaseName}`,
              description: `毕业届别：${status.cohort || '--'} | 整体进度：${status.progressPercent}%（第 ${status.phaseOrder} / ${status.totalPhases} 阶段）`,
              icon: h(FieldTimeOutlined, { style: { color: '#1890ff' } }),
              duration: 3,
              placement: 'topRight'
            })
            sessionStorage.setItem('phaseNotificationShown', '1')
          }
        } catch {
          // 忽略错误，不影响主流程
        }
      }, 600)
    }
  }
})

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
