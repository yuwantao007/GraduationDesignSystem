<template>
  <div class="my-taskbook">
    <a-card title="我的任务书">
      <template v-if="taskBookData">
        <a-descriptions bordered :column="1">
          <a-descriptions-item label="课题名称">
            {{ taskBookData.topicName }}
          </a-descriptions-item>
          <a-descriptions-item label="指导教师">
            {{ taskBookData.teacherName }}
          </a-descriptions-item>
          <a-descriptions-item label="任务书内容">
            <div v-html="taskBookData.content" class="taskbook-content"></div>
          </a-descriptions-item>
          <a-descriptions-item label="附件" v-if="taskBookData.documentId">
            <a :href="taskBookData.documentUrl" target="_blank">
              {{ taskBookData.documentName || '下载附件' }}
            </a>
          </a-descriptions-item>
          <a-descriptions-item label="更新时间">
            {{ taskBookData.updateTime }}
          </a-descriptions-item>
        </a-descriptions>
      </template>
      <template v-else>
        <a-empty description="指导教师尚未为您编写任务书" />
      </template>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { defenseApi } from '@/api/defense'
import type { OpeningTaskBookVO } from '@/types/defense'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const taskBookData = ref<OpeningTaskBookVO | null>(null)

const fetchMyTaskBook = async () => {
  loading.value = true
  try {
    const userId = userStore.userInfo?.userId
    if (userId) {
      const res = await defenseApi.getTaskBookByStudent(userId)
      if (res.code === 200) {
        taskBookData.value = res.data
      }
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchMyTaskBook()
})
</script>

<style scoped>
.my-taskbook {
  padding: 16px;
}
.taskbook-content {
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.8;
}
</style>
