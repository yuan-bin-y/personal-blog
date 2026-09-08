import request, { responseData } from './request'

export const uploadMedia = (file, usageType, onUploadProgress) => {
  const form = new FormData()
  form.append('file', file)
  return request.post('/owner/media', form, {
    params: { usageType },
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress,
    timeout: 120000,
  }).then(responseData)
}

export const getMedia = (params = {}) => request.get('/owner/media', { params }).then(responseData)
export const deleteMedia = (mediaId) => request.delete(`/owner/media/${mediaId}`)
