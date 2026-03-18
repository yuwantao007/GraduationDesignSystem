/**
 * 学生指导记录页面
 * 功能：学生查看自己的被指导记录
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-16
 */
<template>
  <div class="student-guidance-container">
    <!-- 页面标题 -->
    <a-page-header
      title="我的指导记录"
      sub-title="查看导师对您的指导记录"
    >
      <template #extra>
        <a-statistic
          title="总指导次数"
          :value="guidanceList.length"
          style="margin-right: 32px"
        />
        <a-statistic
          title="总指导时长"
          :value="totalHours"
          suffix="小时"
        />
      </template>
    </a-page-header>

    <!-- 筛选工具栏 -->
    <a-card class="filter-card" :bordered="false">
      <a-space wrap>
        <a-select
          v-model:value="filterType"
          placeholder="指导类型"
          style="width: 160px"
          allowClear
          @change="handleFilterChange"
        >
          <a-select-option :value="GuidanceType.PROJECT">项目指导</a-select-option>
          <a-select-option :value="GuidanceType.THESIS">论文指导</a-select-option>
        </a-select>
        <a-range-picker
          v-model:value="dateRange"
          format="YYYY-MM-DD"
          @change="handleFilterChange"
        />
        <a-button @click="resetFilter">重置</a-button>
      </a-space>
    </a-card>

    <!-- 指导记录列表 -->
    <a-spin :spinning="loading">
      <div v-if="filteredList.length > 0" class="guidance-list">
        <a-timeline mode="left">
          <a-timeline-item
            v-for="record in filteredList"
            :key="record.recordId"
            :color="getTimelineColor(record.guidanceType)"
          >
            <template #dot>
              <div class="timeline-dot">
                <BookOutlined v-if="record.guidanceType === GuidanceType.PROJECT" />
                <FileTextOutlined v-else />
              </div>
            </template>
            
            <a-card
              class="guidance-card"
              :bordered="false"
              hoverable
              @click="showRecordDetail(record)"
            >
              <div class="card-header">
                <div class="card-title">
                  <a-tag :color="GuidanceTypeColorMap[record.guidanceType]">
                    {{ GuidanceTypeMap[record.guidanceType] }}
                  </a-tag>
                  <span class="topic-name">{{ record.topicTitle }}</span>
                </div>
                <div class="card-date">
                  <CalendarOutlined />
                  <span>{{ record.guidanceDate }}</span>
                </div>
              </div>
              
              <div class="card-body">
                <div class="teacher-info">
                  <UserOutlined />
                  <span class="teacher-name">{{ record.teacherName }}</span>
                  <span class="guidance-method" v-if="record.guidanceMethod">
                    · {{ record.guidanceMethod }}
                  </span>
                  <span class="duration" v-if="record.durationHours">
                    · {{ record.durationHours }}小时
                  </span>
                </div>
                <p class="content-summary">
                  {{ record.contentSummary || '点击查看详情...' }}
                </p>
              </div>
            </a-card>
          </a-timeline-item>
        </a-timeline>
      </div>

      <!-- 空状态 -->
      <a-empty
        v-else
        description="暂无指导记录"
        :image="Empty.PRESENTED_IMAGE_SIMPLE"
      />
    </a-spin>

    <!-- 详情抽屉 -->
    <a-drawer
      v-model:open="detailDrawerVisible"
      title="指导记录详情"
      placement="right"
      width="500"
    >
      <template v-if="currentRecord">
        <a-descriptions :column="1" bordered size="small">
          <a-descriptions-item label="指导类型">
            <a-tag :color="GuidanceTypeColorMap[currentRecord.guidanceType]">
              {{ GuidanceTypeMap[currentRecord.guidanceType] }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="指导教师">
            {{ currentRecord.teacherName }}
          </a-descriptions-item>
          <a-descriptions-item label="关联课题">
            {{ currentRecord.topicTitle }}
          </a-descriptions-item>
          <a-descriptions-item label="指导日期">
            {{ currentRecord.guidanceDate }}
          </a-descriptions-item>
          <a-descriptions-item label="指导方式" v-if="currentRecord.guidanceMethod">
            {{ currentRecord.guidanceMethod }}
          </a-descriptions-item>
          <a-descriptions-item label="指导时长" v-if="currentRecord.durationHours">
            {{ currentRecord.durationHours }} 小时
          </a-descriptions-item>
        </a-descriptions>
        
        <a-divider orientation="left">指导内容</a-divider>
        <div class="content-detail">
          {{ currentRecord.guidanceContent }}
        </div>
        
        <a-divider />
        <div class="record-meta">
          <p><ClockCircleOutlined /> 记录时间：{{ currentRecord.createTime }}</p>
        </div>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
/**
 * 学生指导记录页面逻辑
 */
import { ref, computed, onMounted } from 'vue'
import { message, Empty } from 'ant-design-vue'
import {
  BookOutlined,
  FileTextOutlined,
  CalendarOutlined,
  UserOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue'
import { guidanceApi } from '@/api/guidance'
import {
  GuidanceType,
  GuidanceTypeMap,
  GuidanceTypeColorMap,
  type GuidanceListVO,
  type GuidanceRecordVO
} from '@/types/guidance'
import dayjs, { type Dayjs } from 'dayjs'

// ==================== 响应式状态 ====================

/** 加载状态 */
const loading = ref<boolean>(false)

/** 指导记录列表 */
const guidanceList = ref<GuidanceListVO[]>([])

/** 筛选类型 */
const filterType = ref<number | undefined>(undefined)

/** 筛选日期范围 */
const dateRange = ref<[Dayjs, Dayjs] | null>(null)

/** 详情抽屉可见性 */
const detailDrawerVisible = ref<boolean>(false)

/** 当前查看的记录详情 */
const currentRecord = ref<GuidanceRecordVO | null>(null)

// ==================== 计算属性 ====================

/**
 * 根据筛选条件过滤后的列表
 */
const filteredList = computed(() => {
  let result = [...guidanceList.value]
  
  // 按类型筛选
  if (filterType.value !== undefined) {
    result = result.filter(item => item.guidanceType === filterType.value)
  }
  
  // 按日期范围筛选
  if (dateRange.value && dateRange.value.length === 2) {
    const [start, end] = dateRange.value
    result = result.filter(item => {
      const date = dayjs(item.guidanceDate)
      return date.isAfter(start.subtract(1, 'day')) && date.isBefore(end.add(1, 'day'))
    })
  }
  
  // 按日期倒序排列
  return result.sort((a, b) => 
    new Date(b.guidanceDate).getTime() - new Date(a.guidanceDate).getTime()
  )
})

/**
 * 统计总指导时长
 */
const totalHours = computed(() => {
  return guidanceList.value.reduce((sum, item) => {
    return sum + (item.durationHours || 0)
  }, 0)
})

// ==================== 方法定义 ====================

/**
 * 加载指导记录列表
 */
const loadGuidanceList = async () => {
  loading.value = true
  try {
    const res = await guidanceApi.getMyGuidanceRecords()
    guidanceList.value = res.data || []
  } catch (error: any) {
    console.error('加载指导记录失败:', error)
    message.error(error.message || '加载指导记录失败')
  } finally {
    loading.value = false
  }
}

/**
 * 获取时间线颜色
 */
const getTimelineColor = (type: number): string => {
  return type === GuidanceType.PROJECT ? '#1890ff' : '#52c41a'
}

/**
 * 筛选条件变化处理
 */
const handleFilterChange = () => {
  // 筛选逻辑已由计算属性处理
}

/**
 * 重置筛选条件
 */
const resetFilter = () => {
  filterType.value = undefined
  dateRange.value = null
}

/**
 * 显示记录详情
 */
const showRecordDetail = async (record: GuidanceListVO) => {
  try {
    const res = await guidanceApi.getGuidanceRecordDetail(record.recordId)
    currentRecord.value = res.data
    detailDrawerVisible.value = true
  } catch (error: any) {
    console.error('加载详情失败:', error)
    message.error(error.message || '加载详情失败')
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadGuidanceList()
})
</script>

<style scoped lang="less">
.student-guidance-container {
  padding: 24px;
  
  .filter-card {
    margin-bottom: 24px;
  }
  
  .guidance-list {
    padding: 16px 0;
    
    .timeline-dot {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: #f0f2f5;
      font-size: 16px;
    }
    
    .guidance-card {
      margin-left: 16px;
      max-width: 800px;
      transition: all 0.3s;
      
      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
        
        .card-title {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .topic-name {
            font-weight: 500;
            color: #333;
          }
        }
        
        .card-date {
          display: flex;
          align-items: center;
          gap: 4px;
          color: #666;
          font-size: 14px;
        }
      }
      
      .card-body {
        .teacher-info {
          display: flex;
          align-items: center;
          gap: 6px;
          color: #666;
          margin-bottom: 8px;
          
          .teacher-name {
            font-weight: 500;
          }
          
          .guidance-method,
          .duration {
            color: #999;
          }
        }
        
        .content-summary {
          color: #666;
          line-height: 1.6;
          margin: 0;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
      }
    }
  }
  
  .content-detail {
    padding: 16px;
    background: #f9f9f9;
    border-radius: 4px;
    line-height: 1.8;
    white-space: pre-wrap;
    word-break: break-word;
  }
  
  .record-meta {
    color: #999;
    font-size: 13px;
    
    p {
      margin: 4px 0;
    }
  }
}
</style>
