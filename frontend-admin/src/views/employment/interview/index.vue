<template>
  <div>
    <el-card>
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%" @expand-change="handleExpand">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div style="padding: 8px 24px" v-loading="row._loading">
              <el-empty v-if="!row._details || row._details.length === 0" description="暂无面试详情" />
              <div v-else>
                <el-card
                  v-for="(item, idx) in row._details"
                  :key="idx"
                  shadow="never"
                  style="margin-bottom: 12px"
                >
                  <template #header>
                    <span style="font-weight: bold">第 {{ idx + 1 }} 题</span>
                  </template>
                  <el-descriptions :column="1" border>
                    <el-descriptions-item label="问题">{{ item.question }}</el-descriptions-item>
                    <el-descriptions-item label="回答">{{ item.answer }}</el-descriptions-item>
                    <el-descriptions-item v-if="item.score !== undefined" label="得分">{{ item.score }}</el-descriptions-item>
                  </el-descriptions>
                </el-card>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studentId" label="学生ID" width="90" />
        <el-table-column prop="jobId" label="岗位ID" width="90" />
        <el-table-column label="面试类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.interviewType === 1 ? 'primary' : 'warning'" size="small">
              {{ row.interviewType === 1 ? '文字' : row.interviewType === 2 ? '语音' : '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="评分" width="80" />
        <el-table-column prop="duration" label="时长(秒)" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getInterviews } from '@/api/employment'

interface InterviewDetail {
  question: string
  answer: string
  score?: number
}

interface Interview {
  id: number
  studentId: number
  jobId: number
  interviewType: number
  score: number
  duration: number
  startTime: string
  endTime: string
  questions?: InterviewDetail[]
  _loading?: boolean
  _details?: InterviewDetail[]
}

const loading = ref(false)
const tableData = ref<Interview[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

async function handleExpand(row: Interview, rows: Interview[]) {
  if (!rows.includes(row)) return
  if (row._details) return

  row._loading = true
  try {
    // 模拟从API获取详情 —— 实际字段名取决于后端返回结构
    const raw = (row as any).questions || (row as any).details || (row as any).qaList
    if (Array.isArray(raw)) {
      row._details = raw.map((item: any) => ({
        question: item.question || item.q || '',
        answer: item.answer || item.a || '',
        score: item.score
      }))
    } else {
      row._details = []
    }
  } catch {
    row._details = []
  } finally {
    row._loading = false
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getInterviews({ page: page.value, size: size.value })
    tableData.value = (res.data?.records || []).map((item: Interview) => ({
      ...item,
      _loading: false,
      _details: undefined
    }))
    total.value = res.data?.total || 0
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>
