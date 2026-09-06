<script setup>
import { computed, provide } from 'vue'
import LeftSpaceSidebar from '../components/space/LeftSpaceSidebar.vue'
import RightSpaceSidebar from '../components/space/RightSpaceSidebar.vue'

const props = defineProps({
  home: Boolean,
  routeName: { type: String, default: '' },
  isOwner: { type: Boolean, default: false },
  canEdit: { type: Boolean, default: false },
  canDelete: { type: Boolean, default: false },
  canPublish: { type: Boolean, default: false },
})

const capabilities = computed(() => ({
  isOwner: props.isOwner,
  canEdit: props.canEdit,
  canDelete: props.canDelete,
  canPublish: props.canPublish,
}))

const isTechDetail = computed(() => props.routeName === 'tech-detail')
provide('spaceCapabilities', capabilities)
</script>

<template>
  <div
    class="space-layout"
    :class="{
      'space-layout--home': home,
      'space-layout--inner': !home,
      'space-layout--tech-detail': isTechDetail,
    }"
  >
    <aside v-if="home" class="space-layout__left" aria-label="空间主人信息">
      <LeftSpaceSidebar :compact="!home" />
    </aside>

    <div class="space-layout__main">
      <slot />
    </div>

    <aside v-if="home" class="space-layout__right" aria-label="空间小组件">
      <RightSpaceSidebar :compact="isTechDetail" />
    </aside>
  </div>
</template>

<style scoped>
.space-layout {
  position: relative;
  z-index: var(--z-raised);
  display: grid;
  grid-template-columns: 252px minmax(0, 1fr) 232px;
  width: min(calc(100% - 2 * var(--page-gutter)), 1260px);
  min-height: 72vh;
  gap: var(--space-6);
  margin-inline: auto;
  padding-top: calc(var(--navbar-height) + var(--space-10));
}

.space-layout--home {
  margin-top: -68px;
  padding-top: 0;
}

.space-layout--inner {
  grid-template-columns: minmax(0, 1fr);
  width: min(calc(100% - 2 * var(--page-gutter)), 1080px);
}

.space-layout--tech-detail {
  grid-template-columns: minmax(0, 1fr);
  width: min(calc(100% - 2 * var(--page-gutter)), 1180px);
}

.space-layout__left,
.space-layout__right {
  align-self: start;
  position: sticky;
  top: calc(var(--navbar-height) + var(--space-10));
}

.space-layout--home .space-layout__left,
.space-layout--home .space-layout__right {
  top: calc(var(--navbar-height) + var(--space-6));
}

.space-layout__main { min-width: 0; }

@media (max-width: 1120px) {
  .space-layout--home {
    grid-template-columns: 224px minmax(0, 1fr);
    max-width: 1000px;
  }

  .space-layout__right { display: none; }
}

@media (max-width: 767px) {
  .space-layout,
  .space-layout--tech-detail {
    grid-template-columns: minmax(0, 1fr);
    width: min(calc(100% - 2 * var(--page-gutter)), 680px);
    gap: var(--space-5);
    padding-top: calc(var(--navbar-height) + var(--space-6));
  }

  .space-layout--home {
    margin-top: -28px;
    padding-top: 0;
  }

  .space-layout__left,
  .space-layout__right {
    position: static;
  }

  .space-layout__right { display: block; }
}
</style>
