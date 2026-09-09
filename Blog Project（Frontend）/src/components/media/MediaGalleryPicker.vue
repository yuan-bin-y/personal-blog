<script setup>
import { onBeforeUnmount, ref } from 'vue'
import { uploadMedia } from '../../api/media'
import { apiMessage } from '../../api/request'

const props = defineProps({ modelValue: { type: Array, default: () => [] }, max: { type: Number, default: 9 } })
const emit = defineEmits(['update:modelValue'])
const input = ref(null)
const uploading = ref(false)
const progressText = ref('')
const error = ref('')
const localPreviews = ref([])
const choose = () => input.value?.click()
const clearPreviews = () => { localPreviews.value.forEach((url) => URL.revokeObjectURL(url)); localPreviews.value = [] }
const select = async (event) => {
  const files = [...(event.target.files || [])].slice(0, Math.max(0, props.max - props.modelValue.length))
  if (!files.length) return
  clearPreviews()
  localPreviews.value = files.map((file) => URL.createObjectURL(file))
  uploading.value = true
  error.value = ''
  const next = [...props.modelValue]
  try {
    for (let index = 0; index < files.length; index += 1) {
      progressText.value = `正在上传 ${index + 1} / ${files.length}`
      const asset = await uploadMedia(files[index], 'POST_CONTENT')
      next.push(asset.url)
      emit('update:modelValue', [...next])
    }
  } catch (reason) { error.value = apiMessage(reason) } finally { clearPreviews(); uploading.value = false; progressText.value = ''; event.target.value = '' }
}
const remove = (index) => emit('update:modelValue', props.modelValue.filter((_, current) => current !== index))
onBeforeUnmount(clearPreviews)
</script>

<template>
  <div class="gallery-picker">
    <header><div><strong>说说图片</strong><small>最多 {{ max }} 张，选中后会依次上传并自动加入说说。</small></div><span>{{ uploading ? progressText : `${modelValue.length} / ${max}` }}</span></header>
    <div v-if="modelValue.length || localPreviews.length" class="gallery-picker__grid"><figure v-for="(url,index) in modelValue" :key="url + index"><img :src="url" alt="说说图片预览" /><button type="button" aria-label="移除图片" @click="remove(index)">×</button></figure><figure v-for="url in localPreviews" :key="url" class="is-uploading"><img :src="url" alt="正在上传" /></figure></div>
    <button type="button" :disabled="uploading || modelValue.length >= max" @click="choose">＋ {{ modelValue.length ? '继续添加图片' : '从电脑选择图片' }}</button>
    <p v-if="error">{{ error }}</p>
    <input ref="input" type="file" accept="image/*" multiple @change="select" />
  </div>
</template>

<style scoped>
.gallery-picker{display:grid;gap:var(--space-3);padding:var(--space-4);background:color-mix(in srgb,var(--color-surface-soft) 70%,transparent);border:1px dashed var(--color-border-strong);border-radius:var(--radius-md)}.gallery-picker header{display:flex;justify-content:space-between;gap:var(--space-3)}.gallery-picker header>div{display:grid;gap:3px}.gallery-picker header strong{color:var(--color-text-primary);font-size:.8125rem}.gallery-picker header small,.gallery-picker header>span{color:var(--color-text-secondary);font-size:.7rem;font-weight:500}.gallery-picker__grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:var(--space-2)}.gallery-picker figure{position:relative;aspect-ratio:1;margin:0;overflow:hidden;background:var(--color-surface);border-radius:var(--radius-sm)}.gallery-picker img{width:100%;height:100%;object-fit:cover}.gallery-picker figure>button{position:absolute;top:6px;right:6px;display:grid;width:30px;min-height:30px!important;padding:0!important;place-items:center;color:white;background:rgb(30 35 40/.7);border:1px solid rgb(255 255 255/.4);border-radius:50%;cursor:pointer}.gallery-picker figure.is-uploading::after{position:absolute;inset:0;display:grid;place-items:center;color:white;background:rgb(30 35 40/.45);content:'上传中'}.gallery-picker>button{justify-self:start;min-height:40px!important;padding:0 var(--space-4)!important;color:var(--color-accent)!important;background:transparent!important;border:1px solid var(--color-border-strong)!important}.gallery-picker>p{margin:0;color:var(--color-error);font-size:.75rem}.gallery-picker>input{position:absolute;width:1px;height:1px;opacity:0;pointer-events:none}
@media(max-width:480px){.gallery-picker__grid{grid-template-columns:repeat(2,minmax(0,1fr))}}
</style>
