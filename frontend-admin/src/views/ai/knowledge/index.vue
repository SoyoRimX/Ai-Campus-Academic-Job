<template>
  <PageShell title="知识库管理" description="管理 AI 智能体的知识库文档">
    <template #toolbar>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>添加文档
      </el-button>
    </template>

    <template #search>
      <el-form :inline="true" :model="searchForm" size="default">
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="选择分类" clearable style="width: 160px">
            <el-option label="全部" value="" />
            <el-option label="课程" value="course" />
            <el-option label="岗位" value="job" />
            <el-option label="面试" value="interview" />
            <el-option label="学校政策" value="school_policy" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </template>

    <SkeletonTable v-if="initialLoading" />
    <template v-else>
      <el-table v-if="tableData.length" :data="tableData" stripe v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ categoryMap[row.category] || row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" min-width="140" show-overflow-tooltip />
        <el-table-column prop="vectorized" label="向量化" width="100">
          <template #default="{ row }">
            <el-tag :type="row.vectorized === 1 ? 'success' : 'info'" size="small" effect="plain">
              {{ row.vectorized === 1 ? '已完成' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState v-else icon="Collection" title="暂无文档" description="点击「添加文档」扩充 AI 知识库" />

      <div class="pagination-wrapper" v-if="pagination.total > 0">
        <el-pagination
          v-model:current-page="pagination.page" v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]" :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData" @current-change="fetchData"
        />
      </div>
    </template>

    <!-- Add/Edit dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑文档' : '添加文档'" width="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="7" placeholder="请输入知识库内容" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="课程" value="course" />
            <el-option label="岗位" value="job" />
            <el-option label="面试" value="interview" />
            <el-option label="学校政策" value="school_policy" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getKnowledgeList, addKnowledge, updateKnowledge, deleteKnowledge } from '@/api/ai'
import PageShell from '@/components/page-shell.vue'
import EmptyState from '@/components/empty-state.vue'
import SkeletonTable from '@/components/skeleton-table.vue'

const categoryMap: Record<string, string> = { course: '课程', job: '岗位', interview: '面试', school_policy: '学校政策' }

const searchForm = reactive({ category: '' })
const tableData = ref<any[]>([])
const loading = ref(false)
const initialLoading = ref(true)
const pagination = reactive({ page: 1, size: 10, total: 0 })

const dialogVisible = ref(false); const isEdit = ref(false); const editId = ref<number | null>(null)
const submitLoading = ref(false); const formRef = ref<FormInstance>()

const form = reactive({ title: '', content: '', category: '', tags: '' })

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { page: pagination.page, size: pagination.size }
    if (searchForm.category) params.category = searchForm.category
    const res = await getKnowledgeList(params)
    const data = res.data || res
    tableData.value = data.records ?? data.list ?? []
    pagination.total = data.total ?? 0
  } finally { loading.value = false; initialLoading.value = false }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.category = ''; handleSearch() }

function handleAdd() { isEdit.value = false; editId.value = null; form.title = ''; form.content = ''; form.category = ''; form.tags = ''; dialogVisible.value = true }
function handleEdit(row: any) { isEdit.value = true; editId.value = row.id; form.title = row.title || ''; form.content = row.content || ''; form.category = row.category || ''; form.tags = row.tags || ''; dialogVisible.value = true }

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false); if (!valid) return
  submitLoading.value = true
  try {
    const data: any = { title: form.title, content: form.content, category: form.category, tags: form.tags }
    if (isEdit.value && editId.value != null) { data.id = editId.value; await updateKnowledge(data); ElMessage.success('编辑成功') }
    else { await addKnowledge(data); ElMessage.success('添加成功') }
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

function handleDelete(row: any) {
  ElMessageBox.confirm('确定要删除该文档吗？', '提示', { type: 'warning' })
    .then(async () => { await deleteKnowledge(row.id); ElMessage.success('删除成功'); fetchData() })
    .catch(() => {})
}

onMounted(fetchData)
</script>

<style scoped>
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--space-4); }
</style>
