<script setup>
import { computed, reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useOwnerMode } from '../stores/useOwnerMode'
import { useSpaceRuntime } from '../stores/useSpaceRuntime'

const route = useRoute()
const router = useRouter()
const mode = ref(route.query.mode === 'register' ? 'register' : 'login')
const auth = useOwnerMode()
const { loadOwnerPosts, loadPublicFeed } = useSpaceRuntime()
const credentials = reactive({ username: '', password: '', displayName: '' })
const submitting = ref(false)
const heading = computed(() => mode.value === 'register' ? '在空间里留下名字' : '欢迎回到 BinSpace')

const submit = async () => {
  submitting.value = true
  try {
    const identity = mode.value === 'register'
      ? await auth.registerVisitor(credentials)
      : await auth.login({ username: credentials.username, password: credentials.password })
    if (identity.role === 'OWNER') await loadOwnerPosts()
    else await loadPublicFeed()
    await router.replace(typeof route.query.redirect === 'string' ? route.query.redirect : '/')
  } catch {
    // 具体错误由集中身份状态展示。
  } finally {
    submitting.value = false
  }
}

const switchMode = (next) => {
  mode.value = next
  auth.state.error = ''
}
</script>

<template>
  <main id="main-content" class="auth-page">
    <section class="auth-page__sheet">
      <p>BINSPACE ACCESS</p>
      <h1>{{ heading }}</h1>
      <span>不登录也可以浏览。登录后可以评论、留言和收藏喜欢的内容；Owner 会额外看到空间管理能力。</span>
      <div class="auth-page__tabs" role="tablist" aria-label="登录或注册">
        <button type="button" :class="{ active: mode === 'login' }" @click="switchMode('login')">登录</button>
        <button type="button" :class="{ active: mode === 'register' }" @click="switchMode('register')">注册 Visitor</button>
      </div>
      <form @submit.prevent="submit">
        <label>用户名<input v-model.trim="credentials.username" autocomplete="username" minlength="3" maxlength="64" required /></label>
        <label v-if="mode === 'register'">昵称<input v-model.trim="credentials.displayName" autocomplete="nickname" maxlength="64" required /></label>
        <label>密码<input v-model="credentials.password" type="password" :autocomplete="mode === 'register' ? 'new-password' : 'current-password'" :minlength="mode === 'register' ? 8 : undefined" maxlength="128" required /></label>
        <p v-if="auth.state.error" class="auth-page__error">{{ auth.state.error }}</p>
        <button type="submit" :disabled="submitting">{{ submitting ? '正在处理…' : mode === 'register' ? '注册并进入空间' : '登录' }}</button>
      </form>
      <small v-if="mode === 'register'">用户名只支持字母、数字和下划线，密码至少 8 位。</small>
      <RouterLink to="/">← 暂不登录，继续浏览</RouterLink>
    </section>
  </main>
</template>

<style scoped>
.auth-page { display: grid; min-height: 68vh; place-items: center; padding: var(--space-8); background: rgb(252 251 249 / .9); border: 1px solid var(--color-border); border-radius: var(--radius-xl); }
.auth-page__sheet { width: min(100%, 500px); }
.auth-page__sheet > p { margin: 0 0 var(--space-2); color: var(--color-accent); font-family: var(--font-mono); font-size: var(--font-size-eyebrow); font-weight: 700; letter-spacing: .12em; }
.auth-page h1 { margin: 0; font-family: var(--font-serif); font-size: clamp(2rem, 5vw, 3.2rem); }
.auth-page__sheet > span { display: block; margin-top: var(--space-4); color: var(--color-text-secondary); line-height: 1.8; }
.auth-page__tabs { display: flex; gap: var(--space-2); margin-top: var(--space-7); padding-bottom: var(--space-2); border-bottom: 1px solid var(--color-border); }
.auth-page__tabs button { min-height: 40px; padding: 0 var(--space-4); color: var(--color-text-secondary); background: transparent; border: 0; border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }
.auth-page__tabs button.active { color: var(--color-accent); background: var(--color-accent-soft); }
.auth-page form { display: grid; gap: var(--space-4); margin-top: var(--space-6); }
.auth-page label { display: grid; gap: var(--space-2); color: var(--color-text-secondary); font-size: var(--font-size-meta); font-weight: 700; }
.auth-page input { min-height: 48px; padding: 0 var(--space-4); color: var(--color-text-primary); background: var(--color-surface); border: 1px solid var(--color-border-strong); border-radius: var(--radius-sm); font: inherit; }
.auth-page form > button { min-height: 48px; color: var(--color-text-inverse); background: var(--color-accent); border: 0; border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }
.auth-page form > button:disabled { cursor: wait; opacity: .65; }
.auth-page__error { margin: 0; color: var(--color-error); }
.auth-page small { display: block; margin-top: var(--space-3); color: var(--color-text-secondary); }
.auth-page a { display: inline-flex; min-height: 44px; align-items: center; margin-top: var(--space-4); color: var(--color-text-secondary); text-decoration: none; }
@media (max-width: 600px) { .auth-page { padding: var(--space-5); } }
</style>
