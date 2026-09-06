<script setup>
import { computed } from 'vue'
import BackendPending from '../layout/BackendPending.vue'
import { RouterLink } from 'vue-router'
import { useOwnerMode } from '../../stores/useOwnerMode'
import { useSpaceRuntime } from '../../stores/useSpaceRuntime'

defineProps({ compact: Boolean })
const { isOwner } = useOwnerMode()
const { state } = useSpaceRuntime()
const profile = computed(() => state.profile)
const musicMedia = computed(() => state.music)
</script>

<template>
  <div class="left-space" :class="{ 'left-space--compact': compact }">
    <section id="profile" class="owner-card" aria-labelledby="space-owner-name">
      <div class="owner-card__avatar">
        <img v-if="profile.avatar" :src="profile.avatar" alt="" />
        <span v-else aria-hidden="true">{{ profile.name }}</span>
      </div>
      <div class="owner-card__copy">
        <p>SPACE OWNER</p>
        <h2 id="space-owner-name">{{ profile.name }}</h2>
        <strong>{{ profile.role }}</strong>
        <blockquote>“{{ profile.bio }}”</blockquote>
        <RouterLink v-if="isOwner" class="space-piece__edit" to="/settings#profile">编辑资料</RouterLink>
      </div>
    </section>

    <section class="space-piece space-piece--announcement" aria-labelledby="announcement-title">
      <h2 id="announcement-title"><span aria-hidden="true"></span>空间公告</h2>
      <p v-if="state.announcement?.enabled && state.announcement?.content" class="space-piece__content">{{ state.announcement.content }}</p>
      <BackendPending v-else compact title="公告未启用" description="主人暂时没有开启空间公告。" />
      <RouterLink v-if="isOwner" class="space-piece__edit" to="/settings#announcement">编辑公告</RouterLink>
    </section>

    <section class="space-piece space-piece--music" aria-labelledby="music-title">
      <h2 id="music-title"><span aria-hidden="true"></span>空间音乐</h2>
      <div v-if="musicMedia.src || musicMedia.audioUrl" class="music-player">
        <p>{{ musicMedia.title }}</p>
        <span>{{ musicMedia.artist }}</span>
        <audio :src="musicMedia.src || musicMedia.audioUrl" controls preload="none"></audio>
      </div>
      <BackendPending v-else compact title="音乐未配置" description="主人暂时没有配置空间音乐。" />
      <RouterLink v-if="isOwner" class="space-piece__edit" to="/settings#music">编辑音乐</RouterLink>
    </section>
  </div>
</template>

<style scoped>
.left-space { display: grid; gap: var(--space-4); }
.owner-card,
.space-piece {
  background: rgb(252 251 249 / var(--space-surface-opacity, 0.95));
  backdrop-filter: blur(16px);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-1);
}

.owner-card {
  padding: var(--space-5);
  border-radius: var(--radius-xl);
  text-align: center;
}

.owner-card__avatar {
  display: grid;
  width: 104px;
  height: 104px;
  margin: -52px auto var(--space-4);
  place-items: center;
  overflow: hidden;
  background: linear-gradient(145deg, var(--color-accent-soft), #d8cbd4);
  border: 5px solid var(--color-surface);
  border-radius: 34% 66% 52% 48% / 48% 42% 58% 52%;
  box-shadow: var(--shadow-2);
  font-family: var(--font-serif);
  font-size: 2.25rem;
  font-weight: 700;
}

.owner-card__avatar img { width: 100%; height: 100%; object-fit: cover; }
.owner-card__copy > p { margin: 0; color: var(--color-accent); font-family: var(--font-mono); font-size: 0.6875rem; font-weight: 700; letter-spacing: 0.11em; }
.owner-card h2 { margin: var(--space-1) 0 0; font-family: var(--font-serif); font-size: 1.65rem; line-height: 1.2; }
.owner-card strong { display: block; margin-top: var(--space-2); color: var(--color-text-secondary); font-size: 0.8125rem; font-weight: 600; }
.owner-card blockquote { margin: var(--space-4) 0 0; padding-top: var(--space-3); color: var(--color-text-secondary); border-top: 1px dashed var(--color-border-strong); font-family: var(--font-serif); font-size: 0.9375rem; line-height: 1.7; }

.space-piece { padding: var(--space-4); border-radius: var(--radius-lg); }
.space-piece h2 { display: flex; align-items: center; gap: var(--space-2); margin: 0; font-size: 0.9375rem; }
.space-piece h2 span { width: 3px; height: 15px; background: var(--color-apricot); border-radius: var(--radius-pill); }
.space-piece :deep(.backend-pending) { border-bottom: 0; }
.space-piece :deep(.backend-pending p) { font-size: 0.8125rem; }
.music-player p { margin: var(--space-3) 0 0; font-weight: 700; }
.music-player > span { color: var(--color-text-secondary); font-size: 0.8125rem; }
.music-player audio { width: 100%; margin-top: var(--space-3); }
.space-piece__content { margin: var(--space-3) 0 0; color: var(--color-text-secondary); font-size: .8125rem; line-height: 1.7; }
.space-piece__edit { display: inline-flex; min-height: 36px; align-items: center; margin-top: var(--space-3); color: var(--color-accent); font-size: .75rem; font-weight: 700; text-decoration: none; }
.owner-card .space-piece__edit { justify-content: center; }

.left-space--compact .owner-card__avatar { width: 82px; height: 82px; margin-top: -36px; font-size: 1.75rem; }

@media (max-width: 767px) {
  .left-space { gap: 0; }
  .owner-card { display: grid; grid-template-columns: 72px minmax(0, 1fr); align-items: center; gap: var(--space-4); padding: var(--space-4); text-align: left; }
  .owner-card__avatar,
  .left-space--compact .owner-card__avatar { width: 72px; height: 72px; margin: 0; border-width: 3px; border-radius: 50%; font-size: 1.5rem; }
  .owner-card__copy > p { display: none; }
  .owner-card h2 { font-size: 1.35rem; }
  .owner-card strong { margin-top: 2px; }
  .owner-card blockquote { grid-column: 1 / -1; margin-top: var(--space-3); padding-top: var(--space-3); }
  .space-piece--announcement,
  .space-piece--music { display: none; }
}
</style>
