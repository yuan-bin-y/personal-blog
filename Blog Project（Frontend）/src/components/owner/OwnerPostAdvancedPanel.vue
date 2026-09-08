<script setup>
import { computed, ref, watch } from 'vue'
import { apiMessage } from '../../api/request'
import { useSpaceRuntime } from '../../stores/useSpaceRuntime'

const props = defineProps({ post: { type: Object, required: true } })
const emit = defineEmits(['updated', 'slug-updated'])
const runtime = useSpaceRuntime()
const open = ref(false)
const loading = ref(false)
const error = ref('')
const notice = ref('')
const publishAt = ref('')
const slug = ref('')
const versions = ref([])
const selectedVersion = ref(null)

const statusLabel = computed(() => ({ DRAFT: '草稿', SCHEDULED: '等待发布', PUBLISHED: '已发布' }[props.post.status] || props.post.status))
const localDateTime = (value) => {
  if (!value) return ''
  const date = new Date(value)
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset())
  return date.toISOString().slice(0, 16)
}
watch(() => props.post, (post) => {
  slug.value = post.slug || ''
  publishAt.value = post.status === 'SCHEDULED' ? localDateTime(post.publishedAt) : ''
}, { immediate: true, deep: true })

const run = async (action, success) => {
  loading.value = true
  error.value = ''
  notice.value = ''
  try {
    const updated = await action()
    if (updated) emit('updated', updated)
    notice.value = success
    return updated
  } catch (reason) {
    error.value = apiMessage(reason)
    return null
  } finally { loading.value = false }
}
const schedule = () => {
  if (!publishAt.value) { error.value = '请选择未来的发布时间'; return }
  run(() => runtime.schedulePost(props.post, new Date(publishAt.value).toISOString()), '已设置定时发布')
}
const cancelSchedule = () => run(() => runtime.cancelScheduledPost(props.post), '已取消定时发布，内容回到草稿')
const loadVersions = async () => {
  if (!open.value) open.value = true
  loading.value = true
  error.value = ''
  try { versions.value = (await runtime.getPostVersions(props.post.id, { page: 1, pageSize: 20 })).items }
  catch (reason) { error.value = apiMessage(reason) }
  finally { loading.value = false }
}
const inspectVersion = async (version) => {
  loading.value = true
  error.value = ''
  try { selectedVersion.value = await runtime.getPostVersion(props.post.id, version.id) }
  catch (reason) { error.value = apiMessage(reason) }
  finally { loading.value = false }
}
const restoreVersion = async (version) => {
  if (!window.confirm(`恢复到版本 ${version.versionNo}？当前内容会先自动保留为一个新版本。`)) return
  const updated = await run(() => runtime.restorePostVersion(props.post, version.id), `已恢复版本 ${version.versionNo}`)
  if (updated) await loadVersions()
}
const saveSlug = async () => {
  const previous = props.post.slug
  const updated = await run(() => runtime.updateTechSlug(props.post, slug.value.trim()), '文章地址已更新')
  if (updated && updated.slug !== previous) emit('slug-updated', updated.slug)
}
const formatTime = (value) => new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit',
}).format(new Date(value))
const snapshotPreview = computed(() => {
  const snapshot = selectedVersion.value?.snapshot
  if (!snapshot) return ''
  return snapshot.title || snapshot.summary || snapshot.content || ''
})
</script>

<template>
  <aside class="advanced-panel" aria-label="内容高级管理">
    <button class="advanced-panel__toggle" type="button" :aria-expanded="open" @click="open = !open">
      <span><i aria-hidden="true"></i>发布与版本</span>
      <small>{{ statusLabel }} · v{{ post.version }}</small>
      <b aria-hidden="true">{{ open ? '−' : '＋' }}</b>
    </button>
    <div v-if="open" class="advanced-panel__body">
      <p v-if="error" class="advanced-panel__error" role="alert">{{ error }}</p>
      <p v-if="notice" class="advanced-panel__notice" aria-live="polite">{{ notice }}</p>

      <section>
        <header><strong>定时发布</strong><span>服务器会在到达时间后自动发布</span></header>
        <div class="advanced-panel__schedule">
          <input v-model="publishAt" type="datetime-local" :disabled="loading" aria-label="计划发布时间" />
          <button type="button" :disabled="loading || !publishAt" @click="schedule">{{ post.status === 'SCHEDULED' ? '修改时间' : '设置定时' }}</button>
          <button v-if="post.status === 'SCHEDULED'" class="is-quiet" type="button" :disabled="loading" @click="cancelSchedule">取消定时</button>
        </div>
      </section>

      <section v-if="post.type === 'TECH'">
        <header><strong>文章地址</strong><span>修改后旧地址将不再访问</span></header>
        <div class="advanced-panel__slug"><span>/tech/</span><input v-model="slug" maxlength="180" /><button type="button" :disabled="loading || !slug || slug === post.slug" @click="saveSlug">保存</button></div>
      </section>

      <section>
        <header><strong>历史版本</strong><button class="text-button" type="button" :disabled="loading" @click="loadVersions">{{ versions.length ? '刷新' : '查看版本' }}</button></header>
        <div v-if="versions.length" class="version-list">
          <article v-for="version in versions" :key="version.id">
            <button type="button" @click="inspectVersion(version)"><b>v{{ version.versionNo }}</b><span>{{ version.summary }}</span><time>{{ formatTime(version.createdAt) }}</time></button>
            <button class="version-list__restore" type="button" :disabled="loading" @click="restoreVersion(version)">恢复</button>
          </article>
        </div>
        <p v-else-if="!loading" class="advanced-panel__empty">还没有历史版本。第一次正式修改后会出现在这里。</p>
        <div v-if="selectedVersion" class="version-preview">
          <span>版本 {{ selectedVersion.versionNo }} 预览</span>
          <p>{{ snapshotPreview }}</p>
        </div>
      </section>
    </div>
  </aside>
</template>

<style scoped>
.advanced-panel { margin: var(--space-5) 0 var(--space-6); color: var(--color-text-primary); background: color-mix(in srgb, var(--color-surface-soft) 58%, transparent); border-block: 1px solid var(--color-border); }
.advanced-panel__toggle { display: grid; width: 100%; min-height: 54px; grid-template-columns: 1fr auto auto; align-items: center; gap: var(--space-3); padding: var(--space-3) var(--space-4); color: inherit; text-align: left; background: transparent; border: 0; cursor: pointer; }
.advanced-panel__toggle > span { display: flex; align-items: center; gap: var(--space-2); font-size: .8125rem; font-weight: 750; }.advanced-panel__toggle i { width: 7px; height: 7px; background: var(--color-accent); border-radius: 50%; }.advanced-panel__toggle small { color: var(--color-text-secondary); }.advanced-panel__toggle b { font-size: 1.1rem; }
.advanced-panel__body { display: grid; gap: var(--space-5); padding: 0 var(--space-4) var(--space-5); }
.advanced-panel__body section { padding-top: var(--space-4); border-top: 1px solid var(--color-border); }.advanced-panel__body section > header { display: flex; align-items: baseline; justify-content: space-between; gap: var(--space-3); margin-bottom: var(--space-3); }.advanced-panel__body section header strong { font-family: var(--font-serif); }.advanced-panel__body section header span { color: var(--color-text-secondary); font-size: .75rem; }
.advanced-panel__error,.advanced-panel__notice { margin: 0; padding: var(--space-3); font-size: .8125rem; }.advanced-panel__error { color: var(--color-error); background: color-mix(in srgb, var(--color-error) 8%, transparent); }.advanced-panel__notice { color: var(--color-success); background: color-mix(in srgb, var(--color-success) 8%, transparent); }
.advanced-panel__schedule,.advanced-panel__slug { display: flex; align-items: center; gap: var(--space-2); }.advanced-panel input { min-height: 42px; min-width: 0; padding: 0 var(--space-3); color: inherit; background: var(--color-surface); border: 1px solid var(--color-border-strong); border-radius: var(--radius-sm); }.advanced-panel button:not(.advanced-panel__toggle):not(.text-button) { min-height: 40px; padding: 0 var(--space-3); color: var(--color-text-inverse); background: var(--color-accent); border: 1px solid var(--color-accent); border-radius: var(--radius-pill); cursor: pointer; font-size: .75rem; font-weight: 700; }.advanced-panel button.is-quiet { color: var(--color-text-secondary); background: transparent; border-color: var(--color-border); }.advanced-panel button:disabled { cursor: not-allowed; opacity: .45; }
.advanced-panel__slug span { color: var(--color-text-secondary); font-family: var(--font-mono); font-size: .75rem; }.advanced-panel__slug input { flex: 1; }
.text-button { color: var(--color-accent); background: transparent; border: 0; cursor: pointer; font-weight: 700; }
.version-list article { display: grid; grid-template-columns: minmax(0,1fr) auto; align-items: center; border-bottom: 1px solid var(--color-border); }.version-list article > button:first-child { display: grid; min-width: 0; grid-template-columns: auto 1fr auto; gap: var(--space-3); padding: var(--space-3) 0; color: inherit; text-align: left; background: transparent; border: 0; cursor: pointer; }.version-list article span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.version-list time { color: var(--color-text-secondary); font-size: .7rem; }.version-list .version-list__restore { min-height: 34px; color: var(--color-accent); background: transparent; border: 0; }
.advanced-panel__empty { margin: 0; color: var(--color-text-secondary); font-size: .8125rem; }.version-preview { margin-top: var(--space-3); padding: var(--space-3); background: var(--color-surface); border-left: 3px solid var(--color-apricot); }.version-preview span { color: var(--color-text-secondary); font-size: .7rem; }.version-preview p { display: -webkit-box; margin: var(--space-2) 0 0; overflow: hidden; line-height: 1.7; -webkit-box-orient: vertical; -webkit-line-clamp: 4; }
@media (max-width: 600px) { .advanced-panel__toggle { grid-template-columns: 1fr auto; }.advanced-panel__toggle small { grid-column: 1; }.advanced-panel__toggle b { grid-column: 2; grid-row: 1 / span 2; }.advanced-panel__schedule,.advanced-panel__slug { align-items: stretch; flex-direction: column; }.advanced-panel__schedule input,.advanced-panel__slug input { width: 100%; }.advanced-panel__body section > header { align-items: flex-start; flex-direction: column; }.version-list article > button:first-child { grid-template-columns: auto 1fr; }.version-list time { grid-column: 2; } }
</style>
