<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import AppNavbar from './components/layout/AppNavbar.vue'
import AppFooter from './components/layout/AppFooter.vue'
import VideoHero from './components/home/VideoHero.vue'
import SpaceLayout from './layouts/SpaceLayout.vue'
import AppearanceControls from './components/home/AppearanceControls.vue'
import { useOwnerMode } from './stores/useOwnerMode'
import { useSpaceRuntime } from './stores/useSpaceRuntime'
import { useNotifications } from './stores/useNotifications'

const route = useRoute()
const { isOwner, isAuthenticated, initializeOwnerMode } = useOwnerMode()
const { state: space, initialize, loadOwnerPosts } = useSpaceRuntime()
const notifications = useNotifications()
const heroVisible = ref(false)
const isHome = computed(() => route.name === 'home')
const isImmersive = computed(() => isHome.value && space.appearance.layoutMode === 'immersive')
const pageBackdrop = computed(() => {
  if (route.name === 'moments' || route.name === 'moment-detail') return space.pageMedia.moments
  if (route.name === 'guestbook') return space.pageMedia.guestbook
  if (route.name === 'tech' || route.name === 'tech-detail') return space.pageMedia.tech
  if (route.name === 'archive') return space.pageMedia.archive
  if (route.name === 'about') return space.pageMedia.about
  return null
})
const pageWallpaper = computed(() => pageBackdrop.value?.poster || '')
const pageVideo = computed(() => pageBackdrop.value?.video || '')
const pageOverlay = computed(() => pageBackdrop.value?.overlay || '')
const pageMotion = computed(() => pageBackdrop.value?.motion || '')
const pageEffect = computed(() => pageBackdrop.value?.effect || '')
const currentWallpaper = computed(() => {
  const mobile = window.matchMedia('(max-width: 767px)').matches
  return mobile ? space.appearance.wallpaper.mobile || space.appearance.wallpaper.desktop : space.appearance.wallpaper.desktop
})
const activeBackgroundMode = computed(() => {
  return space.appearance.backgroundMode === 'wallpaper' && currentWallpaper.value ? 'wallpaper' : 'video'
})
const appStyle = computed(() => ({
  '--space-surface-opacity': isImmersive.value ? space.appearance.surfaceOpacity : 0.96,
  '--space-backdrop-shade': space.appearance.backdropShade,
  '--page-wallpaper': pageWallpaper.value ? `url("${pageWallpaper.value}")` : 'none',
  '--page-overlay': pageOverlay.value ? `url("${pageOverlay.value}")` : 'none',
}))
const navbarTone = computed(() => {
  return (isHome.value && (isImmersive.value || heroVisible.value)) || pageBackdrop.value ? 'overlay' : 'surface'
})
const footerTone = computed(() => {
  if (isHome.value) return 'home'
  if (route.name === 'moments' || route.name === 'moment-detail') return 'moments'
  if (route.name === 'guestbook') return 'guestbook'
  if (route.name === 'tech' || route.name === 'tech-detail') return 'tech'
  if (route.name === 'archive') return 'archive'
  if (route.name === 'about') return 'about'
  return 'neutral'
})

watch(
  () => route.fullPath,
  () => { heroVisible.value = isHome.value },
  { immediate: true },
)

onMounted(async () => {
  await Promise.all([initializeOwnerMode(), initialize()])
  if (isOwner.value) await loadOwnerPosts()
})

watch(isAuthenticated, (active) => active ? notifications.start() : notifications.stop())
</script>

<template>
  <div
    class="app-shell"
    :class="{
      'app-shell--immersive': isImmersive,
      'app-shell--page-wallpaper': pageWallpaper,
      'app-shell--page-video': pageVideo,
      'app-shell--page-image': pageWallpaper && !pageVideo,
      'app-shell--moments-wallpaper': route.name === 'moments' || route.name === 'moment-detail',
      'app-shell--guestbook-wallpaper': route.name === 'guestbook',
      'app-shell--tech-wallpaper': route.name === 'tech' || route.name === 'tech-detail',
      'app-shell--archive-wallpaper': route.name === 'archive',
      'app-shell--about-wallpaper': route.name === 'about',
    }"
    :style="appStyle"
  >
    <div
      v-if="pageWallpaper && !pageVideo"
      class="page-wallpaper-image"
      :class="pageMotion ? `page-wallpaper-image--${pageMotion}` : ''"
      aria-hidden="true"
    ></div>
    <div
      v-if="pageOverlay"
      class="page-wallpaper-overlay"
      :class="pageMotion ? `page-wallpaper-overlay--${pageMotion}` : ''"
      aria-hidden="true"
    ></div>
    <div v-if="pageEffect" class="page-ambient" :class="`page-ambient--${pageEffect}`" aria-hidden="true">
      <span
        v-for="index in 14"
        :key="index"
        :style="{
          '--left': `${(index * 7.4) % 100}%`,
          '--delay': `${index * -1.85}s`,
          '--duration': `${15 + (index % 4) * 3}s`,
          '--drift': `${index % 2 ? 9 : -9}vw`,
        }"
      ></span>
    </div>
    <video
      v-if="pageVideo"
      class="page-wallpaper-video"
      :src="pageVideo"
      :poster="pageWallpaper"
      autoplay
      muted
      loop
      playsinline
      aria-hidden="true"
    ></video>
    <a class="skip-link" href="#main-content">跳到主要内容</a>
    <AppNavbar :tone="navbarTone" />
    <VideoHero
      v-if="isHome"
      :immersive="isImmersive"
      :background-mode="activeBackgroundMode"
      :wallpaper="currentWallpaper"
      :shade="space.appearance.backdropShade"
      :eyebrow="space.hero.eyebrow"
      :title="space.hero.title"
      :description="space.hero.description"
      :media="space.hero.media"
      :is-owner="isOwner"
      @visibility-change="heroVisible = $event"
    />
    <SpaceLayout :home="isHome" :route-name="String(route.name || '')" :is-owner="isOwner" :can-edit="isOwner" :can-delete="isOwner" :can-publish="isOwner">
      <RouterView v-slot="{ Component }">
        <Transition name="page" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </Transition>
      </RouterView>
    </SpaceLayout>
    <AppearanceControls
      v-if="isHome && space.appearance.allowVisitorControls"
      v-model:layout-mode="space.appearance.layoutMode"
      v-model:background-mode="space.appearance.backgroundMode"
      v-model:surface-opacity="space.appearance.surfaceOpacity"
      v-model:backdrop-shade="space.appearance.backdropShade"
      :has-wallpaper="Boolean(currentWallpaper)"
    />
    <AppFooter v-if="!isImmersive" :tone="footerTone" />
  </div>
</template>

<style>
.skip-link {
  position: fixed;
  top: var(--space-3);
  left: var(--space-3);
  z-index: var(--z-toast);
  padding: var(--space-2) var(--space-4);
  color: var(--color-text-inverse);
  background: var(--color-surface-dark);
  border-radius: var(--radius-sm);
  transform: translateY(-160%);
  transition: transform var(--duration-fast) ease-out;
}

.skip-link:focus { transform: translateY(0); }
.app-shell { min-height: 100vh; }
.app-shell--immersive { position: relative; background: transparent; }
.app-shell--page-wallpaper {
  position: relative;
  isolation: isolate;
  background-color: #e9eef1;
  background-image: linear-gradient(rgb(241 247 249 / 0.08), rgb(237 244 247 / 0.14));
}
.app-shell--guestbook-wallpaper {
  background-color: #eadfe5;
  background-image: linear-gradient(rgb(249 239 245 / 0.08), rgb(241 229 236 / 0.18)), var(--page-wallpaper);
}
.page-wallpaper-video {
  position: fixed;
  inset: 0;
  z-index: -2;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  pointer-events: none;
}
.page-wallpaper-image {
  position: fixed;
  inset: -3%;
  z-index: -2;
  background-image: var(--page-wallpaper);
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  pointer-events: none;
  transform: scale(1.035);
  will-change: transform;
}
.page-wallpaper-overlay {
  position: fixed;
  inset: 0;
  z-index: -1;
  background-image: var(--page-overlay);
  background-position: center 67%;
  background-repeat: no-repeat;
  background-size: min(52vw, 980px) auto;
  pointer-events: none;
  will-change: transform;
}
.page-ambient {
  position: fixed;
  inset: 0;
  z-index: -1;
  overflow: hidden;
  pointer-events: none;
}
.page-ambient--petals > span {
  position: absolute;
  top: -12vh;
  left: var(--left);
  width: clamp(7px, 0.75vw, 12px);
  height: clamp(13px, 1.25vw, 20px);
  background: linear-gradient(145deg, rgb(255 250 251 / 0.92), rgb(241 177 191 / 0.78));
  border-radius: 85% 15% 72% 28% / 72% 28% 82% 18%;
  box-shadow: 0 0 9px rgb(255 222 228 / 0.32);
  opacity: 0;
  animation: petal-fall var(--duration) linear var(--delay) infinite;
  will-change: transform, opacity;
}
.page-ambient--petals > span:nth-child(3n) { width: 6px; height: 11px; opacity: 0.62; }
.page-ambient--petals > span:nth-child(4n) { background: rgb(255 255 255 / 0.82); }
.page-ambient--petals::after {
  position: absolute;
  right: -8%;
  bottom: -7%;
  left: -8%;
  height: 43%;
  background:
    repeating-radial-gradient(ellipse at 50% 112%, transparent 0 28px, rgb(242 252 255 / 0.16) 30px 31px, transparent 33px 58px),
    linear-gradient(180deg, transparent, rgb(211 240 248 / 0.1));
  content: '';
  mask-image: linear-gradient(transparent, #000 42%, #000);
  mix-blend-mode: screen;
  opacity: 0.65;
  animation: water-shimmer 9s ease-in-out infinite alternate;
  will-change: transform, opacity;
}
.page-wallpaper-image--drift { animation: page-drift 26s ease-in-out infinite alternate; }
.page-wallpaper-image--train { animation: page-breathe 30s ease-in-out infinite alternate; }
.page-wallpaper-overlay--train { animation: train-glide 30s linear infinite; }
.app-shell--page-video { background-image: linear-gradient(rgb(21 31 38 / 0.08), rgb(21 31 38 / 0.18)); }
.app-shell--tech-wallpaper { background-color: #d9edf4; }
.app-shell--archive-wallpaper { background-color: #263642; }
.app-shell--about-wallpaper { background-color: #bc6d55; }
.app-shell.app-shell--tech-wallpaper .tech-page,
.app-shell.app-shell--tech-wallpaper .article-page {
  --color-accent: #397e9d;
  --color-accent-hover: #2c6782;
  --color-accent-soft: #dceff4;
  --color-border: rgb(113 161 180 / 0.52);
  --color-border-strong: rgb(73 131 153 / 0.62);
  --color-text-primary: #243f4c;
  --color-text-secondary: #526f7b;
  background: rgb(241 249 251 / 0.76);
  backdrop-filter: saturate(0.96);
}
.app-shell.app-shell--archive-wallpaper .archive-page {
  --color-accent: #d6c48f;
  --color-accent-hover: #eadba8;
  --color-accent-soft: rgb(214 196 143 / 0.15);
  --color-border: rgb(225 233 229 / 0.28);
  --color-border-strong: rgb(225 233 229 / 0.42);
  --color-surface-soft: rgb(239 244 241 / 0.1);
  --color-text-primary: #f0f3ed;
  --color-text-secondary: #c4cfca;
  color: var(--color-text-primary);
  background: rgb(24 37 45 / 0.68);
  border-color: var(--color-border);
  backdrop-filter: saturate(0.9);
}
.app-shell.app-shell--about-wallpaper .about-page {
  --color-accent: #9b503f;
  --color-accent-hover: #743b30;
  --color-accent-soft: #f2d5c5;
  --color-border: rgb(139 82 64 / 0.34);
  --color-border-strong: rgb(119 65 51 / 0.48);
  --color-surface-soft: rgb(255 240 226 / 0.62);
  --color-text-primary: #462f2a;
  --color-text-secondary: #75554c;
  background: rgb(255 242 228 / 0.72);
  border-color: var(--color-border);
  backdrop-filter: saturate(0.92);
}
.app-shell--page-wallpaper .app-footer { margin-top: var(--space-24); }
@keyframes page-drift {
  from { transform: scale(1.035) translate3d(-0.6%, -0.3%, 0); }
  to { transform: scale(1.075) translate3d(0.8%, 0.5%, 0); }
}
@keyframes page-breathe {
  from { transform: scale(1.035) translate3d(-0.35%, 0, 0); }
  to { transform: scale(1.065) translate3d(0.45%, -0.35%, 0); }
}
@keyframes train-glide {
  from { transform: translate3d(82vw, 0, 0); }
  to { transform: translate3d(-82vw, 0, 0); }
}
@keyframes petal-fall {
  0% { opacity: 0; transform: translate3d(0, -8vh, 0) rotate(0deg); }
  8% { opacity: 0.82; }
  84% { opacity: 0.68; }
  100% { opacity: 0; transform: translate3d(var(--drift), 116vh, 0) rotate(620deg); }
}
@keyframes water-shimmer {
  from { opacity: 0.42; transform: translate3d(-1.5%, 0, 0) scaleX(0.985); }
  to { opacity: 0.72; transform: translate3d(1.5%, -5px, 0) scaleX(1.02); }
}
@media (max-width: 767px) {
  .app-shell--moments-wallpaper .page-wallpaper-image { background-position: 58% top; }
  .app-shell--tech-wallpaper .page-wallpaper-image { background-position: 58% center; }
  .app-shell--about-wallpaper .page-wallpaper-image { background-position: 60% center; }
  .app-shell--about-wallpaper .page-wallpaper-overlay { background-position: 58% 68%; background-size: min(78vw, 720px) auto; }
  .page-wallpaper-video { object-position: 55% center; }
}
@media (prefers-reduced-motion: reduce) {
  .page-wallpaper-image,
  .page-wallpaper-overlay,
  .page-ambient--petals > span,
  .page-ambient--petals::after { animation: none; }
  .page-ambient--petals > span { display: none; }
}
.page-enter-active,
.page-leave-active { transition: opacity var(--duration-fast) ease-out, transform var(--duration-fast) ease-out; }
.page-enter-from { opacity: 0; transform: translateY(6px); }
.page-leave-to { opacity: 0; transform: translateY(-3px); }
</style>
