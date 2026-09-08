import request, { responseData } from './request'

export const searchPosts = (params) => request.get('/search', { params }).then(responseData)
export const getRelatedPosts = (postId, limit = 4) => request.get(`/posts/${postId}/related`, { params: { limit } }).then(responseData)
export const askAi = (question) => request.post('/search/ai', { question }, { timeout: 60000 }).then(responseData)
export const createReindexTask = () => request.post('/owner/search/reindex').then(responseData)
export const getReindexTask = (taskId) => request.get(`/owner/search/reindex/${taskId}`).then(responseData)
