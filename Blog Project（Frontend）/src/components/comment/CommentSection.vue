<script setup>
import { computed, ref, watch } from 'vue'
import BackendPending from '../layout/BackendPending.vue'
import OwnerDialog from '../owner/OwnerDialog.vue'
import { useOwnerMode } from '../../stores/useOwnerMode'
import { useSpaceRuntime } from '../../stores/useSpaceRuntime'

const props = defineProps({ postId: { type: String, required: true }, title: { type: String, required: true } })
const { isOwner } = useOwnerMode()
const runtime = useSpaceRuntime()
const items = computed(() => runtime.state.comments.filter((entry) => String(entry.postId) === String(props.postId)))
const replying = ref(null)
const deleting = ref(null)
const replyText = ref('')
const nameOf = (entry) => entry.author?.name || entry.authorName || '访客'
const formatDate = (value) => value ? new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(value)) : ''
const saveReply = async () => { if (!replyText.value.trim()) return; await runtime.replyComment(replying.value.id, replyText.value.trim()); replying.value = null; replyText.value = '' }
const confirmDelete = async () => { await runtime.deleteComment(deleting.value.id); deleting.value = null }
watch(() => props.postId, (id) => { if (id) runtime.loadComments(id) }, { immediate: true })
</script>

<template>
  <section id="comments" class="comments" aria-labelledby="comments-title">
    <header><p>COMMENTS</p><h2 id="comments-title">{{ title }}</h2></header>
    <div v-if="items.length" class="comments__list"><article v-for="item in items" :key="item.id"><span class="comments__avatar" aria-hidden="true">{{ nameOf(item).slice(0, 1) }}</span><div><header><strong>{{ nameOf(item) }}</strong><time :datetime="item.createdAt">{{ formatDate(item.createdAt) }}</time></header><p>{{ item.content }}</p><div v-if="item.ownerReply" class="comments__reply"><strong>玢 回复</strong><p>{{ item.ownerReply.content }}</p></div><div v-if="isOwner" class="comments__owner-actions"><button type="button" @click="replying = item">回复</button><button type="button" @click="deleting = item">删除</button></div></div></article></div>
    <BackendPending v-else compact title="暂无评论" description="当前还没有评论。Visitor 评论提交将在后续功能中开放。" />
    <OwnerDialog :open="Boolean(replying)" title="回复评论" description="回复将保存到数据库。" @close="replying = null"><form class="comments__reply-form" @submit.prevent="saveReply"><label>回复内容<textarea v-model="replyText" required rows="4"></textarea></label><button type="submit">保存回复</button></form></OwnerDialog>
    <OwnerDialog :open="Boolean(deleting)" title="删除评论" description="删除后评论将不再显示。" @close="deleting = null"><div class="comments__confirm"><p>确定删除这条评论吗？</p><button type="button" @click="confirmDelete">确认删除</button></div></OwnerDialog>
  </section>
</template>

<style scoped>
.comments { max-width: 820px; margin: var(--space-20) auto 0; padding-top: var(--space-10); border-top: 1px solid var(--color-border); scroll-margin-top: 96px; }
.comments > header { display: grid; grid-template-columns: 1fr auto; align-items: end; }
.comments > header p { grid-column: 1 / -1; margin: 0 0 var(--space-2); color: var(--color-accent); font-family: var(--font-mono); font-size: var(--font-size-eyebrow); font-weight: 700; letter-spacing: 0.12em; }
.comments h2 { margin: 0; font-family: var(--font-serif); font-size: var(--font-size-h2); }
.comments > header span { color: var(--color-text-secondary); font-size: 0.8125rem; }
.comments__form { margin-top: var(--space-6); padding: var(--space-5); background: var(--color-surface-soft); border: 1px solid var(--color-border); border-radius: var(--radius-lg); }
.comments__demo-note { margin: 0 0 var(--space-4); color: var(--color-text-secondary); font-size: 0.8125rem; }
.comments__fields { display: grid; grid-template-columns: 180px 1fr; gap: var(--space-4); }
.comments label { display: grid; align-content: start; gap: var(--space-2); color: var(--color-text-secondary); font-size: 0.8125rem; font-weight: 650; }
.comments input,
.comments textarea { width: 100%; padding: var(--space-3); color: var(--color-text-primary); background: var(--color-surface); border: 1px solid var(--color-border-strong); border-radius: var(--radius-sm); font-size: 0.9375rem; resize: vertical; }
.comments textarea { min-height: 112px; }
.comments__form button { min-height: 44px; margin-top: var(--space-4); padding: 0 var(--space-5); color: var(--color-text-inverse); background: var(--color-accent); border: 0; border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }
.comments__error { color: var(--color-error); }.comments__success { color: var(--color-success); }
.comments__list { display: grid; gap: var(--space-4); margin-top: var(--space-6); }
.comments__list article { display: grid; grid-template-columns: 42px 1fr; gap: var(--space-3); padding: var(--space-4) 0; border-bottom: 1px solid var(--color-border); }
.comments__avatar { display: grid; width: 42px; height: 42px; place-items: center; background: var(--color-accent-soft); border-radius: 50%; font-family: var(--font-serif); font-weight: 700; }
.comments__list article header { display: flex; align-items: baseline; justify-content: space-between; gap: var(--space-3); }
.comments__list time { color: var(--color-text-secondary); font-family: var(--font-mono); font-size: 0.6875rem; }
.comments__list article p { margin: var(--space-1) 0 0; color: var(--color-text-secondary); }
.comments__reply { margin-top: var(--space-3); padding: var(--space-3); background: var(--color-surface-soft); border-left: 3px solid var(--color-accent); }.comments__reply strong { color: var(--color-accent); font-size: .8125rem; }
.comments__owner-actions { display: flex; gap: var(--space-2); margin-top: var(--space-3); }.comments__owner-actions button { min-height: 34px; padding: 0 var(--space-3); color: var(--color-text-secondary); background: transparent; border: 1px solid var(--color-border); border-radius: var(--radius-pill); cursor: pointer; font-size: .75rem; font-weight: 700; }
.comments__reply-form { display: grid; gap: var(--space-4); }.comments__reply-form label { display: grid; gap: var(--space-2); font-size: .8125rem; font-weight: 700; }.comments__reply-form textarea { width: 100%; padding: var(--space-3); border: 1px solid var(--color-border-strong); border-radius: var(--radius-sm); resize: vertical; }.comments__reply-form button, .comments__confirm button { justify-self: end; min-height: 42px; padding: 0 var(--space-4); color: white; background: var(--color-accent); border: 0; border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }.comments__confirm { display: grid; gap: var(--space-5); }.comments__confirm p { margin: 0; }
@media (max-width: 640px) { .comments__fields { grid-template-columns: 1fr; } .comments { margin-top: var(--space-16); } }
</style>
