<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { User, Menu, Close } from '@element-plus/icons-vue'
import { session } from '../stores/session'
const open = ref(false)
const route = useRoute()
watch(
  () => route.fullPath,
  () => {
    open.value = false
  },
)
</script>
<template>
  <header class="site-header" @keydown.esc="open = false">
    <div class="header-inner">
      <router-link class="brand" to="/" aria-label="美妆灵感，首页"
        ><img
          class="brand-logo"
          src="/images/beauty-emblem.png"
          alt=""
          width="64"
          height="64"
        /><span>美妆灵感<small>Beauty, in your own way.</small></span></router-link
      >
      <nav class="desktop-nav" aria-label="主要导航">
        <router-link to="/" :class="{ active: route.name === 'home' }">首页</router-link>
        <router-link to="/products" :class="{ active: route.name === 'products' }"
          >全部商品</router-link
        ><router-link to="/#discover">探索灵感</router-link>
        <router-link to="/#guide">智能导购 <span class="nav-soon">即将开放</span></router-link>
      </nav>
      <div class="header-actions">
        <router-link :to="session.token ? '/profile' : '/login'" class="account-link"
          ><el-icon><User /></el-icon
          ><span>{{ session.token ? '个人中心' : '登录 / 注册' }}</span></router-link
        >
        <button
          class="icon-button mobile-toggle"
          :aria-expanded="open"
          aria-controls="mobile-menu"
          :aria-label="open ? '关闭导航' : '打开导航'"
          @click="open = !open"
        >
          <el-icon><Close v-if="open" /><Menu v-else /></el-icon>
        </button>
      </div>
    </div>
    <nav v-if="open" id="mobile-menu" class="mobile-nav" aria-label="手机导航">
      <router-link to="/">首页</router-link><router-link to="/#discover">探索灵感</router-link
      ><router-link to="/#guide">智能导购 · 即将开放</router-link>
    </nav>
  </header>
</template>
