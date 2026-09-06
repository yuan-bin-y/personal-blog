<script setup>
import BackendPending from '../layout/BackendPending.vue'
</script>

<template>
  <aside class="space-widgets" aria-label="空间小组件">
    <header><span aria-hidden="true">✦</span><p>SPACE CORNER</p></header>
    <BackendPending compact description="状态、标签与归档需要后端数据支持。" />
  </aside>
</template>

<style scoped>
.space-widgets { position: sticky; top: calc(var(--navbar-height) + var(--space-6)); align-self: start; padding: var(--space-6); background: linear-gradient(155deg, rgba(232, 237, 241, 0.78), rgba(252, 251, 249, 0.66)); border-top: 3px solid var(--color-apricot); border-bottom: 1px solid var(--color-border); }
.space-widgets > header { display: flex; align-items: center; gap: var(--space-2); color: var(--color-accent); }
.space-widgets > header span { font-size: 0.75rem; }
.space-widgets > header p,
.space-widget > p { margin: 0; font-family: var(--font-mono); font-size: var(--font-size-eyebrow); font-weight: 700; letter-spacing: 0.12em; }
.space-widgets :deep(.backend-pending) { margin-top: var(--space-4); }
@media (max-width: 959px) { .space-widgets { position: static; margin-top: var(--space-8); } }
@media (max-width: 600px) {
  .space-widgets { padding: var(--space-5); }
}
</style>
