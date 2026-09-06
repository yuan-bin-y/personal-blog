<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import BackendPending from '../layout/BackendPending.vue'
import { useSpaceRuntime } from '../../stores/useSpaceRuntime'

const { state } = useSpaceRuntime()
const entries = computed(() => state.guestbook.slice(0, 3))
const formatDate = (value) => new Intl.DateTimeFormat('zh-CN', { month: 'short', day: 'numeric' }).format(new Date(value))
</script>

<template>
  <section class="guestbook-preview container" aria-labelledby="guestbook-preview-title">
    <header>
      <div><p>GUESTBOOK</p><h2 id="guestbook-preview-title">路过的人，留下了一些话</h2></div>
      <RouterLink to="/guestbook">去留言板 <span aria-hidden="true">→</span></RouterLink>
    </header>
    <ol v-if="entries.length" class="guestbook-preview__list">
      <li v-for="entry in entries" :key="entry.id">
        <div><strong>{{ entry.author?.name || '访客' }}</strong><time :datetime="entry.createdAt">{{ formatDate(entry.createdAt) }}</time></div>
        <p>{{ entry.content }}</p>
      </li>
    </ol>
    <BackendPending v-else compact title="暂无留言" description="数据库中暂无留言。Visitor 留言功能将在后续后端版本开放。" />
  </section>
</template>

<style scoped>
.guestbook-preview { margin-top: var(--space-24); padding: var(--space-10) 0; border-top: 1px solid var(--color-border); border-bottom: 1px solid var(--color-border); }
.guestbook-preview > header { display: flex; align-items: end; justify-content: space-between; gap: var(--space-5); }
.guestbook-preview header p { margin: 0 0 var(--space-2); color: var(--color-accent); font-family: var(--font-mono); font-size: var(--font-size-eyebrow); font-weight: 700; letter-spacing: 0.12em; }
.guestbook-preview h2 { margin: 0; font-family: var(--font-serif); font-size: var(--font-size-h2); line-height: 1.25; }
.guestbook-preview header a { display: inline-flex; align-items: center; min-height: 44px; gap: var(--space-2); color: var(--color-accent); font-size: var(--font-size-meta); font-weight: 700; text-decoration: none; white-space: nowrap; }
.guestbook-preview :deep(.backend-pending) { margin-top: var(--space-6); }
.guestbook-preview__list { display: grid; margin: var(--space-6) 0 0; padding: 0; list-style: none; }
.guestbook-preview__list li { padding: var(--space-4) 0; border-top: 1px dashed var(--color-border-strong); }
.guestbook-preview__list li > div { display: flex; justify-content: space-between; gap: var(--space-4); }
.guestbook-preview__list strong { font-family: var(--font-serif); }
.guestbook-preview__list time { color: var(--color-text-secondary); font-size: var(--font-size-meta); }
.guestbook-preview__list p { margin: var(--space-2) 0 0; color: var(--color-text-secondary); line-height: 1.7; }
@media (max-width: 600px) {
  .guestbook-preview { margin-top: var(--space-16); padding: var(--space-8) 0; }
  .guestbook-preview > header { align-items: flex-start; flex-direction: column; }
  .guestbook-preview h2 { font-size: 1.5rem; }
  .guestbook-preview header a { min-height: 40px; }
}
</style>
