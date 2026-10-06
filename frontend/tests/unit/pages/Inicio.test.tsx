import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { Inicio } from '@/src/pages/Inicio'

describe('Inicio page', () => {
  it('renders the home section', () => {
    render(<Inicio />)

    expect(screen.getByRole('region', { name: 'Inicio' })).toBeInTheDocument()
  })

  it('shows the fallback when the logo cannot be loaded', () => {
    render(<Inicio />)

    expect(screen.getByText('APP')).toBeInTheDocument()
  })
})
