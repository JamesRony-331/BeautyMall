import test from 'node:test'
import assert from 'node:assert/strict'
import { changedProfile, safeRedirect, safeAvatar } from '../src/lib/account.js'
import { createAccountApi } from '../src/api/account.js'

test('only changed profile fields are submitted; unknown data is preserved', () => {
  assert.deepEqual(
    changedProfile(
      { realName: '', phone: '', avatar: '' },
      { realName: ' 林 ', phone: '', avatar: '' },
    ),
    { realName: '林' },
  )
  assert.deepEqual(changedProfile({ phone: '123' }, { phone: '' }), { phone: '' })
})
test('return destination cannot be an external URL or auth loop', () => {
  for (const value of ['https://evil.test', '//evil.test', '/\\evil', '/login', '/register', null])
    assert.equal(safeRedirect(value), '/')
  assert.equal(safeRedirect('/profile?tab=security'), '/profile?tab=security')
})
test('avatars allow https but reject executable or insecure schemes', () => {
  assert.equal(safeAvatar('javascript:alert(1)'), '')
  assert.equal(safeAvatar('https://example.com/a.jpg'), 'https://example.com/a.jpg')
})
test('login sends form encoded credentials and returns server payload', async () => {
  let request
  const api = createAccountApi({
    fetcher: async (url, options) => {
      request = { url, ...options }
      return Response.json({ code: 200, data: { token: 'token' } })
    },
  })
  assert.deepEqual(await api.login({ email: 'a+b@example.com', password: 'x&y' }), {
    token: 'token',
  })
  assert.equal(request.url, '/api/sys-user/login')
  assert.equal(new URLSearchParams(request.body).get('email'), 'a+b@example.com')
  assert.equal(request.method, 'POST')
})
test('profile update uses JSON and bearer token', async () => {
  let request
  const api = createAccountApi({
    token: () => 'abc',
    fetcher: async (_, options) => {
      request = options
      return Response.json({ code: 200, data: 'ok' })
    },
  })
  await api.updateInfo({ phone: '123' })
  assert.equal(request.headers.Authorization, 'Bearer abc')
  assert.equal(request.method, 'PUT')
  assert.deepEqual(JSON.parse(request.body), { phone: '123' })
})
test('expired session notifies session owner and surfaces backend message', async () => {
  let expired = false
  const api = createAccountApi({
    onUnauthorized: () => {
      expired = true
    },
    fetcher: async () => Response.json({ code: 401, info: '登录已过期' }, { status: 401 }),
  })
  await assert.rejects(api.updateInfo({ phone: '123' }), /登录已过期/)
  assert.equal(expired, true)
})
test('business and non JSON failures do not report success', async () => {
  const api = createAccountApi({
    fetcher: async () => Response.json({ code: 500, info: '邮箱已注册' }),
  })
  await assert.rejects(api.register({}), /邮箱已注册/)
  const bad = createAccountApi({
    fetcher: async () => new Response('<html>error</html>', { status: 502 }),
  })
  await assert.rejects(bad.login({}), /服务暂时不可用/)
})
