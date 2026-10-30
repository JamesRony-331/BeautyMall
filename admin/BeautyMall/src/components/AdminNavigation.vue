<script setup>
import { Goods, User, FolderOpened } from '@element-plus/icons-vue'
const emit = defineEmits(['navigate'])
const items = [
  { path: '/products', label: '商品管理', icon: Goods },
  { path: '/users', label: '用户管理', icon: User },
  { path: '/categories', label: '分类管理', icon: FolderOpened },
]
</script>
<template>
  <nav class="admin-nav" aria-label="管理菜单">
    <router-link v-for="item in items" :key="item.path" :to="item.path" @click="emit('navigate')"
      ><el-icon><component :is="item.icon" /></el-icon><span>{{ item.label }}</span></router-link
    >
  </nav>
</template>
