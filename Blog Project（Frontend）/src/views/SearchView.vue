<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import * as searchApi from '../api/search'
import { apiMessage } from '../api/request'
import { useOwnerMode } from '../stores/useOwnerMode'

const route = useRoute()
const router = useRouter()
const auth = useOwnerMode()
const mode = ref(route.query.mode === 'ai' ? 'ai' : 'search')
const filters = reactive({ q: String(route.query.q || ''), type: 'ALL', page: 1, pageSize: 10 })
const results = ref([])
const total = ref(0)
const hasMore = ref(false)
const question = ref('')
const answer = ref(null)
const loading = ref(false)
const error = ref('')
const task = ref(null)
let taskTimer
const hrefOf = (item) => item.type === 'TECH' && item.slug ? `/tech/${item.slug}` : `/moments/${item.id || item.postId}`
const dateOf = (value) => value ? new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: 'short', day: '2-digit' }).format(new Date(value)) : ''
const plainHighlight = (value) => String(value || '').replace(/<\/?em>/gi, '')

const search = async (append = false) => {
  if (!filters.q.trim()) return
  loading.value = true; error.value = ''
  try {
    const page = await searchApi.searchPosts(filters)
    results.value = append ? [...results.value, ...page.items] : page.items
    total.value = page.total; hasMore.value = page.hasMore
    await router.replace({ query: { q: filters.q, ...(mode.value === 'ai' ? { mode: 'ai' } : {}) } })
  } catch (reason) { error.value = apiMessage(reason) } finally { loading.value = false }
}
const more = () => { filters.page += 1; search(true) }
const ask = async () => {
  if (!question.value.trim()) return
  loading.value = true; error.value = ''; answer.value = null
  try { answer.value = await searchApi.askAi(question.value.trim()) } catch (reason) { error.value = apiMessage(reason) } finally { loading.value = false }
}
const pollTask = async () => {
  if (!task.value?.taskId) return
  try {
    task.value = await searchApi.getReindexTask(task.value.taskId)
    if (!['PENDING', 'RUNNING'].includes(task.value.status)) clearInterval(taskTimer)
  } catch (reason) { error.value = apiMessage(reason); clearInterval(taskTimer) }
}
const reindex = async () => {
  error.value = ''
  try { task.value = await searchApi.createReindexTask(); clearInterval(taskTimer); taskTimer = window.setInterval(pollTask, 1500) } catch (reason) { error.value = apiMessage(reason) }
}
onMounted(() => { if (filters.q) search() })
onBeforeUnmount(() => clearInterval(taskTimer))
</script>

<template>
  <main id="main-content" class="search-page">
    <header><p>FIND IN BINSPACE</p><h1>在代码和生活之间寻找</h1><span>普通搜索从已发布内容里查找；AI 问答只根据 BinSpace 的站内内容回答，并附上来源。</span></header>
    <div class="search-page__modes"><button type="button" :class="{ active: mode === 'search' }" @click="mode = 'search'">站内搜索</button><button type="button" :class="{ active: mode === 'ai' }" @click="mode = 'ai'">问问 BinSpace</button></div>

    <section v-if="mode === 'search'" class="search-page__workbench">
      <form @submit.prevent="filters.page = 1; search()"><label><span class="sr-only">搜索关键词</span><input v-model.trim="filters.q" maxlength="100" placeholder="输入 Java、Redis 或一段生活记录…" required /></label><select v-model="filters.type" aria-label="内容类型"><option value="ALL">全部</option><option value="TECH">技术</option><option value="MOMENT">说说</option></select><button type="submit" :disabled="loading">搜索</button></form>
      <p v-if="results.length" class="search-page__count">找到 {{ total }} 条公开内容</p>
      <div class="search-page__results"><RouterLink v-for="item in results" :key="item.id" :to="hrefOf(item)"><div><span>{{ item.type }}</span><time>{{ dateOf(item.publishedAt) }}</time></div><h2>{{ item.title || '一条说说' }}</h2><p>{{ item.excerpt }}</p><ul v-if="item.highlights?.length"><li v-for="text in item.highlights" :key="text">{{ plainHighlight(text) }}</li></ul></RouterLink></div>
      <button v-if="hasMore" class="search-page__more" type="button" :disabled="loading" @click="more">查看更多</button>
      <p v-else-if="filters.q && !loading && !results.length" class="search-page__empty">没有找到相关内容，换个词试试看。</p>
    </section>

    <section v-else class="search-page__ai">
      <form @submit.prevent="ask"><label>你的问题<textarea v-model.trim="question" maxlength="500" rows="4" placeholder="例如：玢写过哪些 Redis 相关内容？" required></textarea><small>{{ question.length }} / 500</small></label><button type="submit" :disabled="loading">{{ loading ? '正在阅读空间…' : '提问' }}</button></form>
      <article v-if="answer"><p>{{ answer.answer }}</p><footer v-if="answer.sources?.length"><strong>回答来源</strong><RouterLink v-for="source in answer.sources" :key="source.postId" :to="source.slug ? `/tech/${source.slug}` : `/moments/${source.postId}`"><span>{{ source.title }}</span><small>{{ source.excerpt }}</small></RouterLink></footer></article>
      <p v-else class="search-page__ai-note">AI 不会脱离站内内容编造答案；没有足够依据时会直接说明。</p>
    </section>

    <p v-if="error" class="search-page__error">{{ error }}</p>
    <aside v-if="auth.isOwner.value" class="search-page__owner"><div><strong>搜索索引</strong><span>文章发布或修改后由后端同步索引；需要时可手动全量重建。</span><small v-if="task">{{ task.status }}<template v-if="task.indexedCount != null"> · {{ task.indexedCount }} 条</template><template v-if="task.failureMessage"> · {{ task.failureMessage }}</template></small></div><button type="button" :disabled="task && ['PENDING','RUNNING'].includes(task.status)" @click="reindex">重建索引</button></aside>
  </main>
</template>

<style scoped>
.search-page{padding:var(--space-8);color:var(--color-text-primary);background:rgb(252 251 249/.94);border:1px solid var(--color-border);border-radius:var(--radius-xl);box-shadow:var(--shadow-1)}.search-page>header{max-width:760px}.search-page>header p{margin:0 0 var(--space-2);color:var(--color-accent);font-family:var(--font-mono);font-size:var(--font-size-eyebrow);font-weight:700;letter-spacing:.12em}.search-page h1{margin:0;font-family:var(--font-serif);font-size:clamp(2rem,5vw,3.6rem);line-height:1.12}.search-page>header span{display:block;margin-top:var(--space-4);color:var(--color-text-secondary);line-height:1.8}.search-page__modes{display:flex;gap:var(--space-2);margin-top:var(--space-8);border-bottom:1px solid var(--color-border)}.search-page__modes button{min-height:46px;padding:0 var(--space-5);color:var(--color-text-secondary);background:transparent;border:0;border-bottom:2px solid transparent;cursor:pointer;font-weight:700}.search-page__modes button.active{color:var(--color-accent);border-color:currentColor}.search-page__workbench,.search-page__ai{max-width:880px;padding-top:var(--space-6)}.search-page__workbench>form{display:grid;grid-template-columns:minmax(0,1fr) 130px auto;gap:var(--space-3)}.search-page input,.search-page select,.search-page textarea{width:100%;padding:var(--space-3);color:var(--color-text-primary);background:var(--color-surface);border:1px solid var(--color-border-strong);border-radius:var(--radius-sm);font:inherit}.search-page input,.search-page select{min-height:48px}.search-page form button,.search-page__more,.search-page__owner button{min-height:46px;padding:0 var(--space-5);color:white;background:var(--color-accent);border:0;border-radius:var(--radius-pill);cursor:pointer;font-weight:700}.search-page__count{color:var(--color-text-secondary);font-size:.8125rem}.search-page__results{display:grid}.search-page__results>a{display:block;padding:var(--space-6) 0;color:inherit;border-bottom:1px solid var(--color-border);text-decoration:none;transition:transform var(--duration-fast)}.search-page__results>a:hover{transform:translateX(4px)}.search-page__results>a>div{display:flex;gap:var(--space-3);color:var(--color-accent);font-family:var(--font-mono);font-size:.7rem}.search-page__results h2{margin:var(--space-2) 0;font-family:var(--font-serif);font-size:1.5rem}.search-page__results p,.search-page__results li{color:var(--color-text-secondary);line-height:1.7}.search-page__results ul{padding-left:1.2rem}.search-page__more{margin-top:var(--space-5)}.search-page__empty,.search-page__ai-note{padding:var(--space-6) 0;color:var(--color-text-secondary)}.search-page__ai form{display:grid;gap:var(--space-4)}.search-page__ai label{position:relative;display:grid;gap:var(--space-2);font-size:.8125rem;font-weight:700}.search-page__ai textarea{resize:vertical}.search-page__ai label small{position:absolute;right:var(--space-3);bottom:var(--space-3);color:var(--color-text-secondary)}.search-page__ai form button{justify-self:start}.search-page__ai article{margin-top:var(--space-8);padding:var(--space-6);background:var(--color-surface-soft);border-left:3px solid var(--color-accent)}.search-page__ai article>p{margin:0;font-family:var(--font-serif);font-size:1.15rem;line-height:1.9;white-space:pre-wrap}.search-page__ai footer{display:grid;gap:var(--space-2);margin-top:var(--space-5);padding-top:var(--space-4);border-top:1px solid var(--color-border)}.search-page__ai footer>a{display:grid;color:inherit;text-decoration:none}.search-page__ai footer small{color:var(--color-text-secondary)}.search-page__error{color:var(--color-error)}.search-page__owner{display:flex;align-items:center;justify-content:space-between;gap:var(--space-5);margin-top:var(--space-10);padding:var(--space-5);background:var(--color-surface-soft);border-left:3px solid var(--color-accent)}.search-page__owner div{display:grid;gap:4px}.search-page__owner span,.search-page__owner small{color:var(--color-text-secondary);font-size:.8125rem}.sr-only{position:absolute;width:1px;height:1px;overflow:hidden;clip:rect(0,0,0,0)}
@media(max-width:640px){.search-page{padding:var(--space-5)}.search-page__workbench>form{grid-template-columns:1fr 110px}.search-page__workbench>form button{grid-column:1/-1}.search-page__owner{align-items:stretch;flex-direction:column}}
</style>
