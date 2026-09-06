<script setup>
import { onMounted, reactive, ref } from 'vue'
import * as taxonomyApi from '../../api/taxonomy'
import { apiMessage } from '../../api/request'

const categories = ref([])
const tags = ref([])
const error = ref('')
const categoryDraft = reactive({ id: '', name: '', slug: '', description: '', sortOrder: 0, version: 0 })
const tagDraft = reactive({ id: '', name: '', slug: '' })

const load = async () => {
  error.value = ''
  try {
    const [categoryPage, tagPage] = await Promise.all([
      taxonomyApi.getOwnerCategories({ page: 1, pageSize: 50 }),
      taxonomyApi.getOwnerTags({ page: 1, pageSize: 50 }),
    ])
    categories.value = categoryPage.items
    tags.value = tagPage.items
  } catch (reason) {
    error.value = apiMessage(reason)
  }
}

const resetCategory = () => Object.assign(categoryDraft, { id: '', name: '', slug: '', description: '', sortOrder: 0, version: 0 })
const resetTag = () => Object.assign(tagDraft, { id: '', name: '', slug: '' })
const editCategory = (item) => Object.assign(categoryDraft, item)
const editTag = (item) => Object.assign(tagDraft, item)

const saveCategory = async () => {
  error.value = ''
  const body = { name: categoryDraft.name.trim(), slug: categoryDraft.slug.trim(), description: categoryDraft.description.trim(), sortOrder: Number(categoryDraft.sortOrder) }
  try {
    if (categoryDraft.id) await taxonomyApi.updateCategory(categoryDraft.id, { ...body, version: categoryDraft.version })
    else await taxonomyApi.createCategory(body)
    resetCategory()
    await load()
  } catch (reason) { error.value = apiMessage(reason) }
}

const saveTag = async () => {
  error.value = ''
  const body = { name: tagDraft.name.trim(), slug: tagDraft.slug.trim() }
  try {
    if (tagDraft.id) await taxonomyApi.updateTag(tagDraft.id, body)
    else await taxonomyApi.createTag(body)
    resetTag()
    await load()
  } catch (reason) { error.value = apiMessage(reason) }
}

const removeCategory = async (item) => {
  if (!window.confirm(`停用分类“${item.name}”？历史文章仍会保留原分类名称。`)) return
  try { await taxonomyApi.deleteCategory(item.id, item.version); await load() } catch (reason) { error.value = apiMessage(reason) }
}
const removeTag = async (item) => {
  if (!window.confirm(`停用标签“${item.name}”？历史文章仍会保留原标签名称。`)) return
  try { await taxonomyApi.deleteTag(item.id); await load() } catch (reason) { error.value = apiMessage(reason) }
}

onMounted(load)
</script>

<template>
  <div class="taxonomy-manager">
    <p v-if="error" class="taxonomy-manager__error">{{ error }}</p>
    <section>
      <h3>技术分类</h3>
      <form @submit.prevent="saveCategory">
        <input v-model="categoryDraft.name" required maxlength="64" placeholder="分类名称" />
        <input v-model="categoryDraft.slug" maxlength="80" placeholder="slug（新建时可留空）" />
        <input v-model.number="categoryDraft.sortOrder" type="number" placeholder="排序" />
        <input v-model="categoryDraft.description" maxlength="255" placeholder="分类说明" />
        <footer><button v-if="categoryDraft.id" type="button" @click="resetCategory">取消编辑</button><button class="is-primary" type="submit">{{ categoryDraft.id ? '保存分类' : '新增分类' }}</button></footer>
      </form>
      <ul><li v-for="item in categories" :key="item.id"><span><strong>{{ item.name }}</strong><small>{{ item.slug }} · {{ item.active ? '启用' : '已停用' }}</small></span><div><button type="button" @click="editCategory(item)">编辑</button><button v-if="item.active" type="button" @click="removeCategory(item)">停用</button></div></li></ul>
    </section>
    <section>
      <h3>文章标签</h3>
      <form @submit.prevent="saveTag">
        <input v-model="tagDraft.name" required maxlength="64" placeholder="标签名称" />
        <input v-model="tagDraft.slug" maxlength="80" placeholder="slug（新建时可留空）" />
        <footer><button v-if="tagDraft.id" type="button" @click="resetTag">取消编辑</button><button class="is-primary" type="submit">{{ tagDraft.id ? '保存标签' : '新增标签' }}</button></footer>
      </form>
      <ul><li v-for="item in tags" :key="item.id"><span><strong>#{{ item.name }}</strong><small>{{ item.slug }} · {{ item.active ? '启用' : '已停用' }}</small></span><div><button type="button" @click="editTag(item)">编辑</button><button v-if="item.active" type="button" @click="removeTag(item)">停用</button></div></li></ul>
    </section>
  </div>
</template>

<style scoped>
.taxonomy-manager { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-8); }
.taxonomy-manager__error { grid-column: 1 / -1; margin: 0; padding: var(--space-3); color: var(--color-error); border-left: 3px solid currentColor; background: var(--color-surface-soft); }
.taxonomy-manager h3 { margin: 0 0 var(--space-4); font-family: var(--font-serif); }
.taxonomy-manager form { display: grid; gap: var(--space-3); }
.taxonomy-manager input { width: 100%; padding: var(--space-3); background: var(--color-surface); border: 1px solid var(--color-border-strong); border-radius: var(--radius-sm); font: inherit; }
.taxonomy-manager footer, .taxonomy-manager li > div { display: flex; justify-content: flex-end; gap: var(--space-2); }
.taxonomy-manager button { min-height: 38px; padding: 0 var(--space-3); color: var(--color-text-secondary); background: transparent; border: 1px solid var(--color-border); border-radius: var(--radius-pill); cursor: pointer; font-weight: 700; }
.taxonomy-manager button.is-primary { color: var(--color-text-inverse); background: var(--color-accent); border-color: var(--color-accent); }
.taxonomy-manager ul { margin: var(--space-5) 0 0; padding: 0; list-style: none; }
.taxonomy-manager li { display: flex; align-items: center; justify-content: space-between; gap: var(--space-3); padding: var(--space-3) 0; border-top: 1px dashed var(--color-border-strong); }
.taxonomy-manager li > span { display: grid; min-width: 0; }
.taxonomy-manager small { overflow: hidden; color: var(--color-text-secondary); text-overflow: ellipsis; white-space: nowrap; }
@media (max-width: 760px) { .taxonomy-manager { grid-template-columns: 1fr; } }
</style>
