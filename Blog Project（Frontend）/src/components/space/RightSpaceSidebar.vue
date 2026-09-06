<script setup>
import { computed, onMounted, ref } from 'vue'
import BackendPending from '../layout/BackendPending.vue'
import { getTags } from '../../api/taxonomy'
import { useSpaceRuntime } from '../../stores/useSpaceRuntime'

defineProps({ compact: Boolean })

const now = new Date()
const year = now.getFullYear()
const month = now.getMonth()
const today = now.getDate()
const monthLabel = new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: 'long' }).format(now)
const firstWeekday = new Date(year, month, 1).getDay()
const dayCount = new Date(year, month + 1, 0).getDate()
const calendarDays = computed(() => [
  ...Array.from({ length: firstWeekday }, () => null),
  ...Array.from({ length: dayCount }, (_, index) => index + 1),
])
const aiMessage = ref('')
const recentTags = ref([])
const { state } = useSpaceRuntime()

const showComingSoon = () => { aiMessage.value = 'Coming Soon · AI 搜索将在后端能力完成后开放。' }
onMounted(async () => {
  try { recentTags.value = (await getTags({ page: 1, pageSize: 8 })).items } catch { recentTags.value = [] }
})
</script>

<template>
  <div class="right-space" :class="{ 'right-space--compact': compact }">
    <section class="calendar" aria-labelledby="calendar-title">
      <h2 id="calendar-title">{{ monthLabel }}</h2>
      <div class="calendar__week" aria-hidden="true"><span>日</span><span>一</span><span>二</span><span>三</span><span>四</span><span>五</span><span>六</span></div>
      <div class="calendar__days" aria-label="本月日历">
        <span v-for="(day, index) in calendarDays" :key="`${day}-${index}`" :class="{ 'is-today': day === today }">{{ day }}</span>
      </div>
    </section>

    <section class="space-corner" aria-labelledby="space-corner-title">
      <h2 id="space-corner-title">SPACE CORNER</h2>
      <div><h3>CURRENTLY</h3><p v-if="state.profile.status?.text" class="space-corner__status"><span>{{ state.profile.status.emoji }}</span>{{ state.profile.status.text }}</p><BackendPending v-else compact title="暂无状态" description="主人还没有填写当前状态。" /></div>
      <div><h3>RECENT TAGS</h3><div v-if="recentTags.length" class="space-corner__tags"><span v-for="tag in recentTags" :key="tag.id">#{{ tag.name }}</span></div><BackendPending v-else compact title="暂无标签" description="数据库中暂无可用标签。" /></div>
    </section>

    <section class="ai-search" aria-labelledby="ai-search-title">
      <p>AI SEARCH</p>
      <h2 id="ai-search-title">问问这个空间</h2>
      <button type="button" @click="showComingSoon">搜索空间内容 <span aria-hidden="true">→</span></button>
      <p class="ai-search__status" aria-live="polite">{{ aiMessage }}</p>
    </section>
  </div>
</template>

<style scoped>
.right-space { display: grid; gap: var(--space-4); }
.calendar,
.space-corner,
.ai-search {
  padding: var(--space-4);
  background: rgb(252 251 249 / var(--space-surface-opacity, 0.94));
  backdrop-filter: blur(16px);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-1);
}

.calendar h2 { margin: 0 0 var(--space-3); color: var(--color-accent); font-family: var(--font-serif); font-size: 1.1rem; }
.calendar__week,
.calendar__days { display: grid; grid-template-columns: repeat(7, 1fr); gap: 3px; }
.calendar__week { margin-bottom: var(--space-1); color: var(--color-text-secondary); font-size: 0.6875rem; text-align: center; }
.calendar__days span { display: grid; min-width: 0; aspect-ratio: 1; place-items: center; border-radius: 50%; font-family: var(--font-mono); font-size: 0.6875rem; }
.calendar__days .is-today { color: var(--color-text-inverse); background: var(--color-accent); font-weight: 700; }

.space-corner > h2,
.ai-search > p:first-child { margin: 0; color: var(--color-accent); font-family: var(--font-mono); font-size: 0.6875rem; font-weight: 700; letter-spacing: 0.11em; }
.space-corner > div { padding-top: var(--space-4); }
.space-corner > div + div { margin-top: var(--space-2); border-top: 1px dashed var(--color-border-strong); }
.space-corner h3 { margin: 0; font-size: 0.75rem; letter-spacing: 0.08em; }
.space-corner__status { display: flex; gap: var(--space-2); margin: var(--space-3) 0 0; color: var(--color-text-secondary); font-size: .8125rem; line-height: 1.6; }
.space-corner__tags { display: flex; gap: var(--space-2); margin-top: var(--space-3); flex-wrap: wrap; }
.space-corner__tags span { color: var(--color-text-secondary); font-size: .75rem; }
.space-corner :deep(.backend-pending) { border-bottom: 0; }
.space-corner :deep(.backend-pending p) { font-size: 0.78rem; }

.ai-search { background: var(--color-surface-dark); color: var(--color-text-inverse); }
.ai-search h2 { margin: var(--space-1) 0 var(--space-3); font-family: var(--font-serif); font-size: 1.15rem; }
.ai-search button { display: flex; width: 100%; min-height: 44px; align-items: center; justify-content: space-between; padding-inline: var(--space-3); color: var(--color-text-primary); background: var(--color-surface); border: 0; border-radius: var(--radius-sm); cursor: pointer; font-size: 0.8125rem; }
.ai-search__status { min-height: 2.5em; margin: var(--space-2) 0 0; color: rgba(255, 247, 238, 0.72); font-size: 0.75rem; line-height: 1.5; }

.right-space--compact .calendar { display: none; }
.right-space--compact .space-corner { padding: var(--space-3); }
.right-space--compact .space-corner :deep(.backend-pending > span) { display: none; }
.right-space--compact .space-corner :deep(.backend-pending) { grid-template-columns: 1fr; }

@media (max-width: 767px) {
  .right-space { grid-template-columns: minmax(0, 1fr); }
  .calendar { display: none; }
  .space-corner { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4); }
  .space-corner > h2 { grid-column: 1 / -1; }
  .space-corner > div { padding-top: var(--space-3); }
  .space-corner > div + div { margin-top: 0; padding-left: var(--space-4); border-top: 0; border-left: 1px dashed var(--color-border-strong); }
  .ai-search__status { min-height: 0; }
}

@media (max-width: 380px) {
  .space-corner { grid-template-columns: 1fr; }
  .space-corner > div + div { padding-left: 0; border-left: 0; border-top: 1px dashed var(--color-border-strong); }
}
</style>
