import { reactive } from 'vue'
import { createAccountApi } from '../api/account'
const KEY = 'beauty-session-v1'
function restore() {
  try {
    const value = JSON.parse(sessionStorage.getItem(KEY))
    return typeof value?.token === 'string' && value.token
      ? {
          token: value.token,
          user: value.user || {},
          hasServerProfile: Boolean(value.hasServerProfile),
        }
      : null
  } catch {
    return null
  }
}
export const session = reactive(restore() || { token: '', user: {}, hasServerProfile: false })
function persist() {
  try {
    sessionStorage.setItem(KEY, JSON.stringify(session))
  } catch {
    /* Session remains usable in memory when browser storage is unavailable. */
  }
}
export function clearSession() {
  session.token = ''
  session.user = {}
  session.hasServerProfile = false
  try {
    sessionStorage.removeItem(KEY)
  } catch {
    /* Storage can be disabled. */
  }
}
export function startSession(data, email) {
  if (typeof data?.token !== 'string' || !data.token.trim())
    throw new Error('登录响应缺少凭证，请联系管理员。')
  session.token = data.token
  const user = data.userVO
  session.hasServerProfile = Boolean(user && typeof user === 'object')
  session.user = { email, ...(session.hasServerProfile ? user : {}) }
  persist()
}
export function updateSessionProfile(changes) {
  session.user = { ...session.user, ...changes }
  persist()
}
export const accountApi = createAccountApi({
  base: import.meta.env.VITE_API_BASE_URL || '/api',
  token: () => session.token,
  onUnauthorized: () => {
    clearSession()
    window.dispatchEvent(new Event('session-expired'))
  },
})
