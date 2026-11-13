// @vitest-environment jsdom
import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import { Routes } from '../../src/Routes'
import App from '../../src/App'
import { Field } from '../../src/components/Field'
import { Heading } from '../../src/components/Heading'
import { Notice } from '../../src/components/Notice'
import { Retry } from '../../src/components/Retry'
import { SiteFooter } from '../../src/components/SiteFooter'
import { SiteHeader } from '../../src/components/SiteHeader'
import { api, getAuthState, logout, notify } from '../../src/api'
import { Require } from '../../src/components/Require'
import { ErrorBoundary } from '../../src/components/ErrorBoundary'
import { publishSession } from '../../src/api/session'
import { Home } from '../../src/pages/Home'
import { ErrorPage } from '../../src/pages/ErrorPage'
import { IncidenceFilters } from '../../src/pages/incidences/IncidenceFilters'
import { IncidenceTable } from '../../src/pages/incidences/IncidenceTable'
import { emptyFilters } from '../../src/model'
import { currentPath } from '../../src/lib/browser'

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

beforeEach(() => {
    // La cabecera autenticada pide el avatar; se evita tocar la red.
    vi.spyOn(api, 'avatar').mockResolvedValue(null)
})

afterEach(() => {
    cleanup()
    logout('')
})

describe('componentes de presentación y rutas públicas', () => {
    it('resuelve rutas directas sin hash', () => {
        globalThis.history.replaceState({}, '', '/urlerronea')

        expect(currentPath()).toBe('/urlerronea')
    })

    it('muestra la página de error cuando falla un componente inesperadamente', () => {
        const error = new Error('fallo de prueba')
        const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

        function BrokenComponent(): never {
            throw error
        }

        render(
            <ErrorBoundary>
                <BrokenComponent />
            </ErrorBoundary>,
        )

        expect(screen.getByRole('heading', { name: 'Algo no ha salido bien' })).toBeInTheDocument()
        expect(errorSpy).toHaveBeenCalledWith(
            'Error inesperado al renderizar la página.',
            error,
            expect.anything(),
        )
    })

    it('renderiza los hijos cuando no hay errores y permite recargar desde la página de error', async () => {
        const user = userEvent.setup()
        const reload = vi.fn()
        vi.stubGlobal('location', { reload })

        render(
            <ErrorBoundary>
                <p>Contenido correcto</p>
            </ErrorBoundary>,
        )

        expect(screen.getByText('Contenido correcto')).toBeInTheDocument()

        render(<ErrorPage />)
        await user.click(screen.getByRole('button', { name: 'Recargar página' }))

        expect(reload).toHaveBeenCalledOnce()
        expect(screen.getByRole('link', { name: 'Volver al inicio' })).toHaveAttribute('href', '#/')
    })

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

    it('conserva la sesión en la pestaña y la elimina al cerrar sesión', () => {
        publishSession({
            id: '00000000-0000-4000-8000-000000000001',
            token: 'token',
            deadline: Date.now() + 60_000,
            user: { username: 'user', roles: ['USER'], alertSubscriptions: [] },
        })

        expect(sessionStorage.getItem('trainfyre.session')).toContain('"token":"token"')

        logout('')

        expect(sessionStorage.getItem('trainfyre.session')).toBeNull()
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
