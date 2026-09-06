<script setup>
defineProps({
  stats: { type: Array, required: true },
})
</script>

<template>
  <dl class="profile-stats" aria-label="空间内容统计">
    <div v-for="stat in stats" :key="stat.label" class="profile-stats__item">
      <dt>{{ stat.label }}</dt>
      <dd>{{ stat.value }}</dd>
    </div>
  </dl>
</template>

<style scoped>
.profile-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(84px, 1fr));
  margin: 0;
  padding: var(--space-1);
  background: rgba(232, 237, 241, 0.62);
  border: 1px solid rgba(217, 222, 226, 0.82);
  border-radius: var(--radius-md);
}

.profile-stats__item {
  display: flex;
  min-height: 48px;
  align-items: baseline;
  justify-content: center;
  gap: var(--space-2);
  padding-inline: var(--space-3);
}

.profile-stats dt {
  color: var(--color-text-secondary);
  font-size: 0.75rem;
  line-height: 1.45;
}

.profile-stats dd {
  margin: 0;
  color: var(--color-text-primary);
  font-family: var(--font-mono);
  font-size: 0.9375rem;
  font-variant-numeric: tabular-nums;
  font-weight: 650;
  line-height: 1.2;
}

.profile-stats__item + .profile-stats__item {
  border-left: 1px dashed rgba(185, 194, 201, 0.82);
}

@media (max-width: 767px) {
  .profile-stats { width: 100%; }
  .profile-stats__item {
    min-height: 44px;
    display: grid;
    justify-items: center;
    align-content: center;
    gap: 0;
    padding: var(--space-2);
    text-align: center;
  }
  .profile-stats dd { grid-row: 1; font-size: 0.875rem; }
  .profile-stats dt { grid-row: 2; }
}
</style>
