<template>
  <PageShell title="简历管理" description="查看和管理学生简历，支持 AI 智能生成">
    <template #toolbar>
      <el-button type="primary" @click="openGenerateDialog">
        <el-icon><MagicStick /></el-icon>AI 生成简历
      </el-button>
    </template>

    <div class="toolbar-filter">
      <el-input v-model="searchStudentNo" placeholder="输入学号搜索" clearable style="width: 220px" @keyup.enter="handleSearch" @clear="handleSearch">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button @click="handleSearch">搜索</el-button>
    </div>

    <SkeletonTable v-if="initialLoading" />
    <template v-else>
      <el-table v-if="filteredData.length" :data="pagedData" stripe v-loading="loading">
        <el-table-column prop="title" label="简历标题" min-width="140" show-overflow-tooltip />
        <el-table-column prop="studentName" label="学生姓名" width="90" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="targetJob" label="目标岗位" min-width="120" />
        <el-table-column prop="targetCity" label="目标城市" width="100" />
        <el-table-column prop="aiScore" label="AI 评分" width="90">
          <template #default="{ row }">
            <span :style="{ color: row.aiScore >= 80 ? 'var(--color-success-500)' : row.aiScore >= 60 ? 'var(--color-warning-500)' : 'var(--color-danger-500)', fontWeight: 'var(--font-weight-semibold)' }">
              {{ row.aiScore ?? '—' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="isDefault" label="默认" width="70">
          <template #default="{ row }">
            <el-tag :type="row.isDefault ? 'success' : 'info'" size="small" effect="plain">
              {{ row.isDefault ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openViewDialog(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState v-else icon="Document" title="暂无简历" description="点击「AI 生成简历」为学生创建智能简历" />

      <div class="pagination-wrapper" v-if="filteredData.length > 0">
        <el-pagination
          v-model:current-page="page" v-model:page-size="size"
          :page-sizes="[10, 20, 50]" :total="filteredData.length"
          layout="total, sizes, prev, pager, next"
        />
      </div>
    </template>

    <!-- View dialog -->
    <el-dialog v-model="viewDialogVisible" title="简历详情" width="700px" top="5vh">
      <el-descriptions v-if="viewResume" :column="2" border size="small">
        <el-descriptions-item label="简历标题">{{ viewResume.title }}</el-descriptions-item>
        <el-descriptions-item label="学生 ID">{{ viewResume.studentId }}</el-descriptions-item>
        <el-descriptions-item label="目标岗位">{{ viewResume.targetJob }}</el-descriptions-item>
        <el-descriptions-item label="目标城市">{{ viewResume.targetCity }}</el-descriptions-item>
        <el-descriptions-item label="AI 评分">{{ viewResume.aiScore }}</el-descriptions-item>
        <el-descriptions-item label="默认简历">
          <el-tag :type="viewResume.isDefault ? 'success' : 'info'" size="small">{{ viewResume.isDefault ? '是' : '否' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ viewResume.createTime }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="viewResume" class="view-content-section">
        <h4>简历内容</h4>
        <div v-if="parsedContent" class="parsed-content">
          <div v-for="(value, key) in parsedContent" :key="key" class="content-field">
            <span class="content-field-label">{{ key }}：</span>
            <span class="content-field-value">{{ value }}</span>
          </div>
        </div>
        <div v-else class="raw-content">{{ viewResume.content || '暂无内容' }}</div>
      </div>
    </el-dialog>

    <!-- Generate dialog -->
    <el-dialog v-model="generateDialogVisible" title="AI 生成简历" width="440px" @closed="generateStudentNo = ''">
      <p class="generate-intro">输入学生学号，AI 将自动获取学生信息并生成专业简历，包含评分与优化建议。</p>
      <el-form label-width="0" style="margin-top: var(--space-4)">
        <el-form-item>
          <el-input v-model="generateStudentNo" placeholder="请输入学号" size="large" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="generating" @click="handleGenerate">开始生成</el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getResumes, generateResume } from '@/api/employment'
import { ElMessage } from 'element-plus'
import PageShell from '@/components/page-shell.vue'
import EmptyState from '@/components/empty-state.vue'
import SkeletonTable from '@/components/skeleton-table.vue'

interface Resume {
  id: number; title: string; studentId: number; studentName: string; studentNo: string
  targetJob: string; targetCity: string; aiScore: number; isDefault: boolean; content: string; createTime: string
}

const loading = ref(false)
const initialLoading = ref(true)
const allData = ref<Resume[]>([])
const searchStudentNo = ref('')
const page = ref(1)
const size = ref(10)

const filteredData = computed(() => {
  if (!searchStudentNo.value) return allData.value
  return allData.value.filter(item => String(item.studentNo).includes(searchStudentNo.value))
})

const pagedData = computed(() => {
  const start = (page.value - 1) * size.value
  return filteredData.value.slice(start, start + size.value)
})

function handleSearch() { page.value = 1 }

const viewDialogVisible = ref(false)
const viewResume = ref<Resume | null>(null)
function openViewDialog(row: Resume) { viewResume.value = row; viewDialogVisible.value = true }

const parsedContent = computed(() => {
  if (!viewResume.value?.content) return null
  try { return JSON.parse(viewResume.value.content) } catch { return null }
})

const generateDialogVisible = ref(false)
const generateStudentNo = ref('')
const generating = ref(false)

function openGenerateDialog() { generateStudentNo.value = ''; generateDialogVisible.value = true }

async function handleGenerate() {
  if (!generateStudentNo.value) { ElMessage.warning('请输入学号'); return }
  generating.value = true
  try {
    await generateResume(generateStudentNo.value)
    ElMessage.success('简历生成成功')
    generateDialogVisible.value = false
    fetchData()
  } finally { generating.value = false }
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getResumes({ page: 1, size: 999 })
    allData.value = res.data?.records || []
  } finally { loading.value = false; initialLoading.value = false }
}

onMounted(fetchData)
</script>

<style scoped>
.toolbar-filter { display: flex; gap: var(--space-2); margin-bottom: var(--space-3); }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--space-4); }
.generate-intro { font-size: var(--font-size-base); color: var(--color-text-secondary); line-height: var(--line-height-relaxed); }
.view-content-section { margin-top: var(--space-4); }
.view-content-section h4 { font-size: var(--font-size-sm); font-weight: var(--font-weight-semibold); margin-bottom: var(--space-2); }
.parsed-content { display: flex; flex-direction: column; gap: var(--space-2); }
.content-field { font-size: var(--font-size-sm); line-height: 1.6; }
.content-field-label { font-weight: var(--font-weight-medium); color: var(--color-text-primary); }
.content-field-value { color: var(--color-text-secondary); }
.raw-content { padding: var(--space-3); background: var(--color-body-bg); border-radius: var(--radius-md); font-size: var(--font-size-sm); color: var(--color-text-secondary); white-space: pre-wrap; max-height: 300px; overflow-y: auto; }
</style>
