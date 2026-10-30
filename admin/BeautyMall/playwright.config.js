import { defineConfig } from '@playwright/test'
export default defineConfig({
  testDir: './tests/e2e',
  workers: 1,
  use: {
    baseURL: process.env.ADMIN_TEST_URL || 'http://127.0.0.1:15274',
    channel: 'chrome',
    headless: true,
  },
  reporter: 'list',
})
