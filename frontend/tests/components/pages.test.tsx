// @vitest-environment jsdom
import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { publishSession, logout } from '../../src/api/session'
import { api } from '../../src/api'
import { AdminUsers } from '../../src/pages/AdminUsers'
import { AccountForm } from '../../src/pages/account/AccountForm'
import { DeleteAccount } from '../../src/pages/account/DeleteAccount'
import { SubscriptionEditor } from '../../src/pages/account/SubscriptionEditor'
import { SubscriptionsSection } from '../../src/pages/account/SubscriptionsSection'
import { IncidenceEditor } from '../../src/pages/incidences/IncidenceEditor'
import { IncidencePage } from '../../src/pages/incidences/IncidencePage'
import { IncidenceList } from '../../src/pages/incidences/IncidenceList'
import { useResource } from '../../src/hooks/useResource'
import { UserPage } from '../../src/pages/account/UserPage'
import { Access } from '../../src/pages/Access'
import { Routes } from '../../src/Routes'
import { IncidenceFilters } from '../../src/pages/incidences/IncidenceFilters'
import { IncidenceTable } from '../../src/pages/incidences/IncidenceTable'
import { emptyFilters } from '../../src/model'

import type { Incidence, Subscription, UserDetails } from '../../src/model'

vi.mock('../../src/hooks/useResource', () => ({
    useResource: vi.fn(),
}))

const resource = vi.mocked(useResource)
const id = '00000000-0000-4000-8000-000000000001'

const subscription: Subscription = {
    lines: [1, 2],
    startTime: '2099-11-03T07:00:00+01:00',
    endTime: '2099-11-03T09:00:00+01:00',
    daysOfTheWeek: ['MONDAY'],
    expiration: '2099-12-03T10:00:00Z',
}

const user: UserDetails = {
    username: 'pablo',
    roles: ['ADMIN'],
    alertSubscriptions: [subscription],
}

const incidence: Incidence = {
    id,
    affectedNetwork: { mapId: 1, lineIds: [4] },
    occurrence: { timestamp: '2026-11-03T12:00:00Z', duration: 'PT15M' },
    description: { name: 'Mantenimiento', summary: 'Trabajos programados' },
    classification: { severity: 'LOW', cause: 'MAINTENANCE' },
}

afterEach(() => {
    cleanup()
    resource.mockReset()
    logout('')
    vi.restoreAllMocks()
})

function loaded<T>(data: T) {
    resource.mockReturnValue({
        data,
        error: '',
        loading: false,
        reload: vi.fn(),
    })
}

describe('páginas de cuenta y administración', () => {
    it('renderiza administración y formularios de cuenta', () => {
        loaded(user)
        render(<AdminUsers />)

        expect(screen.getByRole('heading', { name: 'Administrar usuarios' })).toBeInTheDocument()
        expect(screen.getByRole('heading', { name: 'Crear usuario' })).toBeInTheDocument()
        expect(screen.getByLabelText('UUID del usuario')).toBeInTheDocument()
    })

    it('renderiza edición, suscripciones y eliminación de cuenta', () => {
        publishSession({
            id,
            token: 'token',
            deadline: Date.now() + 60_000,
            user,
        })
        loaded(user)

        render(
            <>
                <AccountForm id={id} own user={user} onSaved={() => {}} />
                <SubscriptionEditor initial={subscription} onSave={async () => {}} />
                <SubscriptionsSection id={id} user={user} onChanged={() => {}} />
                <DeleteAccount username="pablo" targetId={id} own={false} />
            </>,
        )

        expect(screen.getAllByRole('textbox')[0]).toHaveValue('pablo')
        expect(screen.getByRole('button', { name: 'Guardar alerta' })).toBeInTheDocument()
        expect(screen.getByRole('button', { name: 'Eliminar esta cuenta' })).toBeInTheDocument()
        expect(screen.getByText('Líneas 1, 2')).toBeInTheDocument()
    })

    it('valida y envía la creación de usuarios desde administración', async () => {
        const user = userEvent.setup()
        const register = vi.spyOn(api, 'register').mockResolvedValue(id)
        render(<AdminUsers />)

        const fields = screen.getAllByRole('textbox')
        await user.type(fields[1], 'nuevo')
        await user.type(document.querySelector('input[name="password"]')!, 'contraseña-segura')
        await user.type(screen.getByLabelText('Repetir contraseña'), 'contraseña-segura')
        await user.click(screen.getByRole('button', { name: 'Crear usuario' }))

        await waitFor(() => expect(register).toHaveBeenCalledWith('nuevo', 'contraseña-segura'))
    })

    it('actualiza la cuenta, muestra el perfil y ejecuta eliminación', async () => {
        const userEventApi = userEvent.setup()
        const update = vi.spyOn(api, 'updateUser').mockResolvedValue(undefined)
        const remove = vi.spyOn(api, 'deleteUser').mockResolvedValue(undefined)
        vi.stubGlobal('confirm', vi.fn(() => true))
        publishSession({
            id,
            token: 'token',
            deadline: Date.now() + 60_000,
            user,
        })
        loaded(user)

        render(<UserPage />)

        expect(await screen.findByRole('heading', { name: 'Mi cuenta' })).toBeInTheDocument()
        const username = screen.getAllByRole('textbox')[0]
        await userEventApi.clear(username)
        await userEventApi.type(username, 'pablo-nuevo')
        await userEventApi.click(screen.getByRole('button', { name: 'Guardar cambios' }))

        await waitFor(() => expect(update).toHaveBeenCalled())
        await userEventApi.click(screen.getByRole('button', { name: 'Eliminar mi cuenta' }))
        await waitFor(() => expect(remove).toHaveBeenCalledWith(id))
    })

    it('rechaza guardar una cuenta sin cambios y respeta cancelación de borrado', async () => {
        const userEventApi = userEvent.setup()
        const remove = vi.spyOn(api, 'deleteUser')
        vi.stubGlobal('confirm', vi.fn(() => false))
        render(
            <>
                <AccountForm id={id} own={false} user={user} onSaved={() => {}} />
                <DeleteAccount username="pablo" targetId={id} own={false} />
            </>,
        )

        await userEventApi.click(screen.getByRole('button', { name: 'Guardar cambios' }))
        expect(await screen.findByRole('alert')).toHaveTextContent(/cambios/)
        await userEventApi.click(screen.getByRole('button', { name: 'Eliminar esta cuenta' }))
        expect(remove).not.toHaveBeenCalled()
    })

    it('edita, elimina y rechaza suscripciones duplicadas', async () => {
        const userEventApi = userEvent.setup()
        const update = vi.spyOn(api, 'updateUser').mockResolvedValue(undefined)
        vi.stubGlobal('confirm', vi.fn(() => true))
        Element.prototype.scrollIntoView = vi.fn()
        render(<SubscriptionsSection id={id} user={user} onChanged={() => {}} />)

        await userEventApi.click(screen.getByRole('button', { name: 'Editar' }))
        await userEventApi.click(screen.getByRole('button', { name: 'Cancelar edición' }))
        await userEventApi.click(screen.getByRole('button', { name: 'Eliminar' }))
        await waitFor(() => expect(update).toHaveBeenCalled())

        cleanup()
        const save = vi.fn(async () => {})
        render(<SubscriptionEditor initial={subscription} onSave={save} />)
        await userEventApi.clear(screen.getByRole('textbox', { name: /IDs de líneas/ }))
        await userEventApi.type(screen.getByRole('textbox', { name: /IDs de líneas/ }), '3')
        await userEventApi.click(screen.getByRole('button', { name: 'Guardar alerta' }))
        await waitFor(() => expect(save).toHaveBeenCalled())
    })

    it('muestra perfiles ajenos sin roles proporcionados', () => {
        const other = { ...user, username: 'otro', roles: undefined }
        publishSession({
            id,
            token: 'token',
            deadline: Date.now() + 60_000,
            user,
        })
        loaded(other)

        render(<UserPage id="00000000-0000-4000-8000-000000000002" />)

        expect(screen.getByRole('heading', { name: 'Cuenta de otro' })).toBeInTheDocument()
        expect(screen.getByText(/Roles:.*No proporcionados por el backend/)).toBeInTheDocument()
    })
})

describe('páginas de incidencias', () => {
    it('renderiza listado, editor y detalle', () => {
        loaded([incidence])

        render(
            <>
                <IncidenceList />
                <IncidenceEditor initial={incidence} />
            </>,
        )

        expect(screen.getByRole('heading', { name: 'Incidencias' })).toBeInTheDocument()
        expect(screen.getByRole('button', { name: 'Guardar incidencia' })).toBeInTheDocument()
        expect(screen.getAllByText('Mantenimiento').length).toBeGreaterThan(0)

        cleanup()
        loaded(incidence)
        render(<IncidencePage id={id} />)

        expect(screen.getByRole('heading', { name: 'Qué está ocurriendo' })).toBeInTheDocument()
    })

    it('muestra el estado de error de las páginas', () => {
        resource.mockReturnValue({
            data: undefined,
            error: 'No disponible',
            loading: false,
            reload: vi.fn(),
        })

        render(
            <>
                <IncidenceList />
                <IncidencePage id={id} />
            </>,
        )

        expect(screen.getAllByRole('alert')).toHaveLength(2)
        expect(screen.getAllByRole('button', { name: 'Volver a cargar' })).toHaveLength(2)
    })

    it('envía la creación de una incidencia desde el editor', async () => {
        const user = userEvent.setup()
        const create = vi.spyOn(api, 'createIncidence').mockResolvedValue(id)

        render(<IncidenceEditor />)

        await user.type(screen.getByRole('textbox', { name: 'Nombre' }), 'Nueva')
        await user.type(screen.getByRole('textbox', { name: 'Descripción' }), 'Descripción')
        await user.type(screen.getByRole('textbox', { name: 'ID del mapa' }), '1')
        await user.type(screen.getByRole('textbox', { name: /IDs de líneas/ }), '4')
        await user.click(screen.getByRole('button', { name: 'Crear incidencia' }))

        await waitFor(() => expect(create).toHaveBeenCalled())
    })

    it('envía la actualización de una incidencia y permite cancelar acciones destructivas', async () => {
        const userEventApi = userEvent.setup()
        const update = vi.spyOn(api, 'updateIncidence').mockResolvedValue(undefined)
        const remove = vi.spyOn(api, 'deleteIncidence')
        vi.stubGlobal('confirm', vi.fn(() => false))

        publishSession({
            id,
            token: 'token',
            deadline: Date.now() + 60_000,
            user,
        })
        loaded(incidence)
        render(<IncidencePage id={id} />)

        expect(screen.getByRole('button', { name: 'Eliminar' })).toBeInTheDocument()
        await userEventApi.click(screen.getByRole('button', { name: 'Eliminar' }))
        expect(remove).not.toHaveBeenCalled()

        cleanup()
        render(<IncidenceEditor initial={incidence} />)
        await userEventApi.clear(screen.getByRole('textbox', { name: 'Nombre' }))
        await userEventApi.type(screen.getByRole('textbox', { name: 'Nombre' }), 'Actualizada')
        await userEventApi.click(screen.getByRole('button', { name: 'Guardar incidencia' }))
        await waitFor(() => expect(update).toHaveBeenCalledWith(incidence.id, expect.anything()))
    })

    it('aplica filtros y cambia de página en el listado', async () => {
        const userEventApi = userEvent.setup()
        const many = Array.from({ length: 11 }, (_, index) => ({
            ...incidence,
            id: `00000000-0000-4000-8000-${String(index + 10).padStart(12, '0')}`,
        }))
        loaded(many)

        render(<IncidenceList />)

        await userEventApi.type(screen.getByRole('searchbox', { name: 'Buscar' }), 'Mantenimiento')
        await userEventApi.click(screen.getByRole('button', { name: 'Siguiente' }))
        expect(screen.getByText('Página 2 de 2')).toBeInTheDocument()
        await userEventApi.click(screen.getByRole('button', { name: 'Limpiar filtros' }))
        expect(screen.getByRole('searchbox', { name: 'Buscar' })).toHaveValue('')
    })
})

describe('acceso y componentes de filtros', () => {
    it('muestra el formulario de registro y permite volver al acceso', () => {
        render(<Access register />)

        expect(screen.getByRole('heading', { name: 'Tu próximo viaje empieza aquí.' })).toBeInTheDocument()
        expect(screen.getByRole('link', { name: 'Inicia sesión' })).toHaveAttribute('href', '#/acceso')
    })

    it('completa un registro y muestra el error de login posterior', async () => {
        const userEventApi = userEvent.setup()
        const register = vi.spyOn(api, 'register').mockResolvedValue(id)
        const token = btoa(JSON.stringify({
            sub: id,
            roles: ['USER'],
            exp: Math.floor(Date.now() / 1000) + 900,
        }))
        const fetchMock = vi.fn()
            .mockResolvedValueOnce(new Response(JSON.stringify({
                accessToken: `header.${token}.signature`,
                tokenType: 'Bearer',
                expiresIn: 900,
            })))
            .mockResolvedValueOnce(new Response('{}', { status: 401 }))
        vi.stubGlobal('fetch', fetchMock)

        render(<Access register />)
        await userEventApi.type(screen.getByRole('textbox', { name: /Usuario/ }), 'nuevo')
        await userEventApi.type(document.querySelector('input[name="password"]')!, 'contraseña-segura')
        await userEventApi.type(screen.getByLabelText('Repetir contraseña'), 'contraseña-segura')
        await userEventApi.click(screen.getByRole('button', { name: 'Crear cuenta y entrar' }))

        await waitFor(() => expect(register).toHaveBeenCalled())
        expect(await screen.findByText(/Cuenta creada, pero no se pudo iniciar sesión/)).toBeInTheDocument()
        expect(screen.getByText(/Tu cuenta ya está creada/)).toBeInTheDocument()
    })

    it('muestra el acceso restringido para usuarios sin permisos', () => {
        publishSession({
            id,
            token: 'token',
            deadline: Date.now() + 60_000,
            user: { username: 'user', roles: ['USER'], alertSubscriptions: [] },
        })

        render(<Access />)

        expect(screen.getByRole('heading', { name: 'Ya has iniciado sesión' })).toBeInTheDocument()
        expect(screen.getByRole('link', { name: 'Ir a mi cuenta' })).toBeInTheDocument()
    })

    it('renderiza filtros y tabla vacía', async () => {
        const onChange = vi.fn()
        const onClear = vi.fn()
        const user = userEvent.setup()

        render(
            <>
                <IncidenceFilters filters={emptyFilters} onChange={onChange} onClear={onClear} />
                <IncidenceTable
                    rows={[]}
                    total={0}
                    filteredCount={0}
                    page={0}
                    lastPage={0}
                    onPageChange={vi.fn()}
                />
            </>,
        )

        await user.type(screen.getByRole('searchbox', { name: 'Buscar' }), 'texto')
        await user.click(screen.getByRole('button', { name: 'Limpiar filtros' }))
        expect(onChange).toHaveBeenCalledWith('text', 't')
        expect(onClear).toHaveBeenCalled()
        expect(screen.getByRole('heading', { name: 'No hay incidencias para esta consulta' })).toBeInTheDocument()
    })

    it('resuelve rutas dinámicas válidas', () => {
        publishSession({
            id,
            token: 'token',
            deadline: Date.now() + 60_000,
            user,
        })
        loaded(incidence)
        render(<><Routes path={`/incidencias/${id}`} /><Routes path={`/incidencias/${id}/editar`} /></>)

        expect(screen.getByRole('heading', { name: 'Qué está ocurriendo' })).toBeInTheDocument()
        expect(screen.getByRole('button', { name: 'Guardar incidencia' })).toBeInTheDocument()

        cleanup()
        loaded(user)
        render(<Routes path={`/admin/usuarios/${id}`} />)
        expect(screen.getByRole('heading', { name: 'Mi cuenta' })).toBeInTheDocument()
    })
})
