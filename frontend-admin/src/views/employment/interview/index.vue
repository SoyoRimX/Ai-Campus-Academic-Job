<template>
  <PageShell title="面试记录" description="查看学生的模拟面试历史和详细问答">
    <SkeletonTable v-if="initialLoading" />
    <template v-else>
      <el-table
        v-if="tableData.length"
        ref="tableRef"
        :data="tableData"
        stripe
        v-loading="loading"
        @expand-change="handleExpand"
        @row-click="toggleRow"
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-panel" v-loading="row._loading">
              <EmptyState v-if="!row._details || row._details.length === 0" icon="ChatDotRound" title="暂无面试详情" />
              <div v-else class="qa-list">
                <div v-for="(item, idx) in row._details" :key="idx" class="qa-card">
                  <div class="qa-header">
                    <span class="qa-number">第 {{ idx + 1 }} 题</span>
                    <span class="qa-score" v-if="item.score !== undefined">得分 {{ item.score }}</span>
                  </div>
                  <div class="qa-question">{{ item.question }}</div>
                  <div class="qa-answer">{{ item.answer }}</div>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studentName" label="学生姓名" width="90" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="jobTitle" label="岗位名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="company" label="公司" min-width="120" show-overflow-tooltip />
        <el-table-column label="面试类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.interviewType === 1 ? 'primary' : 'warning'" size="small" effect="plain">
              {{ row.interviewType === 0 ? '文字' : row.interviewType === 1 ? '语音' : '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="评分" width="80">
          <template #default="{ row }">
            <span :style="{ fontWeight: 'var(--font-weight-semibold)', color: row.score >= 80 ? 'var(--color-success-500)' : row.score >= 60 ? 'var(--color-warning-500)' : 'var(--color-danger-500)' }">
              {{ row.score ?? '—' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" width="80">
          <template #default="{ row }">{{ row.duration ? row.duration + 's' : '—' }}</template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="170" />
      </el-table>

      <EmptyState v-else icon="List" title="暂无面试记录" description="还没有学生完成模拟面试" />

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="page" v-model:page-size="size"
          :page-sizes="[10, 20, 50]" :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchData" @size-change="fetchData"
        />
      </div>
    </template>
  </PageShell>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getInterviews } from '@/api/employment'
import PageShell from '@/components/page-shell.vue'
import EmptyState from '@/components/empty-state.vue'
import SkeletonTable from '@/components/skeleton-table.vue'

interface InterviewDetail { question: string; answer: string; score?: number }
interface Interview {
  id: number; studentId: number; studentName: string; studentNo: string
  jobTitle: string; company: string; interviewType: number; score: number
  duration: number; startTime: string; endTime: string
  _loading?: boolean; _details?: InterviewDetail[]
}

const tableRef = ref()
const loading = ref(false)
const initialLoading = ref(true)
const tableData = ref<Interview[]>([])
const expandingRow = ref<Interview | null>(null)
const page = ref(1); const size = ref(10); const total = ref(0)

function toggleRow(row: Interview) {
  if (expandingRow.value === row) {
    tableRef.value?.toggleRowExpansion(row, false); expandingRow.value = null
  } else {
    if (expandingRow.value) tableRef.value?.toggleRowExpansion(expandingRow.value, false)
    tableRef.value?.toggleRowExpansion(row, true); expandingRow.value = row
  }
}

async function handleExpand(row: Interview, rows: Interview[]) {
  if (!rows.includes(row) || row._details) return
  row._loading = true
  try {
    const qStr = (row as any).questions || ''; const aStr = (row as any).answers || ''
    const questions = qStr.split('||').filter((q: string) => q.trim())
    const answers = aStr.split('||').filter((a: string) => a.trim())
    row._details = questions.map((q: string, i: number) => ({ question: q, answer: answers[i] || '(未回答)' }))
  } catch { row._details = [] }
  finally { row._loading = false }
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getInterviews({ page: page.value, size: size.value })
    tableData.value = (res.data?.records || []).map((item: Interview) => ({ ...item, _loading: false, _details: undefined }))
    total.value = res.data?.total || 0
  } finally { loading.value = false; initialLoading.value = false }
}

onMounted(fetchData)
</script>

<style scoped>
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--space-4); }
.expand-panel { padding: var(--space-3) var(--space-5); min-height: 120px; }
.qa-list { display: flex; flex-direction: column; gap: var(--space-3); }
.qa-card { padding: var(--space-4); background: var(--color-body-bg); border-radius: var(--radius-md); border: 1px solid var(--color-border-light); }
.qa-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-2); }
.qa-number { font-weight: var(--font-weight-semibold); font-size: var(--font-size-sm); color: var(--color-primary-600); }
.qa-score { font-size: var(--font-size-xs); color: var(--color-text-placeholder); }
.qa-question { font-size: var(--font-size-base); color: var(--color-text-primary); margin-bottom: var(--space-2); line-height: 1.5; }
.qa-answer { font-size: var(--font-size-sm); color: var(--color-text-secondary); line-height: 1.6; }
</style>
