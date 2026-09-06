<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BackendPending from '../components/layout/BackendPending.vue'
import MediaFrame from '../components/media/MediaFrame.vue'
import CommentSection from '../components/comment/CommentSection.vue'
import OwnerToolbar from '../components/owner/OwnerToolbar.vue'
import OwnerDialog from '../components/owner/OwnerDialog.vue'
import OwnerPostEditor from '../components/owner/OwnerPostEditor.vue'
import { useOwnerMode } from '../stores/useOwnerMode'
import { useSpaceRuntime } from '../stores/useSpaceRuntime'

const route = useRoute()
const router = useRouter()
const { isOwner, initializeOwnerMode } = useOwnerMode()
const runtime = useSpaceRuntime()
const post = computed(() => runtime.findTech(route.params.slug))
const editorOpen = ref(false)
const deleteOpen = ref(false)
const formatDate = (value) => new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(value))
const save = async (draft) => { await runtime.updatePost(post.value.id, draft); editorOpen.value = false }
const confirmDelete = async () => { await runtime.deletePost(post.value); router.push('/tech') }
onMounted(async () => {
  await initializeOwnerMode()
  let cached = runtime.findTech(route.params.slug)
  if (isOwner.value && !cached) {
    await runtime.loadOwnerPosts()
    cached = runtime.findTech(route.params.slug)
  }
  if (isOwner.value && cached?.id) await runtime.loadOwnerTech(cached.id)
  else await runtime.loadTechDetail(route.params.slug)
})
</script>

<template>
  <main id="main-content" class="article-page">
    <article v-if="post" class="article-runtime">
      <RouterLink class="article-runtime__back" to="/tech"><span aria-hidden="true">←</span> 返回技术文章</RouterLink>
      <header><div class="article-runtime__kicker"><span>TECH</span><i></i>{{ post.category }}</div><h1>{{ post.title }}</h1><p>{{ post.summary }}</p><div class="article-runtime__meta"><span>{{ post.readingTime }} min read</span><time :datetime="post.createdAt">{{ formatDate(post.createdAt) }}</time></div><OwnerToolbar v-if="isOwner" label="文章管理" @edit="editorOpen = true" @delete="deleteOpen = true" /></header>
      <MediaFrame v-if="post.images?.[0]" class="article-runtime__cover" :media="post.images[0]" eager />
      <div class="article-runtime__body"><p>{{ post.content }}</p></div>
      <footer><span v-for="tag in post.tags" :key="tag">#{{ tag }}</span></footer>
      <CommentSection :post-id="post.id" title="评论" />
    </article>
    <section v-else class="article-page__pending">
      <RouterLink to="/tech"><span aria-hidden="true">←</span> 返回技术文章</RouterLink>
      <BackendPending description="文章正文、目录、标签、点赞和评论都需要从后端数据库读取。" />
    </section>
    <OwnerDialog :open="editorOpen" title="编辑文章" size="large" @close="editorOpen = false"><OwnerPostEditor v-if="post" type="TECH" :post="post" @save="save" @cancel="editorOpen = false" /></OwnerDialog>
    <OwnerDialog :open="deleteOpen" title="确认删除文章" description="删除后文章将不再公开显示。" @close="deleteOpen = false"><div class="delete-confirm"><p>确定删除“{{ post?.title }}”吗？</p><footer><button type="button" @click="deleteOpen = false">取消</button><button class="is-danger" type="button" @click="confirmDelete">确认删除</button></footer></div></OwnerDialog>
  </main>
</template>

<style scoped>
.article-page { padding: var(--space-6); background: rgba(252, 251, 249, 0.88); border: 1px solid var(--color-border); border-radius: var(--radius-xl); }
.article-page__pending { max-width: 900px; }
.article-page__pending > a { display: inline-flex; min-height: 44px; align-items: center; gap: var(--space-2); margin-bottom: var(--space-8); color: var(--color-text-secondary); font-size: var(--font-size-meta); font-weight: 650; text-decoration: none; }
.article-page__pending > a:hover { color: var(--color-accent); }
.article-runtime { max-width: 900px; margin: 0 auto; }
.article-runtime__back { display: inline-flex; min-height: 44px; align-items: center; gap: var(--space-2); margin-bottom: var(--space-8); color: var(--color-text-secondary); font-size: var(--font-size-meta); font-weight: 650; text-decoration: none; }
.article-runtime > header { position: relative; padding-bottom: var(--space-8); border-bottom: 1px solid var(--color-border); }
.article-runtime__kicker { display: flex; align-items: center; gap: var(--space-2); color: var(--color-text-secondary); font-size: .75rem; font-weight: 700; letter-spacing: .1em; }.article-runtime__kicker span { color: var(--color-accent); }.article-runtime__kicker i { width: 24px; height: 1px; background: var(--color-border-strong); }
.article-runtime h1 { max-width: 18ch; margin: var(--space-4) 0 0; font-family: var(--font-serif); font-size: var(--font-size-h1); line-height: 1.18; }
.article-runtime > header > p { max-width: 70ch; color: var(--color-text-secondary); line-height: 1.8; }
.article-runtime__meta { display: flex; gap: var(--space-4); color: var(--color-text-secondary); font-family: var(--font-mono); font-size: .75rem; }
.article-runtime > header :deep(.owner-toolbar) { position: absolute; right: 0; bottom: var(--space-6); }
.article-runtime__cover { margin-top: var(--space-8); max-height: 480px; }
.article-runtime__body { max-width: 72ch; margin: var(--space-12) auto 0; }.article-runtime__body p { font-family: var(--font-serif); font-size: 1.08rem; line-height: 2; white-space: pre-wrap; }
.article-runtime > footer { display: flex; max-width: 72ch; gap: var(--space-2); margin: var(--space-10) auto 0; flex-wrap: wrap; color: var(--color-accent); font-size: .8125rem; }
.delete-confirm p { margin: 0; }.delete-confirm footer { display: flex; justify-content: flex-end; gap: var(--space-3); margin-top: var(--space-6); }.delete-confirm button { min-height: 42px; padding: 0 var(--space-4); background: transparent; border: 1px solid var(--color-border); border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }.delete-confirm .is-danger { color: white; background: var(--color-error); border-color: var(--color-error); }
.article-hero { padding: calc(var(--navbar-height) + var(--space-20)) 0 var(--space-16); background: linear-gradient(155deg, var(--color-surface-soft), var(--color-canvas) 76%); border-bottom: 1px solid var(--color-border); }
.article-hero__inner { max-width: 900px; text-align: center; }
.article-hero__inner > p { display: flex; align-items: center; justify-content: center; gap: var(--space-2); margin: 0; color: var(--color-text-secondary); font-family: var(--font-mono); font-size: var(--font-size-eyebrow); letter-spacing: 0.1em; }
.article-hero__inner > p span { color: var(--color-accent); font-weight: 700; }.article-hero__inner > p i { width: 20px; height: 1px; background: var(--color-border-strong); }
.article-hero h1 { max-width: 820px; margin: var(--space-4) auto 0; font-family: var(--font-serif); font-size: var(--font-size-h1); line-height: 1.18; letter-spacing: -0.025em; text-wrap: balance; }
.article-hero__summary { max-width: 700px; margin: var(--space-5) auto 0; color: var(--color-text-secondary); line-height: 1.85; }
.article-hero__meta,
.article-hero__tags { display: flex; justify-content: center; gap: var(--space-4); margin-top: var(--space-4); flex-wrap: wrap; color: var(--color-text-secondary); font-family: var(--font-mono); font-size: 0.7rem; }
.article-hero__tags { color: var(--color-accent); }
.article-page__cover { max-width: 1000px; max-height: 520px; margin-top: calc(var(--space-10) * -1); box-shadow: var(--shadow-2); }
.article-page__cover :deep(.media-frame__placeholder) { min-height: 360px; }
.article-layout { display: grid; grid-template-columns: minmax(0, 72ch) 220px; justify-content: center; gap: var(--space-16); padding-top: var(--space-16); }
.article-after { max-width: 900px; padding-top: var(--space-10); }
.article-after__tags { display: flex; gap: var(--space-2); margin-top: var(--space-3); flex-wrap: wrap; color: var(--color-accent); font-size: 0.8125rem; }
.related-posts { margin-top: var(--space-20); padding-top: var(--space-10); border-top: 1px solid var(--color-border); }
.related-posts > p { margin: 0; color: var(--color-accent); font-family: var(--font-mono); font-size: var(--font-size-eyebrow); font-weight: 700; letter-spacing: 0.12em; }
.related-posts h2 { margin: var(--space-2) 0 var(--space-6); font-family: var(--font-serif); font-size: var(--font-size-h2); }
.related-posts > div { display: grid; grid-template-columns: repeat(2, 1fr); gap: var(--space-4); }
.article-page__missing { padding: calc(var(--navbar-height) + var(--space-24)) 0; text-align: center; }
.article-page__missing h1 { font-family: var(--font-serif); }.article-page__missing a { color: var(--color-accent); }
@media (max-width: 1099px) { .article-layout { grid-template-columns: minmax(0, 72ch); gap: var(--space-8); } }
@media (max-width: 700px) { .article-page { padding: var(--space-5); border-radius: var(--radius-lg); } .article-hero { padding: var(--space-8) 0 var(--space-12); } .article-page__cover { width: calc(100% - 2 * var(--page-gutter)); margin-top: calc(var(--space-6) * -1); } .article-layout { padding-top: var(--space-10); } .related-posts > div { grid-template-columns: 1fr; } }
</style>
