<template>
  <PageShell title="用户管理" description="管理系统用户账号和权限">
    <template #toolbar>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>添加用户
      </el-button>
    </template>

    <template #search>
      <el-form :inline="true" :model="searchForm" size="default">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索用户名/姓名" clearable style="width: 220px" @keyup.enter="handleSearch" />
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
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="手机" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="userType" label="类型" width="90">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ userTypeMap[row.userType] ?? row.userType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="plain">
              {{ row.status === 1 ? '启用' : '禁用' }}
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

      <EmptyState v-else icon="UserFilled" title="暂无用户" description="点击「添加用户」创建新账号" />

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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '添加用户'" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码" :prop="isEdit ? undefined : 'password'">
          <el-input v-model="form.password" type="password" :placeholder="isEdit ? '留空则不修改' : '请输入密码'" show-password />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="form.userType" style="width: 100%">
            <el-option label="学生" :value="0" />
            <el-option label="教师" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getUsers, addUser, updateUser, deleteUser } from '@/api/system'
import PageShell from '@/components/page-shell.vue'
import EmptyState from '@/components/empty-state.vue'
import SkeletonTable from '@/components/skeleton-table.vue'

const userTypeMap: Record<number, string> = { 0: '学生', 1: '教师', 2: '管理员' }

const searchForm = reactive({ keyword: '' })
const tableData = ref<any[]>([])
const loading = ref(false)
const initialLoading = ref(true)
const pagination = reactive({ page: 1, size: 10, total: 0 })

const dialogVisible = ref(false); const isEdit = ref(false); const editId = ref<number | null>(null)
const submitLoading = ref(false); const formRef = ref<FormInstance>()

const form = reactive({ username: '', realName: '', password: '', phone: '', email: '', userType: 0 as number, status: 1 as number })

const rules = computed<FormRules>(() => {
  const base: FormRules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
    userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
  }
  if (!isEdit.value) base.password = [{ required: true, message: '请输入密码', trigger: 'blur' }]
  return base
})

async function fetchData() {
  loading.value = true
  try {
    const params: any = { page: pagination.page, size: pagination.size }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    const res = await getUsers(params)
    const data = res.data || res
    tableData.value = data.records ?? data.list ?? []
    pagination.total = data.total ?? 0
  } finally { loading.value = false; initialLoading.value = false }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; handleSearch() }

function handleAdd() {
  isEdit.value = false; editId.value = null
  form.username = ''; form.realName = ''; form.password = ''; form.phone = ''; form.email = ''; form.userType = 0; form.status = 1
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true; editId.value = row.id
  form.username = row.username || ''; form.realName = row.realName || ''; form.password = ''
  form.phone = row.phone || ''; form.email = row.email || ''; form.userType = row.userType ?? 0; form.status = row.status ?? 1
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false); if (!valid) return
  submitLoading.value = true
  try {
    const data: any = { username: form.username, realName: form.realName, phone: form.phone, email: form.email, userType: form.userType, status: form.status }
    if (form.password) data.password = form.password
    if (isEdit.value && editId.value != null) { data.id = editId.value; await updateUser(data); ElMessage.success('编辑成功') }
    else { await addUser(data); ElMessage.success('添加成功') }
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

function handleDelete(row: any) {
  ElMessageBox.confirm('确定要删除该用户吗？', '提示', { type: 'warning' })
    .then(async () => { await deleteUser(row.id); ElMessage.success('删除成功'); fetchData() })
    .catch(() => {})
}

onMounted(fetchData)
</script>

<style scoped>
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--space-4); }
</style>
