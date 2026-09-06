<script setup>
import { ref } from 'vue'
import OwnerDialog from '../owner/OwnerDialog.vue'
import { useOwnerMode } from '../../stores/useOwnerMode'
import { useSpaceRuntime } from '../../stores/useSpaceRuntime'
defineProps({ entry: { type: Object, required: true }, index: { type: Number, required: true } })
const formatDate = (value) => new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }).format(new Date(value))
const { isOwner } = useOwnerMode()
const runtime = useSpaceRuntime()
const replyOpen = ref(false)
const deleteOpen = ref(false)
const replyText = ref('')
const saveReply = async (entry) => { if (!replyText.value.trim()) return; await runtime.replyGuestbook(entry.id, replyText.value.trim()); replyText.value = ''; replyOpen.value = false }
const remove = async (entry) => { await runtime.deleteGuestbook(entry.id); deleteOpen.value = false }
</script>

<template>
  <article class="guestbook-entry">
    <span class="guestbook-entry__number">#{{ String(index + 1).padStart(2, '0') }}</span>
    <div class="guestbook-entry__author"><span aria-hidden="true">{{ entry.author.name.slice(0, 1) }}</span><div><strong>{{ entry.author.name }}</strong><time :datetime="entry.createdAt">{{ formatDate(entry.createdAt) }}</time></div></div>
    <p>{{ entry.content }}</p>
    <div v-if="entry.ownerReply" class="guestbook-entry__reply"><strong>玢 回复</strong><p>{{ entry.ownerReply.content }}</p><time :datetime="entry.ownerReply.createdAt">{{ formatDate(entry.ownerReply.createdAt) }}</time></div>
    <div v-if="isOwner" class="guestbook-entry__owner"><button type="button" @click="replyOpen = true">回复</button><button type="button" @click="deleteOpen = true">删除</button></div>
    <OwnerDialog :open="replyOpen" title="回复留言" description="回复将保存到数据库。" @close="replyOpen = false"><form class="guestbook-entry__reply-form" @submit.prevent="saveReply(entry)"><label>回复内容<textarea v-model="replyText" required rows="4"></textarea></label><button type="submit">保存回复</button></form></OwnerDialog>
    <OwnerDialog :open="deleteOpen" title="删除留言" description="删除后留言将不再显示。" @close="deleteOpen = false"><div class="guestbook-entry__confirm"><p>确定删除这条空间留言吗？</p><button type="button" @click="remove(entry)">确认删除</button></div></OwnerDialog>
  </article>
</template>

<style scoped>
.guestbook-entry { position: relative; padding: var(--space-6) var(--space-2); border-bottom: 1px solid var(--color-border); }
.guestbook-entry:nth-child(3n + 1)::before { position: absolute; top: var(--space-5); bottom: var(--space-5); left: calc(var(--space-2) * -1); width: 3px; background: var(--color-apricot); border-radius: var(--radius-pill); content: ''; }
.guestbook-entry__number { position: absolute; top: var(--space-4); right: var(--space-4); color: var(--color-border-strong); font-family: var(--font-mono); font-size: 0.6875rem; }
.guestbook-entry__author { display: flex; align-items: center; gap: var(--space-3); }
.guestbook-entry__author > span { display: grid; width: 42px; height: 42px; place-items: center; background: var(--color-accent-soft); border-radius: 50%; font-family: var(--font-serif); font-weight: 700; }
.guestbook-entry__author div { display: grid; }.guestbook-entry__author time,
.guestbook-entry__reply time { color: var(--color-text-secondary); font-family: var(--font-mono); font-size: 0.75rem; }
.guestbook-entry > p { margin: var(--space-4) 0 0; color: var(--color-text-secondary); line-height: 1.75; }
.guestbook-entry__reply { margin-top: var(--space-4); padding: var(--space-3) var(--space-4); background: var(--color-surface-soft); border-left: 3px solid var(--color-apricot); border-radius: 0 var(--radius-sm) var(--radius-sm) 0; }
.guestbook-entry__reply strong { color: var(--color-accent); font-size: 0.8125rem; }.guestbook-entry__reply p { margin: var(--space-1) 0; font-size: 0.875rem; }
.guestbook-entry__owner { display: flex; gap: var(--space-2); margin-top: var(--space-3); }.guestbook-entry__owner button { min-height: 34px; padding: 0 var(--space-3); color: var(--color-text-secondary); background: transparent; border: 1px solid var(--color-border); border-radius: var(--radius-pill); cursor: pointer; font-size: .75rem; font-weight: 700; }
.guestbook-entry__reply-form { display: grid; gap: var(--space-4); }.guestbook-entry__reply-form label { display: grid; gap: var(--space-2); font-size: .8125rem; font-weight: 700; }.guestbook-entry__reply-form textarea { width: 100%; padding: var(--space-3); border: 1px solid var(--color-border-strong); border-radius: var(--radius-sm); resize: vertical; }.guestbook-entry__reply-form button, .guestbook-entry__confirm button { justify-self: end; min-height: 42px; padding: 0 var(--space-4); color: white; background: var(--color-accent); border: 0; border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }.guestbook-entry__confirm { display: grid; gap: var(--space-5); }.guestbook-entry__confirm p { margin: 0; }
@media (max-width: 600px) {
  .guestbook-entry { padding: var(--space-5) 0; }
  .guestbook-entry__number { right: 0; }
  .guestbook-entry:nth-child(3n + 1)::before { left: -8px; }
}
</style>
