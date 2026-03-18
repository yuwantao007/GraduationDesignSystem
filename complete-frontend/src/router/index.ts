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
      // 教师配对管理路由
      {
        path: 'teacher-relation',
        name: 'TeacherRelation',
        component: () => import('@/views/teacher-relation/TeacherRelationManagement.vue'),
        meta: { title: '教师配对', icon: 'UsergroupAddOutlined', permission: 'teacher_relation:manage' }
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
      // 课题选报路由（学生）
      {
        path: 'topic-selection/list',
        name: 'TopicSelectionList',
        component: () => import('@/views/topic-selection/TopicSelectionList.vue'),
        meta: { title: '课题选报', icon: 'SelectOutlined', permission: 'selection:available' }
      },
      {
        path: 'topic-selection/my',
        name: 'MySelections',
        component: () => import('@/views/topic-selection/MySelections.vue'),
        meta: { title: '我的选报', icon: 'ProfileOutlined', permission: 'selection:my' }
      },
      // 教师确认子模块（企业教师）
      {
        path: 'topic-selection/teacher',
        name: 'TeacherSelectionConfirm',
        component: () => import('@/views/topic-selection/TeacherSelectionConfirm.vue'),
        meta: { title: '选报确认', icon: 'CheckCircleOutlined', permission: 'selection:teacher:list' }
      },
      // 双选审核子模块（企业负责人）
      {
        path: 'topic-selection/leader',
        name: 'SelectionLeaderOverview',
        component: () => import('@/views/topic-selection/SelectionLeaderOverview.vue'),
        meta: { title: '双选审核', icon: 'AuditOutlined', permission: 'selection:leader:overview' }
      },
      // 高校教师查看选题结果
      {
        path: 'topic-selection/univ-teacher',
        name: 'UnivTeacherSelectionView',
        component: () => import('@/views/topic-selection/UnivTeacherSelectionView.vue'),
        meta: { title: '指导学生选题', icon: 'ReadOutlined', permission: 'selection:univ:view' }
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
      },
      // 质量监控路由
      {
        path: 'monitor',
        name: 'MonitorDashboard',
        component: () => import('@/views/monitor/MonitorDashboard.vue'),
        meta: { title: '质量监控', icon: 'BarChartOutlined', permission: 'monitor:dashboard:view' }
      },
      {
        path: 'monitor/alerts',
        name: 'AlertCenter',
        component: () => import('@/views/monitor/AlertCenter.vue'),
        meta: { title: '预警中心', icon: 'BellOutlined', permission: 'monitor:alert:view' }
      },
      // 工作流路由（Flowable 审查流程）
      {
        path: 'workflow/tasks',
        name: 'TaskInbox',
        component: () => import('@/views/workflow/TaskInbox.vue'),
        meta: { title: '待办任务', icon: 'ApartmentOutlined' }
      },
      {
        path: 'workflow/definition',
        name: 'ProcessDefinition',
        component: () => import('@/views/workflow/ProcessDefinition.vue'),
        meta: { title: '流程定义', icon: 'ApartmentOutlined' }
      },
      {
        path: 'workflow/monitor',
        name: 'ProcessMonitor',
        component: () => import('@/views/workflow/ProcessMonitor.vue'),
        meta: { title: '流程监控', icon: 'ApartmentOutlined', permission: 'monitor:dashboard:view' }
      },
      // 过程管理 - 指导记录
      {
        path: 'guidance/teacher',
        name: 'TeacherGuidanceList',
        component: () => import('@/views/guidance/TeacherGuidanceList.vue'),
        meta: { title: '学生指导', icon: 'SolutionOutlined', permission: 'guidance:teacher' }
      },
      {
        path: 'guidance/student',
        name: 'StudentGuidanceView',
        component: () => import('@/views/guidance/StudentGuidanceView.vue'),
        meta: { title: '我的指导记录', icon: 'BookOutlined', permission: 'guidance:student' }
      },
      {
        path: 'guidance/leader',
        name: 'LeaderGuidanceOverview',
        component: () => import('@/views/guidance/LeaderGuidanceOverview.vue'),
        meta: { title: '指导记录总览', icon: 'FundViewOutlined', permission: 'guidance:leader' }
      },
      // 过程管理 - 文档管理
      {
        path: 'document/student',
        name: 'StudentDocumentCenter',
        component: () => import('@/views/document/StudentDocumentCenter.vue'),
        meta: { title: '我的文档', icon: 'FolderOutlined', permission: 'document:student' }
      },
      {
        path: 'document/teacher',
        name: 'TeacherDocumentView',
        component: () => import('@/views/document/TeacherDocumentView.vue'),
        meta: { title: '学生文档', icon: 'FileSearchOutlined', permission: 'document:teacher' }
      },
      // 过程管理 - 开题答辩管理
      {
        path: 'defense/arrangement',
        name: 'DefenseArrangementList',
        component: () => import('@/views/defense/ArrangementList.vue'),
        meta: { title: '答辩安排', icon: 'CalendarOutlined', permission: 'defense:arrangement:list' }
      },
      {
        path: 'defense/taskbook',
        name: 'DefenseTaskBookList',
        component: () => import('@/views/defense/TaskBookList.vue'),
        meta: { title: '任务书管理', icon: 'FormOutlined', permission: 'defense:taskbook:save' }
      },
      {
        path: 'defense/report',
        name: 'DefenseReportList',
        component: () => import('@/views/defense/ReportList.vue'),
        meta: { title: '开题报告审查', icon: 'AuditOutlined', permission: 'defense:report:list' }
      },
      {
        path: 'defense/my-report',
        name: 'MyOpeningReport',
        component: () => import('@/views/defense/MyReport.vue'),
        meta: { title: '我的开题报告', icon: 'FileTextOutlined', permission: 'defense:report:my' }
      },
      {
        path: 'defense/my-taskbook',
        name: 'MyTaskBook',
        component: () => import('@/views/defense/MyTaskBook.vue'),
        meta: { title: '我的任务书', icon: 'ReadOutlined', permission: 'defense:taskbook:detail' }
      },
      // 过程管理 - 中期检查
      {
        path: 'midterm/enterprise',
        name: 'MidtermCheckForm',
        component: () => import('@/views/midterm/MidtermCheckForm.vue'),
        meta: { title: '中期检查填写', icon: 'FormOutlined', permission: 'midterm:enterprise:list' }
      },
      {
        path: 'midterm/univ',
        name: 'MidtermCheckReview',
        component: () => import('@/views/midterm/MidtermCheckReview.vue'),
        meta: { title: '中期检查审查', icon: 'AuditOutlined', permission: 'midterm:univ:list' }
      },
      {
        path: 'midterm/student',
        name: 'MidtermStudentView',
        component: () => import('@/views/midterm/MidtermStudentView.vue'),
        meta: { title: '我的中期检查', icon: 'FileTextOutlined', permission: 'midterm:student:view' }
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

/**
 * 当前页面生命周期内是否已从服务端拉取过最新权限。
 * 使用模块级变量（非 localStorage），保证每次页面刷新/新 Tab 都重新拉取一次，
 * 避免 localStorage 缓存旧权限导致菜单不更新。
 */
let permissionsRefreshed = false

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
      // 每次页面加载后的首次导航，强制从服务端拉取最新用户信息与权限，
      // 确保 SQL 层面的权限变更在无需重新登录的情况下即时生效。
      if (!userStore.userInfo || !permissionsRefreshed) {
        try {
          await userStore.getUserInfoData()
          permissionsRefreshed = true
          next()
        } catch (error) {
          // 获取用户信息失败（token 过期等），清除认证信息并跳转登录页
          await userStore.logout()
          permissionsRefreshed = false
          message.error('获取用户信息失败，请重新登录')
          next(`/login?redirect=${to.path}`)
        }
      } else {
        // 同一页面会话内已刷新过权限，直接使用内存中的数据
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
    permissionsRefreshed = false
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

