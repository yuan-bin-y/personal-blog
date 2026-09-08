<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import MediaFrame from '../media/MediaFrame.vue'
import PostActions from './PostActions.vue'
import OwnerToolbar from '../owner/OwnerToolbar.vue'
import { useOwnerMode } from '../../stores/useOwnerMode'
import { useSpaceRuntime } from '../../stores/useSpaceRuntime'

const props = defineProps({
  post: { type: Object, required: true },
  detail: Boolean,
  presentation: { type: String, default: 'feed' },
})
defineEmits(['edit', 'delete'])
const { isOwner } = useOwnerMode()
const { state } = useSpaceRuntime()
const profile = computed(() => state.profile)

const visualType = computed(() => {
  const imageCount = props.post.images?.length ?? 0
  if (imageCount === 0) return 'text'
  if (imageCount === 1) return 'single'
  return 'gallery'
})

const formatDate = (value) => {
  const date = new Date(value)
  const period = date.getHours() < 6 ? '凌晨' : date.getHours() < 12 ? '上午' : date.getHours() < 18 ? '下午' : '晚上'
  const formatted = new Intl.DateTimeFormat('zh-CN', {
    ...(props.detail ? { year: 'numeric' } : {}),
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
  return props.detail ? `${formatted.split(' ')[0]} · ${period} ${formatted.split(' ')[1]}` : formatted
}
</script>

<template>
  <article
    class="moment-card"
    :class="[
      `moment-card--${visualType}`,
      `moment-card--${presentation}`,
      { 'moment-card--detail': detail },
    ]"
  >
    <OwnerToolbar v-if="isOwner" class="moment-card__owner-tools" label="说说管理" @edit="$emit('edit', post)" @delete="$emit('delete', post)" />
    <header class="moment-card__header">
      <span class="moment-card__avatar" aria-hidden="true"><img v-if="profile.avatar" :src="profile.avatar" alt="" /><template v-else>{{ profile.name }}</template></span>
      <div>
        <strong>{{ profile.name }}</strong>
        <time v-if="detail" :datetime="post.createdAt">{{ formatDate(post.createdAt) }}</time>
        <RouterLink v-else :to="`/moments/${post.id}`">{{ formatDate(post.createdAt) }}</RouterLink>
      </div>
      <span class="moment-card__type">{{ isOwner && post.status !== 'PUBLISHED' ? (post.status === 'SCHEDULED' ? '等待发布' : '草稿') : 'MOMENT' }}</span>
    </header>
    <p class="moment-card__content">{{ post.content }}</p>
    <div v-if="post.images?.length" class="moment-card__media" :class="`moment-card__media--${Math.min(post.images.length, 3)}`">
      <MediaFrame v-for="media in post.images" :key="media.id" :media="media" />
    </div>
    <footer class="moment-card__footer">
      <PostActions :post-id="String(post.id)" :like-count="post.likeCount" :liked-by-me="post.likedByMe" :comment-count="post.commentCount" :comments-to="{ path: `/moments/${post.id}`, hash: '#comments' }" />
      <RouterLink v-if="!detail" class="moment-card__open" :to="`/moments/${post.id}`">查看动态 <span aria-hidden="true">→</span></RouterLink>
    </footer>
  </article>
</template>

<style scoped>
.moment-card {
  position: relative;
  padding: var(--space-6) 0;
  background: transparent;
  border-bottom: 1px solid var(--color-border);
  transition: transform var(--duration-normal) ease-out, border-color var(--duration-normal);
}
.moment-card::before { position: absolute; top: 0; left: 0; width: 42px; height: 3px; background: var(--color-apricot); border-radius: var(--radius-pill); content: ''; }
.moment-card__owner-tools { position: absolute; z-index: 2; top: var(--space-4); right: var(--space-2); opacity: .22; transition: opacity var(--duration-fast); }
.moment-card:hover .moment-card__owner-tools,
.moment-card__owner-tools:focus-within { opacity: 1; }
.moment-card:hover { border-color: var(--color-border-strong); }
.moment-card--single,
.moment-card--gallery {
  padding: var(--space-5);
  background: rgba(252, 251, 249, 0.76);
  border: 1px solid rgba(217, 222, 226, 0.9);
  border-radius: var(--radius-md);
}
.moment-card--gallery { background: transparent; border-color: transparent; border-bottom-color: var(--color-border); border-radius: 0; }
.moment-card--single:hover,
.moment-card--gallery:hover { transform: translateY(-2px); }
.moment-card__header { display: grid; grid-template-columns: 48px 1fr auto; align-items: center; gap: var(--space-3); }
.moment-card__avatar { display: grid; width: 48px; height: 48px; place-items: center; background: linear-gradient(145deg, var(--color-accent-soft), #d8cbd4); border: 3px solid var(--color-canvas); border-radius: 50%; font-family: var(--font-serif); font-weight: 700; }
.moment-card__avatar { overflow: hidden; }
.moment-card__avatar img { width: 100%; height: 100%; object-fit: cover; }
.moment-card__header div { display: grid; line-height: 1.4; }
.moment-card__header strong { font-family: var(--font-serif); }
.moment-card__header a,
.moment-card__header time { color: var(--color-text-secondary); font-size: 0.8125rem; text-decoration: none; }
.moment-card__type { color: var(--color-apricot); font-family: var(--font-mono); font-size: 0.6875rem; font-weight: 700; letter-spacing: 0.1em; }
.moment-card__content { margin: var(--space-5) 0 0; font-size: 1.02rem; line-height: 1.85; white-space: pre-line; }
.moment-card__media { display: grid; gap: var(--space-2); margin-top: var(--space-5); }
.moment-card__media--1 { grid-template-columns: minmax(0, 620px); }
.moment-card__media--2 { grid-template-columns: repeat(2, minmax(0, 1fr)); }
.moment-card__media--3 { grid-template-columns: 1.25fr 1fr 1fr; }
.moment-card__media--3 :deep(.media-frame) { aspect-ratio: 1 / 1 !important; }
.moment-card__footer { display: flex; align-items: center; justify-content: space-between; gap: var(--space-3); margin-top: var(--space-4); padding-top: var(--space-2); border-top: 1px solid rgba(217, 222, 226, 0.78); }
.moment-card__open { display: inline-flex; min-height: 44px; align-items: center; gap: var(--space-2); color: var(--color-text-secondary); font-size: var(--font-size-meta); font-weight: 650; text-decoration: none; }
.moment-card__open:hover { color: var(--color-accent); }
.moment-card--text .moment-card__content { max-width: 66ch; font-size: 1.06rem; }
.moment-card--timeline.moment-card--text { padding: var(--space-5) var(--space-3) var(--space-6); }
.moment-card--timeline.moment-card--single { margin-right: clamp(0px, 8vw, 72px); }
.moment-card--timeline.moment-card--gallery { padding-inline: var(--space-3); }
.moment-card--detail {
  padding: 0;
  background: transparent;
  border: 0;
  border-radius: 0;
  box-shadow: none;
}
.moment-card--detail::before { display: none; }
.moment-card--detail:hover { transform: none; }
.moment-card--detail .moment-card__header { grid-template-columns: 56px 1fr; }
.moment-card--detail .moment-card__avatar { width: 56px; height: 56px; }
.moment-card--detail .moment-card__type { display: none; }
.moment-card--detail .moment-card__content { max-width: 720px; margin-top: var(--space-8); font-family: var(--font-serif); font-size: 1.2rem; line-height: 1.95; }
.moment-card--detail .moment-card__media { max-width: 720px; margin-top: var(--space-6); }
.moment-card--detail .moment-card__footer { max-width: 720px; margin-top: var(--space-5); padding-top: var(--space-4); }

@media (max-width: 600px) {
  .moment-card__owner-tools { position: static; justify-self: end; margin: 0 0 var(--space-2) auto; opacity: 1; }
  .moment-card { padding: var(--space-5) 0; }
  .moment-card--single,
  .moment-card--gallery { padding: var(--space-4); }
  .moment-card--gallery { padding-inline: 0; }
  .moment-card__header { grid-template-columns: 44px 1fr; }
  .moment-card__avatar { width: 44px; height: 44px; }
  .moment-card__type { grid-column: 2; }
  .moment-card__media--3 { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .moment-card__footer { align-items: center; flex-direction: row; }
  .moment-card__open { min-height: 40px; font-size: 0.8125rem; }
  .moment-card--detail { padding: 0; }
  .moment-card--detail .moment-card__header { grid-template-columns: 48px 1fr; }
  .moment-card--detail .moment-card__avatar { width: 48px; height: 48px; }
  .moment-card--detail .moment-card__content { margin-top: var(--space-6); font-size: 1.0625rem; line-height: 1.9; }
}
</style>
