<template>
  <div>
    <el-card>
      <el-row :gutter="16" style="margin-bottom: 16px">
        <el-col :span="6">
          <el-input v-model="searchStudentId" placeholder="输入学生ID" clearable @keyup.enter="handleSearch" />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="openGenerateDialog">AI生成</el-button>
        </el-col>
      </el-row>

      <el-table :data="pagedData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="简历标题" min-width="140" />
        <el-table-column prop="studentId" label="学生ID" width="90" />
        <el-table-column prop="targetJob" label="目标岗位" min-width="120" />
        <el-table-column prop="targetCity" label="目标城市" width="100" />
        <el-table-column prop="aiScore" label="AI评分" width="80" />
        <el-table-column prop="isDefault" label="是否默认" width="90">
          <template #default="{ row }">
            <el-tag :type="row.isDefault ? 'success' : 'info'" size="small">
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

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :page-sizes="[10, 20, 50]"
        :total="filteredData.length"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 查看简历详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="简历详情" width="700px" top="5vh">
      <el-descriptions v-if="viewResume" :column="2" border>
        <el-descriptions-item label="简历标题">{{ viewResume.title }}</el-descriptions-item>
        <el-descriptions-item label="学生ID">{{ viewResume.studentId }}</el-descriptions-item>
        <el-descriptions-item label="目标岗位">{{ viewResume.targetJob }}</el-descriptions-item>
        <el-descriptions-item label="目标城市">{{ viewResume.targetCity }}</el-descriptions-item>
        <el-descriptions-item label="AI评分">{{ viewResume.aiScore }}</el-descriptions-item>
        <el-descriptions-item label="是否默认">
          <el-tag :type="viewResume.isDefault ? 'success' : 'info'" size="small">
            {{ viewResume.isDefault ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ viewResume.createTime }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="viewResume" style="margin-top: 16px">
        <h4>简历内容</h4>
        <div v-if="parsedContent" style="margin-top: 8px">
          <el-descriptions :column="1" border>
            <el-descriptions-item v-for="(value, key) in parsedContent" :key="key" :label="String(key)">
              {{ value }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <el-input
          v-else
          :model-value="viewResume.content"
          type="textarea"
          :autosize="{ minRows: 6, maxRows: 20 }"
          readonly
          style="margin-top: 8px"
        />
      </div>
    </el-dialog>

    <!-- AI生成简历弹窗 -->
    <el-dialog v-model="generateDialogVisible" title="AI生成简历" width="450px" @closed="generateStudentId = ''">
      <el-form label-width="100px">
        <el-form-item label="学生ID" required>
          <el-input v-model="generateStudentId" placeholder="请输入学生ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="generating" @click="handleGenerate">开始生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getResumes, generateResume } from '@/api/employment'
import { ElMessage } from 'element-plus'

interface Resume {
  id: number
  title: string
  studentId: number
  targetJob: string
  targetCity: string
  aiScore: number
  isDefault: boolean
  content: string
  createTime: string
}

const loading = ref(false)
const allData = ref<Resume[]>([])
const searchStudentId = ref('')
const page = ref(1)
const size = ref(10)

const filteredData = computed<Resume[]>(() => {
  if (!searchStudentId.value) return allData.value
  return allData.value.filter(item =>
    String(item.studentId).includes(searchStudentId.value)
  )
})

const pagedData = computed<Resume[]>(() => {
  const start = (page.value - 1) * size.value
  return filteredData.value.slice(start, start + size.value)
})

function handleSearch() {
  page.value = 1
}

const viewDialogVisible = ref(false)
const viewResume = ref<Resume | null>(null)

function openViewDialog(row: Resume) {
  viewResume.value = row
  viewDialogVisible.value = true
}

const parsedContent = computed(() => {
  if (!viewResume.value?.content) return null
  try {
    return JSON.parse(viewResume.value.content)
  } catch {
    return null
  }
})

const generateDialogVisible = ref(false)
const generateStudentId = ref('')
const generating = ref(false)

function openGenerateDialog() {
  generateStudentId.value = ''
  generateDialogVisible.value = true
}

async function handleGenerate() {
  if (!generateStudentId.value) {
    ElMessage.warning('请输入学生ID')
    return
  }
  generating.value = true
  try {
    await generateResume(Number(generateStudentId.value))
    ElMessage.success('简历生成成功')
    generateDialogVisible.value = false
    fetchData()
  } catch {
    // error handled by interceptor
  } finally {
    generating.value = false
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getResumes()
    allData.value = res.data || []
  } catch {
    allData.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>
