<template>
  <PageShell title="简历管理" description="查看和管理学生简历，支持 AI 智能生成">
    <template #toolbar>
      <el-button @click="openJdDialog">
        <el-icon><Document /></el-icon>JD 优化简历
      </el-button>
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

    <!-- ================================================================
         JD 优化 Dialog
         ================================================================ -->
    <el-dialog v-model="jdDialogVisible" :title="jdStep === 0 ? 'JD 优化简历' : jdStep === 1 ? '分析中…' : '优化建议预览'" :width="jdStep === 2 ? '760px' : '520px'" top="5vh" @closed="resetJd">
      <!-- Step 0: 输入 JD + 选择学生 -->
      <template v-if="jdStep === 0">
        <p class="generate-intro">粘贴目标岗位的 JD（职位描述），AI 将对比学生简历与 JD 需求，识别差距并结合知识库生成针对性修改建议。</p>
        <el-form label-width="0" style="margin-top: var(--space-4)">
          <el-form-item>
            <el-select v-model="jdStudentId" placeholder="选择学生" filterable style="width:100%">
              <el-option v-for="r in allData" :key="r.studentId" :label="r.studentName + ' (' + r.studentNo + ')'" :value="r.studentId" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-input v-model="jdText" type="textarea" :rows="8" placeholder="在此粘贴 JD 全文…&#10;&#10;示例：&#10;招聘前端开发工程师，负责Web前端架构设计与核心功能开发。要求：精通React/Vue，熟悉TypeScript，3年以上项目经验，有大型项目架构经验优先。" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="jdDialogVisible = false">取消</el-button>
          <el-button type="primary" :disabled="!jdStudentId || !jdText.trim()" :loading="jdLoading" @click="startOptimize">开始分析</el-button>
        </template>
      </template>

      <!-- Step 1: 分析中（6 步 Stepper） -->
      <template v-if="jdStep === 1">
        <div class="jd-stepper">
          <div v-for="(s, i) in jdSteps" :key="i" class="jd-step" :class="{ done: i < jdStepIndex, active: i === jdStepIndex }">
            <div class="jd-step-dot">{{ i < jdStepIndex ? '✓' : i === jdStepIndex ? '●' : i + 1 }}</div>
            <span class="jd-step-label">{{ s }}</span>
          </div>
        </div>
        <p class="jd-progress-text">{{ jdProgressText }}</p>
      </template>

      <!-- Step 2: 建议预览 -->
      <template v-if="jdStep === 2 && jdResult">
        <!-- 摘要 -->
        <div class="jd-summary">
          <div class="jd-summary-item" v-if="jdResult.jdParsed">
            <strong>解析岗位：</strong>{{ jdResult.jdParsed.position }}
            <span v-if="jdResult.jdParsed.requiredSkills?.length">
              · 必备技能：{{ jdResult.jdParsed.requiredSkills.join('、') }}
            </span>
          </div>
          <div class="jd-summary-item" v-if="jdResult.gapAnalysis">
            <strong>差距分析：</strong>{{ jdResult.gapAnalysis.overallAssessment }}
            <span v-if="jdResult.gapAnalysis.missingSkills?.length" style="color:var(--el-color-danger)">
              · 缺失：{{ jdResult.gapAnalysis.missingSkills.join('、') }}
            </span>
            <span v-if="jdResult.gapAnalysis.matchedSkills?.length" style="color:var(--el-color-success)">
              · 匹配：{{ jdResult.gapAnalysis.matchedSkills.join('、') }}
            </span>
          </div>
        </div>

        <!-- 建议列表 -->
        <div class="jd-suggestions">
          <div class="jd-suggestion-header">
            <h4>修改建议（{{ jdResult.suggestions?.length || 0 }} 条）</h4>
            <div class="jd-suggestion-actions">
              <el-button size="small" text @click="toggleAll(true)">全选</el-button>
              <el-button size="small" text @click="toggleAll(false)">全不选</el-button>
            </div>
          </div>

          <div v-for="(s, i) in jdResult.suggestions" :key="i" class="jd-suggestion-card" :class="{ unchecked: !s._checked }">
            <div class="jd-suggestion-top">
              <el-checkbox v-model="s._checked" />
              <el-tag size="small" :type="s.verified === false ? 'danger' : 'success'">
                {{ s.verified === false ? '需注意' : '已审查' }}
              </el-tag>
              <span class="jd-section-badge">{{ s.section }}</span>
            </div>
            <div class="jd-suggestion-body">
              <div class="jd-suggestion-row">
                <span class="jd-row-label">原文</span>
                <span class="jd-row-text dim">{{ s.original }}</span>
              </div>
              <div class="jd-suggestion-row">
                <span class="jd-row-label">建议</span>
                <span class="jd-row-text highlight">{{ s.suggestion }}</span>
              </div>
              <div class="jd-suggestion-row">
                <span class="jd-row-label">原因</span>
                <span class="jd-row-text">{{ s.reason }}</span>
              </div>
              <div class="jd-suggestion-warning" v-if="s.warning && !s.verified">
                <el-icon><Warning /></el-icon> {{ s.warning }}
              </div>
            </div>
          </div>
        </div>

        <!-- RAG 来源 -->
        <div class="jd-rag" v-if="jdResult.ragSources?.length">
          <h4>参考知识库（{{ jdResult.ragSources.length }} 条）</h4>
          <div v-for="(src, i) in jdResult.ragSources" :key="i" class="jd-rag-item">
            <el-tag size="small" type="info">{{ src.category }}</el-tag>
            <span class="jd-rag-title">{{ src.title }}</span>
            <span class="jd-rag-snippet">{{ src.snippet }}</span>
          </div>
        </div>

        <template #footer>
          <el-button @click="jdDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="jdApplying" @click="applyOptimize">
            确认应用（{{ checkedCount }} 条）
          </el-button>
        </template>
      </template>
    </el-dialog>
  </PageShell>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getResumes, generateResume, optimizeResume, applyOptimize } from '@/api/employment'
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

// ================================================================
// JD 优化
// ================================================================
const jdDialogVisible = ref(false)
const jdStep = ref(0)               // 0=输入, 1=分析中, 2=预览
const jdStepIndex = ref(0)
const jdProgressText = ref('')
const jdStudentId = ref<number | null>(null)
const jdText = ref('')
const jdLoading = ref(false)
const jdApplying = ref(false)
const jdResult = ref<any>(null)

const jdSteps = ['JD 解析', '差距分析', 'RAG 检索', '内容重写', '自我审查', '打包结果']

const checkedCount = computed(() =>
  jdResult.value?.suggestions?.filter((s: any) => s._checked).length || 0
)

function openJdDialog() {
  jdStep.value = 0; jdStepIndex.value = 0; jdText.value = ''
  jdResult.value = null; jdStudentId.value = null
  jdDialogVisible.value = true
}

function resetJd() {
  jdStep.value = 0; jdResult.value = null; jdLoading.value = false; jdApplying.value = false
}

async function startOptimize() {
  if (!jdStudentId.value || !jdText.value.trim()) return
  jdLoading.value = true
  jdStep.value = 1; jdStepIndex.value = 0

  // 模拟进度推进
  const timer = setInterval(() => {
    if (jdStepIndex.value < jdSteps.length - 1) {
      jdStepIndex.value++
      jdProgressText.value = `正在执行：${jdSteps[jdStepIndex.value]}`
    }
  }, 800)

  try {
    const res: any = await optimizeResume({ studentId: jdStudentId.value, jdText: jdText.value })
    clearInterval(timer)
    jdStepIndex.value = jdSteps.length
    jdProgressText.value = '分析完成'
    const data = res.data || {}
    // 给每条建议默认选中
    if (data.suggestions) data.suggestions.forEach((s: any) => s._checked = true)
    jdResult.value = data
    setTimeout(() => { jdStep.value = 2; jdLoading.value = false }, 500)
  } catch {
    clearInterval(timer)
    jdLoading.value = false
    jdStep.value = 0
    ElMessage.error('分析失败，请检查网络或 AI API Key 配置')
  }
}

function toggleAll(v: boolean) {
  jdResult.value?.suggestions?.forEach((s: any) => s._checked = v)
}

async function applyOptimize() {
  const accepted = jdResult.value?.suggestions?.filter((s: any) => s._checked) || []
  if (!accepted.length) { ElMessage.warning('请至少选择一条建议'); return }
  jdApplying.value = true
  try {
    await applyOptimize({ studentId: jdStudentId.value!, acceptedSuggestions: accepted })
    ElMessage.success(`已应用 ${accepted.length} 条建议`)
    jdDialogVisible.value = false
    fetchData()
  } catch {
    ElMessage.error('应用失败')
  } finally { jdApplying.value = false }
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

/* ---- JD Stepper ---- */
.jd-stepper { display: flex; justify-content: space-between; padding: var(--space-6) var(--space-4); }
.jd-step { display: flex; flex-direction: column; align-items: center; gap: var(--space-2); flex: 1; position: relative; }
.jd-step::after { content: ''; position: absolute; top: 12px; left: 50%; width: 100%; height: 2px; background: var(--color-border); z-index: 0; }
.jd-step:last-child::after { display: none; }
.jd-step.done::after { background: var(--color-primary-500); }
.jd-step-dot { width: 24px; height: 24px; border-radius: 50%; background: var(--color-border); color: var(--color-text-secondary); display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: var(--font-weight-bold); z-index: 1; transition: all var(--transition-fast); }
.jd-step.done .jd-step-dot { background: var(--color-success-500); color: #fff; }
.jd-step.active .jd-step-dot { background: var(--color-primary-500); color: #fff; animation: jd-pulse 1s ease infinite; }
@keyframes jd-pulse { 0%, 100% { box-shadow: 0 0 0 0 rgba(79, 110, 247, 0.4); } 50% { box-shadow: 0 0 0 8px rgba(79, 110, 247, 0); } }
.jd-step-label { font-size: var(--font-size-xs); color: var(--color-text-secondary); white-space: nowrap; text-align: center; }
.jd-step.done .jd-step-label { color: var(--color-success-500); }
.jd-step.active .jd-step-label { color: var(--color-primary-500); font-weight: var(--font-weight-semibold); }
.jd-progress-text { text-align: center; font-size: var(--font-size-sm); color: var(--color-text-secondary); margin-top: var(--space-4); }

/* ---- JD Summary ---- */
.jd-summary { margin-bottom: var(--space-4); padding: var(--space-3) var(--space-4); background: var(--color-primary-50); border-radius: var(--radius-md); }
.jd-summary-item { font-size: var(--font-size-sm); color: var(--color-text-primary); line-height: 1.8; }

/* ---- JD Suggestions ---- */
.jd-suggestion-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-3); }
.jd-suggestion-header h4 { font-size: var(--font-size-md); font-weight: var(--font-weight-semibold); }
.jd-suggestion-actions { display: flex; gap: var(--space-1); }
.jd-suggestions { max-height: 400px; overflow-y: auto; }
.jd-suggestion-card { border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: var(--space-3); margin-bottom: var(--space-2); transition: all var(--transition-fast); }
.jd-suggestion-card.unchecked { opacity: 0.5; border-color: var(--color-border-light); }
.jd-suggestion-top { display: flex; align-items: center; gap: var(--space-2); margin-bottom: var(--space-2); }
.jd-section-badge { font-size: var(--font-size-xs); font-weight: var(--font-weight-medium); color: var(--color-primary-600); background: var(--color-primary-50); padding: 1px 8px; border-radius: var(--radius-sm); }
.jd-suggestion-body { display: flex; flex-direction: column; gap: var(--space-2); padding-left: var(--space-5); }
.jd-suggestion-row { font-size: var(--font-size-sm); line-height: 1.5; }
.jd-row-label { font-weight: var(--font-weight-semibold); color: var(--color-text-primary); margin-right: var(--space-1); font-size: var(--font-size-xs); }
.jd-row-text { color: var(--color-text-secondary); }
.jd-row-text.dim { color: var(--color-text-placeholder); }
.jd-row-text.highlight { color: var(--color-primary-700); font-weight: var(--font-weight-medium); }
.jd-suggestion-warning { font-size: var(--font-size-xs); color: var(--color-danger-500); background: var(--color-danger-50); padding: var(--space-1) var(--space-2); border-radius: var(--radius-sm); display: flex; align-items: center; gap: var(--space-1); }

/* ---- JD RAG ---- */
.jd-rag { margin-top: var(--space-4); padding-top: var(--space-4); border-top: 1px solid var(--color-border); }
.jd-rag h4 { font-size: var(--font-size-sm); font-weight: var(--font-weight-semibold); margin-bottom: var(--space-2); }
.jd-rag-item { display: flex; align-items: center; gap: var(--space-2); padding: var(--space-1) 0; font-size: var(--font-size-xs); }
.jd-rag-title { color: var(--color-text-primary); font-weight: var(--font-weight-medium); }
.jd-rag-snippet { color: var(--color-text-secondary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 300px; }
</style>
