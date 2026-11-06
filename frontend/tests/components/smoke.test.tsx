// @vitest-environment jsdom
import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { Routes } from '../../src/Routes'
import App from '../../src/App'
import { Field } from '../../src/components/Field'
import { Heading } from '../../src/components/Heading'
import { Notice } from '../../src/components/Notice'
import { Retry } from '../../src/components/Retry'
import { SiteFooter } from '../../src/components/SiteFooter'
import { SiteHeader } from '../../src/components/SiteHeader'
import { getAuthState, logout, notify } from '../../src/api'
import { Require } from '../../src/components/Require'
import { publishSession } from '../../src/api/session'
import { Home } from '../../src/pages/Home'
import { IncidenceFilters } from '../../src/pages/incidences/IncidenceFilters'
import { IncidenceTable } from '../../src/pages/incidences/IncidenceTable'
import { emptyFilters } from '../../src/model'

const user = {
    username: 'admin',
    roles: ['ADMIN' as const],
    alertSubscriptions: [],
}

const incidence = {
    id: '00000000-0000-4000-8000-000000000001',
    affectedNetwork: { mapId: 1, lineIds: [4] },
    occurrence: { timestamp: '2026-11-03T12:00:00Z', duration: 'PT15M' },
    description: { name: 'Mantenimiento', summary: 'Trabajos en la vía' },
    classification: { severity: 'LOW' as const, cause: 'MAINTENANCE' as const },
}

afterEach(() => {
    cleanup()
    logout('')
})

describe('componentes de presentación y rutas públicas', () => {
    it('monta la aplicación y actualiza el título de la ruta', () => {
        vi.stubGlobal('scrollTo', vi.fn())
        globalThis.location.hash = '#/'

        render(<App />)

        expect(screen.getByRole('main')).toBeInTheDocument()
        expect(document.title).toContain('Inicio')
        expect(screen.getByRole('link', { name: 'Saltar al contenido' })).toBeInTheDocument()
    })

    it('cierra avisos globales y mueve el foco al contenido', async () => {
        const user = userEvent.setup()

        render(<App />)
        const main = screen.getByRole('main')

        publishSession({
            id: '00000000-0000-4000-8000-000000000001',
            token: 'token',
            deadline: Date.now() + 60_000,
            user: { username: 'user', roles: ['USER'], alertSubscriptions: [] },
        })
        notify('Aviso de prueba')

        await screen.findByText('Aviso de prueba')
        await user.click(screen.getByRole('button', { name: 'Cerrar aviso' }))
        expect(screen.queryByText('Aviso de prueba')).not.toBeInTheDocument()

        await user.click(screen.getByRole('link', { name: 'Saltar al contenido' }))
        expect(main).toHaveFocus()
    })

    it('renderiza componentes básicos y sus variantes', () => {
        render(
            <>
                <Field label="Nombre" hint="Ayuda"><input /></Field>
                <Heading title="Título" actions={<button>Acción</button>}>Descripción</Heading>
                <Notice>Correcto</Notice>
                <Notice error>Error</Notice>
                <Retry error="Fallo" retry={() => {}} />
                <SiteFooter />
            </>,
        )

        expect(screen.getByText('Nombre')).toBeInTheDocument()
        expect(screen.getByText('Ayuda')).toBeInTheDocument()
        expect(screen.getByRole('heading', { name: 'Título' })).toBeInTheDocument()
        expect(screen.getAllByRole('alert').map(alert => alert.textContent)).toEqual([
            'Error',
            'Fallo',
        ])
        expect(screen.getByRole('button', { name: 'Volver a cargar' })).toBeInTheDocument()
        expect(screen.getByText(/TrainFyre/)).toBeInTheDocument()
    })

    it('renderiza el encabezado para visitante', () => {
        render(<SiteHeader path="/" />)

        expect(screen.getByRole('link', { name: 'TrainFyre, inicio' })).toBeInTheDocument()
        expect(screen.getByRole('link', { name: 'Acceder' })).toBeInTheDocument()
        expect(screen.queryByRole('button', { name: 'Salir' })).not.toBeInTheDocument()
    })

    it('renderiza estados autenticados, filtros y tabla de incidencias', () => {
        publishSession({
            id: '00000000-0000-4000-8000-000000000001',
            token: 'token',
            deadline: Date.now() + 60_000,
            user,
        })

        render(
            <>
                <SiteHeader path="/incidencias" />
                <Require><Home /></Require>
                <Require admin><Home /></Require>
                <IncidenceFilters
                    filters={emptyFilters}
                    onChange={() => {}}
                    onClear={() => {}}
                />
                <IncidenceTable
                    rows={[incidence]}
                    total={1}
                    filteredCount={1}
                    page={0}
                    lastPage={0}
                    onPageChange={() => {}}
                />
            </>,
        )

        expect(getAuthState().session?.user.username).toBe('admin')
        expect(screen.getByRole('button', { name: 'Salir' })).toBeInTheDocument()
        expect(screen.getByRole('link', { name: 'Administración' })).toBeInTheDocument()
        expect(screen.getByRole('link', { name: 'Mantenimiento' })).toBeInTheDocument()
        expect(screen.getByText('Mapa 1')).toBeInTheDocument()
    })

    it.each([
        ['/', 'Menos incertidumbre.'],
        ['/acceso', 'Qué bueno verte de nuevo.'],
        ['/registro', 'Tu próximo viaje empieza aquí.'],
        ['/ruta-inexistente', 'Página no encontrada'],
        ['/incidencias/incorrecto', 'Página no encontrada'],
    ])('resuelve la ruta pública %s', (path, text) => {
        render(<Routes path={path} />)

        expect(screen.getByText(text, { exact: false })).toBeInTheDocument()
    })
})
