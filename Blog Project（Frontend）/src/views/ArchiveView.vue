<script setup>
import { onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import PageHeader from '../components/layout/PageHeader.vue'
import BackendPending from '../components/layout/BackendPending.vue'
import { useSpaceRuntime } from '../stores/useSpaceRuntime'

const { state, loadArchive } = useSpaceRuntime()
const monthName = (month) => new Intl.DateTimeFormat('zh-CN', { month: 'long' }).format(new Date(2026, month - 1, 1))
const day = (value) => String(new Date(value).getDate()).padStart(2, '0')
onMounted(loadArchive)
</script>

<template>
  <main id="main-content" class="archive-page">
    <PageHeader eyebrow="ARCHIVE" title="内容归档" description="按时间把技术笔记和生活动态放回各自发生的月份。" />
    <div class="archive">
      <template v-if="state.archive.length">
        <div v-for="year in state.archive" :key="year.year" class="archive__year">
          <h2>{{ year.year }}</h2>
          <section v-for="month in year.months" :key="month.month">
            <header><h3>{{ monthName(month.month) }}</h3><span>{{ month.items.length }} NOTES</span></header>
            <ol>
              <li v-for="item in month.items" :key="item.id">
                <time :datetime="item.publishedAt">{{ day(item.publishedAt) }}</time>
                <span :class="item.type === 'TECH' ? 'type-tech' : 'type-moment'">{{ item.type }}</span>
                <RouterLink :to="item.type === 'TECH' ? `/tech/${item.slug}` : `/moments/${item.id}`">{{ item.label }}</RouterLink>
              </li>
            </ol>
          </section>
        </div>
      </template>
      <BackendPending v-else title="暂无归档" description="数据库中还没有已发布内容，因此暂时无法生成归档。" />
    </div>
  </main>
</template>

<style scoped>
.archive-page { padding: var(--space-6); background: rgba(252, 251, 249, 0.8); border: 1px solid var(--color-border); border-radius: var(--radius-xl); }
.archive { max-width: 900px; padding-top: var(--space-8); }
.archive__year + .archive__year { margin-top: var(--space-16); }
.archive > h2 { margin: 0 0 var(--space-10); font-family: var(--font-serif); font-size: 3rem; }
.archive section { display: grid; grid-template-columns: 160px 1fr; gap: var(--space-6); padding: var(--space-6) 0; border-top: 1px solid var(--color-border); scroll-margin-top: 96px; }
.archive section header h3 { margin: 0; font-family: var(--font-serif); font-size: 1.35rem; }.archive section header span { color: var(--color-text-secondary); font-family: var(--font-mono); font-size: 0.6875rem; }
.archive ol { display: grid; gap: var(--space-3); margin: 0; padding: 0; list-style: none; }
.archive li { display: grid; grid-template-columns: 46px 66px 1fr; align-items: baseline; gap: var(--space-3); }
.archive time { color: var(--color-text-secondary); font-family: var(--font-mono); font-size: 0.6875rem; }.archive li > span { padding: 2px 7px; border-radius: var(--radius-pill); font-family: var(--font-mono); font-size: 0.625rem; text-align: center; }
.type-tech { color: var(--color-accent-pressed); background: var(--color-accent-soft); }.type-moment { color: #5f5966; background: #e9e2e8; }
.archive a { overflow: hidden; text-decoration: none; text-overflow: ellipsis; white-space: nowrap; }.archive a:hover { color: var(--color-accent); }
@media (max-width: 650px) { .archive-page { padding: var(--space-5); border-radius: var(--radius-lg); } .archive { padding-top: var(--space-6); } .archive section { grid-template-columns: 1fr; gap: var(--space-4); } .archive li { grid-template-columns: 42px 62px minmax(0, 1fr); } }
</style>
