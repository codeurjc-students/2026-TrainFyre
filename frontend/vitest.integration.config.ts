import react from '@vitejs/plugin-react'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitest/config'

// Client <-> server integration tests: the React components talk to the REAL
// REST API (backend + database must be running, see README of the tests).
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': fileURLToPath(new URL('.', import.meta.url)) },
  },
  test: {
    environment: 'jsdom',
    // The backend only allows CORS from http://localhost:5173 (@CrossOrigin),
    // so jsdom must pretend to be served from that origin.
    environmentOptions: { jsdom: { url: 'http://localhost:5173' } },
    globals: true,
    setupFiles: ['./tests/support/setup.ts'],
    include: ['tests/integration/**/*.test.{ts,tsx}'],
    testTimeout: 30_000,
    hookTimeout: 30_000,
    env: {
      VITE_API_BASE_URL: process.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
      VITE_PAGE_SIZE: process.env.VITE_PAGE_SIZE ?? '100',
    },
  },
})
