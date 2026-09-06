<script setup>
defineProps({ modelValue: { type: String, required: true } })
defineEmits(['update:modelValue'])
const filters = [{ id: 'ALL', label: '全部' }, { id: 'MOMENT', label: '说说' }, { id: 'TECH', label: '技术' }]
</script>

<template>
  <div class="feed-filter" role="group" aria-label="动态类型筛选">
    <button
      v-for="filter in filters"
      :key="filter.id"
      type="button"
      :class="{ 'is-active': modelValue === filter.id }"
      :aria-pressed="modelValue === filter.id"
      @click="$emit('update:modelValue', filter.id)"
    >{{ filter.label }}</button>
  </div>
</template>

<style scoped>
.feed-filter { display: flex; gap: var(--space-2); padding: var(--space-1); background: var(--color-surface-soft); border: 1px solid var(--color-border); border-radius: var(--radius-pill); }
.feed-filter button { min-width: 72px; min-height: 40px; padding: 0 var(--space-4); color: var(--color-text-secondary); background: transparent; border: 0; border-radius: var(--radius-pill); cursor: pointer; font-size: var(--font-size-meta); font-weight: 650; transition: color var(--duration-fast), background-color var(--duration-fast), box-shadow var(--duration-fast); }
.feed-filter button.is-active { color: var(--color-text-inverse); background: var(--color-accent); box-shadow: 0 5px 14px rgba(77, 111, 141, 0.2); }
@media (max-width: 479px) { .feed-filter { width: 100%; } .feed-filter button { min-width: 0; flex: 1; padding-inline: var(--space-2); } }
</style>
