import { isUuid, normalizeIncidence } from '../model'
import { PAGE_SIZE } from './config'
import { ApiError, request } from './http'
import { validateUser } from './auth'

import type {
    Incidence,
    IncidenceDetails,
    Page,
    UpdateUser,
    UserDetails,
} from '../model'

const encode = encodeURIComponent
const INCIDENCE_PATH = '/incidence'
const USERS_PATH = '/users'

export const api = {
    register(username: string, password: string) {
        return request<string>(USERS_PATH, {
            method: 'POST',
            public: true,
            body: { username, password },
        })
    },

    async user(id: string, signal?: AbortSignal) {
        const user = await request<UserDetails>(`${USERS_PATH}/${encode(id)}`, { signal })

        return validateUser(user)
    },

    updateUser(id: string, body: UpdateUser) {
        return request<void>(`${USERS_PATH}/${encode(id)}`, {
            method: 'PUT',
            body,
        })
    },

    deleteUser(id: string) {
        return request<void>(`${USERS_PATH}/${encode(id)}`, {
            method: 'DELETE',
        })
    },

    createIncidence(body: IncidenceDetails) {
        return request<string>(INCIDENCE_PATH, {
            method: 'POST',
            body,
        })
    },

    updateIncidence(id: string, body: IncidenceDetails) {
        return request<void>(INCIDENCE_PATH, {
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
        return request<void>(INCIDENCE_PATH, {
            method: 'DELETE',
            body: { id },
        })
    },
}

async function fetchPage(
    page: number,
    signal: AbortSignal,
): Promise<Page<Incidence>> {
    const response = await request<Page<Incidence>>(
        `${INCIDENCE_PATH}?page=${page}&size=${PAGE_SIZE}`,
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

    return response
}

/** Añade los elementos nuevos a `rows` y devuelve cuántos se añadieron. */
function appendNew(
    rows: Incidence[],
    seen: Set<string>,
    content: Incidence[],
): number {
    let added = 0

    for (const raw of content) {
        const item = normalizeIncidence(raw)

        if (!seen.has(item.id)) {
            rows.push(item)
            seen.add(item.id)
            added++
        }
    }

    return added
}

export async function loadIncidences(
    signal: AbortSignal,
    findId?: string,
): Promise<Incidence[]> {
    const rows: Incidence[] = []
    const seen = new Set<string>()

    for (let page = 0; ; page++) {
        const response = await fetchPage(page, signal)

        if (!response.content.length) {
            if (rows.length < response.totalElements) {
                throw new Error(
                    'El listado cambió durante la descarga o la paginación es inconsistente. Actualiza la consulta.',
                )
            }

            return rows
        }

        const added = appendNew(rows, seen, response.content)

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
            `${INCIDENCE_PATH}/${encode(id)}`,
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
