<template>
  <div class="big-screen">
    <!-- Header -->
    <header class="bs-header">
      <div class="bs-header-left">
        <div class="bs-logo">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <rect width="24" height="24" rx="5" fill="currentColor" fill-opacity="0.2"/>
            <path d="M6 15V9l3 3 2-2 3 3 4-4v6H6z" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="9.5" cy="10.5" r="1.2" fill="currentColor"/>
          </svg>
        </div>
        <span class="bs-system-name">AI 校园学业就业智能服务平台</span>
      </div>
      <div class="bs-header-center">
        <h1 class="bs-title">校方数据大屏</h1>
      </div>
      <div class="bs-header-right">
        <span class="bs-time">{{ currentTime }}</span>
      </div>
    </header>

    <!-- Grid -->
    <div class="bs-grid">
      <!-- Row 1: 6 stat cards -->
      <div class="bs-stat-row">
        <div
          class="bs-stat-card"
          v-for="c in counts"
          :key="c.label"
        >
          <div class="bs-stat-icon" :style="{ background: c.color + '22', color: c.color }">
            <el-icon :size="18"><component :is="c.icon" /></el-icon>
          </div>
          <div class="bs-stat-body">
            <span class="bs-stat-num">{{ c.value }}</span>
            <span class="bs-stat-label">{{ c.label }}</span>
          </div>
        </div>
      </div>

      <!-- Row 2: Student distribution + Warning situation + Employment match -->
      <div class="bs-panel">
        <div class="bs-panel-header">
          <h3>学生专业分布</h3>
        </div>
        <div class="bs-panel-body">
          <div ref="c1" class="chart"></div>
        </div>
      </div>

      <div class="bs-panel">
        <div class="bs-panel-header">
          <h3>学业预警态势</h3>
        </div>
        <div class="bs-panel-body">
          <div ref="c2" class="chart"></div>
        </div>
      </div>

      <div class="bs-panel">
        <div class="bs-panel-header">
          <h3>就业匹配统计</h3>
        </div>
        <div class="bs-panel-body">
          <div ref="c3" class="chart"></div>
        </div>
      </div>

      <!-- Row 3: Live warnings + Top jobs + GPA distribution -->
      <div class="bs-panel bs-panel--scroll">
        <div class="bs-panel-header">
          <h3>实时预警</h3>
          <span class="bs-badge pulse-soft">LIVE</span>
        </div>
        <div class="bs-panel-body">
          <div class="scroll-list">
            <div
              v-for="(w, i) in warnings"
              :key="i"
              class="scroll-item"
            >
              <span class="warn-dot" :class="'level-' + w.level"></span>
              <div class="warn-body">
                <span class="warn-title">{{ w.title }}</span>
                <span class="warn-meta">{{ w.studentName }} · {{ w.studentNo }}</span>
              </div>
              <span class="warn-time">{{ w.time }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="bs-panel">
        <div class="bs-panel-header">
          <h3>热门岗位 TOP 10</h3>
        </div>
        <div class="bs-panel-body">
          <div ref="c4" class="chart"></div>
        </div>
      </div>

      <div class="bs-panel">
        <div class="bs-panel-header">
          <h3>GPA 分布</h3>
        </div>
        <div class="bs-panel-body">
          <div ref="c5" class="chart"></div>
        </div>
      </div>

      <!-- Row 4: Interview pass rate + Employment progress by major -->
      <div class="bs-panel">
        <div class="bs-panel-header">
          <h3>面试通过率趋势</h3>
        </div>
        <div class="bs-panel-body">
          <div ref="c6" class="chart"></div>
        </div>
      </div>

      <div class="bs-panel bs-panel--wide">
        <div class="bs-panel-header">
          <h3>各专业就业进展</h3>
        </div>
        <div class="bs-panel-body">
          <div ref="c7" class="chart"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

const currentTime = ref('')
let timer: ReturnType<typeof setInterval> | null = null

function updateTime() {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const hh = String(now.getHours()).padStart(2, '0')
  const mm = String(now.getMinutes()).padStart(2, '0')
  const ss = String(now.getSeconds()).padStart(2, '0')
  const week = ['日', '一', '二', '三', '四', '五', '六']
  currentTime.value = `${y}年${m}月${d}日 星期${week[now.getDay()]} ${hh}:${mm}:${ss}`
}

const counts = [
  { label: '在校学生', value: '12,480', color: '#818cf8', icon: 'User' },
  { label: '预警学生', value: '286', color: '#f59e0b', icon: 'Warning' },
  { label: '应届毕业生', value: '3,150', color: '#22c55e', icon: 'School' },
  { label: '已就业', value: '2,186', color: '#a78bfa', icon: 'Briefcase' },
  { label: '就业率', value: '69.4%', color: '#f87171', icon: 'TrendCharts' },
  { label: '平均 GPA', value: '3.12', color: '#38bdf8', icon: 'DataLine' },
]

const warnings = [
  { title: '张伟 GPA 低于 2.0 预警', studentName: '张伟', studentNo: '2021001006', level: 3, time: '10:32' },
  { title: '孙悦 挂科数超过 3 门预警', studentName: '孙悦', studentNo: '2021001007', level: 3, time: '09:45' },
  { title: '刘洋 学分未达标预警', studentName: '刘洋', studentNo: '2021001004', level: 2, time: '08:20' },
  { title: '周杰 出勤率低于 70%', studentName: '周杰', studentNo: '2021001008', level: 2, time: '昨天' },
  { title: '王芳 GPA 持续下降', studentName: '王芳', studentNo: '2021001002', level: 1, time: '昨天' },
  { title: '赵磊 学分修读进度滞后', studentName: '赵磊', studentNo: '2021001006', level: 1, time: '2天前' },
  { title: '陈静 挂科预警已处理', studentName: '陈静', studentNo: '2021001005', level: 1, time: '3天前' },
]

const c1 = ref<HTMLElement>()
const c2 = ref<HTMLElement>()
const c3 = ref<HTMLElement>()
const c4 = ref<HTMLElement>()
const c5 = ref<HTMLElement>()
const c6 = ref<HTMLElement>()
const c7 = ref<HTMLElement>()
const charts: echarts.ECharts[] = []

/* Shared dark theme helpers */
const textColor = '#8899aa'
const textColorBright = '#aabbcc'
const gridColor = 'rgba(255,255,255,0.06)'

function darkGrid() {
  return { top: 40, right: 20, bottom: 30, left: 50, show: true, borderColor: gridColor }
}
function darkXAxis(data: string[]) {
  return { type: 'category' as const, data, axisLine: { lineStyle: { color: gridColor } }, axisTick: { show: false }, axisLabel: { color: textColor, fontSize: 11 } }
}
function darkYAxis() {
  return { type: 'value' as const, splitLine: { lineStyle: { color: gridColor } }, axisLabel: { color: textColor, fontSize: 11 } }
}

const chartColors = ['#818cf8', '#38bdf8', '#22c55e', '#f59e0b', '#f87171', '#a78bfa', '#fb923c', '#4ade80']

onMounted(async () => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  // nextTick 仅确保 Vue DOM 更新，不等于浏览器布局计算完成。
  // 双帧 requestAnimationFrame 确保 Grid + Flexbox 容器尺寸已确定。
  await nextTick()
  await new Promise(r => requestAnimationFrame(() => requestAnimationFrame(r)))
  initCharts()
  // 初始化后再次 resize，兜底异步布局延迟
  requestAnimationFrame(() => charts.forEach(c => { try { c.resize() } catch {} }))
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  charts.forEach(c => c.dispose())
})

function initCharts() {
  // Chart 1: Student major distribution (pie)
  if (c1.value) {
    const ch = echarts.init(c1.value)
    ch.setOption({
      color: chartColors,
      tooltip: { trigger: 'item', backgroundColor: '#1e293b', borderColor: '#334155', textStyle: { color: '#e2e8f0' } },
      series: [{
        type: 'pie', radius: ['55%', '80%'], center: ['50%', '55%'],
        label: { color: textColor, fontSize: 11 },
        emphasis: { label: { fontSize: 14, fontWeight: 'bold' } },
        data: [
          { value: 3200, name: '计算机科学' },
          { value: 2800, name: '软件工程' },
          { value: 2100, name: '数据科学' },
          { value: 1800, name: '人工智能' },
          { value: 1400, name: '网络安全' },
          { value: 1180, name: '物联网工程' },
        ]
      }]
    })
    charts.push(ch)
  }

  // Chart 2: Warning trend (line)
  if (c2.value) {
    const ch = echarts.init(c2.value)
    ch.setOption({
      color: [chartColors[4], chartColors[3], chartColors[0]], // 红→橙→黄
      tooltip: { trigger: 'axis', backgroundColor: '#1e293b', borderColor: '#334155', textStyle: { color: '#e2e8f0' } },
      legend: { top: 0, textStyle: { color: textColor, fontSize: 11 } },
      grid: { top: 35, right: 20, bottom: 20, left: 45, containLabel: true },
      xAxis: darkXAxis(['9月', '10月', '11月', '12月', '1月', '2月', '3月', '4月']),
      yAxis: { ...darkYAxis(), min: 0 },
      series: [
        { name: '红色预警', type: 'line', smooth: true, data: [8, 12, 10, 18, 15, 9, 14, 6], areaStyle: { opacity: 0.08 } },
        { name: '橙色预警', type: 'line', smooth: true, data: [15, 18, 22, 25, 20, 16, 19, 10], areaStyle: { opacity: 0.08 } },
        { name: '黄色预警', type: 'line', smooth: true, data: [30, 35, 28, 32, 25, 20, 22, 15], areaStyle: { opacity: 0.08 } },
      ]
    })
    charts.push(ch)
  }

  // Chart 3: Employment match stats (bar)
  if (c3.value) {
    const ch = echarts.init(c3.value)
    ch.setOption({
      color: chartColors,
      tooltip: { trigger: 'axis', backgroundColor: '#1e293b', borderColor: '#334155', textStyle: { color: '#e2e8f0' } },
      grid: darkGrid(),
      xAxis: darkXAxis(['已匹配', '已投递', '面试中', '已录用', '未匹配']),
      yAxis: darkYAxis(),
      series: [{
        type: 'bar', barWidth: 24, itemStyle: { borderRadius: [4, 4, 0, 0] },
        data: [
          { value: 1200, itemStyle: { color: chartColors[0] } },
          { value: 860, itemStyle: { color: chartColors[1] } },
          { value: 340, itemStyle: { color: chartColors[2] } },
          { value: 680, itemStyle: { color: chartColors[3] } },
          { value: 420, itemStyle: { color: textColor } },
        ]
      }]
    })
    charts.push(ch)
  }

  // Chart 4: Top jobs (horizontal bar)
  if (c4.value) {
    const ch = echarts.init(c4.value)
    const jobs = ['前端开发', 'Java 后端', '数据分析师', 'AI 算法', '测试工程师', '产品经理', '运维工程师', 'UI 设计师', '网络安全', '嵌入式']
    const values = [185, 162, 148, 132, 115, 98, 85, 72, 60, 52]
    // 不直接 mutate 原数组，用 toReversed() 或手动反转
    const yLabels = [...jobs].reverse()
    const yValues = [...values].reverse()
    ch.setOption({
      tooltip: { trigger: 'axis', backgroundColor: '#1e293b', borderColor: '#334155', textStyle: { color: '#e2e8f0' } },
      grid: { top: 10, right: 40, bottom: 10, left: 10, containLabel: true },
      xAxis: { type: 'value', splitLine: { lineStyle: { color: gridColor } }, axisLabel: { color: textColor, fontSize: 10 } },
      yAxis: { type: 'category', data: yLabels, axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: textColorBright, fontSize: 11 } },
      series: [{
        type: 'bar', barWidth: 14, data: yValues.map((v, i) => ({
          value: v,
          itemStyle: { color: chartColors[i % chartColors.length], borderRadius: [0, 3, 3, 0] }
        })),
        label: { show: true, position: 'right', color: textColor, fontSize: 10 }
      }]
    })
    charts.push(ch)
  }

  // Chart 5: GPA distribution (bar)
  if (c5.value) {
    const ch = echarts.init(c5.value)
    ch.setOption({
      color: [chartColors[1]],
      tooltip: { trigger: 'axis', backgroundColor: '#1e293b', borderColor: '#334155', textStyle: { color: '#e2e8f0' } },
      grid: darkGrid(),
      xAxis: darkXAxis(['<2.0', '2.0–2.5', '2.5–3.0', '3.0–3.5', '3.5–3.8', '>3.8']),
      yAxis: darkYAxis(),
      series: [{
        type: 'bar', barWidth: 28,
        itemStyle: { borderRadius: [4, 4, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: chartColors[1] }, { offset: 1, color: '#0ea5e9' }
        ])},
        data: [120, 480, 2100, 5200, 3800, 780]
      }]
    })
    charts.push(ch)
  }

  // Chart 6: Interview pass rate (line area)
  if (c6.value) {
    const ch = echarts.init(c6.value)
    ch.setOption({
      color: [chartColors[2]],
      tooltip: { trigger: 'axis', backgroundColor: '#1e293b', borderColor: '#334155', textStyle: { color: '#e2e8f0' } },
      grid: darkGrid(),
      xAxis: darkXAxis(['1月', '2月', '3月', '4月', '5月']),
      yAxis: { ...darkYAxis(), max: 100, axisLabel: { color: textColor, fontSize: 11, formatter: '{value}%' } },
      series: [{
        type: 'line', smooth: true, areaStyle: { opacity: 0.15 },
        lineStyle: { width: 2 }, symbol: 'circle', symbolSize: 6,
        data: [62, 68, 71, 75, 82]
      }]
    })
    charts.push(ch)
  }

  // Chart 7: Employment progress by major
  if (c7.value) {
    const ch = echarts.init(c7.value)
    const majors = ['计算机科学', '软件工程', '大数据', '人工智能', '网络安全']
    ch.setOption({
      color: chartColors,
      tooltip: { trigger: 'axis', backgroundColor: '#1e293b', borderColor: '#334155', textStyle: { color: '#e2e8f0' } },
      legend: { bottom: 0, textStyle: { color: textColor, fontSize: 11 } },
      grid: { top: 40, right: 20, bottom: 40, left: 50 },
      xAxis: darkXAxis(majors),
      yAxis: { ...darkYAxis(), axisLabel: { color: textColor, fontSize: 11, formatter: '{value}%' } },
      series: [
        { name: '已签约', type: 'bar', stack: 'total', barWidth: 32, data: [45, 52, 38, 42, 48], itemStyle: { borderRadius: [0, 0, 0, 0] } },
        { name: '实习中', type: 'bar', stack: 'total', barWidth: 32, data: [20, 18, 22, 15, 19] },
        { name: '求职中', type: 'bar', stack: 'total', barWidth: 32, data: [25, 20, 30, 28, 23], itemStyle: { borderRadius: [4, 4, 0, 0] } },
      ]
    })
    charts.push(ch)
  }
}

/* Resize handler */
function handleResize() {
  charts.forEach(c => c.resize())
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.big-screen {
  min-height: 100vh;
  background: #0f1724;
  color: #cbd5e1;
  padding: 0;
  overflow-x: hidden;
}

/* ------------------------------------------------------------------
   Header
   ------------------------------------------------------------------ */
.bs-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-6);
  height: 56px;
  background: linear-gradient(180deg, rgba(255,255,255,0.03) 0%, transparent 100%);
  border-bottom: 1px solid rgba(255,255,255,0.06);
}

.bs-header-left,
.bs-header-right {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 200px;
}

.bs-header-right {
  justify-content: flex-end;
}

.bs-logo {
  color: #818cf8;
  display: flex;
}

.bs-system-name {
  font-size: var(--font-size-sm);
  color: #64748b;
}

.bs-header-center {
  text-align: center;
}

.bs-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: #e2e8f0;
  letter-spacing: 0.04em;
}

.bs-time {
  font-size: var(--font-size-sm);
  color: #64748b;
  font-variant-numeric: tabular-nums;
}

/* ------------------------------------------------------------------
   Grid
   ------------------------------------------------------------------ */
.bs-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
  padding: var(--space-4) var(--space-6) var(--space-6);
}

.bs-panel--wide {
  grid-column: span 2;
}

/* ------------------------------------------------------------------
   Stat Row
   ------------------------------------------------------------------ */
.bs-stat-row {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: var(--space-3);
}

.bs-stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: var(--radius-lg);
  transition: background var(--transition-fast);
}

.bs-stat-card:hover {
  background: rgba(255, 255, 255, 0.05);
}

.bs-stat-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.bs-stat-body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.bs-stat-num {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: #e2e8f0;
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}

.bs-stat-label {
  font-size: var(--font-size-xs);
  color: #64748b;
  margin-top: 2px;
}

/* ------------------------------------------------------------------
   Panels
   ------------------------------------------------------------------ */
.bs-panel {
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: var(--radius-lg);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.bs-panel-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-5);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.bs-panel-header h3 {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: #94a3b8;
  letter-spacing: 0.01em;
}

.bs-badge {
  font-size: 10px;
  font-weight: var(--font-weight-bold);
  color: #f87171;
  background: rgba(248, 113, 113, 0.12);
  padding: 1px 6px;
  border-radius: var(--radius-sm);
  letter-spacing: 0.08em;
}

.bs-panel-body {
  flex: 1;
  padding: var(--space-3);
  min-height: 0;
}

.bs-panel-body .chart {
  width: 100%;
  height: 260px;
}

/* ------------------------------------------------------------------
   Scroll List (warnings)
   ------------------------------------------------------------------ */
.scroll-list {
  display: flex;
  flex-direction: column;
  max-height: 260px;
  overflow-y: auto;
}

.scroll-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  transition: background var(--transition-fast);
}

.scroll-item:last-child {
  border-bottom: none;
}

.scroll-item:hover {
  background: rgba(255, 255, 255, 0.02);
}

.warn-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  margin-top: 5px;
  flex-shrink: 0;
}

.warn-dot.level-1 { background: #f59e0b; }
.warn-dot.level-2 { background: #f97316; }
.warn-dot.level-3 { background: #f87171; }

.warn-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.warn-title {
  font-size: var(--font-size-sm);
  color: #cbd5e1;
  line-height: 1.4;
}

.warn-meta {
  font-size: var(--font-size-xs);
  color: #64748b;
  margin-top: 2px;
}

.warn-time {
  font-size: var(--font-size-xs);
  color: #475569;
  flex-shrink: 0;
  margin-top: 2px;
}

/* ------------------------------------------------------------------
   Responsive
   ------------------------------------------------------------------ */
@media (max-width: 1400px) {
  .bs-stat-row {
    grid-template-columns: repeat(3, 1fr);
  }

  .bs-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .bs-panel--wide {
    grid-column: span 2;
  }
}

@media (max-width: 900px) {
  .bs-stat-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .bs-grid {
    grid-template-columns: 1fr;
  }

  .bs-panel--wide {
    grid-column: span 1;
  }

  .bs-system-name {
    display: none;
  }
}
</style>
