<script setup>
import { ref } from 'vue'
import MediaFrame from '../media/MediaFrame.vue'

defineProps({ blocks: { type: Array, required: true } })
const copiedIndex = ref(-1)
const tokenize = (text) => text.split(/(`[^`]+`)/g).filter(Boolean).map((value) => ({ value: value.replace(/^`|`$/g, ''), code: value.startsWith('`') }))
const copyCode = async (code, index) => {
  try {
    await navigator.clipboard.writeText(code)
    copiedIndex.value = index
    window.setTimeout(() => { copiedIndex.value = -1 }, 1600)
  } catch { copiedIndex.value = -1 }
}
</script>

<template>
  <div class="article-content">
    <template v-for="(block, index) in blocks" :key="`${block.type}-${block.id || index}`">
      <p v-if="block.type === 'lead'" class="article-content__lead">{{ block.text }}</p>
      <p v-else-if="block.type === 'p'">
        <template v-for="(token, tokenIndex) in tokenize(block.text)" :key="tokenIndex"><code v-if="token.code">{{ token.value }}</code><template v-else>{{ token.value }}</template></template>
      </p>
      <h2 v-else-if="block.type === 'h2'" :id="block.id">{{ block.text }}</h2>
      <h3 v-else-if="block.type === 'h3'" :id="block.id">{{ block.text }}</h3>
      <blockquote v-else-if="block.type === 'blockquote'">{{ block.text }}</blockquote>
      <ul v-else-if="block.type === 'ul'"><li v-for="item in block.items" :key="item">{{ item }}</li></ul>
      <ol v-else-if="block.type === 'ol'"><li v-for="item in block.items" :key="item">{{ item }}</li></ol>
      <div v-else-if="block.type === 'code'" class="code-block">
        <header><span>{{ block.language }}</span><button type="button" @click="copyCode(block.code, index)">{{ copiedIndex === index ? '已复制' : '复制' }}</button></header>
        <pre><code>{{ block.code }}</code></pre><span class="sr-only" aria-live="polite">{{ copiedIndex === index ? '代码已复制' : '' }}</span>
      </div>
      <div v-else-if="block.type === 'table'" class="article-table" tabindex="0" aria-label="可横向滚动的数据表格">
        <table><thead><tr><th v-for="header in block.headers" :key="header">{{ header }}</th></tr></thead><tbody><tr v-for="(row, rowIndex) in block.rows" :key="rowIndex"><td v-for="(cell, cellIndex) in row" :key="cellIndex">{{ cell }}</td></tr></tbody></table>
      </div>
      <figure v-else-if="block.type === 'image'" class="article-image"><MediaFrame :media="block.media" /><figcaption>{{ block.caption }}</figcaption></figure>
    </template>
  </div>
</template>

<style scoped>
.article-content { width: min(100%, 72ch); font-size: 1.0625rem; line-height: 1.85; }
.article-content p { margin: 1em 0; }
.article-content__lead { margin-top: 0 !important; color: var(--color-text-secondary); font-family: var(--font-serif); font-size: 1.2rem; line-height: 1.8; }
.article-content h2,
.article-content h3 { scroll-margin-top: 96px; font-family: var(--font-serif); line-height: 1.3; }
.article-content h2 { margin: 2.2em 0 0.65em; font-size: 1.8rem; }
.article-content h3 { margin: 1.8em 0 0.6em; font-size: 1.3rem; }
.article-content blockquote { margin: var(--space-8) 0; padding: var(--space-4) var(--space-5); color: var(--color-text-secondary); background: var(--color-surface-blush); border-left: 3px solid var(--color-accent); border-radius: 0 var(--radius-md) var(--radius-md) 0; font-family: var(--font-serif); }
.article-content ul,
.article-content ol { padding-left: 1.5em; }
.article-content li + li { margin-top: var(--space-2); }
.article-content p code { padding: 0.1em 0.38em; color: var(--color-accent-pressed); background: var(--color-accent-soft); border-radius: 5px; font-family: var(--font-mono); font-size: 0.88em; }
.code-block { margin: var(--space-8) 0; overflow: hidden; color: var(--color-text-inverse); background: var(--color-surface-dark); border-radius: var(--radius-md); }
.code-block header { display: flex; align-items: center; justify-content: space-between; min-height: 42px; padding: 0 var(--space-4); border-bottom: 1px solid rgba(255, 247, 238, 0.12); }
.code-block header span { font-family: var(--font-mono); font-size: 0.6875rem; letter-spacing: 0.08em; text-transform: uppercase; }
.code-block button { min-width: 58px; min-height: 32px; color: inherit; background: transparent; border: 1px solid rgba(255, 247, 238, 0.24); border-radius: var(--radius-sm); cursor: pointer; font-size: 0.75rem; }
.code-block pre { margin: 0; padding: var(--space-5); overflow-x: auto; }
.code-block code { font-family: var(--font-mono); font-size: 0.875rem; line-height: 1.7; }
.article-table { margin: var(--space-8) 0; overflow-x: auto; border: 1px solid var(--color-border); border-radius: var(--radius-md); }
.article-table table { width: 100%; min-width: 540px; border-collapse: collapse; }
.article-table th,
.article-table td { padding: var(--space-3) var(--space-4); border-bottom: 1px solid var(--color-border); text-align: left; }
.article-table th { background: var(--color-surface-soft); font-size: 0.8125rem; }
.article-image { margin: var(--space-8) 0; }
.article-image figcaption { margin-top: var(--space-2); color: var(--color-text-secondary); font-size: 0.75rem; text-align: center; }
@media (max-width: 600px) { .article-content { font-size: 1rem; } .article-content h2 { font-size: 1.55rem; } .code-block pre { padding: var(--space-4); } }
</style>
