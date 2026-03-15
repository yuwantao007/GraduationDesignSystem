<template>
  <div class="process-definition">
    <a-page-header
      title="审查流程定义"
      sub-title="课题审查 Flowable 流程图可视化"
      style="padding: 0 0 16px 0"
    />

    <!-- 流程路径说明卡片 -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="12">
        <a-card size="small" class="process-definition__path-card process-definition__path-card--a">
          <template #title>
            <a-tag color="blue" style="font-size: 13px">路径 A</a-tag>
            高职升本课题（topicCategory = 1）
          </template>
          <a-steps
            direction="horizontal"
            size="small"
            :current="4"
          >
            <a-step title="提交" description="企业教师" />
            <a-step title="预审" description="高校教师" />
            <a-step title="初审" description="专业方向主管" />
            <a-step title="终审" description="督导教师" />
            <a-step title="完成" />
          </a-steps>
          <div class="process-definition__note">
            <InfoCircleOutlined />
            任意阶段返回"需修改"时，企业教师完成修改后可重新提交，流程回到该阶段首节点。
          </div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card size="small" class="process-definition__path-card process-definition__path-card--b">
          <template #title>
            <a-tag color="green" style="font-size: 13px">路径 B</a-tag>
            3+1 / 实验班课题（topicCategory = 2 / 3）
          </template>
          <a-steps
            direction="horizontal"
            size="small"
            :current="3"
          >
            <a-step title="提交" description="企业教师" />
            <a-step title="初审" description="专业方向主管" />
            <a-step title="终审" description="高校教师" />
            <a-step title="完成" />
          </a-steps>
          <div class="process-definition__note">
            <InfoCircleOutlined />
            初审"需修改"时回到初审节点，终审仅支持"通过"或"不通过"。
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图例说明 -->
    <a-card size="small" style="margin-bottom: 16px">
      <a-space size="large">
        <span class="process-definition__legend">
          <span class="process-definition__legend-dot process-definition__legend-dot--active" />
          活跃节点（橙色）
        </span>
        <span class="process-definition__legend">
          <span class="process-definition__legend-dot process-definition__legend-dot--completed" />
          已完成节点（绿色）
        </span>
        <a-divider type="vertical" />
        <span style="color: #8c8c8c; font-size: 12px">
          流程定义版本：{{ processVersion || '加载中...' }}
        </span>
        <a-button size="small" @click="loadDefinition" :loading="loading">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </a-space>
    </a-card>

    <!-- BPMN 流程图 -->
    <a-card
      title="BPMN 流程图"
      size="small"
      :loading="loading"
    >
      <template #extra>
        <a-space>
          <a-tooltip title="流程图展示课题审查的完整 BPMN 定义，包含两条审查路径和修改循环节点">
            <QuestionCircleOutlined style="color: #8c8c8c; cursor: pointer" />
          </a-tooltip>
        </a-space>
      </template>

      <div v-if="errorMsg" style="padding: 24px">
        <a-result
          status="warning"
          title="流程定义加载失败"
          :sub-title="errorMsg"
        >
          <template #extra>
            <a-button type="primary" @click="loadDefinition">重新加载</a-button>
          </template>
        </a-result>
      </div>

      <div v-else class="process-definition__diagram-container">
        <BpmnViewer
          v-if="bpmnXml"
          :bpmn-xml="bpmnXml"
        />
        <div v-else-if="!loading" class="process-definition__placeholder">
          <a-spin />
          <p style="margin-top: 12px; color: #8c8c8c">正在初始化流程图...</p>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  ReloadOutlined,
  InfoCircleOutlined,
  QuestionCircleOutlined
} from '@ant-design/icons-vue'
import { workflowApi } from '@/api/workflow'
import BpmnViewer from '@/components/workflow/BpmnViewer.vue'

defineOptions({
  name: 'ProcessDefinition'
})

const loading = ref(false)
const bpmnXml = ref<string | null>(null)
const errorMsg = ref<string | null>(null)
const processVersion = ref<string | null>(null)

async function loadDefinition() {
  loading.value = true
  errorMsg.value = null
  bpmnXml.value = null
  try {
    const xml = await workflowApi.getProcessDefinitionXml()
    bpmnXml.value = xml
    // 从 BPMN XML 中提取流程版本信息（格式：process id="topic_review"）
    const versionMatch = xml.match(/flowable:versionTag="([^"]+)"/)
    processVersion.value = versionMatch ? versionMatch[1] : 'v1.0'
  } catch (e: any) {
    const msg = e?.message || '加载失败，请确认后端已正常启动且流程已部署'
    errorMsg.value = msg
    message.error('加载流程定义失败：' + msg)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDefinition()
})
</script>

<style scoped>
.process-definition {
  padding: 24px;
}

.process-definition__path-card {
  height: 100%;
}

.process-definition__path-card--a {
  border-top: 3px solid #1677ff;
}

.process-definition__path-card--b {
  border-top: 3px solid #52c41a;
}

.process-definition__note {
  margin-top: 10px;
  font-size: 12px;
  color: #8c8c8c;
  line-height: 1.5;
  display: flex;
  align-items: flex-start;
  gap: 4px;
}

.process-definition__legend {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #595959;
}

.process-definition__legend-dot {
  display: inline-block;
  width: 14px;
  height: 14px;
  border-radius: 2px;
  border: 2px solid;
  flex-shrink: 0;
}

.process-definition__legend-dot--active {
  border-color: #faad14;
  background: #fff7e6;
}

.process-definition__legend-dot--completed {
  border-color: #52c41a;
  background: #f6ffed;
}

.process-definition__diagram-container {
  height: 540px;
}

.process-definition__placeholder {
  height: 540px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
</style>
