import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import {
    api,
    ApiError,
    getAvatarVersion,
    logout,
    subscribeAvatar,
} from '../../src/api'
import { publishSession } from '../../src/api/session'

const USER_ID = '00000000-0000-4000-8000-000000000001'
const AVATARS_URL = 'https://api.test/api/v1/avatars'

const PNG_BYTES = [0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3]

function imageResponse(type = 'image/png', status = 200) {
    return new Response(new Uint8Array(PNG_BYTES), {
        status,
        headers: { 'Content-Type': type },
    })
}

function lastCall(fetchMock: ReturnType<typeof vi.fn>) {
    return fetchMock.mock.calls.at(-1)! as [string, RequestInit & { headers: Record<string, string> }]
}

let fetchMock: ReturnType<typeof vi.fn>

beforeEach(() => {
    fetchMock = vi.fn()
    vi.stubGlobal('fetch', fetchMock)
    publishSession({
        id: USER_ID,
        token: 'token',
        deadline: Date.now() + 60_000,
        user: { username: 'pablo', alertSubscriptions: [] },
    })
})

afterEach(() => {
    logout('')
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
})

describe('Descarga de avatares', () => {
    it('descarga el binario autenticado y lo devuelve como Blob', async () => {
        fetchMock.mockResolvedValueOnce(imageResponse())

        const blob = await api.avatar('me')
        const [url, options] = lastCall(fetchMock)

        expect(url).toBe(`${AVATARS_URL}/me`)
        expect(options.method).toBe('GET')
        expect(options.headers.Authorization).toBe('Bearer token')
        expect(options.headers.Accept).toBe('*/*')
        expect(blob?.type).toBe('image/png')
        expect(blob?.size).toBe(PNG_BYTES.length)
    })

    it('codifica el identificador en la ruta', async () => {
        fetchMock.mockResolvedValueOnce(imageResponse())

        await api.avatar('a/b')

        expect(lastCall(fetchMock)[0]).toBe(`${AVATARS_URL}/a%2Fb`)
    })

    it('devuelve null si el usuario no tiene avatar (404 o cuerpo vacío)', async () => {
        fetchMock
            .mockResolvedValueOnce(new Response('{}', { status: 404 }))
            .mockResolvedValueOnce(new Response(null, { status: 200 }))

        await expect(api.avatar(USER_ID)).resolves.toBeNull()
        await expect(api.avatar(USER_ID)).resolves.toBeNull()
    })

    it('acepta la imagen aunque el backend no indique un Content-Type de imagen', async () => {
        fetchMock.mockResolvedValueOnce(imageResponse('application/octet-stream'))

        const blob = await api.avatar(USER_ID)

        expect(blob?.type).toBe('image/png')
    })

    it('rechaza contenidos que no son imágenes admitidas', async () => {
        fetchMock.mockResolvedValueOnce(new Response('{"error":"x"}', {
            headers: { 'Content-Type': 'application/json' },
        }))

        await expect(api.avatar(USER_ID)).rejects.toThrow('imagen de perfil no válida')
    })

    it('propaga los errores que no son "no encontrado"', async () => {
        fetchMock.mockResolvedValueOnce(new Response('{}', { status: 500 }))

        await expect(api.avatar(USER_ID)).rejects.toMatchObject({
            name: 'ApiError',
            status: 500,
        })
    })

    it('exige sesión', async () => {
        logout('')

        await expect(api.avatar('me')).rejects.toBeInstanceOf(ApiError)
        expect(fetchMock).not.toHaveBeenCalled()
    })
})

describe('Subida y borrado de avatares', () => {
    it('sube el archivo como multipart sin fijar el Content-Type y avisa del cambio', async () => {
        const listener = vi.fn()
        const unsubscribe = subscribeAvatar(listener)
        const before = getAvatarVersion()
        const file = new File(['png'], 'foto.png', { type: 'image/png' })

        fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }))

        await api.uploadAvatar('me', file)

        const [url, options] = lastCall(fetchMock)
        const body = options.body as FormData

        expect(url).toBe(`${AVATARS_URL}/me`)
        expect(options.method).toBe('PUT')
        expect(options.headers['Content-Type']).toBeUndefined()
        expect(options.headers.Authorization).toBe('Bearer token')
        expect(body).toBeInstanceOf(FormData)
        expect((body.get('file') as File).name).toBe('foto.png')
        expect(listener).toHaveBeenCalledOnce()
        expect(getAvatarVersion()).toBe(before + 1)

        unsubscribe()
    })

    it('no avisa del cambio si la subida falla y traduce 413 y 415', async () => {
        const listener = vi.fn()
        const unsubscribe = subscribeAvatar(listener)
        const file = new File(['png'], 'foto.png', { type: 'image/png' })

        fetchMock
            .mockResolvedValueOnce(new Response('{}', { status: 413 }))
            .mockResolvedValueOnce(new Response('{}', { status: 415 }))

        await expect(api.uploadAvatar(USER_ID, file)).rejects.toThrow('tamaño máximo')
        await expect(api.uploadAvatar(USER_ID, file)).rejects.toThrow('formato')
        expect(listener).not.toHaveBeenCalled()

        unsubscribe()
    })

    it('elimina el avatar y avisa del cambio', async () => {
        const listener = vi.fn()
        const unsubscribe = subscribeAvatar(listener)

        fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }))

        await api.deleteAvatar(USER_ID)

        const [url, options] = lastCall(fetchMock)

        expect(url).toBe(`${AVATARS_URL}/${USER_ID}`)
        expect(options.method).toBe('DELETE')
        expect(listener).toHaveBeenCalledOnce()

        unsubscribe()
    })

    it('deja de avisar a los oyentes dados de baja', async () => {
        const listener = vi.fn()

        subscribeAvatar(listener)()
        fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }))

        await api.deleteAvatar(USER_ID)

        expect(listener).not.toHaveBeenCalled()
    })

    it('mantiene intacto el envío JSON de las demás peticiones', async () => {
        fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }))

        await api.updateUser(USER_ID, { username: 'nuevo' })

        const [, options] = lastCall(fetchMock)

        expect(options.headers['Content-Type']).toBe('application/json')
        expect(options.headers.Accept).toBe('application/json')
        expect(options.body).toBe(JSON.stringify({ username: 'nuevo' }))
    })
})
