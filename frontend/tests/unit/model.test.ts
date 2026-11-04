import { describe, expect, it } from 'vitest'

import {
    defaultExpiration,
    durationSeconds,
    filterIncidences,
    emptyFilters,
    isUuid,
    parseId,
    parseIds,
    sameSubscription,
    subscriptionPatch,
    validatePassword,
    validateUsername,
} from '../../src/model'

import type {
    Incidence,
    Subscription,
} from '../../src/model'

const subscription: Subscription = {
    lines: [1, 2],
    startTime: '2026-11-03T07:00:00+01:00',
    endTime: '2026-11-03T09:00:00+01:00',
    daysOfTheWeek: ['MONDAY', 'TUESDAY'],
    expiration: '2030-12-03T10:00:00Z',
}

describe('Identificadores y credenciales', () => {
    it('valida UUID', () => {
        expect(
            isUuid('00000000-0000-4000-8000-000000000001'),
        ).toBe(true)

        expect(isUuid('incorrecto')).toBe(false)
    })

    it('deduplica líneas y rechaza IDs imprecisos', () => {
        expect(parseIds('1, 2, 1')).toEqual([1, 2])

        expect(() => parseIds('')).toThrow()
        expect(() => parseId('1.5')).toThrow()
        expect(() => parseId('9007199254740992')).toThrow()
    })

    it('aplica las restricciones del usuario', () => {
        expect(validateUsername('pablo.sainz')).toBe('pablo.sainz')

        expect(() => validateUsername('ab')).toThrow()
        expect(() => validateUsername('usuario con espacios')).toThrow()
        expect(() => validateUsername('x'.repeat(33))).toThrow()
    })

    it('aplica las restricciones de contraseña', () => {
        expect(
            validatePassword('contraseña-larga'),
        ).toBe('contraseña-larga')

        expect(() => validatePassword('corta')).toThrow()
        expect(() => validatePassword(' '.repeat(12))).toThrow()
        expect(() => validatePassword('x'.repeat(129))).toThrow()
    })
})

describe('Duraciones', () => {
    it('interpreta el formato java.time.Duration', () => {
        expect(durationSeconds('PT0S')).toBe(0)
        expect(durationSeconds('PT1H30M')).toBe(5400)
        expect(durationSeconds('P1D')).toBe(86400)
        expect(durationSeconds('PT0.5S')).toBe(0.5)
    })

    it('rechaza formatos incompletos y duraciones negativas', () => {
        const invalid = [
            'P',
            'PT',
            'P1DT',
            'P1M',
            '-PT1M',
            'texto',
        ]

        for (const value of invalid) {
            expect(() => durationSeconds(value)).toThrow()
        }
    })
})

describe('Suscripciones', () => {
    it('establece exactamente 30 días de expiración por defecto', () => {
        const now = new Date('2026-11-03T10:00:00Z')

        expect(defaultExpiration(now)).toBe(
            '2026-12-03T10:00:00.000Z',
        )
    })

    it('compara los conjuntos sin depender del orden', () => {
        const reordered: Subscription = {
            ...subscription,
            lines: [2, 1],
            daysOfTheWeek: ['TUESDAY', 'MONDAY'],
        }

        expect(
            sameSubscription(subscription, reordered),
        ).toBe(true)
    })

    it('no envía una actualización sin cambios', () => {
        expect(
            subscriptionPatch(subscription, { ...subscription }),
        ).toBeNull()
    })

    it('edita añadiendo el nuevo objeto y eliminando el original', () => {
        const next: Subscription = {
            ...subscription,
            lines: [3],
    }

        const patch = subscriptionPatch(subscription, next)

        expect(patch?.alertSubscriptionListToAdd).toEqual([next])

        expect(
            patch?.alertSubscriptionListToDelete?.[0],
    ).toBe(subscription)
    })
})

describe('Filtros', () => {
    const item: Incidence = {
        id: '00000000-0000-4000-8000-000000000001',
        affectedNetwork: {
            mapId: 1,
            lineIds: [4],
},
    occurrence: {
        timestamp: '2026-11-03T12:00:00Z',
            duration: 'PT15M',
    },
    description: {
        name: 'Mantenimiento',
            summary: 'Trabajos en la vía',
    },
    classification: {
        severity: 'LOW',
            cause: 'MAINTENANCE',
    },
}

    it('combina mapa y línea sin confundir mapas distintos', () => {
        const other: Incidence = {
            ...item,
            id: '00000000-0000-4000-8000-000000000002',
            affectedNetwork: {
                mapId: 2,
                lineIds: [4],
    },
    }

        expect(
            filterIncidences([item, other], {
                ...emptyFilters,
                map: '1',
                line: '4',
            }),
        ).toEqual([item])
    })

    it('busca por descripción y gravedad', () => {
        expect(
            filterIncidences([item], {
                ...emptyFilters,
                text: 'TRABAJOS',
                severity: 'LOW',
            }),
        ).toEqual([item])
    })
})
