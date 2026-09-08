<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useNotifications } from '../../stores/useNotifications'
import { useSpaceRuntime } from '../../stores/useSpaceRuntime'

const open = ref(false)
const router = useRouter()
const notices = useNotifications()
const space = useSpaceRuntime()
const time = (value) => new Intl.DateTimeFormat('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }).format(new Date(value))
const visit = async (item) => {
  if (!item.read) await notices.markRead(item.id)
  open.value = false
  if (item.resourceType === 'GUESTBOOK') router.push('/guestbook')
  else if (item.resourceType === 'POST' && item.resourceId) {
    const post = space.state.posts.find((entry) => String(entry.id) === String(item.resourceId))
    if (post) router.push(post.type === 'TECH' ? `/tech/${post.slug}` : `/moments/${post.id}`)
  }
}
</script>

<template>
  <div class="notification-center">
    <button type="button" class="notification-center__bell" :aria-expanded="open" aria-label="通知" @click="open = !open">♢<span v-if="notices.unreadCount.value">{{ notices.unreadCount.value > 99 ? '99+' : notices.unreadCount.value }}</span></button>
    <section v-if="open" class="notification-center__panel">
      <header><div><strong>空间消息</strong><small>{{ notices.state.connected ? '实时连接中' : '消息记录' }}</small></div><button v-if="notices.unreadCount.value" type="button" @click="notices.markAllRead">全部已读</button></header>
      <div v-if="notices.state.items.length" class="notification-center__list"><button v-for="item in notices.state.items" :key="item.id" type="button" :class="{ unread: !item.read }" @click="visit(item)"><span>{{ (item.actor?.name || '系').slice(0, 1) }}</span><div><strong>{{ item.summary }}</strong><small>{{ time(item.createdAt) }}</small></div></button></div>
      <p v-else>暂时没有新消息。</p>
    </section>
  </div>
</template>

<style scoped>
.notification-center{position:relative}.notification-center__bell{position:relative;display:grid;width:38px;height:38px;place-items:center;color:inherit;background:rgb(255 255 255/.12);border:1px solid currentColor;border-radius:50%;cursor:pointer;font-size:1.25rem}.notification-center__bell>span{position:absolute;top:-5px;right:-5px;display:grid;min-width:19px;height:19px;padding:0 4px;place-items:center;color:white;background:#b75d68;border:2px solid var(--color-surface);border-radius:99px;font-size:.6rem;font-weight:800}.notification-center__panel{position:absolute;top:calc(100% + 14px);right:0;width:min(360px,calc(100vw - 28px));max-height:480px;overflow:auto;color:var(--color-text-primary);background:rgb(252 251 249/.98);border:1px solid var(--color-border);border-radius:var(--radius-lg);box-shadow:var(--shadow-2)}.notification-center__panel>header{position:sticky;top:0;display:flex;align-items:center;justify-content:space-between;padding:var(--space-4);background:inherit;border-bottom:1px solid var(--color-border)}.notification-center__panel header div{display:grid}.notification-center__panel small{color:var(--color-text-secondary);font-size:.7rem}.notification-center__panel header button{color:var(--color-accent);background:transparent;border:0;cursor:pointer;font-size:.75rem;font-weight:700}.notification-center__list{display:grid}.notification-center__list>button{display:grid;grid-template-columns:36px 1fr;gap:var(--space-3);padding:var(--space-3) var(--space-4);color:inherit;background:transparent;border:0;border-bottom:1px solid var(--color-border);cursor:pointer;text-align:left}.notification-center__list>button.unread{background:var(--color-accent-soft)}.notification-center__list>button>span{display:grid;width:36px;height:36px;place-items:center;background:var(--color-surface);border-radius:50%;font-family:var(--font-serif)}.notification-center__list>button>div{display:grid;gap:4px}.notification-center__list strong{font-size:.8125rem;line-height:1.5}.notification-center__panel>p{margin:0;padding:var(--space-6);color:var(--color-text-secondary)}
</style>
