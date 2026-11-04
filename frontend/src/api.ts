import {
    isUuid,
    normalizeIncidence,
    readIdentity,
    tokenDeadline,
} from './model'

import type {
    Incidence,
    IncidenceDetails,
    Page,
    TokenResponse,
    UpdateUser,
    UserDetails,
} from './model'

export const API_BASE_URL = (
    import.meta.env.VITE_API_BASE_URL || 'https://localhost:8443'
).replace(/\/+$/, '')

const configuredSize = Number(import.meta.env.VITE_PAGE_SIZE || 100)

const PAGE_SIZE =
    Number.isSafeInteger(configuredSize) && configuredSize > 0
        ? configuredSize
        : 100

export interface Session {
    id: string
    token: string
    deadline: number
    user: UserDetails
}

interface AuthState {
    session: Session | null
    notice: string
}

let state: AuthState = { session: null, notice: '' }
let expirationTimer: ReturnType<typeof setTimeout> | undefined
let authGeneration = 0

const listeners = new Set<() => void>()

export const getAuthState = () => state

export function subscribeAuth(listener: () => void) {
    listeners.add(listener)
    return () => { listeners.delete(listener) }
}

function emit(next: AuthState) {
    state = next
    listeners.forEach(listener => listener())
}

export function notify(notice: string) {
    emit({ ...state, notice })
}

export function logout(notice = 'Has cerrado la sesión.') {
    authGeneration++
    clearTimeout(expirationTimer)
    emit({ session: null, notice })
}

export function checkSession() {
    if (state.session && Date.now() >= state.session.deadline) {
        logout('Tu sesión ha caducado. Vuelve a iniciar sesión.')
    }
}

function armExpiration() {
    clearTimeout(expirationTimer)

    if (!state.session) return

    const delay = Math.min(
        Math.max(state.session.deadline - Date.now(), 0),
        2147483647,
    )

    expirationTimer = setTimeout(() => {
        checkSession()
        if (state.session) armExpiration()
    }, delay)
}

export function updateCurrentUser(id: string, user: UserDetails) {
    const current = state.session

    if (!current || current.id !== id) return

    emit({
        ...state,
        session: {
            ...current,
            user: { ...user, roles: user.roles ?? current.user.roles },
        },
    })
}

export class ApiError extends Error {
    status: number

    constructor(status: number, message: string) {
        super(message)
        this.name = 'ApiError'
        this.status = status
    }
}

function httpMessage(status: number): string {
    const messages: Record<number, string> = {
        400: 'Revisa los datos. El backend ha rechazado la solicitud.',
        401: 'Las credenciales son incorrectas o la sesión ya no es válida.',
        403: 'El backend no permite esta operación con tu cuenta.',
        404: 'No se ha encontrado el recurso.',
        409: 'Hay un conflicto: el usuario o la suscripción podrían existir ya.',
        429: 'Demasiadas solicitudes. Espera antes de volver a intentarlo.',
    }

    return messages[status] ?? (
        status >= 500
            ? 'El backend no pudo completar la operación. Comprueba su estado antes de reintentar.'
            : `La solicitud falló con HTTP ${status}.`
    )
}

interface RequestOptions {
    method?: string
    body?: unknown
    signal?: AbortSignal
    public?: boolean
    token?: string
}

export async function request<T>(
    path: string,
    options: RequestOptions = {},
): Promise<T> {
    checkSession()

    const token = options.public
        ? undefined
        : options.token ?? state.session?.token

    if (!options.public && !token) {
        throw new ApiError(401, 'Inicia sesión para realizar esta operación.')
    }

    const controller = new AbortController()
    const abort = () => controller.abort()

    if (options.signal?.aborted) controller.abort()
    options.signal?.addEventListener('abort', abort, { once: true })

    const timeout = setTimeout(() => controller.abort(), 25000)
    const headers: Record<string, string> = {
        Accept: 'application/json',
    }

    if (token) headers.Authorization = `Bearer ${token}`
    if (options.body !== undefined) headers['Content-Type'] = 'application/json'

    try {
        const response = await fetch(`${API_BASE_URL}${path}`, {
            method: options.method ?? 'GET',
            headers,
            body: options.body === undefined ? undefined : JSON.stringify(options.body),
            signal: controller.signal,
            credentials: 'omit',
            cache: 'no-store',
        })

        const raw = await response.text()
        let data: unknown

        try {
            data = raw ? JSON.parse(raw) : undefined
        } catch {
            data = raw
        }

        if (!response.ok) {
            if (
                response.status === 401 &&
                token &&
                state.session?.token === token
            ) {
                logout('Tu sesión ya no es válida. Vuelve a iniciar sesión.')
            }

            throw new ApiError(response.status, httpMessage(response.status))
        }

        return data as T
    } catch (error) {
        if (error instanceof ApiError) throw error
        if (options.signal?.aborted) throw error

        throw new ApiError(
            0,
            controller.signal.aborted
                ? 'La solicitud agotó el tiempo de espera. Comprueba el estado antes de repetir una operación.'
                : 'No se pudo conectar con la API. Revisa el backend, el certificado HTTPS y CORS.',
        )
    } finally {
        clearTimeout(timeout)
        options.signal?.removeEventListener('abort', abort)
    }
}

function validateUser(user: UserDetails): UserDetails {
    if (
        !user ||
        typeof user.username !== 'string' ||
        !Array.isArray(user.alertSubscriptions)
    ) {
        throw new Error('El backend devolvió un perfil no válido.')
    }

    if (user.roles !== undefined && !Array.isArray(user.roles)) {
        throw new Error('El campo roles debe ser un array.')
    }

    return user
}

export async function login(username: string, password: string): Promise<void> {
    const attempt = ++authGeneration

    const response = await request<TokenResponse>('/auth/login', {
        method: 'POST',
        public: true,
        body: { username, password },
    })

    const identity = readIdentity(response.accessToken)
    const deadline = tokenDeadline(response)

    // No se publica la sesión hasta recuperar correctamente el perfil.
    const user = validateUser(await request<UserDetails>('/users/me', {
        token: response.accessToken,
    }))

    if (attempt !== authGeneration) {
        throw new Error('El inicio de sesión se ha cancelado.')
    }

    if (deadline <= Date.now()) {
        throw new Error('La sesión caducó durante el inicio de sesión. Inténtalo de nuevo.')
    }

    emit({
        notice: '',
        session: {
            id: identity.id,
            token: response.accessToken,
            deadline,
            user: { ...user, roles: user.roles ?? identity.roles },
        },
    })

    armExpiration()
}

export const api = {
    register(username: string, password: string) {
        return request<string>('/users', {
            method: 'POST',
            public: true,
            body: { username, password },
        })
    },

    async user(id: string, signal?: AbortSignal) {
        const user = await request<UserDetails>(
            `/users/${encodeURIComponent(id)}`,
            { signal },
        )

        return validateUser(user)
    },

    updateUser(id: string, body: UpdateUser) {
        return request<void>(`/users/${encodeURIComponent(id)}`, {
            method: 'PUT',
            body,
        })
    },

    deleteUser(id: string) {
        return request<void>(`/users/${encodeURIComponent(id)}`, {
            method: 'DELETE',
        })
    },

    createIncidence(body: IncidenceDetails) {
        return request<string>('/incidence', {
            method: 'POST',
            body,
        })
    },

    updateIncidence(id: string, body: IncidenceDetails) {
        return request<void>('/incidence', {
            method: 'PUT',
            body: {
                uuid: id,
                changeAffectedNetwork: body.affectedNetwork,
                changeOccurrence: body.occurrence,
                changeDescription: body.description,
                changeClassification: body.classification,
            },
        })
    },

    deleteIncidence(id: string) {
        return request<void>('/incidence', {
            method: 'DELETE',
            body: { id },
        })
    },
}

export async function loadIncidences(
    signal: AbortSignal,
    findId?: string,
): Promise<Incidence[]> {
    const rows: Incidence[] = []
    const seen = new Set<string>()

    for (let page = 0; ; page++) {
        const response = await request<Page<Incidence>>(
            `/incidence?page=${page}&size=${PAGE_SIZE}`,
            { signal, public: true },
        )

        if (
            !response ||
            !Array.isArray(response.content) ||
            !Number.isSafeInteger(response.totalElements) ||
            response.totalElements < 0
        ) {
            throw new Error('La API devolvió una respuesta paginada no válida.')
        }

        if (signal.aborted) throw new DOMException('Cancelado', 'AbortError')

        if (!response.content.length) {
            if (rows.length < response.totalElements) {
                throw new Error(
                    'El listado cambió durante la descarga o la paginación es inconsistente. Actualiza la consulta.',
                )
            }

            return rows
        }

        let added = 0

        for (const raw of response.content) {
            const item = normalizeIncidence(raw)

            if (!seen.has(item.id)) {
                rows.push(item)
                seen.add(item.id)
                added++
            }
        }

        if (findId && seen.has(findId)) return rows
        if (rows.length >= response.totalElements) return rows

        if (!added) {
            throw new Error(
                'El backend repitió una página. Se ha detenido la descarga para evitar un bucle.',
            )
        }
    }
}

export async function loadIncidence(
    id: string,
    admin: boolean,
    signal: AbortSignal,
): Promise<Incidence> {
    if (!isUuid(id)) throw new Error('El UUID de la incidencia no es válido.')

    if (admin) {
        const details = await request<IncidenceDetails>(
            `/incidence/${encodeURIComponent(id)}`,
            { signal },
        )

        return normalizeIncidence({ ...details, id })
    }

    // El backend actual protege GET /incidence/{id}.
    // El detalle público se obtiene del listado público, que ya contiene sus datos.
    const rows = await loadIncidences(signal, id)
    const result = rows.find(row => row.id === id)

    if (!result) throw new ApiError(404, 'La incidencia no existe o ya no está disponible.')

    return result
}
