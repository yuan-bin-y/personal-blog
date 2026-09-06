<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BackendPending from '../components/layout/BackendPending.vue'
import MomentPostCard from '../components/post/MomentPostCard.vue'
import OwnerDialog from '../components/owner/OwnerDialog.vue'
import OwnerPostEditor from '../components/owner/OwnerPostEditor.vue'
import { useOwnerMode } from '../stores/useOwnerMode'
import { useSpaceRuntime } from '../stores/useSpaceRuntime'

const route = useRoute()
const router = useRouter()
const { isOwner, initializeOwnerMode } = useOwnerMode()
const { momentPosts, createMoment, updatePost, deletePost, loadMoments, loadOwnerPosts } = useSpaceRuntime()
const editorOpen = ref(false)
const editing = ref(null)
const deleting = ref(null)
const groups = computed(() => {
  const result = new Map()
  ;[...momentPosts.value].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).forEach((post) => {
    const date = new Date(post.createdAt)
    const key = `${date.getFullYear()}-${date.getMonth()}`
    if (!result.has(key)) result.set(key, { label: new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: 'long' }).format(date), posts: [] })
    result.get(key).posts.push(post)
  })
  return [...result.values()]
})
watch(() => route.query.compose, (value) => { if (isOwner.value && value === '1') { editing.value = null; editorOpen.value = true } }, { immediate: true })
const closeEditor = () => { editorOpen.value = false; editing.value = null; if (route.query.compose) router.replace({ path: '/moments' }) }
const startEdit = (post) => { editing.value = post; editorOpen.value = true }
const save = async (draft) => { editing.value ? await updatePost(editing.value.id, draft) : await createMoment(draft); closeEditor() }
const confirmDelete = async () => { await deletePost(deleting.value); deleting.value = null }
onMounted(async () => {
  await initializeOwnerMode()
  if (isOwner.value) await loadOwnerPosts()
  else await loadMoments()
})
</script>

<template>
  <main id="main-content" class="moments-page">
    <header class="moments-intro">
      <div><p>MOMENTS</p><h1>最近留下的一些日常。</h1></div>
      <button v-if="isOwner" type="button" @click="editorOpen = true">＋ 发说说</button>
      <span>有些是几句话，有些是一张图，都是当时想记住的时刻。</span>
    </header>
    <div class="moments-timeline">
      <section v-for="group in groups" :key="group.label" class="moments-month"><header><span aria-hidden="true"></span><h2>{{ group.label }}</h2><small>{{ group.posts.length }} MOMENTS</small></header><div class="moments-month__list"><MomentPostCard v-for="post in group.posts" :key="post.id" :post="post" presentation="timeline" @edit="startEdit" @delete="deleting = $event" /></div></section>
      <BackendPending v-if="!groups.length" title="暂无说说" :description="isOwner ? '数据库中还没有说说，可以发布第一条动态。' : '数据库中还没有已发布的说说。'" />
    </div>
    <OwnerDialog :open="editorOpen" :title="editing ? '编辑说说' : '发说说'" @close="closeEditor"><OwnerPostEditor type="MOMENT" :post="editing" @save="save" @cancel="closeEditor" /></OwnerDialog>
    <OwnerDialog :open="Boolean(deleting)" title="确认删除说说" description="删除后说说将不再公开显示。" @close="deleting = null"><div class="delete-confirm"><p>确定删除这条说说吗？</p><footer><button type="button" @click="deleting = null">取消</button><button class="is-danger" type="button" @click="confirmDelete">确认删除</button></footer></div></OwnerDialog>
  </main>
</template>

<style scoped>
.moments-page {
  --color-accent: #4d7690;
  --color-accent-hover: #3d6178;
  --color-accent-soft: #deebf1;
  --color-border: #cddde4;
  --color-border-strong: #aabfc9;
  --color-text-primary: #283943;
  --color-text-secondary: #586f7a;
  position: relative;
  padding: var(--space-6);
  overflow: hidden;
  background: linear-gradient(180deg, rgb(247 251 252 / 0.5), rgb(246 250 251 / 0.64));
  border: 1px solid rgb(205 221 228 / 0.72);
  border-radius: var(--radius-xl);
  box-shadow: 0 14px 42px rgb(64 101 119 / 0.12);
  backdrop-filter: saturate(0.96);
}
.moments-page::after { position: absolute; inset: 0; z-index: 0; border-radius: inherit; box-shadow: inset 0 0 64px rgb(255 255 255 / 0.12); content: ''; pointer-events: none; }
.moments-page > * { position: relative; z-index: 1; }
.moments-intro { display: grid; grid-template-columns: 1fr auto; align-items: end; gap: var(--space-4); max-width: 900px; }
.moments-intro p { margin: 0 0 var(--space-3); color: var(--color-accent); font-family: var(--font-mono); font-size: var(--font-size-eyebrow); font-weight: 700; letter-spacing: 0.12em; }
.moments-intro h1 { max-width: 720px; margin: 0; font-family: var(--font-serif); font-size: clamp(2rem, 4vw, 3.25rem); line-height: 1.2; letter-spacing: -0.025em; }
.moments-intro > span { grid-column: 1 / -1; display: block; max-width: 620px; color: var(--color-text-secondary); line-height: 1.8; }
.moments-intro > button { min-height: 42px; padding: 0 var(--space-5); color: var(--color-text-inverse); background: var(--color-accent); border: 0; border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }
.moments-timeline { max-width: 900px; padding-top: var(--space-12); }
.moments-month + .moments-month { margin-top: var(--space-20); }
.moments-month > header { display: grid; grid-template-columns: 14px 1fr auto; align-items: center; gap: var(--space-3); margin-bottom: var(--space-6); }
.moments-month > header > span { width: 10px; height: 10px; background: var(--color-apricot); border-radius: 50%; box-shadow: 0 0 0 5px rgba(139, 159, 168, 0.16); }
.moments-month h2 { margin: 0; font-family: var(--font-mono); font-size: 0.8rem; letter-spacing: 0.12em; }
.moments-month small { color: var(--color-text-secondary); font-family: var(--font-mono); font-size: 0.625rem; }
.moments-month__list { position: relative; display: grid; gap: var(--space-3); padding-left: var(--space-8); }
.moments-month__list::before { position: absolute; top: 0; bottom: 0; left: 5px; width: 1px; background: var(--color-border); content: ''; }
.delete-confirm p { margin: 0; }.delete-confirm footer { display: flex; justify-content: flex-end; gap: var(--space-3); margin-top: var(--space-6); }.delete-confirm button { min-height: 42px; padding: 0 var(--space-4); background: transparent; border: 1px solid var(--color-border); border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }.delete-confirm .is-danger { color: white; background: var(--color-error); border-color: var(--color-error); }
@media (max-width: 600px) {
  .moments-page { padding: var(--space-5); border-radius: var(--radius-lg); }
  .moments-intro h1 { font-size: 2rem; }
  .moments-intro { grid-template-columns: 1fr; align-items: start; }.moments-intro > span { grid-column: auto; }.moments-intro > button { justify-self: start; }
  .moments-timeline { padding-top: var(--space-10); }
  .moments-month + .moments-month { margin-top: var(--space-12); }
  .moments-month__list { gap: var(--space-2); padding-left: 0; }
  .moments-month__list::before { display: none; }
}
</style>
