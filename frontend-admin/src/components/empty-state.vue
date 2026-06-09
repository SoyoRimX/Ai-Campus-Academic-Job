<template>
  <div class="empty-state">
    <div class="empty-icon">
      <el-icon :size="48"><component :is="icon" /></el-icon>
    </div>
    <h4 class="empty-title">{{ title }}</h4>
    <p class="empty-description" v-if="description">{{ description }}</p>
    <div class="empty-action" v-if="$slots.default">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  icon?: string
  title?: string
  description?: string
}>()
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) var(--space-6);
  text-align: center;
}

.empty-icon {
  color: var(--color-text-placeholder);
  margin-bottom: var(--space-4);
  opacity: 0.6;
}

.empty-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin-bottom: var(--space-2);
}

.empty-description {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  max-width: 360px;
  line-height: var(--line-height-relaxed);
}

.empty-action {
  margin-top: var(--space-5);
}
</style>
