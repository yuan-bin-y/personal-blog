<script setup>
import { computed, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { apiMessage } from '../../api/request'
import { useOwnerMode } from '../../stores/useOwnerMode'
import { useSpaceRuntime } from '../../stores/useSpaceRuntime'

const route = useRoute()
const auth = useOwnerMode()
const runtime = useSpaceRuntime()
const content = ref('')
const busy = ref(false)
const error = ref('')
const success = ref('')
const loginTo = computed(() => ({ path: '/owner-login', query: { redirect: route.fullPath } }))

const submit = async () => {
  if (!content.value.trim()) return
  busy.value = true
  error.value = ''
  success.value = ''
  try {
    await runtime.createGuestbook(content.value.trim())
    content.value = ''
    success.value = '留言已经留在这里了。'
  } catch (reason) { error.value = apiMessage(reason) } finally { busy.value = false }
}
</script>

<template>
  <form v-if="auth.isAuthenticated.value" class="guestbook-form" @submit.prevent="submit">
    <p>以 <strong>{{ auth.currentUser.value?.displayName }}</strong> 的名字，给空间主人留句话。</p>
    <label>留言内容<textarea v-model="content" maxlength="2000" rows="6" placeholder="来都来了，写点什么吧…" required></textarea><small>{{ content.length }} / 2000</small></label>
    <p v-if="error" class="guestbook-form__error">{{ error }}</p><p v-if="success" class="guestbook-form__success">{{ success }}</p>
    <button type="submit" :disabled="busy">{{ busy ? '正在留下…' : '留下留言' }}</button>
  </form>
  <div v-else class="guestbook-form guestbook-form--login"><p>浏览留言不需要账号。想给玢留句话时，再登录就好。</p><RouterLink :to="loginTo">登录或注册 →</RouterLink></div>
</template>

<style scoped>
.guestbook-form { padding: var(--space-5) 0; border-block: 1px solid var(--color-border); }.guestbook-form > p { margin: 0 0 var(--space-4); color: var(--color-text-secondary); }.guestbook-form label { position: relative; display: grid; gap: var(--space-2); color: var(--color-text-secondary); font-size: .8125rem; font-weight: 700; }.guestbook-form textarea { width: 100%; min-height: 144px; padding: var(--space-3); color: var(--color-text-primary); background: rgb(255 255 255 / .55); border: 1px solid var(--color-border-strong); border-radius: var(--radius-sm); font: inherit; resize: vertical; }.guestbook-form label small { position: absolute; right: var(--space-3); bottom: var(--space-2); color: var(--color-text-secondary); font-family: var(--font-mono); font-size: .6875rem; }.guestbook-form button,.guestbook-form a { display: inline-flex; min-height: 44px; align-items: center; margin-top: var(--space-4); padding: 0 var(--space-5); color: white; background: var(--color-accent); border: 0; border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; text-decoration: none; }.guestbook-form button:disabled { opacity: .6; }.guestbook-form__error { color: var(--color-error) !important; }.guestbook-form__success { color: var(--color-success) !important; }.guestbook-form--login { padding: var(--space-5); background: rgb(255 255 255 / .24); border: 1px dashed var(--color-border-strong); }.guestbook-form--login p { margin: 0; }
</style>
