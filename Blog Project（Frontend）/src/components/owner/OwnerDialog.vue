<script setup>
import { nextTick, onBeforeUnmount, watch } from 'vue'

const props = defineProps({
  open: Boolean,
  title: { type: String, required: true },
  description: { type: String, default: '' },
  size: { type: String, default: 'medium' },
})
const emit = defineEmits(['close'])

const close = () => emit('close')
const onKeydown = (event) => { if (event.key === 'Escape') close() }

watch(() => props.open, async (open) => {
  document.body.style.overflow = open ? 'hidden' : ''
  if (open) {
    await nextTick()
    document.querySelector('.owner-dialog__panel input, .owner-dialog__panel textarea, .owner-dialog__panel button')?.focus()
  }
})
onBeforeUnmount(() => { document.body.style.overflow = '' })
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="owner-dialog" role="presentation" @keydown="onKeydown">
      <button class="owner-dialog__backdrop" type="button" aria-label="关闭" @click="close"></button>
      <section class="owner-dialog__panel" :class="`owner-dialog__panel--${size}`" role="dialog" aria-modal="true" :aria-labelledby="`owner-dialog-${title}`">
        <header>
          <div><p>OWNER MODE</p><h2 :id="`owner-dialog-${title}`">{{ title }}</h2><span v-if="description">{{ description }}</span></div>
          <button type="button" aria-label="关闭" @click="close">×</button>
        </header>
        <div class="owner-dialog__body"><slot /></div>
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.owner-dialog { position: fixed; inset: 0; z-index: var(--z-modal, 1000); display: grid; place-items: center; padding: var(--space-4); }
.owner-dialog__backdrop { position: absolute; inset: 0; width: 100%; background: rgba(26, 35, 43, 0.54); border: 0; backdrop-filter: blur(4px); }
.owner-dialog__panel { position: relative; width: min(100%, 620px); max-height: min(88dvh, 820px); overflow: auto; color: var(--color-text-primary); background: rgba(252, 251, 249, 0.98); border: 1px solid var(--color-border); border-radius: var(--radius-xl); box-shadow: 0 28px 80px rgba(26, 35, 43, 0.28); }
.owner-dialog__panel--large { width: min(100%, 820px); }
.owner-dialog__panel > header { position: sticky; top: 0; z-index: 1; display: flex; justify-content: space-between; gap: var(--space-4); padding: var(--space-5) var(--space-6); background: rgba(252, 251, 249, 0.96); border-bottom: 1px solid var(--color-border); }
.owner-dialog__panel > header p { margin: 0; color: var(--color-accent); font-family: var(--font-mono); font-size: 0.6875rem; font-weight: 700; letter-spacing: .11em; }
.owner-dialog__panel > header h2 { margin: var(--space-1) 0 0; font-family: var(--font-serif); font-size: 1.5rem; }
.owner-dialog__panel > header span { display: block; margin-top: var(--space-1); color: var(--color-text-secondary); font-size: .8125rem; }
.owner-dialog__panel > header button { width: 40px; height: 40px; flex: 0 0 auto; color: inherit; background: transparent; border: 1px solid var(--color-border); border-radius: 50%; cursor: pointer; font-size: 1.35rem; }
.owner-dialog__body { padding: var(--space-6); }
@media (max-width: 600px) { .owner-dialog { align-items: end; padding: 0; } .owner-dialog__panel { width: 100%; max-height: 92dvh; border-radius: var(--radius-xl) var(--radius-xl) 0 0; } .owner-dialog__panel > header, .owner-dialog__body { padding: var(--space-4); } }
</style>

