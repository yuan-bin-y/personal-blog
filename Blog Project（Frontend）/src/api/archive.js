import request, { responseData } from './request'

export const getArchive = (params = {}) => request.get('/archive', { params }).then(responseData)
