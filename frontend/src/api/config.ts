const DEFAULT_API_BASE_URL = 'https://localhost:8443'
const DEFAULT_PAGE_SIZE = 100
const LOOPBACK_HOSTS = new Set(['localhost', '127.0.0.1', '[::1]'])

function trimTrailingSlashes(value: string): string {
    let end = value.length

    while (end > 0 && value[end - 1] === '/') end--

    return value.slice(0, end)
}

/**
 * Valida la URL base de la API. El token viaja en la cabecera Authorization,
 * así que solo se admite HTTPS (o HTTP únicamente contra la propia máquina).
 */
export function resolveApiBaseUrl(configured: string | undefined): string {
    const candidate = configured || DEFAULT_API_BASE_URL
    let url: URL

    try {
        url = new URL(candidate)
    } catch (error) {
        throw new Error('VITE_API_BASE_URL no es una URL válida.', { cause: error })
    }

    const secure =
        url.protocol === 'https:' ||
        (url.protocol === 'http:' && LOOPBACK_HOSTS.has(url.hostname))

    if (!secure) {
        throw new Error('VITE_API_BASE_URL debe usar HTTPS (HTTP solo se admite en localhost).')
    }

    if (url.username || url.password) {
        throw new Error('VITE_API_BASE_URL no puede incluir credenciales.')
    }

    return trimTrailingSlashes(candidate)
}

export function resolvePageSize(configured: string | undefined): number {
    const size = Number(configured || DEFAULT_PAGE_SIZE)

    return Number.isSafeInteger(size) && size > 0 ? size : DEFAULT_PAGE_SIZE
}

export const API_BASE_URL = resolveApiBaseUrl(import.meta.env.VITE_API_BASE_URL)
export const PAGE_SIZE = resolvePageSize(import.meta.env.VITE_PAGE_SIZE)
