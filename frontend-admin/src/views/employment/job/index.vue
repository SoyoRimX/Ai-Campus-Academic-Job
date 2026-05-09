<template>
  <div>
    <el-card>
      <el-row :gutter="16" style="margin-bottom: 16px">
        <el-col :span="6">
          <el-input v-model="keyword" placeholder="输入关键字搜索" clearable @keyup.enter="handleSearch" />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button type="success" @click="openAddDialog">新增岗位</el-button>
        </el-col>
      </el-row>

      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="jobName" label="岗位名称" min-width="120" />
        <el-table-column prop="companyName" label="公司" min-width="140" />
        <el-table-column prop="salary" label="薪资" width="120" />
        <el-table-column prop="city" label="城市" width="90" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val: boolean) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-popconfirm
              title="确定删除该岗位吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
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

    <!-- 新增/编辑岗位弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑岗位' : '新增岗位'"
      width="600px"
      top="5vh"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="岗位名称" prop="jobName">
          <el-input v-model="form.jobName" placeholder="请输入岗位名称" />
        </el-form-item>
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="form.companyName" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="薪资范围" prop="salary">
          <el-input v-model="form.salary" placeholder="如：8K-15K" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="form.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="学历要求" prop="education">
          <el-input v-model="form.education" placeholder="如：本科" />
        </el-form-item>
        <el-form-item label="经验要求" prop="experience">
          <el-input v-model="form.experience" placeholder="如：1-3年" />
        </el-form-item>
        <el-form-item label="技能要求" prop="skills">
          <el-input v-model="form.skills" placeholder="多个技能用逗号分隔" />
        </el-form-item>
        <el-form-item label="岗位描述" prop="description">
          <el-input v-model="form.description" type="textarea" :autosize="{ minRows: 4, maxRows: 8 }" placeholder="请输入岗位描述" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getJobs, addJob, updateJob, deleteJob } from '@/api/employment'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

interface Job {
  id?: number
  jobName: string
  companyName: string
  salary: string
  city: string
  education: string
  experience: string
  skills: string
  description: string
  type: number
  status?: number
}

const loading = ref(false)
const tableData = ref<Job[]>([])
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

function typeLabel(type: number): string {
  const labels: Record<number, string> = { 1: '全职', 2: '实习', 3: '校招' }
  return labels[type] || '未知'
}

function handleSearch() {
  page.value = 1
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getJobs({ page: page.value, size: size.value, keyword: keyword.value || undefined })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const emptyForm: Job = {
  jobName: '',
  companyName: '',
  salary: '',
  city: '',
  education: '',
  experience: '',
  skills: '',
  description: '',
  type: 1
}

const form = reactive<Job>({ ...emptyForm })

const rules: FormRules = {
  jobName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  salary: [{ required: true, message: '请输入薪资范围', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

function openAddDialog() {
  isEdit.value = false
  submitId.value = null
  Object.assign(form, emptyForm)
  dialogVisible.value = true
}

function openEditDialog(row: Job) {
  isEdit.value = true
  submitId.value = row.id ?? null
  Object.assign(form, row)
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
}

async function handleStatusChange(row: any, val: boolean) {
  try {
    await updateJob({ id: row.id, status: val ? 1 : 0 })
    row.status = val ? 1 : 0
    ElMessage.success(val ? '已启用' : '已禁用')
  } catch {
    // error handled by interceptor
  }
}

async function handleSubmit() {
  const valid = await formRef.value!.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value && submitId.value) {
      await updateJob({ id: submitId.value, ...form })
      ElMessage.success('编辑成功')
    } else {
      await addJob(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: Job) {
  if (!row.id) return
  try {
    await deleteJob(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // error handled by interceptor
  }
}

onMounted(() => {
  fetchData()
})
</script>
