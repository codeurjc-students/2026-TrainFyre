import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    environment: 'node',
    include: ['tests/unit/**/*.test.ts'],
    env: {
      VITE_API_BASE_URL: 'https://api.test/api/v1',
      VITE_PAGE_SIZE: '2',
    },
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
      include: ['src/model.ts'],
    },
  },
})
