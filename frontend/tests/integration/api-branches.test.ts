import { afterEach, describe, expect, it, vi } from 'vitest'

import {
    api,
    ApiError,
    checkSession,
    getAuthState,
    loadIncidence,
    loadIncidences,
    logout,
    notify,
    request,
    subscribeAuth,
    updateCurrentUser,
    validateUser,
} from '../../src/api'
import { readIdentity, tokenDeadline } from '../../src/model/identity'
import { publishSession } from '../../src/api/session'
import { beginLogin, isCurrentLogin } from '../../src/api/session'

const ID = '00000000-0000-4000-8000-000000000001'
const OTHER_ID = '00000000-0000-4000-8000-000000000002'

const incidence = {
    id: ID,
    affectedNetwork: { mapId: 1, lineIds: [4] },
    occurrence: { timestamp: '2026-11-03T12:00:00Z', duration: 'PT15M' },
    description: { name: 'Mantenimiento', summary: 'Trabajos' },
    classification: { severity: 'LOW' as const, cause: 'MAINTENANCE' as const },
}

function response(body: unknown, status = 200) {
    return new Response(body === undefined ? null : JSON.stringify(body), {
        status,
        headers: { 'Content-Type': 'application/json' },
    })
}

function jwt(subject = ID, roles: string[] = ['USER'], exp?: number) {
    const payload = btoa(JSON.stringify({ sub: subject, roles, exp }))

    return `header.${payload}.signature`
}

afterEach(() => {
    logout('')
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
})

describe('validación de identidad y perfiles', () => {
    it('rechaza perfiles y JWT que no cumplen el contrato', () => {
        expect(() => validateUser(null as never)).toThrow(/perfil no válido/)
        expect(() => validateUser({
            username: 'user',
            alertSubscriptions: [],
            roles: 'ADMIN' as never,
        })).toThrow(/roles/)
        expect(() => readIdentity('no-es-un-jwt')).toThrow(/identificador/)
        expect(() => readIdentity(jwt('not-an-id'))).toThrow(/identificador/)
    })

    it('valida expiración, tipo de token y límites del JWT', () => {
        const now = 1_000_000
        const token = jwt(ID, ['ADMIN'], 1_100)

        expect(tokenDeadline({
            accessToken: token,
            tokenType: 'Bearer',
            expiresIn: 900,
        }, now)).toBe(1_100_000)

        expect(() => tokenDeadline({
            accessToken: token,
            tokenType: 'Basic',
            expiresIn: 900,
        })).toThrow(/autenticación/)

        expect(() => tokenDeadline({
            accessToken: token,
            tokenType: 'Bearer',
            expiresIn: 0,
        })).toThrow(/autenticación/)
    })
})

describe('paginación e incidencias', () => {
    it('combina páginas, elimina duplicados y detiene la búsqueda al encontrar el ID', async () => {
        const fetchMock = vi.fn()
            .mockResolvedValueOnce(response({
                content: [incidence],
                totalElements: 2,
            }))
            .mockResolvedValueOnce(response({
                content: [{ ...incidence, id: OTHER_ID }, incidence],
                totalElements: 2,
            }))

        vi.stubGlobal('fetch', fetchMock)

        await expect(loadIncidences(new AbortController().signal)).resolves.toEqual([
            incidence,
            { ...incidence, id: OTHER_ID },
        ])

        fetchMock.mockReset()
        fetchMock.mockResolvedValueOnce(response({
            content: [incidence],
            totalElements: 2,
        }))

        await expect(loadIncidences(
            new AbortController().signal,
            ID,
        )).resolves.toEqual([incidence])
        expect(fetchMock).toHaveBeenCalledTimes(1)
    })

    it('rechaza páginas repetidas y respuestas inválidas', async () => {
        vi.stubGlobal('fetch', vi.fn().mockImplementation(() => Promise.resolve(response({
            content: [incidence],
            totalElements: 2,
        }))))

        await expect(loadIncidences(new AbortController().signal))
            .rejects.toThrow(/repitió una página/)

        vi.stubGlobal('fetch', vi.fn().mockResolvedValue(response({
            content: {},
            totalElements: 0,
        })))

        await expect(loadIncidences(new AbortController().signal))
            .rejects.toThrow(/respuesta paginada/)
    })

    it('carga detalles administrativos y públicos y valida identificadores', async () => {
        const fetchMock = vi.fn()
            .mockResolvedValueOnce(response({
                ...incidence,
                description: undefined,
                incidenceDetails: incidence,
            }))
            .mockResolvedValueOnce(response({
                content: [incidence],
                totalElements: 1,
            }))

        vi.stubGlobal('fetch', fetchMock)
        publishSession({
            id: ID,
            token: jwt(),
            deadline: Date.now() + 60_000,
            user: { username: 'user', alertSubscriptions: [] },
        })

        await expect(loadIncidence(ID, true, new AbortController().signal))
            .resolves.toEqual(incidence)
        await expect(loadIncidence(ID, false, new AbortController().signal))
            .resolves.toEqual(incidence)
        await expect(loadIncidence('invalid', false, new AbortController().signal))
            .rejects.toThrow(/UUID/)
    })

    it('devuelve 404 para incidencias públicas ausentes', async () => {
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue(response({
            content: [],
            totalElements: 0,
        })))

        await expect(loadIncidence(ID, false, new AbortController().signal))
            .rejects.toMatchObject({ status: 404 })
    })
})

describe('operaciones de API', () => {
    it('construye las peticiones de usuarios e incidencias', async () => {
        const fetchMock = vi.fn()
            .mockResolvedValue(response('created-id', 201))
            .mockResolvedValue(response(undefined, 204))

        vi.stubGlobal('fetch', fetchMock)
        publishSession({
            id: ID,
            token: jwt(),
            deadline: Date.now() + 60_000,
            user: { username: 'user', alertSubscriptions: [] },
        })

        await api.register('new-user', 'password')
        await api.updateUser(ID, { username: 'changed' })
        await api.deleteUser(ID)
        await api.createIncidence(incidence)
        await api.updateIncidence(ID, incidence)
        await api.deleteIncidence(ID)

        expect(fetchMock).toHaveBeenCalledTimes(6)
        expect(fetchMock.mock.calls[0][1].method).toBe('POST')
        expect(fetchMock.mock.calls[1][1].method).toBe('PUT')
        expect(fetchMock.mock.calls[2][1].method).toBe('DELETE')
        expect(JSON.parse(fetchMock.mock.calls[4][1].body)).toEqual({
            uuid: ID,
            changeAffectedNetwork: incidence.affectedNetwork,
            changeOccurrence: incidence.occurrence,
            changeDescription: incidence.description,
            changeClassification: incidence.classification,
        })
    })
})

describe('HTTP y sesión', () => {
    it('mapea errores HTTP, respuestas no JSON y errores de red', async () => {
        const fetchMock = vi.fn()
            .mockResolvedValueOnce(response('texto', 400))
            .mockResolvedValueOnce(response(undefined, 500))
            .mockRejectedValueOnce(new Error('offline'))

        vi.stubGlobal('fetch', fetchMock)

        await expect(request('/bad', { public: true })).rejects.toMatchObject({ status: 400 })
        await expect(request('/server', { public: true })).rejects.toThrow(/backend/)
        await expect(request('/offline', { public: true })).rejects.toThrow(/conectar/)
    })

    it('notifica cambios, actualiza usuarios y caduca sesiones', () => {
        const listener = vi.fn()
        const unsubscribe = subscribeAuth(listener)
        const user = { username: 'user', alertSubscriptions: [] }

        notify('aviso')
        expect(getAuthState().notice).toBe('aviso')
        updateCurrentUser(ID, user)
        expect(listener).toHaveBeenCalled()
        unsubscribe()

        checkSession()
        expect(getAuthState().session).toBeNull()
        expect(() => { throw new ApiError(409, 'conflicto') }).toThrow('conflicto')
    })

    it('invalida intentos de login anteriores y actualiza solo la sesión correcta', () => {
        const first = beginLogin()
        const second = beginLogin()
        const user = { username: 'updated', alertSubscriptions: [] }

        expect(isCurrentLogin(first)).toBe(false)
        expect(isCurrentLogin(second)).toBe(true)
        updateCurrentUser(OTHER_ID, user)
        expect(getAuthState().session).toBeNull()
    })
})
