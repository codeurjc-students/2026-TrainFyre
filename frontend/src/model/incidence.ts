import { localInput } from './datetime'
import { isUuid } from './validation'

import type { Filters, Incidence, IncidenceDetails } from './types'

export function normalizeIncidence(
    raw: Incidence & { incidenceDetails?: IncidenceDetails },
): Incidence {
    const details = raw.incidenceDetails ?? raw

    if (
        !isUuid(raw.id) ||
        !details.affectedNetwork ||
        !details.occurrence ||
        !details.description ||
        !details.classification ||
        !Array.isArray(details.affectedNetwork.lineIds)
    ) {
        throw new Error('La API devolvió una incidencia incompleta.')
    }

    if (
        !Number.isSafeInteger(details.affectedNetwork.mapId) ||
        !details.affectedNetwork.lineIds.every(id => Number.isSafeInteger(id))
    ) {
        throw new Error('La API devolvió IDs fuera del rango numérico seguro del navegador.')
    }

    return { ...details, id: raw.id }
}

export function filterIncidences(rows: Incidence[], filters: Filters): Incidence[] {
    return rows.filter(row => {
        const date = localInput(row.occurrence.timestamp).slice(0, 10)
        const description =
            `${row.description.name} ${row.description.summary}`.toLocaleLowerCase('es')

        return (
            (!filters.text || description.includes(filters.text.toLocaleLowerCase('es'))) &&
            (!filters.map || String(row.affectedNetwork.mapId) === filters.map.trim()) &&
            (!filters.line || row.affectedNetwork.lineIds.some(
                id => String(id) === filters.line.trim(),
            )) &&
            (!filters.severity || row.classification.severity === filters.severity) &&
            (!filters.cause || row.classification.cause === filters.cause) &&
            (!filters.from || date >= filters.from) &&
            (!filters.to || date <= filters.to)
        )
    })
}
