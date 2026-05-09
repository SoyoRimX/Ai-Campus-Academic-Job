<template>
  <div class="big-screen">
    <div class="header">
      <h1>🎓 AI 校园学业就业智能服务平台</h1>
      <p>{{ currentTime }}</p>
    </div>

    <div class="grid">
      <!-- 学生总览 -->
      <div class="panel">
        <h3>学生总览</h3>
        <el-row :gutter="12">
          <el-col :span="8" v-for="c in counts" :key="c.label">
            <div class="stat-box" :style="{ borderColor: c.color }">
              <span class="num" :style="{ color: c.color }">{{ c.value }}</span>
              <span class="label">{{ c.label }}</span>
            </div>
          </el-col>
        </el-row>
        <div ref="chart1" style="height:220px; margin-top:12px"></div>
      </div>

      <!-- 学业预警态势 -->
      <div class="panel">
        <h3>学业预警态势</h3>
        <div ref="chart2" style="height:280px"></div>
      </div>

      <!-- 就业匹配 -->
      <div class="panel">
        <h3>就业匹配统计</h3>
        <div ref="chart3" style="height:280px"></div>
      </div>

      <!-- 实时预警滚动 -->
      <div class="panel">
        <h3>实时预警</h3>
        <div class="scroll-list">
          <div v-for="(w, i) in warnings" :key="i" class="scroll-item">
            <el-tag :type="w.level === 3 ? 'danger' : w.level === 2 ? 'warning' : ''" size="small">
              {{ ['','黄色','橙色','红色'][w.level] }}
            </el-tag>
            <span>{{ w.title }}</span>
            <span class="time">{{ w.time }}</span>
          </div>
        </div>
      </div>

      <!-- 岗位热度 -->
      <div class="panel">
        <h3>热门岗位</h3>
        <div ref="chart4" style="height:280px"></div>
      </div>

      <!-- GPA 分布 -->
      <div class="panel">
        <h3>GPA 分布</h3>
        <div ref="chart5" style="height:280px"></div>
      </div>

      <!-- 面试统计 -->
      <div class="panel">
        <h3>面试通过率</h3>
        <div ref="chart6" style="height:280px"></div>
      </div>

      <!-- 实时就业进展 -->
      <div class="panel wide">
        <h3>各专业就业进展</h3>
        <div ref="chart7" style="height:280px"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

const currentTime = ref('')
let timer: any = null

const counts = [
  { label: '在校学生', value: 12480, color: '#409EFF' },
  { label: '预警学生', value: 286, color: '#E6A23C' },
  { label: '应届毕业生', value: 3150, color: '#67C23A' },
  { label: '已就业', value: 2186, color: '#9B59B6' },
  { label: '就业率', value: '69.4%', color: '#F56C6C' },
  { label: '平均 GPA', value: 3.12, color: '#00D2FF' },
]

const warnings = [
  { title: '王芳 - 数据结构与算法挂科预警', level: 2, time: '10:23' },
  { title: '张伟 - 学分进度滞后预警', level: 1, time: '10:18' },
  { title: '刘洋 - 绩点低于2.0预警', level: 3, time: '09:55' },
  { title: '陈静 - 出勤率不足预警', level: 2, time: '09:42' },
  { title: '赵磊 - 补考未通过预警', level: 2, time: '08:30' },
]

const chart1 = ref()
const chart2 = ref()
const chart3 = ref()
const chart4 = ref()
const chart5 = ref()
const chart6 = ref()
const chart7 = ref()
const instances: any[] = []

function initCharts() {
  const c1 = echarts.init(chart1.value)
  c1.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 10, right: 10, bottom: 10, left: 40 },
    xAxis: { data: ['计科', '软工', '大数据', 'AI', '网安', '物联网'], axisLabel: { fontSize: 10 } },
    yAxis: { axisLabel: { fontSize: 10 } },
    series: [{ type: 'bar', data: [520, 380, 290, 350, 240, 200], itemStyle: { borderRadius: [4,4,0,0], color: '#409EFF' } }]
  })
  instances.push(c1)

  const c2 = echarts.init(chart2.value)
  c2.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 10, right: 10, bottom: 10, left: 40 },
    xAxis: { data: ['9月','10月','11月','12月','1月','2月','3月','4月'], axisLabel: { fontSize: 10 } },
    yAxis: { axisLabel: { fontSize: 10 } },
    series: [
      { name: '挂科', type: 'line', smooth: true, data: [8,12,9,15,18,14,10,6] },
      { name: '学分', type: 'line', smooth: true, data: [3,5,4,7,9,6,5,3] },
      { name: '绩点', type: 'line', smooth: true, data: [2,3,5,4,6,8,5,4] },
    ]
  })
  instances.push(c2)

  const c3 = echarts.init(chart3.value)
  c3.setOption({
    tooltip: { trigger: 'item' },
    series: [{ type: 'pie', radius: ['45%','70%'], center: ['50%','55%'],
      label: { fontSize: 10 },
      data: [
        { value: 820, name: '已匹配' }, { value: 540, name: '已投递' },
        { value: 320, name: '面试中' }, { value: 186, name: '已录用' }, { value: 120, name: '待匹配' }
      ]
    }]
  })
  instances.push(c3)

  const c4 = echarts.init(chart4.value)
  c4.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 10, right: 20, bottom: 10, left: 100 },
    yAxis: { type: 'category', data: ['Java开发','前端开发','AI算法','数据分析','产品经理','测试工程'], axisLabel: { fontSize: 10 } },
    xAxis: { axisLabel: { fontSize: 10 } },
    series: [{ type: 'bar', data: [156, 132, 98, 85, 72, 65], itemStyle: { borderRadius: [0,4,4,0], color: '#67C23A' }, label: { show: true, position: 'right', fontSize: 10 } }]
  })
  instances.push(c4)

  const c5 = echarts.init(chart5.value)
  c5.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 10, right: 10, bottom: 10, left: 40 },
    xAxis: { data: ['<2.0','2.0-2.5','2.5-3.0','3.0-3.5','3.5-3.8','>3.8'], axisLabel: { fontSize: 10 } },
    yAxis: { axisLabel: { fontSize: 10 } },
    series: [{ type: 'bar', data: [120, 380, 890, 1520, 980, 520],
      itemStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1,[
        { offset: 0, color: '#409EFF' }, { offset: 1, color: '#00D2FF' }
      ]), borderRadius: [4,4,0,0] }
    }]
  })
  instances.push(c5)

  const c6 = echarts.init(chart6.value)
  c6.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 10, right: 10, bottom: 10, left: 40 },
    xAxis: { data: ['1月','2月','3月','4月','5月'], axisLabel: { fontSize: 10 } },
    yAxis: { max: 100, axisLabel: { fontSize: 10, formatter: '{value}%' } },
    series: [
      { name: '通过率', type: 'line', smooth: true, data: [62,68,72,75,78], areaStyle: { opacity: 0.2 },
        itemStyle: { color: '#67C23A' } }
    ]
  })
  instances.push(c6)

  const c7 = echarts.init(chart7.value)
  c7.setOption({
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0, textStyle: { fontSize: 10 } },
    grid: { top: 10, right: 10, bottom: 30, left: 40 },
    xAxis: { data: ['计科','软工','大数据','AI','网安'], axisLabel: { fontSize: 10 } },
    yAxis: { axisLabel: { fontSize: 10, formatter: '{value}%' } },
    series: [
      { name: '已签约', type: 'bar', data: [72,68,65,78,70], itemStyle: { borderRadius: [4,4,0,0] } },
      { name: '面试中', type: 'bar', data: [15,18,20,12,16], itemStyle: { borderRadius: [4,4,0,0] } },
      { name: '未就业', type: 'bar', data: [13,14,15,10,14], itemStyle: { borderRadius: [4,4,0,0] } },
    ]
  })
  instances.push(c7)
}

onMounted(() => {
  currentTime.value = new Date().toLocaleString('zh-CN')
  timer = setInterval(() => { currentTime.value = new Date().toLocaleString('zh-CN') }, 1000)
  nextTick(initCharts)
})

onUnmounted(() => {
  clearInterval(timer)
  instances.forEach(c => c.dispose())
})
</script>

<style scoped>
.big-screen {
  min-height: 100vh;
  background: #0a1628;
  padding: 12px 20px 20px;
  color: #fff;
}
.header {
  text-align: center;
  padding: 8px 0 12px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  margin-bottom: 12px;
}
.header h1 {
  margin: 0;
  font-size: 26px;
  background: linear-gradient(90deg, #409EFF, #00D2FF);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.header p { margin: 4px 0 0; color: #8899aa; font-size: 14px; }

.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.panel {
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 8px;
  padding: 14px;
}
.panel.wide { grid-column: span 2; }
.panel h3 {
  margin: 0 0 10px;
  font-size: 15px;
  color: #8899cc;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}

.stat-box {
  text-align: center;
  padding: 12px 4px;
  border: 1px solid;
  border-radius: 6px;
  background: rgba(255,255,255,0.03);
}
.stat-box .num { font-size: 22px; font-weight: bold; display: block; }
.stat-box .label { font-size: 11px; color: #8899aa; margin-top: 2px; display: block; }

.scroll-list { max-height: 250px; overflow-y: auto; }
.scroll-item {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 0; border-bottom: 1px solid rgba(255,255,255,0.05);
  font-size: 13px;
}
.scroll-item .time { margin-left: auto; color: #8899aa; font-size: 12px; }

@media (max-width: 1200px) {
  .grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
