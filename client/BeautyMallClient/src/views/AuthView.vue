<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { accountApi, startSession } from '../stores/session'
import { safeRedirect } from '../lib/account'
const route = useRoute(),
  router = useRouter()
const register = computed(() => route.name === 'register')
const formRef = ref(),
  busy = ref(false),
  error = ref('')
const form = reactive({ email: '', password: '', confirmPassword: '' })
const rules = computed(() => ({
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    ...(register.value
      ? [{ min: 8, max: 64, message: '密码长度为 8–64 位', trigger: 'blur' }]
      : []),
  ],
  confirmPassword: [
    {
      validator: (_, value, callback) =>
        callback(
          !value
            ? new Error('请再次输入密码')
            : value !== form.password
              ? new Error('两次输入的密码不一致')
              : undefined,
        ),
      trigger: 'blur',
    },
  ],
}))
watch(
  () => route.name,
  () => {
    form.password = ''
    form.confirmPassword = ''
    error.value = ''
    form.email = typeof route.query.email === 'string' ? route.query.email : ''
    formRef.value?.clearValidate()
  },
  { immediate: true },
)
async function submit() {
  if (busy.value || !(await formRef.value.validate().catch(() => false))) return
  busy.value = true
  error.value = ''
  const mode = route.name
  try {
    const credentials = { email: form.email.trim(), password: form.password }
    if (register.value) {
      await accountApi.register(credentials)
      if (route.name !== mode) return
      ElMessage.success('注册成功，请登录')
      await router.replace({
        name: 'login',
        query: { email: credentials.email, redirect: safeRedirect(route.query.redirect) },
      })
    } else {
      const data = await accountApi.login(credentials)
      if (route.name !== mode) return
      startSession(data, credentials.email)
      await router.replace({ name: 'home' })
    }
  } catch (e) {
    error.value = e.message
  } finally {
    busy.value = false
  }
}
</script>
<template>
  <div class="auth-page">
    <aside class="auth-visual">
      <img
        src="/images/beauty-editorial.jpg"
        alt="自然光下的美妆产品静物"
        width="1536"
        height="1024"
      />
      <div class="auth-visual-copy">
        <span>美妆灵感</span>
        <h2>每一种美，<br />都值得被发现。</h2>
        <p>从这里，开启属于你的美妆探索。</p>
      </div>
      <span class="auth-visual-bottom">Beauty, in your own way.</span>
    </aside>
    <section class="auth-panel" :aria-labelledby="register ? 'register-title' : 'login-title'">
      <router-link to="/" class="back-link"
        ><el-icon><ArrowLeft /></el-icon>返回首页</router-link
      >
      <div class="auth-form-wrap">
        <p class="section-label">{{ register ? '开始你的美妆探索' : '很高兴再次见到你' }}</p>
        <h1 :id="register ? 'register-title' : 'login-title'">
          {{ register ? '创建你的账号' : '欢迎回来' }}
        </h1>
        <p class="form-intro">
          {{ register ? '以自己的方式，发现日常之美。' : '登录，继续属于你的美妆旅程。' }}
        </p>
        <div class="auth-tabs" aria-label="账号入口">
          <router-link
            :to="{ name: 'login', query: { redirect: safeRedirect(route.query.redirect) } }"
            :class="{ selected: !register }"
            >登录</router-link
          ><router-link
            :to="{ name: 'register', query: { redirect: safeRedirect(route.query.redirect) } }"
            :class="{ selected: register }"
            >注册</router-link
          >
        </div>
        <el-alert
          v-if="route.query.expired && !register"
          title="登录已过期，请重新登录。"
          type="warning"
          :closable="false"
          show-icon
        />
        <div v-if="error" class="form-error" role="alert">{{ error }}</div>
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
          :disabled="busy"
          @submit.prevent="submit"
        >
          <el-form-item label="邮箱地址" prop="email"
            ><el-input
              aria-label="邮箱地址"
              v-model.trim="form.email"
              type="email"
              placeholder="请输入邮箱地址"
              autocomplete="username"
              maxlength="254"
          /></el-form-item>
          <el-form-item label="密码" prop="password"
            ><el-input
              aria-label="密码"
              v-model="form.password"
              type="password"
              show-password
              :placeholder="register ? '设置 8–64 位密码' : '请输入密码'"
              :autocomplete="register ? 'new-password' : 'current-password'"
              :maxlength="register ? 64 : undefined"
          /></el-form-item>
          <el-form-item v-if="register" label="确认密码" prop="confirmPassword"
            ><el-input
              aria-label="确认密码"
              v-model="form.confirmPassword"
              type="password"
              show-password
              placeholder="请再次输入密码"
              autocomplete="new-password"
              maxlength="64"
          /></el-form-item>
          <el-button native-type="submit" type="primary" class="submit-button" :loading="busy"
            >{{ register ? '创建账号' : '登录' }}<el-icon v-if="!busy"><ArrowRight /></el-icon
          ></el-button>
        </el-form>
        <p class="auth-switch">
          {{ register ? '已经有账号？' : '还没有账号？' }}
          <router-link
            :to="{
              name: register ? 'login' : 'register',
              query: { redirect: safeRedirect(route.query.redirect) },
            }"
            >{{ register ? '立即登录' : '创建账号' }}</router-link
          >
        </p>
        <p class="auth-note">你的美，由你定义。</p>
      </div>
    </section>
  </div>
</template>
