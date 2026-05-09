<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6" v-for="card in cards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
            <div class="stat-icon" :style="{ background: card.color }">
              <el-icon :size="28"><component :is="card.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>学业预警趋势</template>
          <div ref="chart1Ref" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>就业匹配统计</template>
          <div ref="chart2Ref" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const cards = [
  { label: '学生总数', value: '1,280', icon: 'User', color: '#409EFF' },
  { label: '预警学生', value: '86', icon: 'Warning', color: '#E6A23C' },
  { label: '招聘岗位', value: '342', icon: 'Briefcase', color: '#67C23A' },
  { label: '本月面试', value: '156', icon: 'ChatDotRound', color: '#F56C6C' }
]

const chart1Ref = ref()
const chart2Ref = ref()

onMounted(() => {
  const chart1 = echarts.init(chart1Ref.value!)
  chart1.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { data: ['1月','2月','3月','4月','5月','6月'] },
    yAxis: {},
    series: [{
      name: '预警数', type: 'line', smooth: true,
      data: [12, 15, 9, 18, 14, 8],
      areaStyle: { opacity: 0.3 }
    }]
  })

  const chart2 = echarts.init(chart2Ref.value!)
  chart2.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { value: 120, name: '已匹配' },
        { value: 80, name: '已投递' },
        { value: 45, name: '面试中' },
        { value: 22, name: '已录用' }
      ]
    }]
  })
})
</script>

<style scoped>
.stat-card { cursor: pointer; }
.stat-content { display: flex; justify-content: space-between; align-items: center; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
.stat-icon {
  width: 56px; height: 56px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; color: #fff;
}
</style>
