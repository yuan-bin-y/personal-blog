<script setup>
import { RouterLink } from 'vue-router'
import MediaFrame from '../media/MediaFrame.vue'
import PostActions from './PostActions.vue'
import OwnerToolbar from '../owner/OwnerToolbar.vue'
import { useOwnerMode } from '../../stores/useOwnerMode'

defineProps({
  post: { type: Object, required: true },
  compact: Boolean,
  variant: { type: String, default: 'card' },
})
const formatDate = (value) => new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(value))
defineEmits(['edit', 'delete'])
const { isOwner } = useOwnerMode()
</script>

<template>
  <article class="tech-card" :class="[`tech-card--${variant}`, { 'tech-card--compact': compact }]">
    <OwnerToolbar v-if="isOwner" class="tech-card__owner-tools" label="文章管理" @edit="$emit('edit', post)" @delete="$emit('delete', post)" />
    <div class="tech-card__body">
      <div class="tech-card__kicker"><span>TECH</span><i></i>{{ post.category }}</div>
      <h3><RouterLink :to="`/tech/${post.slug}`">{{ post.title }}</RouterLink></h3>
      <p class="tech-card__summary">{{ post.summary }}</p>
      <div class="tech-card__tags" aria-label="文章标签">
        <span v-for="tag in post.tags" :key="tag">#{{ tag }}</span>
      </div>
      <div class="tech-card__meta">
        <span>{{ post.readingTime }} min read</span><span>{{ formatDate(post.createdAt) }}</span>
      </div>
      <div class="tech-card__footer">
        <RouterLink class="tech-card__read" :to="`/tech/${post.slug}`">阅读全文 <span aria-hidden="true">→</span></RouterLink>
        <PostActions :like-count="post.likeCount" :comment-count="post.commentCount" :comments-to="{ path: `/tech/${post.slug}`, hash: '#comments' }" />
      </div>
    </div>
    <MediaFrame v-if="post.images?.[0]" class="tech-card__cover" :media="post.images[0]" />
  </article>
</template>

<style scoped>
.tech-card {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 7fr) minmax(220px, 5fr);
  gap: var(--space-6);
  padding: var(--space-6);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-1);
  transition: transform var(--duration-normal) ease-out, border-color var(--duration-normal), box-shadow var(--duration-normal);
}
.tech-card__owner-tools { position: absolute; z-index: 2; top: var(--space-3); right: var(--space-3); opacity: .22; transition: opacity var(--duration-fast); }
.tech-card:hover .tech-card__owner-tools,
.tech-card__owner-tools:focus-within { opacity: 1; }
.tech-card:hover { transform: translateY(-2px); border-color: var(--color-border-strong); box-shadow: 0 12px 30px rgba(37, 45, 54, 0.09); }
.tech-card__body { min-width: 0; }
.tech-card__kicker { display: flex; align-items: center; gap: var(--space-2); color: var(--color-text-secondary); font-size: var(--font-size-eyebrow); font-weight: 700; letter-spacing: 0.1em; }
.tech-card__kicker span { color: var(--color-accent); }
.tech-card__kicker i { width: 18px; height: 1px; background: var(--color-border-strong); }
.tech-card h3 { margin: var(--space-3) 0 0; font-family: var(--font-serif); font-size: var(--font-size-h3); line-height: 1.35; }
.tech-card h3 a { text-decoration: none; }
.tech-card h3 a:hover { color: var(--color-accent); }
.tech-card__summary { display: -webkit-box; margin: var(--space-3) 0 0; overflow: hidden; color: var(--color-text-secondary); line-height: 1.75; -webkit-box-orient: vertical; -webkit-line-clamp: 3; }
.tech-card__tags { display: flex; gap: var(--space-2); margin-top: var(--space-3); flex-wrap: wrap; color: var(--color-accent); font-size: 0.75rem; }
.tech-card__meta { display: flex; gap: var(--space-4); margin-top: var(--space-3); color: var(--color-text-secondary); font-family: var(--font-mono); font-size: 0.6875rem; }
.tech-card__footer { display: flex; align-items: center; justify-content: space-between; gap: var(--space-3); margin-top: var(--space-4); }
.tech-card__read { display: inline-flex; min-height: 44px; align-items: center; gap: var(--space-2); color: var(--color-accent); font-size: var(--font-size-meta); font-weight: 700; text-decoration: none; }
.tech-card__cover { align-self: stretch; min-height: 100%; }
.tech-card--compact { grid-template-columns: 1fr; }
.tech-card--compact .tech-card__cover { display: none; }
.tech-card--featured { grid-template-columns: minmax(0, 5fr) minmax(320px, 7fr); padding: var(--space-8); background: linear-gradient(145deg, var(--color-surface-soft), var(--color-surface) 62%); border-color: var(--color-border-strong); box-shadow: none; }
.tech-card--featured h3 { max-width: 16ch; font-size: clamp(1.75rem, 3vw, 2.5rem); line-height: 1.22; }
.tech-card--featured .tech-card__summary { font-size: 1.02rem; }
.tech-card--featured .tech-card__cover { min-height: 340px; }
.tech-card--row,
.tech-card--editorial { padding: var(--space-6) var(--space-2); background: transparent; border: 0; border-bottom: 1px solid var(--color-border); border-radius: 0; box-shadow: none; }
.tech-card--row { grid-template-columns: minmax(0, 8fr) minmax(180px, 4fr); }
.tech-card--row .tech-card__cover { max-height: 220px; }
.tech-card--editorial { grid-template-columns: minmax(0, 1fr); }
.tech-card--editorial .tech-card__body { display: grid; grid-template-columns: minmax(0, 1fr) auto; column-gap: var(--space-8); }
.tech-card--editorial .tech-card__kicker,
.tech-card--editorial h3,
.tech-card--editorial .tech-card__summary,
.tech-card--editorial .tech-card__tags { grid-column: 1; }
.tech-card--editorial .tech-card__meta,
.tech-card--editorial .tech-card__footer { grid-column: 2; }
.tech-card--editorial .tech-card__meta { grid-row: 1 / span 2; align-self: start; justify-content: flex-end; }
.tech-card--editorial .tech-card__footer { grid-row: 3 / span 2; align-self: end; flex-direction: column; align-items: flex-end; }
.tech-card--editorial .tech-card__cover { display: none; }
.tech-card--row:hover,
.tech-card--editorial:hover { box-shadow: none; }

@media (max-width: 767px) {
  .tech-card__owner-tools { position: static; grid-column: 1 / -1; justify-self: end; margin-bottom: calc(var(--space-2) * -1); opacity: 1; }
  .tech-card { grid-template-columns: 1fr; padding: var(--space-5); }
  .tech-card__cover { grid-row: 1; min-height: 0; }
  .tech-card__footer { align-items: flex-start; flex-direction: column; }
  .tech-card--featured { padding: var(--space-5); }
  .tech-card--featured .tech-card__cover { min-height: 0; }
  .tech-card--featured h3 { max-width: none; font-size: 1.75rem; }
  .tech-card--row,
  .tech-card--editorial { padding: var(--space-5) 0; }
  .tech-card--row .tech-card__cover { display: none; }
  .tech-card--editorial .tech-card__body { display: block; }
  .tech-card--editorial .tech-card__footer { align-items: flex-start; }
  .tech-card__meta { gap: var(--space-3); font-size: 0.75rem; flex-wrap: wrap; }
  .tech-card__tags { font-size: 0.8125rem; }
  .tech-card__footer { gap: var(--space-2); margin-top: var(--space-3); }
}
</style>
