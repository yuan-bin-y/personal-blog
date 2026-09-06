<script setup>
import { computed } from 'vue'
import { useSpaceRuntime } from '../stores/useSpaceRuntime'

const { state } = useSpaceRuntime()
const profile = computed(() => state.profile)
const externalLinks = computed(() => state.externalLinks)

const lifeNotes = [
  { label: '最近在做', text: '等待后端开发' },
  { label: '最近在学', text: '等待后端开发' },
  { label: '最近在听', text: '等待后端开发' },
]
</script>

<template>
  <main id="main-content" class="about-page">
    <div class="about-layout">
      <header class="about-heading"><p>ABOUT</p><h1>关于玢</h1><span>这里不是一份在线简历，而是一间慢慢布置起来的个人空间。</span></header>
      <section class="about-intro"><span class="about-intro__avatar" aria-hidden="true"><img v-if="profile.avatar" :src="profile.avatar" alt="" /><template v-else>{{ profile.name }}</template></span><div><h2>{{ profile.name }}</h2><strong>{{ profile.role }}</strong><blockquote>“{{ profile.bio }}”</blockquote></div></section>
      <section class="about-now" aria-labelledby="about-now-title"><h2 id="about-now-title">近来的小事</h2><dl><div v-for="note in lifeNotes" :key="note.label"><dt>{{ note.label }}</dt><dd>{{ note.text }}</dd></div></dl></section>
      <section><h2>为什么做这个空间</h2><div class="about-copy"><p>技术博客常常只留下整理好的结论，但真正的生活还有卡住的晚上、突然想通的瞬间，以及和代码无关的小事。</p><p>BinSpace 想把这些内容放在一起。TECH 用来认真说明一个问题，MOMENT 用来记住无需成为文章的日常。</p></div></section>
      <section><h2>喜欢记录什么</h2><div class="about-chips"><span>Java 后端</span><span>Spring Boot</span><span>Redis</span><span>数据库</span><span>项目复盘</span><span>普通生活</span></div></section>
      <section><h2>空间之外</h2><div class="about-links"><a v-if="externalLinks.homepage" :href="externalLinks.homepage">个人主页 ↗</a><span v-else>个人主页 · 待配置</span><a v-if="externalLinks.github" :href="externalLinks.github">GitHub ↗</a><span v-else>GitHub · 待配置</span></div></section>
    </div>
  </main>
</template>

<style scoped>
.about-page { padding: var(--space-6); background: rgba(252, 251, 249, 0.8); border: 1px solid var(--color-border); border-radius: var(--radius-xl); }
.about-layout { max-width: 920px; }.about-layout > section { padding: var(--space-8) 0; border-bottom: 1px solid var(--color-border); }
.about-heading { max-width: 760px; padding-bottom: var(--space-10); border-bottom: 1px solid var(--color-border); }
.about-heading p { margin: 0 0 var(--space-3); color: var(--color-accent); font-family: var(--font-mono); font-size: var(--font-size-eyebrow); font-weight: 700; letter-spacing: 0.12em; }
.about-heading h1 { margin: 0; font-family: var(--font-serif); font-size: var(--font-size-h1); }
.about-heading span { display: block; margin-top: var(--space-4); color: var(--color-text-secondary); font-family: var(--font-serif); font-size: 1.12rem; line-height: 1.9; }
.about-layout h2 { margin: 0; font-family: var(--font-serif); font-size: var(--font-size-h2); }.about-intro { display: grid; grid-template-columns: 128px 1fr; align-items: center; gap: var(--space-6); }
.about-intro__avatar { display: grid; width: 128px; height: 128px; place-items: center; background: linear-gradient(145deg, var(--color-accent-soft), #d8cbd4); border: 5px solid var(--color-surface); border-radius: 50%; box-shadow: var(--shadow-1); font-family: var(--font-serif); font-size: 2.6rem; font-weight: 700; }
.about-intro__avatar img { width: 100%; height: 100%; object-fit: cover; border-radius: inherit; }
.about-intro strong { display: block; margin-top: var(--space-2); color: var(--color-text-secondary); font-family: var(--font-mono); font-size: 0.875rem; }.about-intro blockquote { margin: var(--space-4) 0 0; font-family: var(--font-serif); font-size: 1.1rem; }
.about-copy { max-width: 72ch; margin-top: var(--space-5); color: var(--color-text-secondary); }.about-copy p { line-height: 1.85; }
.about-now { display: grid; grid-template-columns: minmax(180px, 1fr) minmax(0, 2fr); gap: var(--space-8); }
.about-now dl { margin: 0; }
.about-now dl > div { display: grid; grid-template-columns: 110px 1fr; gap: var(--space-4); padding: var(--space-3) 0; border-top: 1px dashed var(--color-border-strong); }
.about-now dt { color: var(--color-accent); font-size: var(--font-size-meta); font-weight: 700; }
.about-now dd { margin: 0; color: var(--color-text-secondary); }
.about-chips,
.about-links { display: flex; gap: var(--space-3); margin-top: var(--space-5); flex-wrap: wrap; }.about-chips span,
.about-links > * { padding: var(--space-2) var(--space-4); background: var(--color-surface-soft); border: 1px solid var(--color-border); border-radius: var(--radius-pill); font-size: 0.875rem; text-decoration: none; }.about-links span { color: var(--color-text-secondary); }
@media (max-width: 600px) {
  .about-page { padding: var(--space-5); border-radius: var(--radius-lg); }
  .about-heading { padding-bottom: var(--space-8); }
  .about-layout > section { padding: var(--space-8) 0; }
  .about-intro { grid-template-columns: 84px 1fr; }
  .about-intro__avatar { width: 84px; height: 84px; font-size: 1.8rem; }
  .about-now { grid-template-columns: 1fr; gap: var(--space-5); }
  .about-now dl > div { grid-template-columns: 96px 1fr; gap: var(--space-3); }
}
</style>
