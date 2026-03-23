<template>
  <div class="arrangement-detail-page">
    <a-card title="答辩安排详情">
      <div v-if="loading" class="loading-wrap">
        <a-spin tip="加载中..." />
      </div>

      <a-result
        v-else-if="errorState"
        :status="errorState.status"
        :title="errorState.title"
        :sub-title="errorState.subTitle"
      >
        <template #extra>
          <a-space>
            <a-button @click="goBack">返回消息中心</a-button>
            <a-button type="primary" @click="refresh">重新加载</a-button>
          </a-space>
        </template>
      </a-result>

      <a-descriptions v-else :column="2" bordered size="small">
        <a-descriptions-item label="答辩类型">{{ detail?.defenseTypeName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="课题类别">{{ detail?.topicCategory || '-' }}</a-descriptions-item>
        <a-descriptions-item label="对应专业">{{ detail?.majorName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="答辩时间">{{ detail?.defenseTime || '-' }}</a-descriptions-item>
        <a-descriptions-item label="答辩地点">{{ detail?.defenseLocation || '-' }}</a-descriptions-item>
        <a-descriptions-item label="创建人">{{ detail?.creatorName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="报告截止时间" :span="2">{{ detail?.deadline || '-' }}</a-descriptions-item>
        <a-descriptions-item label="答辩小组" :span="2">{{ panelTeacherText }}</a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">{{ detail?.remark || '-' }}</a-descriptions-item>
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
import { defenseApi } from '@/api/defense'
import type { DefenseArrangementVO } from '@/types/defense'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref<DefenseArrangementVO | null>(null)
const errorState = ref<{ status: '404' | '403' | 'error'; title: string; subTitle: string } | null>(null)

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
    errorState.value = {
      status: '404',
      title: '答辩安排链接无效',
      subTitle: '当前链接缺少有效的安排ID，请返回消息中心重新进入。'
    }
    return
  }

  detail.value = null
  errorState.value = null
  loading.value = true

  const resolveErrorState = (msg: string) => {
    if (msg.includes('不存在') || msg.includes('未找到')) {
      return {
        status: '404' as const,
        title: '该答辩安排已不存在',
        subTitle: '该消息对应的安排可能已删除或已失效，请返回消息中心查看最新通知。'
      }
    }
    if (msg.includes('无权') || msg.includes('权限')) {
      return {
        status: '403' as const,
        title: '暂无查看权限',
        subTitle: '仅与该安排关联的教师可查看详情，如有疑问请联系企业负责人确认教师配对关系。'
      }
    }
    return {
      status: 'error' as const,
      title: '加载详情失败',
      subTitle: '服务暂时不可用，请稍后重试。'
    }
  }

  try {
    const res = await defenseApi.getArrangementDetail(arrangementId.value)
    if (res.code === 200) {
      detail.value = res.data
      return
    }

    const fallbackMsg = res.message || '加载答辩安排详情失败'
    errorState.value = resolveErrorState(fallbackMsg)
  } catch (error) {
    const errorMsg = (error as any)?.response?.data?.message || (error as Error)?.message || '加载答辩安排详情失败'
    errorState.value = resolveErrorState(errorMsg)
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
