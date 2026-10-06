import { defineConfig, devices } from '@playwright/test'

// System (E2E) tests of the UI.
// Requirements: backend + database running on http://localhost:8080 with the example
// data loaded. The frontend dev server is started automatically on port 5173
// (the port the backend allows through CORS).
export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  expect: { timeout: 15_000 },
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? [['github'], ['html', { open: 'never' }]] : 'list',
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: 'npm run dev -- --port 5173 --strictPort',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 60_000,
  },
})
