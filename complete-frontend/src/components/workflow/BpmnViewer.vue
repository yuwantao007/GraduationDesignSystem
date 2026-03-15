<template>
  <div class="bpmn-viewer-wrapper">
    <div v-if="loading" class="bpmn-loading">
      <a-spin tip="正在加载流程图..." />
    </div>
    <div v-if="error" class="bpmn-error">
      <a-result status="error" :title="error" />
    </div>
    <div ref="containerRef" class="bpmn-container" />
    <!-- 工具栏 -->
    <div class="bpmn-toolbar">
      <a-button-group size="small">
        <a-button @click="zoomIn"><ZoomInOutlined /></a-button>
        <a-button @click="zoomOut"><ZoomOutOutlined /></a-button>
        <a-button @click="fitView">适配</a-button>
      </a-button-group>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { ZoomInOutlined, ZoomOutOutlined } from '@ant-design/icons-vue'

const props = defineProps<{
  /** BPMN 2.0 XML 字符串 */
  bpmnXml: string | null
  /** 需要高亮的节点 activityId 列表（当前活跃节点） */
  activeActivityIds?: string[]
  /** 需要标记为已完成的节点 activityId 列表 */
  completedActivityIds?: string[]
}>()

const containerRef = ref<HTMLElement | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

// bpmn-js 实例（懒加载，避免 SSR 问题）
let viewer: any = null

async function initViewer() {
  if (!containerRef.value || !props.bpmnXml) return
  loading.value = true
  error.value = null

  try {
    // 动态导入 bpmn-js（避免 SSR 问题）
    const BpmnViewer = (await import('bpmn-js')).default

    if (viewer) {
      viewer.destroy()
      viewer = null
    }

    viewer = new BpmnViewer({ container: containerRef.value })
    await viewer.importXML(props.bpmnXml)

    // 自适应视图
    viewer.get('canvas').zoom('fit-viewport', 'auto')

    // 高亮活跃节点（橙色）
    applyHighlights()
  } catch (e: any) {
    error.value = '流程图加载失败：' + (e?.message || '未知错误')
    console.error('[BpmnViewer] 加载失败', e)
  } finally {
    loading.value = false
  }
}

function applyHighlights() {
  if (!viewer) return
  const canvas = viewer.get('canvas')
  const elementRegistry = viewer.get('elementRegistry')

  // 高亮活跃节点
  if (props.activeActivityIds?.length) {
    props.activeActivityIds.forEach(id => {
      try {
        const element = elementRegistry.get(id)
        if (element) {
          canvas.addMarker(id, 'highlight-active')
        }
      } catch {
        // 节点不在图中，静默忽略
      }
    })
  }

  // 标记已完成节点
  if (props.completedActivityIds?.length) {
    props.completedActivityIds.forEach(id => {
      try {
        const element = elementRegistry.get(id)
        if (element && !(props.activeActivityIds || []).includes(id)) {
          canvas.addMarker(id, 'highlight-completed')
        }
      } catch {
        // 忽略
      }
    })
  }
}

function zoomIn() {
  viewer?.get('zoomScroll')?.zoom(0.1)
}

function zoomOut() {
  viewer?.get('zoomScroll')?.zoom(-0.1)
}

function fitView() {
  viewer?.get('canvas')?.zoom('fit-viewport', 'auto')
}

watch(() => props.bpmnXml, (newVal) => {
  if (newVal) initViewer()
})

watch(() => [props.activeActivityIds, props.completedActivityIds], () => {
  applyHighlights()
}, { deep: true })

onMounted(() => {
  if (props.bpmnXml) initViewer()
})

onUnmounted(() => {
  if (viewer) {
    viewer.destroy()
    viewer = null
  }
})
</script>

<style scoped>
.bpmn-viewer-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 400px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  overflow: hidden;
  background: #fafafa;
}

.bpmn-container {
  width: 100%;
  height: 100%;
  min-height: 400px;
}

.bpmn-loading,
.bpmn-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
}

.bpmn-toolbar {
  position: absolute;
  bottom: 12px;
  right: 12px;
  z-index: 10;
}
</style>

<style>
/* bpmn-js 节点高亮样式（全局，因为 bpmn-js 动态插入 SVG） */
.bjs-container .highlight-active .djs-visual > :is(rect, circle, polygon) {
  stroke: #faad14 !important;
  stroke-width: 3px !important;
  fill: #fff7e6 !important;
}

.bjs-container .highlight-completed .djs-visual > :is(rect, circle, polygon) {
  stroke: #52c41a !important;
  fill: #f6ffed !important;
}
</style>
