<template>
  <div class="arrangement-detail-page">
    <a-card title="答辩安排详情">
      <div v-if="loading" class="loading-wrap">
        <a-spin tip="加载中..." />
      </div>

      <a-result
        v-else-if="!detail"
        status="404"
        title="未找到答辩安排"
        sub-title="该安排可能已删除，或您无权访问该安排详情。"
      >
        <template #extra>
          <a-button type="primary" @click="goBack">返回消息中心</a-button>
        </template>
      </a-result>

      <a-descriptions v-else :column="2" bordered size="small">
        <a-descriptions-item label="答辩类型">{{ detail.defenseTypeName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="课题类别">{{ detail.topicCategory || '-' }}</a-descriptions-item>
        <a-descriptions-item label="对应专业">{{ detail.majorName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="答辩时间">{{ detail.defenseTime || '-' }}</a-descriptions-item>
        <a-descriptions-item label="答辩地点">{{ detail.defenseLocation || '-' }}</a-descriptions-item>
        <a-descriptions-item label="创建人">{{ detail.creatorName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="报告截止时间" :span="2">{{ detail.deadline || '-' }}</a-descriptions-item>
        <a-descriptions-item label="答辩小组" :span="2">{{ panelTeacherText }}</a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</a-descriptions-item>
      </a-descriptions>

      <div class="actions" v-if="detail">
        <a-space>
          <a-button @click="goBack">返回消息中心</a-button>
          <a-button type="primary" @click="refresh">刷新</a-button>
        </a-space>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { defenseApi } from '@/api/defense'
import type { DefenseArrangementVO } from '@/types/defense'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref<DefenseArrangementVO | null>(null)

const arrangementId = computed(() => String(route.params.arrangementId || ''))

const panelTeacherText = computed(() => {
  if (!detail.value?.panelTeacherInfos?.length) {
    return '-'
  }
  const teachers = detail.value.panelTeacherInfos
  const leader = teachers[0]?.realName || '-'
  const members = teachers.slice(1).map((item) => item.realName).join('、')
  if (!members) {
    return `组长：${leader}`
  }
  return `组长：${leader}；答辩老师：${members}`
})

const loadDetail = async () => {
  if (!arrangementId.value) {
    detail.value = null
    return
  }
  loading.value = true
  try {
    const res = await defenseApi.getArrangementDetail(arrangementId.value)
    if (res.code === 200) {
      detail.value = res.data
    } else {
      detail.value = null
    }
  } catch (error) {
    detail.value = null
    message.error('加载答辩安排详情失败')
  } finally {
    loading.value = false
  }
}

const refresh = async () => {
  await loadDetail()
}

const goBack = () => {
  router.push('/notification/center')
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.arrangement-detail-page {
  padding: 16px;
}

.loading-wrap {
  text-align: center;
  padding: 40px 0;
}

.actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
