import { describe, expect, it } from 'vitest'

import { localInput } from '../../src/model'
import { buildIncidencePayload } from '../../src/pages/incidences/payload'

function form(values: Record<string, string>): FormData {
    const data = new FormData()

    for (const [key, value] of Object.entries(values)) data.set(key, value)

    return data
}

const valid = {
    name: 'Obras',
    summary: 'Trabajos en la vía',
    mapId: '4',
    lineIds: '1, 2, 1',
    cause: 'MAINTENANCE',
    timestamp: '2030-11-03T10:00:00',
    duration: 'PT15M',
}

describe('buildIncidencePayload', () => {
    it('construye el cuerpo normal de una incidencia', () => {
        expect(buildIncidencePayload(form(valid), {
            severity: 'LOW',
            instant: false,
        })).toEqual({
            affectedNetwork: { mapId: 4, lineIds: [1, 2] },
            occurrence: {
                timestamp: new Date('2030-11-03T10:00:00').toISOString(),
                duration: 'PT15M',
            },
            description: { name: 'Obras', summary: 'Trabajos en la vía' },
            classification: { severity: 'LOW', cause: 'MAINTENANCE' },
        })
    })

    it('fuerza duración cero para incidencias instantáneas e informativas', () => {
        expect(buildIncidencePayload(form({ ...valid, duration: 'PT1H' }), {
            severity: 'INFORMATIONAL',
            instant: false,
        }).occurrence.duration).toBe('PT0S')

        expect(buildIncidencePayload(form(valid), {
            severity: 'LOW',
            instant: true,
        }).occurrence.duration).toBe('PT0S')
    })

    it('mantiene el timestamp original cuando no cambia el campo local', () => {
        const initial = {
            id: '00000000-0000-4000-8000-000000000001',
            affectedNetwork: { mapId: 4, lineIds: [1] },
            occurrence: {
                timestamp: '2030-11-03T10:00:00.000Z',
                duration: 'PT15M',
            },
            description: { name: 'Obras', summary: 'Trabajos' },
            classification: { severity: 'LOW' as const, cause: 'MAINTENANCE' as const },
        }

        expect(buildIncidencePayload(form({
            ...valid,
            timestamp: localInput(initial.occurrence.timestamp),
        }), {
            initial,
            severity: 'LOW',
            instant: false,
        }).occurrence.timestamp).toBe(initial.occurrence.timestamp)
    })

    it('rechaza datos incompletos, causas desconocidas y duraciones incompatibles', () => {
        expect(() => buildIncidencePayload(form({ ...valid, name: '' }), {
            severity: 'LOW',
            instant: false,
        })).toThrow(/obligatorios/)

        expect(() => buildIncidencePayload(form({ ...valid, cause: 'UNKNOWN' }), {
            severity: 'LOW',
            instant: false,
        })).toThrow(/causa válida/)

        const initial = {
            id: '00000000-0000-4000-8000-000000000001',
            affectedNetwork: { mapId: 4, lineIds: [1] },
            occurrence: {
                timestamp: '2030-11-03T10:00:00.000Z',
                duration: 'PT0S',
            },
            description: { name: 'Aviso', summary: 'Información' },
            classification: { severity: 'INFORMATIONAL' as const, cause: 'OTHER' as const },
        }

        expect(() => buildIncidencePayload(form(valid), {
            initial,
            severity: 'LOW',
            instant: false,
        })).toThrow(/guarda primero/)
    })
})
