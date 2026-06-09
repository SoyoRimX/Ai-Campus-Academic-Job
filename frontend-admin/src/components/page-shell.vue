<template>
  <div class="page-shell">
    <div class="page-toolbar" v-if="title || $slots.toolbar">
      <div class="page-toolbar-left">
        <h2 v-if="title" class="page-title">{{ title }}</h2>
        <span v-if="description" class="page-description">{{ description }}</span>
      </div>
      <div class="page-toolbar-right" v-if="$slots.toolbar">
        <slot name="toolbar" />
      </div>
    </div>

    <div class="page-search" v-if="$slots.search">
      <el-card shadow="never" class="search-card">
        <slot name="search" />
      </el-card>
    </div>

    <div class="page-content">
      <el-card shadow="never" class="content-card">
        <slot />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
  description?: string
}>()
</script>

<style scoped>
.page-shell {
  display: flex;
  flex-direction: column;
  gap: 0;
  height: 100%;
}

.page-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: var(--space-4);
  min-height: 0;
}

.page-toolbar:empty {
  display: none;
}

.page-toolbar-left {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  line-height: var(--line-height-tight);
}

.page-description {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
}

.page-toolbar-right {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
}

.search-card {
  margin-bottom: var(--space-4);
}

.search-card :deep(.el-card__body) {
  padding: var(--space-4) var(--space-5);
}

.search-card :deep(.el-form-item) {
  margin-bottom: 0;
}

.content-card {
  flex: 1;
  min-height: 400px;
}

.content-card :deep(.el-card__body) {
  padding: var(--space-5);
}
</style>
