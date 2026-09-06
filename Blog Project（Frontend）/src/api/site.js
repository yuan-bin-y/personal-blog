import request from './request'

export function getSiteBootstrap() {
  return request.get('/site/bootstrap').then((response) => response.data.data)
}

const put = (path, body) => request.put(`/owner/site/${path}`, body).then((response) => response.data.data)
export const updateSiteBasic = (body) => put('basic', body)
export const updateHero = (body) => put('hero', body)
export const updateAnnouncement = (body) => put('announcement', body)
export const updateMusic = (body) => put('music', body)
export const updateAppearance = (body) => put('appearance', body)
export const updatePageMedia = (body) => put('page-media', body)
export const updateProfile = (body) => request.put('/owner/profile', body).then((response) => response.data.data)
