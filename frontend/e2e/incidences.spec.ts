import { expect, test } from '@playwright/test'

test.describe('TrainFyre UI', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
  })

  test('shows the home page with the main navigation', async ({ page }) => {
    await expect(page.getByRole('region', { name: 'Inicio' })).toBeVisible()

    const nav = page.getByRole('navigation', { name: 'Menú principal' })
    await expect(nav.getByRole('button', { name: 'Inicio' })).toHaveAttribute('aria-pressed', 'true')
    await expect(nav.getByRole('button', { name: 'Incidencias' })).toBeVisible()
  })

  test('shows the example incidences of the database in the incidences page', async ({ page }) => {
    await page.getByRole('button', { name: 'Incidencias' }).click()

    await expect(page.getByRole('heading', { name: 'Incidencias' })).toBeVisible()
    await expect(page.getByText('Cargando incidencias…')).toBeHidden()
    await expect(page.getByRole('alert')).toHaveCount(0)

    // All the example data (300 incidences, loaded in pages of 100) is shown
    await expect(page.getByText('Mostrando 300 de 300 incidencias')).toBeVisible()
    await expect(page.getByRole('row')).toHaveCount(300 + 1) // + header row
    await expect(page.getByRole('cell', { name: 'Avería técnica en Cercanías' }).first()).toBeVisible()
  })

  test('filters the incidences by map', async ({ page }) => {
    await page.getByRole('button', { name: 'Incidencias' }).click()
    await expect(page.getByText('Mostrando 300 de 300 incidencias')).toBeVisible()

    await page.getByRole('combobox').selectOption({ label: 'Mapa 3' })

    const counter = page.getByText(/^Mostrando \d+ incidencias$/)
    await expect(counter).toBeVisible()
    const shown = Number((await counter.innerText()).match(/\d+/)![0])
    expect(shown).toBeGreaterThan(0)
    expect(shown).toBeLessThan(300)
    await expect(page.getByRole('row')).toHaveCount(shown + 1)

    await page.getByRole('combobox').selectOption({ label: 'Todos los mapas' })
    await expect(page.getByText('Mostrando 300 de 300 incidencias')).toBeVisible()
  })

  test('goes back to the home page with the logo', async ({ page }) => {
    await page.getByRole('button', { name: 'Incidencias' }).click()
    await expect(page.getByRole('heading', { name: 'Incidencias' })).toBeVisible()

    await page.getByRole('button', { name: 'Ir a inicio' }).click()

    await expect(page.getByRole('region', { name: 'Inicio' })).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Incidencias' })).toHaveCount(0)
  })
})
