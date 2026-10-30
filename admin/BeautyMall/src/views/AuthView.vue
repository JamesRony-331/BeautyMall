<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createUsersApi } from '../lib/users'
const route = useRoute(),
  router = useRouter()
const register = computed(() => route.name === 'register')
const form = reactive({ email: '', password: '', confirm: '' })
const busy = ref(false),
  error = ref('')
const api = createUsersApi()
async function submit() {
  error.value = ''
  if (register.value && form.password !== form.confirm) {
    error.value = '两次输入的密码不一致。'
    return
  }
  busy.value = true
  try {
    const credentials = { email: form.email.trim(), password: form.password }
    if (register.value) {
      await api.register(credentials)
      await router.replace({ path: '/login', query: { registered: '1' } })
    } else {
      const data = await api.login(credentials)
      if (!data?.token) throw new Error('登录响应缺少凭证，请重试。')
      sessionStorage.setItem('beauty-admin-token', data.token)
      sessionStorage.setItem('beauty-admin-id', String(data.userVO?.id || ''))
      await router.replace('/products')
    }
  } catch (e) {
    error.value = e.message
  } finally {
    busy.value = false
  }
}
</script>
<template>
  <main id="admin-main" class="auth-page">
    <aside class="auth-brand">
      <div class="auth-wordmark">
        <img src="/images/beauty-emblem.png" alt="" width="48" height="48" />美妆灵感
      </div>
      <div class="auth-intro">
        <span>BEAUTY, IN YOUR OWN WAY.</span>
        <h2>美的秩序，<br />从这里开始。</h2>
        <p>美妆灵感 · 管理工作台</p>
      </div>
      <img class="auth-photo" src="/images/beauty-editorial.jpg" alt="美妆产品与光影" />
    </aside>
    <section class="auth-content">
      <form class="auth-form" @submit.prevent="submit">
        <span class="auth-eyebrow">美妆灵感 / 管理端</span>
        <h1>{{ register ? '创建账号' : '欢迎回来' }}</h1>
        <p class="auth-description">
          {{ register ? '填写邮箱与密码，创建你的账号。' : '登录账号，进入管理工作台。' }}
        </p>
        <p v-if="!register && route.query.registered" class="auth-success" role="status">
          注册成功，请登录。
        </p>
        <p v-if="error" class="auth-error" role="alert">{{ error }}</p>
        <label for="auth-email">邮箱</label>
        <el-input
          id="auth-email"
          v-model="form.email"
          type="email"
          required
          maxlength="254"
          autocomplete="username"
          placeholder="请输入邮箱"
          :disabled="busy"
        />
        <label for="auth-password">密码</label>
        <el-input
          id="auth-password"
          v-model="form.password"
          type="password"
          show-password
          required
          :autocomplete="register ? 'new-password' : 'current-password'"
          placeholder="请输入密码"
          :disabled="busy"
        />
        <template v-if="register"
          ><label for="auth-confirm">确认密码</label
          ><el-input
            id="auth-confirm"
            v-model="form.confirm"
            type="password"
            show-password
            required
            autocomplete="new-password"
            placeholder="请再次输入密码"
            :disabled="busy"
        /></template>
        <el-button class="auth-submit" type="primary" native-type="submit" :loading="busy">{{
          register ? '注册账号' : '登录'
        }}</el-button>
        <p class="auth-switch">
          {{ register ? '已有账号？' : '还没有账号？'
          }}<router-link :to="register ? '/login' : '/register'">{{
            register ? '立即登录' : '创建账号'
          }}</router-link>
        </p>
      </form>
      <footer>美妆灵感 · 管理工作台</footer>
    </section>
  </main>
</template>
<style scoped>
.auth-page {
  min-height: 100dvh;
  display: grid;
  grid-template-columns: 46% 54%;
  background: #fff;
}
.auth-brand {
  position: relative;
  background: #eeece8;
  overflow: hidden;
  padding: 44px;
  display: flex;
  flex-direction: column;
}
.auth-wordmark {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 21px;
  font-weight: 600;
  z-index: 1;
}
.auth-intro {
  margin-top: 64px;
  z-index: 1;
}
.auth-intro span,
.auth-eyebrow {
  font-size: 11px;
  letter-spacing: 2px;
  color: #626267;
}
.auth-intro h2 {
  font-size: clamp(32px, 3.5vw, 54px);
  font-weight: 500;
  line-height: 1.4;
  margin: 22px 0;
}
.auth-intro p {
  color: #626267;
}
.auth-photo {
  width: calc(100% + 88px);
  margin: 36px -44px -44px;
  flex: 1;
  min-height: 180px;
  max-height: 440px;
  object-fit: cover;
}
.auth-content {
  padding: 56px 32px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.auth-form {
  width: 100%;
  max-width: 380px;
  margin: auto 0;
  padding: 40px 0;
}
.auth-form h1 {
  font-size: 32px;
  margin: 20px 0 12px;
  font-weight: 550;
}
.auth-description {
  color: #626267;
  margin-bottom: 36px;
  line-height: 1.7;
}
.auth-form label {
  display: block;
  margin: 22px 0 10px;
  font-size: 14px;
}
.auth-form :deep(.el-input__wrapper) {
  min-height: 46px;
}
.auth-submit {
  width: 100%;
  min-height: 48px;
  margin-top: 30px;
}
.auth-switch {
  text-align: center;
  color: #626267;
  font-size: 14px;
  margin-top: 24px;
}
.auth-switch a {
  color: #171717;
  margin-left: 8px;
  text-underline-offset: 5px;
}
.auth-content footer {
  color: #626267;
  font-size: 12px;
}
.auth-error,
.auth-success {
  padding: 12px;
  line-height: 1.6;
  background: #f5f5f7;
  color: #972c2c;
}
.auth-success {
  color: #32634a;
}
@media (max-width: 767px) {
  .auth-page {
    grid-template-columns: 1fr;
  }
  .auth-brand {
    padding: 22px 24px;
  }
  .auth-intro,
  .auth-photo {
    display: none;
  }
  .auth-content {
    padding: 24px;
  }
  .auth-form {
    padding: 20px 0 40px;
  }
}
</style>
