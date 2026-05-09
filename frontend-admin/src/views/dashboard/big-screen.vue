<template>
  <div class="big-screen">
    <div class="header">
      <h1>AI 校园学业就业智能服务平台</h1>
      <span class="time">{{ currentTime }}</span>
    </div>

    <div class="grid">
      <!-- 第1行: 6个统计卡片 -->
      <div class="stat-row">
        <div class="stat-card" v-for="c in counts" :key="c.label" :style="{ borderColor: c.color }">
          <span class="stat-num" :style="{ color: c.color }">{{ c.value }}</span>
          <span class="stat-label">{{ c.label }}</span>
        </div>
      </div>

      <!-- 第2行: 学生分布 + 预警态势 + 就业匹配 -->
      <div class="panel"><h3>学生专业分布</h3><div ref="c1" class="chart"></div></div>
      <div class="panel"><h3>学业预警态势</h3><div ref="c2" class="chart"></div></div>
      <div class="panel"><h3>就业匹配统计</h3><div ref="c3" class="chart"></div></div>

      <!-- 第3行: 实时预警 + 热门岗位 + GPA分布 -->
      <div class="panel">
        <h3>实时预警</h3>
        <div class="scroll-list">
          <div v-for="(w, i) in warnings" :key="i" class="scroll-item">
            <el-tag :type="w.level===3?'danger':w.level===2?'warning':'info'" size="small">{{ levelMap[w.level] }}</el-tag>
            <span class="warn-title">{{ w.title }}</span>
            <span class="warn-time">{{ w.time }}</span>
          </div>
        </div>
      </div>
      <div class="panel"><h3>热门岗位 TOP</h3><div ref="c4" class="chart"></div></div>
      <div class="panel"><h3>GPA 分布</h3><div ref="c5" class="chart"></div></div>

      <!-- 第4行: 面试通过率 + 各专业就业进展 -->
      <div class="panel"><h3>面试通过率趋势</h3><div ref="c6" class="chart"></div></div>
      <div class="panel wide"><h3>各专业就业进展</h3><div ref="c7" class="chart"></div></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

const currentTime = ref('')
let timer: any = null
const levelMap: Record<number, string> = { 1: '黄色', 2: '橙色', 3: '红色' }

const counts = [
  { label: '在校学生', value: '12,480', color: '#409EFF' },
  { label: '预警学生', value: '286', color: '#E6A23C' },
  { label: '应届毕业生', value: '3,150', color: '#67C23A' },
  { label: '已就业', value: '2,186', color: '#9B59B6' },
  { label: '就业率', value: '69.4%', color: '#F56C6C' },
  { label: '平均 GPA', value: '3.12', color: '#00D2FF' },
]

const warnings = [
  { title: '王芳 - 数据结构与算法挂科预警', level: 2, time: '10:23' },
  { title: '张伟 - 学分进度滞后预警', level: 1, time: '10:18' },
  { title: '刘洋 - 绩点低于2.0预警', level: 3, time: '09:55' },
  { title: '陈静 - 出勤率不足预警', level: 2, time: '09:42' },
  { title: '赵磊 - 补考未通过预警', level: 2, time: '08:30' },
]

const c1 = ref(), c2 = ref(), c3 = ref(), c4 = ref(), c5 = ref(), c6 = ref(), c7 = ref()
const charts: any[] = []

// 暗色主题通用配置
function darkGrid() {
  return { top: 16, right: 16, bottom: 16, left: 16, containLabel: true }
}
function darkXAxis(data: string[]) {
  return { type: 'category', data, axisLabel: { color: '#8899aa', fontSize: 11 }, axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } } }
}
function darkYAxis() {
  return { type: 'value', axisLabel: { color: '#8899aa', fontSize: 11 }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } } }
}

function initCharts() {
  // 学生专业分布
  const ch1 = echarts.init(c1.value)
  ch1.setOption({
    tooltip: { trigger: 'axis' },
    grid: darkGrid(),
    xAxis: darkXAxis(['计科', '软工', '大数据', 'AI', '网安', '物联网']),
    yAxis: darkYAxis(),
    series: [{ type: 'bar', data: [520, 380, 290, 350, 240, 200],
      itemStyle: { color: '#409EFF', borderRadius: [4,4,0,0] }, barWidth: '50%' }]
  })
  charts.push(ch1)

  // 学业预警态势
  const ch2 = echarts.init(c2.value)
  ch2.setOption({
    tooltip: { trigger: 'axis' },
    grid: darkGrid(),
    xAxis: darkXAxis(['9月','10月','11月','12月','1月','2月','3月','4月']),
    yAxis: darkYAxis(),
    legend: { bottom: 0, textStyle: { color: '#8899aa', fontSize: 11 } },
    series: [
      { name: '挂科', type: 'line', smooth: true, data: [8,12,9,15,18,14,10,6], itemStyle: { color: '#E6A23C' } },
      { name: '学分', type: 'line', smooth: true, data: [3,5,4,7,9,6,5,3], itemStyle: { color: '#409EFF' } },
    ]
  })
  charts.push(ch2)

  // 就业匹配
  const ch3 = echarts.init(c3.value)
  ch3.setOption({
    tooltip: { trigger: 'item' },
    series: [{ type: 'pie', radius: ['50%','75%'], center: ['50%','55%'],
      label: { show: true, formatter: '{b}\n{d}%', color: '#8899aa', fontSize: 11 },
      data: [
        { value: 820, name: '已匹配' }, { value: 540, name: '已投递' },
        { value: 320, name: '面试中' }, { value: 186, name: '已录用' }
      ]
    }]
  })
  charts.push(ch3)

  // 热门岗位
  const ch4 = echarts.init(c4.value)
  ch4.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 10, right: 30, bottom: 10, left: 20, containLabel: true },
    yAxis: { type: 'category', data: ['Java开发','前端开发','AI算法','数据分析','产品经理','测试工程'],
      axisLabel: { color: '#8899aa', fontSize: 11 }, axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } } },
    xAxis: { type: 'value', axisLabel: { color: '#8899aa', fontSize: 11 }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } } },
    series: [{ type: 'bar', data: [156, 132, 98, 85, 72, 65],
      itemStyle: { color: '#67C23A', borderRadius: [0,4,4,0] }, label: { show: true, position: 'right', color: '#8899aa', fontSize: 11 } }]
  })
  charts.push(ch4)

  // GPA 分布
  const ch5 = echarts.init(c5.value)
  ch5.setOption({
    tooltip: { trigger: 'axis' },
    grid: darkGrid(),
    xAxis: darkXAxis(['<2.0','2.0-2.5','2.5-3.0','3.0-3.5','3.5-3.8','>3.8']),
    yAxis: darkYAxis(),
    series: [{ type: 'bar', data: [120, 380, 890, 1520, 980, 520], barWidth: '55%',
      itemStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1, [{ offset:0, color:'#409EFF' },{ offset:1, color:'#00D2FF' }]), borderRadius: [4,4,0,0] } }]
  })
  charts.push(ch5)

  // 面试通过率
  const ch6 = echarts.init(c6.value)
  ch6.setOption({
    tooltip: { trigger: 'axis' },
    grid: darkGrid(),
    xAxis: darkXAxis(['1月','2月','3月','4月','5月']),
    yAxis: { ...darkYAxis(), max: 100, axisLabel: { color: '#8899aa', fontSize: 11, formatter: '{value}%' } },
    series: [{ name: '通过率', type: 'line', smooth: true, data: [62,68,72,75,78],
      areaStyle: { color: 'rgba(103,194,58,0.15)' }, itemStyle: { color: '#67C23A' } }]
  })
  charts.push(ch6)

  // 各专业就业
  const ch7 = echarts.init(c7.value)
  ch7.setOption({
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0, textStyle: { color: '#8899aa', fontSize: 11 } },
    grid: { top: 16, right: 16, bottom: 36, left: 16, containLabel: true },
    xAxis: darkXAxis(['计科','软工','大数据','AI','网安']),
    yAxis: { ...darkYAxis(), axisLabel: { color: '#8899aa', fontSize: 11, formatter: '{value}%' } },
    series: [
      { name: '已签约', type: 'bar', data: [72,68,65,78,70], barMaxWidth: 24, itemStyle: { color: '#67C23A', borderRadius: [3,3,0,0] } },
      { name: '面试中', type: 'bar', data: [15,18,20,12,16], barMaxWidth: 24, itemStyle: { color: '#409EFF', borderRadius: [3,3,0,0] } },
      { name: '未就业', type: 'bar', data: [13,14,15,10,14], barMaxWidth: 24, itemStyle: { color: '#E6A23C', borderRadius: [3,3,0,0] } },
    ]
  })
  charts.push(ch7)
}

onMounted(() => {
  currentTime.value = new Date().toLocaleString('zh-CN')
  timer = setInterval(() => { currentTime.value = new Date().toLocaleString('zh-CN') }, 1000)
  nextTick(initCharts)
})

onUnmounted(() => {
  clearInterval(timer)
  charts.forEach(c => c.dispose())
})
</script>

<style scoped>
.big-screen {
  min-height: 100vh;
  background: #0a1628;
  padding: 16px 24px 24px;
  color: #fff;
  box-sizing: border-box;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  margin-bottom: 16px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.header h1 {
  margin: 0;
  font-size: 24px;
  background: linear-gradient(90deg, #409EFF, #00D2FF);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.header .time { color: #8899aa; font-size: 14px; }

.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stat-row {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}
.stat-card {
  text-align: center;
  padding: 16px 8px;
  border: 1px solid;
  border-radius: 6px;
  background: rgba(255,255,255,0.03);
}
.stat-num { font-size: 26px; font-weight: bold; display: block; }
.stat-label { font-size: 12px; color: #8899aa; margin-top: 4px; display: block; }

.panel {
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: 8px;
  padding: 14px;
  display: flex;
  flex-direction: column;
}
.panel.wide { grid-column: span 2; }
.panel h3 {
  margin: 0 0 8px;
  font-size: 14px;
  color: #8899cc;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  flex-shrink: 0;
}
.chart { height: 260px; }

.scroll-list { height: 260px; overflow-y: auto; }
.scroll-item {
  display: flex; align-items: center; gap: 8px;
  padding: 9px 0; border-bottom: 1px solid rgba(255,255,255,0.04);
  font-size: 13px;
}
.warn-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.warn-time { color: #6a7a8a; font-size: 12px; flex-shrink: 0; }
</style>
