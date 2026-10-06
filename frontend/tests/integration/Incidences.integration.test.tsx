/**
 * Client <-> server integration test.
 *
 * Renders the REAL <Incidences /> page (no mocks: real hook, real service, real
 * axios) and checks that it shows what the REAL REST API returns.
 *
 * Requirements: backend + database running on VITE_API_BASE_URL (default
 * http://localhost:8080) with the example data loaded (Spring profile "dev").
 *
 *   docker compose -f backend/TrainFyre/docker-compose.yaml up -d --build
 *   npm run test:integration
 */
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeAll, describe, expect, it } from 'vitest'
import { Incidences } from '@/src/pages/Incidences'
import type { Incidence, PagedResponseIncidence } from '@/src/types/incidences'

const API = import.meta.env.VITE_API_BASE_URL as string

/**
 * Reads every incidence straight from the API with plain fetch (independent from the
 * app code), so the expectations do not depend on the application under test.
 */
async function fetchAllFromApi(): Promise<Incidence[]> {
  const all: Incidence[] = []
  for (let page = 0; ; page++) {
    const response = await fetch(`${API}/incidence?page=${page}&size=100`)
    expect(response.ok).toBe(true)
    const body = (await response.json()) as PagedResponseIncidence
    all.push(...body.content)
    if (body.content.length === 0 || all.length >= body.totalElements) return all
  }
}

async function renderAndWaitForData() {
  const view = render(<Incidences />)
  await waitFor(() => expect(screen.queryByRole('status')).not.toBeInTheDocument(), { timeout: 20_000 })
  return view
}

function dataRows() {
  return screen.getAllByRole('row').slice(1) // skip the header row
}

describe('Incidences page <-> REST API', () => {
  let expected: Incidence[]

  beforeAll(async () => {
    expected = await fetchAllFromApi()
  })

  it('the API has example data to show', () => {
    expect(expected.length).toBeGreaterThan(0)
  })

  it('lists every incidence returned by the API (all the pages)', async () => {
    await renderAndWaitForData()

    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    expect(dataRows()).toHaveLength(expected.length)
    expect(
      screen.getByText(`Mostrando ${expected.length} de ${expected.length} incidencias`),
    ).toBeInTheDocument()
  })

  it('shows the name, the lines and the delay of an incidence as the API sends them', async () => {
    await renderAndWaitForData()
    const first = expected[0]
    const minutes = Temporal.Duration.from(first.occurrence.duration).total({ unit: 'minutes' })

    const row = dataRows().find((candidate) => within(candidate).queryByText(first.description.name))

    expect(row).toBeDefined()
    first.affectedNetwork.lineIds.forEach((lineId) => expect(row).toHaveTextContent(String(lineId)))
    expect(row).toHaveTextContent(`${minutes} min`)
  })

  it('filters by map using the maps present in the API data', async () => {
    const user = userEvent.setup()
    await renderAndWaitForData()
    const mapId = expected[0].affectedNetwork.mapId
    const expectedForMap = expected.filter((item) => item.affectedNetwork.mapId === mapId)

    await user.selectOptions(screen.getByRole('combobox'), `Mapa ${mapId}`)

    expect(dataRows()).toHaveLength(expectedForMap.length)
    expect(screen.getByText(`Mostrando ${expectedForMap.length} incidencias`)).toBeInTheDocument()
  })
})
