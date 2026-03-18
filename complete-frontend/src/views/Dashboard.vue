<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { message } from 'ant-design-vue';
import { 
  UserOutlined, 
  ProjectOutlined, 
  FileTextOutlined, 
  ClockCircleOutlined,
  CheckCircleOutlined,
  EyeOutlined,
  PlusOutlined,
  BellOutlined,
  SettingOutlined
} from '@ant-design/icons-vue';
import { useUserStore } from '@/stores/user';
import { dashboardApi } from '@/api/dashboard';

const userStore = useUserStore();
const currentUser = computed(() => userStore.userInfo);

// 模拟阶段数据
const currentPhase = ref(1); // 0: 课题申报阶段, 1: 课题双选阶段, 2: 课题指导阶段, 3: 毕业答辩阶段
const phaseProgress = ref(38); // 当前整体进度

// 模拟统计数据
const statistics = ref([
  { key: 'totalUsers', title: '总用户数', value: 0, icon: UserOutlined, color: '#1890ff', bgColor: '#e6f7ff' },
  { key: 'totalTopics', title: '课题总数', value: 0, icon: FileTextOutlined, color: '#722ed1', bgColor: '#f9f0ff' },
  { key: 'pendingApprovals', title: '待审审批', value: 0, icon: ClockCircleOutlined, color: '#fa8c16', bgColor: '#fff7e6' },
  { key: 'todayVisits', title: '今日访问', value: 0, icon: EyeOutlined, color: '#52c41a', bgColor: '#f6ffed' },
]);

const loadDashboardStats = async () => {
  try {
    const response = await dashboardApi.getStats();
    const data = response.data;

    statistics.value = statistics.value.map((item) => ({
      ...item,
      value: Number(data[item.key as keyof typeof data] ?? 0),
    }));
  } catch (error) {
    message.error('加载首页统计数据失败');
    console.error('加载首页统计数据失败:', error);
  }
};

// 模拟动态活动数据
const activities = ref([
  { id: 1, user: '张三', action: '提交了开题报告', time: '2小时前', color: 'blue' },
  { id: 2, user: '李四', action: '上传了毕业论文', time: '5小时前', color: 'blue' },
  { id: 3, user: '王五', action: '申请了答辩', time: '1天前', color: 'blue' },
  { id: 4, user: '赵六', action: '完成了中期检查', time: '2天前', color: 'blue' },
  { id: 5, user: '系统', action: '发布了新通知：关于开展课题双选的通知', time: '3天前', color: 'orange' },
]);

// 模拟待办事项
const todos = ref([
  { id: 1, title: '审批课题申请', time: '今天 18:00', priority: '紧急', isDone: false },
  { id: 2, title: '评阅学生论文', time: '明天 12:00', priority: '普通', isDone: false },
  { id: 3, title: '安排答辩时间', time: '后天 10:00', priority: '普通', isDone: false },
]);

// 快捷导航
const quickNavs = ref([
  { title: '课题管理', icon: ProjectOutlined },
  { title: '学生管理', icon: UserOutlined },
  { title: '审批中心', icon: CheckCircleOutlined },
  { title: '系统通知', icon: BellOutlined },
  { title: '账号设置', icon: SettingOutlined },
]);

onMounted(() => {
  loadDashboardStats();
});
</script>

<template>
  <div class="dashboard-container">
    <!-- 1. 欢迎工作区 (参考 Ant Design Pro 风格) -->
    <a-card :bordered="false" class="mb-4 welcome-card">
      <div class="welcome-wrapper">
        <div class="welcome-header">
          <a-avatar :size="72" :src="currentUser?.avatar" style="background-color: #1890ff;">
            {{ currentUser?.realName?.charAt(0) || 'U' }}
          </a-avatar>
          <div class="welcome-text">
            <h2>早安，{{ currentUser?.realName || '管理员' }}，祝你开心每一天！</h2>
            <p class="secondary-text">计算机科学与技术系 - 毕业设计管理系统后台</p>
          </div>
        </div>
        <div class="welcome-stats">
          <div class="stat-item">
            <span class="stat-label">待办项目</span>
            <span class="stat-value">23</span>
          </div>
          <a-divider type="vertical" class="stat-divider"></a-divider>
          <div class="stat-item">
            <span class="stat-label">系统排名</span>
            <span class="stat-value">8 <span class="stat-suffix">/ 24</span></span>
          </div>
          <a-divider type="vertical" class="stat-divider"></a-divider>
          <div class="stat-item">
            <span class="stat-label">项目访问</span>
            <span class="stat-value">2,223</span>
          </div>
        </div>
      </div>
    </a-card>

    <!-- 2. 当前阶段进度区 (参考图1特点) -->
    <a-card :bordered="false" class="mb-4 phase-card">
      <template #title>
        <span style="font-weight: bold; font-size: 16px;">
          <ClockCircleOutlined style="color: #1890ff; margin-right: 8px;"></ClockCircleOutlined>
          当前阶段进度
        </span>
      </template>
      <div class="phase-info">
        <div class="phase-info-item">
          <div class="info-label">当前阶段</div>
          <div class="info-value text-primary">课题双选阶段 <a-tag color="blue">2 / 4</a-tag></div>
        </div>
        <a-divider type="vertical" style="height: 50px;"></a-divider>
        <div class="phase-info-item">
          <div class="info-label">毕业届别</div>
          <div class="info-value">2026届</div>
        </div>
        <a-divider type="vertical" style="height: 50px;"></a-divider>
        <div class="phase-info-item">
          <div class="info-label">整体进度</div>
          <div class="info-value text-primary">38%</div>
        </div>
      </div>
      
      <!-- 进度条 -->
      <div class="progress-bar-wrapper">
        <a-progress 
          :percent="phaseProgress" 
          :stroke-width="8" 
          status="active"
          :stroke-color="{
            '0%': '#108ee9',
            '100%': '#87d068',
          }"
        ></a-progress>
      </div>

      <!-- 阶段步骤条 -->
      <a-steps :current="currentPhase" class="mt-4" size="small">
        <a-step title="课题申报阶段"></a-step>
        <a-step title="课题双选阶段"></a-step>
        <a-step title="课题指导阶段"></a-step>
        <a-step title="毕业答辩阶段"></a-step>
      </a-steps>
    </a-card>

    <!-- 3. 数据统计卡片 (四个数据面) -->
    <a-row :gutter="16" class="mb-4">
      <a-col :span="6" v-for="(item, index) in statistics" :key="index">
        <a-card :bordered="false" class="stat-box-card">
          <div class="stat-box">
            <div class="stat-box-icon" :style="{ backgroundColor: item.bgColor, color: item.color }">
              <component :is="item.icon"></component>
            </div>
            <div class="stat-box-content">
              <div class="box-title">{{ item.title }}</div>
              <div class="box-value">{{ item.value }}</div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 4. 下半部分 (动态与待办) -->
    <a-row :gutter="16">
      <!-- 左侧：最近活动 -->
      <a-col :span="16">
        <a-card :bordered="false" title="最近活动" class="h-100">
          <a-timeline>
            <a-timeline-item :color="item.color" v-for="item in activities" :key="item.id">
              <div class="activity-item">
                <div class="activity-content">
                  <a-avatar size="small" class="activity-avatar">{{ item.user.charAt(0) }}</a-avatar>
                  <span class="activity-user">{{ item.user }}</span>
                  <span class="activity-action">{{ item.action }}</span>
                </div>
                <div class="activity-time">{{ item.time }}</div>
              </div>
            </a-timeline-item>
          </a-timeline>
        </a-card>
      </a-col>

      <!-- 右侧：快捷导航 & 待办事项 -->
      <a-col :span="8">
        <!-- 快捷导航 -->
        <a-card :bordered="false" title="快捷导航" class="mb-4">
          <div class="quick-navs">
            <div class="nav-item" v-for="(nav, idx) in quickNavs" :key="idx">
              <div class="nav-icon"><component :is="nav.icon"></component></div>
              <div class="nav-title">{{ nav.title }}</div>
            </div>
            <div class="nav-item">
              <div class="nav-icon" style="border: 1px dashed #d9d9d9; background: #fafafa;">
                <PlusOutlined style="color: #bfbfbf;"></PlusOutlined>
              </div>
              <div class="nav-title">添加</div>
            </div>
          </div>
        </a-card>

        <!-- 待办事项 -->
        <a-card :bordered="false" title="待办事项">
          <a-list item-layout="horizontal" :data-source="todos">
            <template #renderItem="{ item }">
              <a-list-item>
                <div class="todo-item w-100">
                  <div class="todo-main">
                    <a-checkbox v-model:checked="item.isDone"></a-checkbox>
                    <div class="todo-text" :class="{ 'text-strike': item.isDone }">{{ item.title }}</div>
                  </div>
                  <div class="todo-meta">
                    <span class="todo-time">{{ item.time }}</span>
                    <a-tag :color="item.priority === '紧急' ? 'red' : 'blue'">{{ item.priority }}</a-tag>
                  </div>
                </div>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<style scoped>
.dashboard-container {
  padding: 16px;
  background-color: #f0f2f5;
  min-height: 100vh;
}
.mb-4 {
  margin-bottom: 16px;
}
.mt-4 {
  margin-top: 16px;
}
.h-100 {
  height: 100%;
}
.w-100 {
  width: 100%;
}

/* 欢迎区 */
.welcome-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.welcome-header {
  display: flex;
  align-items: center;
  gap: 24px;
}
.welcome-text h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
}
.secondary-text {
  margin: 0;
  color: rgba(0, 0, 0, 0.45);
}
.welcome-stats {
  display: flex;
  align-items: center;
  gap: 24px;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}
.stat-label {
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
  margin-bottom: 4px;
}
.stat-value {
  color: rgba(0, 0, 0, 0.85);
  font-size: 24px;
}
.stat-suffix {
  font-size: 16px;
  color: rgba(0, 0, 0, 0.45);
}
.stat-divider {
  height: 40px;
}

/* 阶段区 */
.phase-card {
  border-radius: 8px;
}
.phase-info {
  display: flex;
  justify-content: space-around;
  align-items: center;
  text-align: center;
  margin-bottom: 24px;
  background-color: #fafafa;
  padding: 16px;
  border-radius: 8px;
}
.info-label {
  color: rgba(0, 0, 0, 0.45);
  margin-bottom: 8px;
  font-size: 14px;
}
.info-value {
  font-size: 20px;
  font-weight: bold;
  color: rgba(0, 0, 0, 0.85);
}
.text-primary {
  color: #1890ff;
}
.progress-bar-wrapper {
  margin-bottom: 16px;
}

/* 统计小卡片 */
.stat-box-card {
  border-radius: 8px;
}
.stat-box {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-box-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
.stat-box-content {
  display: flex;
  flex-direction: column;
}
.box-title {
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
  margin-bottom: 4px;
}
.box-value {
  color: rgba(0, 0, 0, 0.85);
  font-size: 28px;
  font-weight: 500;
  line-height: 1;
}

/* 活动区 */
.activity-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.activity-content {
  display: flex;
  align-items: center;
  gap: 8px;
}
.activity-avatar {
  background-color: #1890ff;
  color: white;
  margin-right: 8px;
}
.activity-user {
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
}
.activity-action {
  color: rgba(0, 0, 0, 0.65);
}
.activity-time {
  color: rgba(0, 0, 0, 0.45);
  font-size: 13px;
}

/* 快捷导航 */
.quick-navs {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.nav-item {
  width: calc(33.33% - 11px);
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
}
.nav-item:hover .nav-icon {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.nav-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background-color: #e6f7ff;
  color: #1890ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  margin-bottom: 8px;
}
.nav-title {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.65);
}

/* 待办事项 */
.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.todo-main {
  display: flex;
  align-items: center;
  gap: 12px;
}
.todo-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}
.todo-text {
  color: rgba(0, 0, 0, 0.85);
}
.todo-time {
  color: rgba(0, 0, 0, 0.45);
  font-size: 13px;
}
.text-strike {
  text-decoration: line-through;
  color: rgba(0, 0, 0, 0.25);
}
</style>

