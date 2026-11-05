import {
    causeLabels,
    durationSeconds,
    localInput,
    localToIso,
    parseId,
    parseIds,
    text,
} from '../../model'

import type {
    Cause,
    Incidence,
    IncidenceDetails,
    Severity,
} from '../../model'

interface PayloadContext {
    readonly initial?: Incidence
    readonly severity: Severity
    readonly instant: boolean
}

function readCause(data: FormData): Cause {
    const cause = text(data, 'cause')

    if (!Object.hasOwn(causeLabels, cause)) {
        throw new Error('Selecciona una causa válida.')
    }

    return cause as Cause
}

function readTimestamp(data: FormData, initial?: Incidence): string {
    const input = text(data, 'timestamp')

    return initial && input === localInput(initial.occurrence.timestamp)
        ? initial.occurrence.timestamp
        : localToIso(input)
}

/** Valida el formulario de incidencia y construye el cuerpo que espera la API. */
export function buildIncidencePayload(
    data: FormData,
    { initial, severity, instant }: PayloadContext,
): IncidenceDetails {
    const name = text(data, 'name').trim()
    const summary = text(data, 'summary').trim()

    if (!name || !summary) {
        throw new Error('El nombre y la descripción son obligatorios.')
    }

    const duration =
        instant || severity === 'INFORMATIONAL'
            ? 'PT0S'
            : text(data, 'duration').trim()

    const seconds = durationSeconds(duration)
    const timestamp = readTimestamp(data, initial)

    if (
        initial?.classification.severity === 'INFORMATIONAL' &&
        severity !== 'INFORMATIONAL' &&
        seconds > 0
    ) {
        throw new Error(
            'Por el orden de actualización del backend, guarda primero la nueva gravedad con duración cero. Después podrás editar su duración.',
        )
    }

    return {
        affectedNetwork: {
            mapId: parseId(text(data, 'mapId')),
            lineIds: parseIds(text(data, 'lineIds')),
        },
        occurrence: {
            timestamp,
            duration,
            // instantaneous es calculado por Java a partir de duration.
        },
        description: { name, summary },
        classification: {
            severity,
            cause: readCause(data),
        },
    }
}
