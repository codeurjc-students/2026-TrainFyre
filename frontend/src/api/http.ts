import { API_BASE_URL } from './config'
import { checkSession, getAuthState, logout } from './session'

const REQUEST_TIMEOUT_MS = 25_000
const HTTP_UNAUTHORIZED = 401
const HTTP_SERVER_ERROR = 500

const HTTP_MESSAGES = new Map<number, string>([
    [400, 'Revisa los datos. El backend ha rechazado la solicitud.'],
    [401, 'Las credenciales son incorrectas o la sesión ya no es válida.'],
    [403, 'El backend no permite esta operación con tu cuenta.'],
    [404, 'No se ha encontrado el recurso.'],
    [409, 'Hay un conflicto: el usuario o la suscripción podrían existir ya.'],
    [413, 'El archivo supera el tamaño máximo permitido por el backend.'],
    [415, 'El backend no admite este formato de archivo.'],
    [429, 'Demasiadas solicitudes. Espera antes de volver a intentarlo.'],
])

export class ApiError extends Error {
    readonly status: number

    constructor(status: number, message: string) {
        super(message)
        this.name = 'ApiError'
        this.status = status
    }
}

function httpMessage(status: number): string {
    const known = HTTP_MESSAGES.get(status)

    if (known) return known

    return status >= HTTP_SERVER_ERROR
        ? 'El backend no pudo completar la operación. Comprueba su estado antes de reintentar.'
        : `La solicitud falló con HTTP ${status}.`
}

export interface RequestOptions {
    method?: string
    body?: unknown
    signal?: AbortSignal
    public?: boolean
    token?: string
    /** `blob` para descargar ficheros binarios (por defecto, JSON). */
    responseType?: 'json' | 'blob'
}

function resolveToken(options: RequestOptions): string | undefined {
    if (options.public) return undefined

    const token = options.token ?? getAuthState().session?.token

    if (!token) {
        throw new ApiError(HTTP_UNAUTHORIZED, 'Inicia sesión para realizar esta operación.')
    }

    return token
}

/** Une la cancelación del llamante con un tiempo máximo de espera. */
function withTimeout(external?: AbortSignal) {
    const controller = new AbortController()
    const abort = () => controller.abort()

    if (external?.aborted) controller.abort()
    external?.addEventListener('abort', abort, { once: true })

    const timeout = setTimeout(abort, REQUEST_TIMEOUT_MS)

    return {
        signal: controller.signal,
        dispose() {
            clearTimeout(timeout)
            external?.removeEventListener('abort', abort)
        },
    }
}

/**
 * Un `FormData` se envía tal cual: el navegador fija el `Content-Type`
 * multipart con su boundary. Cualquier otro cuerpo se serializa como JSON.
 */
function encodeBody(body: unknown, headers: Record<string, string>): BodyInit | undefined {
    if (body === undefined) return undefined
    if (body instanceof FormData) return body

    headers['Content-Type'] = 'application/json'

    return JSON.stringify(body)
}

async function readBody(response: Response, responseType: RequestOptions['responseType']): Promise<unknown> {
    if (responseType === 'blob') return response.blob()

    const raw = await response.text()

    if (!raw) return undefined

    try {
        return JSON.parse(raw)
    } catch {
        return raw
    }
}

function failure(status: number, token: string | undefined): ApiError {
    if (
        status === HTTP_UNAUTHORIZED &&
        token &&
        getAuthState().session?.token === token
    ) {
        logout('Tu sesión ya no es válida. Vuelve a iniciar sesión.')
    }

    return new ApiError(status, httpMessage(status))
}

function networkFailure(timedOut: boolean): ApiError {
    return new ApiError(
        0,
        timedOut
            ? 'La solicitud agotó el tiempo de espera. Comprueba el estado antes de repetir una operación.'
            : 'No se pudo conectar con la API. Revisa el backend, el certificado HTTPS y CORS.',
    )
}

export async function request<T>(
    path: string,
    options: RequestOptions = {},
): Promise<T> {
    checkSession()

    const token = resolveToken(options)
    const { signal, dispose } = withTimeout(options.signal)
    const headers: Record<string, string> = {
        Accept: options.responseType === 'blob' ? '*/*' : 'application/json',
    }

    if (token) headers.Authorization = `Bearer ${token}`

    const body = encodeBody(options.body, headers)

    try {
        const response = await fetch(`${API_BASE_URL}${path}`, {
            method: options.method ?? 'GET',
            headers,
            body,
            signal,
            credentials: 'omit',
            cache: 'no-store',
        })

        const data = await readBody(response, options.responseType)

        if (!response.ok) throw failure(response.status, token)

        return data as T
    } catch (error) {
        if (error instanceof ApiError) throw error
        if (options.signal?.aborted) throw error

        throw networkFailure(signal.aborted)
    } finally {
        dispose()
    }
}
