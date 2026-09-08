<script setup>
import { ref } from 'vue'
import OwnerDialog from '../owner/OwnerDialog.vue'
import { apiMessage } from '../../api/request'
import { useOwnerMode } from '../../stores/useOwnerMode'
import { useSpaceRuntime } from '../../stores/useSpaceRuntime'

const props = defineProps({ entry: { type: Object, required: true }, index: { type: Number, required: true } })
const formatDate = (value) => new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }).format(new Date(value))
const auth = useOwnerMode()
const runtime = useSpaceRuntime()
const dialog = ref('')
const text = ref('')
const busy = ref(false)
const error = ref('')
const open = (type) => { dialog.value = type; text.value = type === 'edit' ? props.entry.content : '' }
const close = () => { dialog.value = ''; text.value = ''; error.value = '' }
const save = async () => {
  if (!text.value.trim()) return
  busy.value = true
  try {
    if (dialog.value === 'reply') await runtime.replyGuestbook(props.entry.id, text.value.trim())
    else await runtime.updateOwnGuestbook(props.entry.id, text.value.trim())
    close()
  } catch (reason) { error.value = apiMessage(reason) } finally { busy.value = false }
}
const remove = async () => {
  busy.value = true
  try {
    if (auth.isOwner.value) await runtime.deleteGuestbook(props.entry.id)
    else await runtime.deleteOwnGuestbook(props.entry.id)
    close()
  } catch (reason) { error.value = apiMessage(reason) } finally { busy.value = false }
}
</script>

<template>
  <article class="guestbook-entry">
    <span class="guestbook-entry__number">#{{ String(index + 1).padStart(2, '0') }}</span>
    <div class="guestbook-entry__author"><span aria-hidden="true">{{ (entry.author?.name || '访').slice(0, 1) }}</span><div><strong>{{ entry.author?.name || '访客' }}</strong><time :datetime="entry.createdAt">{{ formatDate(entry.createdAt) }}</time></div></div>
    <p>{{ entry.content }}</p>
    <div v-if="entry.ownerReply" class="guestbook-entry__reply"><strong>玢 回复</strong><p>{{ entry.ownerReply.content }}</p><time :datetime="entry.ownerReply.createdAt">{{ formatDate(entry.ownerReply.createdAt) }}</time></div>
    <div v-if="auth.isOwner.value || entry.ownedByMe" class="guestbook-entry__actions">
      <button v-if="entry.ownedByMe && !auth.isOwner.value" type="button" @click="open('edit')">编辑</button>
      <button v-if="auth.isOwner.value" type="button" @click="open('reply')">回复</button>
      <button type="button" @click="open('delete')">删除</button>
    </div>
    <OwnerDialog :open="dialog === 'reply'" title="回复留言" description="回复将保存到数据库。" @close="close"><form class="guestbook-entry__dialog" @submit.prevent="save"><textarea v-model="text" maxlength="2000" required rows="4"></textarea><p v-if="error">{{ error }}</p><button type="submit" :disabled="busy">保存回复</button></form></OwnerDialog>
    <OwnerDialog :open="dialog === 'edit'" title="修改自己的留言" description="保存后会更新数据库中的留言。" @close="close"><form class="guestbook-entry__dialog" @submit.prevent="save"><textarea v-model="text" maxlength="2000" required rows="5"></textarea><p v-if="error">{{ error }}</p><button type="submit" :disabled="busy">保存修改</button></form></OwnerDialog>
    <OwnerDialog :open="dialog === 'delete'" title="删除留言" description="删除后留言将不再显示。" @close="close"><div class="guestbook-entry__confirm"><p>确定删除这条空间留言吗？</p><p v-if="error">{{ error }}</p><button type="button" :disabled="busy" @click="remove">确认删除</button></div></OwnerDialog>
  </article>
</template>

<style scoped>
.guestbook-entry { position: relative; padding: var(--space-6) var(--space-2); border-bottom: 1px solid var(--color-border); }.guestbook-entry:nth-child(3n + 1)::before { position: absolute; top: var(--space-5); bottom: var(--space-5); left: calc(var(--space-2) * -1); width: 3px; background: var(--color-accent); border-radius: var(--radius-pill); content: ''; }.guestbook-entry__number { position: absolute; top: var(--space-4); right: var(--space-4); color: var(--color-border-strong); font-family: var(--font-mono); font-size: .6875rem; }.guestbook-entry__author { display: flex; align-items: center; gap: var(--space-3); }.guestbook-entry__author > span { display: grid; width: 42px; height: 42px; place-items: center; background: var(--color-accent-soft); border-radius: 50%; font-family: var(--font-serif); font-weight: 700; }.guestbook-entry__author div { display: grid; }.guestbook-entry time { color: var(--color-text-secondary); font-family: var(--font-mono); font-size: .75rem; }.guestbook-entry > p { margin: var(--space-4) 0 0; color: var(--color-text-secondary); line-height: 1.75; }.guestbook-entry__reply { margin-top: var(--space-4); padding: var(--space-3) var(--space-4); background: var(--color-surface-soft); border-left: 3px solid var(--color-accent); }.guestbook-entry__reply strong { color: var(--color-accent); font-size: .8125rem; }.guestbook-entry__reply p { margin: var(--space-1) 0; }.guestbook-entry__actions { display: flex; gap: var(--space-2); margin-top: var(--space-3); }.guestbook-entry__actions button { min-height: 34px; padding: 0 var(--space-3); color: var(--color-text-secondary); background: transparent; border: 1px solid var(--color-border); border-radius: var(--radius-pill); cursor: pointer; font-size: .75rem; font-weight: 700; }.guestbook-entry__dialog,.guestbook-entry__confirm { display: grid; gap: var(--space-4); }.guestbook-entry__dialog textarea { width: 100%; padding: var(--space-3); border: 1px solid var(--color-border-strong); border-radius: var(--radius-sm); font: inherit; resize: vertical; }.guestbook-entry__dialog button,.guestbook-entry__confirm button { justify-self: end; min-height: 42px; padding: 0 var(--space-4); color: white; background: var(--color-accent); border: 0; border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }.guestbook-entry__dialog p,.guestbook-entry__confirm p { margin: 0; }.guestbook-entry__dialog p { color: var(--color-error); }
@media (max-width:600px){.guestbook-entry{padding:var(--space-5) 0}.guestbook-entry__number{right:0}}
</style>
