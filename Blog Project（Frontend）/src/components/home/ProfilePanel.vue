<script setup>
import ProfileStats from './ProfileStats.vue'
import { RouterLink } from 'vue-router'
import { useOwnerMode } from '../../stores/useOwnerMode'

defineProps({
  profile: { type: Object, required: true },
})
const { isOwner } = useOwnerMode()
</script>

<template>
  <article id="profile" class="profile-panel container">
    <div class="profile-panel__identity">
      <div class="profile-panel__avatar-wrap">
        <img
          v-if="profile.avatar"
          class="profile-panel__avatar"
          :src="profile.avatar"
          :alt="`${profile.name}的头像`"
        />
        <span v-else class="profile-panel__avatar profile-panel__avatar--placeholder" aria-hidden="true">
          {{ profile.name }}
        </span>
        <span class="profile-panel__status" aria-label="最近有更新" title="最近有更新"></span>
      </div>
      <RouterLink v-if="isOwner" class="profile-panel__edit" to="/settings#profile">编辑资料</RouterLink>

      <div class="profile-panel__copy">
        <p class="profile-panel__eyebrow"><span aria-hidden="true"></span>空间主人</p>
        <div class="profile-panel__name-row">
          <h2>{{ profile.name }}</h2>
          <span>{{ profile.role }}</span>
        </div>
        <p class="profile-panel__bio">“{{ profile.bio }}”</p>
        <p v-if="profile.status" class="profile-panel__space-status">
          <span>{{ profile.status.label }}</span>
          <strong>{{ profile.status.text }}</strong>
          <span aria-hidden="true">{{ profile.status.emoji }}</span>
        </p>
      </div>
    </div>

    <ProfileStats :stats="profile.stats" />
  </article>
</template>

<style scoped>
.profile-panel {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  min-height: 176px;
  gap: var(--space-8);
  padding: var(--space-8);
  background: linear-gradient(112deg, rgba(251, 226, 220, 0.38), transparent 34%), var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-2);
}

.profile-panel::after {
  position: absolute;
  top: 18px;
  right: 22px;
  width: 38px;
  height: 13px;
  background: rgba(139, 159, 168, 0.22);
  content: '';
  transform: rotate(4deg);
}

.profile-panel__identity { display: flex; align-items: center; min-width: 0; gap: var(--space-6); }
.profile-panel__avatar-wrap { position: relative; flex: 0 0 auto; }
.profile-panel__avatar {
  display: grid;
  width: 112px;
  height: 112px;
  place-items: center;
  object-fit: cover;
  color: var(--color-text-primary);
  background: var(--color-surface-soft);
  border: 4px solid var(--color-canvas);
  border-radius: 50%;
  box-shadow: 0 8px 22px rgba(37, 45, 54, 0.14);
}

.profile-panel__avatar--placeholder {
  background:
    radial-gradient(circle at 68% 26%, rgba(252, 251, 249, 0.92) 0 9%, transparent 9.5%),
    linear-gradient(145deg, var(--color-accent-soft), #d8cbd4);
  font-family: var(--font-serif);
  font-size: 2.35rem;
  font-weight: 700;
}

.profile-panel__status {
  position: absolute;
  right: 7px;
  bottom: 8px;
  width: 16px;
  height: 16px;
  background: var(--color-success);
  border: 3px solid var(--color-surface);
  border-radius: 50%;
}

.profile-panel__copy { min-width: 0; }
.profile-panel__edit { position: absolute; top: var(--space-5); right: var(--space-6); z-index: 1; color: var(--color-accent); font-size: .75rem; font-weight: 700; text-decoration: none; }
.profile-panel__eyebrow {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0 0 var(--space-2);
  color: var(--color-accent);
  font-family: var(--font-mono);
  font-size: var(--font-size-eyebrow);
  font-weight: 700;
  letter-spacing: 0.12em;
}

.profile-panel__eyebrow span {
  width: 6px;
  height: 6px;
  background: var(--color-apricot);
  border-radius: 50%;
  box-shadow: 0 0 0 4px rgba(139, 159, 168, 0.16);
}

.profile-panel__name-row { display: flex; align-items: baseline; gap: var(--space-3); flex-wrap: wrap; }
.profile-panel h2 {
  margin: 0;
  font-family: var(--font-serif);
  font-size: 2rem;
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.profile-panel__name-row > span {
  color: var(--color-text-secondary);
  font-family: var(--font-mono);
  font-size: 0.8125rem;
  line-height: 1.5;
}

.profile-panel__bio {
  margin: var(--space-2) 0 0;
  color: var(--color-text-secondary);
  font-family: var(--font-serif);
  line-height: 1.7;
}

.profile-panel__space-status {
  display: inline-flex;
  align-items: center;
  min-height: 36px;
  gap: var(--space-2);
  margin: var(--space-3) 0 0;
  padding: var(--space-1) var(--space-3);
  color: var(--color-text-secondary);
  background: rgba(232, 237, 241, 0.74);
  border: 1px solid rgba(217, 222, 226, 0.88);
  border-radius: var(--radius-sm);
  font-size: 0.8125rem;
  line-height: 1.45;
}

.profile-panel__space-status > span:first-child {
  color: var(--color-accent);
  font-size: 0.75rem;
  font-weight: 650;
}

.profile-panel__space-status strong {
  color: var(--color-text-primary);
  font-weight: 550;
}

@media (max-width: 900px) {
  .profile-panel { grid-template-columns: 1fr; gap: var(--space-5); }
}

@media (max-width: 767px) {
  .profile-panel { min-height: 0; padding: var(--space-5); }
  .profile-panel__identity { align-items: flex-start; gap: var(--space-4); }
  .profile-panel__avatar { width: 84px; height: 84px; }
  .profile-panel__avatar--placeholder { font-size: 1.8rem; }
  .profile-panel h2 { font-size: 1.65rem; }
  .profile-panel__name-row { display: grid; gap: 2px; }
  .profile-panel__bio { margin-top: var(--space-1); }
  .profile-panel__space-status { margin-top: var(--space-2); }
}

@media (max-width: 419px) {
  .profile-panel__identity { display: grid; grid-template-columns: 72px 1fr; }
  .profile-panel__avatar { width: 72px; height: 72px; }
}
</style>
