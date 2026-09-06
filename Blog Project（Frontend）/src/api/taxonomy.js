import request, { responseData } from './request'

export const getCategories = (params = {}) => request.get('/categories', { params }).then(responseData)
export const getOwnerCategories = (params = {}) => request.get('/owner/categories', { params }).then(responseData)
export const createCategory = (body) => request.post('/owner/categories', body).then(responseData)
export const updateCategory = (id, body) => request.put(`/owner/categories/${id}`, body).then(responseData)
export const deleteCategory = (id, version) => request.delete(`/owner/categories/${id}`, { params: { version } })
export const getTags = (params = {}) => request.get('/tags', { params }).then(responseData)
export const getOwnerTags = (params = {}) => request.get('/owner/tags', { params }).then(responseData)
export const createTag = (body) => request.post('/owner/tags', body).then(responseData)
export const updateTag = (id, body) => request.put(`/owner/tags/${id}`, body).then(responseData)
export const deleteTag = (id) => request.delete(`/owner/tags/${id}`)
