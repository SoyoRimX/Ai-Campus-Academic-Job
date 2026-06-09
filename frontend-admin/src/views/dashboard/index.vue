<template>
  <div class="dashboard">
    <!-- Welcome -->
    <div class="dash-welcome">
      <div>
        <h2>数据总览</h2>
        <p>欢迎回来，{{ userStore.userInfo?.realName || '用户' }}</p>
      </div>
    </div>

    <!-- Stat cards -->
    <div class="dash-stats">
      <StatCard
        v-for="card in cards"
        :key="card.label"
        :icon="card.icon"
        :label="card.label"
        :value="card.value"
        :icon-bg="card.iconBg"
        :icon-color="card.iconColor"
        :trend="card.trend"
      />
    </div>

    <!-- Charts row -->
    <div class="dash-charts">
      <el-card shadow="never" class="chart-card">
        <template #header>
          <div class="chart-header">
            <span>学业预警趋势</span>
            <span class="chart-subtitle">近6个月</span>
          </div>
        </template>
        <div ref="chart1Ref" class="chart"></div>
      </el-card>

      <el-card shadow="never" class="chart-card">
        <template #header>
          <div class="chart-header">
            <span>就业匹配统计</span>
            <span class="chart-subtitle">当前状态分布</span>
          </div>
        </template>
        <div ref="chart2Ref" class="chart"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { useUserStore } from '@/stores/user'
import StatCard from '@/components/stat-card.vue'

const userStore = useUserStore()

const cards = [
  { label: '学生总数', value: '1,280', icon: 'User', iconBg: 'var(--color-primary-50)', iconColor: 'var(--color-primary-500)', trend: 3.2 },
  { label: '预警学生', value: '86', icon: 'Warning', iconBg: 'var(--color-warning-50)', iconColor: 'var(--color-warning-500)', trend: -12.5 },
  { label: '招聘岗位', value: '342', icon: 'Briefcase', iconBg: 'var(--color-success-50)', iconColor: 'var(--color-success-500)', trend: 8.1 },
  { label: '本月面试', value: '156', icon: 'ChatDotRound', iconBg: '#fdf2f8', iconColor: '#ec4899', trend: 15.3 },
]

const chart1Ref = ref<HTMLElement>()
const chart2Ref = ref<HTMLElement>()
const charts: echarts.ECharts[] = []

onMounted(() => {
  if (chart1Ref.value) {
    const c1 = echarts.init(chart1Ref.value)
    c1.setOption({
      tooltip: { trigger: 'axis' },
      grid: { top: 10, right: 20, bottom: 10, left: 40 },
      xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'], axisLine: { show: false }, axisTick: { show: false } },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f3f6' } } },
      series: [{
        type: 'line', smooth: true, symbol: 'circle', symbolSize: 6,
        data: [12, 15, 9, 18, 14, 8],
        areaStyle: { opacity: 0.08, color: '#4f6ef7' },
        lineStyle: { color: '#4f6ef7', width: 2 },
        itemStyle: { color: '#4f6ef7' },
      }]
    })
    charts.push(c1)
  }

  if (chart2Ref.value) {
    const c2 = echarts.init(chart2Ref.value)
    c2.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['50%', '75%'],
        center: ['50%', '50%'],
        label: { show: true, formatter: '{b}\n{d}%', fontSize: 12 },
        data: [
          { value: 120, name: '已匹配' },
          { value: 80, name: '已投递' },
          { value: 45, name: '面试中' },
          { value: 22, name: '已录用' },
        ],
        color: ['#4f6ef7', '#38bdf8', '#f59e0b', '#22c55e'],
      }]
    })
    charts.push(c2)
  }
})

onUnmounted(() => {
  charts.forEach(c => c.dispose())
})
</script>

<style scoped>
.dashboard {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.dash-welcome {
  margin-bottom: var(--space-6);
}

.dash-welcome h2 {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin-bottom: var(--space-1);
}

.dash-welcome p {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
}

.dash-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.dash-charts {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: var(--space-5);
}

.chart-card {
  border: 1px solid var(--color-border);
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
}

.chart-subtitle {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-normal);
  color: var(--color-text-placeholder);
}

.chart {
  height: 300px;
}
</style>
