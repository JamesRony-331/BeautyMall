import { test, expect } from '@playwright/test'
test('user login, pagination, edit and confirmed disable use management endpoints', async ({
  page,
}) => {
  const calls = []
  await page.route('**/api/sys-user/**', async (route) => {
    const req = route.request(),
      url = new URL(req.url())
    calls.push({
      path: url.pathname,
      method: req.method(),
      body: req.postData(),
      auth: req.headers().authorization,
    })
    const data = url.pathname.endsWith('/login')
      ? { token: 'test-token', userVO: { id: 1 } }
      : url.pathname.endsWith('/page')
        ? {
            totalCount: 11,
            records: [
              { id: 2, realName: '测试用户', email: 'test@example.com', phone: '', status: 1 },
            ],
          }
        : '成功'
    await route.fulfill({ json: { code: 200, data } })
  })
  await page.goto('/users')
  await page.getByLabel('邮箱', { exact: true }).fill('admin@example.com')
  await page.getByLabel('密码', { exact: true }).fill('test-password')
  await page.getByRole('button', { name: '登录', exact: true }).click()
  await expect(page).toHaveURL(/products/)
  await page.getByRole('link', { name: '用户管理', exact: true }).click()
  await expect(page.getByText('测试用户', { exact: true })).toBeVisible()
  await page.getByRole('button', { name: '下一页' }).click()
  await page.getByRole('button', { name: '编辑', exact: true }).click()
  await page.getByLabel('姓名', { exact: true }).fill('新姓名')
  await page.getByRole('button', { name: '保存修改' }).click()
  await expect(page.getByRole('dialog')).toBeHidden()
  await page.getByRole('button', { name: '禁用', exact: true }).click()
  await page.getByRole('button', { name: '取消', exact: true }).click()
  expect(calls.some((c) => c.path.endsWith('/disable'))).toBe(false)
  await page.getByRole('button', { name: '禁用', exact: true }).click()
  await page.getByRole('button', { name: '确认禁用', exact: true }).click()
  await expect(page.getByText('用户已禁用', { exact: true })).toBeVisible()
  expect(calls.find((c) => c.path.endsWith('/2/info'))).toMatchObject({
    method: 'PUT',
    auth: 'Bearer test-token',
  })
  expect(JSON.parse(calls.find((c) => c.path.endsWith('/2/info')).body).realName).toBe('新姓名')
  expect(calls.find((c) => c.path.endsWith('/2/disable')).method).toBe('PUT')
})
