<template>
  <div class="skeleton-table">
    <div class="skeleton-header">
      <div
        class="skeleton-line"
        v-for="col in columns"
        :key="col"
        :style="{ width: col + 'px' }"
      />
    </div>
    <div class="skeleton-row" v-for="row in rows" :key="row">
      <div
        class="skeleton-line"
        v-for="col in columns"
        :key="col"
        :style="{ width: (col * (0.7 + Math.random() * 0.3)) + 'px' }"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  rows?: number
  columns?: number[]
}>(), {
  rows: 8,
  columns: () => [80, 120, 160, 100, 120, 100, 140]
})
</script>

<style scoped>
.skeleton-table {
  padding: 0;
}

.skeleton-header {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-3) var(--space-4);
  background: #f8fafc;
  border-radius: var(--radius-md) var(--radius-md) 0 0;
  border-bottom: 1px solid var(--color-border-light);
}

.skeleton-row {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-border-light);
}

.skeleton-row:last-child {
  border-bottom: none;
}
</style>
