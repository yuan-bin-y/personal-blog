<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BackendPending from '../components/layout/BackendPending.vue'
import MomentPostCard from '../components/post/MomentPostCard.vue'
import CommentSection from '../components/comment/CommentSection.vue'
import OwnerDialog from '../components/owner/OwnerDialog.vue'
import OwnerPostEditor from '../components/owner/OwnerPostEditor.vue'
import { useSpaceRuntime } from '../stores/useSpaceRuntime'
import { useOwnerMode } from '../stores/useOwnerMode'

const route = useRoute()
const router = useRouter()
const runtime = useSpaceRuntime()
const { isOwner, initializeOwnerMode } = useOwnerMode()
const post = computed(() => runtime.findMoment(route.params.id))
const editorOpen = ref(false)
const deleteOpen = ref(false)
const save = async (draft) => { await runtime.updatePost(post.value.id, draft); editorOpen.value = false }
const confirmDelete = async () => { await runtime.deletePost(post.value); router.push('/moments') }
onMounted(async () => {
  await initializeOwnerMode()
  if (isOwner.value) await runtime.loadOwnerMoment(route.params.id)
  else await runtime.loadMomentDetail(route.params.id)
})
</script>

<template>
  <main id="main-content" class="moment-detail-page">
    <div class="moment-detail-page__content">
      <RouterLink class="moment-detail-page__back" to="/moments"><span aria-hidden="true">←</span> 返回说说</RouterLink>
      <template v-if="post"><MomentPostCard :post="post" detail @edit="editorOpen = true" @delete="deleteOpen = true" /><CommentSection :post-id="post.id" title="评论" /></template>
      <BackendPending v-else description="说说详情、点赞数和评论都需要后端数据库支持。" />
    </div>
    <OwnerDialog :open="editorOpen" title="编辑说说" @close="editorOpen = false"><OwnerPostEditor v-if="post" type="MOMENT" :post="post" @save="save" @cancel="editorOpen = false" /></OwnerDialog>
    <OwnerDialog :open="deleteOpen" title="确认删除说说" description="删除后说说将不再公开显示。" @close="deleteOpen = false"><div class="delete-confirm"><p>确定删除这条说说吗？</p><footer><button type="button" @click="deleteOpen = false">取消</button><button class="is-danger" type="button" @click="confirmDelete">确认删除</button></footer></div></OwnerDialog>
  </main>
</template>

<style scoped>
.moment-detail-page {
  --color-accent: #4d7690;
  --color-accent-soft: #deebf1;
  --color-border: #cddde4;
  --color-text-primary: #283943;
  --color-text-secondary: #586f7a;
  padding: var(--space-6);
  background: linear-gradient(180deg, rgb(247 251 252 / 0.52), rgb(246 250 251 / 0.66));
  border: 1px solid rgb(205 221 228 / 0.72);
  border-radius: var(--radius-xl);
  box-shadow: 0 14px 42px rgb(64 101 119 / 0.12);
  backdrop-filter: saturate(0.96);
}
.moment-detail-page__content { max-width: 820px; }
.moment-detail-page__back { display: inline-flex; min-height: 44px; align-items: center; gap: var(--space-2); margin-bottom: var(--space-8); color: var(--color-text-secondary); font-size: var(--font-size-meta); font-weight: 650; text-decoration: none; }
.moment-detail-page__back:hover { color: var(--color-accent); }
.delete-confirm p { margin: 0; }.delete-confirm footer { display: flex; justify-content: flex-end; gap: var(--space-3); margin-top: var(--space-6); }.delete-confirm button { min-height: 42px; padding: 0 var(--space-4); background: transparent; border: 1px solid var(--color-border); border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }.delete-confirm .is-danger { color: white; background: var(--color-error); border-color: var(--color-error); }
@media (max-width: 600px) {
  .moment-detail-page { padding: var(--space-5); border-radius: var(--radius-lg); }
  .moment-detail-page__back { margin-bottom: var(--space-5); }
}
</style>
