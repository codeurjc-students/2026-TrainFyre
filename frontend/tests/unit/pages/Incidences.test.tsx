import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { PAGE_SIZE } from '@/src/config/api'
import { useAllIncidences } from '@/src/hooks/useAllIncidences'
import { Incidences } from '@/src/pages/Incidences'
import type { Incidence } from '@/src/types/incidences'
import { makeIncidence } from '../../support/fixtures'

vi.mock('@/src/hooks/useAllIncidences')

const mockedHook = vi.mocked(useAllIncidences)

function mockState(state: Partial<ReturnType<typeof useAllIncidences>> = {}) {
  mockedHook.mockReturnValue({
    incidencias: [],
    total: null,
    cargando: false,
    error: null,
    ...state,
  })
}

function incidence(name: string, mapId: number, lineIds: number[], duration: string): Incidence {
  return makeIncidence({
    description: { name, summary: '' },
    affectedNetwork: { mapId, lineIds },
    occurrence: { timestamp: '2026-09-03T05:10:00', duration, instantaneous: false },
  })
}

const metro = incidence('Metro breakdown', 1, [101], 'PT20M')
const bus = incidence('Bus strike', 2, [205], 'PT1H')
const metroTwo = incidence('Metro maintenance', 1, [102], 'PT90M')

describe('Incidences page', () => {
  beforeEach(() => {
    mockedHook.mockReset()
  })

  it('asks the hook for pages of the configured size', () => {
    mockState()

    render(<Incidences />)

    expect(mockedHook).toHaveBeenCalledWith(PAGE_SIZE)
  })

  it('shows the title and the table headers', () => {
    mockState()

    render(<Incidences />)

    expect(screen.getByRole('heading', { level: 1, name: 'Incidencias' })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: 'Nombre' })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: 'Líneas afectadas' })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: 'Retraso previsto' })).toBeInTheDocument()
  })

  it('shows one row per incidence with name, lines and delay in minutes', () => {
    mockState({ incidencias: [metro, bus], total: 2 })

    render(<Incidences />)

    const rows = screen.getAllByRole('row').slice(1) // skip the header row
    expect(rows).toHaveLength(2)
    expect(within(rows[0]).getByText('Metro breakdown')).toBeInTheDocument()
    expect(within(rows[0]).getByText('101')).toBeInTheDocument()
    expect(within(rows[0]).getByText('20 min')).toBeInTheDocument()
    expect(within(rows[1]).getByText('Bus strike')).toBeInTheDocument()
    expect(within(rows[1]).getByText('205')).toBeInTheDocument()
    expect(within(rows[1]).getByText('60 min')).toBeInTheDocument()
  })

  it('shows how many incidences are displayed out of the total', () => {
    mockState({ incidencias: [metro, bus], total: 5 })

    render(<Incidences />)

    expect(screen.getByText('Mostrando 2 de 5 incidencias')).toBeInTheDocument()
  })

  it('omits the total while it is still unknown', () => {
    mockState({ incidencias: [metro], total: null, cargando: true })

    render(<Incidences />)

    expect(screen.getByText('Mostrando 1 incidencias')).toBeInTheDocument()
  })

  it('shows a loading message only while loading', () => {
    mockState({ cargando: true })
    const { rerender } = render(<Incidences />)
    expect(screen.getByRole('status')).toHaveTextContent('Cargando incidencias…')

    mockState({ cargando: false })
    rerender(<Incidences />)
    expect(screen.queryByRole('status')).not.toBeInTheDocument()
  })

  it('shows an empty-state row when there is nothing to list', () => {
    mockState({ total: 0 })

    render(<Incidences />)

    expect(screen.getByText('No hay incidencias disponibles.')).toBeInTheDocument()
    expect(screen.getByText('Mostrando 0 de 0 incidencias')).toBeInTheDocument()
  })

  it('does not show the empty-state row while loading', () => {
    mockState({ cargando: true })

    render(<Incidences />)

    expect(screen.queryByText('No hay incidencias disponibles.')).not.toBeInTheDocument()
  })

  it('shows the error and hides the empty-state row when loading fails', () => {
    mockState({ error: 'Network Error' })

    render(<Incidences />)

    expect(screen.getByRole('alert')).toHaveTextContent('Error al cargar incidencias: Network Error')
    expect(screen.queryByText('No hay incidencias disponibles.')).not.toBeInTheDocument()
  })

  it('does not show any alert when there is no error', () => {
    mockState({ incidencias: [metro], total: 1 })

    render(<Incidences />)

    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('offers each map only once in the filter', () => {
    mockState({ incidencias: [metro, bus, metroTwo], total: 3 })

    render(<Incidences />)

    const options = within(screen.getByRole('combobox')).getAllByRole('option')
    expect(options.map((option) => option.textContent)).toEqual(['Todos los mapas', 'Mapa 1', 'Mapa 2'])
  })

  it('filters the rows by map and updates the counter', async () => {
    mockState({ incidencias: [metro, bus, metroTwo], total: 3 })
    const user = userEvent.setup()

    render(<Incidences />)
    await user.selectOptions(screen.getByRole('combobox'), 'Mapa 1')

    expect(screen.getByText('Metro breakdown')).toBeInTheDocument()
    expect(screen.getByText('Metro maintenance')).toBeInTheDocument()
    expect(screen.queryByText('Bus strike')).not.toBeInTheDocument()
    // With a filter active the total is not shown
    expect(screen.getByText('Mostrando 2 incidencias')).toBeInTheDocument()
  })

  it('shows every row again when the filter is cleared', async () => {
    mockState({ incidencias: [metro, bus, metroTwo], total: 3 })
    const user = userEvent.setup()

    render(<Incidences />)
    await user.selectOptions(screen.getByRole('combobox'), 'Mapa 2')
    expect(screen.queryByText('Metro breakdown')).not.toBeInTheDocument()

    await user.selectOptions(screen.getByRole('combobox'), 'Todos los mapas')

    expect(screen.getAllByRole('row').slice(1)).toHaveLength(3)
    expect(screen.getByText('Mostrando 3 de 3 incidencias')).toBeInTheDocument()
  })
})
