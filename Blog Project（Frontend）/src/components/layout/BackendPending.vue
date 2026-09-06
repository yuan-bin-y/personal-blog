<script setup>
defineProps({
  title: { type: String, default: '等待后端开发' },
  description: { type: String, default: '这里的内容需要从后端数据库读取，当前不使用前端伪造数据。' },
  compact: Boolean,
})
</script>

<template>
  <section class="backend-pending" :class="{ 'backend-pending--compact': compact }" role="status">
    <span aria-hidden="true">…</span>
    <div><h2>{{ title }}</h2><p>{{ description }}</p></div>
  </section>
</template>

<style scoped>
.backend-pending { display: grid; grid-template-columns: 48px minmax(0, 1fr); align-items: start; gap: var(--space-4); padding: var(--space-8) 0; border-top: 1px solid var(--color-border); border-bottom: 1px solid var(--color-border); }
.backend-pending > span { display: grid; width: 48px; height: 48px; place-items: center; color: var(--color-accent); background: var(--color-surface-soft); border-radius: 50%; font-family: var(--font-serif); font-size: 1.5rem; }
.backend-pending h2 { margin: 0; font-family: var(--font-serif); font-size: var(--font-size-h3); }
.backend-pending p { max-width: 620px; margin: var(--space-2) 0 0; color: var(--color-text-secondary); line-height: 1.75; }
.backend-pending--compact { grid-template-columns: 36px minmax(0, 1fr); padding: var(--space-5) 0; }
.backend-pending--compact > span { width: 36px; height: 36px; font-size: 1rem; }
.backend-pending--compact h2 { font-size: 1rem; }
.backend-pending--compact p { font-size: var(--font-size-meta); }
@media (max-width: 600px) { .backend-pending { grid-template-columns: 40px minmax(0, 1fr); padding: var(--space-6) 0; }.backend-pending > span { width: 40px; height: 40px; } }
</style>
