<script setup>
defineProps({
  isPlaying: Boolean,
  isMuted: { type: Boolean, default: true },
  volume: { type: Number, default: 0.55 },
})

defineEmits(['toggle-playback', 'toggle-muted', 'update-volume'])
</script>

<template>
  <div class="hero-controls" aria-label="Hero 媒体控制">
    <button
      class="hero-controls__button"
      type="button"
      :aria-label="isPlaying ? '暂停背景视频' : '播放背景视频'"
      @click="$emit('toggle-playback')"
    >
      <span class="hero-controls__symbol" aria-hidden="true">{{ isPlaying ? 'Ⅱ' : '▶' }}</span>
      {{ isPlaying ? 'PAUSE' : 'PLAY' }}
    </button>
    <button
      class="hero-controls__button"
      type="button"
      :aria-pressed="!isMuted"
      :aria-label="isMuted ? '打开背景视频声音' : '关闭背景视频声音'"
      @click="$emit('toggle-muted')"
    >
      <span class="hero-controls__sound" aria-hidden="true"></span>
      {{ isMuted ? 'SOUND OFF' : 'SOUND ON' }}
    </button>
    <label class="hero-controls__volume">
      <span class="sr-only">背景视频音量</span>
      <input
        type="range"
        min="0"
        max="1"
        step="0.05"
        :value="volume"
        aria-label="背景视频音量"
        @input="$emit('update-volume', Number($event.target.value))"
      />
    </label>
  </div>
</template>

<style scoped>
.hero-controls {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.hero-controls__button {
  min-height: 44px;
  color: var(--color-text-inverse);
  background: rgba(25, 32, 40, 0.42);
  border: 1px solid rgba(255, 247, 238, 0.38);
  border-radius: var(--radius-pill);
  backdrop-filter: blur(12px);
  font-family: var(--font-mono);
  font-size: 0.6875rem;
  letter-spacing: 0.08em;
}

.hero-controls__button {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding-inline: var(--space-4);
  cursor: pointer;
  transition: background-color var(--duration-fast) ease-out, border-color var(--duration-fast) ease-out;
}

.hero-controls__button:hover {
  background: rgba(25, 32, 40, 0.68);
  border-color: rgba(255, 247, 238, 0.72);
}

.hero-controls__symbol { display: inline-grid; width: 14px; place-items: center; font-size: 0.625rem; }
.hero-controls__sound {
  width: 7px;
  height: 7px;
  background: currentColor;
  border-radius: 50%;
  box-shadow: 0 0 0 4px rgba(255, 247, 238, 0.12);
}

.hero-controls__volume {
  display: flex;
  align-items: center;
  width: 92px;
  min-height: 44px;
  padding-inline: var(--space-3);
  background: rgba(25, 32, 40, 0.42);
  border: 1px solid rgba(255, 247, 238, 0.38);
  border-radius: var(--radius-pill);
  backdrop-filter: blur(12px);
}
.hero-controls__volume input { width: 100%; accent-color: var(--color-apricot); cursor: pointer; }

@media (max-width: 479px) {
  .hero-controls { justify-content: flex-start; }
  .hero-controls__volume { width: 76px; }
}
</style>
