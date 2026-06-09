<template>
  <div class="stat-card" :class="{ 'stat-card--clickable': !!$attrs.onClick }">
    <div class="stat-icon" :style="{ background: iconBg, color: iconColor }">
      <el-icon :size="20"><component :is="icon" /></el-icon>
    </div>
    <div class="stat-body">
      <div class="stat-value" :style="{ color: valueColor }">
        <span v-if="prefix" class="stat-prefix">{{ prefix }}</span>
        <span class="stat-number">{{ displayValue }}</span>
        <span v-if="suffix" class="stat-suffix">{{ suffix }}</span>
      </div>
      <div class="stat-label">{{ label }}</div>
      <div class="stat-trend" v-if="trend !== undefined">
        <el-icon :size="14" :color="trend >= 0 ? 'var(--color-success-500)' : 'var(--color-danger-500)'">
          <component :is="trend >= 0 ? 'Top' : 'Bottom'" />
        </el-icon>
        <span :style="{ color: trend >= 0 ? 'var(--color-success-500)' : 'var(--color-danger-500)' }">
          {{ Math.abs(trend) }}%
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  icon?: string
  label?: string
  value?: string | number
  prefix?: string
  suffix?: string
  valueColor?: string
  iconBg?: string
  iconColor?: string
  trend?: number
}>(), {
  icon: 'DataAnalysis',
  valueColor: 'var(--color-text-primary)',
  iconBg: 'var(--color-primary-50)',
  iconColor: 'var(--color-primary-500)',
})

const displayValue = computed(() => {
  if (props.value === undefined || props.value === null) return '—'
  return props.value
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  transition: box-shadow var(--transition-fast),
              transform var(--transition-fast);
}

.stat-card--clickable {
  cursor: pointer;
}

.stat-card--clickable:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-body {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  line-height: var(--line-height-tight);
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.stat-prefix,
.stat-suffix {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  opacity: 0.7;
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-top: var(--space-1);
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-top: var(--space-2);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
}
</style>
