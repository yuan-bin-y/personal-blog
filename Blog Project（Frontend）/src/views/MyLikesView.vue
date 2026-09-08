<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { getMyLikes } from '../api/likes'
import { apiMessage } from '../api/request'
import { useOwnerMode } from '../stores/useOwnerMode'

const route = useRoute()
const auth = useOwnerMode()
const items = ref([])
const error = ref('')
const hrefOf = (post) => post.type === 'TECH' ? `/tech/${post.slug}` : `/moments/${post.id}`
const load = async () => {
  if (!auth.isAuthenticated.value) return
  try { items.value = (await getMyLikes({ page: 1, pageSize: 50 })).items } catch (reason) { error.value = apiMessage(reason) }
}
onMounted(load)
</script>

<template>
  <main id="main-content" class="likes-page">
    <header><p>MY NOTES</p><h1>喜欢过的内容</h1><span>这里来自你的账号与数据库，取消点赞后会从列表中移除。</span></header>
    <div v-if="auth.isAuthenticated.value && items.length" class="likes-page__list"><RouterLink v-for="item in items" :key="item.id" :to="hrefOf(item)"><span>{{ item.type }}</span><div><h2>{{ item.title || '一条说说' }}</h2><p>{{ item.summary || item.content }}</p></div><b>♡ {{ item.likeCount }}</b></RouterLink></div>
    <p v-else-if="auth.isAuthenticated.value" class="likes-page__empty">{{ error || '还没有点赞过内容。' }}</p>
    <section v-else class="likes-page__empty"><p>登录后才能查看自己的点赞。</p><RouterLink :to="{ path: '/owner-login', query: { redirect: route.fullPath } }">登录或注册 →</RouterLink></section>
  </main>
</template>

<style scoped>
.likes-page{padding:var(--space-8);background:rgb(252 251 249/.94);border:1px solid var(--color-border);border-radius:var(--radius-xl)}.likes-page>header p{margin:0 0 var(--space-2);color:var(--color-accent);font-family:var(--font-mono);font-size:var(--font-size-eyebrow);font-weight:700;letter-spacing:.12em}.likes-page h1{margin:0;font-family:var(--font-serif);font-size:clamp(2rem,5vw,3.4rem)}.likes-page>header span{display:block;margin-top:var(--space-3);color:var(--color-text-secondary)}.likes-page__list{display:grid;margin-top:var(--space-8)}.likes-page__list>a{display:grid;grid-template-columns:70px minmax(0,1fr) auto;gap:var(--space-4);padding:var(--space-5) 0;color:inherit;border-bottom:1px solid var(--color-border);text-decoration:none}.likes-page__list>a>span{color:var(--color-accent);font-family:var(--font-mono);font-size:.75rem;font-weight:700}.likes-page__list h2{margin:0;font-family:var(--font-serif);font-size:1.3rem}.likes-page__list p{margin:var(--space-2) 0 0;color:var(--color-text-secondary);line-height:1.6}.likes-page__list b{color:var(--color-text-secondary);font-size:.8125rem}.likes-page__empty{margin-top:var(--space-8);padding:var(--space-5) 0;color:var(--color-text-secondary);border-top:1px solid var(--color-border)}.likes-page__empty a{color:var(--color-accent);font-weight:700}
@media(max-width:600px){.likes-page{padding:var(--space-5)}.likes-page__list>a{grid-template-columns:1fr}.likes-page__list>a>b{justify-self:start}}
</style>
