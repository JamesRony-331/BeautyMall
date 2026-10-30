import { createRouter, createWebHistory } from 'vue-router'
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/AuthView.vue'),
      meta: { title: '登录', public: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/AuthView.vue'),
      meta: { title: '注册', public: true },
    },
    { path: '/', redirect: '/products' },
    {
      path: '/products',
      name: 'products',
      component: () => import('../views/ProductsView.vue'),
      meta: { title: '商品管理', subtitle: '维护商品信息，管理上架状态与库存。' },
    },
    {
      path: '/users',
      name: 'users',
      component: () => import('../views/UsersView.vue'),
      meta: { title: '用户管理', subtitle: '查询用户信息与账号状态。' },
    },
    {
      path: '/categories',
      name: 'categories',
      component: () => import('../views/CategoriesView.vue'),
      meta: { title: '分类管理', subtitle: '整理商品分类，维护清晰的商品目录。' },
    },
    { path: '/:pathMatch(.*)*', redirect: '/products' },
  ],
  scrollBehavior: () => ({ top: 0 }),
})
router.beforeEach((to) => {
  const authenticated = Boolean(sessionStorage.getItem('beauty-admin-token'))
  if (!to.meta.public && !authenticated) return '/login'
  if (to.meta.public && authenticated) return '/products'
})
router.afterEach((to) => {
  document.title = `${to.meta.title} · 美妆灵感管理端`
})
export default router
