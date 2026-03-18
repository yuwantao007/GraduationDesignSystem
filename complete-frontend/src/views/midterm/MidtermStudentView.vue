<!--
  学生中期检查查看页面
  功能：学生查看自己的中期检查表状态
  @author 系统架构师
  @version 1.0
  @since 2026-03-17
-->
<template>
  <div class="midterm-student-view">
    <a-card title="我的中期检查表" :bordered="false">
      <a-spin :spinning="loading">
        <!-- 已有中期检查表 -->
        <template v-if="checkData">
          <a-descriptions :column="1" bordered>
            <a-descriptions-item label="课题名称">
              {{ checkData.topicName }}
            </a-descriptions-item>
            <a-descriptions-item label="企业教师">
              {{ checkData.enterpriseTeacherName }}
            </a-descriptions-item>
            <a-descriptions-item label="提交状态">
              <a-tag :color="checkData.submitStatus === 1 ? 'processing' : 'default'">
                {{ checkData.submitStatusDesc }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item v-if="checkData.submitStatus === 1" label="审查状态">
              <a-tag :color="getReviewStatusColor(checkData.reviewStatus)">
                {{ checkData.reviewStatusDesc }}
              </a-tag>
            </a-descriptions-item>
          </a-descriptions>

          <!-- 已提交时显示详细内容 -->
          <template v-if="checkData.submitStatus === 1">
            <a-divider>检查表内容</a-divider>
            <a-descriptions :column="1" bordered>
              <a-descriptions-item label="完成情况">
                <a-typography-paragraph
                  v-if="checkData.completionStatus"
                  :ellipsis="{ rows: 4, expandable: true }"
                >
                  {{ checkData.completionStatus }}
                </a-typography-paragraph>
                <span v-else style="color: #bfbfbf">未填写</span>
              </a-descriptions-item>
              <a-descriptions-item label="存在问题">
                <a-typography-paragraph
                  v-if="checkData.existingProblems"
                  :ellipsis="{ rows: 4, expandable: true }"
                >
                  {{ checkData.existingProblems }}
                </a-typography-paragraph>
                <span v-else style="color: #bfbfbf">未填写</span>
              </a-descriptions-item>
              <a-descriptions-item label="下一步计划">
                <a-typography-paragraph
                  v-if="checkData.nextPlan"
                  :ellipsis="{ rows: 4, expandable: true }"
                >
                  {{ checkData.nextPlan }}
                </a-typography-paragraph>
                <span v-else style="color: #bfbfbf">未填写</span>
              </a-descriptions-item>
              <a-descriptions-item label="提交时间">
                {{ checkData.updateTime }}
              </a-descriptions-item>
            </a-descriptions>

            <!-- 审查结果 -->
            <template v-if="checkData.reviewStatus !== 0">
              <a-divider>审查结果</a-divider>
              <a-descriptions :column="1" bordered>
                <a-descriptions-item label="审查状态">
                  <a-tag :color="getReviewStatusColor(checkData.reviewStatus)">
                    {{ checkData.reviewStatusDesc }}
                  </a-tag>
                </a-descriptions-item>
                <a-descriptions-item v-if="checkData.reviewerName" label="审查人">
                  {{ checkData.reviewerName }}
                </a-descriptions-item>
                <a-descriptions-item v-if="checkData.reviewTime" label="审查时间">
                  {{ checkData.reviewTime }}
                </a-descriptions-item>
                <a-descriptions-item v-if="checkData.reviewComment" label="审查意见">
                  <a-typography-paragraph>{{ checkData.reviewComment }}</a-typography-paragraph>
                </a-descriptions-item>
              </a-descriptions>

              <!-- 审查结果提示 -->
              <a-alert
                v-if="checkData.reviewStatus === 1"
                type="success"
                message="恭喜！您的中期检查表已通过审查。"
                show-icon
                style="margin-top: 16px"
              />
              <a-alert
                v-else-if="checkData.reviewStatus === 2"
                type="error"
                message="您的中期检查表未通过审查，请与企业教师沟通后重新提交。"
                show-icon
                style="margin-top: 16px"
              />
            </template>

            <!-- 等待审查提示 -->
            <a-alert
              v-else
              type="info"
              message="您的中期检查表已提交，正在等待高校教师审查。"
              show-icon
              style="margin-top: 16px"
            />
          </template>

          <!-- 未提交时显示提示 -->
          <template v-else>
            <a-alert
              type="warning"
              message="您的中期检查表尚未提交，请等待企业教师填写并提交。"
              show-icon
              style="margin-top: 16px"
            />
          </template>
        </template>

        <!-- 暂无中期检查表 -->
        <a-empty v-else description="暂无中期检查表">
          <template #image>
            <FileTextOutlined style="font-size: 64px; color: #d9d9d9" />
          </template>
          <a-typography-paragraph type="secondary" style="margin-top: 16px">
            中期检查表由企业教师为您创建和填写，请耐心等待。
          </a-typography-paragraph>
        </a-empty>
      </a-spin>

      <!-- 刷新按钮 -->
      <div style="margin-top: 24px; text-align: center">
        <a-button @click="loadData">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { ReloadOutlined, FileTextOutlined } from '@ant-design/icons-vue'
import { midtermApi } from '@/api/midterm'
import type { MidtermCheckVO } from '@/types/midterm'
import { getReviewStatusColor } from '@/types/midterm'

// 数据
const checkData = ref<MidtermCheckVO | null>(null)
const loading = ref(false)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await midtermApi.getStudentCheck()
    checkData.value = res.data || null
  } catch (error: any) {
    // 404表示暂无数据，不显示错误
    if (error.code !== 404) {
      message.error(error.message || '加载失败')
    }
    checkData.value = null
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.midterm-student-view {
  padding: 24px;
}
</style>
