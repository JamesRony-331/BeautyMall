import { createRouter, createWebHistory } from 'vue-router'
import { session } from '../stores/session'
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/products',
      name: 'products',
      component: () => import('../views/ProductsView.vue'),
      meta: { title: '全部商品' },
    },
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
      meta: { title: '发现适合自己的美' },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/AuthView.vue'),
      meta: { title: '登录' },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/AuthView.vue'),
      meta: { title: '创建账号' },
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { title: '个人中心', requiresAuth: true },
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
  scrollBehavior(to) {
    return to.hash ? { el: to.hash, top: 100 } : { top: 0 }
  },
})
router.beforeEach((to) => {
  if (to.meta.requiresAuth && !session.token)
    return { name: 'login', query: { redirect: to.fullPath } }
  if (session.token && ['login', 'register'].includes(to.name)) return { name: 'home' }
})
router.afterEach((to) => {
  document.title = `${to.meta.title} · 美妆灵感`
})
export default router
