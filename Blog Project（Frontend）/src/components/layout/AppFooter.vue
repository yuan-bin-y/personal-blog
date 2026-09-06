<script setup>
import { RouterLink } from 'vue-router'
import { siteMeta } from '../../mock/site'
import { useSpaceRuntime } from '../../stores/useSpaceRuntime'

defineProps({
  tone: {
    type: String,
    default: 'neutral',
    validator: (value) => ['home', 'moments', 'guestbook', 'tech', 'archive', 'about', 'neutral'].includes(value),
  },
})
const { state: space } = useSpaceRuntime()
</script>

<template>
  <footer class="app-footer" :class="`app-footer--${tone}`">
    <div class="app-footer__inner">
      <div><strong>{{ space.basic.chineseName }}</strong><p>{{ space.basic.description }}</p></div>
      <nav aria-label="页脚导航"><RouterLink to="/about">关于这个空间</RouterLink><RouterLink to="/archive">内容归档</RouterLink></nav>
      <p class="app-footer__mark">© {{ siteMeta.currentYear }} {{ space.basic.name.toUpperCase() }} · MADE SLOWLY</p>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  --footer-text: #303b44;
  --footer-muted: rgb(48 59 68 / 0.7);
  margin-top: var(--space-24);
  padding: 0 0 var(--space-8);
  color: var(--footer-text);
  background: transparent;
}
.app-footer--home {
  --footer-text: #2e414e;
  --footer-muted: rgb(46 65 78 / 0.68);
}
.app-footer--moments {
  --footer-text: #2d4654;
  --footer-muted: rgb(45 70 84 / 0.7);
}
.app-footer--guestbook {
  --footer-text: #493740;
  --footer-muted: rgb(73 55 64 / 0.7);
}
.app-footer--tech {
  --footer-text: #f4fbfd;
  --footer-muted: rgb(244 251 253 / 0.76);
}
.app-footer--tech .app-footer__inner { text-shadow: 0 2px 14px rgb(21 55 69 / 0.68); }
.app-footer--archive {
  --footer-text: #f0f3ed;
  --footer-muted: rgb(240 243 237 / 0.72);
}
.app-footer--archive .app-footer__inner { text-shadow: 0 2px 14px rgb(9 18 23 / 0.6); }
.app-footer--about {
  --footer-text: #54352e;
  --footer-muted: rgb(84 53 46 / 0.72);
}
.app-footer__inner {
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: end;
  width: min(calc(100% - 2 * var(--page-gutter)), 1260px);
  gap: var(--space-6);
  margin-inline: auto;
  padding: var(--space-5) 0;
  text-shadow: 0 1px 12px rgb(255 255 255 / 0.42);
}
.app-footer strong { font-family: var(--font-serif); font-size: 1.25rem; }
.app-footer p { margin: var(--space-1) 0 0; color: var(--footer-muted); font-size: 0.8125rem; }
.app-footer nav { display: flex; gap: var(--space-5); }
.app-footer nav a { min-height: 44px; display: inline-flex; align-items: center; font-size: 0.8125rem; text-underline-offset: 4px; }
.app-footer__mark { grid-column: 1 / -1; padding-top: var(--space-2); font-family: var(--font-mono); letter-spacing: 0.08em; }
@media (max-width: 600px) {
  .app-footer { padding-bottom: var(--space-4); }
  .app-footer__inner { grid-template-columns: 1fr; align-items: start; padding: var(--space-4) 0; }
  .app-footer nav { flex-wrap: wrap; }
}
</style>
