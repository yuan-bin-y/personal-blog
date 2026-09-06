<script setup>
defineProps({
  eyebrow: { type: String, required: true },
  title: { type: String, required: true },
  description: { type: String, required: true },
})
</script>

<template>
  <header class="page-header">
    <div class="page-header__inner">
      <p>{{ eyebrow }}</p><h1>{{ title }}</h1><span>{{ description }}</span>
    </div>
  </header>
</template>

<style scoped>
.page-header { padding: var(--space-6) 0 var(--space-8); border-bottom: 1px solid var(--color-border); }
.page-header__inner { max-width: 960px; }
.page-header p { margin: 0 0 var(--space-3); color: var(--color-accent); font-family: var(--font-mono); font-size: var(--font-size-eyebrow); font-weight: 700; letter-spacing: 0.13em; }
.page-header h1 { margin: 0; font-family: var(--font-serif); font-size: var(--font-size-h1); line-height: 1.15; letter-spacing: -0.025em; }
.page-header span { display: block; max-width: 680px; margin-top: var(--space-4); color: var(--color-text-secondary); line-height: 1.8; }
@media (max-width: 767px) { .page-header { padding: var(--space-4) 0 var(--space-6); } }
</style>
