import { describe, expect, it } from 'vitest'
import { API_BASE_URL, PAGE_SIZE } from '@/src/config/api'

describe('api config', () => {
  it('reads the API base URL from the Vite environment', () => {
    expect(API_BASE_URL).toBe('http://api.test')
  })

  it('reads the page size from the Vite environment', () => {
    expect(PAGE_SIZE).toBe('100')
  })
})
