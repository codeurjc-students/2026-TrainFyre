import { describe, expect, it } from 'vitest'

import {
    durationLabel,
    stripZone,
    subscriptionKey,
    validateNewCredentials,
} from '../../src/model'

import type { Subscription } from '../../src/model'

function form(values: Record<string, string>): FormData {
    const data = new FormData()

    for (const [key, value] of Object.entries(values)) data.set(key, value)

    return data
}

describe('stripZone', () => {
    it('elimina la región solo cuando está al final', () => {
        expect(stripZone('2026-11-03T10:00:00+01:00[Europe/Madrid]')).toBe('2026-11-03T10:00:00+01:00')
        expect(stripZone('2026-11-03T10:00:00Z')).toBe('2026-11-03T10:00:00Z')
        expect(stripZone('[]')).toBe('[]')
        expect(stripZone('')).toBe('')
    })

    it('mantiene un coste lineal con entradas hostiles', () => {
        const started = performance.now()

        stripZone('['.repeat(200_000))

        expect(performance.now() - started).toBeLessThan(500)
    })
})

describe('durationLabel', () => {
    it('describe duraciones y devuelve el texto original si no es válido', () => {
        expect(durationLabel('PT0S')).toBe('Sin retraso')
        expect(durationLabel('PT1H30M')).toBe('1 h 30 min')
        expect(durationLabel('basura')).toBe('basura')
    })
})

describe('subscriptionKey', () => {
    it('no depende del orden de líneas ni de días', () => {
        const base: Subscription = {
            lines: [2, 1],
            startTime: '2026-11-03T07:00:00+01:00',
            endTime: '2026-11-03T09:00:00+01:00',
            daysOfTheWeek: ['TUESDAY', 'MONDAY'],
            expiration: '2030-12-03T10:00:00Z',
        }

        expect(subscriptionKey(base)).toBe(
            subscriptionKey({ ...base, lines: [1, 2], daysOfTheWeek: ['MONDAY', 'TUESDAY'] }),
        )
    })
})

describe('validateNewCredentials', () => {
    const secret = 'x'.repeat(16)
    const valid = { username: 'usuario.1', password: secret, confirmation: secret }

    it('devuelve las credenciales válidas', () => {
        expect(validateNewCredentials(form(valid))).toEqual({
            username: 'usuario.1',
            password: secret,
        })
    })

    it('rechaza contraseñas que no coinciden', () => {
        expect(() => validateNewCredentials(form({ ...valid, confirmation: 'otra' }))).toThrow(/no coinciden/)
    })

    it('rechaza usuarios y contraseñas inválidos', () => {
        expect(() => validateNewCredentials(form({ ...valid, username: 'a b' }))).toThrow()
        expect(() => validateNewCredentials(form({ ...valid, password: 'corta', confirmation: 'corta' }))).toThrow()
    })
})
