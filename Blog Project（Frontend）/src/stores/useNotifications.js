import { computed, reactive } from 'vue'
import * as api from '../api/notifications'
import { apiMessage } from '../api/request'

const state = reactive({ items: [], unreadCount: 0, connected: false, loading: false, error: '', lastEventId: '' })
let controller
let reconnectTimer

const merge = (item, prepend = true) => {
  if (!item?.id) return
  const index = state.items.findIndex((entry) => entry.id === item.id)
  if (index >= 0) state.items[index] = item
  else if (prepend) state.items.unshift(item)
  if (!item.read && index < 0) state.unreadCount += 1
}
const load = async () => {
  state.loading = true
  try {
    const page = await api.getNotifications({ page: 1, pageSize: 30 })
    state.items = page.items
    state.unreadCount = page.items.filter((item) => !item.read).length
  } catch (error) { state.error = apiMessage(error) } finally { state.loading = false }
}
const stop = () => { controller?.abort(); controller = null; state.connected = false; clearTimeout(reconnectTimer) }
const connect = () => {
  stop()
  controller = new AbortController()
  api.subscribeNotifications({
    signal: controller.signal,
    lastEventId: state.lastEventId,
    onEvent: (event) => {
      state.connected = true
      if (event.id) state.lastEventId = event.id
      if (typeof event.data === 'object') merge(event.data)
    },
  }).catch((error) => {
    if (controller?.signal.aborted) return
    state.connected = false
    state.error = apiMessage(error)
    reconnectTimer = window.setTimeout(connect, 4000)
  })
}
const start = async () => { await load(); connect() }
const markRead = async (id) => {
  const item = await api.markNotificationRead(id)
  const before = state.items.find((entry) => entry.id === id)
  if (before && !before.read) state.unreadCount = Math.max(0, state.unreadCount - 1)
  merge(item, false)
  return item
}
const markAllRead = async () => {
  const result = await api.markAllNotificationsRead()
  state.items = state.items.map((item) => ({ ...item, read: true }))
  state.unreadCount = result.unreadCount
}

export const useNotifications = () => ({ state, unreadCount: computed(() => state.unreadCount), start, stop, load, markRead, markAllRead })
