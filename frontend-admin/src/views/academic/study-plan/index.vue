<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-select v-if="!isStudent" v-model="searchForm.studentId" placeholder="筛选学生" clearable style="width:200px" @change="fetchData">
          <el-option v-for="s in studentOptions" :key="s.id" :label="s.studentNo + ' ' + s.major" :value="s.id" />
        </el-select>
        <span v-else></span>
        <el-button type="primary" @click="openAddDialog">添加规划</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="studentName" label="学生姓名" width="90" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="planTitle" label="规划标题" min-width="180" />
        <el-table-column prop="semester" label="目标学期" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="openViewDialog(row)">查看</el-button>
            <el-button size="small" link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除?" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="searchForm.page"
        v-model:page-size="searchForm.size"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="editId ? '编辑规划' : '添加规划'" v-model="dialogVisible" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="学生ID" required>
          <el-input-number v-model="form.studentId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="规划标题" required>
          <el-input v-model="form.planTitle" />
        </el-form-item>
        <el-form-item label="目标学期" required>
          <el-input v-model="form.semester" placeholder="如 2024-2025-2" />
        </el-form-item>
        <el-form-item label="规划详情">
          <el-input v-model="form.planDetail" type="textarea" :rows="6" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="进行中" :value="0" />
            <el-option label="已完成" :value="1" />
            <el-option label="已放弃" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog title="规划详情" v-model="viewVisible" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="规划标题" :span="2">{{ viewData.planTitle }}</el-descriptions-item>
        <el-descriptions-item label="目标学期">{{ viewData.semester }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusMap[viewData.status]?.type">{{ statusMap[viewData.status]?.text }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ viewData.createTime }}</el-descriptions-item>
      </el-descriptions>
      <div style="margin-top:16px">
        <strong>规划详情：</strong>
        <div style="white-space:pre-wrap;margin-top:8px;color:#606266">{{ viewData.planDetail || '暂无' }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getStudyPlans, addStudyPlan, updateStudyPlan, deleteStudyPlan } from '@/api/academic'
import { getStudents } from '@/api/academic'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isStudent = computed(() => userStore.userInfo?.userType === 0)

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const viewVisible = ref(false)
const editId = ref<number | null>(null)
const viewData = ref<any>({})
const studentOptions = ref<any[]>([])

const searchForm = reactive({ page: 1, size: 10, studentId: undefined as number | undefined })
const form = reactive({ studentId: 1, planTitle: '', semester: '', planDetail: '', status: 0 })

const statusMap: Record<number, { type: string; text: string }> = {
  0: { type: 'warning', text: '进行中' },
  1: { type: 'success', text: '已完成' },
  2: { type: 'info', text: '已放弃' }
}

onMounted(() => {
  fetchData()
  loadStudents()
})

async function loadStudents() {
  const res = await getStudents({ page: 1, size: 999 })
  studentOptions.value = res.data?.records || []
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getStudyPlans({ ...searchForm })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  editId.value = null
  Object.assign(form, { studentId: 1, planTitle: '', semester: '', planDetail: '', status: 0 })
  dialogVisible.value = true
}

function openEditDialog(row: any) {
  editId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

function openViewDialog(row: any) {
  viewData.value = row
  viewVisible.value = true
}

async function handleSave() {
  if (!form.planTitle || !form.semester) {
    ElMessage.warning('请填写标题和学期')
    return
  }
  if (editId.value) {
    await updateStudyPlan({ id: editId.value, ...form })
    ElMessage.success('更新成功')
  } else {
    await addStudyPlan(form)
    ElMessage.success('添加成功')
  }
  dialogVisible.value = false
  fetchData()
}

async function handleDelete(id: number) {
  await deleteStudyPlan(id)
  ElMessage.success('删除成功')
  fetchData()
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}
</style>
