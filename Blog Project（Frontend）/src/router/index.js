import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import MomentsView from '../views/MomentsView.vue'
import MomentDetailView from '../views/MomentDetailView.vue'
import TechView from '../views/TechView.vue'
import TechDetailView from '../views/TechDetailView.vue'
import GuestbookView from '../views/GuestbookView.vue'
import ArchiveView from '../views/ArchiveView.vue'
import AboutView from '../views/AboutView.vue'
import SettingsView from '../views/SettingsView.vue'
import OwnerLoginView from '../views/OwnerLoginView.vue'
import NotFoundView from '../views/NotFoundView.vue'
import SearchView from '../views/SearchView.vue'
import MyLikesView from '../views/MyLikesView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: {
        navMode: 'hero',
        title: '玢的空间',
      },
    },
    {
      path: '/moments',
      name: 'moments',
      component: MomentsView,
      meta: { navMode: 'surface', title: '说说' },
    },
    {
      path: '/moments/:id',
      name: 'moment-detail',
      component: MomentDetailView,
      meta: { navMode: 'surface', title: '说说详情' },
    },
    {
      path: '/tech',
      name: 'tech',
      component: TechView,
      meta: { navMode: 'surface', title: '技术' },
    },
    {
      path: '/tech/:slug',
      name: 'tech-detail',
      component: TechDetailView,
      meta: { navMode: 'surface', title: '技术文章' },
    },
    {
      path: '/guestbook',
      name: 'guestbook',
      component: GuestbookView,
      meta: { navMode: 'surface', title: '留言板' },
    },
    {
      path: '/archive',
      name: 'archive',
      component: ArchiveView,
      meta: { navMode: 'surface', title: '归档' },
    },
    {
      path: '/about',
      name: 'about',
      component: AboutView,
      meta: { navMode: 'surface', title: '关于' },
    },
    {
      path: '/owner-login',
      name: 'owner-login',
      component: OwnerLoginView,
      meta: { navMode: 'surface', title: '登录与注册' },
    },
    {
      path: '/settings',
      name: 'settings',
      component: SettingsView,
      meta: { navMode: 'surface', title: '空间设置' },
    },
    {
      path: '/search',
      name: 'search',
      component: SearchView,
      meta: { navMode: 'surface', title: '搜索' },
    },
    {
      path: '/me/likes',
      name: 'my-likes',
      component: MyLikesView,
      meta: { navMode: 'surface', title: '我的点赞' },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: NotFoundView,
      meta: { navMode: 'surface', title: '页面未找到' },
    },
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash, top: 96, behavior: 'smooth' }
    return { top: 0 }
  },
})

router.afterEach((to) => {
  document.title = `${to.meta.title || '玢的空间'} · BinSpace`
})

export default router
