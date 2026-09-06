import { computed, reactive } from 'vue'
import { appearanceConfig, heroContent, musicMedia, pageMedia, siteMeta } from '../mock/site'
import { profile as emptyProfile } from '../mock/profile'
import * as postApi from '../api/posts'
import * as siteApi from '../api/site'
import * as interactionApi from '../api/interactions'
import { getArchive } from '../api/archive'
import { apiMessage } from '../api/request'

const copy = (value) => JSON.parse(JSON.stringify(value))

const runtime = reactive({
  ready: false,
  loading: false,
  error: '',
  posts: [],
  comments: [],
  guestbook: [],
  archive: [],
  profile: copy(emptyProfile),
  hero: copy(heroContent),
  music: copy(musicMedia),
  announcement: { content: '', enabled: false },
  basic: copy(siteMeta),
  appearance: copy(appearanceConfig),
  pageMedia: copy(pageMedia),
  externalLinks: { homepage: '', github: '' },
  siteVersion: 0,
})

const media = (item) => item ? { ...item, src: item.src || '' } : null
const normalizeProfile = (profile) => ({
  ...profile,
  stats: Array.isArray(profile?.stats)
    ? profile.stats
    : [
        { label: '技术文章', value: profile?.stats?.techPostCount ?? 0 },
        { label: '说说', value: profile?.stats?.momentCount ?? 0 },
        { label: '留言', value: profile?.stats?.guestbookCount ?? 0 },
      ],
})
const normalizePost = (post) => ({
  ...post,
  category: post.category?.name || null,
  categoryId: post.category?.id || null,
  tags: (post.tags || []).map((tag) => tag.name),
  tagIds: (post.tags || []).map((tag) => tag.id),
  cover: media(post.cover),
  images: post.type === 'TECH'
    ? (post.cover ? [media(post.cover)] : [])
    : (post.images || []).map(media),
})

const applyBootstrap = (data) => {
  runtime.basic = { ...runtime.basic, ...data.basic }
  runtime.profile = normalizeProfile(data.profile)
  runtime.hero = {
    eyebrow: data.hero.eyebrow,
    title: data.hero.heroTitle,
    description: data.hero.heroSubtitle,
    media: {
      desktop: { webm: '', mp4: data.hero.desktopVideo || '' },
      mobile: { webm: '', mp4: data.hero.mobileVideo || '' },
      poster: { avif: '', fallback: data.hero.poster || '' },
    },
  }
  runtime.music = { ...(data.music || {}), src: data.music?.audioUrl || '' }
  runtime.announcement = data.announcement || { content: '', enabled: false }
  runtime.appearance = data.appearance || runtime.appearance
  runtime.pageMedia = data.pageMedia || runtime.pageMedia
  runtime.externalLinks = data.externalLinks || runtime.externalLinks
  runtime.siteVersion = data.version
}

let initialization
const initialize = async () => {
  if (initialization) return initialization
  initialization = (async () => {
    runtime.loading = true
    runtime.error = ''
    try {
      const [bootstrap, feed, guestbook] = await Promise.all([
        siteApi.getSiteBootstrap(),
        postApi.getPublicFeed({ type: 'ALL', page: 1, pageSize: 50 }),
        interactionApi.getGuestbook({ page: 1, pageSize: 50 }),
      ])
      applyBootstrap(bootstrap)
      runtime.posts = feed.items.map(normalizePost)
      runtime.guestbook = guestbook.items
    } catch (error) {
      runtime.error = apiMessage(error)
    } finally {
      runtime.loading = false
      runtime.ready = true
    }
  })()
  return initialization
}

const replacePost = (post) => {
  const normalized = normalizePost(post)
  const index = runtime.posts.findIndex((item) => item.id === normalized.id)
  if (index < 0) runtime.posts.unshift(normalized)
  else runtime.posts[index] = normalized
  return normalized
}

const createTech = async (draft) => replacePost(await postApi.createTech(draft))
const createMoment = async (draft) => replacePost(await postApi.createMoment(draft))
const updatePost = async (id, draft) => {
  const current = runtime.posts.find((post) => String(post.id) === String(id))
  const updated = current?.type === 'TECH'
    ? await postApi.updateTech(id, { ...draft, version: current.version })
    : await postApi.updateMoment(id, { ...draft, version: current.version })
  return replacePost(updated)
}
const deletePost = async (post) => {
  if (post.type === 'TECH') await postApi.deleteTech(post.id, post.version)
  else await postApi.deleteMoment(post.id, post.version)
  runtime.posts = runtime.posts.filter((item) => item.id !== post.id)
}

const loadTech = async (category) => {
  const page = await postApi.getPublicTech({ category: category || undefined, page: 1, pageSize: 50 })
  runtime.posts = [...runtime.posts.filter((post) => post.type !== 'TECH'), ...page.items.map(normalizePost)]
  return page.items.map(normalizePost)
}
const loadPublicFeed = async () => {
  const page = await postApi.getPublicFeed({ type: 'ALL', page: 1, pageSize: 50 })
  runtime.posts = page.items.map(normalizePost)
  return runtime.posts
}
const loadOwnerPosts = async () => {
  const page = await postApi.getOwnerPosts({ type: 'ALL', status: 'ALL', page: 1, pageSize: 50 })
  runtime.posts = page.items.map(normalizePost)
  return runtime.posts
}
const loadMoments = async () => {
  const page = await postApi.getPublicMoments({ page: 1, pageSize: 50 })
  runtime.posts = [...runtime.posts.filter((post) => post.type !== 'MOMENT'), ...page.items.map(normalizePost)]
  return page.items.map(normalizePost)
}
const loadTechDetail = async (slug) => replacePost(await postApi.getTechDetail(slug))
const loadMomentDetail = async (id) => replacePost(await postApi.getMomentDetail(id))
const loadOwnerTech = async (id) => replacePost(await postApi.getOwnerTech(id))
const loadOwnerMoment = async (id) => replacePost(await postApi.getOwnerMoment(id))
const loadComments = async (postId) => {
  const page = await interactionApi.getComments(postId, { page: 1, pageSize: 50 })
  runtime.comments = [...runtime.comments.filter((item) => item.postId !== String(postId)), ...page.items]
  return page.items
}
const loadGuestbook = async () => {
  const page = await interactionApi.getGuestbook({ page: 1, pageSize: 50 })
  runtime.guestbook = page.items
  return page.items
}
const loadArchive = async () => {
  const data = await getArchive({ type: 'ALL', page: 1, pageSize: 50 })
  runtime.archive = data.groups
  return data.groups
}

const applySection = (result, key, mapper = (value) => value) => {
  runtime[key] = mapper(result.config)
  runtime.siteVersion = result.version
  return runtime[key]
}

export const useSpaceRuntime = () => ({
  state: runtime,
  initialize,
  techPosts: computed(() => runtime.posts.filter((post) => post.type === 'TECH')),
  momentPosts: computed(() => runtime.posts.filter((post) => post.type === 'MOMENT')),
  sortedPosts: computed(() => [...runtime.posts].sort((a, b) => new Date(b.publishedAt || b.createdAt) - new Date(a.publishedAt || a.createdAt))),
  findTech: (slug) => runtime.posts.find((post) => post.type === 'TECH' && post.slug === slug),
  findMoment: (id) => runtime.posts.find((post) => post.type === 'MOMENT' && String(post.id) === String(id)),
  createTech,
  createMoment,
  updatePost,
  deletePost,
  loadTech,
  loadPublicFeed,
  loadOwnerPosts,
  loadMoments,
  loadTechDetail,
  loadMomentDetail,
  loadOwnerTech,
  loadOwnerMoment,
  loadComments,
  loadGuestbook,
  loadArchive,
  updateProfile: async (values) => {
    runtime.profile = normalizeProfile(await siteApi.updateProfile({ ...values, version: runtime.profile.version }))
    return runtime.profile
  },
  updateHero: (values) => siteApi.updateHero({ ...values, version: runtime.siteVersion }).then((r) => applySection(r, 'hero', (v) => ({ eyebrow: v.eyebrow, title: v.heroTitle, description: v.heroSubtitle, media: { desktop: { webm: '', mp4: v.desktopVideo || '' }, mobile: { webm: '', mp4: v.mobileVideo || '' }, poster: { avif: '', fallback: v.poster || '' } } }))),
  updateMusic: (values) => siteApi.updateMusic({ ...values, version: runtime.siteVersion }).then((r) => applySection(r, 'music', (v) => ({ ...v, src: v.audioUrl || '' }))),
  updateAnnouncement: (values) => siteApi.updateAnnouncement({ ...values, version: runtime.siteVersion }).then((r) => applySection(r, 'announcement')),
  updateBasic: (values) => siteApi.updateSiteBasic({ ...values, version: runtime.siteVersion }).then((r) => applySection(r, 'basic')),
  updateAppearance: (values) => siteApi.updateAppearance({ ...values, version: runtime.siteVersion }).then((r) => applySection(r, 'appearance')),
  updatePageMedia: (values) => siteApi.updatePageMedia({ ...values, version: runtime.siteVersion }).then((r) => applySection(r, 'pageMedia')),
  replyComment: async (id, content) => {
    const updated = await interactionApi.replyComment(id, content)
    const index = runtime.comments.findIndex((item) => item.id === updated.id)
    if (index >= 0) runtime.comments[index] = updated
  },
  deleteComment: async (id) => {
    await interactionApi.deleteComment(id)
    runtime.comments = runtime.comments.filter((item) => item.id !== id)
  },
  replyGuestbook: async (id, content) => {
    const updated = await interactionApi.replyGuestbook(id, content)
    const index = runtime.guestbook.findIndex((item) => item.id === updated.id)
    if (index >= 0) runtime.guestbook[index] = updated
  },
  deleteGuestbook: async (id) => {
    await interactionApi.deleteGuestbook(id)
    runtime.guestbook = runtime.guestbook.filter((item) => item.id !== id)
  },
})
