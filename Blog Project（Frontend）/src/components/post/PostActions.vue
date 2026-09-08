<script setup>
import { ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import * as likeApi from '../../api/likes'
import { apiMessage } from '../../api/request'
import { useOwnerMode } from '../../stores/useOwnerMode'

const props = defineProps({
  postId: { type: String, required: true },
  likeCount: { type: Number, default: 0 },
  likedByMe: { type: Boolean, default: false },
  commentCount: { type: Number, default: 0 },
  commentsTo: { type: [String, Object], required: true },
})
const emit = defineEmits(['like-change'])
const auth = useOwnerMode()
const route = useRoute()
const router = useRouter()
const liked = ref(props.likedByMe)
const count = ref(props.likeCount)
const busy = ref(false)
const error = ref('')
watch(() => [props.likedByMe, props.likeCount], ([nextLiked, nextCount]) => { liked.value = nextLiked; count.value = nextCount })
const toggleLike = async () => {
  if (!auth.isAuthenticated.value) {
    await router.push({ path: '/owner-login', query: { redirect: route.fullPath } })
    return
  }
  if (busy.value) return
  busy.value = true
  error.value = ''
  try {
    const state = liked.value ? await likeApi.unlikePost(props.postId) : await likeApi.likePost(props.postId)
    liked.value = state.liked
    count.value = state.likeCount
    emit('like-change', state)
  } catch (reason) { error.value = apiMessage(reason) } finally { busy.value = false }
}
</script>

<template>
  <div class="post-actions">
    <button class="post-actions__button" :class="{ 'post-actions__button--liked': liked }" type="button" :disabled="busy" :aria-pressed="liked" :aria-label="liked ? `取消点赞，当前 ${count} 个赞` : `点赞，当前 ${count} 个赞`" @click="toggleLike"><span aria-hidden="true">{{ liked ? '♥' : '♡' }}</span>{{ count }}</button>
    <RouterLink class="post-actions__button" :to="commentsTo" :aria-label="`查看 ${commentCount} 条评论`"><span aria-hidden="true">↳</span>评论 {{ commentCount }}</RouterLink>
    <small v-if="error" class="post-actions__error">{{ error }}</small>
  </div>
</template>

<style scoped>
.post-actions { display:flex;align-items:center;gap:var(--space-2);flex-wrap:wrap }.post-actions__button{display:inline-flex;align-items:center;min-height:44px;gap:6px;padding:0 var(--space-3);color:var(--color-text-secondary);background:transparent;border:0;border-radius:var(--radius-pill);cursor:pointer;font-size:var(--font-size-meta);font-weight:550;text-decoration:none;transition:color var(--duration-fast),background-color var(--duration-fast),transform var(--duration-fast)}.post-actions__button:hover{color:var(--color-accent);background:var(--color-accent-soft);transform:translateY(-1px)}.post-actions__button--liked{color:var(--color-accent)}.post-actions__button:disabled{cursor:wait;opacity:.65}.post-actions__button span{font-size:1.05rem}.post-actions__error{flex-basis:100%;color:var(--color-error);font-size:.7rem}
</style>
