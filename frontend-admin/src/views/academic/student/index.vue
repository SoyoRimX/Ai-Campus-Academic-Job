<template>
  <PageShell title="学生管理" description="查看和管理在校学生信息">
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
        <el-table-column prop="studentName" label="姓名" width="90" />
        <el-table-column prop="studentNo" label="学号" min-width="120" />
        <el-table-column prop="major" label="专业" min-width="160" show-overflow-tooltip />
        <el-table-column prop="grade" label="年级" min-width="100" />
        <el-table-column prop="gpa" label="GPA" width="80" />
        <el-table-column prop="totalCredits" label="已获学分" width="100" />
        <el-table-column prop="failCount" label="不及格科目" width="110">
          <template #default="{ row }">
            <span :style="{ color: row.failCount > 0 ? 'var(--color-danger-500)' : 'var(--color-text-secondary)' }">
              {{ row.failCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="advisor" label="辅导员" min-width="100" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="$router.push(`/academic/student/${row.id}`)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState v-else icon="User" title="暂无学生数据" description="当前系统中还没有学生记录" />

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
  </PageShell>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getStudents } from '@/api/academic'
import PageShell from '@/components/page-shell.vue'
import EmptyState from '@/components/empty-state.vue'
import SkeletonTable from '@/components/skeleton-table.vue'

const loading = ref(false)
const initialLoading = ref(true)
const tableData = ref<any[]>([])
const total = ref(0)

const searchForm = reactive({ keyword: '', page: 1, size: 10 })

function fetchData() {
  loading.value = true
  getStudents({ page: searchForm.page, size: searchForm.size, keyword: searchForm.keyword || undefined })
    .then((res: any) => {
      if (res.data) {
        tableData.value = res.data.records || []
        total.value = res.data.total || 0
      }
    })
    .finally(() => {
      loading.value = false
      initialLoading.value = false
    })
}

function handleSearch() { searchForm.page = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; handleSearch() }
function handleSizeChange() { searchForm.page = 1; fetchData() }
function handlePageChange() { fetchData() }

onMounted(fetchData)
</script>

<style scoped>
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--space-4); }
</style>
