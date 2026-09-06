<template>
  <main id="main-content" class="not-found">
    <p>404 · LOST NOTE</p><h1>这张纸条不在空间里</h1><span>可能是地址写错了，也可能它还没有被记录下来。</span><RouterLink to="/">回到首页 →</RouterLink>
  </main>
</template>

<style scoped>
.not-found { display: grid; min-height: 60vh; place-items: center; align-content: center; text-align: center; }
.not-found p { margin: 0; color: var(--color-accent); font-family: var(--font-mono); font-size: var(--font-size-eyebrow); font-weight: 700; letter-spacing: 0.12em; }.not-found h1 { margin: var(--space-3) 0; font-family: var(--font-serif); font-size: var(--font-size-h1); }.not-found span { color: var(--color-text-secondary); }.not-found a { display: inline-flex; min-height: 44px; align-items: center; margin-top: var(--space-6); color: var(--color-accent); font-weight: 700; }
</style>
