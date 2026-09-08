<script setup>
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import OwnerDialog from '../owner/OwnerDialog.vue'
import { apiMessage } from '../../api/request'
import { useOwnerMode } from '../../stores/useOwnerMode'
import { useSpaceRuntime } from '../../stores/useSpaceRuntime'

const props = defineProps({ postId: { type: String, required: true }, title: { type: String, required: true } })
const route = useRoute()
const auth = useOwnerMode()
const runtime = useSpaceRuntime()
const items = computed(() => runtime.state.comments.filter((entry) => String(entry.postId) === String(props.postId)))
const composing = ref('')
const editing = ref(null)
const replying = ref(null)
const deleting = ref(null)
const replyText = ref('')
const editText = ref('')
const busy = ref(false)
const error = ref('')
const loginTo = computed(() => ({ path: '/owner-login', query: { redirect: route.fullPath } }))
const nameOf = (entry) => entry.author?.name || entry.authorName || '访客'
const formatDate = (value) => value ? new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(value)) : ''

const submitComment = async () => {
  if (!composing.value.trim()) return
  busy.value = true
  error.value = ''
  try {
    await runtime.createComment(props.postId, composing.value.trim())
    composing.value = ''
  } catch (reason) { error.value = apiMessage(reason) } finally { busy.value = false }
}
const openEdit = (item) => { editing.value = item; editText.value = item.content }
const saveEdit = async () => {
  if (!editText.value.trim()) return
  busy.value = true
  try { await runtime.updateOwnComment(editing.value.id, editText.value.trim()); editing.value = null } catch (reason) { error.value = apiMessage(reason) } finally { busy.value = false }
}
const saveReply = async () => {
  if (!replyText.value.trim()) return
  busy.value = true
  try { await runtime.replyComment(replying.value.id, replyText.value.trim()); replying.value = null; replyText.value = '' } catch (reason) { error.value = apiMessage(reason) } finally { busy.value = false }
}
const confirmDelete = async () => {
  busy.value = true
  try {
    if (auth.isOwner.value) await runtime.deleteComment(deleting.value.id)
    else await runtime.deleteOwnComment(deleting.value.id)
    deleting.value = null
  } catch (reason) { error.value = apiMessage(reason) } finally { busy.value = false }
}
watch(() => props.postId, (id) => { if (id) runtime.loadComments(id) }, { immediate: true })
</script>

<template>
  <section id="comments" class="comments" aria-labelledby="comments-title">
    <header><p>COMMENTS</p><h2 id="comments-title">{{ title }}</h2><span>{{ items.length }} 条</span></header>
    <form v-if="auth.isAuthenticated.value" class="comments__composer" @submit.prevent="submitComment">
      <span class="comments__avatar" aria-hidden="true">{{ (auth.currentUser.value?.displayName || '我').slice(0, 1) }}</span>
      <label><span class="sr-only">评论内容</span><textarea v-model="composing" maxlength="2000" rows="3" placeholder="写下你的想法…" required></textarea><small>{{ composing.length }} / 2000</small></label>
      <button type="submit" :disabled="busy">发表评论</button>
    </form>
    <p v-else class="comments__login-note"><RouterLink :to="loginTo">登录或注册</RouterLink> 后可以参与评论，浏览不受影响。</p>
    <p v-if="error" class="comments__error">{{ error }}</p>
    <div v-if="items.length" class="comments__list">
      <article v-for="item in items" :key="item.id">
        <span class="comments__avatar" aria-hidden="true">{{ nameOf(item).slice(0, 1) }}</span>
        <div>
          <header><strong>{{ nameOf(item) }}</strong><time :datetime="item.createdAt">{{ formatDate(item.createdAt) }}</time></header>
          <p>{{ item.content }}</p>
          <div v-if="item.ownerReply" class="comments__reply"><strong>玢 回复</strong><p>{{ item.ownerReply.content }}</p></div>
          <div v-if="auth.isOwner.value || item.ownedByMe" class="comments__actions">
            <button v-if="item.ownedByMe && !auth.isOwner.value" type="button" @click="openEdit(item)">编辑</button>
            <button v-if="auth.isOwner.value" type="button" @click="replying = item">回复</button>
            <button type="button" @click="deleting = item">删除</button>
          </div>
        </div>
      </article>
    </div>
    <p v-else class="comments__empty">还没有评论，等第一句话。</p>

    <OwnerDialog :open="Boolean(editing)" title="修改自己的评论" description="保存后会更新数据库中的这条评论。" @close="editing = null"><form class="comments__dialog" @submit.prevent="saveEdit"><textarea v-model="editText" maxlength="2000" required rows="5"></textarea><button type="submit" :disabled="busy">保存修改</button></form></OwnerDialog>
    <OwnerDialog :open="Boolean(replying)" title="回复评论" description="回复将保存到数据库。" @close="replying = null"><form class="comments__dialog" @submit.prevent="saveReply"><textarea v-model="replyText" maxlength="2000" required rows="4"></textarea><button type="submit" :disabled="busy">保存回复</button></form></OwnerDialog>
    <OwnerDialog :open="Boolean(deleting)" title="删除评论" description="删除后评论将不再显示。" @close="deleting = null"><div class="comments__confirm"><p>确定删除这条评论吗？</p><button type="button" :disabled="busy" @click="confirmDelete">确认删除</button></div></OwnerDialog>
  </section>
</template>

<style scoped>
.comments { max-width: 820px; margin: var(--space-20) auto 0; padding-top: var(--space-10); border-top: 1px solid var(--color-border); scroll-margin-top: 96px; }
.comments > header { display: grid; grid-template-columns: 1fr auto; align-items: end; }
.comments > header p { grid-column: 1 / -1; margin: 0 0 var(--space-2); color: var(--color-accent); font-family: var(--font-mono); font-size: var(--font-size-eyebrow); font-weight: 700; letter-spacing: .12em; }
.comments h2 { margin: 0; font-family: var(--font-serif); font-size: var(--font-size-h2); }.comments > header span { color: var(--color-text-secondary); font-size: .8125rem; }
.comments__composer { display: grid; grid-template-columns: 42px minmax(0,1fr) auto; align-items: start; gap: var(--space-3); margin-top: var(--space-6); padding: var(--space-4) 0; border-block: 1px solid var(--color-border); }
.comments__composer label { position: relative; }.comments textarea { width: 100%; padding: var(--space-3); color: var(--color-text-primary); background: var(--color-surface); border: 1px solid var(--color-border-strong); border-radius: var(--radius-sm); font: inherit; resize: vertical; }
.comments__composer small { position: absolute; right: var(--space-2); bottom: var(--space-2); color: var(--color-text-secondary); font-size: .6875rem; }
.comments__composer button,.comments__dialog button,.comments__confirm button { min-height: 42px; padding: 0 var(--space-4); color: white; background: var(--color-accent); border: 0; border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }
.comments__avatar { display: grid; width: 42px; height: 42px; place-items: center; background: var(--color-accent-soft); border-radius: 50%; font-family: var(--font-serif); font-weight: 700; }
.comments__login-note,.comments__empty { margin: var(--space-5) 0; padding: var(--space-3) 0; color: var(--color-text-secondary); border-bottom: 1px solid var(--color-border); }.comments__login-note a { color: var(--color-accent); font-weight: 700; }
.comments__error { color: var(--color-error); }.comments__list { display: grid; margin-top: var(--space-4); }
.comments__list article { display: grid; grid-template-columns: 42px 1fr; gap: var(--space-3); padding: var(--space-5) 0; border-bottom: 1px solid var(--color-border); }.comments__list article header { display: flex; justify-content: space-between; gap: var(--space-3); }.comments__list time { color: var(--color-text-secondary); font-family: var(--font-mono); font-size: .75rem; }.comments__list article p { margin: var(--space-2) 0 0; color: var(--color-text-secondary); line-height: 1.75; }
.comments__reply { margin-top: var(--space-3); padding: var(--space-3); background: var(--color-surface-soft); border-left: 3px solid var(--color-accent); }.comments__reply strong { color: var(--color-accent); font-size: .8125rem; }
.comments__actions { display: flex; gap: var(--space-2); margin-top: var(--space-3); }.comments__actions button { min-height: 34px; padding: 0 var(--space-3); color: var(--color-text-secondary); background: transparent; border: 1px solid var(--color-border); border-radius: var(--radius-pill); cursor: pointer; font-size: .75rem; font-weight: 700; }
.comments__dialog,.comments__confirm { display: grid; gap: var(--space-4); }.comments__dialog button,.comments__confirm button { justify-self: end; }.comments__confirm p { margin: 0; }
.sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0,0,0,0); white-space: nowrap; }
@media (max-width: 640px) { .comments { margin-top: var(--space-14); }.comments__composer { grid-template-columns: 36px 1fr; }.comments__composer .comments__avatar { width: 36px; height: 36px; }.comments__composer button { grid-column: 2; justify-self: end; } }
</style>
