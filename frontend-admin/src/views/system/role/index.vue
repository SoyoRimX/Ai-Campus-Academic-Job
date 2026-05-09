<template>
  <div class="role-page">
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="roleName" label="角色名称" min-width="150" />
        <el-table-column prop="roleCode" label="角色编码" min-width="150" />
        <el-table-column prop="remark" label="备注" min-width="200" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getRoles } from '@/api/system'

const tableData = ref<any[]>([])
const loading = ref(false)

async function fetchData() {
  loading.value = true
  try {
    const res = await getRoles()
    const data = res.data || res
    tableData.value = Array.isArray(data) ? data : (data.records ?? data.list ?? [])
  } catch {
    // roles are typically few; keep table empty on error
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.role-page {
  width: 100%;
}
</style>
