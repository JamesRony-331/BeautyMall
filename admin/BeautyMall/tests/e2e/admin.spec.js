import { test, expect } from '@playwright/test'
test.beforeEach(async ({ page }) => {
  await page.route('**/api/sys-user/page?**', (route) =>
    route.fulfill({ json: { code: 200, data: { records: [], totalCount: 0 } } }),
  )
  await page.addInitScript(() => sessionStorage.setItem('beauty-admin-token', 'test-token'))
})
test('product search filters pagination and details work', async ({ page }) => {
  await page.goto('/products')
  await expect(page.getByRole('heading', { name: '商品管理', exact: true })).toBeVisible()
  await expect(page.locator('.el-table__body-wrapper tbody tr')).toHaveCount(6)
  await page.getByRole('button', { name: '下一页' }).click()
  await expect(page.getByText('SP0007', { exact: true })).toBeVisible()
  await page.getByRole('textbox', { name: '商品搜索' }).fill('精华')
  await page.getByRole('button', { name: '查询', exact: true }).click()
  await expect(page.locator('.el-table__body-wrapper tbody tr')).toHaveCount(1)
  await page.getByRole('button', { name: '查看水润保湿精华详情' }).click()
  await expect(page.getByRole('dialog')).toBeVisible()
  await page.getByRole('dialog').getByRole('button', { name: '关闭', exact: true }).last().click()
  await page.getByRole('button', { name: '重置', exact: true }).click()
  await page.getByLabel('上架状态', { exact: true }).selectOption('已下架')
  await expect(page.locator('.el-table__body-wrapper tbody tr')).toHaveCount(2)
})
test('categories and truthful user unavailable state', async ({ page }) => {
  await page.goto('/categories')
  await page.getByLabel('分类状态', { exact: true }).selectOption('停用')
  await expect(page.locator('.el-table__body-wrapper tbody tr')).toHaveCount(2)
  await page.getByRole('link', { name: '用户管理', exact: true }).click()
  await expect(page.getByRole('heading', { name: '用户管理', exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: '刷新' })).toBeVisible()
})
test('responsive routes render without runtime errors or document overflow', async ({ page }) => {
  const errors = []
  page.on('pageerror', (error) => errors.push(error.message))
  for (const width of [1440, 768, 375]) {
    await page.setViewportSize({ width, height: 950 })
    for (const route of ['products', 'users', 'categories']) {
      await page.goto('/' + route)
      await expect(page.locator('h1')).toBeVisible()
      await page.evaluate(
        () => new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve))),
      )
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(
        true,
      )
      await page.screenshot({
        path: `docs/previews/${route}-${width}.png`,
        fullPage: true,
        animations: 'disabled',
      })
    }
  }
  await page.getByRole('button', { name: '打开管理菜单' }).click()
  await page.getByRole('dialog').getByRole('link', { name: '商品管理' }).click()
  await expect(page).toHaveURL(/products$/)
  await expect(page.getByRole('dialog')).toBeHidden()
  expect(errors).toEqual([])
})
