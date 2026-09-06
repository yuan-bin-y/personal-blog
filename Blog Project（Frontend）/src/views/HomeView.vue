<script setup>
import { computed, ref } from 'vue'

import BackendPending from '../components/layout/BackendPending.vue'
import FeedFilter from '../components/home/FeedFilter.vue'
import GuestbookPreview from '../components/home/GuestbookPreview.vue'
import PostFeed from '../components/post/PostFeed.vue'
import OwnerDialog from '../components/owner/OwnerDialog.vue'
import OwnerPostEditor from '../components/owner/OwnerPostEditor.vue'

import { useOwnerMode } from '../stores/useOwnerMode'
import { useSpaceRuntime } from '../stores/useSpaceRuntime'

const { isOwner } = useOwnerMode()
const { state, sortedPosts, updatePost, deletePost } = useSpaceRuntime()

const activeFilter = ref('ALL')
const editing = ref(null)
const deleting = ref(null)

const visiblePosts = computed(() =>
  activeFilter.value === 'ALL'
    ? sortedPosts.value
    : sortedPosts.value.filter(
        (post) => post.type === activeFilter.value
      )
)

const saveEdit = async (draft) => {
  await updatePost(editing.value.id, draft)
  editing.value = null
}

const confirmDelete = async () => {
  await deletePost(deleting.value)
  deleting.value = null
}
</script>

<template>
  <main id="main-content" class="home-view">
    <section class="home-feed" aria-labelledby="recent-notes-title">
      <header class="home-feed__header">
        <div>
          <p>RECENT NOTES</p>
          <h1 id="recent-notes-title">最近更新</h1>
          <span>技术笔记与生活片段，按时间留在同一个空间里。</span>
        </div>
        <FeedFilter v-model="activeFilter" />
      </header>

      <p v-if="state.error" class="home-feed__error">{{ state.error }}</p>
      <PostFeed v-if="visiblePosts.length" :posts="visiblePosts" @edit="editing = $event" @delete="deleting = $event" />
      <BackendPending
        v-else
        title="暂无内容"
        :description="state.loading ? '正在从后端读取空间内容…' : '数据库中还没有可显示的内容。Owner 登录后可以发布第一篇文章或说说。'"
      />
    </section>

    <GuestbookPreview />

    <OwnerDialog :open="Boolean(editing)" :title="editing?.type === 'TECH' ? '编辑文章' : '编辑说说'" :size="editing?.type === 'TECH' ? 'large' : 'default'" @close="editing = null">
      <OwnerPostEditor v-if="editing" :type="editing.type" :post="editing" @save="saveEdit" @cancel="editing = null" />
    </OwnerDialog>
    <OwnerDialog :open="Boolean(deleting)" :title="deleting?.type === 'TECH' ? '确认删除文章' : '确认删除说说'" description="删除后内容将不再公开显示。" @close="deleting = null">
      <div class="delete-confirm">
        <p>确定删除这条内容吗？此操作会写入数据库。</p>
        <footer><button type="button" @click="deleting = null">取消</button><button class="is-danger" type="button" @click="confirmDelete">确认删除</button></footer>
      </div>
    </OwnerDialog>
  </main>
</template>

<style scoped>
.home-view { min-width: 0; padding: var(--space-6); background: rgb(252 251 249 / var(--space-surface-opacity, .94)); border: 1px solid var(--color-border); border-radius: var(--radius-xl); box-shadow: var(--shadow-1); backdrop-filter: blur(16px); }
.home-feed__header { display: flex; align-items: end; justify-content: space-between; gap: var(--space-6); padding-bottom: var(--space-8); border-bottom: 1px solid var(--color-border); }
.home-feed__header p { margin: 0 0 var(--space-2); color: var(--color-accent); font-family: var(--font-mono); font-size: var(--font-size-eyebrow); font-weight: 700; letter-spacing: .12em; }
.home-feed__header h1 { margin: 0; font-family: var(--font-serif); font-size: clamp(2rem, 4vw, 3.1rem); line-height: 1.1; }
.home-feed__header span { display: block; margin-top: var(--space-3); color: var(--color-text-secondary); line-height: 1.7; }
.home-feed :deep(.post-feed) { margin-top: var(--space-6); }
.home-feed__error { padding: var(--space-3) var(--space-4); color: var(--color-error); background: var(--color-surface-soft); border-left: 3px solid currentColor; }
.delete-confirm p { margin: 0; }
.delete-confirm footer { display: flex; justify-content: flex-end; gap: var(--space-3); margin-top: var(--space-6); }
.delete-confirm button { min-height: 42px; padding: 0 var(--space-4); background: transparent; border: 1px solid var(--color-border); border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }
.delete-confirm .is-danger { color: white; background: var(--color-error); border-color: var(--color-error); }
@media (max-width: 760px) { .home-view { padding: var(--space-5); border-radius: var(--radius-lg); } .home-feed__header { align-items: stretch; flex-direction: column; padding-bottom: var(--space-6); } }
</style>
