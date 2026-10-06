import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { AppLayout } from '@/components/layout/AppLayout'

const options = [
  { id: 'inicio', name: 'Inicio' },
  { id: 'incidencias', name: 'Incidencias' },
]

describe('AppLayout', () => {
  it('renders header, children inside main, and footer', () => {
    render(
      <AppLayout options={options} currentPage="inicio" onChangePage={vi.fn()}>
        <p>Page content</p>
      </AppLayout>,
    )

    expect(screen.getByRole('banner')).toBeInTheDocument()
    expect(within(screen.getByRole('main')).getByText('Page content')).toBeInTheDocument()
    expect(screen.getByRole('contentinfo')).toBeInTheDocument()
  })

  it('passes the navigation props down to the header', async () => {
    const onChangePage = vi.fn()
    const user = userEvent.setup()
    render(
      <AppLayout options={options} currentPage="incidencias" onChangePage={onChangePage}>
        <p>Page content</p>
      </AppLayout>,
    )

    expect(screen.getByRole('button', { name: 'Incidencias' })).toHaveAttribute('aria-pressed', 'true')

    await user.click(screen.getByRole('button', { name: 'Inicio' }))
    expect(onChangePage).toHaveBeenCalledWith('inicio')
  })
})
