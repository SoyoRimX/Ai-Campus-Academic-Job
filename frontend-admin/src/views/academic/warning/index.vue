<template>
  <PageShell title="学业预警" description="查看和处理学生学业预警信息">
    <template #search>
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="学号">
          <el-input
            v-model="searchForm.keyword"
            placeholder="输入学号搜索"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </template>

    <SkeletonTable v-if="initialLoading" />
    <template v-else>
      <el-table v-if="tableData.length" v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="title" label="预警标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="studentName" label="学生姓名" width="90" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="warningType" label="预警类型" width="110">
          <template #default="{ row }">
            {{ typeMap[row.warningType] || '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="warningLevel" label="预警等级" width="100">
          <template #default="{ row }">
            <el-tag :type="levelTagType[row.warningLevel] || 'info'" size="small" effect="dark">
              {{ levelTextMap[row.warningLevel] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isRead" label="已读" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isRead ? 'success' : 'info'" size="small" effect="plain">
              {{ row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isHandled" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isHandled ? 'success' : 'danger'" size="small" effect="plain">
              {{ row.isHandled ? '已处理' : '待处理' }}
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
            <span v-else class="handled-text">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState v-else icon="Warning" title="暂无预警" description="当前没有需要处理的学业预警" />

      <div class="pagination-wrapper" v-if="total > 0">
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
    </template>

    <!-- Handle dialog -->
    <el-dialog v-model="dialogVisible" title="处理预警" width="480px" :close-on-click-modal="false">
      <div class="handle-info">
        <div class="handle-info-item">
          <span class="handle-info-label">预警标题</span>
          <span class="handle-info-value">{{ currentRow?.title }}</span>
        </div>
        <div class="handle-info-item">
          <span class="handle-info-label">预警类型</span>
          <span class="handle-info-value">{{ currentRow ? typeMap[currentRow.warningType] : '' }}</span>
        </div>
      </div>
      <el-form :model="handleForm" label-width="0" style="margin-top: var(--space-4)">
        <el-form-item>
          <el-input
            v-model="handleForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理备注..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="handleLoading" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getWarnings, handleWarning } from '@/api/academic'
import PageShell from '@/components/page-shell.vue'
import EmptyState from '@/components/empty-state.vue'
import SkeletonTable from '@/components/skeleton-table.vue'

interface WarningRecord {
  id: number; title: string; studentId: number; studentName: string; studentNo: string
  warningType: number; warningLevel: number; isRead: boolean; isHandled: boolean; createTime: string
}

const typeMap: Record<number, string> = { 1: '挂科预警', 2: '绩点预警', 3: '学分预警', 4: '出勤预警' }
const levelTagType: Record<number, string> = { 1: 'warning', 2: 'danger', 3: 'danger' }
const levelTextMap: Record<number, string> = { 1: '黄色', 2: '橙色', 3: '红色' }

const loading = ref(false)
const initialLoading = ref(true)
const tableData = ref<WarningRecord[]>([])
const total = ref(0)
const searchForm = reactive({ keyword: '', page: 1, size: 10 })

function fetchData() {
  loading.value = true
  const params: any = { page: searchForm.page, size: searchForm.size }
  if (searchForm.keyword) params.keyword = searchForm.keyword
  getWarnings(params)
    .then((res: any) => {
      if (res.data) { tableData.value = res.data.records || []; total.value = res.data.total || 0 }
    })
    .finally(() => { loading.value = false; initialLoading.value = false })
}

function handleSearch() { searchForm.page = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; handleSearch() }
function handleSizeChange() { searchForm.page = 1; fetchData() }
function handlePageChange() { fetchData() }

const dialogVisible = ref(false)
const handleLoading = ref(false)
const currentRow = ref<WarningRecord | null>(null)
const handleForm = reactive({ remark: '' })

function openHandleDialog(row: WarningRecord) {
  currentRow.value = row; handleForm.remark = ''; dialogVisible.value = true
}

function submitHandle() {
  if (!currentRow.value) return
  handleLoading.value = true
  handleWarning(currentRow.value.id, { remark: handleForm.remark })
    .then(() => { ElMessage.success('处理成功'); dialogVisible.value = false; fetchData() })
    .finally(() => { handleLoading.value = false })
}

onMounted(fetchData)
</script>

<style scoped>
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--space-4); }
.handled-text { color: var(--color-text-placeholder); font-size: var(--font-size-sm); }
.handle-info { display: flex; flex-direction: column; gap: var(--space-2); padding: var(--space-3); background: var(--color-body-bg); border-radius: var(--radius-md); }
.handle-info-item { display: flex; gap: var(--space-3); }
.handle-info-label { color: var(--color-text-secondary); font-size: var(--font-size-sm); min-width: 60px; }
.handle-info-value { color: var(--color-text-primary); font-size: var(--font-size-sm); }
</style>
