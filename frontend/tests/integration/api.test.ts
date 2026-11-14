import {
    afterEach,
    describe,
    expect,
    it,
    vi,
} from 'vitest'

import {
    api,
    getAuthState,
    loadIncidences,
    login,
    logout,
    request,
} from '../../src/api'

const USER_ID = '00000000-0000-4000-8000-000000000001'

function token() {
    const payload = btoa(JSON.stringify({
        sub: USER_ID,
        roles: ['USER'],
        exp: Math.floor(Date.now() / 1000) + 900,
    }))

    // Token ficticio: solo se usa con fetch simulado.
    return `header.${payload}.signature`
}

async function authenticate(
    fetchMock: ReturnType<typeof vi.fn>,
) {
    fetchMock
        .mockResolvedValueOnce(
            new Response(JSON.stringify({
                accessToken: token(),
                tokenType: 'Bearer',
                expiresIn: 900,
            })),
        )
        .mockResolvedValueOnce(
            new Response(JSON.stringify({
                username: 'pablo',
                roles: ['USER'],
                alertSubscriptions: [],
            })),
        )

    await login('pablo', 'contraseña-de-prueba')
}

afterEach(() => {
    logout('')
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
})

describe('Cliente HTTP', () => {
    it('inicia sesión y envía DELETE de incidencia con cuerpo JSON', async () => {
        const fetchMock = vi.fn()
        vi.stubGlobal('fetch', fetchMock)

        await authenticate(fetchMock)

        fetchMock.mockResolvedValueOnce(
            new Response(null, { status: 204 }),
        )

        await api.deleteIncidence(USER_ID)

        const [url, options] = fetchMock.mock.calls.at(-1)!

        expect(url).toBe('https://api.test/api/v1/incidence')
        expect(options.method).toBe('DELETE')

        expect(
            JSON.parse(options.body),
        ).toEqual({ id: USER_ID })

        expect(
            options.headers.Authorization,
        ).toMatch(/^Bearer /)
    })

    it('no envía el token al consultar un recurso público', async () => {
        const fetchMock = vi.fn()
        vi.stubGlobal('fetch', fetchMock)

        await authenticate(fetchMock)

        fetchMock.mockResolvedValueOnce(
            new Response('{}'),
        )

        await request('/incidence?page=0&size=2', {
            public: true,
        })

        const options = fetchMock.mock.calls.at(-1)![1]

        expect(
            options.headers.Authorization,
        ).toBeUndefined()
    })

    it('elimina la sesión cuando el backend responde 401', async () => {
        const fetchMock = vi.fn()
        vi.stubGlobal('fetch', fetchMock)

        await authenticate(fetchMock)

        fetchMock.mockResolvedValueOnce(
            new Response('{}', { status: 401 }),
        )

        await expect(
            api.user('me'),
        ).rejects.toThrow()

        expect(getAuthState().session).toBeNull()
    })

    it('rechaza páginas vacías prematuras', async () => {
        const fetchMock = vi.fn().mockResolvedValue(
            new Response(JSON.stringify({
                content: [],
                page: 0,
                size: 2,
                totalElements: 1,
            })),
        )

        vi.stubGlobal('fetch', fetchMock)

        await expect(
            loadIncidences(new AbortController().signal),
        ).rejects.toThrow('paginación es inconsistente')
    })

    it('no conserva una sesión si falla la obtención del perfil', async () => {
        const fetchMock = vi.fn()
            .mockResolvedValueOnce(
                new Response(JSON.stringify({
                    accessToken: token(),
                    tokenType: 'Bearer',
                    expiresIn: 900,
                })),
            )
            .mockResolvedValueOnce(
                new Response('{}', { status: 403 }),
            )

        vi.stubGlobal('fetch', fetchMock)

        await expect(
            login('pablo', 'contraseña-de-prueba'),
        ).rejects.toThrow()

        expect(getAuthState().session).toBeNull()
    })
})
