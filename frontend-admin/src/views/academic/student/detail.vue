<template>
  <div class="student-detail">
    <el-page-header @back="$router.back()" class="detail-header">
      <template #content>
        <span class="header-title">学生详情</span>
      </template>
      <template #extra>
        <span class="header-subtitle">{{ student.studentNo }} · {{ student.realName }}</span>
      </template>
    </el-page-header>

    <el-row :gutter="20">
      <!-- Left: Basic info -->
      <el-col :span="8">
        <el-card shadow="never" class="info-card">
          <template #header>
            <div class="card-title">基本信息</div>
          </template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="学号">{{ student.studentNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ student.realName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="专业">{{ student.major || '—' }}</el-descriptions-item>
            <el-descriptions-item label="年级">{{ student.grade || '—' }}</el-descriptions-item>
            <el-descriptions-item label="班级">{{ student.className || '—' }}</el-descriptions-item>
            <el-descriptions-item label="辅导员">{{ student.advisor || '—' }}</el-descriptions-item>
            <el-descriptions-item label="入学年份">{{ student.enrollmentYear || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- Right: Academic overview + grades + warnings -->
      <el-col :span="16">
        <!-- Stats -->
        <div class="stats-row">
          <StatCard
            icon="DataLine"
            label="GPA"
            :value="student.gpa ?? '—'"
            icon-bg="var(--color-primary-50)"
            icon-color="var(--color-primary-500)"
          />
          <StatCard
            icon="DocumentChecked"
            :label="'已修 / 应修学分'"
            :value="student.totalCredits ?? '—'"
            :suffix="student.requiredCredits ? ' / ' + student.requiredCredits : ''"
            icon-bg="var(--color-success-50)"
            icon-color="var(--color-success-500)"
          />
          <StatCard
            icon="WarningFilled"
            label="不及格科目"
            :value="student.failCount ?? 0"
            :value-color="student.failCount > 0 ? 'var(--color-danger-500)' : 'var(--color-success-500)'"
            icon-bg="var(--color-danger-50)"
            icon-color="var(--color-danger-500)"
          />
        </div>

        <!-- Grades -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-title">成绩记录</div>
          </template>
          <el-table v-if="grades.length" :data="grades" size="small" stripe>
            <el-table-column prop="courseName" label="课程" min-width="140" show-overflow-tooltip />
            <el-table-column prop="semester" label="学期" width="140" />
            <el-table-column prop="score" label="分数" width="80" />
            <el-table-column prop="gradePoint" label="绩点" width="80" />
            <el-table-column prop="passed" label="通过" width="80">
              <template #default="{ row }">
                <el-tag :type="row.passed ? 'success' : 'danger'" size="small" effect="plain">
                  {{ row.passed ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="examType" label="考试类型" width="100">
              <template #default="{ row }">
                {{ examTypeMap[row.examType] || '—' }}
              </template>
            </el-table-column>
          </el-table>
          <EmptyState v-else icon="Document" title="暂无成绩记录" />
        </el-card>

        <!-- Warnings -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-title">预警记录</div>
          </template>
          <el-table v-if="warnings.length" :data="warnings" size="small" stripe>
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="warningType" label="类型" width="100">
              <template #default="{ row }">
                {{ warningTypeMap[row.warningType] || '—' }}
              </template>
            </el-table-column>
            <el-table-column prop="warningLevel" label="等级" width="80">
              <template #default="{ row }">
                <el-tag :type="row.warningLevel === 3 ? 'danger' : row.warningLevel === 2 ? 'warning' : ''" size="small" effect="dark">
                  {{ ['', '黄色', '橙色', '红色'][row.warningLevel] || '—' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="isRead" label="已读" width="70">
              <template #default="{ row }">{{ row.isRead ? '是' : '否' }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="180" />
          </el-table>
          <EmptyState v-else icon="Warning" title="暂无预警记录" description="该学生当前没有任何学业预警" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getStudent, getWarnings } from '@/api/academic'
import StatCard from '@/components/stat-card.vue'
import EmptyState from '@/components/empty-state.vue'

const route = useRoute()
const studentId = Number(route.params.id)

const student = ref<any>({})
const grades = ref<any[]>([])
const warnings = ref<any[]>([])

const examTypeMap: Record<number, string> = { 1: '正考', 2: '补考', 3: '重修' }
const warningTypeMap: Record<number, string> = { 1: '挂科预警', 2: '绩点预警', 3: '学分预警', 4: '出勤预警' }

onMounted(async () => {
  const res = await getStudent(studentId)
  student.value = res.data || {}
  grades.value = student.value.grades || []

  try {
    const wRes = await getWarnings({ page: 1, size: 100, studentId })
    warnings.value = wRes.data?.records || []
  } catch { warnings.value = [] }
})
</script>

<style scoped>
.student-detail {
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.detail-header {
  margin-bottom: var(--space-5);
}

.detail-header :deep(.el-page-header__content) {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
}

.header-subtitle {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.info-card,
.section-card {
  border: 1px solid var(--color-border);
  margin-bottom: var(--space-4);
}

.info-card :deep(.el-card__body) {
  padding: var(--space-3) var(--space-5) var(--space-5);
}

.section-card :deep(.el-card__body) {
  padding: 0;
}

.card-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

@media (max-width: 960px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
}
</style>
