<script setup>
import { ref } from 'vue'

const props = defineProps({
  layoutMode: { type: String, default: 'standard' },
  backgroundMode: { type: String, default: 'video' },
  surfaceOpacity: { type: Number, default: 0.86 },
  backdropShade: { type: Number, default: 0.3 },
  hasWallpaper: Boolean,
})

const emit = defineEmits(['update:layoutMode', 'update:backgroundMode', 'update:surfaceOpacity', 'update:backdropShade'])
const open = ref(false)

const useStandard = () => emit('update:layoutMode', 'standard')
const useFullscreenVideo = () => {
  emit('update:backgroundMode', 'video')
  emit('update:layoutMode', 'immersive')
}
const useFullscreenWallpaper = () => {
  if (!props.hasWallpaper) return
  emit('update:backgroundMode', 'wallpaper')
  emit('update:layoutMode', 'immersive')
}
const applyReadingPreset = () => emit('update:surfaceOpacity', 0.82)
const applyTransparentPreset = () => emit('update:surfaceOpacity', 0.48)
</script>

<template>
  <div class="appearance-controls" :class="{ 'is-open': open }">
    <button
      class="appearance-controls__trigger"
      type="button"
      :aria-expanded="open"
      aria-controls="appearance-panel"
      @click="open = !open"
    >
      空间外观
      <span aria-hidden="true">{{ open ? '×' : '◐' }}</span>
    </button>

    <section v-if="open" id="appearance-panel" class="appearance-controls__panel" aria-label="空间外观设置">
      <header><p>APPEARANCE</p><h2>空间外观</h2><span>设置只在当前浏览期间生效。</span></header>

      <div class="appearance-controls__modes" aria-label="页面模式">
        <button type="button" :class="{ 'is-active': layoutMode === 'standard' }" @click="useStandard">标准首页</button>
        <button type="button" :class="{ 'is-active': layoutMode === 'immersive' && backgroundMode === 'video' }" @click="useFullscreenVideo">全屏视频</button>
        <button type="button" :class="{ 'is-active': layoutMode === 'immersive' && backgroundMode === 'wallpaper', 'is-disabled': !hasWallpaper }" :disabled="!hasWallpaper" @click="useFullscreenWallpaper">{{ hasWallpaper ? '全屏壁纸' : '壁纸待配置' }}</button>
      </div>

      <div v-if="layoutMode === 'immersive'" class="appearance-controls__immersive-options">
        <div class="appearance-controls__presets" aria-label="透明度预设">
          <button type="button" @click="applyReadingPreset">清晰内容</button>
          <button type="button" @click="applyTransparentPreset">透明内容</button>
        </div>

        <label class="appearance-controls__range">
          <span>内容不透明度 <output>{{ Math.round(surfaceOpacity * 100) }}%</output></span>
          <input type="range" min="0.42" max="0.96" step="0.02" :value="surfaceOpacity" @input="emit('update:surfaceOpacity', Number($event.target.value))" />
        </label>

        <label class="appearance-controls__range">
          <span>背景遮罩 <output>{{ Math.round(backdropShade * 100) }}%</output></span>
          <input type="range" min="0.08" max="0.68" step="0.02" :value="backdropShade" @input="emit('update:backdropShade', Number($event.target.value))" />
        </label>
      </div>
    </section>
  </div>
</template>

<style scoped>
.appearance-controls { position: fixed; right: var(--space-6); bottom: var(--space-6); z-index: var(--z-overlay); display: grid; justify-items: end; gap: var(--space-3); }
.appearance-controls__trigger { display: inline-flex; min-height: 44px; align-items: center; gap: var(--space-3); padding-inline: var(--space-4); color: var(--color-text-inverse); background: rgb(37 45 54 / 0.82); border: 1px solid rgb(249 250 251 / 0.26); border-radius: var(--radius-pill); box-shadow: var(--shadow-2); backdrop-filter: blur(16px); cursor: pointer; font-size: 0.8125rem; font-weight: 700; }
.appearance-controls__trigger span { display: grid; width: 22px; height: 22px; place-items: center; border: 1px solid currentColor; border-radius: 50%; }
.appearance-controls__panel { width: min(340px, calc(100vw - 2 * var(--page-gutter))); padding: var(--space-5); color: var(--color-text-primary); background: rgb(252 251 249 / 0.94); border: 1px solid var(--color-border); border-radius: var(--radius-xl); box-shadow: var(--shadow-2); backdrop-filter: blur(20px); }
.appearance-controls__panel header p { margin: 0; color: var(--color-accent); font-family: var(--font-mono); font-size: 0.6875rem; font-weight: 700; letter-spacing: 0.11em; }
.appearance-controls__panel h2 { margin: var(--space-1) 0 0; font-family: var(--font-serif); font-size: 1.35rem; }
.appearance-controls__panel header span { color: var(--color-text-secondary); font-size: 0.75rem; }
.appearance-controls__presets { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-2); margin-top: var(--space-4); }
.appearance-controls__presets button { min-height: 40px; background: var(--color-surface-soft); border: 1px solid var(--color-border); border-radius: var(--radius-sm); cursor: pointer; font-size: 0.8125rem; font-weight: 650; }
.appearance-controls__modes { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--space-2); margin-top: var(--space-4); }
.appearance-controls__modes button { min-height: 44px; padding: var(--space-2); color: var(--color-text-secondary); background: transparent; border: 1px solid var(--color-border); border-radius: var(--radius-sm); cursor: pointer; font-size: 0.75rem; font-weight: 700; }
.appearance-controls__modes button.is-active { color: var(--color-text-inverse); background: var(--color-accent); border-color: var(--color-accent); }
.appearance-controls__modes button.is-disabled { cursor: not-allowed; opacity: 0.58; }
.appearance-controls__immersive-options { margin-top: var(--space-4); padding-top: 1px; border-top: 1px solid var(--color-border); }
.appearance-controls fieldset { display: grid; gap: var(--space-2); margin: var(--space-4) 0 0; padding: var(--space-3) 0; border: 0; border-top: 1px solid var(--color-border); }
.appearance-controls legend { padding: var(--space-3) 0 0; font-size: 0.75rem; font-weight: 700; }
.appearance-controls fieldset label { display: flex; min-height: 36px; align-items: center; gap: var(--space-2); font-size: 0.8125rem; }
.appearance-controls fieldset label.is-disabled { color: var(--color-text-secondary); }
.appearance-controls input { accent-color: var(--color-accent); }
.appearance-controls__range { display: grid; gap: var(--space-2); padding-top: var(--space-3); border-top: 1px dashed var(--color-border-strong); }
.appearance-controls__range > span { display: flex; justify-content: space-between; color: var(--color-text-secondary); font-size: 0.75rem; font-weight: 650; }
.appearance-controls__range input { width: 100%; }
@media (max-width: 767px) { .appearance-controls { right: var(--space-3); bottom: var(--space-3); } .appearance-controls__panel { padding: var(--space-4); } }
</style>
