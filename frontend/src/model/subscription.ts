import {
    clock,
    defaultExpiration,
    localInput,
    localOffset,
    localToIso,
    normalizeClock,
    stripZone,
} from './datetime'
import { dayLabels } from './labels'
import { parseIds, text } from './validation'

import type { Day, Subscription, UpdateUser } from './types'

const ZONE_OFFSET_SUFFIX = /(Z|[+-]\d{2}:\d{2})$/
const DATE_LENGTH = 10

function recurringTime(value: string, original?: string): string {
    const time = normalizeClock(value)

    if (!original) {
        const now = new Date()

        return `${localInput(now.toISOString()).slice(0, DATE_LENGTH)}T${time}${localOffset(now)}`
    }

    // Mantener el valor original evita perder precisión o información de zona.
    if (clock(original) === time) return original

    const withoutRegion = stripZone(original)
    const offset = ZONE_OFFSET_SUFFIX.exec(withoutRegion)?.[1]
    const region = original.slice(withoutRegion.length)

    if (!offset) {
        throw new Error('La suscripción original no contiene una zona horaria válida.')
    }

    return `${original.slice(0, DATE_LENGTH)}T${time}${offset}${region}`
}

function resolveExpiration(input: string, original?: Subscription): string {
    if (original && input === localInput(original.expiration)) {
        return original.expiration
    }

    return input ? localToIso(input) : defaultExpiration()
}

function readDays(data: FormData): Day[] {
    const days = data.getAll('days').map(String) as Day[]

    if (!days.length || days.some(day => !Object.hasOwn(dayLabels, day))) {
        throw new Error('Selecciona al menos un día válido.')
    }

    return [...new Set(days)]
}

export function makeSubscription(
    data: FormData,
    original?: Subscription,
): Subscription {
    const start = normalizeClock(text(data, 'startTime'))
    const end = normalizeClock(text(data, 'endTime'))

    if (end < start) {
        throw new Error(
            'El backend actual no admite franjas que crucen medianoche. Crea dos suscripciones.',
        )
    }

    const days = readDays(data)
    const expiration = resolveExpiration(text(data, 'expiration'), original)

    if (new Date(stripZone(expiration)).getTime() <= Date.now()) {
        throw new Error('La expiración debe estar en el futuro.')
    }

    return {
        lines: parseIds(text(data, 'lines')),
        startTime: recurringTime(start, original?.startTime),
        endTime: recurringTime(end, original?.endTime),
        daysOfTheWeek: days,
        expiration,
    }
}

function canonical(value: Subscription): Subscription {
    return {
        ...value,
        lines: [...value.lines].sort((x, y) => x - y),
        daysOfTheWeek: [...value.daysOfTheWeek].sort((x, y) => x.localeCompare(y)),
    }
}

export function sameSubscription(a: Subscription, b: Subscription): boolean {
    return JSON.stringify(canonical(a)) === JSON.stringify(canonical(b))
}

/** Clave estable y comparable de una suscripción (independiente del orden). */
export function subscriptionKey(value: Subscription): string {
    return JSON.stringify(canonical(value))
}

export function subscriptionPatch(
    original: Subscription | undefined,
    next: Subscription,
): UpdateUser | null {
    if (original && sameSubscription(original, next)) return null

    return {
        alertSubscriptionListToAdd: [next],
        ...(original ? { alertSubscriptionListToDelete: [original] } : {}),
    }
}
