<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'

const props = defineProps({
  likeCount: { type: Number, default: 0 },
  commentCount: { type: Number, default: 0 },
  commentsTo: { type: [String, Object], required: true },
})

const liked = ref(false)
const displayLikes = () => props.likeCount + (liked.value ? 1 : 0)
</script>

<template>
  <div class="post-actions">
    <button
      class="post-actions__button"
      :class="{ 'post-actions__button--liked': liked }"
      type="button"
      :aria-pressed="liked"
      :aria-label="liked ? `取消点赞，当前 ${displayLikes()} 个赞` : `点赞，当前 ${displayLikes()} 个赞`"
      @click="liked = !liked"
    >
      <span aria-hidden="true">{{ liked ? '♥' : '♡' }}</span>{{ displayLikes() }}
    </button>
    <RouterLink class="post-actions__button" :to="commentsTo" :aria-label="`查看 ${commentCount} 条评论`">
      <span aria-hidden="true">↳</span>评论 {{ commentCount }}
    </RouterLink>
  </div>
</template>

<style scoped>
.post-actions { display: flex; align-items: center; gap: var(--space-2); flex-wrap: wrap; }
.post-actions__button {
  display: inline-flex;
  align-items: center;
  min-height: 44px;
  gap: 6px;
  padding: 0 var(--space-3);
  color: var(--color-text-secondary);
  background: transparent;
  border: 0;
  border-radius: var(--radius-pill);
  cursor: pointer;
  font-size: var(--font-size-meta);
  font-weight: 550;
  text-decoration: none;
  transition: color var(--duration-fast), background-color var(--duration-fast), transform var(--duration-fast);
}
.post-actions__button:hover { color: var(--color-accent); background: var(--color-accent-soft); transform: translateY(-1px); }
.post-actions__button--liked { color: var(--color-accent); }
.post-actions__button span { font-size: 1.05rem; }
</style>
