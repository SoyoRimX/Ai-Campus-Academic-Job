<template>
  <div>
    <el-page-header @back="$router.back()" content="学生详情" style="margin-bottom:20px" />

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>基本信息</template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="学号">{{ student.studentNo }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ student.realName }}</el-descriptions-item>
            <el-descriptions-item label="专业">{{ student.major }}</el-descriptions-item>
            <el-descriptions-item label="年级">{{ student.grade }}</el-descriptions-item>
            <el-descriptions-item label="班级">{{ student.className }}</el-descriptions-item>
            <el-descriptions-item label="辅导员">{{ student.advisor }}</el-descriptions-item>
            <el-descriptions-item label="入学年份">{{ student.enrollmentYear }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <template #header>学业概况</template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-value" style="color:#409EFF">{{ student.gpa }}</div>
                <div class="stat-label">GPA</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-value" style="color:#67C23A">{{ student.totalCredits }}/{{ student.requiredCredits }}</div>
                <div class="stat-label">已修/应修学分</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <div class="stat-value" :style="{ color: student.failCount > 0 ? '#F56C6C' : '#67C23A' }">{{ student.failCount }}</div>
                <div class="stat-label">不及格科目</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <el-card style="margin-top:16px">
          <template #header>成绩记录</template>
          <el-table :data="grades" v-loading="gradesLoading" size="small" empty-text="暂无成绩">
            <el-table-column prop="courseName" label="课程" />
            <el-table-column prop="semester" label="学期" width="140" />
            <el-table-column prop="score" label="分数" width="80" />
            <el-table-column prop="gradePoint" label="绩点" width="80" />
            <el-table-column prop="passed" label="通过" width="80">
              <template #default="{ row }">
                <el-tag :type="row.passed ? 'success' : 'danger'" size="small">{{ row.passed ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="examType" label="考试类型" width="100">
              <template #default="{ row }">
                {{ examTypeMap[row.examType] || '-' }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card style="margin-top:16px">
          <template #header>预警记录</template>
          <el-table :data="warnings" v-loading="warningsLoading" size="small" empty-text="暂无预警">
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="warningType" label="类型" width="100">
              <template #default="{ row }">
                {{ warningTypeMap[row.warningType] }}
              </template>
            </el-table-column>
            <el-table-column prop="warningLevel" label="等级" width="80">
              <template #default="{ row }">
                <el-tag :type="row.warningLevel === 3 ? 'danger' : row.warningLevel === 2 ? 'warning' : ''" size="small">
                  {{ ['','黄色','橙色','红色'][row.warningLevel] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="isRead" label="已读" width="70">
              <template #default="{ row }">{{ row.isRead ? '是' : '否' }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getStudent, getWarnings } from '@/api/academic'

const route = useRoute()
const studentId = Number(route.params.id)

const student = ref<any>({})
const grades = ref<any[]>([])
const warnings = ref<any[]>([])
const gradesLoading = ref(false)
const warningsLoading = ref(false)

const examTypeMap: Record<number, string> = { 1: '正考', 2: '补考', 3: '重修' }
const warningTypeMap: Record<number, string> = { 1: '挂科预警', 2: '绩点预警', 3: '学分预警', 4: '出勤预警' }

onMounted(async () => {
  const res = await getStudent(studentId)
  student.value = res.data

  warningsLoading.value = true
  try {
    const wRes = await getWarnings({ page: 1, size: 100, studentId })
    warnings.value = wRes.data?.records || []
  } finally {
    warningsLoading.value = false
  }
})
</script>

<style scoped>
.stat-card {
  text-align: center;
  padding: 20px 0;
}
.stat-value {
  font-size: 32px;
  font-weight: bold;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 8px;
}
</style>
