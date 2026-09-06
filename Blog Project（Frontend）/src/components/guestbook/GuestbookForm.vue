<script setup>
import BackendPending from '../layout/BackendPending.vue'
</script>

<template>
  <BackendPending compact description="留言提交需要后端接口与数据库持久化。" />
</template>

<style scoped>
.guestbook-form { padding: var(--space-6); background: var(--color-surface-soft); border: 1px solid var(--color-border); border-radius: var(--radius-lg); }
.guestbook-form__note { margin: 0 0 var(--space-5); color: var(--color-text-secondary); font-size: 0.8125rem; }
.guestbook-form label { position: relative; display: grid; gap: var(--space-2); margin-top: var(--space-4); color: var(--color-text-secondary); font-size: 0.8125rem; font-weight: 650; }
.guestbook-form input,
.guestbook-form textarea { width: 100%; padding: var(--space-3); color: var(--color-text-primary); background: var(--color-surface); border: 1px solid var(--color-border-strong); border-radius: var(--radius-sm); font-size: 0.9375rem; resize: vertical; }
.guestbook-form textarea { min-height: 144px; }
.guestbook-form label small { position: absolute; right: var(--space-3); bottom: var(--space-2); color: var(--color-text-secondary); font-family: var(--font-mono); font-size: 0.625rem; }
.guestbook-form button { display: inline-flex; align-items: center; min-height: 46px; gap: var(--space-2); margin-top: var(--space-5); padding: 0 var(--space-5); color: var(--color-text-inverse); background: var(--color-accent); border: 0; border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }
.guestbook-form button:hover { background: var(--color-accent-hover); }
.guestbook-form__error { color: var(--color-error); }.guestbook-form__success { color: var(--color-success); }
</style>
