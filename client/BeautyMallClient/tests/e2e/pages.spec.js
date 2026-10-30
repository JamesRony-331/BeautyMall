import { test, expect } from '@playwright/test'
const ok = (data) => ({
  status: 200,
  contentType: 'application/json',
  body: JSON.stringify({ code: 200, data }),
})
async function mockAccount(page) {
  const requests = []
  await page.route('**/api/sys-user/**', async (route) => {
    requests.push({
      url: route.request().url(),
      body: route.request().postData(),
      headers: route.request().headers(),
    })
    await route.fulfill(
      ok(route.request().url().endsWith('/login') ? { token: 'test-token', userVO: null } : '成功'),
    )
  })
  return requests
}
async function login(page) {
  await page.goto('/login')
  await page.getByRole('textbox', { name: '邮箱地址', exact: true }).fill('beauty@example.com')
  await page.getByRole('textbox', { name: '密码', exact: true }).fill('Password123')
  await page.getByRole('button', { name: '登录', exact: true }).click()
  await expect(page).toHaveURL('http://127.0.0.1:5173/')
  await page.getByRole('link', { name: '个人中心', exact: true }).click()
  await expect(page).toHaveURL(/\/profile$/)
}
test('registration validates confirmation, then login and sparse profile update work', async ({
  page,
}) => {
  const requests = await mockAccount(page)
  await page.goto('/register')
  await page.getByRole('textbox', { name: '邮箱地址', exact: true }).fill('beauty@example.com')
  await page.getByRole('textbox', { name: '密码', exact: true }).fill('Password123')
  await page.getByRole('textbox', { name: '确认密码', exact: true }).fill('Mismatch')
  await page.getByRole('button', { name: '创建账号', exact: true }).click()
  await expect(page.getByText('两次输入的密码不一致')).toBeVisible()
  expect(requests).toHaveLength(0)
  await page.getByRole('textbox', { name: '确认密码', exact: true }).fill('Password123')
  await page.getByRole('button', { name: '创建账号', exact: true }).click()
  await expect(page).toHaveURL(/\/login\?/)
  await expect(page.getByRole('textbox', { name: '邮箱地址', exact: true })).toHaveValue(
    'beauty@example.com',
  )
  await page.getByRole('textbox', { name: '密码', exact: true }).fill('Password123')
  await page.getByRole('button', { name: '登录', exact: true }).click()
  await expect(page).toHaveURL('http://127.0.0.1:5173/')
  await page.getByRole('link', { name: '个人中心', exact: true }).click()
  await expect(page).toHaveURL(/\/profile$/)
  await expect(page.getByText('当前无法读取已有资料', { exact: false })).toBeVisible()
  await page.getByRole('textbox', { name: '姓名', exact: true }).fill('林同学')
  await page.getByRole('button', { name: '保存修改' }).click()
  await expect(page.getByText('个人资料已保存', { exact: true })).toBeVisible()
  const update = requests.find((r) => r.url.endsWith('/info'))
  expect(JSON.parse(update.body)).toEqual({ realName: '林同学' })
  expect(update.headers.authorization).toBe('Bearer test-token')
  await page.reload()
  await expect(page.getByRole('textbox', { name: '姓名', exact: true })).toHaveValue('林同学')
  await page.screenshot({
    path: 'docs/previews/profile-desktop.png',
    fullPage: true,
    animations: 'disabled',
  })
  await page.getByRole('button', { name: '账号安全' }).click()
  await page.getByRole('textbox', { name: '当前密码', exact: true }).fill('Password123')
  await page.getByRole('textbox', { name: '新密码', exact: true }).fill('NewPassword123')
  await page.getByRole('textbox', { name: '确认新密码', exact: true }).fill('NewPassword123')
  await page.getByRole('button', { name: '更新密码' }).click()
  await expect(page).toHaveURL(/\/login$/)
  expect(await page.evaluate(() => sessionStorage.getItem('beauty-session-v1'))).toBeNull()
})
test('protected routing, server errors, and session expiry are visible', async ({ page }) => {
  await page.goto('/profile')
  await expect(page).toHaveURL(/\/login\?redirect=/)
  await page.route('**/api/sys-user/login', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ code: 500, info: '密码错误' }),
    }),
  )
  await page.getByRole('textbox', { name: '邮箱地址', exact: true }).fill('beauty@example.com')
  await page.getByRole('textbox', { name: '密码', exact: true }).fill('Password123')
  await page.getByRole('button', { name: '登录', exact: true }).click()
  await expect(page.getByRole('alert').filter({ hasText: '密码错误' })).toBeVisible()
  await mockAccount(page)
  await login(page)
  await page.route('**/api/sys-user/info', (route) =>
    route.fulfill({
      status: 401,
      contentType: 'application/json',
      body: JSON.stringify({ code: 401, info: '登录已过期' }),
    }),
  )
  await page.getByRole('textbox', { name: '姓名', exact: true }).fill('测试')
  await page.getByRole('button', { name: '保存修改' }).click()
  await expect(page).toHaveURL(/expired=1/)
  await expect(page.getByText('登录已过期，请重新登录。', { exact: true })).toBeVisible()
})
test('pages fit four viewport sizes and have no runtime errors', async ({ page }) => {
  const errors = []
  page.on('pageerror', (e) => errors.push(e.message))
  await mockAccount(page)
  for (const width of [375, 768, 1024, 1440]) {
    await page.setViewportSize({ width, height: 960 })
    for (const path of ['/', '/login', '/register']) {
      await page.goto(path)
      await expect(page.locator('h1')).toBeVisible()
      await page.evaluate(
        () => new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve))),
      )
      expect(
        await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth),
      ).toBe(true)
      if (width === 375 || width === 1440)
        await page.screenshot({
          path: `docs/previews/${path === '/' ? 'home' : path.slice(1)}-${width}.png`,
          fullPage: true,
          animations: 'disabled',
        })
    }
  }
  await login(page)
  for (const width of [375, 768, 1024, 1440]) {
    await page.setViewportSize({ width, height: 960 })
    await page.evaluate(
      () => new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve))),
    )
    expect(
      await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth),
    ).toBe(true)
    if (width === 375)
      await page.screenshot({
        path: 'docs/previews/profile-mobile.png',
        fullPage: true,
        animations: 'disabled',
      })
  }
  expect(errors).toEqual([])
})
test('keyboard navigation, mobile menu, and logout confirmation work', async ({ page }) => {
  await page.goto('/')
  await page.keyboard.press('Tab')
  await expect(page.getByRole('link', { name: '跳到主要内容' })).toBeFocused()
  await page.keyboard.press('Enter')
  await expect(page.locator('#main-content')).toBeFocused()
  await page.setViewportSize({ width: 375, height: 812 })
  await page.evaluate(
    () => new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve))),
  )
  await page.getByRole('button', { name: '打开导航' }).click()
  await expect(page.getByRole('navigation', { name: '手机导航' })).toBeVisible()
  await page.keyboard.press('Escape')
  await expect(page.getByRole('navigation', { name: '手机导航' })).toHaveCount(0)
  await mockAccount(page)
  await login(page)
  await page.getByRole('button', { name: '退出登录', exact: true }).click()
  await page.getByRole('button', { name: '取消', exact: true }).click()
  await expect(page.getByRole('dialog')).toBeHidden()
  await expect(page).toHaveURL(/\/profile$/)
  await page.getByRole('button', { name: '退出登录', exact: true }).click()
  await page.getByRole('dialog').getByRole('button', { name: '退出登录', exact: true }).click()
  await expect(page).toHaveURL(/\/$/)
  await page.goto('/profile')
  await expect(page).toHaveURL(/\/login\?redirect=/)
})
test('existing profile is displayed and unchanged legacy avatar does not block name updates', async ({
  page,
}) => {
  let update
  await page.route('**/api/sys-user/login', (route) =>
    route.fulfill(
      ok({
        token: 'test-token',
        userVO: {
          email: 'beauty@example.com',
          realName: '原姓名',
          phone: '13800000000',
          avatar: 'http://example.com/old-avatar.jpg',
        },
      }),
    ),
  )
  await page.route('**/api/sys-user/info', async (route) => {
    update = JSON.parse(route.request().postData())
    await route.fulfill(ok('保存成功'))
  })
  await login(page)
  await expect(page.getByRole('textbox', { name: '姓名', exact: true })).toHaveValue('原姓名')
  await expect(page.getByText('当前无法读取已有资料', { exact: false })).toHaveCount(0)
  await page.getByRole('textbox', { name: '姓名', exact: true }).fill('新姓名')
  await page.getByRole('button', { name: '保存修改' }).click()
  await expect(page.getByText('个人资料已保存', { exact: true })).toBeVisible()
  expect(update).toEqual({ realName: '新姓名' })
})

test('profile removes avatar input and adds address favorites and order navigation', async ({
  page,
}) => {
  await mockAccount(page)
  await login(page)
  await expect(page.getByRole('textbox', { name: '头像地址' })).toHaveCount(0)
  const nav = page.getByRole('navigation', { name: '个人中心导航' })
  for (const name of ['个人地址管理', '我的收藏', '我的订单']) {
    await nav.getByRole('button', { name, exact: true }).click()
    await expect(page.getByRole('heading', { name, exact: true })).toBeVisible()
  }
  for (const name of ['待付款', '待发货', '待评价', '退款', '售后']) {
    const filter = page.getByRole('button', { name, exact: true })
    await filter.click()
    await expect(filter).toHaveAttribute('aria-pressed', 'true')
    await expect(page.getByRole('status')).toContainText('订单服务准备中')
  }
  await page.reload()
  await expect(page.getByRole('button', { name: '售后', exact: true })).toHaveAttribute(
    'aria-pressed',
    'true',
  )
  await page.setViewportSize({ width: 375, height: 812 })
  await page.evaluate(
    () => new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve))),
  )
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
  await page.screenshot({
    path: 'docs/previews/orders-mobile.png',
    fullPage: true,
    animations: 'disabled',
  })
})

test('product catalog supports navigation search pagination and mobile layout', async ({
  page,
}) => {
  await page.goto('/')
  await page
    .getByRole('navigation', { name: '主要导航' })
    .getByRole('link', { name: '全部商品' })
    .click()
  await expect(page).toHaveURL(/\/products$/)
  await expect(page.locator('.product-item')).toHaveCount(8)
  const first = await page.locator('.product-item h2').first().textContent()
  await page.getByRole('button', { name: '下一页' }).click()
  await expect(page).toHaveURL(/page=2/)
  await expect(page.locator('.product-item h2').first()).not.toHaveText(first)
  await page.getByRole('textbox', { name: '商品搜索关键词' }).fill('精华')
  await page.getByRole('button', { name: '搜索', exact: true }).click()
  await expect(page.locator('.product-item')).toHaveCount(2)
  await expect(page).not.toHaveURL(/page=2/)
  await page.reload()
  await expect(page.getByRole('textbox', { name: '商品搜索关键词' })).toHaveValue('精华')
  await page.getByRole('textbox', { name: '商品搜索关键词' }).fill('找不到这个产品')
  await page.getByRole('button', { name: '搜索', exact: true }).click()
  await expect(page.getByRole('heading', { name: '没有找到相关商品' })).toBeVisible()
  await page.getByRole('button', { name: '查看全部商品' }).click()
  await page.getByLabel('每页商品数量').selectOption('12')
  await expect(page.locator('.product-item')).toHaveCount(12)
  await page.getByLabel('每页商品数量').selectOption('8')
  for (const width of [1440, 768, 375]) {
    await page.setViewportSize({ width, height: 900 })
    await page.evaluate(
      () => new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve))),
    )
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
    await page.screenshot({
      path: `docs/previews/products-${width}.png`,
      fullPage: true,
      animations: 'disabled',
    })
  }
})
