<script setup>
import { onMounted, ref } from 'vue'
import * as mediaApi from '../../api/media'
import { apiMessage } from '../../api/request'

const items = ref([])
const usageType = ref('POST_CONTENT')
const mediaType = ref('')
const busy = ref(false)
const progress = ref(0)
const error = ref('')
const copied = ref('')
const load = async () => {
  busy.value = true; error.value = ''
  try { items.value = (await mediaApi.getMedia({ mediaType: mediaType.value || undefined, page: 1, pageSize: 50 })).items } catch (reason) { error.value = apiMessage(reason) } finally { busy.value = false }
}
const upload = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  busy.value = true; progress.value = 0; error.value = ''
  try {
    const item = await mediaApi.uploadMedia(file, usageType.value, ({ loaded, total }) => { progress.value = total ? Math.round(loaded / total * 100) : 0 })
    items.value.unshift(item); event.target.value = ''
  } catch (reason) { error.value = apiMessage(reason) } finally { busy.value = false; progress.value = 0 }
}
const remove = async (item) => {
  if (!window.confirm(`确认删除媒体“${item.originalName}”吗？`)) return
  try { await mediaApi.deleteMedia(item.id); items.value = items.value.filter((entry) => entry.id !== item.id) } catch (reason) { error.value = apiMessage(reason) }
}
const copyUrl = async (item) => { await navigator.clipboard.writeText(item.url); copied.value = item.id; window.setTimeout(() => { copied.value = '' }, 1600) }
const readableSize = (size) => size >= 1024 * 1024 ? `${(size / 1024 / 1024).toFixed(1)} MB` : `${Math.ceil(size / 1024)} KB`
onMounted(load)
</script>

<template>
  <div class="media-library">
    <div class="media-library__upload"><label>用途<select v-model="usageType"><option value="AVATAR">头像</option><option value="HERO">Hero</option><option value="POST_COVER">文章封面</option><option value="POST_CONTENT">正文 / 说说</option><option value="MUSIC">音乐</option><option value="PAGE_BACKGROUND">页面背景</option></select></label><label class="media-library__file"><span>{{ busy && progress ? `上传中 ${progress}%` : '选择文件上传' }}</span><input type="file" accept="image/*,video/*,audio/*" :disabled="busy" @change="upload" /></label></div>
    <div class="media-library__filter"><label>筛选<select v-model="mediaType" @change="load"><option value="">全部媒体</option><option value="IMAGE">图片</option><option value="VIDEO">视频</option><option value="AUDIO">音频</option></select></label><button type="button" @click="load">刷新</button></div>
    <p v-if="error" class="media-library__error">{{ error }}</p>
    <div v-if="items.length" class="media-library__grid"><article v-for="item in items" :key="item.id"><div class="media-library__preview"><img v-if="item.mediaType === 'IMAGE'" :src="item.url" :alt="item.originalName" /><video v-else-if="item.mediaType === 'VIDEO'" :src="item.url" muted preload="metadata"></video><span v-else>♪</span></div><div><strong>{{ item.originalName }}</strong><small>{{ item.usageType }} · {{ readableSize(item.size) }}</small></div><footer><button type="button" @click="copyUrl(item)">{{ copied === item.id ? '已复制' : '复制 URL' }}</button><button type="button" @click="remove(item)">删除</button></footer></article></div>
    <p v-else-if="!busy" class="media-library__empty">媒体库目前为空，上传后的真实资源会显示在这里。</p>
  </div>
</template>

<style scoped>
.media-library{display:grid;gap:var(--space-5)}.media-library__upload,.media-library__filter{display:flex;align-items:end;gap:var(--space-3);flex-wrap:wrap}.media-library label{display:grid;gap:var(--space-2);color:var(--color-text-secondary);font-size:.8125rem;font-weight:700}.media-library select{min-height:44px;padding:0 var(--space-3);color:var(--color-text-primary);background:var(--color-surface);border:1px solid var(--color-border-strong);border-radius:var(--radius-sm)}.media-library__file{position:relative;display:flex!important;min-height:44px;align-items:center;padding:0 var(--space-5);color:white!important;background:var(--color-accent);border-radius:var(--radius-pill);cursor:pointer}.media-library__file input{position:absolute;width:1px;height:1px;opacity:0}.media-library__filter{padding-bottom:var(--space-4);border-bottom:1px solid var(--color-border)}.media-library__filter button,.media-library footer button{min-height:36px;padding:0 var(--space-3);color:var(--color-text-secondary);background:transparent;border:1px solid var(--color-border);border-radius:var(--radius-pill);cursor:pointer;font-size:.75rem;font-weight:700}.media-library__grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:var(--space-4)}.media-library article{display:grid;align-content:start;overflow:hidden;background:var(--color-surface-soft);border:1px solid var(--color-border);border-radius:var(--radius-md)}.media-library__preview{display:grid;aspect-ratio:16/10;place-items:center;overflow:hidden;background:#e9e7e2;font-size:2rem}.media-library__preview img,.media-library__preview video{width:100%;height:100%;object-fit:cover}.media-library article>div:nth-child(2){display:grid;gap:4px;padding:var(--space-3)}.media-library article strong{overflow:hidden;font-size:.8125rem;text-overflow:ellipsis;white-space:nowrap}.media-library article small{color:var(--color-text-secondary);font-size:.6875rem}.media-library footer{display:flex;gap:var(--space-2);padding:0 var(--space-3) var(--space-3)}.media-library__error{color:var(--color-error)}.media-library__empty{color:var(--color-text-secondary)}
@media(max-width:760px){.media-library__grid{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:420px){.media-library__grid{grid-template-columns:1fr}}
</style>
