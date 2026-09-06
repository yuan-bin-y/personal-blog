<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { getCategories, getTags } from '../../api/taxonomy'
import { apiMessage } from '../../api/request'

const props = defineProps({
  type: { type: String, required: true, validator: (value) => ['TECH', 'MOMENT'].includes(value) },
  post: { type: Object, default: null },
})
const emit = defineEmits(['save', 'cancel'])

const categories = ref([])
const tags = ref([])
const error = ref('')
const emptyDraft = () => props.type === 'TECH'
  ? { title: '', summary: '', categoryId: '', tagIds: [], cover: '', content: '', contentFormat: 'MARKDOWN', status: 'DRAFT' }
  : { content: '', images: '', status: 'PUBLISHED' }
const draft = reactive(emptyDraft())

watch(() => props.post, (post) => {
  Object.assign(draft, emptyDraft())
  if (!post) return
  if (props.type === 'TECH') Object.assign(draft, post, { categoryId: post.categoryId || '', tagIds: post.tagIds || [], cover: post.cover?.src || post.images?.[0]?.src || '' })
  else Object.assign(draft, post, { images: post.images?.map((item) => item.src).join('\n') || '' })
}, { immediate: true })

const submit = () => {
  if (!draft.content.trim()) return
  if (props.type === 'TECH' && (!draft.title.trim() || !draft.summary.trim() || !draft.categoryId)) return
  if (props.type === 'TECH') {
    emit('save', {
      title: draft.title.trim(), summary: draft.summary.trim(), content: draft.content.trim(),
      contentFormat: draft.contentFormat, categoryId: String(draft.categoryId), tagIds: [...draft.tagIds],
      cover: draft.cover.trim() ? { src: draft.cover.trim(), mediaType: 'IMAGE', poster: null, alt: draft.title.trim(), width: null, height: null, sortOrder: 0 } : null,
      status: draft.status,
    })
  } else {
    const images = draft.images.split(/\r?\n/).map((src) => src.trim()).filter(Boolean)
      .map((src, index) => ({ src, mediaType: 'IMAGE', poster: null, alt: '', width: null, height: null, sortOrder: index }))
    emit('save', { content: draft.content.trim(), images, status: draft.status })
  }
}

onMounted(async () => {
  if (props.type !== 'TECH') return
  try {
    const [categoryPage, tagPage] = await Promise.all([
      getCategories({ page: 1, pageSize: 50 }),
      getTags({ page: 1, pageSize: 50 }),
    ])
    categories.value = categoryPage.items
    tags.value = tagPage.items
  } catch (reason) {
    error.value = apiMessage(reason)
  }
})
</script>

<template>
  <form class="owner-editor" @submit.prevent="submit">
    <p class="owner-editor__notice">内容将保存到 BinSpace 数据库。媒体字段当前填写已有 URL。</p>
    <p v-if="error" class="owner-editor__notice">{{ error }}</p>
    <template v-if="type === 'TECH'">
      <label>标题<input v-model="draft.title" required maxlength="120" /></label>
      <label>摘要<textarea v-model="draft.summary" required rows="3" maxlength="320"></textarea></label>
      <div class="owner-editor__split"><label>分类<select v-model="draft.categoryId" required><option value="" disabled>请选择分类</option><option v-for="item in categories" :key="item.id" :value="item.id">{{ item.name }}</option></select></label><label>标签<select v-model="draft.tagIds" multiple><option v-for="item in tags" :key="item.id" :value="item.id">{{ item.name }}</option></select></label></div>
      <label>封面路径<input v-model="draft.cover" placeholder="/media/... 或远程 URL" /></label>
      <label>正文<textarea v-model="draft.content" required rows="12" placeholder="正文将保存到数据库"></textarea></label>
      <div class="owner-editor__split"><label>正文格式<select v-model="draft.contentFormat"><option value="MARKDOWN">Markdown</option><option value="PLAIN_TEXT">纯文本</option></select></label><label>状态<select v-model="draft.status"><option value="DRAFT">草稿</option><option value="PUBLISHED">发布</option></select></label></div>
    </template>
    <template v-else>
      <label>说说内容<textarea v-model="draft.content" required rows="7" maxlength="2000"></textarea></label>
      <label>图片路径<textarea v-model="draft.images" rows="4" placeholder="每行一个 Mock 路径，不做真实上传"></textarea></label>
      <label>状态<select v-model="draft.status"><option value="DRAFT">草稿</option><option value="PUBLISHED">发布</option></select></label>
    </template>
    <footer><button type="button" @click="$emit('cancel')">取消</button><button class="owner-editor__primary" type="submit">{{ post ? '保存修改' : '保存内容' }}</button></footer>
  </form>
</template>

<style scoped>
.owner-editor { display: grid; gap: var(--space-4); }
.owner-editor__notice { margin: 0; padding: var(--space-3) var(--space-4); color: var(--color-text-secondary); background: var(--color-surface-soft); border-left: 3px solid var(--color-accent); font-size: .8125rem; }
.owner-editor label { display: grid; gap: var(--space-2); color: var(--color-text-secondary); font-size: .8125rem; font-weight: 700; }
.owner-editor input, .owner-editor textarea, .owner-editor select { width: 100%; padding: var(--space-3); color: var(--color-text-primary); background: var(--color-surface); border: 1px solid var(--color-border-strong); border-radius: var(--radius-sm); font: inherit; font-weight: 400; resize: vertical; }
.owner-editor input:focus, .owner-editor textarea:focus { outline: 2px solid var(--color-accent-soft); border-color: var(--color-accent); }
.owner-editor__split { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4); }
.owner-editor footer { display: flex; justify-content: flex-end; gap: var(--space-3); padding-top: var(--space-2); }
.owner-editor footer button { min-height: 44px; padding: 0 var(--space-5); color: var(--color-text-secondary); background: transparent; border: 1px solid var(--color-border); border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }
.owner-editor footer .owner-editor__primary { color: var(--color-text-inverse); background: var(--color-accent); border-color: var(--color-accent); }
@media (max-width: 560px) { .owner-editor__split { grid-template-columns: 1fr; } .owner-editor footer { flex-direction: column-reverse; } .owner-editor footer button { width: 100%; } }
</style>
