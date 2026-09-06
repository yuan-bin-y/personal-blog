<script setup>
import TechPostCard from './TechPostCard.vue'
import MomentPostCard from './MomentPostCard.vue'
defineProps({ posts: { type: Array, required: true } })
defineEmits(['edit', 'delete'])
</script>

<template>
  <div class="post-feed">
    <template v-for="(post, index) in posts" :key="post.id">
      <TechPostCard v-if="post.type === 'TECH'" :post="post" :style="{ '--stagger': `${Math.min(index, 5) * 45}ms` }" @edit="$emit('edit', $event)" @delete="$emit('delete', $event)" />
      <MomentPostCard v-else :post="post" :style="{ '--stagger': `${Math.min(index, 5) * 45}ms` }" @edit="$emit('edit', $event)" @delete="$emit('delete', $event)" />
    </template>
  </div>
</template>

<style scoped>
.post-feed { display: grid; gap: var(--space-6); }
.post-feed > :deep(*) { animation: feed-in var(--duration-slow) var(--ease-standard) both; animation-delay: var(--stagger); }
@keyframes feed-in { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: none; } }
@media (prefers-reduced-motion: reduce) { .post-feed > :deep(*) { animation: none; } }
</style>
