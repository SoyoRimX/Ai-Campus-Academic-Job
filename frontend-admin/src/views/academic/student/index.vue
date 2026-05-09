<template>
  <div class="student-page">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="学生姓名">
          <el-input
            v-model="searchForm.keyword"
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
        <el-table-column prop="studentNo" label="学号" min-width="120" />
        <el-table-column prop="major" label="专业" min-width="160" />
        <el-table-column prop="grade" label="年级" min-width="100" />
        <el-table-column prop="gpa" label="GPA" width="80" />
        <el-table-column prop="totalCredits" label="已获学分" width="100" />
        <el-table-column prop="failCount" label="不及格科目数" width="120" />
        <el-table-column prop="advisor" label="辅导员" min-width="100" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="$router.push(`/academic/student/${row.id}`)">查看</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getStudents } from '@/api/academic'

interface StudentRecord {
  studentNo: string
  name: string
  major: string
  grade: string
  gpa: number
  earnedCredits: number
  failedCount: number
  advisor: string
}

const loading = ref(false)
const tableData = ref<StudentRecord[]>([])
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  page: 1,
  size: 10
})

function fetchData() {
  loading.value = true
  getStudents({ page: searchForm.page, size: searchForm.size })
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
  searchForm.keyword = ''
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

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.student-page {
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
