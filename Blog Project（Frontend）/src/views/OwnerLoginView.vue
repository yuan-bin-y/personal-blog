<script setup>
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useOwnerMode } from '../stores/useOwnerMode'
import { useSpaceRuntime } from '../stores/useSpaceRuntime'

const router = useRouter()
const { state, loginOwner } = useOwnerMode()
const { loadOwnerPosts } = useSpaceRuntime()
const credentials = reactive({ username: '', password: '' })
const submitting = ref(false)

const submit = async () => {
  submitting.value = true
  try {
    await loginOwner(credentials)
    await loadOwnerPosts()
    await router.replace('/')
  } catch {
    // 具体错误由集中身份状态展示。
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main id="main-content" class="owner-login">
    <section>
      <p>OWNER ACCESS</p>
      <h1>回到自己的空间</h1>
      <span>登录后仍使用同一套 BinSpace 页面，只会多出发布、编辑与设置能力。</span>
      <form @submit.prevent="submit">
        <label>用户名<input v-model="credentials.username" autocomplete="username" required /></label>
        <label>密码<input v-model="credentials.password" type="password" autocomplete="current-password" required /></label>
        <p v-if="state.error" class="owner-login__error">{{ state.error }}</p>
        <button type="submit" :disabled="submitting">{{ submitting ? '正在登录…' : '进入主人模式' }}</button>
      </form>
      <RouterLink to="/">← 返回空间</RouterLink>
    </section>
  </main>
</template>

<style scoped>
.owner-login { display: grid; min-height: 68vh; place-items: center; padding: var(--space-8); background: rgb(252 251 249 / .9); border: 1px solid var(--color-border); border-radius: var(--radius-xl); }
.owner-login > section { width: min(100%, 460px); }
.owner-login > section > p { margin: 0 0 var(--space-2); color: var(--color-accent); font-family: var(--font-mono); font-size: var(--font-size-eyebrow); font-weight: 700; letter-spacing: .12em; }
.owner-login h1 { margin: 0; font-family: var(--font-serif); font-size: clamp(2rem, 5vw, 3.2rem); }
.owner-login > section > span { display: block; margin-top: var(--space-4); color: var(--color-text-secondary); line-height: 1.8; }
.owner-login form { display: grid; gap: var(--space-4); margin-top: var(--space-8); }
.owner-login label { display: grid; gap: var(--space-2); color: var(--color-text-secondary); font-size: var(--font-size-meta); font-weight: 700; }
.owner-login input { min-height: 48px; padding: 0 var(--space-4); background: var(--color-surface); border: 1px solid var(--color-border-strong); border-radius: var(--radius-sm); font: inherit; }
.owner-login button { min-height: 48px; color: var(--color-text-inverse); background: var(--color-accent); border: 0; border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }
.owner-login button:disabled { cursor: wait; opacity: .65; }
.owner-login__error { margin: 0; color: var(--color-error); }
.owner-login a { display: inline-flex; min-height: 44px; align-items: center; margin-top: var(--space-5); color: var(--color-text-secondary); text-decoration: none; }
@media (max-width: 600px) { .owner-login { padding: var(--space-5); } }
</style>
