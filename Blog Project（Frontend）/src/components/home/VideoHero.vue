<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import HeroControls from './HeroControls.vue'

const props = defineProps({
  eyebrow: { type: String, default: 'BINSPACE' },
  title: { type: String, required: true },
  description: { type: String, required: true },
  media: {
    type: Object,
    default: () => ({
      desktop: { webm: '', mp4: '' },
      mobile: { webm: '', mp4: '' },
      poster: { avif: '', fallback: '' },
    }),
  },
  immersive: Boolean,
  backgroundMode: { type: String, default: 'video' },
  wallpaper: { type: String, default: '' },
  shade: { type: Number, default: 0.3 },
  isOwner: Boolean,
})

const emit = defineEmits(['visibility-change'])
const heroElement = ref(null)
const videoElement = ref(null)
const allowVideo = ref(false)
const isPlaying = ref(false)
const isMuted = ref(true)
const volume = ref(0.55)
const userPaused = ref(false)
const heroInView = ref(true)
let observer

const configuredSources = computed(() => [
  props.media?.mobile?.webm,
  props.media?.mobile?.mp4,
  props.media?.desktop?.webm,
  props.media?.desktop?.mp4,
].filter(Boolean))
const hasConfiguredVideo = computed(() => configuredSources.value.length > 0)
const mediaAvailable = computed(() => hasConfiguredVideo.value && allowVideo.value)
const hasPoster = computed(() => Boolean(props.media?.poster?.avif || props.media?.poster?.fallback))

const attemptPlayback = async () => {
  if (!mediaAvailable.value || !videoElement.value || userPaused.value) return
  try {
    await videoElement.value.play()
  } catch {
    isPlaying.value = false
  }
}

const togglePlayback = async () => {
  if (!videoElement.value) return
  if (isPlaying.value) {
    userPaused.value = true
    videoElement.value.pause()
  } else {
    userPaused.value = false
    await attemptPlayback()
  }
}

const toggleMuted = () => {
  if (!videoElement.value) return
  videoElement.value.muted = !videoElement.value.muted
  if (!videoElement.value.muted && videoElement.value.volume === 0) {
    volume.value = 0.55
    videoElement.value.volume = volume.value
  }
  isMuted.value = videoElement.value.muted
}

const updateVolume = (nextVolume) => {
  if (!videoElement.value) return
  volume.value = Math.min(1, Math.max(0, nextVolume))
  videoElement.value.volume = volume.value
  videoElement.value.muted = volume.value === 0
  isMuted.value = videoElement.value.muted
}

const handleDocumentVisibility = () => {
  if (!videoElement.value || !mediaAvailable.value) return
  if (document.hidden) videoElement.value.pause()
  else if (heroInView.value && !userPaused.value) attemptPlayback()
}

onMounted(async () => {
  const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  const saveData = navigator.connection?.saveData === true
  allowVideo.value = hasConfiguredVideo.value && !reducedMotion && !saveData

  observer = new IntersectionObserver(([entry]) => {
    heroInView.value = entry.isIntersecting
    emit('visibility-change', entry.isIntersecting)
    if (!videoElement.value || !mediaAvailable.value) return
    if (!entry.isIntersecting) videoElement.value.pause()
    else if (!userPaused.value && !document.hidden) attemptPlayback()
  }, { threshold: 0.12, rootMargin: '-72px 0px 0px 0px' })

  observer.observe(heroElement.value)
  document.addEventListener('visibilitychange', handleDocumentVisibility)
  if (allowVideo.value) {
    await nextTick()
    if (videoElement.value) videoElement.value.volume = volume.value
    attemptPlayback()
  }
})

onBeforeUnmount(() => {
  observer?.disconnect()
  document.removeEventListener('visibilitychange', handleDocumentVisibility)
  emit('visibility-change', false)
})
</script>

<template>
  <section
    ref="heroElement"
    class="video-hero"
    :class="{ 'video-hero--immersive': immersive }"
    :style="{ '--hero-shade': shade }"
    aria-labelledby="hero-title"
  >
    <div class="video-hero__media" aria-hidden="true">
      <div v-if="backgroundMode === 'wallpaper' && wallpaper" class="video-hero__wallpaper" :style="{ backgroundImage: `url(${wallpaper})` }"></div>
      <picture v-else-if="hasPoster" class="video-hero__poster">
        <source v-if="media.poster.avif" :srcset="media.poster.avif" type="image/avif" />
        <img :src="media.poster.fallback || media.poster.avif" alt="" />
      </picture>
      <div v-else class="video-hero__placeholder">
        <span class="video-hero__sun"></span>
        <span class="video-hero__cloud video-hero__cloud--one"></span>
        <span class="video-hero__cloud video-hero__cloud--two"></span>
        <span class="video-hero__window-lines"></span>
      </div>
      <video
        v-if="mediaAvailable && backgroundMode === 'video'"
        ref="videoElement"
        class="video-hero__video"
        autoplay muted loop playsinline preload="metadata"
        :poster="media.poster.fallback || undefined"
        @play="isPlaying = true"
        @pause="isPlaying = false"
        @volumechange="isMuted = $event.currentTarget.muted"
      >
        <source v-if="media.mobile.webm" :src="media.mobile.webm" type="video/webm" media="(max-width: 767px)" />
        <source v-if="media.mobile.mp4" :src="media.mobile.mp4" type="video/mp4" media="(max-width: 767px)" />
        <source v-if="media.desktop.webm" :src="media.desktop.webm" type="video/webm" />
        <source v-if="media.desktop.mp4" :src="media.desktop.mp4" type="video/mp4" />
      </video>
    </div>

    <div class="video-hero__scrim" aria-hidden="true"></div>
    <div class="video-hero__content container">
      <div class="video-hero__copy">
        <p class="video-hero__eyebrow"><span aria-hidden="true"></span>{{ eyebrow }}</p>
        <h1 id="hero-title">{{ title }}</h1>
        <p class="video-hero__description">{{ description }}</p>
        <a class="video-hero__scroll-cue" href="#profile"><span>向下看看</span><i aria-hidden="true"></i></a>
      </div>
      <HeroControls
        v-if="mediaAvailable && backgroundMode === 'video'"
        class="video-hero__controls"
        :is-playing="isPlaying"
        :is-muted="isMuted"
        :volume="volume"
        @toggle-playback="togglePlayback"
        @toggle-muted="toggleMuted"
        @update-volume="updateVolume"
      />
      <RouterLink v-if="isOwner" class="video-hero__owner-edit" to="/settings#hero">更换背景</RouterLink>
    </div>
    <div class="video-hero__paper-edge" aria-hidden="true"></div>
  </section>
</template>

<style scoped>
.video-hero {
  position: relative;
  min-height: clamp(480px, 68vh, 720px);
  overflow: hidden;
  isolation: isolate;
  color: var(--color-text-inverse);
  background: #657888;
}

.video-hero__media,
.video-hero__wallpaper,
.video-hero__poster,
.video-hero__poster img,
.video-hero__video,
.video-hero__placeholder,
.video-hero__scrim { position: absolute; inset: 0; width: 100%; height: 100%; }
.video-hero__poster img,
.video-hero__video { object-fit: cover; }
.video-hero__wallpaper {
  background-position: center;
  background-size: cover;
  animation: hero-wallpaper-breathe 28s ease-in-out infinite alternate;
  transform: scale(1.025);
  will-change: transform;
}

.video-hero__placeholder {
  overflow: hidden;
  background:
    linear-gradient(152deg, rgba(48, 62, 75, 0.18) 0 28%, transparent 28.2%),
    linear-gradient(180deg, #7590a6 0%, #9db1c0 44%, #cbd5dc 69%, #596875 100%);
}

.video-hero__placeholder::before {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 78% 28%, rgba(255, 246, 214, 0.56), transparent 12%),
    linear-gradient(112deg, transparent 0 58%, rgba(255, 239, 204, 0.13) 58.2% 59%, transparent 59.2%);
  content: '';
}

.video-hero__placeholder::after {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 180 180' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='.82' numOctaves='2' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='.26'/%3E%3C/svg%3E");
  content: '';
  mix-blend-mode: soft-light;
  opacity: 0.24;
}

.video-hero__sun {
  position: absolute;
  top: 19%;
  right: 15%;
  width: clamp(96px, 13vw, 184px);
  aspect-ratio: 1;
  background: rgba(255, 239, 189, 0.92);
  border-radius: 50%;
  box-shadow: 0 0 80px rgba(255, 224, 157, 0.48);
}

.video-hero__cloud {
  position: absolute;
  height: clamp(32px, 5vw, 66px);
  background: rgba(255, 235, 215, 0.38);
  border-radius: var(--radius-pill);
  filter: blur(2px);
}

.video-hero__cloud--one { top: 28%; right: 5%; width: 34%; animation: cloud-drift 24s ease-in-out infinite alternate; }
.video-hero__cloud--two { top: 43%; left: 46%; width: 23%; opacity: 0.58; animation: cloud-drift 30s ease-in-out -8s infinite alternate-reverse; }
.video-hero__window-lines {
  position: absolute;
  inset: 0 0 0 auto;
  width: min(38vw, 560px);
  border-left: 1px solid rgba(255, 239, 220, 0.18);
  background:
    linear-gradient(90deg, transparent 49.8%, rgba(255, 239, 220, 0.15) 50%, transparent 50.2%),
    linear-gradient(0deg, transparent 57%, rgba(255, 239, 220, 0.15) 57.2%, transparent 57.4%);
  transform: skewX(-7deg);
  transform-origin: bottom right;
}

.video-hero__scrim {
  z-index: 1;
  background:
    linear-gradient(90deg, rgba(28, 36, 46, 0.62) 0%, rgba(28, 36, 46, 0.26) 52%, rgba(28, 36, 46, 0.08) 100%),
    linear-gradient(180deg, rgba(25, 32, 40, 0.14) 0%, rgba(25, 32, 40, 0.2) 45%, rgba(25, 32, 40, 0.72) 100%),
    rgb(25 32 40 / var(--hero-shade));
}

.video-hero--immersive { min-height: 100svh; overflow: visible; background: transparent; isolation: auto; }
.video-hero--immersive .video-hero__media,
.video-hero--immersive .video-hero__scrim { position: fixed; z-index: 0; }
.video-hero--immersive .video-hero__content { min-height: 100svh; }
.video-hero--immersive .video-hero__paper-edge { display: none; }

.video-hero__content {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
  min-height: clamp(480px, 68vh, 720px);
  gap: var(--space-8);
  padding-top: calc(var(--navbar-height) + var(--space-12));
  padding-bottom: 132px;
}

.video-hero__copy { max-width: 720px; }
.video-hero__eyebrow {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin: 0 0 var(--space-5);
  font-family: var(--font-mono);
  font-size: var(--font-size-eyebrow);
  font-weight: 700;
  letter-spacing: 0.14em;
}
.video-hero__eyebrow span { width: 34px; height: 1px; background: currentColor; }
.video-hero h1 {
  max-width: 680px;
  margin: 0;
  font-family: var(--font-serif);
  font-size: var(--font-size-display);
  font-weight: 700;
  line-height: 1.02;
  letter-spacing: -0.04em;
  text-wrap: balance;
  text-shadow: 0 3px 24px rgba(25, 32, 40, 0.28);
}

.video-hero__description {
  max-width: 590px;
  margin: var(--space-5) 0 0;
  color: rgba(255, 247, 238, 0.9);
  font-size: 1rem;
  line-height: 1.8;
}

.video-hero__scroll-cue {
  display: inline-flex;
  align-items: center;
  min-height: 44px;
  gap: var(--space-3);
  margin-top: var(--space-6);
  font-size: var(--font-size-meta);
  font-weight: 600;
  text-decoration: none;
}
.video-hero__scroll-cue i { width: 42px; height: 1px; background: currentColor; transform-origin: left; transition: transform var(--duration-normal) ease-out; }
.video-hero__scroll-cue:hover i { transform: scaleX(1.22); }
.video-hero__controls { padding-bottom: 2px; }
.video-hero__owner-edit { position: absolute; right: 0; bottom: 74px; display: inline-flex; min-height: 38px; align-items: center; padding: 0 var(--space-4); color: rgba(255,255,255,.92); background: rgba(25,32,40,.3); border: 1px solid rgba(255,255,255,.46); border-radius: var(--radius-pill); backdrop-filter: blur(8px); font-size: .75rem; font-weight: 700; text-decoration: none; }
.video-hero__owner-edit:hover { background: rgba(25,32,40,.48); }
.video-hero__paper-edge {
  position: absolute;
  right: 0;
  bottom: -1px;
  left: 0;
  z-index: 3;
  height: 18px;
  background: var(--color-canvas);
  clip-path: polygon(0 72%, 8% 58%, 16% 76%, 27% 48%, 39% 70%, 51% 44%, 63% 68%, 75% 51%, 88% 73%, 100% 54%, 100% 100%, 0 100%);
}

@keyframes cloud-drift {
  from { transform: translate3d(-10px, 0, 0); }
  to { transform: translate3d(18px, 4px, 0); }
}

@keyframes hero-wallpaper-breathe {
  from { transform: scale(1.025) translate3d(-0.25%, 0, 0); }
  to { transform: scale(1.065) translate3d(0.35%, -0.35%, 0); }
}

@media (max-width: 767px) {
  .video-hero,
  .video-hero__content { min-height: max(480px, 72svh); }
  .video-hero__content {
    grid-template-columns: 1fr;
    align-content: end;
    gap: var(--space-5);
    padding-top: calc(var(--navbar-height) + var(--space-8));
    padding-bottom: 84px;
  }
  .video-hero__scrim { background: linear-gradient(180deg, rgba(25, 32, 40, 0.16) 0%, rgba(25, 32, 40, 0.24) 34%, rgba(25, 32, 40, 0.78) 100%); }
  .video-hero__description { max-width: 34rem; }
  .video-hero__window-lines { width: 54vw; }
  .video-hero__owner-edit { right: var(--page-gutter); bottom: 28px; }
}

@media (max-width: 479px) {
  .video-hero h1 { max-width: 9em; }
  .video-hero__eyebrow { letter-spacing: 0.1em; }
  .video-hero__description { font-size: 0.9375rem; }
  .video-hero__scroll-cue { display: none; }
}

@media (prefers-reduced-motion: reduce) {
  .video-hero__cloud,
  .video-hero__wallpaper { animation: none; }
}
</style>
