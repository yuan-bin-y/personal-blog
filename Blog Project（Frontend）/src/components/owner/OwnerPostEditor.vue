<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { getCategories, getTags } from '../../api/taxonomy'
import { autosavePost, importMarkdown } from '../../api/posts'
import { apiMessage } from '../../api/request'
import MediaPicker from '../media/MediaPicker.vue'
import MediaGalleryPicker from '../media/MediaGalleryPicker.vue'

const props = defineProps({
  type: { type: String, required: true, validator: (value) => ['TECH', 'MOMENT'].includes(value) },
  post: { type: Object, default: null },
})
const emit = defineEmits(['save', 'cancel'])

const categories = ref([])
const tags = ref([])
const error = ref('')
const autosaveState = ref('')
const importing = ref(false)
const contentMediaUrl = ref('')
let autosaveTimer
let suppressAutosave = true
const emptyDraft = () => props.type === 'TECH'
  ? { title: '', summary: '', categoryId: '', tagIds: [], cover: '', content: '', contentFormat: 'MARKDOWN', status: 'DRAFT' }
  : { content: '', images: '', status: 'PUBLISHED' }
const draft = reactive(emptyDraft())
const momentImages = computed({
  get: () => draft.images.split(/\r?\n/).map((src) => src.trim()).filter(Boolean),
  set: (urls) => { draft.images = urls.join('\n') },
})

watch(() => props.post, async (post) => {
  suppressAutosave = true
  Object.assign(draft, emptyDraft())
  if (post) {
    if (props.type === 'TECH') Object.assign(draft, post, { categoryId: post.categoryId || '', tagIds: post.tagIds || [], cover: post.cover?.src || post.images?.[0]?.src || '' })
    else Object.assign(draft, post, { images: post.images?.map((item) => item.src).join('\n') || '' })
    // 正式编辑接口只接受 DRAFT/PUBLISHED；编辑已定时内容会先回到草稿，之后可重新设定时间。
    if (post.status === 'SCHEDULED') draft.status = 'DRAFT'
  }
  await nextTick()
  suppressAutosave = false
}, { immediate: true })

const imageInputs = () => draft.images.split(/\r?\n/).map((src) => src.trim()).filter(Boolean)
  .map((src, index) => ({ src, mediaType: 'IMAGE', poster: null, alt: '', width: null, height: null, sortOrder: index }))
const payload = () => props.type === 'TECH' ? {
  title: draft.title, summary: draft.summary, content: draft.content,
  contentFormat: draft.contentFormat, categoryId: String(draft.categoryId || ''), tagIds: [...draft.tagIds],
  cover: draft.cover.trim() ? { src: draft.cover.trim(), mediaType: 'IMAGE', poster: null, alt: draft.title.trim(), width: null, height: null, sortOrder: 0 } : null,
  status: draft.status,
} : { content: draft.content, images: imageInputs(), status: draft.status }

const submit = () => {
  if (!draft.content.trim()) return
  if (props.type === 'TECH' && (!draft.title.trim() || !draft.summary.trim() || !draft.categoryId)) return
  if (props.type === 'TECH') {
    const value = payload()
    emit('save', { ...value, title: value.title.trim(), summary: value.summary.trim(), content: value.content.trim() })
  } else {
    emit('save', { ...payload(), content: draft.content.trim() })
  }
}

watch(draft, () => {
  if (suppressAutosave || !props.post?.id || props.post.version == null) return
  clearTimeout(autosaveTimer)
  autosaveState.value = '有未保存的编辑'
  autosaveTimer = window.setTimeout(async () => {
    autosaveState.value = '正在自动保存…'
    try {
      await autosavePost(props.post.id, { type: props.type, payload: payload(), baseVersion: props.post.version })
      autosaveState.value = '已自动保存草稿'
    } catch (reason) {
      autosaveState.value = `自动保存失败：${apiMessage(reason)}`
    }
  }, 900)
}, { deep: true })

const handleMarkdown = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  importing.value = true
  error.value = ''
  try {
    const result = await importMarkdown(file)
    if (result.title) draft.title = result.title
    draft.content = result.content
    const importedTags = Array.isArray(result.frontMatter?.tags) ? result.frontMatter.tags : []
    if (importedTags.length) draft.tagIds = tags.value.filter((tag) => importedTags.includes(tag.name)).map((tag) => tag.id)
  } catch (reason) { error.value = apiMessage(reason) }
  finally { importing.value = false; event.target.value = '' }
}

const insertContentMedia = (asset) => {
  const alt = asset.originalName?.replace(/\.[^.]+$/, '') || '正文图片'
  const markdown = `![${alt}](${asset.url})`
  draft.content = `${draft.content.trimEnd()}${draft.content.trim() ? '\n\n' : ''}${markdown}\n`
  contentMediaUrl.value = ''
}

onBeforeUnmount(() => clearTimeout(autosaveTimer))

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
    <p class="owner-editor__notice">内容保存到 BinSpace 数据库；图片会先上传到后端配置的 Local / OSS，成功后自动绑定到当前内容。</p>
    <p v-if="post?.status === 'SCHEDULED'" class="owner-editor__notice">这篇内容正在等待定时发布。保存正文后会先回到草稿，请在详情页重新设置发布时间。</p>
    <p v-if="post && autosaveState" class="owner-editor__autosave" aria-live="polite">{{ autosaveState }}</p>
    <p v-if="error" class="owner-editor__notice">{{ error }}</p>
    <template v-if="type === 'TECH'">
      <label class="owner-editor__import">从 Markdown 开始<input type="file" accept=".md,.markdown,text/markdown,text/plain" :disabled="importing" @change="handleMarkdown" /><span>{{ importing ? '正在解析…' : '选择 .md 文件，只导入到当前编辑器，不会直接发布' }}</span></label>
      <label>标题<input v-model="draft.title" required maxlength="120" /></label>
      <label>摘要<textarea v-model="draft.summary" required rows="3" maxlength="320"></textarea></label>
      <div class="owner-editor__split"><label>分类<select v-model="draft.categoryId" required><option value="" disabled>请选择分类</option><option v-for="item in categories" :key="item.id" :value="item.id">{{ item.name }}</option></select></label><label>标签<select v-model="draft.tagIds" multiple><option v-for="item in tags" :key="item.id" :value="item.id">{{ item.name }}</option></select></label></div>
      <MediaPicker v-model="draft.cover" usage-type="POST_COVER" media-type="IMAGE" label="文章封面" hint="选择图片后自动上传并回填，不需要复制 URL。" />
      <label>正文<textarea v-model="draft.content" required rows="12" placeholder="正文将保存到数据库"></textarea></label>
      <MediaPicker v-if="draft.contentFormat === 'MARKDOWN'" v-model="contentMediaUrl" usage-type="POST_CONTENT" media-type="IMAGE" label="插入正文图片" hint="上传成功后自动把 Markdown 图片语法插入正文末尾。" @uploaded="insertContentMedia" />
      <div class="owner-editor__split"><label>正文格式<select v-model="draft.contentFormat"><option value="MARKDOWN">Markdown</option><option value="PLAIN_TEXT">纯文本</option></select></label><label>状态<select v-model="draft.status"><option value="DRAFT">草稿</option><option value="PUBLISHED">发布</option></select></label></div>
    </template>
    <template v-else>
      <label>说说内容<textarea v-model="draft.content" required rows="7" maxlength="2000"></textarea></label>
      <MediaGalleryPicker v-model="momentImages" :max="9" />
      <label>状态<select v-model="draft.status"><option value="DRAFT">草稿</option><option value="PUBLISHED">发布</option></select></label>
    </template>
    <footer><button type="button" @click="$emit('cancel')">取消</button><button class="owner-editor__primary" type="submit">{{ post ? '保存修改' : '保存内容' }}</button></footer>
  </form>
</template>

<style scoped>
.owner-editor { display: grid; gap: var(--space-4); }
.owner-editor__notice { margin: 0; padding: var(--space-3) var(--space-4); color: var(--color-text-secondary); background: var(--color-surface-soft); border-left: 3px solid var(--color-accent); font-size: .8125rem; }
.owner-editor__autosave { margin: calc(var(--space-2) * -1) 0 0; color: var(--color-text-secondary); font-size: .75rem; text-align: right; }
.owner-editor__import { padding: var(--space-3) var(--space-4); background: color-mix(in srgb, var(--color-surface-soft) 60%, transparent); border-block: 1px solid var(--color-border); }
.owner-editor__import input { padding: var(--space-2); background: transparent; border: 0; }
.owner-editor__import span { color: var(--color-text-secondary); font-size: .72rem; font-weight: 400; }
.owner-editor label { display: grid; gap: var(--space-2); color: var(--color-text-secondary); font-size: .8125rem; font-weight: 700; }
.owner-editor input, .owner-editor textarea, .owner-editor select { width: 100%; padding: var(--space-3); color: var(--color-text-primary); background: var(--color-surface); border: 1px solid var(--color-border-strong); border-radius: var(--radius-sm); font: inherit; font-weight: 400; resize: vertical; }
.owner-editor input:focus, .owner-editor textarea:focus { outline: 2px solid var(--color-accent-soft); border-color: var(--color-accent); }
.owner-editor__split { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4); }
.owner-editor footer { display: flex; justify-content: flex-end; gap: var(--space-3); padding-top: var(--space-2); }
.owner-editor footer button { min-height: 44px; padding: 0 var(--space-5); color: var(--color-text-secondary); background: transparent; border: 1px solid var(--color-border); border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }
.owner-editor footer .owner-editor__primary { color: var(--color-text-inverse); background: var(--color-accent); border-color: var(--color-accent); }
@media (max-width: 560px) { .owner-editor__split { grid-template-columns: 1fr; } .owner-editor footer { flex-direction: column-reverse; } .owner-editor footer button { width: 100%; } }
</style>
