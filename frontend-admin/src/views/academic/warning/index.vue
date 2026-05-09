<template>
  <div class="warning-page">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="学生姓名">
          <el-input
            v-model="searchForm.studentId"
            placeholder="请输入学生姓名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="title" label="预警标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="studentId" label="学生ID" width="100" />
        <el-table-column prop="type" label="预警类型" width="120">
          <template #default="{ row }">
            {{ typeMap[row.type] || '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="level" label="预警等级" width="100">
          <template #default="{ row }">
            <el-tag :type="levelColorMap[row.level] || 'info'">
              {{ levelTextMap[row.level] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isRead" label="是否已读" width="90">
          <template #default="{ row }">
            <el-tag :type="row.isRead ? 'success' : 'info'" size="small">
              {{ row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isHandled" label="是否处理" width="90">
          <template #default="{ row }">
            <el-tag :type="row.isHandled ? 'success' : 'danger'" size="small">
              {{ row.isHandled ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!row.isHandled"
              type="primary"
              size="small"
              @click="openHandleDialog(row)"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="searchForm.page"
          v-model:page-size="searchForm.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 处理弹窗 -->
    <el-dialog v-model="dialogVisible" title="处理预警" width="500px" :close-on-click-modal="false">
      <el-form :model="handleForm" label-width="80px">
        <el-form-item label="预警标题">
          <span>{{ currentRow?.title }}</span>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="handleForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="handleLoading" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getWarnings, handleWarning } from '@/api/academic'

interface WarningRecord {
  id: number
  title: string
  studentId: number
  type: number
  level: number
  isRead: boolean
  isHandled: boolean
  createTime: string
}

const typeMap: Record<number, string> = {
  1: '挂科预警',
  2: '绩点预警',
  3: '学分预警',
  4: '出勤预警'
}

const levelColorMap: Record<number, string> = {
  1: 'warning',
  2: 'danger',
  3: 'danger'
}

const levelTextMap: Record<number, string> = {
  1: '黄色',
  2: '橙色',
  3: '红色'
}

const loading = ref(false)
const tableData = ref<WarningRecord[]>([])
const total = ref(0)

const searchForm = reactive({
  studentId: '',
  page: 1,
  size: 10
})

function fetchData() {
  loading.value = true
  const params: { page: number; size: number; studentId?: string } = {
    page: searchForm.page,
    size: searchForm.size
  }
  if (searchForm.studentId) {
    params.studentId = searchForm.studentId
  }
  getWarnings(params as any)
    .then((res: any) => {
      if (res.data) {
        tableData.value = res.data.records || []
        total.value = res.data.total || 0
      }
    })
    .finally(() => {
      loading.value = false
    })
}

function handleSearch() {
  searchForm.page = 1
  fetchData()
}

function handleReset() {
  searchForm.studentId = ''
  searchForm.page = 1
  fetchData()
}

function handleSizeChange() {
  searchForm.page = 1
  fetchData()
}

function handlePageChange() {
  fetchData()
}

// ---- 处理弹窗 ----
const dialogVisible = ref(false)
const handleLoading = ref(false)
const currentRow = ref<WarningRecord | null>(null)

const handleForm = reactive({
  remark: ''
})

function openHandleDialog(row: WarningRecord) {
  currentRow.value = row
  handleForm.remark = ''
  dialogVisible.value = true
}

function submitHandle() {
  if (!currentRow.value) return
  handleLoading.value = true
  handleWarning(currentRow.value.id, { remark: handleForm.remark })
    .then(() => {
      ElMessage.success('处理成功')
      dialogVisible.value = false
      fetchData()
    })
    .finally(() => {
      handleLoading.value = false
    })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.warning-page {
  padding: 16px;
}
.search-card {
  margin-bottom: 16px;
}
.table-card {
  min-height: 400px;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
