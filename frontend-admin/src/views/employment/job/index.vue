<template>
  <PageShell title="岗位管理" description="管理招聘岗位信息">
    <template #toolbar>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>新增岗位
      </el-button>
    </template>

    <div class="toolbar-filter">
      <el-input v-model="keyword" placeholder="搜索岗位名称、公司" clearable style="width: 240px" @keyup.enter="handleSearch">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button @click="handleSearch">搜索</el-button>
    </div>

    <SkeletonTable v-if="initialLoading" />
    <template v-else>
      <el-table v-if="tableData.length" :data="tableData" stripe v-loading="loading">
        <el-table-column prop="jobTitle" label="岗位名称" min-width="130" show-overflow-tooltip />
        <el-table-column prop="company" label="公司" min-width="140" show-overflow-tooltip />
        <el-table-column prop="salaryRange" label="薪资范围" width="110" />
        <el-table-column prop="city" label="城市" width="90" />
        <el-table-column prop="jobType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ typeLabel(row.jobType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              size="small"
              @change="(val: boolean) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该岗位吗？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState v-else icon="Briefcase" title="暂无岗位" description="点击「新增岗位」添加招聘信息" />

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="page" v-model:page-size="size"
          :page-sizes="[10, 20, 50]" :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchData" @size-change="fetchData"
        />
      </div>
    </template>

    <!-- Add/Edit dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑岗位' : '新增岗位'" width="600px" top="5vh" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="岗位名称" prop="jobTitle">
              <el-input v-model="form.jobTitle" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公司名称" prop="company">
              <el-input v-model="form.company" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="薪资范围" prop="salaryRange">
              <el-input v-model="form.salaryRange" placeholder="如：8K-15K" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="城市" prop="city">
              <el-input v-model="form.city" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学历要求">
              <el-input v-model="form.education" placeholder="如：本科" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经验要求">
              <el-input v-model="form.experience" placeholder="如：1-3年" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="技能要求">
          <el-input v-model="form.requiredSkills" placeholder="多个技能用逗号分隔" />
        </el-form-item>
        <el-form-item label="岗位描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入岗位描述" />
        </el-form-item>
        <el-form-item label="类型" prop="jobType">
          <el-select v-model="form.jobType" style="width: 100%">
            <el-option label="全职" :value="1" />
            <el-option label="实习" :value="2" />
            <el-option label="校招" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getJobs, addJob, updateJob, deleteJob } from '@/api/employment'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import PageShell from '@/components/page-shell.vue'
import EmptyState from '@/components/empty-state.vue'
import SkeletonTable from '@/components/skeleton-table.vue'

interface Job {
  id?: number; jobTitle: string; company: string; salaryRange: string
  city: string; education: string; experience: string; requiredSkills: string
  description: string; jobType: number; status?: number
}

const loading = ref(false)
const initialLoading = ref(true)
const tableData = ref<Job[]>([])
const keyword = ref('')
const page = ref(1); const size = ref(10); const total = ref(0)

function typeLabel(t: number) { const m: Record<number, string> = { 1: '全职', 2: '实习', 3: '校招' }; return m[t] || '未知' }
function handleSearch() { page.value = 1; fetchData() }

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getJobs({ page: page.value, size: size.value, keyword: keyword.value || undefined })
    tableData.value = res.data?.records || []; total.value = res.data?.total || 0
  } finally { loading.value = false; initialLoading.value = false }
}

const dialogVisible = ref(false); const isEdit = ref(false); const submitId = ref<number | null>(null)
const submitting = ref(false); const formRef = ref<FormInstance>()

const emptyForm: Job = { jobTitle: '', company: '', salaryRange: '', city: '', education: '', experience: '', requiredSkills: '', description: '', jobType: 1 }
const form = reactive<Job>({ ...emptyForm })

const rules: FormRules = {
  jobTitle: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  company: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  salaryRange: [{ required: true, message: '请输入薪资范围', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  jobType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

function openAddDialog() { isEdit.value = false; submitId.value = null; Object.assign(form, emptyForm); dialogVisible.value = true }
function openEditDialog(row: Job) { isEdit.value = true; submitId.value = row.id ?? null; Object.assign(form, row); dialogVisible.value = true }
function resetForm() { formRef.value?.resetFields() }

async function handleStatusChange(row: any, val: boolean) {
  try { await updateJob({ id: row.id, status: val ? 1 : 0 }); row.status = val ? 1 : 0; ElMessage.success(val ? '已启用' : '已禁用') } catch {}
}

async function handleSubmit() {
  const valid = await formRef.value!.validate().catch(() => false); if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value && submitId.value) { await updateJob({ id: submitId.value, ...form }); ElMessage.success('编辑成功') }
    else { await addJob(form); ElMessage.success('添加成功') }
    dialogVisible.value = false; fetchData()
  } finally { submitting.value = false }
}

async function handleDelete(row: Job) { if (!row.id) return; await deleteJob(row.id); ElMessage.success('删除成功'); fetchData() }

onMounted(fetchData)
</script>

<style scoped>
.toolbar-filter { display: flex; gap: var(--space-2); margin-bottom: var(--space-3); }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--space-4); }
</style>
