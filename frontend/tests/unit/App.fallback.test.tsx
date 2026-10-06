import { act, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import App from '@/src/App'

// The real menu only ever sends valid ids, so to reach the defensive fallback of App
// the layout is replaced by a stub that lets the test send any id.
const layout = vi.hoisted(() => ({
  props: null as null | { currentPage: string; onChangePage: (id: string) => void },
}))

vi.mock('@/components/layout/AppLayout', () => ({
  AppLayout: (props: NonNullable<typeof layout.props> & { children: React.ReactNode }) => {
    layout.props = props
    return <main>{props.children}</main>
  },
}))

describe('App with an unknown page id', () => {
  it('falls back to the home page', () => {
    render(<App />)

    act(() => layout.props!.onChangePage('does-not-exist'))

    expect(screen.getByRole('region', { name: 'Inicio' })).toBeInTheDocument()
    expect(layout.props!.currentPage).toBe('inicio')
  })
})
