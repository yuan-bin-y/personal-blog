import { posts } from './posts'
import { comments } from './comments'
import { guestbookEntries } from './guestbook'

const newestFirst = (a, b) => new Date(b.createdAt) - new Date(a.createdAt)

export const getAllPosts = () => [...posts].sort(newestFirst)
export const getPostsByType = (type) => getAllPosts().filter((post) => post.type === type)
export const getPostById = (id) => posts.find((post) => post.id === id)
export const getTechPostBySlug = (slug) => posts.find((post) => post.type === 'TECH' && post.slug === slug)
export const getCommentsByPostId = (postId) => comments.filter((comment) => comment.postId === postId).sort(newestFirst)
export const getLatestGuestbookEntries = (limit = 3) => [...guestbookEntries].sort(newestFirst).slice(0, limit)
export const getRelatedTechPosts = (post, limit = 2) => getPostsByType('TECH')
  .filter((item) => item.id !== post.id)
  .sort((a, b) => Number(b.category === post.category) - Number(a.category === post.category))
  .slice(0, limit)

export const formatMonthKey = (date) => date.slice(0, 7)
export const groupPostsByMonth = (items) => items.reduce((groups, post) => {
  const key = formatMonthKey(post.createdAt)
  if (!groups[key]) groups[key] = []
  groups[key].push(post)
  return groups
}, {})

export const getMomentGroups = () => groupPostsByMonth(getPostsByType('MOMENT'))
export const getArchiveGroups = () => groupPostsByMonth(getAllPosts())
