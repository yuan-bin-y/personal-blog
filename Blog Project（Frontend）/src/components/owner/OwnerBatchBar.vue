<script setup>
defineProps({
  selected: { type: Number, default: 0 },
  total: { type: Number, default: 0 },
  busy: Boolean,
  message: { type: String, default: '' },
})
defineEmits(['toggle-all', 'publish', 'delete', 'trash'])
</script>

<template>
  <aside class="batch-bar" aria-label="批量内容操作">
    <label>
      <input type="checkbox" :checked="selected > 0 && selected === total" @change="$emit('toggle-all')" />
      <span>{{ selected ? `已选 ${selected} 项` : '批量选择' }}</span>
    </label>
    <p v-if="message" aria-live="polite">{{ message }}</p>
    <div>
      <button type="button" :disabled="!selected || busy" @click="$emit('publish')">批量发布</button>
      <button class="is-danger" type="button" :disabled="!selected || busy" @click="$emit('delete')">移到回收站</button>
      <button type="button" :disabled="busy" @click="$emit('trash')">查看回收站</button>
    </div>
  </aside>
</template>

<style scoped>
.batch-bar { display: flex; min-height: 58px; align-items: center; gap: var(--space-4); margin-top: var(--space-5); padding: var(--space-2) var(--space-3) var(--space-2) var(--space-4); color: var(--color-text-secondary); background: color-mix(in srgb, var(--color-surface-soft) 72%, transparent); border-block: 1px solid var(--color-border); }
.batch-bar label { display: inline-flex; align-items: center; gap: var(--space-2); font-size: .8125rem; font-weight: 700; cursor: pointer; }
.batch-bar input { width: 17px; height: 17px; accent-color: var(--color-accent); }
.batch-bar p { flex: 1; margin: 0; font-size: .75rem; }
.batch-bar > div { display: flex; gap: var(--space-2); margin-left: auto; }
.batch-bar button { min-height: 38px; padding: 0 var(--space-3); color: var(--color-text-secondary); background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-pill); cursor: pointer; font-size: .75rem; font-weight: 700; }
.batch-bar button:hover:not(:disabled) { color: var(--color-accent); border-color: var(--color-border-strong); }
.batch-bar button.is-danger:hover:not(:disabled) { color: var(--color-error); }
.batch-bar button:disabled { cursor: not-allowed; opacity: .48; }
@media (max-width: 700px) { .batch-bar { align-items: flex-start; flex-direction: column; } .batch-bar > div { width: 100%; margin-left: 0; overflow-x: auto; } .batch-bar button { flex: 0 0 auto; } }
</style>
