import request, { getAccessToken, responseData } from './request'

export const getNotifications = (params = {}) => request.get('/notifications', { params }).then(responseData)
export const markNotificationRead = (id) => request.put(`/notifications/${id}/read`).then(responseData)
export const markAllNotificationsRead = () => request.put('/notifications/read-all').then(responseData)

export const subscribeNotifications = async ({ signal, lastEventId = '', onEvent }) => {
  const headers = { Accept: 'text/event-stream' }
  const token = getAccessToken()
  if (token) headers.Authorization = `Bearer ${token}`
  if (lastEventId) headers['Last-Event-ID'] = lastEventId
  const response = await fetch('/api/realtime/events', { headers, signal })
  if (!response.ok) throw new Error(`实时连接失败（${response.status}）`)
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  while (!signal.aborted) {
    const { value, done } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const blocks = buffer.split(/\r?\n\r?\n/)
    buffer = blocks.pop() || ''
    for (const block of blocks) {
      const parsed = { id: '', event: 'message', data: '' }
      for (const line of block.split(/\r?\n/)) {
        if (line.startsWith('id:')) parsed.id = line.slice(3).trim()
        else if (line.startsWith('event:')) parsed.event = line.slice(6).trim()
        else if (line.startsWith('data:')) parsed.data += line.slice(5).trim()
      }
      if (!parsed.data) continue
      try { parsed.data = JSON.parse(parsed.data) } catch { /* heartbeat/plain event */ }
      onEvent?.(parsed)
    }
  }
}
