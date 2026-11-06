import { describe, expect, it } from 'vitest'

import {
    clock,
    durationLabel,
    localInput,
    localToIso,
    makeSubscription,
    stripZone,
    subscriptionKey,
    validateNewCredentials,
} from '../../src/model'
import { localOffset, normalizeClock } from '../../src/model/datetime'

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

    describe('fechas y suscripciones', () => {
        function subscriptionForm(values: Record<string, string>): FormData {
            const data = new FormData()

            for (const [key, value] of Object.entries(values)) data.set(key, value)
            data.set('days', 'MONDAY')
            data.append('days', 'MONDAY')

            return data
        }

        it('convierte fechas y horas locales y rechaza valores inválidos', () => {
            const iso = localToIso('2099-11-03T10:20:30')

            expect(localInput(iso)).toBe('2099-11-03T10:20:30')
            expect(clock(`${iso}[Europe/Madrid]`)).toBe('09:20:30')
            expect(clock('sin hora')).toBe('00:00:00')
            expect(normalizeClock('10:20')).toBe('10:20:00')
            expect(localOffset(new Date('2026-01-01T00:00:00Z'))).toMatch(/^[+-]\d{2}:\d{2}$/)
            expect(() => localToIso('')).toThrow()
            expect(() => normalizeClock('25:00')).toThrow()
            expect(localInput('no es una fecha')).toBe('')
        })

        it('crea y edita suscripciones, deduplicando días', () => {
            const data = subscriptionForm({
                lines: '1, 2, 1',
                startTime: '07:00',
                endTime: '09:00:00',
                expiration: '2099-11-03T10:20:30',
            })

            const created = makeSubscription(data)

            expect(created.lines).toEqual([1, 2])
            expect(created.daysOfTheWeek).toEqual(['MONDAY'])
            expect(created.startTime).toContain('T07:00:00')

            const edited = makeSubscription(data, {
                ...created,
                startTime: '2099-11-03T07:00:00+01:00[Europe/Madrid]',
                endTime: '2099-11-03T09:00:00+01:00[Europe/Madrid]',
                expiration: '2099-11-03T09:20:30.000Z',
            })

            expect(edited.startTime).toContain('T07:00:00')
            expect(edited.expiration).toBe('2099-11-03T09:20:30.000Z')
        })

        it('rechaza franjas, días y expiraciones no válidas', () => {
            const base = {
                lines: '1',
                startTime: '10:00',
                endTime: '09:00',
                expiration: '2099-11-03T10:20:30',
            }

            expect(() => makeSubscription(subscriptionForm(base))).toThrow(/medianoche/)
            expect(() => makeSubscription(subscriptionForm({
                ...base,
                startTime: '07:00',
                endTime: '09:00',
                expiration: '2020-01-01T10:20:30',
            }))).toThrow(/futuro/)

            const noDays = new FormData()
            noDays.set('lines', '1')
            noDays.set('startTime', '07:00')
            noDays.set('endTime', '09:00')
            noDays.set('expiration', '2099-11-03T10:20:30')
            expect(() => makeSubscription(noDays)).toThrow(/día/)
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
