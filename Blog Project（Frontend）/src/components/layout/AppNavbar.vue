<script setup>
import { nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { navigationItems } from '../../mock/site'
import { useOwnerMode } from '../../stores/useOwnerMode'
import { useSpaceRuntime } from '../../stores/useSpaceRuntime'

defineProps({
  tone: {
    type: String,
    default: 'surface',
    validator: (value) => ['overlay', 'surface'].includes(value),
  },
})

const menuOpen = ref(false)
const publishOpen = ref(false)
const route = useRoute()
const router = useRouter()
const { isOwner } = useOwnerMode()
const { state: space } = useSpaceRuntime()
const menuButton = ref(null)
const menuPanel = ref(null)
let previousBodyOverflow = ''
const isItemActive = (item) => item.to === '/' ? route.path === '/' : route.path.startsWith(item.to)
const startPublishing = (type) => {
  publishOpen.value = false
  router.push({ path: type === 'TECH' ? '/tech' : '/moments', query: { compose: '1' } })
}

const closeMenu = ({ restoreFocus = true } = {}) => {
  menuOpen.value = false
  if (restoreFocus) nextTick(() => menuButton.value?.focus())
}

const handlePanelKeydown = (event) => {
  if (event.key === 'Escape') {
    closeMenu()
    return
  }
  if (event.key !== 'Tab') return

  const focusable = [...menuPanel.value.querySelectorAll('a[href], button:not([disabled])')]
  const first = focusable[0]
  const last = focusable.at(-1)
  if (event.shiftKey && document.activeElement === first) {
    event.preventDefault()
    last?.focus()
  } else if (!event.shiftKey && document.activeElement === last) {
    event.preventDefault()
    first?.focus()
  }
}

watch(menuOpen, async (open) => {
  if (open) {
    previousBodyOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    await nextTick()
    menuPanel.value?.querySelector('a[href], button:not([disabled])')?.focus()
  } else {
    document.body.style.overflow = previousBodyOverflow
  }
})

watch(() => route.fullPath, () => {
  publishOpen.value = false
  if (menuOpen.value) closeMenu({ restoreFocus: false })
})

onBeforeUnmount(() => { document.body.style.overflow = previousBodyOverflow })
</script>

<template>
  <header class="navbar" :class="[`navbar--${tone}`, { 'navbar--menu-open': menuOpen }]">
    <div class="navbar__inner">
      <RouterLink class="navbar__brand" to="/" aria-label="玢的空间首页">
        <span class="navbar__monogram" aria-hidden="true">玢</span>
        <span class="navbar__brand-copy">
          <strong>{{ space.basic.chineseName }}</strong>
          <small>{{ space.basic.name }}</small>
        </span>
      </RouterLink>

      <nav class="navbar__desktop-nav" aria-label="主要导航">
        <template v-for="item in navigationItems" :key="item.to">
          <RouterLink
            v-if="item.available"
            class="navbar__link"
            :class="{ 'is-active': isItemActive(item) }"
            :aria-current="isItemActive(item) ? 'page' : undefined"
            :to="item.to"
          >
            {{ item.label }}
          </RouterLink>
          <span
            v-else
            class="navbar__link navbar__link--planned"
            aria-disabled="true"
            title="后续阶段开放"
          >{{ item.label }}</span>
        </template>
      </nav>

      <div v-if="isOwner" class="navbar__owner-actions">
        <div class="navbar__publish">
          <button type="button" :aria-expanded="publishOpen" @click="publishOpen = !publishOpen">＋ 发布</button>
          <div v-if="publishOpen" class="navbar__publish-menu">
            <button type="button" @click="startPublishing('MOMENT')">发说说</button>
            <button type="button" @click="startPublishing('TECH')">写文章</button>
          </div>
        </div>
        <RouterLink class="navbar__settings" to="/settings" aria-label="空间设置">⚙ <span>空间设置</span></RouterLink>
      </div>
      <RouterLink v-else class="navbar__owner-login" to="/owner-login" aria-label="主人登录">主人登录</RouterLink>

      <button
        ref="menuButton"
        class="navbar__menu-button"
        type="button"
        :aria-expanded="menuOpen"
        aria-controls="space-menu"
        @click="menuOpen = !menuOpen"
      >
        <span>{{ menuOpen ? '关闭' : '空间菜单' }}</span>
        <span class="navbar__menu-icon" aria-hidden="true"><i></i><i></i></span>
      </button>
    </div>

    <button
      v-if="menuOpen"
      class="navbar__backdrop"
      type="button"
      aria-label="关闭空间菜单"
      @click="closeMenu()"
    ></button>

    <nav
      v-if="menuOpen"
      id="space-menu"
      ref="menuPanel"
      class="navbar__mobile-panel"
      aria-label="移动端主要导航"
      @keydown="handlePanelKeydown"
    >
      <p class="navbar__mobile-eyebrow">WANDER AROUND</p>
      <template v-for="(item, index) in navigationItems" :key="item.to">
        <RouterLink
          v-if="item.available"
          class="navbar__mobile-link"
          :class="{ 'is-active': isItemActive(item) }"
          :aria-current="isItemActive(item) ? 'page' : undefined"
          :to="item.to"
          @click="closeMenu({ restoreFocus: false })"
        >
          <span>{{ String(index + 1).padStart(2, '0') }}</span>{{ item.label }}
        </RouterLink>
        <span v-else class="navbar__mobile-link navbar__mobile-link--planned" aria-disabled="true">
          <span>{{ String(index + 1).padStart(2, '0') }}</span>{{ item.label }}<small>稍后开放</small>
        </span>
      </template>
      <div v-if="isOwner" class="navbar__mobile-owner">
        <p>OWNER TOOLS</p>
        <button type="button" @click="startPublishing('MOMENT')">＋ 发说说</button>
        <button type="button" @click="startPublishing('TECH')">＋ 写文章</button>
        <RouterLink to="/settings" @click="closeMenu({ restoreFocus: false })">⚙ 空间设置</RouterLink>
      </div>
      <RouterLink v-else class="navbar__mobile-login" to="/owner-login" @click="closeMenu({ restoreFocus: false })">主人登录</RouterLink>
    </nav>
  </header>
</template>

<style scoped>
.navbar {
  position: fixed;
  top: var(--space-4);
  left: 50%;
  z-index: var(--z-sticky);
  width: min(calc(100% - 2 * var(--page-gutter)), 1080px);
  height: var(--navbar-height);
  color: var(--color-text-inverse);
  background: rgba(28, 38, 48, 0.28);
  border: 1px solid rgba(249, 250, 251, 0.24);
  border-radius: var(--radius-lg);
  box-shadow: 0 10px 34px rgba(23, 32, 42, 0.14);
  transform: translateX(-50%);
  backdrop-filter: blur(14px);
  transition: color var(--duration-normal) ease-out, background-color var(--duration-normal) ease-out,
    border-color var(--duration-normal) ease-out, box-shadow var(--duration-normal) ease-out;
}

.navbar--surface,
.navbar--menu-open {
  color: var(--color-text-primary);
  background: rgba(252, 251, 249, 0.92);
  border-color: rgba(185, 194, 201, 0.72);
  box-shadow: 0 8px 28px rgba(37, 45, 54, 0.07);
}

.navbar__inner {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  height: 100%;
  padding-inline: var(--space-5);
}

.navbar__brand {
  display: inline-flex;
  align-items: center;
  min-height: 44px;
  gap: var(--space-3);
  text-decoration: none;
}

.navbar__monogram {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  color: var(--color-text-primary);
  background: linear-gradient(145deg, var(--color-surface), var(--color-surface-soft));
  border: 1px solid rgba(255, 255, 255, 0.56);
  border-radius: 48% 52% 48% 52% / 55% 44% 56% 45%;
  box-shadow: 0 6px 18px rgba(37, 45, 54, 0.12);
  font-family: var(--font-serif);
  font-weight: 700;
}

.navbar__brand-copy { display: grid; line-height: 1.1; }
.navbar__brand-copy strong { font-size: 0.95rem; letter-spacing: 0.06em; }
.navbar__brand-copy small {
  margin-top: 4px;
  font-family: var(--font-mono);
  font-size: 0.625rem;
  letter-spacing: 0.18em;
  opacity: 0.68;
  text-transform: uppercase;
}

.navbar__desktop-nav {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: space-evenly;
  min-width: 0;
  margin-left: var(--space-8);
}
.navbar__owner-actions { display: flex; align-items: center; gap: var(--space-2); margin-left: var(--space-3); }
.navbar__owner-login { display: inline-flex; min-height: 38px; align-items: center; margin-left: var(--space-3); padding: 0 var(--space-3); color: inherit; border: 1px solid currentColor; border-radius: var(--radius-pill); font-size: .75rem; font-weight: 700; text-decoration: none; white-space: nowrap; opacity: .78; }
.navbar__publish { position: relative; }
.navbar__publish > button,
.navbar__settings { display: inline-flex; min-height: 38px; align-items: center; gap: 5px; padding: 0 var(--space-3); color: inherit; background: rgba(252, 251, 249, .12); border: 1px solid currentColor; border-radius: var(--radius-pill); cursor: pointer; font-size: .75rem; font-weight: 700; text-decoration: none; white-space: nowrap; }
.navbar--surface .navbar__publish > button,
.navbar--surface .navbar__settings { border-color: var(--color-border-strong); }
.navbar__publish-menu { position: absolute; top: calc(100% + 10px); right: 0; display: grid; min-width: 132px; padding: var(--space-2); color: var(--color-text-primary); background: rgba(252, 251, 249, .98); border: 1px solid var(--color-border); border-radius: var(--radius-md); box-shadow: var(--shadow-2); }
.navbar__publish-menu button { min-height: 40px; padding: 0 var(--space-3); color: inherit; background: transparent; border: 0; border-radius: var(--radius-sm); cursor: pointer; text-align: left; }
.navbar__publish-menu button:hover { color: var(--color-accent); background: var(--color-surface-soft); }
.navbar__link {
  position: relative;
  display: inline-flex;
  flex: 1;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding-inline: var(--space-2);
  font-size: var(--font-size-meta);
  font-weight: 600;
  text-decoration: none;
}

.navbar__link::after {
  position: absolute;
  right: 24%;
  bottom: 5px;
  left: 24%;
  height: 2px;
  background: currentColor;
  border-radius: var(--radius-pill);
  content: '';
  opacity: 0;
  transform: scaleX(0.45);
  transition: opacity var(--duration-fast), transform var(--duration-fast);
}

.navbar__link:hover::after,
.navbar__link.is-active::after { opacity: 1; transform: scaleX(1); }
.navbar--overlay .navbar__link.is-active::after { background: #d8e7f3; }
.navbar--surface .navbar__link.is-active { color: var(--color-accent); }
.navbar__link--planned { cursor: default; opacity: 0.76; }
.navbar__link--planned::after { display: none; }

.navbar__menu-button {
  display: none;
  align-items: center;
  min-height: 44px;
  gap: var(--space-3);
  padding: 0 var(--space-3);
  color: inherit;
  background: transparent;
  border: 1px solid currentColor;
  border-radius: var(--radius-pill);
  cursor: pointer;
  font-size: 0.8125rem;
  font-weight: 650;
  margin-left: auto;
}

.navbar__menu-icon { display: grid; width: 17px; gap: 5px; }
.navbar__menu-icon i { display: block; height: 1px; background: currentColor; }

.navbar__backdrop {
  position: fixed;
  inset: 0;
  z-index: -1;
  width: 100%;
  padding: 0;
  background: rgba(24, 32, 42, 0.46);
  border: 0;
}

.navbar__mobile-panel {
  position: fixed;
  top: calc(var(--navbar-height) + var(--space-8));
  right: 0;
  left: 0;
  display: grid;
  max-height: calc(100dvh - var(--navbar-height) - 24px);
  padding: var(--space-5);
  overflow-y: auto;
  color: var(--color-text-primary);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-2);
}

.navbar__mobile-eyebrow {
  margin: 0 0 var(--space-3);
  color: var(--color-text-secondary);
  font-family: var(--font-mono);
  font-size: var(--font-size-eyebrow);
  letter-spacing: 0.12em;
}

.navbar__mobile-link {
  display: grid;
  grid-template-columns: 36px 1fr auto;
  align-items: center;
  min-height: 52px;
  color: inherit;
  border-top: 1px solid var(--color-border);
  font-family: var(--font-serif);
  font-size: 1.125rem;
  font-weight: 700;
  text-decoration: none;
}

.navbar__mobile-link > span {
  color: var(--color-accent);
  font-family: var(--font-mono);
  font-size: 0.6875rem;
}

.navbar__mobile-link small {
  color: var(--color-text-secondary);
  font-family: var(--font-sans);
  font-size: 0.6875rem;
  font-weight: 500;
}

.navbar__mobile-link--planned { opacity: 0.72; }
.navbar__mobile-link.is-active { color: var(--color-accent); }
.navbar__mobile-owner { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-2); padding-top: var(--space-4); border-top: 1px solid var(--color-border); }
.navbar__mobile-owner p { grid-column: 1 / -1; margin: 0; color: var(--color-accent); font-family: var(--font-mono); font-size: .6875rem; font-weight: 700; letter-spacing: .1em; }
.navbar__mobile-owner button,
.navbar__mobile-owner a { display: flex; min-height: 44px; align-items: center; justify-content: center; padding: 0 var(--space-3); color: var(--color-text-primary); background: var(--color-surface-soft); border: 1px solid var(--color-border); border-radius: var(--radius-sm); cursor: pointer; font-size: .8125rem; font-weight: 700; text-decoration: none; }
.navbar__mobile-owner a { grid-column: 1 / -1; }
.navbar__mobile-login { display: flex; min-height: 48px; align-items: center; justify-content: center; margin-top: var(--space-4); color: var(--color-text-secondary); background: var(--color-surface-soft); border: 1px solid var(--color-border); border-radius: var(--radius-sm); font-weight: 700; text-decoration: none; }

@media (max-width: 767px) {
  .navbar { top: var(--space-2); width: calc(100% - 2 * var(--space-3)); border-radius: var(--radius-md); }
  .navbar__inner { padding-inline: var(--space-3); }
  .navbar__desktop-nav { display: none; }
  .navbar__owner-actions { display: none; }
  .navbar__owner-login { display: none; }
  .navbar__menu-button { display: inline-flex; }
  .navbar__brand-copy small { display: none; }
}
</style>
