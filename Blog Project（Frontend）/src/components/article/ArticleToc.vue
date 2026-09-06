<script setup>
import { computed } from 'vue'
const props = defineProps({ blocks: { type: Array, required: true } })
const headings = computed(() => props.blocks.filter((block) => ['h2', 'h3'].includes(block.type)))
</script>

<template>
  <nav class="article-toc" aria-label="文章目录">
    <p>ON THIS PAGE</p>
    <a v-for="heading in headings" :key="heading.id" :class="{ 'is-sub': heading.type === 'h3' }" :href="`#${heading.id}`">{{ heading.text }}</a>
  </nav>
</template>

<style scoped>
.article-toc { position: sticky; top: calc(var(--navbar-height) + var(--space-6)); display: grid; align-self: start; padding-left: var(--space-5); border-left: 1px solid var(--color-border); }
.article-toc p { margin: 0 0 var(--space-3); color: var(--color-accent); font-family: var(--font-mono); font-size: var(--font-size-eyebrow); font-weight: 700; letter-spacing: 0.1em; }
.article-toc a { padding: 7px 0; color: var(--color-text-secondary); font-size: 0.78rem; line-height: 1.45; text-decoration: none; }
.article-toc a:hover { color: var(--color-accent); }
.article-toc a.is-sub { padding-left: var(--space-3); font-size: 0.72rem; }
@media (max-width: 1099px) { .article-toc { position: static; order: -1; padding: var(--space-4); background: var(--color-surface-soft); border: 1px solid var(--color-border); border-radius: var(--radius-md); } }
</style>
