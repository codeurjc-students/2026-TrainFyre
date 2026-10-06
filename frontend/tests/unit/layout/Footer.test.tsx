import { render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { Footer } from '@/components/layout/Footer'

describe('Footer', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows the copyright with the current year', () => {
    // Vitest's fake timers clash with the Temporal polyfill, so only the year is stubbed
    vi.spyOn(Date.prototype, 'getFullYear').mockReturnValue(2027)

    render(<Footer />)

    expect(screen.getByRole('contentinfo')).toHaveTextContent('© 2027 Mi aplicación')
  })
})
