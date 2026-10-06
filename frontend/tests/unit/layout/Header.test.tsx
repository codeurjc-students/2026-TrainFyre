import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { Header } from '@/components/layout/Header'

const options = [
  { id: 'inicio', name: 'Inicio' },
  { id: 'incidencias', name: 'Incidencias' },
]

describe('Header', () => {
  it('renders one button per menu option inside the main navigation', () => {
    render(<Header options={options} currentPage="inicio" onChangePage={vi.fn()} />)

    const nav = screen.getByRole('navigation', { name: 'Menú principal' })
    expect(nav).toContainElement(screen.getByRole('button', { name: 'Inicio' }))
    expect(nav).toContainElement(screen.getByRole('button', { name: 'Incidencias' }))
  })

  it('marks only the current page as pressed', () => {
    render(<Header options={options} currentPage="incidencias" onChangePage={vi.fn()} />)

    expect(screen.getByRole('button', { name: 'Incidencias' })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getByRole('button', { name: 'Inicio' })).toHaveAttribute('aria-pressed', 'false')
  })

  it('notifies the id of the clicked option', async () => {
    const onChangePage = vi.fn()
    const user = userEvent.setup()
    render(<Header options={options} currentPage="inicio" onChangePage={onChangePage} />)

    await user.click(screen.getByRole('button', { name: 'Incidencias' }))

    expect(onChangePage).toHaveBeenCalledTimes(1)
    expect(onChangePage).toHaveBeenCalledWith('incidencias')
  })

  it('goes back home when the logo is clicked', async () => {
    const onChangePage = vi.fn()
    const user = userEvent.setup()
    render(<Header options={options} currentPage="incidencias" onChangePage={onChangePage} />)

    await user.click(screen.getByRole('button', { name: 'Ir a inicio' }))

    expect(onChangePage).toHaveBeenCalledWith('inicio')
  })

  it('shows the profile avatar', () => {
    render(<Header options={options} currentPage="inicio" onChangePage={vi.fn()} />)

    expect(screen.getByLabelText('Perfil')).toBeInTheDocument()
  })
})
