<script setup>
defineProps({
  media: { type: Object, required: true },
  eager: Boolean,
})
</script>

<template>
  <figure class="media-frame" :class="`media-frame--${media.tone || 'sand'}`" :style="{ aspectRatio: media.ratio || '4 / 3' }">
    <img
      v-if="media.src"
      :src="media.src"
      :alt="media.alt"
      :width="media.width"
      :height="media.height"
      :loading="eager ? 'eager' : 'lazy'"
      decoding="async"
    />
    <div v-else class="media-frame__placeholder" role="img" :aria-label="media.alt">
      <span aria-hidden="true"></span>
      <small>{{ media.label || 'MEDIA PLACEHOLDER' }}</small>
    </div>
  </figure>
</template>

<style scoped>
.media-frame {
  position: relative;
  margin: 0;
  overflow: hidden;
  background: var(--color-surface-soft);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}
.media-frame img { width: 100%; height: 100%; object-fit: cover; }
.media-frame__placeholder {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  align-content: center;
  gap: var(--space-3);
  color: rgba(41, 48, 56, 0.64);
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.46), transparent 48%),
    repeating-linear-gradient(135deg, transparent 0 18px, rgba(255, 255, 255, 0.14) 18px 19px);
}
.media-frame__placeholder span {
  width: clamp(42px, 8vw, 72px);
  aspect-ratio: 1;
  border: 1px solid currentColor;
  border-radius: 50%;
  opacity: 0.42;
}
.media-frame__placeholder small {
  max-width: 80%;
  font-family: var(--font-mono);
  font-size: 0.625rem;
  letter-spacing: 0.08em;
  text-align: center;
}
.media-frame--coral { background: #e4d8df; }
.media-frame--peach { background: #e2e6ea; }
.media-frame--sage { background: #dce2cf; }
.media-frame--blue { background: #d6e0e3; }
.media-frame--slate { background: #d5d0cd; }
.media-frame--paper { background: #e8e8e5; }
.media-frame--sand { background: #dde3e7; }
</style>
