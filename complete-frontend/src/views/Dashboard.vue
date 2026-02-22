<template>
  <div class="dashboard">
    <a-row :gutter="16">
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="总用户数"
            :value="statistics.totalUsers"
            :prefix="() => h(UserOutlined)"
          />
        </a-card>
      </a-col>
      
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="课题总数"
            :value="statistics.totalTopics"
            :prefix="() => h(FileTextOutlined)"
          />
        </a-card>
      </a-col>
      
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="待审批"
            :value="statistics.pendingApprovals"
            :prefix="() => h(ClockCircleOutlined)"
          />
        </a-card>
      </a-col>
      
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="今日访问"
            :value="statistics.todayVisits"
            :prefix="() => h(EyeOutlined)"
          />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="12">
        <a-card title="最近活动">
          <a-list
            :data-source="recentActivities"
            :loading="loading"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta
                  :title="item.title"
                  :description="item.time"
                />
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
      
      <a-col :span="12">
        <a-card title="待办事项">
          <a-list
            :data-source="todoList"
            :loading="loading"
          >
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta
                  :title="item.title"
                  :description="item.deadline"
                />
                <template #extra>
                  <a-tag :color="item.priority === 'high' ? 'red' : 'blue'">
                    {{ item.priority === 'high' ? '紧急' : '普通' }}
                  </a-tag>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import {
  UserOutlined,
  FileTextOutlined,
  ClockCircleOutlined,
  EyeOutlined
} from '@ant-design/icons-vue'

// 统计数据
const statistics = ref({
  totalUsers: 0,
  totalTopics: 0,
  pendingApprovals: 0,
  todayVisits: 0
})

// 最近活动
const recentActivities = ref([
  { title: '张三提交了开题报告', time: '2小时前' },
  { title: '李四上传了毕业论文', time: '5小时前' },
  { title: '王五申请了答辩', time: '1天前' },
  { title: '赵六完成了中期检查', time: '2天前' }
])

// 待办事项
const todoList = ref([
  { title: '审批课题申请', deadline: '今天 18:00', priority: 'high' },
  { title: '评阅学生论文', deadline: '明天 12:00', priority: 'normal' },
  { title: '安排答辩时间', deadline: '后天 10:00', priority: 'normal' }
])

const loading = ref(false)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 模拟加载数据
    setTimeout(() => {
      statistics.value = {
        totalUsers: 1234,
        totalTopics: 567,
        pendingApprovals: 23,
        todayVisits: 456
      }
      loading.value = false
    }, 1000)
  } catch (error) {
    console.error('加载数据失败:', error)
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}
</style>
