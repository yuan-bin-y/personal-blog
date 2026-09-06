<script setup>
defineProps({ label: { type: String, default: '内容管理' } })
defineEmits(['edit', 'delete'])
</script>

<template>
  <div class="owner-toolbar" :aria-label="label">
    <button type="button" @click="$emit('edit')">编辑</button>
    <button class="owner-toolbar__danger" type="button" @click="$emit('delete')">删除</button>
  </div>
</template>

<style scoped>
.owner-toolbar { display: inline-flex; align-items: center; gap: 2px; padding: 3px; background: rgba(252, 251, 249, .88); border: 1px solid var(--color-border); border-radius: var(--radius-pill); box-shadow: var(--shadow-1); }
.owner-toolbar button { min-height: 34px; padding: 0 var(--space-3); color: var(--color-text-secondary); background: transparent; border: 0; border-radius: var(--radius-pill); cursor: pointer; font-size: .75rem; font-weight: 700; }
.owner-toolbar button:hover { color: var(--color-accent); background: var(--color-accent-soft); }
.owner-toolbar .owner-toolbar__danger:hover { color: var(--color-error); background: rgba(177, 74, 66, .09); }
</style>

