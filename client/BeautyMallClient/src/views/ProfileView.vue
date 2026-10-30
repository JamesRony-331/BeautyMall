<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  User,
  Lock,
  SwitchButton,
  ArrowRight,
  Check,
  Location,
  Star,
  Tickets,
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { session, accountApi, clearSession, updateSessionProfile } from '../stores/session'
import { changedProfile, safeAvatar } from '../lib/account'
import AccountCollection from '../components/AccountCollection.vue'
const router = useRouter(),
  route = useRoute()
const sections = [
  { key: 'info', label: '个人资料', description: '完善你的个人信息。', icon: User },
  {
    key: 'addresses',
    label: '个人地址管理',
    description: '管理收货信息，让好物准确送达。',
    icon: Location,
  },
  {
    key: 'favorites',
    label: '我的收藏',
    description: '留住心动，随时回看喜欢的美妆。',
    icon: Star,
  },
  {
    key: 'orders',
    label: '我的订单',
    description: '查看订单进度与退款、售后状态。',
    icon: Tickets,
  },
  { key: 'security', label: '账号安全', description: '定期更新密码，保护你的账号。', icon: Lock },
]
const activeSection = computed(
  () => sections.find((item) => item.key === route.query.tab) || sections[0],
)
const tab = computed(() => activeSection.value.key)
const profileRef = ref(),
  passwordRef = ref(),
  busy = ref(false),
  error = ref('')
const original = reactive({
  realName: session.user.realName || '',
  phone: session.user.phone || '',
})
const profile = reactive({ ...original })
const password = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const displayName = computed(() => session.user.realName || '美妆探索者')
const dirty = computed(() => Object.keys(changedProfile(original, profile)).length > 0)
const profileRules = {
  realName: [
    {
      validator: (_, value, cb) =>
        cb(
          value !== original.realName && !value.trim()
            ? new Error('姓名不能为空')
            : value.length > 64
              ? new Error('姓名不能超过 64 个字符')
              : undefined,
        ),
      trigger: 'blur',
    },
  ],
  phone: [{ max: 20, message: '联系电话不能超过 20 个字符', trigger: 'blur' }],
}
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 64, message: '新密码长度为 8–64 位', trigger: 'blur' },
    {
      validator: (_, value, cb) =>
        cb(
          value && value === password.oldPassword
            ? new Error('新密码不能与当前密码相同')
            : undefined,
        ),
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    {
      validator: (_, value, cb) =>
        cb(
          !value
            ? new Error('请再次输入新密码')
            : value !== password.newPassword
              ? new Error('两次输入的新密码不一致')
              : undefined,
        ),
      trigger: 'blur',
    },
  ],
}
function selectTab(value) {
  if (!busy.value) {
    error.value = ''
    router.replace({ name: 'profile', query: value === 'info' ? {} : { tab: value } })
  }
}
async function saveProfile() {
  if (busy.value || !(await profileRef.value.validate().catch(() => false))) return
  const changes = changedProfile(original, profile)
  if (!Object.keys(changes).length) return
  busy.value = true
  error.value = ''
  try {
    await accountApi.updateInfo(changes)
    updateSessionProfile(changes)
    Object.assign(original, changes)
    Object.assign(profile, changes)
    ElMessage.success('个人资料已保存')
  } catch (e) {
    error.value = e.message
  } finally {
    busy.value = false
  }
}
async function savePassword() {
  if (busy.value || !(await passwordRef.value.validate().catch(() => false))) return
  busy.value = true
  error.value = ''
  try {
    await accountApi.updatePassword({ ...password })
    clearSession()
    ElMessage.success('密码已修改，请使用新密码登录')
    await router.replace('/login')
  } catch (e) {
    error.value = e.message
  } finally {
    busy.value = false
  }
}
async function logout() {
  try {
    await ElMessageBox.confirm('退出后，需重新登录才能管理个人资料。', '退出登录', {
      confirmButtonText: '退出登录',
      cancelButtonText: '取消',
      type: 'info',
    })
  } catch {
    return
  }
  clearSession()
  router.replace('/')
}
</script>
<template>
  <div class="profile-page section-wrap">
    <div class="profile-heading">
      <p class="section-label">我的美妆空间</p>
      <h1>个人中心</h1>
      <p>管理个人资料，让每一次探索都有迹可循。</p>
    </div>
    <div class="profile-layout">
      <aside class="profile-sidebar">
        <div class="identity">
          <el-avatar :size="64" :src="safeAvatar(session.user.avatar)" class="identity-avatar">{{
            displayName.slice(0, 1)
          }}</el-avatar>
          <h2>{{ displayName }}</h2>
          <p>{{ session.user.email }}</p>
          <span class="identity-status"
            ><el-icon><Check /></el-icon>已登录</span
          >
        </div>
        <nav aria-label="个人中心导航">
          <button
            v-for="item in sections"
            :key="item.key"
            :class="{ selected: tab === item.key }"
            :aria-current="tab === item.key ? 'page' : undefined"
            :disabled="busy"
            @click="selectTab(item.key)"
          >
            <el-icon><component :is="item.icon" /></el-icon>{{ item.label
            }}<el-icon class="nav-arrow"><ArrowRight /></el-icon>
          </button>
          <button class="logout-button" :disabled="busy" @click="logout">
            <el-icon><SwitchButton /></el-icon>退出登录
          </button>
        </nav>
        <div class="sidebar-note">美不止一种答案。<br />在这里，找到自己的表达。</div>
      </aside>
      <section class="profile-main" aria-labelledby="profile-section-title">
        <div class="profile-section-title">
          <div>
            <h2 id="profile-section-title">
              {{ activeSection.label }}
            </h2>
            <p>{{ activeSection.description }}</p>
          </div>
          <el-icon><component :is="activeSection.icon" /></el-icon>
        </div>
        <div v-if="error" class="form-error" role="alert">{{ error }}</div>
        <template v-if="tab === 'info'">
          <div v-if="!session.hasServerProfile" class="profile-notice" role="status">
            当前无法读取已有资料，仅提交本次修改的字段。已保存的修改会显示在本次登录会话中。
          </div>
          <el-form
            ref="profileRef"
            :model="profile"
            :rules="profileRules"
            label-position="top"
            size="large"
            :disabled="busy"
            class="profile-form"
            @submit.prevent="saveProfile"
          >
            <div class="form-columns">
              <el-form-item label="姓名" prop="realName"
                ><el-input
                  aria-label="姓名"
                  v-model="profile.realName"
                  placeholder="请输入姓名"
                  maxlength="64"
                  autocomplete="name" /></el-form-item
              ><el-form-item label="联系电话" prop="phone"
                ><el-input
                  aria-label="联系电话"
                  v-model.trim="profile.phone"
                  placeholder="请输入联系电话"
                  maxlength="20"
                  type="tel"
                  autocomplete="tel"
              /></el-form-item>
            </div>
            <el-form-item label="登录邮箱"
              ><el-input
                aria-label="登录邮箱"
                :model-value="session.user.email"
                readonly
                autocomplete="email"
              /><span class="field-help">邮箱用于登录，暂不支持修改。</span></el-form-item
            >
            <div class="form-actions">
              <el-button type="primary" native-type="submit" :loading="busy" :disabled="!dirty"
                >保存修改</el-button
              ><span>{{ dirty ? '你有尚未保存的修改' : '资料修改后可保存' }}</span>
            </div>
          </el-form>
        </template>
        <el-form
          v-else-if="tab === 'security'"
          ref="passwordRef"
          :model="password"
          :rules="passwordRules"
          label-position="top"
          size="large"
          :disabled="busy"
          class="password-form"
          @submit.prevent="savePassword"
        >
          <el-form-item label="当前密码" prop="oldPassword"
            ><el-input
              aria-label="当前密码"
              v-model="password.oldPassword"
              type="password"
              show-password
              autocomplete="current-password"
              placeholder="请输入当前密码"
          /></el-form-item>
          <el-form-item label="新密码" prop="newPassword"
            ><el-input
              aria-label="新密码"
              v-model="password.newPassword"
              type="password"
              show-password
              autocomplete="new-password"
              maxlength="64"
              placeholder="设置 8–64 位新密码"
          /></el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword"
            ><el-input
              aria-label="确认新密码"
              v-model="password.confirmPassword"
              type="password"
              show-password
              autocomplete="new-password"
              maxlength="64"
              placeholder="请再次输入新密码"
          /></el-form-item>
          <p class="field-help">修改成功后，需要使用新密码重新登录。</p>
          <div class="form-actions">
            <el-button type="primary" native-type="submit" :loading="busy">更新密码</el-button>
          </div>
        </el-form>
        <AccountCollection v-else :section="tab" />
      </section>
    </div>
  </div>
</template>
