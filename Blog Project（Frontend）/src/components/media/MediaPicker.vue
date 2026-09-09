<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { uploadMedia } from '../../api/media'
import { apiMessage } from '../../api/request'

const props = defineProps({
  modelValue: { type: String, default: '' },
  usageType: { type: String, required: true },
  mediaType: { type: String, default: 'IMAGE', validator: (value) => ['IMAGE', 'VIDEO', 'AUDIO'].includes(value) },
  label: { type: String, default: '媒体文件' },
  hint: { type: String, default: '' },
})
const emit = defineEmits(['update:modelValue', 'uploaded'])
const input = ref(null)
const uploading = ref(false)
const progress = ref(0)
const error = ref('')
const localPreview = ref('')
const preview = computed(() => localPreview.value || props.modelValue)
const accept = computed(() => ({ IMAGE: 'image/*', VIDEO: 'video/*', AUDIO: 'audio/*' })[props.mediaType])

const clearLocalPreview = () => {
  if (localPreview.value) URL.revokeObjectURL(localPreview.value)
  localPreview.value = ''
}
const choose = () => input.value?.click()
const select = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  clearLocalPreview()
  localPreview.value = URL.createObjectURL(file)
  uploading.value = true
  progress.value = 0
  error.value = ''
  try {
    const asset = await uploadMedia(file, props.usageType, ({ loaded, total }) => {
      progress.value = total ? Math.round(loaded / total * 100) : 0
    })
    emit('update:modelValue', asset.url)
    emit('uploaded', asset)
    clearLocalPreview()
  } catch (reason) {
    error.value = apiMessage(reason)
  } finally {
    uploading.value = false
    progress.value = 0
    event.target.value = ''
  }
}
const remove = () => {
  clearLocalPreview()
  emit('update:modelValue', '')
}
watch(() => props.modelValue, () => { if (!uploading.value) clearLocalPreview() })
onBeforeUnmount(clearLocalPreview)
</script>

<template>
  <div class="media-picker" :class="`media-picker--${mediaType.toLowerCase()}`">
    <div class="media-picker__heading"><div><strong>{{ label }}</strong><small v-if="hint">{{ hint }}</small></div><span v-if="uploading">上传中 {{ progress }}%</span><span v-else-if="modelValue">已上传，等待保存</span></div>
    <div class="media-picker__stage" :class="{ 'is-empty': !preview }">
      <img v-if="preview && mediaType === 'IMAGE'" :src="preview" alt="当前媒体预览" />
      <video v-else-if="preview && mediaType === 'VIDEO'" :src="preview" muted controls preload="metadata"></video>
      <audio v-else-if="preview && mediaType === 'AUDIO'" :src="preview" controls preload="metadata"></audio>
      <div v-else class="media-picker__empty"><span aria-hidden="true">{{ mediaType === 'IMAGE' ? '▧' : mediaType === 'VIDEO' ? '▷' : '♪' }}</span><p>从电脑选择{{ mediaType === 'IMAGE' ? '图片' : mediaType === 'VIDEO' ? '视频' : '音频' }}</p></div>
      <div v-if="uploading" class="media-picker__progress"><i :style="{ width: `${progress}%` }"></i></div>
    </div>
    <div class="media-picker__actions"><button type="button" :disabled="uploading" @click="choose">{{ preview ? '更换文件' : '选择文件' }}</button><button v-if="modelValue" class="is-quiet" type="button" :disabled="uploading" @click="remove">移除当前配置</button></div>
    <p v-if="error" class="media-picker__error">{{ error }}</p>
    <details><summary>手动填写已有 URL</summary><input :value="modelValue" placeholder="https://..." @input="emit('update:modelValue', $event.target.value)" /></details>
    <input ref="input" class="media-picker__native" type="file" :accept="accept" @change="select" />
  </div>
</template>

<style scoped>
.media-picker{display:grid;gap:var(--space-3);padding:var(--space-4);background:color-mix(in srgb,var(--color-surface-soft) 70%,transparent);border:1px dashed var(--color-border-strong);border-radius:var(--radius-md)}.media-picker__heading{display:flex;align-items:start;justify-content:space-between;gap:var(--space-3)}.media-picker__heading>div{display:grid;gap:3px}.media-picker__heading strong{color:var(--color-text-primary);font-size:.8125rem}.media-picker__heading small,.media-picker__heading>span{color:var(--color-text-secondary);font-size:.7rem;font-weight:500}.media-picker__stage{position:relative;display:grid;min-height:140px;place-items:center;overflow:hidden;background:rgb(255 255 255/.45);border:1px solid var(--color-border);border-radius:var(--radius-sm)}.media-picker--video .media-picker__stage{aspect-ratio:16/7}.media-picker--audio .media-picker__stage{min-height:82px}.media-picker__stage img,.media-picker__stage video{width:100%;height:100%;max-height:280px;object-fit:cover}.media-picker__stage audio{width:min(100% - 32px,520px)}.media-picker__empty{display:grid;justify-items:center;gap:var(--space-2);color:var(--color-text-secondary)}.media-picker__empty span{font-size:1.7rem;opacity:.6}.media-picker__empty p{margin:0;font-size:.8125rem}.media-picker__progress{position:absolute;right:0;bottom:0;left:0;height:4px;background:var(--color-border)}.media-picker__progress i{display:block;height:100%;background:var(--color-accent);transition:width .15s linear}.media-picker__actions{display:flex;gap:var(--space-2);flex-wrap:wrap}.media-picker__actions button{min-height:38px!important;padding:0 var(--space-4)!important;color:var(--color-text-inverse)!important;background:var(--color-accent)!important;border:1px solid var(--color-accent)!important;border-radius:var(--radius-pill)!important;cursor:pointer}.media-picker__actions button:disabled{cursor:wait;opacity:.6}.media-picker__actions .is-quiet{color:var(--color-text-secondary)!important;background:transparent!important;border:1px solid var(--color-border)!important}.media-picker__error{margin:0;color:var(--color-error);font-size:.75rem}.media-picker details{color:var(--color-text-secondary);font-size:.72rem}.media-picker summary{cursor:pointer}.media-picker details input{width:100%;min-height:42px;margin-top:var(--space-2);padding:0 var(--space-3);color:var(--color-text-primary);background:var(--color-surface);border:1px solid var(--color-border-strong);border-radius:var(--radius-sm);font:inherit}.media-picker__native{position:absolute!important;width:1px!important;height:1px!important;overflow:hidden;opacity:0;pointer-events:none}
@media(max-width:560px){.media-picker{padding:var(--space-3)}.media-picker__stage{min-height:112px}.media-picker__heading{display:grid}}
</style>
