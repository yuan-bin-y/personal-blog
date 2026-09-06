import request, { responseData } from './request'

export const getComments = (postId, params = {}) => request.get(`/posts/${postId}/comments`, { params }).then(responseData)
export const replyComment = (id, content) => request.post(`/owner/comments/${id}/reply`, { content }).then(responseData)
export const deleteComment = (id) => request.delete(`/owner/comments/${id}`)
export const getGuestbook = (params = {}) => request.get('/guestbook', { params }).then(responseData)
export const replyGuestbook = (id, content) => request.post(`/owner/guestbook/${id}/reply`, { content }).then(responseData)
export const deleteGuestbook = (id) => request.delete(`/owner/guestbook/${id}`)
