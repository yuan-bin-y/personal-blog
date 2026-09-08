import request, { responseData } from './request'

export const getPublicFeed = (params) => request.get('/posts', { params }).then(responseData)
export const getPublicTech = (params) => request.get('/posts/tech', { params }).then(responseData)
export const getTechDetail = (slug) => request.get(`/posts/tech/${slug}`).then(responseData)
export const getPublicMoments = (params) => request.get('/posts/moments', { params }).then(responseData)
export const getMomentDetail = (id) => request.get(`/posts/moments/${id}`).then(responseData)
export const getOwnerPosts = (params) => request.get('/owner/posts', { params }).then(responseData)
export const getOwnerTech = (id) => request.get(`/owner/posts/tech/${id}`).then(responseData)
export const getOwnerMoment = (id) => request.get(`/owner/posts/moments/${id}`).then(responseData)
export const createTech = (body) => request.post('/owner/posts/tech', body).then(responseData)
export const updateTech = (id, body) => request.put(`/owner/posts/tech/${id}`, body).then(responseData)
export const deleteTech = (id, version) => request.delete(`/owner/posts/tech/${id}`, { params: { version } })
export const createMoment = (body) => request.post('/owner/posts/moments', body).then(responseData)
export const updateMoment = (id, body) => request.put(`/owner/posts/moments/${id}`, body).then(responseData)
export const deleteMoment = (id, version) => request.delete(`/owner/posts/moments/${id}`, { params: { version } })

// Owner Advanced Posts
export const schedulePost = (postId, publishAt, version) => request.put(
  `/owner/posts/${postId}/schedule`,
  { publishAt, version },
).then(responseData)
export const cancelScheduledPost = (postId, version) => request.delete(
  `/owner/posts/${postId}/schedule`,
  { params: { version } },
).then(responseData)
export const autosavePost = (postId, body) => request.put(
  `/owner/posts/${postId}/autosave`,
  body,
).then(responseData)
export const getPostVersions = (postId, params) => request.get(
  `/owner/posts/${postId}/versions`,
  { params },
).then(responseData)
export const getPostVersion = (postId, versionId) => request.get(
  `/owner/posts/${postId}/versions/${versionId}`,
).then(responseData)
export const restorePostVersion = (postId, versionId, version) => request.post(
  `/owner/posts/${postId}/versions/${versionId}/restore`,
  { version },
).then(responseData)
export const getPostTrash = (params) => request.get('/owner/posts/trash', { params }).then(responseData)
export const restoreTrashedPost = (postId, version) => request.post(
  `/owner/posts/${postId}/restore`,
  null,
  { params: { version } },
).then(responseData)
export const batchPublishPosts = (items) => request.post('/owner/posts/batch/publish', { items }).then(responseData)
export const batchDeletePosts = (items) => request.post('/owner/posts/batch/delete', { items }).then(responseData)
export const updateTechSlug = (id, slug, version) => request.put(
  `/owner/posts/tech/${id}/slug`,
  { slug, version },
).then(responseData)
export const importMarkdown = (file) => {
  const form = new FormData()
  form.append('file', file)
  return request.post('/owner/posts/import/markdown', form).then(responseData)
}
