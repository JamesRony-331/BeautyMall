<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Menu, ArrowRight, Monitor } from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import AdminNavigation from './components/AdminNavigation.vue'
const router = useRouter()
function logout() {
  sessionStorage.removeItem('beauty-admin-token')
  sessionStorage.removeItem('beauty-admin-id')
  router.replace('/login')
}
const route = useRoute(),
  mobileOpen = ref(false)
watch(
  () => route.fullPath,
  () => {
    mobileOpen.value = false
  },
)
</script>
<template>
  <el-config-provider :locale="zhCn"
    ><a href="#admin-main" class="skip-link">跳到主要内容</a>
    <router-view v-if="route.meta.public" :key="route.name" />
    <div v-else class="admin-shell">
      <aside class="admin-sidebar">
        <router-link to="/" class="admin-brand"
          ><img src="/images/beauty-emblem.png" alt="" width="48" height="48" /><span
            >美妆灵感<small>管理工作台</small></span
          ></router-link
        >
        <p class="nav-label">业务管理</p>
        <AdminNavigation />
        <div class="sidebar-footer">
          <span class="workspace-dot"></span>管理端工作空间<small>Beauty, in your own way.</small>
        </div>
      </aside>
      <div class="admin-body">
        <header class="admin-topbar">
          <div class="breadcrumb">
            <button class="mobile-menu" aria-label="打开管理菜单" @click="mobileOpen = true">
              <el-icon><Menu /></el-icon></button
            ><span>工作台</span><el-icon><ArrowRight /></el-icon
            ><strong>{{ route.meta.title }}</strong>
          </div>
          <div class="topbar-right">
            <el-button text @click="logout">退出登录</el-button>
            <span class="workspace-label"
              ><el-icon><Monitor /></el-icon>管理端</span
            ><span class="admin-avatar" aria-label="管理工作空间">管</span>
          </div>
        </header>
        <main id="admin-main" class="admin-main" tabindex="-1">
          <div class="page-heading">
            <div>
              <h1>{{ route.meta.title }}</h1>
              <p>{{ route.meta.subtitle }}</p>
            </div>
            <span class="page-mode">{{ route.name === 'users' ? '用户管理' : '静态预览' }}</span>
          </div>
          <router-view :key="route.name" />
          <footer class="workspace-footer">
            <span>美妆灵感 · 管理工作台</span><span>清晰管理，从每一处细节开始。</span>
          </footer>
        </main>
      </div>
    </div>
    <el-drawer v-model="mobileOpen" title="美妆灵感 · 管理菜单" direction="ltr" size="280px"
      ><AdminNavigation @navigate="mobileOpen = false" /></el-drawer
  ></el-config-provider>
</template>
