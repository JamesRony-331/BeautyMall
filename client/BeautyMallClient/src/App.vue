<script setup>
import { onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import SiteHeader from './components/SiteHeader.vue'
import SiteFooter from './components/SiteFooter.vue'
const router = useRouter()
function expired() {
  router.replace({
    name: 'login',
    query: { redirect: router.currentRoute.value.fullPath, expired: '1' },
  })
}
let removeHook
onMounted(() => {
  window.addEventListener('session-expired', expired)
  removeHook = router.afterEach(async (to, from) => {
    if (!from.matched.length) return
    await nextTick()
    document.querySelector('#main-content')?.focus({ preventScroll: true })
  })
})
onUnmounted(() => {
  window.removeEventListener('session-expired', expired)
  removeHook?.()
})
</script>
<template>
  <a class="skip-link" href="#main-content">跳到主要内容</a>
  <SiteHeader />
  <main id="main-content" tabindex="-1"><router-view /></main>
  <SiteFooter />
</template>
