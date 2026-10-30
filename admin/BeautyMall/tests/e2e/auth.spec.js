import { test, expect } from '@playwright/test'
test('registration validation and success, route guard and mobile layout', async ({ page }) => {
  let registrations = 0
  await page.route('**/api/sys-user/reg', async (route) => {
    registrations++
    expect(route.request().headers()['content-type']).toContain('application/x-www-form-urlencoded')
    await route.fulfill({ json: { code: 200, data: '注册成功' } })
  })
  await page.goto('/categories')
  await expect(page).toHaveURL(/login/)
  await page.getByRole('link', { name: '创建账号' }).click()
  await expect(page.getByRole('heading', { name: '创建账号', exact: true })).toBeVisible()
  await page.getByLabel('邮箱', { exact: true }).fill('new@example.com')
  await page.getByLabel('密码', { exact: true }).fill('password123')
  await page.getByLabel('确认密码', { exact: true }).fill('other')
  await page.getByRole('button', { name: '注册账号' }).click()
  await expect(page.getByRole('alert')).toHaveText('两次输入的密码不一致。')
  expect(registrations).toBe(0)
  await page.getByLabel('确认密码', { exact: true }).fill('password123')
  await page.getByRole('button', { name: '注册账号' }).click()
  await expect(page.getByRole('status')).toHaveText('注册成功，请登录。')
  for (const route of ['/login', '/register']) {
    await page.goto(route)
    await page.setViewportSize({ width: 375, height: 812 })
    await expect(page.locator('h1')).toBeVisible()
    expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(375)
  }
})
