import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import App from '@/src/App'
import { useAllIncidences } from '@/src/hooks/useAllIncidences'
import { makeIncidence } from '../support/fixtures'

vi.mock('@/src/hooks/useAllIncidences')

describe('App', () => {
  beforeEach(() => {
    vi.mocked(useAllIncidences).mockReturnValue({
      incidencias: [makeIncidence({ description: { name: 'Metro breakdown', summary: '' } })],
      total: 1,
      cargando: false,
      error: null,
    })
  })

  it('starts on the home page', () => {
    render(<App />)

    expect(screen.getByRole('region', { name: 'Inicio' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Inicio' })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.queryByRole('heading', { name: 'Incidencias' })).not.toBeInTheDocument()
  })

  it('navigates to the incidences page from the menu', async () => {
    const user = userEvent.setup()
    render(<App />)

    await user.click(screen.getByRole('button', { name: 'Incidencias' }))

    expect(screen.getByRole('heading', { name: 'Incidencias' })).toBeInTheDocument()
    expect(screen.getByText('Metro breakdown')).toBeInTheDocument()
    expect(screen.queryByRole('region', { name: 'Inicio' })).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Incidencias' })).toHaveAttribute('aria-pressed', 'true')
  })

  it('returns to the home page with the logo button', async () => {
    const user = userEvent.setup()
    render(<App />)
    await user.click(screen.getByRole('button', { name: 'Incidencias' }))

    await user.click(screen.getByRole('button', { name: 'Ir a inicio' }))

    expect(screen.getByRole('region', { name: 'Inicio' })).toBeInTheDocument()
  })
})
