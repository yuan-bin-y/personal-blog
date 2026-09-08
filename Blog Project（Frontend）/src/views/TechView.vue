<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '../components/layout/PageHeader.vue'
import BackendPending from '../components/layout/BackendPending.vue'
import TechPostCard from '../components/post/TechPostCard.vue'
import OwnerDialog from '../components/owner/OwnerDialog.vue'
import OwnerPostEditor from '../components/owner/OwnerPostEditor.vue'
import OwnerBatchBar from '../components/owner/OwnerBatchBar.vue'
import OwnerTrash from '../components/owner/OwnerTrash.vue'
import { apiMessage } from '../api/request'
import { useOwnerMode } from '../stores/useOwnerMode'
import { useSpaceRuntime } from '../stores/useSpaceRuntime'

const route = useRoute()
const router = useRouter()
const { isOwner, initializeOwnerMode } = useOwnerMode()
const { techPosts, createTech, updatePost, deletePost, loadTech, loadOwnerPosts } = useSpaceRuntime()
const editorOpen = ref(false)
const editing = ref(null)
const deleting = ref(null)
const trashOpen = ref(false)
const selected = ref(new Set())
const batchBusy = ref(false)
const batchMessage = ref('')
const category = ref('ALL')
const categories = computed(() => ['ALL', ...new Set(techPosts.value.map((post) => post.category).filter(Boolean))])
const visiblePosts = computed(() => category.value === 'ALL' ? techPosts.value : techPosts.value.filter((post) => post.category === category.value))
const selectedPosts = computed(() => visiblePosts.value.filter((post) => selected.value.has(post.id)))
watch(() => route.query.compose, (value) => { if (isOwner.value && value === '1') { editing.value = null; editorOpen.value = true } }, { immediate: true })
const closeEditor = () => { editorOpen.value = false; editing.value = null; if (route.query.compose) router.replace({ path: '/tech' }) }
const startEdit = (post) => { editing.value = post; editorOpen.value = true }
const save = async (draft) => { editing.value ? await updatePost(editing.value.id, draft) : await createTech(draft); closeEditor() }
const confirmDelete = async () => { await deletePost(deleting.value); deleting.value = null }
const toggle = (id) => {
  const next = new Set(selected.value)
  next.has(id) ? next.delete(id) : next.add(id)
  selected.value = next
}
const toggleAll = () => { selected.value = selected.value.size === visiblePosts.value.length ? new Set() : new Set(visiblePosts.value.map((post) => post.id)) }
const runBatch = async (action) => {
  if (!selectedPosts.value.length) return
  if (action === 'delete' && !window.confirm(`将选中的 ${selectedPosts.value.length} 篇文章移到回收站？`)) return
  batchBusy.value = true
  batchMessage.value = ''
  try {
    const result = action === 'publish'
      ? await useSpaceRuntime().batchPublishPosts(selectedPosts.value)
      : await useSpaceRuntime().batchDeletePosts(selectedPosts.value)
    batchMessage.value = `成功 ${result.succeededIds.length} 项${result.failed.length ? `，失败 ${result.failed.length} 项` : ''}`
    selected.value = new Set(result.failed.map((item) => item.postId))
  } catch (reason) { batchMessage.value = apiMessage(reason) }
  finally { batchBusy.value = false }
}
onMounted(async () => {
  await initializeOwnerMode()
  if (isOwner.value) await loadOwnerPosts()
  else await loadTech()
})
</script>

<template>
  <main id="main-content" class="tech-page">
    <PageHeader eyebrow="TECH NOTES" title="写给未来自己的技术笔记" description="尽量说明问题从哪里来、为什么这样解决，也诚实记录方案没有覆盖的边界。" />
    <section class="tech-index" aria-labelledby="tech-index-title">
      <header><h2 id="tech-index-title">文章索引</h2><button v-if="isOwner" class="tech-index__create" type="button" @click="editorOpen = true">＋ 写文章</button></header>
      <nav v-if="categories.length > 1" class="tech-index__filters" aria-label="文章分类"><button v-for="item in categories" :key="item" :class="{ 'is-active': category === item }" type="button" @click="category = item">{{ item }}</button></nav>
      <OwnerBatchBar v-if="isOwner && visiblePosts.length" :selected="selected.size" :total="visiblePosts.length" :busy="batchBusy" :message="batchMessage" @toggle-all="toggleAll" @publish="runBatch('publish')" @delete="runBatch('delete')" @trash="trashOpen = true" />
      <div v-if="visiblePosts.length" class="tech-index__list"><div v-for="(post, index) in visiblePosts" :key="post.id" class="owner-selectable"><label v-if="isOwner" class="owner-selectable__check"><input type="checkbox" :checked="selected.has(post.id)" @change="toggle(post.id)" /><span>选择</span></label><TechPostCard :post="post" :variant="index === 0 ? 'featured' : index % 3 === 1 ? 'row' : 'editorial'" @edit="startEdit" @delete="deleting = $event" /></div></div>
      <BackendPending v-else title="暂无文章" :description="isOwner ? '数据库中还没有文章，可以写下第一篇草稿。' : '数据库中还没有已发布的技术文章。'" />
    </section>
    <OwnerDialog :open="editorOpen" :title="editing ? '编辑文章' : '写文章'" size="large" @close="closeEditor"><OwnerPostEditor type="TECH" :post="editing" @save="save" @cancel="closeEditor" /></OwnerDialog>
    <OwnerDialog :open="Boolean(deleting)" title="确认删除文章" description="删除后文章将不再公开显示。" @close="deleting = null"><div class="delete-confirm"><p>确定删除“{{ deleting?.title }}”吗？</p><footer><button type="button" @click="deleting = null">取消</button><button class="is-danger" type="button" @click="confirmDelete">确认删除</button></footer></div></OwnerDialog>
    <OwnerDialog :open="trashOpen" title="技术文章回收站" @close="trashOpen = false"><OwnerTrash v-if="trashOpen" type="TECH" @restored="loadOwnerPosts" /></OwnerDialog>
  </main>
</template>

<style scoped>
.tech-page { padding: var(--space-6); background: rgba(252, 251, 249, 0.84); border: 1px solid var(--color-border); border-radius: var(--radius-xl); }
.tech-index { max-width: 1000px; padding-top: var(--space-8); }
.tech-index > header { display: flex; align-items: baseline; justify-content: space-between; gap: var(--space-4); }
.tech-index__create { min-height: 42px; padding: 0 var(--space-5); color: var(--color-text-inverse); background: var(--color-accent); border: 0; border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }
.tech-index h2 { margin: 0; font-family: var(--font-serif); font-size: var(--font-size-h2); }
.tech-index > header p { color: var(--color-text-secondary); font-family: var(--font-mono); font-size: 0.6875rem; }
.tech-index__filters { display: flex; gap: var(--space-2); margin-top: var(--space-6); padding-bottom: var(--space-2); overflow-x: auto; }
.tech-index__filters button { min-height: 40px; padding: 0 var(--space-4); color: var(--color-text-secondary); background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-pill); cursor: pointer; white-space: nowrap; }
.tech-index__filters button.is-active { color: var(--color-text-inverse); background: var(--color-accent); border-color: var(--color-accent); }
.tech-index__featured { margin-top: var(--space-8); }
.tech-index__featured > p { margin: 0 0 var(--space-3); color: var(--color-accent); font-family: var(--font-mono); font-size: var(--font-size-eyebrow); font-weight: 700; letter-spacing: 0.12em; }
.tech-index__list { display: grid; margin-top: var(--space-6); }
.owner-selectable { position: relative; }.owner-selectable__check { position: absolute; z-index: 3; top: var(--space-3); left: var(--space-3); display: inline-flex; align-items: center; gap: var(--space-1); padding: 5px 9px; color: var(--color-text-secondary); background: color-mix(in srgb, var(--color-surface) 88%, transparent); border: 1px solid var(--color-border); border-radius: var(--radius-pill); font-size: .68rem; cursor: pointer; }.owner-selectable__check input { accent-color: var(--color-accent); }.owner-selectable__check span { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0 0 0 0); }
.delete-confirm p { margin: 0; }.delete-confirm footer { display: flex; justify-content: flex-end; gap: var(--space-3); margin-top: var(--space-6); }.delete-confirm button { min-height: 42px; padding: 0 var(--space-4); background: transparent; border: 1px solid var(--color-border); border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }.delete-confirm .is-danger { color: white; background: var(--color-error); border-color: var(--color-error); }
@media (max-width: 600px) {
  .tech-page { padding: var(--space-5); border-radius: var(--radius-lg); }
  .tech-index { padding-top: var(--space-6); }
  .tech-index__featured { margin-top: var(--space-6); }
}
</style>
