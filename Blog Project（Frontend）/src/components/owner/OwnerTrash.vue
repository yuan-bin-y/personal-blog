<script setup>
import { onMounted, ref } from 'vue'
import { apiMessage } from '../../api/request'
import { useSpaceRuntime } from '../../stores/useSpaceRuntime'

const props = defineProps({ type: { type: String, required: true } })
const emit = defineEmits(['restored'])
const runtime = useSpaceRuntime()
const items = ref([])
const loading = ref(false)
const error = ref('')

const load = async () => {
  loading.value = true
  error.value = ''
  try { items.value = (await runtime.loadTrash(props.type)).items }
  catch (reason) { error.value = apiMessage(reason) }
  finally { loading.value = false }
}
const restore = async (post) => {
  loading.value = true
  error.value = ''
  try {
    await runtime.restoreTrashedPost(post)
    items.value = items.value.filter((item) => item.id !== post.id)
    emit('restored')
  } catch (reason) { error.value = apiMessage(reason) }
  finally { loading.value = false }
}
const label = (post) => post.type === 'TECH' ? post.title : post.content
onMounted(load)
</script>

<template>
  <section class="trash-list">
    <p class="trash-list__note">删除内容会先留在这里；恢复后统一回到草稿状态。</p>
    <p v-if="error" class="trash-list__error" role="alert">{{ error }}</p>
    <p v-if="loading && !items.length">正在翻找回收站…</p>
    <div v-else-if="items.length">
      <article v-for="post in items" :key="post.id">
        <span>{{ post.type }}</span>
        <p>{{ label(post) }}</p>
        <button type="button" :disabled="loading" @click="restore(post)">恢复为草稿</button>
      </article>
    </div>
    <p v-else>这里是空的。</p>
  </section>
</template>

<style scoped>
.trash-list__note { margin: 0 0 var(--space-5); color: var(--color-text-secondary); line-height: 1.7; }
.trash-list__error { color: var(--color-error); }
.trash-list article { display: grid; grid-template-columns: auto minmax(0, 1fr) auto; align-items: center; gap: var(--space-3); padding: var(--space-4) 0; border-bottom: 1px solid var(--color-border); }
.trash-list article > span { color: var(--color-accent); font-family: var(--font-mono); font-size: .6875rem; font-weight: 700; }
.trash-list article p { margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.trash-list button { min-height: 38px; padding: 0 var(--space-3); color: var(--color-accent); background: transparent; border: 1px solid var(--color-border); border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }
@media (max-width: 560px) { .trash-list article { grid-template-columns: auto 1fr; } .trash-list article button { grid-column: 1 / -1; justify-self: start; } }
</style>
