<template>
  <canvas
    ref="canvasRef"
    :width="width"
    :height="height"
    class="captcha-canvas"
    @click="refreshCaptcha"
  />
</template>

<script setup lang="ts">
/**
 * 图形验证码组件
 * 生成4位随机验证码（数字+字母），防止机器人恶意攻击
 */
import { ref, onMounted, watch } from 'vue'

defineOptions({
  name: 'CaptchaCanvas'
})

interface Props {
  /** 画布宽度 */
  width?: number
  /** 画布高度 */
  height?: number
  /** 验证码长度 */
  length?: number
}

const props = withDefaults(defineProps<Props>(), {
  width: 120,
  height: 40,
  length: 4
})

const emit = defineEmits<{
  change: [code: string]
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)
const captchaCode = ref('')

/** 生成随机字符（数字+大写字母，排除易混淆字符 0O1lI） */
const getRandomChar = (): string => {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789'
  return chars.charAt(Math.floor(Math.random() * chars.length))
}

/** 生成随机颜色 */
const getRandomColor = (min: number, max: number): string => {
  const r = Math.floor(Math.random() * (max - min) + min)
  const g = Math.floor(Math.random() * (max - min) + min)
  const b = Math.floor(Math.random() * (max - min) + min)
  return `rgb(${r}, ${g}, ${b})`
}

/** 生成随机数 */
const getRandomNum = (min: number, max: number): number => {
  return Math.floor(Math.random() * (max - min) + min)
}

/** 绘制验证码 */
const drawCaptcha = () => {
  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const { width, height, length } = props

  // 清空画布
  ctx.clearRect(0, 0, width, height)

  // 绘制背景色
  ctx.fillStyle = getRandomColor(200, 240)
  ctx.fillRect(0, 0, width, height)

  // 生成验证码字符
  let code = ''
  for (let i = 0; i < length; i++) {
    const char = getRandomChar()
    code += char

    // 绘制字符
    ctx.font = `${getRandomNum(22, 30)}px 'Consolas', 'Monaco', monospace`
    ctx.fillStyle = getRandomColor(50, 160)
    ctx.textBaseline = 'middle'

    // 旋转角度
    const deg = getRandomNum(-15, 15)
    const x = 10 + i * (width - 20) / length
    const y = height / 2 + getRandomNum(-4, 4)

    ctx.save()
    ctx.translate(x, y)
    ctx.rotate((deg * Math.PI) / 180)
    ctx.fillText(char, 0, 0)
    ctx.restore()
  }

  // 绘制干扰线
  for (let i = 0; i < 4; i++) {
    ctx.strokeStyle = getRandomColor(100, 200)
    ctx.lineWidth = 1
    ctx.beginPath()
    ctx.moveTo(getRandomNum(0, width), getRandomNum(0, height))
    ctx.lineTo(getRandomNum(0, width), getRandomNum(0, height))
    ctx.stroke()
  }

  // 绘制干扰点
  for (let i = 0; i < 30; i++) {
    ctx.fillStyle = getRandomColor(150, 230)
    ctx.beginPath()
    ctx.arc(getRandomNum(0, width), getRandomNum(0, height), 1, 0, 2 * Math.PI)
    ctx.fill()
  }

  captchaCode.value = code
  emit('change', code)
}

/** 刷新验证码 */
const refreshCaptcha = () => {
  drawCaptcha()
}

/** 获取当前验证码值 */
const getCode = (): string => {
  return captchaCode.value
}

// 挂载后绘制
onMounted(() => {
  drawCaptcha()
})

// 监听尺寸变化重绘
watch(() => [props.width, props.height, props.length], () => {
  drawCaptcha()
})

// 暴露方法
defineExpose({
  refreshCaptcha,
  getCode
})
</script>

<style scoped>
.captcha-canvas {
  cursor: pointer;
  border-radius: 4px;
  vertical-align: middle;
  user-select: none;
}
</style>
