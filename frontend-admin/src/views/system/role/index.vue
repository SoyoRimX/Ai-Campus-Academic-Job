<template>
  <PageShell title="角色管理" description="查看系统角色和权限配置">
    <SkeletonTable v-if="initialLoading" />
    <template v-else>
      <el-table v-if="tableData.length" :data="tableData" stripe v-loading="loading">
        <el-table-column prop="roleName" label="角色名称" min-width="150" />
        <el-table-column prop="roleCode" label="角色编码" min-width="150" />
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>

      <EmptyState v-else icon="Avatar" title="暂无角色数据" description="系统中还没有配置角色" />
    </template>
  </PageShell>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getRoles } from '@/api/system'
import PageShell from '@/components/page-shell.vue'
import EmptyState from '@/components/empty-state.vue'
import SkeletonTable from '@/components/skeleton-table.vue'

const tableData = ref<any[]>([])
const loading = ref(false)
const initialLoading = ref(true)

async function fetchData() {
  loading.value = true
  try {
    const res = await getRoles()
    const data = res.data || res
    tableData.value = Array.isArray(data) ? data : (data.records ?? data.list ?? [])
  } finally { loading.value = false; initialLoading.value = false }
}

onMounted(fetchData)
</script>
