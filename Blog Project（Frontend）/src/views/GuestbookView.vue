<script setup>
import { onMounted } from 'vue'
import BackendPending from '../components/layout/BackendPending.vue'
import GuestbookEntry from '../components/guestbook/GuestbookEntry.vue'
import { useSpaceRuntime } from '../stores/useSpaceRuntime'
const { state, loadGuestbook } = useSpaceRuntime()
onMounted(loadGuestbook)
</script>

<template>
  <main id="main-content" class="guestbook-page">
    <header class="guestbook-intro">
      <div><p>GUESTBOOK</p><h1>留言板</h1></div>
      <blockquote>来都来了，<br />留句话再走吧。</blockquote>
      <span>这里的话写给空间主人，不属于某一篇文章或说说。</span>
    </header>
    <div class="guestbook-layout">
      <section aria-labelledby="leave-message-title"><h2 id="leave-message-title">给玢留句话</h2><BackendPending compact description="Visitor 留言提交接口尚未开放，当前可以浏览已有留言。" /></section>
      <section aria-labelledby="message-list-title"><header><div><h2 id="message-list-title">空间留言</h2><p>留言属于整个个人空间，不属于某篇内容。</p></div></header><div v-if="state.guestbook.length" class="guestbook-list"><GuestbookEntry v-for="(entry, index) in state.guestbook" :key="entry.id" :entry="entry" :index="index" /></div><BackendPending v-else compact title="暂无留言" description="数据库中当前还没有空间留言。" /></section>
    </div>
  </main>
</template>

<style scoped>
.guestbook-page {
  --color-accent: #8d5f75;
  --color-accent-hover: #72485c;
  --color-accent-soft: #f0e0e8;
  --color-border: #dccdd5;
  --color-border-strong: #c0aab5;
  --color-text-primary: #3d3038;
  --color-text-secondary: #75606a;
  position: relative;
  padding: var(--space-6);
  overflow: hidden;
  background: linear-gradient(180deg, rgb(252 247 250 / 0.5), rgb(250 246 248 / 0.64));
  border: 1px solid rgb(220 205 213 / 0.72);
  border-radius: var(--radius-xl);
  box-shadow: 0 16px 44px rgb(95 65 80 / 0.14);
  backdrop-filter: saturate(0.96);
}
.guestbook-page::after { position: absolute; inset: 0; z-index: 0; background: linear-gradient(120deg, transparent 42%, rgb(255 255 255 / 0.09)); content: ''; pointer-events: none; }
.guestbook-page > * { position: relative; z-index: 1; }
.guestbook-intro { display: grid; grid-template-columns: 1fr 1fr; align-items: end; gap: var(--space-6); max-width: 1060px; }
.guestbook-intro p { margin: 0 0 var(--space-2); color: var(--color-accent); font-family: var(--font-mono); font-size: var(--font-size-eyebrow); font-weight: 700; letter-spacing: 0.12em; }
.guestbook-intro h1 { margin: 0; font-family: var(--font-serif); font-size: var(--font-size-h1); }
.guestbook-intro blockquote { margin: 0; color: var(--color-accent); font-family: var(--font-serif); font-size: clamp(1.75rem, 3vw, 2.6rem); line-height: 1.35; }
.guestbook-intro > span { grid-column: 1 / -1; max-width: 680px; padding-top: var(--space-5); color: var(--color-text-secondary); border-top: 1px solid var(--color-border); line-height: 1.8; }
.guestbook-layout { display: grid; grid-template-columns: 1fr; gap: var(--space-10); padding-top: var(--space-10); }
.guestbook-layout section > header p { margin: var(--space-1) 0 var(--space-5); color: var(--color-text-secondary); font-size: var(--font-size-meta); }
.guestbook-layout h2 { margin: 0 0 var(--space-5); font-family: var(--font-serif); font-size: var(--font-size-h2); }
.guestbook-layout section > header { display: flex; align-items: start; justify-content: space-between; border-bottom: 1px solid var(--color-border); }.guestbook-layout section > header span { margin-top: var(--space-2); color: var(--color-text-secondary); font-size: var(--font-size-meta); }
.guestbook-list { display: grid; }
@media (max-width: 880px) { .guestbook-layout { grid-template-columns: 1fr; } }
@media (max-width: 600px) {
  .guestbook-page { padding: var(--space-5); border-radius: var(--radius-lg); }
  .guestbook-intro { grid-template-columns: 1fr; }
  .guestbook-intro blockquote { font-size: 1.75rem; }
  .guestbook-intro > span { grid-column: auto; padding-top: var(--space-4); }
  .guestbook-layout { padding-top: var(--space-10); gap: var(--space-12); }
}
</style>
