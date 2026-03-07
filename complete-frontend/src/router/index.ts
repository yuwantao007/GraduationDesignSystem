import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/auth'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'DashboardOutlined' }
      },
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理', icon: 'UserOutlined', permission: 'user:view' }
      },
      {
        path: 'user/role',
        name: 'RoleManagement',
        component: () => import('@/views/user/RoleList.vue'),
        meta: { title: '角色权限', icon: 'SafetyCertificateOutlined', permission: 'role:view' }
      },
      {
        path: 'enterprise',
        name: 'EnterpriseManagement',
        redirect: '/enterprise/overview',
        meta: { title: '企业管理', icon: 'BankOutlined', permission: 'enterprise:view' },
        children: [
          {
            path: 'overview',
            name: 'EnterpriseOverview',
            component: () => import('@/views/enterprise/EnterpriseOverview.vue'),
            meta: { title: '企业概览', permission: 'enterprise:view' }
          },
          {
            path: 'list',
            name: 'EnterpriseList',
            component: () => import('@/views/enterprise/EnterpriseList.vue'),
            meta: { title: '企业列表', permission: 'enterprise:view' }
          },
          {
            path: 'major',
            name: 'MajorManagement',
            component: () => import('@/views/major/MajorList.vue'),
            meta: { title: '专业管理', permission: 'enterprise:major:view' }
          }
        ]
      },
      {
        path: 'school',
        name: 'SchoolManagement',
        component: () => import('@/views/school/SchoolList.vue'),
        meta: { title: '学校管理', icon: 'ReadOutlined', permission: 'school:view' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人中心', icon: 'UserOutlined' }
      },
      // 课题管理相关路由
      {
        path: 'topic/list',
        name: 'TopicList',
        component: () => import('@/views/topic/TopicList.vue'),
        meta: { title: '课题列表', icon: 'FileTextOutlined', permission: 'topic:view' }
      },
      {
        path: 'topic/create',
        name: 'TopicCreate',
        component: () => import('@/views/topic/TopicForm.vue'),
        meta: { title: '创建课题', permission: 'topic:create', hideInMenu: true }
      },
      {
        path: 'topic/edit/:id',
        name: 'TopicEdit',
        component: () => import('@/views/topic/TopicForm.vue'),
        meta: { title: '编辑课题', permission: 'topic:edit', hideInMenu: true }
      },
      {
        path: 'topic/detail/:id',
        name: 'TopicDetail',
        component: () => import('@/views/topic/TopicDetail.vue'),
        meta: { title: '课题详情', permission: 'topic:view', hideInMenu: true }
      },
      // 课题审查路由
      {
        path: 'topic/review',
        name: 'TopicReview',
        component: () => import('@/views/topic/TopicReview.vue'),
        meta: { title: '课题审查', icon: 'AuditOutlined', permission: 'topic:review' }
      },
      // 阶段管理路由
      {
        path: 'system/phase/overview',
        name: 'PhaseOverview',
        component: () => import('@/views/phase/PhaseOverview.vue'),
        meta: { title: '阶段概览', icon: 'FieldTimeOutlined', permission: 'phase:view' }
      },
      {
        path: 'system/phase/records',
        name: 'PhaseRecords',
        component: () => import('@/views/phase/PhaseRecords.vue'),
        meta: { title: '切换记录', icon: 'HistoryOutlined', permission: 'phase:records' }
      }
    ]
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/Forbidden.vue'),
    meta: { title: '403 - 无权限' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 白名单路由（不需要登录即可访问）
const whiteList = ['/login', '/register']

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || '毕业设计管理系统'} - 毕业设计全过程管理系统`

  const token = getToken()
  const userStore = useUserStore()

  if (token) {
    // 已登录
    if (to.path === '/login') {
      // 如果已登录并访问登录页，重定向到首页
      next({ path: '/' })
    } else {
      // 检查是否有用户信息
      if (!userStore.userInfo) {
        try {
          // 获取用户信息
          await userStore.getUserInfoData()
          next()
        } catch (error) {
          // 获取用户信息失败，清除token并重定向到登录页
          await userStore.logout()
          message.error('获取用户信息失败，请重新登录')
          next(`/login?redirect=${to.path}`)
        }
      } else {
        // 检查权限
        if (to.meta.permission && !userStore.hasPermission(to.meta.permission as string)) {
          message.error('没有访问权限')
          next({ path: '/403' })
        } else {
          next()
        }
      }
    }
  } else {
    // 未登录
    if (whiteList.includes(to.path)) {
      // 在白名单中，直接访问
      next()
    } else {
      // 不在白名单中，重定向到登录页
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router

