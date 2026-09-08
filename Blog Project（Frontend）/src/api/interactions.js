import request, { responseData } from './request'

export const getComments = (postId, params = {}) => request.get(`/posts/${postId}/comments`, { params }).then(responseData)
export const createComment = (postId, content) => request.post(`/posts/${postId}/comments`, { content }).then(responseData)
export const updateOwnComment = (id, content) => request.put(`/comments/${id}`, { content }).then(responseData)
export const deleteOwnComment = (id) => request.delete(`/comments/${id}`)
export const replyComment = (id, content) => request.post(`/owner/comments/${id}/reply`, { content }).then(responseData)
export const deleteComment = (id) => request.delete(`/owner/comments/${id}`)
export const getGuestbook = (params = {}) => request.get('/guestbook', { params }).then(responseData)
export const createGuestbook = (content) => request.post('/guestbook', { content }).then(responseData)
export const updateOwnGuestbook = (id, content) => request.put(`/guestbook/${id}`, { content }).then(responseData)
export const deleteOwnGuestbook = (id) => request.delete(`/guestbook/${id}`)
export const replyGuestbook = (id, content) => request.post(`/owner/guestbook/${id}/reply`, { content }).then(responseData)
export const deleteGuestbook = (id) => request.delete(`/owner/guestbook/${id}`)
