import { defineConfig } from 'vitest/config'

export default defineConfig({
    test: {
        environment: 'jsdom',
        setupFiles: ['tests/components/setup.ts'],
        include: ['tests/components/**/*.test.tsx'],
        env: {
            VITE_API_BASE_URL: 'https://api.test',
            VITE_PAGE_SIZE: '2',
        },
    },
})
