import request, { responseData } from './request'

export const likePost = (postId) => request.put(`/posts/${postId}/like`).then(responseData)
export const unlikePost = (postId) => request.delete(`/posts/${postId}/like`).then(responseData)
export const getMyLikes = (params = {}) => request.get('/me/likes', { params }).then(responseData)
